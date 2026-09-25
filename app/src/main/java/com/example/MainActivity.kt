package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.database.PdfDatabase
import com.example.data.repository.PdfRepository
import com.example.ui.screens.discovery.DiscoveryScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.reader.PdfReaderScreen
import com.example.ui.screens.settings.PrivacyScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.tools.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  private lateinit var repository: PdfRepository
  private var navControllerHolder: androidx.navigation.NavHostController? = null

  private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    uri?.let {
      contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
      navControllerHolder?.navigate("reader?uri=${Uri.encode(it.toString())}&page=0")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = PdfDatabase.getDatabase(applicationContext)
    repository = PdfRepository(applicationContext, database.documentDao(), database.bookmarkDao(), database.annotationDao())

    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        navControllerHolder = navController

        val recentDocs by repository.recentDocuments.collectAsStateWithLifecycle(initialValue = emptyList())
        val allDocs by repository.allDocuments.collectAsStateWithLifecycle(initialValue = emptyList())
        val favoriteDocs by repository.favoriteDocuments.collectAsStateWithLifecycle(initialValue = emptyList())

        val coroutineScope = rememberCoroutineScope()
        var hasCompletedOnboarding by remember { mutableStateOf(false) }

        NavHost(
          navController = navController,
          startDestination = if (hasCompletedOnboarding) "home" else "onboarding"
        ) {
          composable("onboarding") {
            OnboardingScreen(
              onGetStarted = {
                hasCompletedOnboarding = true
                navController.navigate("home") {
                  popUpTo("onboarding") { inclusive = true }
                }
              }
            )
          }

          composable("home") {
            HomeScreen(
              recentDocs = recentDocs,
              onOpenPdf = { uri ->
                navController.navigate("reader?uri=${Uri.encode(uri)}&page=0")
              },
              onBrowseFiles = {
                openDocumentLauncher.launch(arrayOf("application/pdf"))
              },
              onOpenCategory = { cat ->
                navController.navigate("discovery?category=$cat")
              },
              onOpenTools = {
                navController.navigate("tools")
              },
              onOpenSettings = {
                navController.navigate("settings")
              },
              onToggleFavorite = { uri ->
                coroutineScope.launch {
                  repository.toggleFavorite(uri)
                }
              },
              onDeleteDocument = { uri ->
                coroutineScope.launch {
                  repository.deleteDocument(uri)
                }
              }
            )
          }

          composable(
            route = "reader?uri={uri}&page={page}",
            arguments = listOf(
              navArgument("uri") { type = NavType.StringType },
              navArgument("page") { type = NavType.IntType; defaultValue = 0 }
            )
          ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("uri") ?: ""
            val page = backStackEntry.arguments?.getInt("page") ?: 0

            LaunchedEffect(uri) {
              if (uri.isNotEmpty()) {
                val (pageCount, size) = repository.getPdfPageCountAndInfo(uri)
                val name = Uri.parse(uri).lastPathSegment ?: "Document"
                repository.saveOrUpdateDocument(uri, name, size, pageCount, page)
              }
            }

            PdfReaderScreen(
              uriString = uri,
              initialPage = page,
              onBack = { navController.popBackStack() },
              onUpdateLastPage = { u, p ->
                coroutineScope.launch {
                  repository.updateLastPage(u, p)
                }
              },
              onAddBookmark = { u, p, title ->
                coroutineScope.launch {
                  repository.addBookmark(u, p, title)
                }
              },
              onRemoveBookmark = { u, p ->
                coroutineScope.launch {
                  repository.removeBookmark(u, p)
                }
              }
            )
          }

          composable(
            route = "discovery?category={category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
          ) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: "All PDFs"
            val docs = when (category) {
              "Favorites" -> favoriteDocs
              "Recent" -> recentDocs
              "Downloads" -> allDocs.filter { it.uri.contains("Download", ignoreCase = true) }
              "Large Files" -> allDocs.filter { it.size > 2 * 1024 * 1024 }
              else -> allDocs
            }

            DiscoveryScreen(
              title = category,
              documents = docs,
              onBack = { navController.popBackStack() },
              onOpenPdf = { uri ->
                navController.navigate("reader?uri=${Uri.encode(uri)}&page=0")
              },
              onToggleFavorite = { uri ->
                coroutineScope.launch {
                  repository.toggleFavorite(uri)
                }
              },
              onDeleteDocument = { uri ->
                coroutineScope.launch {
                  repository.deleteDocument(uri)
                }
              }
            )
          }

          composable("tools") {
            PdfToolsScreen(
              onBack = { navController.popBackStack() },
              onNavigateTool = { route -> navController.navigate(route) }
            )
          }

          composable("images_to_pdf") {
            ImagesToPdfScreen(onBack = { navController.popBackStack() })
          }

          composable("merge_pdf") {
            MergePdfScreen(onBack = { navController.popBackStack() })
          }

          composable("split_pdf") {
            SplitPdfScreen(onBack = { navController.popBackStack() })
          }

          composable("compress_pdf") {
            CompressPdfScreen(onBack = { navController.popBackStack() })
          }

          composable("settings") {
            SettingsScreen(
              onBack = { navController.popBackStack() },
              onOpenPrivacy = { navController.navigate("privacy") }
            )
          }

          composable("privacy") {
            PrivacyScreen(onBack = { navController.popBackStack() })
          }
        }
      }
    }
  }
}
