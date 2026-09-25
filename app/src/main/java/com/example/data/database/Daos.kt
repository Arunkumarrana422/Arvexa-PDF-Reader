package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
  @Query("SELECT * FROM documents ORDER BY lastOpened DESC")
  fun getAllDocuments(): Flow<List<DocumentEntity>>

  @Query("SELECT * FROM documents WHERE isFavorite = 1 ORDER BY lastOpened DESC")
  fun getFavoriteDocuments(): Flow<List<DocumentEntity>>

  @Query("SELECT * FROM documents ORDER BY lastOpened DESC LIMIT 10")
  fun getRecentDocuments(): Flow<List<DocumentEntity>>

  @Query("SELECT * FROM documents WHERE uri = :uri")
  suspend fun getDocumentByUri(uri: String): DocumentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(document: DocumentEntity)

  @Update
  suspend fun updateDocument(document: DocumentEntity)

  @Delete
  suspend fun deleteDocument(document: DocumentEntity)

  @Query("DELETE FROM documents WHERE uri = :uri")
  suspend fun deleteByUri(uri: String)
}

@Dao
interface BookmarkDao {
  @Query("SELECT * FROM bookmarks WHERE documentUri = :documentUri ORDER BY pageNumber ASC")
  fun getBookmarksForDocument(documentUri: String): Flow<List<BookmarkEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmark(bookmark: BookmarkEntity)

  @Delete
  suspend fun deleteBookmark(bookmark: BookmarkEntity)

  @Query("DELETE FROM bookmarks WHERE documentUri = :documentUri AND pageNumber = :pageNumber")
  suspend fun deleteBookmarkByPage(documentUri: String, pageNumber: Int)
}

@Dao
interface AnnotationDao {
  @Query("SELECT * FROM annotations WHERE documentUri = :documentUri AND pageNumber = :pageNumber")
  fun getAnnotationsForPage(documentUri: String, pageNumber: Int): Flow<List<AnnotationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAnnotation(annotation: AnnotationEntity)

  @Delete
  suspend fun deleteAnnotation(annotation: AnnotationEntity)
}
