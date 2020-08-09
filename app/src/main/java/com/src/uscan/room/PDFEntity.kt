package com.src.uscan.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.src.uscan.utils.StringConverter


@Entity(tableName = "pdf")
class PDFEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
     var id = 0

    @ColumnInfo(name ="_path")
    var path = ""

    @ColumnInfo(name ="_time")
    var time = ""

    @ColumnInfo(name ="_pdfPath")
    var pdfPath = ""

    @TypeConverters(StringConverter::class)
    @ColumnInfo(name = "_images")
    var images: List<String>? = null
}