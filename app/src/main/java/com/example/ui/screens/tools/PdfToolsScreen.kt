package com.example.ui.screens.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArvexaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolsScreen(
  onBack: () -> Unit,
  onNavigateTool: (String) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("PDF Tools & Utilities") },
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
        ToolCard(
          icon = Icons.Default.Image,
          title = "Images to PDF",
          subtitle = "Convert multiple photos and images into a single professional PDF document.",
          onClick = { onNavigateTool("images_to_pdf") }
        )
      }
      item {
        ToolCard(
          icon = Icons.Default.Merge,
          title = "Merge PDFs",
          subtitle = "Combine multiple PDF documents into one organized file.",
          onClick = { onNavigateTool("merge_pdf") }
        )
      }
      item {
        ToolCard(
          icon = Icons.Default.CallSplit,
          title = "Split PDF",
          subtitle = "Extract specific pages or page ranges from a PDF document.",
          onClick = { onNavigateTool("split_pdf") }
        )
      }
      item {
        ToolCard(
          icon = Icons.Default.Compress,
          title = "Compress PDF",
          subtitle = "Reduce PDF file size while keeping high visual quality.",
          onClick = { onNavigateTool("compress_pdf") }
        )
      }
    }
  }
}

@Composable
fun ToolCard(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .padding(20.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = ArvexaBlue.copy(alpha = 0.15f),
        modifier = Modifier.size(56.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArvexaBlue,
            modifier = Modifier.size(28.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
