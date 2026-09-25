package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
  @PrimaryKey val uri: String,
  val name: String,
  val size: Long,
  val pageCount: Int,
  val lastOpened: Long,
  val lastPage: Int,
  val isFavorite: Boolean,
  val createdAt: Long,
  val modifiedAt: Long,
  val isDownloaded: Boolean = false
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val documentUri: String,
  val pageNumber: Int,
  val title: String
)

@Entity(tableName = "annotations")
data class AnnotationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val documentUri: String,
  val pageNumber: Int,
  val type: String, // HIGHLIGHT, UNDERLINE, NOTE, DRAWING
  val data: String, // JSON or serialized coordinate/color data
  val createdAt: Long
)
