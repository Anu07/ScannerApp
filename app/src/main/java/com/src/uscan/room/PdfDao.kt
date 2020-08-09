package com.src.uscan.room

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.src.uscan.utils.StringConverter


@Dao
interface PdfDao {
    @Query("SELECT * FROM pdf")
    fun getAll(): List<PDFEntity>?

    @Query("SELECT * FROM pdf WHERE _pdfPath = :pdfPath")
    fun findSpecificEvent(pdfPath: String): List<PDFEntity>?

    @TypeConverters(StringConverter::class)
    @Query("SELECT _images FROM pdf WHERE _pdfPath = :pdfPath")
    fun findSpecificIamgesEvent(pdfPath: String): List<String>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(pdf: PDFEntity?)

    @TypeConverters(StringConverter::class)
    @Query("UPDATE pdf SET _images = :images_ WHERE _path =:path_")
    fun updateValues(images_: ArrayList<String>, path_: String?)


    @Query("UPDATE pdf SET _images = null WHERE _path =:path_")
    fun emptyImagesValues( path_: String?)

    @Query("UPDATE pdf SET _pdfPath = :path_ WHERE _pdfPath =:oldPath")
    fun updatePDFPath( oldPath:String,path_: String?)


    @Query("Delete FROM pdf where _pdfPath = :pdf")
    fun deletePdf(pdf: String?)

    @Query("Delete FROM pdf where _pdfPath = :pdf")
    fun deleteImages(pdf: String?)

    @Update
    fun update(pdf: PDFEntity?)

    @Query("DELETE FROM pdf")
    fun emptyPDFTable()

}