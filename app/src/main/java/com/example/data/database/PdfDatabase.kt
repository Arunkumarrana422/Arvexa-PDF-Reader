package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DocumentEntity::class, BookmarkEntity::class, AnnotationEntity::class], version = 1, exportSchema = false)
abstract class PdfDatabase : RoomDatabase() {
  abstract fun documentDao(): DocumentDao
  abstract fun bookmarkDao(): BookmarkDao
  abstract fun annotationDao(): AnnotationDao

  companion object {
    @Volatile
    private var INSTANCE: PdfDatabase? = null

    fun getDatabase(context: Context): PdfDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          PdfDatabase::class.java,
          "arvexa_pdf_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
