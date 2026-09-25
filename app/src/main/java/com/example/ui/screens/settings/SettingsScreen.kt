package com.example.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  onBack: () -> Unit,
  onOpenPrivacy: () -> Unit
) {
  var keepScreenAwake by remember { mutableStateOf(true) }
  var darkMode by remember { mutableStateOf("System Default") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Text(
          text = "Appearance & Reading",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
      item {
        SettingsToggleItem(
          icon = Icons.Default.WbSunny,
          title = "Keep Screen Awake",
          subtitle = "Prevent screen from dimming while reading documents",
          checked = keepScreenAwake,
          onCheckedChange = { keepScreenAwake = it }
        )
      }
      item {
        SettingsActionItem(
          icon = Icons.Default.DarkMode,
          title = "Theme Mode",
          subtitle = darkMode,
          onClick = { darkMode = if (darkMode == "System Default") "Dark" else "System Default" }
        )
      }
      item {
        HorizontalDivider()
      }
      item {
        Text(
          text = "Privacy & Data",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
      item {
        SettingsActionItem(
          icon = Icons.Default.PrivacyTip,
          title = "Privacy Policy & Info",
          subtitle = "Learn how your documents remain secure on your device",
          onClick = onOpenPrivacy
        )
      }
      item {
        HorizontalDivider()
      }
      item {
        Text(
          text = "About",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
      item {
        SettingsActionItem(
          icon = Icons.Default.Info,
          title = "App Version",
          subtitle = "Arvexa PDF Reader v1.0.0 (Production)",
          onClick = {}
        )
      }
    }
  }
}

@Composable
fun SettingsToggleItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}

@Composable
fun SettingsActionItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
