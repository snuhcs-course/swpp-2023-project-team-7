package com.snu.readability.ui.models.viewer

import android.content.Context
import com.snu.readability.data.viewer.SettingDao
import com.snu.readability.data.viewer.SettingRepository
import com.snu.readability.data.viewer.ViewerStyleBuilder
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingRepositoryTest {

    private lateinit var settingRepository: SettingRepository
    private lateinit var mockSettingDao: SettingDao
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        mockSettingDao = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        every { mockContext.assets } returns mockk(relaxed = true)
        every { mockContext.assets.open(any()) } returns "".byteInputStream()
        settingRepository = SettingRepository(mockSettingDao, mockContext)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resetViewerStyle deletes and inserts a new ViewerStyle`() = runTest {
        // Arrange
        coEvery { mockSettingDao.delete() } just Runs
        coEvery { mockSettingDao.insert(any()) } just Runs

        // Act
        settingRepository.resetViewerStyle()

        // Assert
        coVerify { mockSettingDao.delete() }
        coVerify { mockSettingDao.insert(any()) }
    }

    @Test
    fun `updateViewerStyle updates the ViewerStyle in SettingDao`() = runTest {
        // Arrange
        val viewerStyle = ViewerStyleBuilder().textSize(18f).build()
        coEvery { mockSettingDao.update(any()) } just Runs

        // Act
        settingRepository.updateViewerStyle(viewerStyle)

        // Assert
        coVerify(exactly = 1) { mockSettingDao.update(viewerStyle) }
    }

    @Test
    fun `viewerStyle returns default ViewerStyle if SettingDao returns null`() = runTest {
        // Arrange
        coEvery { mockSettingDao.get() } returns flowOf(null)

        // Act
        val result = settingRepository.viewerStyle.value

        // Assert
        assertEquals(ViewerStyleBuilder().build(), result)
    }
}
