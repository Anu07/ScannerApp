package com.src.uscan.ui

import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.src.uscan.R
import com.src.uscan.UscanApplication
import com.src.uscan.room.DatabaseClient
import com.src.uscan.room.ImageEntity
import com.src.uscan.room.PDFEntity
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.RoomOperationCompleted
import com.src.uscan.utils.RoomOperationImageCompleted
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.preview_activity.*
import java.io.File
import java.util.ArrayList


class PreViewActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener,
    OnPageErrorListener, LongPressListener,RoomOperationImageCompleted {

    private val REQUEST_TAKE_PHOTO: Int = 900
    private var calledOnce: Boolean = false
    private var imgFile: File? =null
    private var imgUri: String = ""
    private var selectedPos: Int = -1
    private var imagesPdfExistingList: ArrayList<ImageEntity>? = ArrayList()
    private var mAdapter: GridPdfAdapter? = null
    private var pdfPath: String? =""
    var resultUri: String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_activity)

        if (intent.hasExtra("image")) {
            saveToRoom()
//            saveImagesInList()
            mAdapter?.notifyDataSetChanged()
        }

        if(intent.hasExtra("PDF")){
            pdfPath = intent.getStringExtra("PDF")
            setImageGridAdapter()
            displayFromUri()
        }

    }

    private fun setImageGridAdapter() {

        pdfRecyclerView.layoutManager =
            GridLayoutManager(this@PreViewActivity, calculateNoOfColumns(200f))
        mAdapter = GridPdfAdapter(this@PreViewActivity, imagesPdfExistingList, this)
        pdfRecyclerView.adapter = mAdapter
        pdfRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position: Int =
                    parent.getChildAdapterPosition(view) // item position
                val spanCount = 2
                val spacing = 20 //spacing between views in grid
                if (position >= 0) {
                    val column = position % spanCount // item column
                    outRect.left =
                        spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right =
                        (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
                    if (position < spanCount) { // top edge
                        outRect.top = spacing
                    }
                    outRect.bottom = spacing // item bottom
                } else {
                    outRect.left = 0
                    outRect.right = 0
                    outRect.top = 0
                    outRect.bottom = 0
                }
            }
        })
    }

    private fun deleteSelectedFile(): Boolean {
        deleteFromRoom(imgUri)
        pdfRecyclerView.adapter = GridPdfAdapter(this, imagesPdfExistingList, this)
        return baseContext.deleteFile(imgFile?.name)
    }

    private fun deleteDialog() {
        val builder = AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("continue") { dialog, which ->
            Log.i("Delete", "Result" + deleteSelectedFile())
            mAdapter?.notifyDataSetChanged()
        };
        builder.setNegativeButton("Cancel") { dialog, which ->
            finish()
        };
        builder.show()
    }


    private fun displayFromUri() {
        val file = File(pdfPath)
        if (file.exists()) {
            pdfView.fromFile(file)
                .defaultPage(1)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .load()
        }

    }


    private fun hideBottomsheet() {
        var sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
        }
    }


    private fun getRoomImagesList(imagePath: String?) {
        val ut = RoomGetTask(imagePath)
        ut.delegate = this@PreViewActivity
        ut.execute()
    }

    private fun saveToRoom() {
        //creating a pdf entity
        val ut = RoomTask(resultUri,pdfPath)
        ut.execute()
    }


    private fun deleteFromRoom(imgUri: String) {
        val ut = RoomDeleteTask(imgUri)
        ut.delegate = this@PreViewActivity
        ut.execute()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                //                val picBitmap: Bitmap = BitmapFactory.decodeFile(photoFile?.path)
//                    UCrop.of(Uri.fromFile(photoFile),Uri.fromFile(getExternalFilesDir(Environment.DIRECTORY_DCIM)))
//                        .start(this);
                if (data != null) {
                    resultUri =data!!.getStringExtra("filePath")
//                    resultUri = File(data!!.getStringExtra("filePath"))
                    saveToRoom()
                }


            }
        }
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


    fun calculateNoOfColumns(
        columnWidthDp: Float
    ): Int { // For example columnWidthdp=180
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidthDp: Float = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / columnWidthDp + 0.5).toInt()
    }

    override fun onLongPress(position: Int): Boolean {
        var sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;
        } else {
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        selectedPos = position
        imgUri = imagesPdfExistingList!![position].path
        imgFile = File(Uri.parse(imgUri)?.path)

        return true
    }

    override fun onPress(position: Int) {
        if(position==-1){
            fetchFromGallery()
        }else{
            displayFromUri()
        }
    }



    internal open class RoomTask(
        imageUri: String?,
        pdfPath: String?
    ) :
        AsyncTask<String?, Void?, Void?>() {
        var image_Path: String? = imageUri
        var mainPath = pdfPath
        var delegate: RoomOperationCompleted? = null

        override fun doInBackground(vararg strings: String?): Void? {
            val pdf = ImageEntity()
            pdf.path = image_Path.toString()

            DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                ?.pdfDao()?.updateValues(pdf,mainPath)

            Log.i(
                "RoomTask",
                "Check DB" + DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.getAll()
            )
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            delegate?.processFinish(null)
        }
    }


    internal open class RoomDeleteTask(
        imgPath: String?
    ) :
        AsyncTask<String?, Void?, Void?>() {
        var image_Path: String? = imgPath
        var delegate: RoomOperationImageCompleted? = null

        override fun doInBackground(vararg strings: String?): Void? {

            DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                ?.pdfDao()?.emptyImagesValues(image_Path)

            Log.i(
                "RoomTask",
                "Check DB" + DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.getAll()
            )
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            delegate?.processFinish(null);

        }
    }


    internal open class RoomGetTask(
        imgPath: String?
    ) :
        AsyncTask<String?, Void?, ArrayList<ImageEntity>?>() {
        var image_Path: String? = imgPath
        var delegate: RoomOperationImageCompleted? = null

        override fun doInBackground(vararg strings: String?): ArrayList<ImageEntity>? {
                return DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.findSpecificIamgesEvent(image_Path!!) as ArrayList
        }

        override fun onPostExecute(aVoid: ArrayList<ImageEntity>?) {
            super.onPostExecute(aVoid)
            delegate?.processFinish(aVoid)
        }
    }

    override fun processFinish(output: ArrayList<ImageEntity>?) {
        if (output.isNullOrEmpty() && !calledOnce) {
            calledOnce = !calledOnce
            getRoomImagesList(null)
        } else if (output?.isNotEmpty()!!) {
            imagesPdfExistingList = output
            if (imagesPdfExistingList?.isEmpty()!!
            ) {
                initialLayout.visibility = View.VISIBLE

            } else {
                imageLayout.visibility = View.VISIBLE
                initialLayout.visibility = View.GONE
                var imgEmptyEntity = ImageEntity()
                imagesPdfExistingList!!.add(imgEmptyEntity)
                pdfRecyclerView.adapter =
                    GridPdfAdapter(this@PreViewActivity, imagesPdfExistingList, this)
            }
        }
    }

    fun fetchFromGallery(){
        val intent =
            Intent(this@PreViewActivity, GetPhotoActivity::class.java)
        intent.putExtra("Gallery", "Gallery")
        startActivityForResult(
            intent,
            REQUEST_TAKE_PHOTO
        )
    }

}