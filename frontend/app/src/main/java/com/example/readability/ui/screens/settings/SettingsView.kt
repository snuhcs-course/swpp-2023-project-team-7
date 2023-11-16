package com.example.readability.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.readability.ui.components.SettingTitle
import com.example.readability.ui.theme.ReadabilityTheme

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
    onBack: () -> Unit = {},
    onNavigatePasswordCheck: () -> Unit = {},
    onNavigateViewer: () -> Unit = {},
    onNavigateAbout: (type: String) -> Unit = {},
) {
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
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(20.dp)),
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
        }
    }
}

