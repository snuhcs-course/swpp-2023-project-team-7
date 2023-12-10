package com.example.readability.ui.models.viewer

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.example.readability.data.viewer.FontDataSource
import com.example.readability.data.viewer.ViewerStyleBuilder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FontDataSourceTest {

    private lateinit var fontDataSource: FontDataSource
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockkStatic(ResourcesCompat::class)
        mockContext = mockk(relaxed = true)
        every { ResourcesCompat.getFont(mockContext, any()) } returns mockk(relaxed = true)
        fontDataSource = FontDataSource(mockContext)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getCharWidthArray returns char width array`() {
        // Arrange
        val viewerStyle = ViewerStyleBuilder().build()

        // Act
        val result = fontDataSource.getCharWidthArray(viewerStyle)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `buildTextPaint returns text paint`() {
        // Arrange
        val viewerStyle = ViewerStyleBuilder().build()

        // Act
        val result = fontDataSource.buildTextPaint(viewerStyle)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `calculateReferenceCharWidth updates char width array`() {
        // Arrange
        val viewerStyle = ViewerStyleBuilder().build()

        // Act
        fontDataSource.calculateReferenceCharWidth(viewerStyle)

        // It works without exception
    }

    @Test
    fun `calculateCharWidth updates char width array`() {
        // Arrange
        val viewerStyle = ViewerStyleBuilder().build()

        // Act
        fontDataSource.calculateCharWidth(viewerStyle)

        // It works without exception
    }
}
