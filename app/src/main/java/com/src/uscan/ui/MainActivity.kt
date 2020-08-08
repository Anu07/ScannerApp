package com.src.uscan.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.src.uscan.BuildConfig
import com.src.uscan.R
import com.src.uscan.UscanApplication.Companion.applicationContext
import com.src.uscan.room.DatabaseClient
import com.src.uscan.room.PDFEntity
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.RoomOperationCompleted
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), LongPressListener, RoomOperationCompleted {

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
        docGridInitialize()

        //bottom Sheet controls
        share.setOnClickListener {
            shareContent(imgUri)
        }

        overflow_menu.setOnClickListener {
            //Creating the instance of PopupMenu

            //Creating the instance of PopupMenu
            val popup = PopupMenu(this@MainActivity, overflow_menu)
            //Inflating the Popup using xml file
            //Inflating the Popup using xml file
            popup.menuInflater.inflate(R.menu.menu_pop, popup.getMenu())

            //registering popup with OnMenuItemClickListener

            //registering popup with OnMenuItemClickListener
            popup.setOnMenuItemClickListener(object : MenuItem.OnMenuItemClickListener,
                PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        R.id.grid -> {
                            changeLayoutToGrid()
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
                        R.id.three -> {
                            shareContent()
                        }
                    }
                    return true
                }

            })

            popup.show() //showing popup menu

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
        docsGrid.layoutManager =
            GridLayoutManager(this@MainActivity, calculateNoOfColumns(200f))
        mAdapter = DocumentAdapter(this@MainActivity, imagesExistingList, this, OR_GRID)
        docsGrid.adapter = mAdapter
        docsGrid.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
        ut.delegate = this@MainActivity
        ut.execute()
    }


    private fun changeLayoutToGrid() {
        docsGrid.layoutManager =
            LinearLayoutManager(this@MainActivity, VERTICAL, false)
        mAdapter = DocumentAdapter(this@MainActivity, imagesExistingList, this, OR_LIST)
        docsGrid.adapter = mAdapter
    }


    private fun shareContent() {
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
        deleteFromRoom(imgUri)
        docsGrid.adapter = DocumentAdapter(this, imagesExistingList, this, OR_GRID)
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

    private fun hideBottomsheet() {
        var sheetBehavior = BottomSheetBehavior.from(bottom_sheet);
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
//        if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                //                val picBitmap: Bitmap = BitmapFactory.decodeFile(photoFile?.path)
//                    UCrop.of(Uri.fromFile(photoFile),Uri.fromFile(getExternalFilesDir(Environment.DIRECTORY_DCIM)))
//                        .start(this);
                if (data != null) {
                    var resultUri = File(data!!.getStringExtra("filePath"))
//                     = UCrop.getOutput(data!!)
                    var intent = Intent(this@MainActivity, FilterActivity::class.java)
                    intent.putExtra("image", resultUri.toString())
                    startActivity(intent)

                }


            }

//                UCrop.REQUEST_CROP -> {
//                    when (resultCode) {
//                        RESULT_OK -> {
//                            var resultUri = UCrop.getOutput(data!!)
//                            var intent = Intent(this@MainActivity, FilterActivity::class.java)
//                            intent.putExtra("image", resultUri.toString())
//                            startActivity(intent)
//                        }
//                        UCrop.RESULT_ERROR -> {
//                            var error = UCrop.getError(data!!)
//                            Toast.makeText(this@MainActivity, error?.message, Toast.LENGTH_SHORT).show()
//                        }
//                    }
////                }
//               /* 114 -> {
//                    val data = intent.data
//                    Log.i("Main", "File path: " + data!!.path)
//                    val pdfIntent = Intent(this, PreViewActivity::class.java)
//                    pdfIntent.data = data
//                    startActivity(pdfIntent)
//
//                }*/
//            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (progress.isVisible) {
            progress.visibility = GONE
            hideBottomsheet()
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

        return true
    }


    override fun onPress(position: Int) {
        imagesExistingList?.get(position)?.pdfPath?.let { openPdf(it) }
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


    private fun shareContent(imagUri: String) {
        val imageUri =
            Uri.parse(imagUri)
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/jpeg"
        intent.putExtra(Intent.EXTRA_STREAM, imageUri)
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
    }

    fun openPdf(pdfPath: String) {
        startActivity(
            Intent(this@MainActivity, PreViewActivity::class.java).putExtra(
                "PDF",
                pdfPath
            )
        )
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
            val pdf = PDFEntity()
            pdf.path = image_Path.toString()
            pdf.time = image_Time.toString()
            pdf.pdfPath = pdf_.toString()

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
        var delegate: RoomOperationCompleted? = null

        override fun doInBackground(vararg strings: String?): Void? {
            val pdf = PDFEntity()
            pdf.path = image_Path.toString()

            DatabaseClient.getInstance(applicationContext())?.appDatabase
                ?.pdfDao()?.delete(pdf)

            Log.i(
                "RoomTask",
                "Check DB" + DatabaseClient.getInstance(applicationContext())?.appDatabase
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
        AsyncTask<String?, Void?, ArrayList<PDFEntity>?>() {
        var image_Path: String? = imgPath
        var delegate: RoomOperationCompleted? = null

        override fun doInBackground(vararg strings: String?): ArrayList<PDFEntity>? {
            if (image_Path!=null) {
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

    override fun processFinish(output: ArrayList<PDFEntity>?) {
        if (output.isNullOrEmpty() && !calledOnce) {
            calledOnce = !calledOnce
            getRoomImagesList(null)
        } else if (output?.isNotEmpty()!!) {
            imagesExistingList = output
            if (imagesExistingList?.isEmpty()!!
            ) {
                initialLayout.visibility = VISIBLE

            } else {
                imageLayout.visibility = VISIBLE
                initialLayout.visibility = GONE
                docsGrid.adapter =
                    DocumentAdapter(this@MainActivity, imagesExistingList, this, OR_GRID)
            }
        }
    }

}