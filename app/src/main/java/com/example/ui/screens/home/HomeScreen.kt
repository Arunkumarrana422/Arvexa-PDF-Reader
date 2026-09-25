package com.example.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DocumentEntity
import com.example.ui.theme.ArvexaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  recentDocs: List<DocumentEntity>,
  onOpenPdf: (String) -> Unit,
  onBrowseFiles: () -> Unit,
  onOpenCategory: (String) -> Unit,
  onOpenTools: () -> Unit,
  onOpenSettings: () -> Unit,
  onToggleFavorite: (String) -> Unit,
  onDeleteDocument: (String) -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ArvexaBlue),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Arvexa PDF Reader",
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            )
          }
        },
        actions = {
          IconButton(onClick = onBrowseFiles) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search & Browse")
          }
          Box {
            IconButton(onClick = { showMenu = true }) {
              Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
            }
            DropdownMenu(
              expanded = showMenu,
              onDismissRequest = { showMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("PDF Tools") },
                onClick = {
                  showMenu = false
                  onOpenTools()
                },
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null) }
              )
              DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                  showMenu = false
                  onOpenSettings()
                },
                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
              )
            }
          }
        }
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Quick Actions",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          QuickActionButton(
            icon = Icons.Default.FolderOpen,
            label = "Browse",
            onClick = onBrowseFiles
          )
          QuickActionButton(
            icon = Icons.Default.Favorite,
            label = "Favorites",
            onClick = { onOpenCategory("Favorites") }
          )
          QuickActionButton(
            icon = Icons.Default.History,
            label = "Recent",
            onClick = { onOpenCategory("Recent") }
          )
          QuickActionButton(
            icon = Icons.Default.Build,
            label = "Tools",
            onClick = onOpenTools
          )
        }
      }

      item {
        Text(
          text = "Categories",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          val categories = listOf("All PDFs", "Recent", "Favorites", "Downloads", "Large Files")
          items(categories) { category ->
            FilterChip(
              selected = false,
              onClick = { onOpenCategory(category) },
              label = { Text(category) },
              leadingIcon = {
                Icon(
                  imageVector = when (category) {
                    "Favorites" -> Icons.Default.Favorite
                    "Recent" -> Icons.Default.History
                    "Downloads" -> Icons.Default.Download
                    "Large Files" -> Icons.Default.Storage
                    else -> Icons.Default.Description
                  },
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
              }
            )
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Recent Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          TextButton(onClick = { onOpenCategory("Recent") }) {
            Text("View All")
          }
        }
      }

      if (recentDocs.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = ArvexaBlue,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No PDFs Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Your PDF documents will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = onBrowseFiles,
                colors = ButtonDefaults.buttonColors(containerColor = ArvexaBlue)
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open File")
              }
            }
          }
        }
      } else {
        items(recentDocs) { doc ->
          DocumentCard(
            document = doc,
            onOpen = { onOpenPdf(doc.uri) },
            onToggleFavorite = { onToggleFavorite(doc.uri) },
            onDelete = { onDeleteDocument(doc.uri) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
fun QuickActionButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(8.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
      modifier = Modifier.size(64.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = ArvexaBlue,
          modifier = Modifier.size(28.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun DocumentCard(
  document: DocumentEntity,
  onOpen: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDelete: () -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Card(
    onClick = onOpen,
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = ArvexaBlue.copy(alpha = 0.15f),
        modifier = Modifier.size(48.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.PictureAsPdf,
            contentDescription = null,
            tint = ArvexaBlue
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = document.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${document.pageCount} pages • ${(document.size / 1024)} KB",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(onClick = onToggleFavorite) {
        Icon(
          imageVector = if (document.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (document.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Box {
        IconButton(onClick = { menuExpanded = true }) {
          Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
        }
        DropdownMenu(
          expanded = menuExpanded,
          onDismissRequest = { menuExpanded = false }
        ) {
          DropdownMenuItem(
            text = { Text("Open") },
            onClick = {
              menuExpanded = false
              onOpen()
            }
          )
          DropdownMenuItem(
            text = { Text(if (document.isFavorite) "Remove Favorite" else "Add Favorite") },
            onClick = {
              menuExpanded = false
              onToggleFavorite()
            }
          )
          DropdownMenuItem(
            text = { Text("Delete") },
            onClick = {
              menuExpanded = false
              onDelete()
            }
          )
        }
      }
    }
  }
}
