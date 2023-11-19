package com.example.readability.data.viewer

import androidx.compose.ui.graphics.NativeCanvas
import com.example.readability.data.book.BookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class PageSplitData(
    var pageSplits: List<Int>, val width: Int, val height: Int, val viewerStyle: ViewerStyle
)

@Singleton
class PageSplitRepository @Inject constructor(
    private val pageSplitDataSource: PageSplitDataSource,
    private val fontDataSource: FontDataSource,
    private val settingRepository: SettingRepository,
    private val bookRepository: BookRepository,
) {
    private val pageSplitMap = mutableMapOf<Int, PageSplitData>()
    private val bookContentMap = mutableMapOf<Int, String>()

    /**
     * Get [PageSplitData] for [bookId] with [width] and [height]
     * @param bookId book id
     * @param width page width
     * @param height page height
     * @return page split data
     */
    suspend fun getSplitData(bookId: Int, width: Int, height: Int): PageSplitData? {
        if (pageSplitMap[bookId] != null) {
            val pageSplitData = pageSplitMap[bookId]!!
            if (pageSplitData.width == width && pageSplitData.height == height && pageSplitData.viewerStyle == settingRepository.viewerStyle.firstOrNull()) {
                return pageSplitData
            }
        }
        val book = withContext(Dispatchers.IO) {
            bookRepository.getBook(bookId)
        }.firstOrNull() ?: return null
        val content = book.contentData ?: return null
        val viewerStyle = settingRepository.viewerStyle.firstOrNull() ?: return null
        val textPaint = fontDataSource.buildTextPaint(viewerStyle)
        val charWidths = fontDataSource.getCharWidthArray(viewerStyle)
        val pageSplits = pageSplitDataSource.splitPage(
            width = width,
            height = height,
            content = content,
            viewerStyle = viewerStyle,
            textPaint = textPaint,
            charWidths = charWidths,
        )
        val pageSplitData = PageSplitData(
            pageSplits = pageSplits.toList(),
            width = width,
            height = height,
            viewerStyle = viewerStyle,
        )
        pageSplitMap[bookId] = pageSplitData
        return pageSplitData
    }

    fun drawPage(canvas: NativeCanvas, bookId: Int, page: Int, isDarkMode: Boolean) {
        val pageSplitData = pageSplitMap[bookId] ?: return
        val content = if (bookContentMap.containsKey(bookId)) {
            bookContentMap[bookId]!!
        } else {
            runBlocking {
                withContext(Dispatchers.IO) {
                    bookRepository.getBook(bookId)
                }.firstOrNull()
            }?.contentData ?: return
        }
        val pageContent = content.substring(
            if (page == 0) 0 else pageSplitData.pageSplits[page - 1],
            pageSplitData.pageSplits[page]
        )
        val viewerStyle = pageSplitData.viewerStyle
        val textPaint = fontDataSource.buildTextPaint(viewerStyle)
        if (isDarkMode) {
            textPaint.color = viewerStyle.darkTextColor
        } else {
            textPaint.color = viewerStyle.brightTextColor
        }
        val charWidths = fontDataSource.getCharWidthArray(viewerStyle)
        pageSplitDataSource.drawPage(
            canvas = canvas,
            width = pageSplitData.width,
            pageContent = pageContent,
            charWidths = charWidths,
            viewerStyle = viewerStyle,
            textPaint = textPaint,
        )
    }

    fun drawPageRaw(canvas: NativeCanvas, pageContent: String, viewerStyle: ViewerStyle, width: Int, isDarkMode: Boolean) {
        val textPaint = fontDataSource.buildTextPaint(viewerStyle)
        val charWidths = fontDataSource.getCharWidthArray(viewerStyle)
        if (isDarkMode) {
            textPaint.color = viewerStyle.darkTextColor
        } else {
            textPaint.color = viewerStyle.brightTextColor
        }
        pageSplitDataSource.drawPage(
            canvas = canvas,
            width = width,
            pageContent = pageContent,
            charWidths = charWidths,
            viewerStyle = viewerStyle,
            textPaint = textPaint,
        )
    }
}