package com.src.uscan.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import com.src.uscan.R
import com.src.uscan.room.PDFEntity
import com.src.uscan.utils.DateUtils
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.PdfRequestHandler
import java.io.File


class DocumentAdapter(
    val context: Context,
    values: ArrayList<PDFEntity>?,
    val mListener: LongPressListener,
    val orientation: Int
) :
    RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
    var mValues: ArrayList<PDFEntity>? = values
    var mContext: Context = context

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var imageView: ImageView = v.findViewById(R.id.imgDoc) as ImageView
//        var imgpdf: ImageView = v.findViewById(R.id.imgpdf) as ImageView
        var docName: TextView = v.findViewById(R.id.docName) as TextView
        var subDocName: TextView = v.findViewById(R.id.subdocDetail) as TextView
        var relativeLayout: LinearLayout = v.findViewById(R.id.linearParent)
        var detailLAyout : LinearLayout = v.findViewById(R.id.detailLayout)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = if(orientation == 0){
            LayoutInflater.from(mContext).inflate(R.layout.item_list_img, parent, false)
        }else{
            LayoutInflater.from(mContext).inflate(R.layout.item_grid_outer_img, parent, false)
        }
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mValues?.size!!
    }

    var picassoInstance: Picasso? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mValues!![position].path.isNotEmpty()) {
            holder.detailLAyout.visibility = VISIBLE
            Picasso.get().load(Uri.parse(mValues!![position].path))
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(holder.imageView)
            holder.docName.text = File(Uri.parse(mValues!![position].path)?.path).name
            holder.subDocName.text = DateUtils.convertTime(mValues!![position].time.toLong())


            val extension: String = mValues!![position].path.substring(mValues!![position].path.lastIndexOf("."))

            if(extension == ".pdf"){

                picassoInstance = Picasso.Builder(context.applicationContext)
                    .addRequestHandler(PdfRequestHandler())
                    .build()

                picassoInstance!!.load(PdfRequestHandler.SCHEME_PDF+":"+mValues!![position].pdfPath)
                    .fit()
                    .into(holder.imageView)
            }


            holder.docName.text = File(Uri.parse(mValues!![position].path)?.path).name
            holder.relativeLayout.setOnLongClickListener {
                mListener.onLongPress(position)
            }
            holder.relativeLayout.setOnClickListener {
                mListener.onPress(position)
            }
        }else{
            holder.detailLAyout.visibility = GONE
        }

    }



}

