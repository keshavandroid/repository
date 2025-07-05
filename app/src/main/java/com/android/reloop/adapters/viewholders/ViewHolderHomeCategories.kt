package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R

class ViewHolderHomeCategories(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var heading: TextView? = itemView.findViewById(R.id.tv_heading_category)
    var unit: TextView? = itemView.findViewById(R.id.tv_unit)
    var imageIcon: ImageView? = itemView.findViewById(R.id.imageView_category_icon)
    var points: TextView? = itemView.findViewById(R.id.points)
    var info: ImageButton? = itemView.findViewById(R.id.info_button)
    var tv_status: TextView?= itemView.findViewById(R.id.tv_status)


}