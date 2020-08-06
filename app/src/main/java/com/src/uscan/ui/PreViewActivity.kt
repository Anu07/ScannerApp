package com.src.uscan.ui

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.src.uscan.R
import kotlinx.android.synthetic.main.preview_activity.*


class PreViewActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener,
    OnPageErrorListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_activity)
        displayFromUri(intent.data!!)
    }
    private fun displayFromUri(uri: Uri) {
        var pdfFileName = getFileName(uri)
        pdfView.fromUri(uri)
            .defaultPage(1)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(DefaultScrollHandle(this))
            .spacing(10) // in dp
            .onPageError(this)
            .load()
    }


    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result
    }


    companion object {
        private val TAG = PreViewActivity::class.java.name
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        Log.i("Loading ","Page")
    }

    override fun loadComplete(nbPages: Int) {
        Log.i("Loading completed","")
    }

    override fun onPageError(page: Int, t: Throwable?) {
        Toast.makeText(this@PreViewActivity,"Some error occurred", Toast.LENGTH_SHORT).show()
    }
}