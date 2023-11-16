package com.example.readability.ui

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Build
import android.text.DynamicLayout
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextPaint
import android.text.style.RelativeSizeSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import com.example.readability.R
import com.example.readability.ReadabilityApplication
import com.example.readability.ui.models.BookData
import com.example.readability.ui.models.PageSplitData
import com.example.readability.ui.theme.md_theme_dark_background
import com.example.readability.ui.theme.md_theme_dark_onBackground
import com.example.readability.ui.theme.md_theme_light_background
import com.example.readability.ui.theme.md_theme_light_onBackground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.System.lineSeparator

data class ViewerStyle(
    val textSize: Float = 24f,
    val lineHeight: Float = 1.2f,
    val letterSpacing: Float = 0f,
    val paragraphSpacing: Float = 1.2f,
    val fontFamily: String = "garamond",
    val verticalPadding: Float = 16f,
    val horizontalPadding: Float = 16f,
    val brightBackgroundColor: Color = md_theme_light_background,
    val darkBackgroundColor: Color = md_theme_dark_background,
    val brightTextColor: Color = md_theme_light_onBackground,
    val darkTextColor: Color = md_theme_dark_onBackground,
)

class PageSplitter {
    private var _viewerStyle = MutableStateFlow(ViewerStyle())
    val viewerStyle = _viewerStyle.asStateFlow()
    private var charWidthArray = FloatArray(65536)
    private val cacheCharScope = CoroutineScope(Dispatchers.Default)
    private var cacheCharJob: Job? = null
    private val everyChars = CharArray(65536) { it.toChar() }

    private class TextDrawer(
        val textPaint: TextPaint,
        val content: String,
        val canvas: Canvas,
    ) {
        fun drawText(start: Int, end: Int, x: Float, y: Float) {
            canvas.drawText(content, start, end, x, y, textPaint)
        }
    }

    private fun cacheCharWidthArray() {
        val textPaint = buildTextPaint()
        textPaint.getTextWidths(everyChars, 0, 128, charWidthArray)
        charWidthArray['\n'.code] = Float.POSITIVE_INFINITY
        charWidthArray['\r'.code] = 0f
        if (cacheCharJob != null) {
            cacheCharJob!!.cancel()
        }
        cacheCharJob = cacheCharScope.launch {
            val startTime = System.nanoTime()
            val buffer = FloatArray(128)
            for (i in 128 until 65536 step 128) {
                textPaint.getTextWidths(everyChars, i, 128, buffer)
                for (j in 0 until 128) {
                    charWidthArray[i + j] = buffer[j]
                }
                if (!isActive) {
                    break
                }
            }
            charWidthArray['\n'.code] = Float.POSITIVE_INFINITY
            charWidthArray['\r'.code] = 0f
            println("cache char time = ${(System.nanoTime() - startTime) / 1000000f}ms")
        }
    }

    fun setViewerStyle(viewerStyle: ViewerStyle) {
        val lastFontFamily = this.viewerStyle.value.fontFamily
        val lastTextSize = this.viewerStyle.value.textSize
        val lastLetterSpacing = this.viewerStyle.value.letterSpacing
        _viewerStyle.value = viewerStyle
        if (viewerStyle.fontFamily != lastFontFamily || viewerStyle.textSize != lastTextSize || viewerStyle.letterSpacing != lastLetterSpacing) {
            // cache charWidthArray
            cacheCharWidthArray()
        }
    }

    init {
        cacheCharWidthArray()
    }

    companion object {
        const val USE_NATIVE_SPLIT = true
        const val USE_NATIVE_PAGE_DRAW = true
        val fontMap = mapOf<String, Int>(
            Pair("garamond", R.font.garamond),
            Pair("gabarito", R.font.gabarito),
        )

        init {
            System.loadLibrary("readability")
        }
    }

    private external fun splitPageNative(
        content: String,
        charWidths: FloatArray,
        lineHeight: Float,
        width: Float,
        height: Float,
        paragraphSpacing: Float
    ): IntArray

