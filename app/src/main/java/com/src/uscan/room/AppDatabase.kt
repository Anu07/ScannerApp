package com.src.uscan.room

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [PDFEntity::class], version = 3,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pdfDao(): PdfDao?
}