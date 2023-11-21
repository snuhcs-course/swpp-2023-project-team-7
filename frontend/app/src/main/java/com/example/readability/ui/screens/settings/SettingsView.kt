package com.example.readability.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.readability.ui.components.SettingTitle
import com.example.readability.ui.theme.ReadabilityTheme
import kotlinx.coroutines.launch

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun SettingsViewPreview() {
    ReadabilityTheme {
        SettingsView()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onSignOut: suspend () -> Result<Unit> = { Result.success(Unit) },
    onBack: () -> Unit = {},
    onNavigatePasswordCheck: () -> Unit = {},
    onNavigateViewer: () -> Unit = {},
    onNavigateAbout: (type: String) -> Unit = {},
    onNavigateIntro: () -> Unit = {},
) {
    val context = LocalContext.current
    val logoutScope = rememberCoroutineScope()

    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Settings") }, navigationIcon = {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            SettingTitle(text = "General")
            ListItem(
                modifier = Modifier.clickable {
                    onNavigatePasswordCheck()
                },
                leadingContent = {
                    AsyncImage(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        model = "https://picsum.photos/200/200",
                        contentDescription = "Profile Picture"
                    )
                },
                headlineContent = {
                    Text(text = "John Doe", style = MaterialTheme.typography.bodyLarge)
                },
                supportingContent = {
                    Text(
                        text = "Account Settings",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate"
                    )
                },
            )
            ListItem(modifier = Modifier.clickable {
                onNavigateViewer()
            }, headlineContent = {
                Text(text = "Viewer Settings", style = MaterialTheme.typography.bodyLarge)
            }, trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Navigate"
                )
            })
            SettingTitle(modifier = Modifier.padding(top = 24.dp), text = "About")
            ListItem(modifier = Modifier.clickable {
                onNavigateAbout("privacy_policy")
            }, headlineContent = {
                Text(text = "Privacy Policy", style = MaterialTheme.typography.bodyLarge)
            }, trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Navigate"
                )
            })
            ListItem(modifier = Modifier.clickable {
                onNavigateAbout("terms_of_use")
            }, headlineContent = {
                Text(text = "Terms of Use", style = MaterialTheme.typography.bodyLarge)
            }, trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Navigate"
                )
            })
            ListItem(modifier = Modifier.clickable {
                onNavigateAbout("open_source_licenses")
            }, headlineContent = {
                Text(text = "Open Source Licenses", style = MaterialTheme.typography.bodyLarge)
            }, trailingContent = {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Navigate"
                )
            })
            Box(modifier = Modifier
                .padding(16.dp, 40.dp, 16.dp, 16.dp)
                .fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                TextButton(onClick = {
                    logoutScope.launch {
                        onSignOut().onSuccess {
                            Toast.makeText(
                                context, "Logout Success", Toast.LENGTH_SHORT
                            ).show()
                            onNavigateIntro()
                        }.onFailure {
                            Toast.makeText(
                                context, "Logout Failed", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }) {
                    Text(text = "Logout")
                }
            }
        }
    }
}