    private external fun drawPageNative(
        content: String,
        charWidths: FloatArray,
        lineHeight: Float,
        offset: Float,
        width: Float,
        paragraphSpacing: Float,
        textDrawer: TextDrawer
    )

    private var customTypeFace: Typeface? = null
    private var customTypeFaceName: String? = null
    fun buildDynamicLayout(
        base: CharSequence, textPaint: TextPaint, width: Int
    ): DynamicLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DynamicLayout.Builder.obtain(base, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, viewerStyle.value.lineHeight)
                .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE).build()
        } else {
            DynamicLayout(base, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
        }
    }

    fun buildTextPaint(): TextPaint {
        if (customTypeFace == null || customTypeFaceName != viewerStyle.value.fontFamily) {
            ReadabilityApplication.instance?.let {
                val context = it.applicationContext
                customTypeFace =
                    ResourcesCompat.getFont(context, fontMap[viewerStyle.value.fontFamily]!!)
                customTypeFaceName = viewerStyle.value.fontFamily
            }
        }
        val density = Resources.getSystem().displayMetrics.density
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = viewerStyle.value.textSize * density
        textPaint.typeface = customTypeFace
        textPaint.letterSpacing = viewerStyle.value.letterSpacing
        return textPaint
    }

    fun appendParagraphSpacing(base: SpannableStringBuilder) {
        val content = base.toString()
        Regex(lineSeparator()).findAll(content).forEachIndexed { index, result ->
            if (result.range.last > content.length) {
                return@forEachIndexed
            }
            base.setSpan(
                RelativeSizeSpan(
                    (viewerStyle.value.paragraphSpacing / (viewerStyle.value.lineHeight * viewerStyle.value.textSize)) + 1f
                ), result.range.last, result.range.last + 1, SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun drawPage(
        canvas: Canvas, bookData: BookData, page: Int, isDarkMode: Boolean, waitCache: Boolean = true
    ) {
        val content = bookData.content.subSequence(
            if (page == 0) 0 else bookData.pageSplitData!!.pageSplits[page - 1],
            bookData.pageSplitData!!.pageSplits[page]
        )
        val width = bookData.pageSplitData!!.width
        val textPaint = buildTextPaint()
        val lineHeight =
            (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top + textPaint.fontMetrics.leading) * viewerStyle.value.lineHeight
        val offset = -textPaint.fontMetrics.top

        val widths = FloatArray(content.length)
        val indices = IntArray(content.length)
        val isWhitespaces = BooleanArray(content.length)

        // preprocess

        val startTime = System.nanoTime()
        if (waitCache) {
            runBlocking {
                cacheCharJob?.join()
            }
        }

        if (USE_NATIVE_PAGE_DRAW) {
            textPaint.color = (if (isDarkMode) viewerStyle.value.darkTextColor else viewerStyle.value.brightTextColor).toArgb()
            val textDrawer = TextDrawer(textPaint, content.toString(), canvas)
            val density = Resources.getSystem().displayMetrics.density
            drawPageNative(
                content.toString(),
                charWidthArray,
                lineHeight,
                offset,
                width.toFloat(),
                viewerStyle.value.paragraphSpacing,
                textDrawer
            )
        } else {
            var i = 0
            var wordWidth = 0f
            var lastWhitespace = false
            var dataIndex = 0
            val len = content.length
            while (i < len) {
                val c = content[i]
                val cInt = c.code
                val isWhitespace = c.isWhitespace()

                if (!lastWhitespace && isWhitespace) {
                    // end of word
                    widths[dataIndex] = wordWidth
                    indices[dataIndex] = i
                    isWhitespaces[dataIndex] = false
                    dataIndex++
                } else if (lastWhitespace && !isWhitespace) {
                    // start of word
                    wordWidth = charWidthArray[cInt]
                } else {
                    wordWidth += charWidthArray[cInt]
                }

                if (isWhitespace) {
                    widths[dataIndex] = charWidthArray[cInt]
                    indices[dataIndex] = i + 1
                    isWhitespaces[dataIndex] = true
                    dataIndex++
                }

                lastWhitespace = isWhitespace
                i++
            }
            if (!lastWhitespace) {
                widths[dataIndex] = wordWidth
                indices[dataIndex] = i
                isWhitespaces[dataIndex] = false
                dataIndex++
            }

            // align
            i = 0
            val dataLen = dataIndex
            var x = 0f
            var y = 0f
            val pageRange = mutableListOf<Int>()
            while (i < dataLen) {
                val wordWidth = widths[i]
                val isWhitespace = isWhitespaces[i]
                val lastIndex = if (i == 0) 0 else indices[i - 1]
                val index = indices[i]

                if (x + wordWidth > width) { // overflow
                    if (isWhitespace) {
                        y += lineHeight
                        x = 0f
                    } else {
                        if (wordWidth > width) {
                            var wordPartialIndex = 0
                            val wordIndexMax = index - lastIndex
                            while (wordPartialIndex < wordIndexMax) {
                                val lastWordPartialIndex = wordPartialIndex
                                var remainingWidth = width - x
                                while (wordPartialIndex < wordIndexMax) {
                                    if (remainingWidth - charWidthArray[content[lastIndex + wordPartialIndex].code] < 0) {
                                        break
                                    }
                                    remainingWidth -= charWidthArray[content[lastIndex + wordPartialIndex].code]
                                    wordPartialIndex++
                                }
                                canvas.drawText(
                                    content,
                                    lastIndex + lastWordPartialIndex,
                                    lastIndex + wordPartialIndex,
                                    x,
                                    y + offset,
                                    textPaint
                                )
                                if (wordPartialIndex < wordIndexMax) {
                                    x = 0f
                                    y += lineHeight
                                }
                            }
                        } else {
                            y += lineHeight
                            canvas.drawText(content, lastIndex, index, 0f, y + offset, textPaint)
                            x = wordWidth
                        }
                    }
                } else { // not overflow
                    canvas.drawText(content, lastIndex, index, x, y + offset, textPaint)
                    x += wordWidth
                }

                i++
            }
        }

        println("page draw time = ${(System.nanoTime() - startTime) / 1000000f}ms, USE_NATIVE = $USE_NATIVE_PAGE_DRAW")
    }

    private suspend fun splitPageManual(
        width: Int, height: Int, bookData: BookData, setPageSplitData: (PageSplitData) -> Unit
    ) {
        withContext(Dispatchers.Default) {
            val pageSplitData = PageSplitData(
                pageSplits = listOf(),
                width = width,
                height = height,
                viewerStyle = viewerStyle.value
            )
            val content = bookData.content
            val textPaint = buildTextPaint()
            val lineHeight =
                (textPaint.fontMetrics.bottom - textPaint.fontMetrics.top + textPaint.fontMetrics.leading) * viewerStyle.value.lineHeight

            val startTime = System.nanoTime()
            cacheCharJob?.join()

            if (USE_NATIVE_SPLIT) {
                val density = Resources.getSystem().displayMetrics.density
                setPageSplitData(
                    pageSplitData.copy(
                        pageSplits = splitPageNative(
                            content,
                            charWidthArray,
                            lineHeight,
                            width.toFloat(),
                            height.toFloat(),
                            viewerStyle.value.paragraphSpacing
                        ).toMutableList()
                    )
                )
            } else {
                val widths = FloatArray(content.length)
                val indices = IntArray(content.length)
                val isWhitespaces = BooleanArray(content.length)
                var i = 0
                var dataIndex = 0
                var wordWidth = 0f
                var lastWhitespace = false

                val len = content.length
                while (isActive && i < len) {
                    val c = content[i]
                    val cInt = c.code
                    val isWhitespace = c.isWhitespace()

                    if (!lastWhitespace && isWhitespace) {
                        // end of word
                        widths[dataIndex] = wordWidth
                        indices[dataIndex] = i
                        isWhitespaces[dataIndex] = false
                        dataIndex++
                    } else if (lastWhitespace && !isWhitespace) {
                        // start of word
                        wordWidth = charWidthArray[cInt]
                    } else {
                        wordWidth += charWidthArray[cInt]
                    }

                    if (isWhitespace) {
                        widths[dataIndex] = charWidthArray[cInt]
                        indices[dataIndex] = i + 1
                        isWhitespaces[dataIndex] = true
                        dataIndex++
                    }

                    lastWhitespace = isWhitespace
                    i++
                }
                if (!lastWhitespace) {
                    // end of word
                    widths[dataIndex] = wordWidth
                    indices[dataIndex] = i
                    isWhitespaces[dataIndex] = false
                    dataIndex++
                }

                val pageRange = mutableListOf<Int>()
                val dataLen = dataIndex
                var x = 0f
                var y = lineHeight
                i = 0
                while (isActive && i < dataLen) {
                    val wordWidth = widths[i]
                    val isWhitespace = isWhitespaces[i]
                    val index = indices[i]
                    val lastIndex = if (i == 0) 0 else indices[i - 1]

                    if (x + wordWidth > width) { // overflow
                        if (isWhitespace) {
                            y += lineHeight
                            x = 0f
                            if (y > height) {
                                pageRange.add(index)
                                y = lineHeight
                            }
                        } else {
                            if (wordWidth > width) {
                                var wordPartialIndex = 0
                                val wordIndexMax = index - lastIndex
                                while (wordPartialIndex < wordIndexMax) {
                                    var remainingWidth = width - x
                                    while (wordPartialIndex < wordIndexMax) {
                                        if (remainingWidth - charWidthArray[content[lastIndex + wordPartialIndex].code] < 0) {
                                            break
                                        }
                                        remainingWidth -= charWidthArray[content[lastIndex + wordPartialIndex].code]
                                        wordPartialIndex++
                                    }
                                    if (wordPartialIndex < wordIndexMax) {
                                        x = 0f
                                        y += lineHeight
                                        if (y > height) {
                                            pageRange.add(lastIndex + wordPartialIndex)
                                            y = 0f
                                        }
                                    }
                                }
                            } else {
                                y += lineHeight
                                x = wordWidth
                                if (y > height) {
                                    pageRange.add(lastIndex)
                                    y = lineHeight
                                }
                            }
                        }
                    } else { // not overflow
                        x += wordWidth
                    }

                    i++
                }
                if (pageRange.lastOrNull() != content.length) {
                    pageRange.add(content.length)
                }
                setPageSplitData(pageSplitData.copy(pageSplits = pageRange))
            }
            println("caching time = ${(System.nanoTime() - startTime) / 1000000f}ms, USE_NATIVE = $USE_NATIVE_SPLIT")

        }
    }

    suspend fun splitPageDynamicLayout(
        width: Int, height: Int, bookData: BookData, setPageSplitData: (PageSplitData) -> Unit
    ) {
        withContext(Dispatchers.Default) {
            val textPaint = buildTextPaint()
            val density = Resources.getSystem().displayMetrics.density
            val base = SpannableStringBuilder(bookData.content)
            val startTime = System.nanoTime()
            // TODO: too slow
            appendParagraphSpacing(base)

            val dynamicLayout = buildDynamicLayout(base, textPaint, width)

            val pageSplitData = PageSplitData(
                pageSplits = listOf(),
                width = width,
                height = height,
                viewerStyle = viewerStyle.value
            )
            var heightOffset = 0
            var line = 0

            while (isActive) {
                var isOverflow = false
                while (line < dynamicLayout.lineCount - 1) {
                    val isNextLineOverflow =
                        dynamicLayout.getLineBottom(line + 1) >= height + heightOffset
                    if (isNextLineOverflow) {
                        isOverflow = true
                        break
                    } else {
                        line++
                    }
                }
                if (!isOverflow) {
                    setPageSplitData(pageSplitData)
                    break
                }
                pageSplitData.pageSplits += dynamicLayout.getLineEnd(line)
                setPageSplitData(pageSplitData)
                heightOffset = dynamicLayout.getLineBottom(line)

            }

            println("dynamic layout time = ${(System.nanoTime() - startTime) / 1000000f}ms")
        }
    }

    suspend fun splitPage(
        width: Int, height: Int, bookData: BookData, setPageSplitData: (PageSplitData) -> Unit
    ) {
        splitPageManual(width, height, bookData, setPageSplitData)
    }
}