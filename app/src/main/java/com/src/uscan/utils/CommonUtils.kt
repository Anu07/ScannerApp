package com.src.uscan.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.util.Log
import com.shockwave.pdfium.PdfiumCore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.util.*


class CommonUtils {
    fun moveRename(fileName:String){
        val sdcard = Environment.getExternalStorageDirectory()
        val from = File(sdcard, "/ecatAgent/$fileName")
        val to = File(sdcard, "/ecatAgent/" + "Delete")
        from.renameTo(to)
    }

    @Throws(IOException::class)
     fun copyFile(sourceFile: File,ctx:Context) {
        if (!sourceFile.exists()) {
            return
        }
        var dstPath = File.separator + "uScan"+File.separator
        val dst = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DCIM),dstPath)
        var source: FileChannel? = null
        var destination: FileChannel? = null
        source = FileInputStream(sourceFile).channel
        destination = FileOutputStream(dst).channel
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        Log.e("Saved","successfully")
        source?.close()
        destination?.close()
    }


    fun generateImageFromPdf(context: Context, pdfUri: Uri?) {
        val pageNumber = 0
        val pdfiumCore = PdfiumCore(context)
        try {
            val fd: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(pdfUri!!, "r")
            val pdfDocument: com.shockwave.pdfium.PdfDocument? = pdfiumCore.newDocument(fd)
            pdfiumCore.openPage(pdfDocument, pageNumber)
            val width: Int = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber)
            val height: Int = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height)
            saveImage(bmp)
            pdfiumCore.closeDocument(pdfDocument) // important!
        } catch (e: Exception) {
            //todo with exception
        }
    }

    val FOLDER =
        Environment.getExternalStorageDirectory().toString() + "/Uscan"

    private fun saveImage(bmp: Bitmap) {
        var out: FileOutputStream? = null
        try {
            val folder = File(FOLDER)
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, "PDF.png")
            out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
        } catch (e: Exception) {
            //todo with exception
        } finally {
            try {
                out?.close()
            } catch (e: Exception) {
                //todo with exception
            }
        }
    }


    private fun SaveDImage(finalBitmap: Bitmap) {
        val root = Environment.getExternalStorageDirectory().toString()
        val myDir = File("$root/Uscan")
        myDir.mkdirs()
        val generator = Random()
        var n = 10000
        n = generator.nextInt(n)
        val fname = "Image-$n.jpg"
        val file = File(myDir, fname)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}