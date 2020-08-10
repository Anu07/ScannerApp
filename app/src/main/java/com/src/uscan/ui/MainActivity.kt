package com.src.uscan.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.src.uscan.BuildConfig
import com.src.uscan.R
import com.src.uscan.UscanApplication.Companion.applicationContext
import com.src.uscan.room.DatabaseClient
import com.src.uscan.room.PDFEntity
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.MySharedPreferences
import com.src.uscan.utils.RoomOperationCompleted
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_share_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet.bottom_sheet
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), LongPressListener, RoomOperationCompleted {
    private var pdfPath: String=""
    private var orientation: Int = 0
    var popup: PopupMenu? = null
    private var calledOnce: Boolean = false
    private var imagesExistingList: ArrayList<PDFEntity>? = ArrayList()
    var imagePath: String? = ""
    var imageTime: String? = ""
    var pdf: String? = ""
    private val OR_GRID: Int = 1
    private val OR_LIST: Int = 0
    private var selectedPos: Int = 0
    private var imgUri: String = ""
    private val REQUEST_TAKE_PHOTO: Int = 111
    var photoFile: File? = null
    var mAdapter: DocumentAdapter? = null
    var imgFile: File? = null
    val TAG = MainActivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->

//            selectImage()
            val intent =
                Intent(this, GetPhotoActivity::class.java)
            startActivityForResult(
                intent,
                REQUEST_TAKE_PHOTO
            )
        }
        MySharedPreferences.getInstance(this@MainActivity)
            .put(MySharedPreferences.Key.ORIENTATION_SELECTED, orientation)
        docGridInitialize()

        //bottom Sheet controls
        shareApp.setOnClickListener {
            hideBottomsheet()
            var sheetBehavior = BottomSheetBehavior.from(bottom_share_sheet);
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED;
                HandleFabMargin(true)
            } else {
                HandleFabMargin(false)
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }


        sharePdf.setOnClickListener {
            hideBottomShareSheet()
            shareContent()          //share PDF
        }

        overflow_menu.setOnClickListener {
            //Creating the instance of PopupMenu

            //Creating the instance of PopupMenu
            popup = PopupMenu(this@MainActivity, overflow_menu)
            //Inflating the Popup using xml file
            //Inflating the Popup using xml file
            popup?.menuInflater?.inflate(R.menu.menu_pop, popup?.getMenu())


            val item: MenuItem? = popup?.menu?.getItem(1) // here itemIndex is int

            if (MySharedPreferences.getInstance(this@MainActivity)
                    .getInt(MySharedPreferences.Key.ORIENTATION_SELECTED, -1) == 0
            ) {
                orientation = OR_LIST
                item?.title = "GridView"
                docsGrid.layoutManager =
                    LinearLayoutManager(this@MainActivity)
                mAdapter = DocumentAdapter(this@MainActivity, imagesExistingList, this, OR_LIST)
                docsGrid.adapter = mAdapter

            } else {
                orientation = OR_GRID
                item?.title = "ListView"
                docsGrid.layoutManager =
                    GridLayoutManager(this@MainActivity, 3)
                mAdapter = DocumentAdapter(this@MainActivity, imagesExistingList, this, OR_GRID)
                docsGrid.adapter = mAdapter
            }

            popup?.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        R.id.grid -> {
                            changeLayoutToGrid(item)
//                            createDirectory()
                        }
                        R.id.two -> {
                            val intent =
                                Intent(this@MainActivity, GetPhotoActivity::class.java)
                            intent.putExtra("Gallery", "Gallery")
                            startActivityForResult(
                                intent,
                                REQUEST_TAKE_PHOTO
                            )
                        }
                    }
                    return true
                }

            })

            popup?.show() //showing popup menu

        }

        saveGallery.setOnClickListener {
//            CommonUtils().copyFile(imgFile!!.path,this)
        }
        delete.setOnClickListener {
            hideBottomsheet()
            deleteDialog()
        }

        more.setOnClickListener {
            progress.visibility = VISIBLE
            hideBottomsheet()
            startActivity(
                Intent(this@MainActivity, FilterActivity::class.java).putExtra(
                    "image",
                    imgUri
                )
            )
        }
        getRoomImagesList(null)


        if (intent.hasExtra("image")) {

            saveToRoom()
//            saveImagesInList()
            Handler().postDelayed({
                getRoomImagesList(null)
            }, 2000)
            initialLayout.visibility = GONE
            mAdapter?.notifyDataSetChanged()
            imageLayout.visibility = VISIBLE
        }
        val adRequest = AdRequest.Builder().build()
        adView1.loadAd(adRequest)


        /*searchSV.setOnCloseListener {
            Log.i("Test","close click")
        }*/
        val searchManager: SearchManager =
            getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchSV.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        var searchList = imagesExistingList
        searchSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                val newList: ArrayList<PDFEntity> = ArrayList()
                for (i in 0 until searchList?.size!!) {
                    if (!searchList[i].path.isNullOrBlank() && searchList[i].path.contains(s.toLowerCase())) {
                        newList?.add(searchList[i])
                    }
                }
                mAdapter = DocumentAdapter(this@MainActivity, newList, this@MainActivity, OR_GRID)
                docsGrid.adapter = mAdapter
