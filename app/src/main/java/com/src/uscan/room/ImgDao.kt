package com.src.uscan.room

import android.provider.CalendarContract.Events
import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface ImgDao {
    @Query("SELECT * FROM pdf")
    fun getAll(): List<PDFEntity>?

    @Query("SELECT * FROM pdf WHERE _pdfPath = :pdfPath")
    fun findSpecificEvent(pdfPath: String): List<PDFEntity>?


    @Insert
    fun insert(pdf: PDFEntity?)

    @Delete
    fun delete(pdf: PDFEntity?)

    @Update
    fun update(pdf: PDFEntity?)
}