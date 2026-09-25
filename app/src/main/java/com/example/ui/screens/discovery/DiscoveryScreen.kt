package com.example.ui.screens.discovery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.database.DocumentEntity
import com.example.ui.screens.home.DocumentCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
  title: String,
  documents: List<DocumentEntity>,
  onBack: () -> Unit,
  onOpenPdf: (String) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onDeleteDocument: (String) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var sortBy by remember { mutableStateOf("Name") }

  val filteredDocs = documents.filter {
    it.name.contains(searchQuery, ignoreCase = true)
  }.sortedWith(
    when (sortBy) {
      "Date" -> compareByDescending { it.lastOpened }
      "Size" -> compareByDescending { it.size }
      "Pages" -> compareByDescending { it.pageCount }
      else -> compareBy { it.name }
    }
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(title) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search $title...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = MaterialTheme.shapes.medium,
        singleLine = true
      )

      Spacer(modifier = Modifier.height(16.dp))

      if (filteredDocs.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No documents found",
            style = MaterialTheme.colorScheme.onSurfaceVariant.let { MaterialTheme.typography.bodyLarge }
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(filteredDocs) { doc ->
            DocumentCard(
              document = doc,
              onOpen = { onOpenPdf(doc.uri) },
              onToggleFavorite = { onToggleFavorite(doc.uri) },
              onDelete = { onDeleteDocument(doc.uri) }
            )
          }
        }
      }
    }
  }
}
