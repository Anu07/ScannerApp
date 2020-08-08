package com.src.uscan.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(
    entity = PDFEntity::class,
    parentColumns = ["_id"],
    childColumns = ["_childId"],
    onDelete = ForeignKey.NO_ACTION
)]
)
class ImageEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_childId")
    var id = 0

    @ColumnInfo(name ="_path")
   var path = ""

}