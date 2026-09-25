package com.example.ui.screens.reader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArvexaBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
  uriString: String,
  initialPage: Int = 0,
  onBack: () -> Unit,
  onUpdateLastPage: (String, Int) -> Unit,
  onAddBookmark: (String, Int, String) -> Unit,
  onRemoveBookmark: (String, Int) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
  var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
  var pageCount by remember { mutableStateOf(0) }
  var currentPage by remember { mutableStateOf(initialPage) }
  var isLoading by remember { mutableStateOf(true) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isBookmarked by remember { mutableStateOf(false) }

  var showInfoDialog by remember { mutableStateOf(false) }
  var showTocDialog by remember { mutableStateOf(false) }
  var showJumpDialog by remember { mutableStateOf(false) }
  var showSearchDialog by remember { mutableStateOf(false) }
  var readingMode by remember { mutableStateOf("Normal") }

  var scale by remember { mutableStateOf(1.0f) }
  var offsetX by remember { mutableStateOf(0f) }
  var offsetY by remember { mutableStateOf(0f) }

  val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialPage)

  LaunchedEffect(uriString) {
    withContext(Dispatchers.IO) {
      try {
        val uri = Uri.parse(uriString)
        val pfd = context.contentResolver.openFileDescriptor(uri, "r")
        if (pfd != null) {
          fileDescriptor = pfd
          val r = PdfRenderer(pfd)
          renderer = r
          pageCount = r.pageCount
          isLoading = false
        } else {
          errorMessage = "Could not open document."
          isLoading = false
        }
      } catch (e: Exception) {
        errorMessage = e.localizedMessage ?: "Failed to load PDF."
        isLoading = false
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      try {
        renderer?.close()
        fileDescriptor?.close()
      } catch (_: Exception) {}
    }
  }

  LaunchedEffect(listState.firstVisibleItemIndex) {
    val idx = listState.firstVisibleItemIndex
    if (idx in 0 until pageCount) {
      currentPage = idx
      onUpdateLastPage(uriString, idx)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = Uri.parse(uriString).lastPathSegment ?: "PDF Document",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1
            )
            Text(
              text = "Page ${currentPage + 1} of $pageCount",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = {
            isBookmarked = !isBookmarked
            if (isBookmarked) {
              onAddBookmark(uriString, currentPage, "Page ${currentPage + 1}")
            } else {
              onRemoveBookmark(uriString, currentPage)
            }
          }) {
            Icon(
              imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (isBookmarked) ArvexaBlue else MaterialTheme.colorScheme.onSurface
            )
          }
          IconButton(onClick = { showTocDialog = true }) {
            Icon(imageVector = Icons.Default.List, contentDescription = "Table of Contents")
          }
          IconButton(onClick = { showInfoDialog = true }) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Document Info")
          }
        }
      )
    },
    bottomBar = {
      Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              if (currentPage > 0) {
                coroutineScope.launch {
                  listState.animateScrollToItem(currentPage - 1)
                }
              }
            },
            enabled = currentPage > 0
          ) {
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Page")
          }

          TextButton(onClick = { showJumpDialog = true }) {
            Text("Page ${currentPage + 1} / $pageCount", fontWeight = FontWeight.Bold)
          }

          IconButton(
            onClick = {
              if (currentPage < pageCount - 1) {
                coroutineScope.launch {
                  listState.animateScrollToItem(currentPage + 1)
                }
              }
            },
            enabled = currentPage < pageCount - 1
          ) {
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Page")
          }
        }
      }
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(
          when (readingMode) {
            "Dark" -> Color(0xFF121212)
            "Sepia" -> Color(0xFFF4ECD8)
            else -> MaterialTheme.colorScheme.background
          }
        )
        .pointerInput(Unit) {
          detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(1.0f, 4.0f)
            if (scale > 1.0f) {
              offsetX += pan.x
              offsetY += pan.y
            } else {
              offsetX = 0f
              offsetY = 0f
            }
          }
        },
      contentAlignment = Alignment.Center
    ) {
      if (isLoading) {
        CircularProgressIndicator(color = ArvexaBlue)
      } else if (errorMessage != null) {
        Text(text = errorMessage ?: "Error", color = MaterialTheme.colorScheme.error)
      } else if (renderer != null && pageCount > 0) {
        LazyColumn(
          state = listState,
          modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
              scaleX = scale,
              scaleY = scale,
              translationX = offsetX,
              translationY = offsetY
            ),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          items(pageCount) { index ->
            PdfPageItem(
              renderer = renderer,
              pageIndex = index
            )
          }
        }
      }
    }

    if (showJumpDialog) {
      var targetPageText by remember { mutableStateOf("${currentPage + 1}") }
      AlertDialog(
        onDismissRequest = { showJumpDialog = false },
        title = { Text("Jump to Page") },
        text = {
          OutlinedTextField(
            value = targetPageText,
            onValueChange = { targetPageText = it },
            label = { Text("Page number (1 - $pageCount)") },
            singleLine = true
          )
        },
        confirmButton = {
          TextButton(onClick = {
            showJumpDialog = false
            val p = targetPageText.toIntOrNull()
            if (p != null && p in 1..pageCount) {
              coroutineScope.launch {
                listState.scrollToItem(p - 1)
              }
            }
          }) {
            Text("Go")
          }
        },
        dismissButton = {
          TextButton(onClick = { showJumpDialog = false }) { Text("Cancel") }
        }
      )
    }

    if (showInfoDialog) {
      AlertDialog(
        onDismissRequest = { showInfoDialog = false },
        title = { Text("Document Information") },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("File Name: ${Uri.parse(uriString).lastPathSegment ?: "Unknown"}")
            Text("Total Pages: $pageCount")
            Text("Location: $uriString")
          }
        },
        confirmButton = {
          TextButton(onClick = { showInfoDialog = false }) { Text("Close") }
        }
      )
    }

    if (showTocDialog) {
      AlertDialog(
        onDismissRequest = { showTocDialog = false },
        title = { Text("Table of Contents") },
        text = {
          Column(modifier = Modifier.fillMaxWidth()) {
            Text("Document pages:")
            Spacer(modifier = Modifier.height(12.dp))
            for (i in 0 until minOf(pageCount, 10)) {
              TextButton(onClick = {
                showTocDialog = false
                coroutineScope.launch { listState.scrollToItem(i) }
              }) {
                Text("Page ${i + 1}")
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showTocDialog = false }) { Text("Close") }
        }
      )
    }

    if (showSearchDialog) {
      var query by remember { mutableStateOf("") }
      AlertDialog(
        onDismissRequest = { showSearchDialog = false },
        title = { Text("Search in PDF") },
        text = {
          Column {
            OutlinedTextField(
              value = query,
              onValueChange = { query = it },
              placeholder = { Text("Search text...") },
              singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Search index active.")
          }
        },
        confirmButton = {
          TextButton(onClick = { showSearchDialog = false }) {
            Text("Search")
          }
        },
        dismissButton = {
          TextButton(onClick = { showSearchDialog = false }) { Text("Cancel") }
        }
      )
    }
  }
}

@Composable
fun PdfPageItem(
  renderer: PdfRenderer?,
  pageIndex: Int
) {
  var bitmap by remember { mutableStateOf<Bitmap?>(null) }

  LaunchedEffect(renderer, pageIndex) {
    withContext(Dispatchers.IO) {
      try {
        renderer?.let { r ->
          synchronized(r) {
            val page = r.openPage(pageIndex)
            val w = page.width * 2
            val h = page.height * 2
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap = bmp
          }
        }
      } catch (_: Exception) {}
    }
  }

  Box(
    modifier = Modifier
      .fillMaxWidth(0.92f)
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(650.dp),
      elevation = CardDefaults.cardElevation(1.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      shape = RoundedCornerShape(8.dp)
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        if (bitmap != null) {
          Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "PDF Page ${pageIndex + 1}",
            modifier = Modifier.fillMaxSize()
          )
        } else {
          CircularProgressIndicator(color = ArvexaBlue)
        }
      }
    }
  }
}
