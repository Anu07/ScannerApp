package com.src.uscan.room

import android.provider.CalendarContract.Events
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf")
    fun getAll(): List<PDFEntity>?

    @Query("SELECT * FROM pdf WHERE _pdfPath = :pdfPath")
    fun findSpecificEvent(pdfPath: String): List<PDFEntity>?

    @Query("SELECT _images FROM pdf WHERE _pdfPath = :pdfPath")
    fun findSpecificIamgesEvent(pdfPath: String): List<ImageEntity>?

    @Insert
    fun insert(pdf: PDFEntity?)

    @Query("UPDATE pdf SET _images = :images_ WHERE _path =:path_")
    fun updateValues(images_: ImageEntity, path_: String?)

    @Query("UPDATE pdf SET _images = null WHERE _path =:path_")
    fun emptyImagesValues( path_: String?)

    @Delete
    fun delete(pdf: PDFEntity?)

    @Update
    fun update(pdf: PDFEntity?)
}