package com.example.readability.data.viewer

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextPaint
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.example.readability.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for font data, including char width array and text paint
 * @param context application context
 */
@Singleton
class FontDataSource @Inject constructor(
    @ApplicationContext val context: Context,
) {
    companion object {
        val fontMap = mapOf<String, Int>(
            Pair("garamond", R.font.garamond),
            Pair("gabarito", R.font.gabarito),
        )
    }

    // char width array with 16dp text size and 0 letter spacing
    private val charWidthReferenceArray = FloatArray(65536)

    // char width array of most recent text size and letter spacing
    private val charWidthArray = FloatArray(65536)
    private val everyChars = CharArray(65536) { it.toChar() }

    private var lastTextSize = 0f
    private var lastLetterSpacing = 0f
    var customTypeface: MutableStateFlow<Typeface?> = MutableStateFlow(null)

    // line height with 16dp text size
    var referenceLineHeight: MutableStateFlow<Float> = MutableStateFlow(0f)
    private var customTypefaceName = ""

    init {
        customTypefaceName = "garamond"
        customTypeface.value = ResourcesCompat.getFont(context, R.font.garamond)
        calculateReferenceCharWidth(ViewerStyleBuilder().build())
        updateReferenceLineHeight()
    }

    /**
     * Update reference line height with current typeface
     */
    private fun updateReferenceLineHeight() {
        val fontMetrics = buildTextPaint(
            ViewerStyleBuilder()
                .textSize(16f)
                .letterSpacing(0f)
                .build(),
        ).fontMetrics
        referenceLineHeight.value = fontMetrics.bottom - fontMetrics.top + fontMetrics.leading
    }

    /**
     * Get char width array with current text size and letter spacing
     * @param viewerStyle current viewer style
     * @return char width array with current text size and letter spacing
     */
    fun getCharWidthArray(viewerStyle: ViewerStyle): FloatArray {
        if (viewerStyle.fontFamily != customTypefaceName) {
            customTypefaceName = viewerStyle.fontFamily
            customTypeface.value = ResourcesCompat.getFont(
                context, fontMap[viewerStyle.fontFamily] ?: R.font.garamond,
            )
            calculateReferenceCharWidth(viewerStyle)
            updateReferenceLineHeight()
        } else if (viewerStyle.textSize != lastTextSize || viewerStyle.letterSpacing != lastLetterSpacing) {
            lastTextSize = viewerStyle.textSize
            lastLetterSpacing = viewerStyle.letterSpacing
            calculateCharWidth(viewerStyle)
        }
        return charWidthArray
    }

    /**
     * Build text paint with current text size and letter spacing
     * @param viewerStyle current viewer style
     */
    fun buildTextPaint(viewerStyle: ViewerStyle): TextPaint {
        val density = Resources.getSystem().displayMetrics.density
        val textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = viewerStyle.textSize * density
        textPaint.typeface = customTypeface.value
        textPaint.letterSpacing = viewerStyle.letterSpacing
        return textPaint
    }

    /**
     * Cache reference char width array with 16dp text size and 0 letter spacing
     * @param viewerStyle current viewer style
     */
    fun calculateReferenceCharWidth(viewerStyle: ViewerStyle) {
        // setup reference text paint
        val textPaint = buildTextPaint(viewerStyle)
        val density = Resources.getSystem().displayMetrics.density
        textPaint.textSize = 16f * density
        textPaint.letterSpacing = 0f

        // calculate reference char width
        val startTime = System.nanoTime()
        val buffer = FloatArray(128)
        for (i in 0 until 65536 step 128) {
            textPaint.getTextWidths(everyChars, i, 128, buffer)
            for (j in 0 until 128) {
                charWidthReferenceArray[i + j] = buffer[j]
            }
        }
        // handle line break
        charWidthReferenceArray['\n'.code] = Float.POSITIVE_INFINITY
        charWidthReferenceArray['\r'.code] = 0f
        // copy reference char width to current char width
        calculateCharWidth(viewerStyle)
        Log.d("FontDataSource", "calculateReferenceCharWidth: ${(System.nanoTime() - startTime) / 1000000}ms")
    }

    /**
     * Cache char width array with current text size and letter spacing
     * @param viewerStyle current viewer style
     */
    fun calculateCharWidth(viewerStyle: ViewerStyle) {
        val density = Resources.getSystem().displayMetrics.density
        val textSizeRatio = viewerStyle.textSize / 16f
        val letterSpacing = viewerStyle.letterSpacing * viewerStyle.textSize * density
        for (i in 0 until 65536) {
            charWidthArray[i] = charWidthReferenceArray[i] * textSizeRatio + letterSpacing
        }
    }
}
