package com.example.readability.ui

import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.DynamicLayout
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.TextPaint
import android.text.style.LineHeightSpan
import android.text.style.RelativeSizeSpan
import androidx.annotation.Px
import androidx.core.content.res.ResourcesCompat
import com.example.readability.R
import com.example.readability.ReadabilityApplication
import com.example.readability.ui.models.BookData
import com.example.readability.ui.models.PageSplitData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.lang.System.lineSeparator

// https://stackoverflow.com/questions/33105161/how-to-increase-the-spacing-between-paragraphs-in-a-textview
class ParagraphSpacingSpan(
    @Px @androidx.annotation.IntRange(from = 1) val lineHeight: Int,
    @Px @androidx.annotation.IntRange(from = 0) val spacing: Int
) : LineHeightSpan {

    override fun chooseHeight(
        t: CharSequence, s: Int, e: Int, v: Int, l: Int, fm: Paint.FontMetricsInt
    ) {
        val textHeight: Int = fm.descent - fm.ascent

        fm.descent = textHeight - lineHeight + spacing
        fm.ascent = 0
    }
}

class PageSplitter {
    var textSize = MutableStateFlow(24f)
    var lineHeight = MutableStateFlow(1.2f)
    var letterSpacing = MutableStateFlow(0f)
    var paragraphSpacing = MutableStateFlow(1.2f)
    var fontFamily = MutableStateFlow("garamond")

    companion object {
        val fontMap = mapOf<String, Int>(
            Pair("garamond", R.font.garamond),
            Pair("gabarito", R.font.gabarito),
        )
    }

    private var customTypeFace: Typeface? = null
    private var customTypeFaceName: String? = null
    fun buildDynamicLayout(
        base: CharSequence, textPaint: TextPaint, width: Int
    ): DynamicLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            DynamicLayout.Builder.obtain(base, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL).setLineSpacing(0f, lineHeight.value)
                .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE).build()
        } else {
            DynamicLayout(base, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
        }
    }

    fun buildTextPaint(): TextPaint {
        if (customTypeFace == null || customTypeFaceName != fontFamily.value) {
            val context = ReadabilityApplication.instance!!.applicationContext
            customTypeFace = ResourcesCompat.getFont(context, fontMap[fontFamily.value]!!)
            customTypeFaceName = fontFamily.value
        }
        val density = Resources.getSystem().displayMetrics.density
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize.value * density
        textPaint.typeface = customTypeFace
        textPaint.letterSpacing = letterSpacing.value
        return textPaint
    }

    suspend fun splitPage(
        width: Int, height: Int, bookData: BookData, setPageSplitData: (PageSplitData) -> Unit
    ) {
        withContext(Dispatchers.Default) {
            val textPaint = buildTextPaint()
            val density = Resources.getSystem().displayMetrics.density
            val lineHeightPx = (lineHeight.value * textSize.value * density).toInt()
            val base = SpannableStringBuilder(bookData.content)
            // TODO: too slow
            Regex(lineSeparator()).findAll(bookData.content).forEachIndexed { index, result ->
//                val span = ParagraphSpacingSpan(lineHeightPx, paragraphSpacing.value.toInt())
//
//                val separator = SpannableString(lineSeparator())
//                separator.setSpan(span, 0, separator.length, SPAN_EXCLUSIVE_EXCLUSIVE)
//
//                base.insert(result.range.last + 1 + index * separator.length, separator)
                if (result.range.last > bookData.content.length) {
                    return@forEachIndexed
                }
                base.setSpan(
                    RelativeSizeSpan(paragraphSpacing.value),
                    result.range.last,
                    result.range.last + 1,
                    SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            val dynamicLayout = buildDynamicLayout(base, textPaint, width)

            val pageSplitData = PageSplitData(
                pageSplits = listOf(), width = width, height = height
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

        }
    }
}