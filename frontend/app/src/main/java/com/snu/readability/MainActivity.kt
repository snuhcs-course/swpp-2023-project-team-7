package com.snu.readability

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.snu.readability.data.viewer.FontDataSource
import com.snu.readability.ui.screens.Screen
import com.snu.readability.ui.theme.ReadabilityTheme
import com.snu.readability.ui.viewmodels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SnackBarState(val state: SnackbarHostState? = null, val scope: CoroutineScope? = null) {
    fun showSnackbar(message: String) {
        scope?.launch {
            state?.showSnackbar(message)
        }
    }
}

val LocalSnackbarHost = staticCompositionLocalOf {
    SnackBarState()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // inject FontDataSource into MainActivity
    // to initialize on app startup
    @Inject
    lateinit var fontDataSource: FontDataSource

    companion object {
        init {
            System.loadLibrary("readability")
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)

        installSplashScreen()

        val userViewModel: UserViewModel by viewModels()
        val isSignedIn = runBlocking {
            withContext(Dispatchers.IO) {
                userViewModel.isSignedIn()
            }
        }

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarScope = rememberCoroutineScope()
            ReadabilityTheme {
                Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
                    CompositionLocalProvider(
                        LocalSnackbarHost provides SnackBarState(
                            state = snackbarHostState, scope = snackbarScope,
                        ),
                    ) {
                        Screen(isSignedIn = isSignedIn)
                    }
                }
            }
        }
    }
}
