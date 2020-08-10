package com.src.uscan.ui

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.GridLayoutManager
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.src.uscan.BuildConfig
import com.src.uscan.R
import com.src.uscan.UscanApplication
import com.src.uscan.room.DatabaseClient
import com.src.uscan.room.PDFEntity
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.RoomOperationImageCompleted
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.custom_pdf_toolbar.*
import kotlinx.android.synthetic.main.preview_activity.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PreViewActivity : AppCompatActivity(), OnPageChangeListener, OnLoadCompleteListener,
    OnPageErrorListener, LongPressListener, RoomOperationImageCompleted {

    private var newImageAdded: Boolean = false
    private var IMAGES: ArrayList<String> = ArrayList()
    private val GALLERYCODE: Int = 8888
    private var page: Int =1
    private var updatePDFPath:String?=""
    private var resultURI: Uri? = null
    private val REQUEST_TAKE_PHOTO: Int = 111
    private var imgFile: File? = null
    private var imgUri: String = ""
    private var selectedPos: Int = -1
    private var imagesPdfExistingList: ArrayList<String>? = ArrayList()
    private var mAdapter: GridPdfAdapter? = null
    private var pdfPath: String? = ""
    private var imgPdfPath: String? = ""
    var resultFileUri: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preview_activity)

        if (intent.hasExtra("image")) {
//            saveToRoom()
//            saveImagesInList()
            mAdapter?.notifyDataSetChanged()
        }

        if (intent.hasExtra("PDF")) {
            pdfPath = intent.getStringExtra("PDF")
            imgPdfPath = intent.getStringExtra("IMG")
            IMAGES.add(File(imgPdfPath).path)
            getRoomImagesList()
        }

        pdfPreview.setOnClickListener {
            if(newImageAdded){
                manipulatePdf()
            }
            displayFromUri()
        }
        backNavigation.setOnClickListener {
            if (pdfView.isVisible) {
                pdfView.visibility = GONE
                imgRecView.visibility = VISIBLE
                pdfPreview.visibility = VISIBLE
                shareView.visibility = VISIBLE
                overflow_menu.visibility = VISIBLE
            }else{
                onBackPressed()
            }
        }

        shareView.setOnClickListener {
            shareAppContent()
        }


        shareApp.setOnClickListener {
            hideBottomsheet()
            if(updatePDFPath?.isNotEmpty()!!){
                shareImageContent(updatePDFPath!!)
            }else{
                shareImageContent(pdfPath!!)
            }
        }


        delete.setOnClickListener {
            hideBottomsheet()
            deleteDialog()
        }

        more.setOnClickListener {
            hideBottomsheet()
           /* startActivity(
                Intent(this@PreViewActivity, FilterActivity::class.java).putExtra(
                    "image",
                    imgUri
                )
            )*/
        }


        overflow_menu.setOnClickListener {
            //Creating the instance of PopupMenu

            //Creating the instance of PopupMenu
            val popup = PopupMenu(this@PreViewActivity, overflow_menu)
            //Inflating the Popup using xml file
            //Inflating the Popup using xml file
            popup.menuInflater.inflate(R.menu.menu_detail, popup.menu)

            //registering popup with OnMenuItemClickListener

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        R.id.two -> {
                            val intent =
                                Intent(this@PreViewActivity, GetPhotoActivity::class.java)
                            intent.putExtra("Gallery", "Gallery")
                            startActivityForResult(
                                intent,
                                REQUEST_TAKE_PHOTO
                            )
                        }
                        R.id.three -> {
                            shareAppContent()
                        }
                    }
                    return true
                }

            })

            popup.show() //showing popup menu

        }

    }


    private fun shareImageContent(image: String) {
        val outputFile = File(
            image
        )
        val uri = FileProvider.getUriForFile(this@PreViewActivity, applicationContext.packageName + ".provider", outputFile);
        val share = Intent()
        share.action = Intent.ACTION_SEND
        share.type = "application/pdf"
        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share"));
    }


    private fun setImageGridAdapter() {
        pdfRecyclerView.layoutManager =
            GridLayoutManager(this@PreViewActivity, 2)
        mAdapter = GridPdfAdapter(this@PreViewActivity, imagesPdfExistingList, this)
        pdfRecyclerView.adapter = mAdapter
    }

    private fun deleteSelectedFile(): Boolean {
        imagesPdfExistingList?.removeAll(listOf(imgUri))       //removed selected item from list
        deleteFromRoom(pdfPath!!) //update whole list after removing this item into pdf entity
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
            getRoomImagesList()
        };
        builder.setNegativeButton("Cancel") { dialog, which ->
            finish()
        };
        builder.show()
    }



    private fun displayFromUri() {
        var pdfRoute = ""
        pdfRoute = if(updatePDFPath?.isNotEmpty()!!){
            updatePDFPath!!
        }else{
            pdfPath!!
        }
        pdfPreview.visibility = GONE
        shareView.visibility = GONE
        overflow_menu.visibility = GONE
        Log.i("PDF","Route"+pdfRoute)
        val file = File(pdfRoute)
        if (file.exists()) {
            pdfView.visibility = VISIBLE
            imgRecView.visibility = GONE
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


    private fun getRoomImagesList() {
        imagesPdfExistingList = ArrayList()
        Log.i("PDF","Path key"+pdfPath)
        val ut = RoomGetTask(pdfPath)
        ut.delegate = this@PreViewActivity
        ut.execute()
    }

    private fun saveToRoom() {
        //creating a pdf entity
        val ut = RoomTask(imgPdfPath, resultURI.toString(), pdfPath)
        ut.execute()
    }


    private fun deleteFromRoom(
        imgUri: String
    ) {
        val ut = RoomDeleteTask(imgUri, imagesPdfExistingList)
        ut.delegate = this@PreViewActivity
        ut.execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            GALLERYCODE -> {
                if (data != null) {
                    resultFileUri = File(data!!.getStringExtra("filePath"))
                    resultURI = Uri.fromFile(resultFileUri)
                    Log.i("Result", "PAth${resultFileUri!!.path}")
                    imagesPdfExistingList?.add(resultURI.toString())
                    newImageAdded = true
                    mAdapter?.notifyDataSetChanged()
                    page++
                    saveToRoom()
                    IMAGES.add(resultFileUri!!.path)
//                    updatePDF(pdfPath!!, resultFileUri!!.path)
                }
            }
        }
    }


    companion object {
        private val TAG = PreViewActivity::class.java.name
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        Log.i("Loading ", "Page")
    }

    override fun loadComplete(nbPages: Int) {
        Log.i("Loading completed", "")
    }

    override fun onPageError(page: Int, t: Throwable?) {
        Toast.makeText(this@PreViewActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
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
        imgUri = imagesPdfExistingList!![position]
        imgFile = File(Uri.parse(imgUri)?.path)
        return true
    }

    override fun onPress(position: Int) {
        if (position == -1) {
            fetchFromGallery()
        } else {
            displayFromUri()
        }
    }

//        val ut = RoomTask(imgPdfPath, resultURI.toString(), pdfPath)
    internal open class RoomTask(
        originalImgPdf: String?,
        imageUri: String?,
        pdfPath: String?
    ) :
        AsyncTask<String?, Void?, Boolean>() {
        var image_Path: String? = imageUri
        var mainPath = pdfPath
        var originalJPGFromPDf = originalImgPdf

        override fun doInBackground(vararg strings: String?): Boolean {
            var addImgPath: ArrayList<String>

            var mainListObject =
                DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.findSpecificEvent(mainPath!!)
            Log.i("ORiginal", "IMAGES Path" + mainListObject?.size)

            if(mainListObject?.size!!>0){
                if (image_Path != "null") {
                    if (mainListObject?.isNotEmpty()!! && mainListObject?.get(0)?.images != null) {
                        addImgPath = mainListObject?.get(0)?.images as ArrayList
                        addImgPath.add(image_Path!!)
                    } else {
                        addImgPath = ArrayList()
                        addImgPath.add(originalJPGFromPDf!!)
                        addImgPath.add(image_Path!!)
                    }

                    var pdfObj = PDFEntity()
                    pdfObj.id = mainListObject?.get(0)?.id!!
                    pdfObj.pdfPath = mainListObject?.get(0)?.pdfPath
                    pdfObj.path = mainListObject?.get(0)?.path
                    pdfObj.time = mainListObject?.get(0)?.time
                    pdfObj.images = addImgPath
                    Log.i("PReView", "IMAGES SIZe" + pdfObj?.images?.size)

                    DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                        ?.pdfDao()?.update(pdfObj)
                }
            }
            return true

        }
    }


    internal open class RoomDeleteTask(
        imgPath: String?,
        imagesPdfExistingList: ArrayList<String>?
    ) :
        AsyncTask<String?, Void?, Void?>() {
        var image_Path: String? = imgPath
        var listToUpdate = imagesPdfExistingList
        var delegate: RoomOperationImageCompleted? = null

        override fun doInBackground(vararg strings: String?): Void? {
            var addImgPath: ArrayList<String>

            var mainListObject =
                DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.findSpecificEvent(image_Path!!)
            addImgPath = listToUpdate!!
            var pdfObj = PDFEntity()
            pdfObj.id = mainListObject?.get(0)?.id!!
            pdfObj.pdfPath = mainListObject?.get(0)?.pdfPath
            pdfObj.path = mainListObject?.get(0)?.path
            pdfObj.time = mainListObject?.get(0)?.time
            pdfObj.images = addImgPath
            Log.i("PReView", "IMAGES SIZe" + pdfObj?.images?.size)
            DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                ?.pdfDao()?.deleteImages(image_Path)
            DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                ?.pdfDao()?.insert(pdfObj)

            Log.i(
                "RoomTask",
                "Check DB after deletion" + DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.getAll()
            )
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            delegate?.processFinishImages(null);

        }
    }


    internal open class RoomGetTask(
        imgPath: String?
    ) :
        AsyncTask<String?, Void?, ArrayList<String>?>() {
        var image_Path: String? = imgPath
        var delegate: RoomOperationImageCompleted? = null

        override fun doInBackground(vararg strings: String?): ArrayList<String>? {
            Log.e("PDF", "Path" + image_Path)
            var listFetched =
                DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.findSpecificIamgesEvent(image_Path!!) as ArrayList
            Log.e("Records", "not found" + listFetched.size)
            return listFetched
        }

        override fun onPostExecute(aVoid: ArrayList<String>?) {
            super.onPostExecute(aVoid)
            delegate?.processFinishImages(aVoid)
        }
    }


    private fun shareAppContent() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Uscan")
            var shareMessage =
                "\nLet me recommend you this amazing application for document processing\n\n"
            shareMessage =
                """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }


    private fun fetchFromGallery() {
        val intent =
            Intent(this@PreViewActivity, GetPhotoActivity::class.java)
        intent.putExtra("GalleryPreview", "GalleryPreview")
        startActivityForResult(
            intent,
            GALLERYCODE
        )
    }

    override fun processFinishImages(output: ArrayList<String>?) {
        if (output.isNullOrEmpty()) {
            getRoomImagesList()
        } else if (output != null && output?.isNotEmpty()!!) {
            imagesPdfExistingList = splitListToImagePath(output)
            IMAGES = ArrayList()
            imagesPdfExistingList?.let { IMAGES.addAll(it) }
            Log.i("Check","size"+IMAGES.size)
            imgRecView.visibility = View.VISIBLE
            if (imagesPdfExistingList.isNullOrEmpty()) {
                imagesPdfExistingList?.add(imgPdfPath!!)
                imagesPdfExistingList?.add("")
                setImageGridAdapter()
            } else {
                page = imagesPdfExistingList?.size!!
                if (imagesPdfExistingList?.contains("")!!) {
                    imagesPdfExistingList?.remove("")           //previously added ""
                }
                imagesPdfExistingList!!.add("")
                setImageGridAdapter()
            }
        }
    }

    private fun splitListToImagePath(output: java.util.ArrayList<String>): java.util.ArrayList<String>? {
        var resultList: ArrayList<String> = ArrayList()
        if (output[0] != null && output[0].isNotEmpty()) {
            var resultStr = output[0].split(",")
            resultList = ArrayList(resultStr)
            resultList.removeAll(listOf("null"))
            resultList.remove("")
        }
        Log.i("PReviewActivity", "" + resultList.size)
        return resultList
    }

    @Throws(java.lang.Exception::class)
    protected fun manipulatePdf() {

        var destPdf = getDestinationPdfPath()
        var image =
            Image(ImageDataFactory.create(IMAGES.get(0)))
        val pdfDoc =
            PdfDocument(PdfWriter(destPdf))
        val doc = Document(
            pdfDoc,
            PageSize(image.imageWidth, image.imageHeight)
        )
        for (i in 0 until IMAGES.size) {
            if(IMAGES[i]!=""){
                image = Image(ImageDataFactory.create(IMAGES[i]))
                pdfDoc.addNewPage(PageSize(image.imageWidth, image.imageHeight))
                image.setFixedPosition(i + 1, 0f, 0f)
                doc.add(image)
            }
        }
        doc.close()
        val f =
            File(pdfPath)
        Log.e("Old file deleted","status"+f.delete())
        updatePDFPath = destPdf
        updatePDFPathInDB()
        pdfPath = destPdf
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(newImageAdded){
            manipulatePdf()
        }
    }


    override fun onResume() {
        super.onResume()
        getRoomImagesList()
    }


    private fun updatePDFPathInDB() {
        var updateThread = RoomUpdatePDF(pdfPath,updatePDFPath)
        updateThread.execute()
    }

    private fun getDestinationPdfPath(): String {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "PDF_" + timeStamp + "_"
        val root = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(root, "$imageFileName.pdf")
        return file.path
    }




    internal open class RoomUpdatePDF(
        originalPdfPath:String?,
        updatedPdfPath: String?
    ) :
        AsyncTask<String?, Void?, Void?>() {
        var oldPath = originalPdfPath
        var updatedPDFPath = updatedPdfPath

        override fun doInBackground(vararg strings: String?): Void? {

                DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
                    ?.pdfDao()?.updatePDFPath(oldPath!!,updatedPDFPath)

            return null
        }

    }


    private fun deletefile(uri: Uri, filename: String) {
        val pickedDir: DocumentFile = DocumentFile.fromTreeUri(this, uri)!!
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        val file: DocumentFile? = pickedDir.findFile(filename)
        if (file?.delete()!!) Log.d(
            "Log ID",
            "Delete successful"
        ) else Log.d("Log ID", "Delete unsuccessful")
    }
}