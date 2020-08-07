package com.src.uscan.ui

import android.Manifest
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
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
import android.widget.Toast
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
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.src.uscan.BuildConfig
import com.src.uscan.R
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.MySharedPreferences
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.custom_toolbar.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), LongPressListener {

    private val OR_GRID: Int = 1
    private val OR_LIST: Int = 0
    private var selectedPos: Int = 0
    private val GOOGLE_PHOTOS_PACKAGE_NAME: String? = "com.google.android.apps.photos"
    private var imgUri: String = ""
    private val REQUEST_GALLERY_PHOTO: Int = 112
    private val REQUEST_TAKE_PHOTO: Int = 111
    var photoFile: File? = null
    var mAdapter: DocumentAdapter? = null
    var imagePaths: ArrayList<String>? = ArrayList()
    var imgFile: File? = null


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
        docsGrid.layoutManager =
            GridLayoutManager(this@MainActivity, calculateNoOfColumns(200f))
        mAdapter = DocumentAdapter(this@MainActivity, imagePaths, this,OR_GRID)
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

        move.setOnClickListener {

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


        if (intent.hasExtra("image")) {
            imagePaths?.add(intent.getStringExtra("image")+","+intent.getStringExtra("image_time"))
            saveImagesInList()
            getImageArrayList()
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
        var searchList = MySharedPreferences.getInstance(this@MainActivity).arrayList
        searchSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                val newList: ArrayList<String> = ArrayList()
                for (i in 0 until searchList?.size!!) {
                    if (!searchList[i].isNullOrBlank() && searchList[i].contains(s.toLowerCase())) {
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

    private fun changeLayoutToGrid() {
        docsGrid.layoutManager =
            LinearLayoutManager(this@MainActivity, VERTICAL,false)
        mAdapter = DocumentAdapter(this@MainActivity, imagePaths, this,OR_LIST)
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
        imagePaths?.remove(imagePaths?.get(selectedPos))
        MySharedPreferences.getInstance(this).arrayList = imagePaths
        docsGrid.adapter = DocumentAdapter(this, imagePaths, this, OR_GRID)
        return baseContext.deleteFile(imgFile?.name)
    }

    private fun deleteDialog() {
        val builder = AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure you want to delete?");
        builder.setPositiveButton("continue", DialogInterface.OnClickListener { dialog, which ->
            Log.i("Delete", "Result" + deleteSelectedFile())
            mAdapter?.notifyDataSetChanged()
        });
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            finish()

        });
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


    private fun selectImage() {
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        builder.setTitle("Add Photo!")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    Dexter.withContext(this)
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(object : PermissionListener {
                            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                                dispatchTakePictureIntent()
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                p0: PermissionRequest?,
                                p1: PermissionToken?
                            ) {
                            }

                            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                            }

                        }).check()
                }
                options[item] == "Choose from Gallery" -> {
                    dispatchGalleryIntent()
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }


    /**
     * Capture image from camera
     */
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the File where the photo should go
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                ex.printStackTrace()
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile!!
                )
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                val intent =
                    Intent(this, GetPhotoActivity::class.java)
                startActivityForResult(
                    intent,
                    REQUEST_TAKE_PHOTO
                )
            }
        }
    }


    /**
     * Select image fro gallery
     */
    private fun dispatchGalleryIntent() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    val pickPhoto = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    pickPhoto.setPackage(GOOGLE_PHOTOS_PACKAGE_NAME);
                    startActivityForResult(pickPhoto, REQUEST_GALLERY_PHOTO)

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                }
            }).check()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
        when (requestCode) {
            REQUEST_TAKE_PHOTO -> {
                //                val picBitmap: Bitmap = BitmapFactory.decodeFile(photoFile?.path)
//                    UCrop.of(Uri.fromFile(photoFile),Uri.fromFile(getExternalFilesDir(Environment.DIRECTORY_DCIM)))
//                        .start(this);
                if(data!=null){
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

        if (MySharedPreferences.getInstance(this@MainActivity).arrayList != null && MySharedPreferences.getInstance(
                this@MainActivity
            ).arrayList.isNotEmpty()
        ) {
            imageLayout.visibility = VISIBLE
            initialLayout.visibility = GONE
            imagePaths = getImageArrayList()
            docsGrid.adapter = DocumentAdapter(this@MainActivity, imagePaths, this, OR_GRID)
        } else {
            initialLayout.visibility = VISIBLE
        }

    }

    private fun getImageArrayList(): java.util.ArrayList<String>? {
        var arr = MySharedPreferences.getInstance(this@MainActivity).arrayList
        if(arr.contains("")){
            Collections.swap(arr,arr.indexOf(""),arr.lastIndex)
        }
        Log.i("Last","item swapped"+arr[arr.lastIndex])
        return arr
    }

    private fun saveImagesInList() {
        if (MySharedPreferences.getInstance(this@MainActivity).arrayList.isNullOrEmpty()) {
            MySharedPreferences.getInstance(this@MainActivity).arrayList = imagePaths
            initialLayout.visibility = VISIBLE
        } else {
            initialLayout.visibility = GONE
            imageLayout.visibility = VISIBLE
            var imagePathsTemp: ArrayList<String> =
                MySharedPreferences.getInstance(this@MainActivity).arrayList
            imagePathsTemp.addAll(imagePaths!!)
            imagePathsTemp.add("")
            MySharedPreferences.getInstance(this@MainActivity).arrayList = imagePathsTemp
            Log.i("Size", "" + imagePathsTemp.lastIndex)

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
        imgUri = imagePaths!![position].split(",")[0]
        imgFile = File(Uri.parse(imgUri)?.path)

        return true
    }

    override fun onPress(position: Int) {
        if (position == -1) {
            val intent =
                Intent(this@MainActivity, GetPhotoActivity::class.java)
            intent.putExtra("Gallery", "Gallery")
            startActivityForResult(
                intent,
                REQUEST_TAKE_PHOTO
            )
        }
//        var intent = Intent(Intent.ACTION_GET_CONTENT);
//        intent.type = "file/pdf";
//        intent.addCategory(Intent.CATEGORY_OPENABLE)
//        startActivityForResult(intent, 114);
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

    fun createDirectory() {
        val file = File(
            Environment.getExternalStorageDirectory().toString() + "/Uscan_more"
        )
        val success = true
        if (!file.exists()) {
            Toast.makeText(
                applicationContext, "Directory does not exist, create it",
                Toast.LENGTH_LONG
            ).show()
        }
        Toast.makeText(
            application, "Directory created",
            Toast.LENGTH_LONG
        ).show()
    }


    fun openPdf(){
        val file = File(
            filesDir,"Uscan"
        )
        if (file.exists()) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.fromFile(file)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this@MainActivity,
                    "No Application available to view pdf",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}