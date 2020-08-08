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
import com.src.uscan.room.ImageEntity
import com.src.uscan.room.PDFEntity
import com.src.uscan.utils.DateUtils
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.PdfRequestHandler
import java.io.File


class GridPdfAdapter(
    val context: Context,
    values: ArrayList<ImageEntity>?,
    val mListener: LongPressListener
) :
    RecyclerView.Adapter<GridPdfAdapter.ViewHolder>() {
    var mValues: ArrayList<ImageEntity>? = values
    var mContext: Context = context

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var imageView: ImageView = v.findViewById(R.id.imgDoc) as ImageView
        var docName: TextView = v.findViewById(R.id.docName) as TextView
        var relativeLayout: RelativeLayout = v.findViewById(R.id.relative)
        var detailLayout: LinearLayout = v.findViewById(R.id.detailLayout)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_grid_img, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mValues?.size!!
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mValues!![position].path.isNotEmpty()) {
            Picasso.get().load(Uri.parse(mValues!![position].path))
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(holder.imageView)
            holder.docName.text = File(Uri.parse(mValues!![position].path)?.path).name

            holder.relativeLayout.setOnLongClickListener {
                mListener.onLongPress(position)
            }
            holder.relativeLayout.setOnClickListener {
                mListener.onPress(position)
            }
            holder.detailLayout.visibility = VISIBLE

        }else{
            holder.relativeLayout.setOnClickListener {
                mListener.onPress(-1)
            }
            holder.detailLayout.visibility = GONE
        }

    }


}

