package com.example.readability.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.readability.R
import com.example.readability.ui.components.SettingTitle
import com.example.readability.ui.theme.ReadabilityTheme

@Composable
@Preview(showBackground = true, device = "id:pixel_5")
fun SettingsViewPreview() {
    ReadabilityTheme {
        SettingsView("John Doe")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    username: String,
    onBack: () -> Unit = {},
    onNavigateAccountSetting: () -> Unit = {},
    onNavigateViewer: () -> Unit = {},
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(text = "Settings") }, navigationIcon = {
            IconButton(onClick = { onBack() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        })
    }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
        ) {
            SettingTitle(text = "General")
            ListItem(
                modifier = Modifier.clickable(
                    onClickLabel = "Account Settings",
                ) {
                    onNavigateAccountSetting()
                },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(5.dp, 10.dp, 5.dp, 4.dp)
                                .fillMaxSize(),
                            painter = painterResource(R.drawable.avatar),
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                headlineContent = {
                    Text(text = username, style = MaterialTheme.typography.bodyLarge)
                },
                supportingContent = {
                    Text(
                        text = "Account Settings",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate",
                    )
                },
            )
            ListItem(
                modifier = Modifier.clickable(
                    onClickLabel = "Viewer Settings",
                ) {
                    onNavigateViewer()
                },
                headlineContent = {
                    Text(text = "Viewer Settings", style = MaterialTheme.typography.bodyLarge)
                },
                trailingContent = {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Navigate",
                    )
                },
            )
        }
    }
}
