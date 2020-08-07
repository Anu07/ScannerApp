package com.src.uscan.ui

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import com.squareup.picasso.Picasso
import com.src.uscan.R
import com.src.uscan.utils.LongPressListener
import com.src.uscan.utils.PdfRequestHandler
import java.io.File


class DocumentAdapter(
    val context: Context,
    values: ArrayList<String>?,
    val mListener: LongPressListener
) :
    RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
    var mValues: ArrayList<String>? = values
    var mContext: Context = context

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var imageView: ImageView = v.findViewById(R.id.imgDoc) as ImageView
        var imgpdf: ImageView = v.findViewById(R.id.imgpdf) as ImageView
        var docName: TextView = v.findViewById(R.id.docName) as TextView
        var relativeLayout: RelativeLayout = v.findViewById(R.id.relative)
        var detailLAyout : LinearLayout = v.findViewById(R.id.detailLayout)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.item_grid_img, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int {
        return mValues?.size!!
    }

    var picassoInstance: Picasso? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mValues!![position].isNotEmpty()) {
            holder.detailLAyout.visibility = VISIBLE

            val extension: String = mValues!![position].substring(mValues!![position].lastIndexOf("."))

            if(extension.equals(".pdf")){
//                holder.imgpdf.visibility = VISIBLE
//                holder.imageView.visibility = GONE

                picassoInstance = Picasso.Builder(context.applicationContext)
                    .addRequestHandler(PdfRequestHandler())
                    .build()

//                picassoInstance!!.load(PdfRequestHandler.SCHEME_PDF+":"+mValues!![position])

                picassoInstance!!.load(PdfRequestHandler.SCHEME_PDF+":"+mValues!![position])
                    .fit()
                    .into(holder.imageView)
            }


            holder.docName.text = File(Uri.parse(mValues!![position])?.path).name
            holder.relativeLayout.setOnLongClickListener {
                mListener.onLongPress(position)
            }
            holder.relativeLayout.setOnClickListener {
                mListener.onPress(position)
            }
        }else{
            holder.detailLAyout.visibility = GONE
            holder.imageView.setOnClickListener {
                mListener.onPress(-1)
            }
        }

    }



}