//                mAdapter!!.notifyDataSetChanged()

                return true
            }
        })

    }

    private fun docGridInitialize() {
        orientation = OR_LIST
        docsGrid.layoutManager =
            LinearLayoutManager(this@MainActivity)
        mAdapter = DocumentAdapter(this@MainActivity, imagesExistingList, this, orientation)
        docsGrid.adapter = mAdapter
    }

    private fun getRoomImagesList(imagePath: String?) {
        val ut = RoomGetTask(imagePath)
        ut.delegate = this@MainActivity
        ut.execute()
    }

    private fun saveToRoom() {
        //creating a pdf entity
        imagePath = intent.getStringExtra("image")
        imageTime = intent.getStringExtra("image_time")
        pdf = intent.getStringExtra("PDF")
        val ut = RoomTask(imagePath, imageTime, pdf)
        ut.execute()
    }


    private fun deleteFromRoom(imgUri: String) {
        //creating a pdf entity
        val ut = RoomDeleteTask(imgUri)
        ut.execute()
    }


    private fun changeLayoutToGrid(item: MenuItem) {
        if (popup != null && orientation == OR_LIST) {

            orientation = OR_GRID
            item?.title = "ListView"
            MySharedPreferences.getInstance(this@MainActivity)
                .put(MySharedPreferences.Key.ORIENTATION_SELECTED, orientation)
            docsGrid.layoutManager =
                GridLayoutManager(this@MainActivity, 3)
            mAdapter = DocumentAdapter(this@MainActivity, imagesExistingList, this, orientation)
            docsGrid.adapter = mAdapter
        } else {
            orientation = OR_LIST
            MySharedPreferences.getInstance(this@MainActivity)
                .put(MySharedPreferences.Key.ORIENTATION_SELECTED, orientation)
            item?.title = "GridView"
            docsGrid.layoutManager =
                LinearLayoutManager(this@MainActivity)
            mAdapter = DocumentAdapter(this@MainActivity, imagesExistingList, this, orientation)
            docsGrid.adapter = mAdapter
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

    private fun deleteSelectedFile(): Boolean {
        deleteFromRoom(pdfPath)
        Handler().postDelayed({
            getRoomImagesList(null)
        }, 2000)
        return baseContext.deleteFile(imgFile?.name)
    }

    private fun deleteDialog() {
        val builder = AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("continue") { dialog, which ->
            Log.i("Delete", "Result" + deleteSelectedFile())
//            mAdapter?.notifyDataSetChanged()
        };
        builder.setNegativeButton("Cancel") { dialog, which ->
            finish()
        };
        builder.show()
    }

    private fun hideBottomsheet() {
        var sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
            HandleFabMargin(false)
        }
    }


    private fun hideBottomShareSheet() {
        var sheetBehavior = BottomSheetBehavior.from(bottom_share_sheet);
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
            HandleFabMargin(false)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            else -> super.onOptionsItemSelected(item)
        }
    }


    /**
     * Create file with current timestamp name
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val mFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(mFileName, ".jpg", storageDir)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                if (data != null && !data.hasExtra("GalleryPreview")) {
                    var resultUri = File(data!!.getStringExtra("filePath"))
                    var intent = Intent(this@MainActivity, FilterActivity::class.java)
                    intent.putExtra("image", resultUri.toString())
                    startActivity(intent)
                }
            }


        }
    }


    override fun onResume() {
        super.onResume()
        if (progress.isVisible) {
            progress.visibility = GONE
            hideBottomsheet()
            hideBottomShareSheet()
        }

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
            HandleFabMargin(true)
        } else {
            HandleFabMargin(false)
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
        selectedPos = position
        imgUri = imagesExistingList!![position].path
        imgFile = File(Uri.parse(imgUri)?.path)
        pdfPath = imagesExistingList!![position].pdfPath
        return true
    }


    override fun onPress(position: Int) {
        imagesExistingList?.get(position)?.let { openPdf(it) }
    }

    private fun HandleFabMargin(b: Boolean) {
        if (b) {
            val params = CoordinatorLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.BOTTOM or Gravity.END
            params.setMargins(46, 46, 46, 150)
            fab.layoutParams = params
        } else {
            val params = CoordinatorLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.BOTTOM or Gravity.END
            params.setMargins(46, 46, 46, 60)
            fab.layoutParams = params
        }

    }


    private fun shareContent() {
        Log.e("MainActivity",""+pdfPath)
        val outputFile = File(
            pdfPath
        )
        val uri = FileProvider.getUriForFile(this@MainActivity, applicationContext.packageName + ".provider", outputFile);
        val share = Intent()
        share.action = Intent.ACTION_SEND
        share.type = "application/pdf"
        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share"));
    }

    fun openPdf(pdf: PDFEntity) {
        var intent = Intent(this@MainActivity, PreViewActivity::class.java)
        intent.putExtra("PDF", pdf.pdfPath)         //pdf uri as string
        intent.putExtra("IMG", pdf.path)        //path of image in pdf
        startActivity(intent)
    }

    internal open class RoomTask(
        imagePath: String?,
        imageTime: String?,
        pdf: String?
    ) :
        AsyncTask<String?, Void?, Void?>() {
        var image_Path: String? = imagePath
        var image_Time: String? = imageTime
        var pdf_: String? = pdf
        var delegate: RoomOperationCompleted? = null

        override fun doInBackground(vararg strings: String?): Void? {
            var tempArray = ArrayList<String>()
            tempArray.add(image_Path!!)
            val pdf = PDFEntity()
            pdf.path = image_Path.toString()
            pdf.time = image_Time.toString()
            pdf.pdfPath = pdf_.toString()
            pdf.images = tempArray

            DatabaseClient.getInstance(applicationContext())?.appDatabase
                ?.pdfDao()?.insert(pdf)

            Log.i(
                "RoomTask",
                "Check DB" + DatabaseClient.getInstance(applicationContext())?.appDatabase
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

        override fun doInBackground(vararg strings: String?): Void? {

            DatabaseClient.getInstance(applicationContext())?.appDatabase
                ?.pdfDao()?.deletePdf(image_Path)

            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
        }
    }


    internal open class RoomGetTask(
        imgPath: String?
    ) :
        AsyncTask<String?, Void?, ArrayList<PDFEntity>?>() {
        var image_Path: String? = imgPath
        var delegate: RoomOperationCompleted? = null

        override fun doInBackground(vararg strings: String?): ArrayList<PDFEntity>? {
            if (image_Path != null) {
                val pdf = PDFEntity()
                pdf.path = image_Path.toString()
                return DatabaseClient.getInstance(applicationContext())?.appDatabase
                    ?.pdfDao()?.findSpecificEvent(pdf.path) as ArrayList
            }
            return DatabaseClient.getInstance(applicationContext())?.appDatabase
                ?.pdfDao()?.getAll() as ArrayList
        }

        override fun onPostExecute(aVoid: ArrayList<PDFEntity>?) {
            super.onPostExecute(aVoid)
            delegate?.processFinish(aVoid)
        }
    }


    internal open class RoomDeleteDatabase(
    ) :
        AsyncTask<String?, Void?, Void?>() {

        override fun doInBackground(vararg strings: String?): Void? {
            //clear all data
            DatabaseClient.getInstance(applicationContext())?.appDatabase
                ?.pdfDao()?.emptyPDFTable()

            Log.i(
                "RoomTask",
                "Check DB" + DatabaseClient.getInstance(applicationContext())?.appDatabase
                    ?.pdfDao()?.getAll()
            )
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
        }
    }


    override fun processFinish(output: ArrayList<PDFEntity>?) {
        if (output.isNullOrEmpty() && !calledOnce) {
            calledOnce = !calledOnce
            getRoomImagesList(null)
        } else if (output?.isNotEmpty()!!) {
            imagesExistingList = output
            if (imagesExistingList?.isEmpty()!!
            ) {
                initialLayout.visibility = VISIBLE
                var deleteTask = RoomDeleteDatabase()
                deleteTask.execute()
            } else {
                imageLayout.visibility = VISIBLE
                initialLayout.visibility = GONE
                docsGrid.adapter =
                    DocumentAdapter(this@MainActivity, imagesExistingList, this, orientation)
            }
        }else{
            mAdapter?.notifyDataSetChanged()
        }
    }
    //clear all data
//    DatabaseClient.getInstance(UscanApplication.applicationContext())?.appDatabase
//    ?.pdfDao()?.emptyPDFTable()


    override fun onBackPressed() {
        var sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
        var shareSheetBehavior = BottomSheetBehavior.from(bottom_share_sheet);
        if (sheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            hideBottomsheet()
        }else if(shareSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            hideBottomShareSheet()
        }else{
            super.onBackPressed()
        }

    }
}