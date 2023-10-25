package com.example.shareader.ui

import android.content.res.Resources
import android.os.Build
import android.text.DynamicLayout
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.TextPaint
import com.example.shareader.ui.models.BookData
import com.example.shareader.ui.models.PageSplitData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class PageSplitter {
    companion object {
        fun buildDynamicLayout(base: CharSequence, textPaint: TextPaint, width: Int): DynamicLayout {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                DynamicLayout.Builder.obtain(base, textPaint, width)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(0f, 1f)
                    .setIncludePad(false)
                    .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
                    .build()
            } else {
                DynamicLayout(base, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
            }
        }
    }

    suspend fun splitPage (width: Int, height: Int, bookData: BookData, setBookData : (BookData) -> Unit) {
        withContext(Dispatchers.Default) {
            val density = Resources.getSystem().displayMetrics.density
            val textPaint = TextPaint()
            textPaint.isAntiAlias = true
            textPaint.textSize = 24f * density

            var base = SpannableStringBuilder()
            var dynamicLayout = buildDynamicLayout(base, textPaint, width)

            val content = bookData.content
            val pageSplitData = PageSplitData(
                pageSplits = listOf(),
                width = width,
                height = height
            )

            var lastLength = 0
            content.split(" ").let { tokens ->
                tokens.mapIndexed { index, token ->
                    if (index != tokens.size - 1) {
                        "$token "
                    } else {
                        token
                    }
                }.forEach { token ->
                    base.append(token)
                    if (dynamicLayout.height > height) {
                        base.delete(base.length - token.length, base.length)
                        // split
                        pageSplitData.pageSplits += base.length + lastLength
                        lastLength += base.length
                        setBookData(bookData.copy(
                            pageSplitData = pageSplitData.copy()
                        ))
                        base = SpannableStringBuilder()
                        base.append(token)
                        dynamicLayout = buildDynamicLayout(base, textPaint, width)
                        if (!isActive) {
                            return@forEach
                        }
                    }
                }
            }

            pageSplitData.pageSplits += content.length
            setBookData(bookData.copy(
                pageSplitData = pageSplitData.copy()
            ))
        }
    }
}