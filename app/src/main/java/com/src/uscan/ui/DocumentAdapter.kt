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
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.src.uscan.R
import com.src.uscan.utils.DateUtils
import com.src.uscan.utils.LongPressListener
import java.io.File


class DocumentAdapter(
    val context: Context,
    values: ArrayList<String>?,
    val mListener: LongPressListener,
    val orientation: Int
) :
    RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
    var mValues: ArrayList<String>? = values
    var mContext: Context = context

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var imageView: ImageView = v.findViewById(R.id.imgDoc) as ImageView
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


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (mValues!![position].isNotEmpty()) {
            holder.detailLAyout.visibility = VISIBLE
            Picasso.get().load(Uri.parse(mValues!![position].split(",")[0]))
                .placeholder(ColorDrawable(R.drawable.ic_baseline_image_plcholder))
                .into(holder.imageView)
            holder.docName.text = File(Uri.parse(mValues!![position].split(",")[0])?.path).name
            holder.subDocName.text = DateUtils.convertTime(mValues!![position].split(",")[1].toLong())

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

