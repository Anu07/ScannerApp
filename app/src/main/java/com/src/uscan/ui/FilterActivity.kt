package com.src.uscan.ui

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.src.uscan.R
import kotlinx.android.synthetic.main.activity_filter_preview.*
import net.alhazmy13.imagefilter.ImageFilter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class FilterActivity : AppCompatActivity() {
    var originalMap: Bitmap? = null
    private var selectedPos: Int = 0
    var bMap: Bitmap? = null
    private var outPutFile: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_filter_preview)
        supportActionBar?.hide()
        outPutFile =  File(intent.getStringExtra("image"))
        try {
            originalMap = getScaledBitmap( outPutFile!!.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        preViewImage.setImageBitmap(originalMap)
        saveImg.setOnClickListener {
            val intent = Intent(this@FilterActivity, MainActivity::class.java)
            intent.putExtra("image",generateSelectedImageBitMap(selectedPos)!!.toString())
            startActivity(intent)
            finish()
        }
        val adRequest = AdRequest.Builder().build()
        adView1.loadAd(adRequest)
        filter0.setImageBitmap(originalMap)
        filter1.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.OIL))
        filter2.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.SKETCH))
        filter3.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.GRAY))
        filter4.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.HDR))
        filter0.setOnClickListener {
            selectedPos = 0
            preViewImage.setImageBitmap(originalMap)
          /*  Picasso.get().load(outPutFile!!)
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(preViewImage)*/
        }
        filter1.setOnClickListener {
            selectedPos = 1
            preViewImage.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.OIL))

         /*   Picasso.get().load(getImageUri(this,ImageFilter.applyFilter(originalMap, ImageFilter.Filter.OIL)))
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(preViewImage)*/
        }
        filter2.setOnClickListener {
            selectedPos = 2
            preViewImage.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.SKETCH))
           /* Picasso.get().load(getImageUri(this,ImageFilter.applyFilter(originalMap, ImageFilter.Filter.SKETCH)))
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(preViewImage)*/
        }
        filter3.setOnClickListener {
            selectedPos = 3
            preViewImage.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.GRAY))

            /*Picasso.get().load(getImageUri(this,ImageFilter.applyFilter(originalMap, ImageFilter.Filter.GRAY)))
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(preViewImage)*/
        }
        filter4.setOnClickListener {
            selectedPos = 4
            preViewImage.setImageBitmap(ImageFilter.applyFilter(originalMap, ImageFilter.Filter.HDR))
/*
            Picasso.get().load(getImageUri(this,ImageFilter.applyFilter(originalMap, ImageFilter.Filter.HDR)))
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(preViewImage)*/
        }

    }

    private fun getBitmapFromUri(bMapUri: Uri?): Bitmap? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        return BitmapFactory.decodeFile(getRealPathFromURI(this,bMapUri), options)
    }


    private fun generateSelectedImageBitMap(pos: Int): Uri? {
        when (pos) {
            0 -> {
                bMap = originalMap
            }
            1 -> {
                bMap = ImageFilter.applyFilter(originalMap, ImageFilter.Filter.OIL)

            }
            2 -> {
                bMap = ImageFilter.applyFilter(originalMap, ImageFilter.Filter.SKETCH)
            }
            3 -> {
                bMap = ImageFilter.applyFilter(originalMap, ImageFilter.Filter.GRAY)

            }
            4 -> {
                bMap = ImageFilter.applyFilter(originalMap, ImageFilter.Filter.HDR)
            }
        }
        return SaveImage(bMap!!)
    }

    private fun SaveImage(finalBitmap: Bitmap): Uri? {

        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val root = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(root, "$imageFileName.jpg")
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
            Log.i("PAth", "Filepath" + file.path)
            bMap?.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Uri.fromFile(file)
    }


    private fun getImageUri(
        context: Context,
        inImage: Bitmap
    ): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = try {
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                inImage,
                "Title",
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Uri.parse(path as String?)
    }


    private fun getRealPathFromURI(
        context: Context,
        contentUri: Uri?
    ): String? {
        var cursor: Cursor? = null
        return try {
            val proj =
                arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri!!, proj, null, null, null)
            val column_index: Int = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)!!
            cursor?.moveToFirst()
            cursor?.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    /**
     * Decode a scaled image
     *
     * Reduce the amount of dynamic heap used by expanding the JPEG into a memory array that's already scaled to match the size of the destination view
     *
     * @param mImageView        destination view
     * @param mCurrentPhotoPath path of the image
     * @return scaled bitmap
     */
    fun getScaledBitmap(
        mCurrentPhotoPath: String?
    ): Bitmap? {
        // Get the dimensions of the View
        val targetW = 200
        val targetH = 300

        // Get the dimensions of the bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        bmOptions.inPurgeable = true
        return BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions)
    }

}