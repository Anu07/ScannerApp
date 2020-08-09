package com.src.uscan.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.src.uscan.utils.StringConverter


@Database(entities = [PDFEntity::class], version = 4,exportSchema = false)
@TypeConverters(StringConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pdfDao(): PdfDao?
}