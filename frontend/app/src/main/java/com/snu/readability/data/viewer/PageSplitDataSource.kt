package com.snu.readability.data.viewer

import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import javax.inject.Inject
import javax.inject.Singleton

private class TextDrawer(
    val textPaint: TextPaint,
    val content: String,
    val canvas: Canvas,
) {
    fun drawText(start: Int, end: Int, x: Float, y: Float) {
        canvas.drawText(content, start, end, x, y, textPaint)
    }
}

val Paint.FontMetrics.lineHeight: Float
    get() = (bottom - top + leading)

@Singleton
class PageSplitDataSource @Inject constructor() {
    private external fun splitPageNative(
        content: String,
        charWidths: FloatArray,
        lineHeight: Float,
        width: Float,
        height: Float,
        paragraphSpacing: Float,
    ): IntArray

    private external fun drawPageNative(
        content: String,
        charWidths: FloatArray,
        lineHeight: Float,
        offset: Float,
        width: Float,
        paragraphSpacing: Float,
        textDrawer: TextDrawer,
    )

    /**
     * Split [content] into pages with [width] and [height]
     * @param width page width
     * @param height page height
     * @param content content to be split
     * @param viewerStyle current viewer style
     * @param charWidths char width array
     * @param textPaint text paint
     * @return int array of page start and end index
     */
    fun splitPage(
        width: Int,
        height: Int,
        content: String,
        viewerStyle: ViewerStyle,
        charWidths: FloatArray,
        textPaint: TextPaint,
    ): IntArray {
        val lineHeight = textPaint.fontMetrics.lineHeight * viewerStyle.lineHeight

        return splitPageNative(
            content,
            charWidths,
            lineHeight,
            width.toFloat(),
            height.toFloat(),
            viewerStyle.paragraphSpacing,
        )
    }

    /**
     * Draw [pageContent] on [canvas] with [width]
     * @param canvas canvas to draw on
     * @param width page width
     * @param pageContent content to be drawn
     * @param viewerStyle current viewer style
     * @param charWidths char width array
     * @param textPaint text paint
     * @return int array of page start and end index
     * @see splitPage
     */
    fun drawPage(
        canvas: Canvas,
        width: Int,
        pageContent: String,
        viewerStyle: ViewerStyle,
        charWidths: FloatArray,
        textPaint: TextPaint,
    ) {
        val lineHeight = textPaint.fontMetrics.lineHeight * viewerStyle.lineHeight
        val offset = -textPaint.fontMetrics.top

        val textDrawer = TextDrawer(textPaint, pageContent, canvas)
        drawPageNative(
            pageContent,
            charWidths,
            lineHeight,
            offset,
            width.toFloat(),
            viewerStyle.paragraphSpacing,
            textDrawer,
        )
    }
}
