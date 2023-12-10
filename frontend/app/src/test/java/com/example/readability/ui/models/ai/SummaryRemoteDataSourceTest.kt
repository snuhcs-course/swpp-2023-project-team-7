package com.example.readability.ui.models.ai

import com.example.readability.data.ai.SummaryAPI
import com.example.readability.data.ai.SummaryRemoteDataSource
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayInputStream
import java.io.IOException

val exampleSummary = """
    event: summary
    data: 
    
    event: summary
    data: This is 
    
    event: summary
    data: a test 
    
    event: summary
    data: summary.
    
    event: summary
    data: 
""".trimIndent()

class SummaryRemoteDataSourceTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var summaryAPI: SummaryAPI

    @InjectMocks
    lateinit var summaryRemoteDataSource: SummaryRemoteDataSource

    @Before
    fun setUp() {
        summaryRemoteDataSource = SummaryRemoteDataSource(summaryAPI)
    }

    @Test
    fun `getSummary success`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"
        val responseBody = mock(ResponseBody::class.java)
        doReturn(ByteArrayInputStream(exampleSummary.toByteArray())).`when`(responseBody).byteStream()
        val response = Response.success(responseBody)

        val call = mock(Call::class.java)
        doReturn(response).`when`(call).execute()
        doReturn(call).`when`(summaryAPI).getSummary(bookId, progress, accessToken)

        // Act
        val result = summaryRemoteDataSource.getSummary(bookId, progress, accessToken)

        // Assert
        result.toList().reduce {
            acc, s -> acc + s
        }.let {
            assert(it == "This is a test summary.\n")
        }

        // Verify
        verify(summaryAPI).getSummary(bookId, progress, accessToken)
    }

    @Test(expected = Throwable::class)
    fun `getSummary network error`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"

        doThrow(IOException("Network error")).`when`(summaryAPI).getSummary(bookId, progress, accessToken).execute()

        // Act
        val result = summaryRemoteDataSource.getSummary(bookId, progress, accessToken)

        // Assert
        result.collect {
            // This block should not be reached
        }

        // Verify
        verify(summaryAPI).getSummary(bookId, progress, accessToken)
    }

    @Test(expected = Throwable::class)
    fun `getSummary server error`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"
        val errorBody = mock(ResponseBody::class.java)
        doReturn("{\"detail\":\"Server error\"}").`when`(errorBody).string()
        val response = Response.error<ResponseBody>(400, errorBody)

        doReturn(response).`when`(summaryAPI).getSummary(bookId, progress, accessToken)

        // Act
        val result = summaryRemoteDataSource.getSummary(bookId, progress, accessToken)

        // Assert
        result.collect {
            // This block should not be reached
        }

        // Verify
        verify(summaryAPI).getSummary(bookId, progress, accessToken)
    }

    @Test(expected = Throwable::class)
    fun `getSummary parsing error`() = runTest {
        // Arrange
        val bookId = 1
        val progress = 0.5
        val accessToken = "testAccessToken"
        val responseBody = mock(ResponseBody::class.java)
        doReturn(ByteArrayInputStream("data:".toByteArray())).`when`(responseBody).byteStream()
        val response = Response.success(responseBody)

        val call = mock(Call::class.java)
        doReturn(response).`when`(call).execute()
        doReturn(call).`when`(summaryAPI).getSummary(bookId, progress, accessToken)

        // Act
        val result = summaryRemoteDataSource.getSummary(bookId, progress, accessToken)

        // Assert
        result.collect {
            // This block should not be reached
        }

        // Verify
        verify(summaryAPI).getSummary(bookId, progress, accessToken)
    }
}
