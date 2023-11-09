package com.example.readability.ui

import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.text.DynamicLayout
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.readability.R
import com.example.readability.ReadabilityApplication
import com.example.readability.ui.models.BookData
import com.example.readability.ui.models.PageSplitData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class PageSplitter {
    companion object {
        private var customTypeFace: Typeface? = null
        fun buildDynamicLayout(
            base: CharSequence,
            textPaint: TextPaint,
            width: Int
        ): DynamicLayout {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                DynamicLayout.Builder.obtain(base, textPaint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1f)
                    .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
                    .build()
            } else {
                DynamicLayout(base, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true)
            }
        }

        fun buildTextPaint(): TextPaint {
            if (customTypeFace == null) {
                val context = ReadabilityApplication.instance!!.applicationContext
                customTypeFace = ResourcesCompat.getFont(context, R.font.garamond)
            }
            val density = Resources.getSystem().displayMetrics.density
            val textPaint = TextPaint()
            textPaint.isAntiAlias = true
            textPaint.textSize = 24f * density
            textPaint.typeface = customTypeFace
            return textPaint
        }
    }

    suspend fun splitPage(
        width: Int,
        height: Int,
        bookData: BookData,
        setPageSplitData: (PageSplitData) -> Unit
    ) {
        withContext(Dispatchers.Default) {
            val textPaint = buildTextPaint()
            val base = SpannableStringBuilder()
            val dynamicLayout = buildDynamicLayout(base, textPaint, width)

            val pageSplitData = PageSplitData(
                pageSplits = listOf(),
                width = width,
                height = height
            )

//            val chunkSize =
//                (width * height / (textPaint.textSize * textPaint.textSize) * 75).toInt()
            val chunkSize = bookData.content.length
            var heightOffset = 0
            var line = 0
            var charOffset = 0

            while (isActive) {
                val lastLine = line
                var isOverflow = false
                while (line < dynamicLayout.lineCount - 1) {
                    val isNextLineOverflow = dynamicLayout.getLineBottom(line + 1) >= height + heightOffset
                    if (isNextLineOverflow) {
                        isOverflow = true
                        break;
                    } else {
                        line++
                    }
                }
                if (!isOverflow) {
                    // if line is not changed, add more characters
                    if (charOffset == bookData.content.length) {
                        pageSplitData.pageSplits += dynamicLayout.getLineEnd(line)
                        setPageSplitData(pageSplitData)
                        break
                    } else if (charOffset + chunkSize < bookData.content.length) {
                        base.append(bookData.content.substring(charOffset, charOffset + chunkSize))
                        charOffset += chunkSize
                        line = lastLine
                    } else {
                        base.append(bookData.content.substring(charOffset, bookData.content.length))
                        charOffset = bookData.content.length
                        line = lastLine
                    }
                    continue
                }
//                println("line: $line")
                pageSplitData.pageSplits += dynamicLayout.getLineEnd(line)
                setPageSplitData(pageSplitData)
                heightOffset = dynamicLayout.getLineBottom(line)
//                println("heightOffset: $heightOffset")

            }

        }
    }
}