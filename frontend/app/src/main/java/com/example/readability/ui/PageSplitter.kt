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

    suspend fun splitPage (width: Int, height: Int, bookData: BookData, setBookData : (BookData) -> Unit) {
        withContext(Dispatchers.Default) {
            val textPaint = buildTextPaint()

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
                            return@let
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