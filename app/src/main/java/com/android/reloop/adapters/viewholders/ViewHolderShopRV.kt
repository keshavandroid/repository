package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R


class ViewHolderShopRV(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var heading: TextView? = itemView.findViewById(R.id.tv_heading_category)
    var imageIcon: ImageView? = itemView.findViewById(R.id.imageView_category_icon)
    var info: ImageButton? = itemView.findViewById(R.id.info_button)
    var layout: LinearLayout? = itemView.findViewById(R.id.layout)
//    var products_heading:TextView?=itemView.findViewById(R.id.products_heading)
    var cardView:CardView?=itemView.findViewById(R.id.cardView)


}