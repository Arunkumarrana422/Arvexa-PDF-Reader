package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.data.database.AnnotationEntity
import com.example.data.database.BookmarkEntity
import com.example.data.database.DocumentDao
import com.example.data.database.BookmarkDao
import com.example.data.database.AnnotationDao
import com.example.data.database.DocumentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfRepository(
  private val context: Context,
  private val documentDao: DocumentDao,
  private val bookmarkDao: BookmarkDao,
  private val annotationDao: AnnotationDao
) {
  val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()
  val favoriteDocuments: Flow<List<DocumentEntity>> = documentDao.getFavoriteDocuments()
  val recentDocuments: Flow<List<DocumentEntity>> = documentDao.getRecentDocuments()

  fun getBookmarks(uri: String): Flow<List<BookmarkEntity>> = bookmarkDao.getBookmarksForDocument(uri)
  fun getAnnotations(uri: String, page: Int): Flow<List<AnnotationEntity>> = annotationDao.getAnnotationsForPage(uri, page)

  suspend fun getDocument(uri: String): DocumentEntity? = documentDao.getDocumentByUri(uri)

  suspend fun saveOrUpdateDocument(
    uri: String,
    name: String,
    size: Long,
    pageCount: Int,
    lastPage: Int = 0,
    isFavorite: Boolean = false
  ) {
    val existing = documentDao.getDocumentByUri(uri)
    val now = System.currentTimeMillis()
    val doc = DocumentEntity(
      uri = uri,
      name = name,
      size = size,
      pageCount = pageCount,
      lastOpened = now,
      lastPage = if (existing != null && existing.lastPage > 0) existing.lastPage else lastPage,
      isFavorite = existing?.isFavorite ?: isFavorite,
      createdAt = existing?.createdAt ?: now,
      modifiedAt = now
    )
    documentDao.insertDocument(doc)
  }

  suspend fun updateLastPage(uri: String, page: Int) {
    val doc = documentDao.getDocumentByUri(uri)
    doc?.let {
      documentDao.insertDocument(it.copy(lastPage = page, lastOpened = System.currentTimeMillis()))
    }
  }

  suspend fun toggleFavorite(uri: String) {
    val doc = documentDao.getDocumentByUri(uri)
    doc?.let {
      documentDao.insertDocument(it.copy(isFavorite = !it.isFavorite))
    }
  }

  suspend fun deleteDocument(uri: String) {
    documentDao.deleteByUri(uri)
  }

  suspend fun addBookmark(uri: String, pageNumber: Int, title: String) {
    bookmarkDao.insertBookmark(BookmarkEntity(documentUri = uri, pageNumber = pageNumber, title = title))
  }

  suspend fun removeBookmark(uri: String, pageNumber: Int) {
    bookmarkDao.deleteBookmarkByPage(uri, pageNumber)
  }

  suspend fun addAnnotation(uri: String, pageNumber: Int, type: String, data: String) {
    annotationDao.insertAnnotation(AnnotationEntity(documentUri = uri, pageNumber = pageNumber, type = type, data = data, createdAt = System.currentTimeMillis()))
  }

  suspend fun getPdfPageCountAndInfo(uriString: String): Pair<Int, Long> = withContext(Dispatchers.IO) {
    try {
      val uri = Uri.parse(uriString)
      val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext Pair(0, 0L)
      val renderer = PdfRenderer(pfd)
      val pageCount = renderer.pageCount
      renderer.close()
      pfd.close()

      val fileDescriptor = context.contentResolver.openAssetFileDescriptor(uri, "r")
      val size = fileDescriptor?.length ?: 0L
      fileDescriptor?.close()

      Pair(pageCount, size)
    } catch (e: Exception) {
      Pair(1, 0L)
    }
  }

  suspend fun renderPageThumbnail(uriString: String, pageIndex: Int, width: Int, height: Int): Bitmap? = withContext(Dispatchers.IO) {
    try {
      val uri = Uri.parse(uriString)
      val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext null
      val renderer = PdfRenderer(pfd)
      if (pageIndex >= renderer.pageCount) {
        renderer.close()
        pfd.close()
        return@withContext null
      }
      val page = renderer.openPage(pageIndex)
      val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
      page.close()
      renderer.close()
      pfd.close()
      bitmap
    } catch (e: Exception) {
      null
    }
  }
}
