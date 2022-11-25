package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.interfaces.RecyclerViewItemClick

class ViewHolderMonthlySubscription(itemView: View, itemClick: RecyclerViewItemClick) :
    RecyclerView.ViewHolder(itemView) {
    var heading: TextView? = itemView.findViewById(R.id.tv_heading_category)
    var buyNow: Button? = itemView.findViewById(R.id.buy_now)
    var imageIcon: ImageView? = itemView.findViewById(R.id.imageView_category_icon)
    var description: TextView? = itemView.findViewById(R.id.tv_description)
    var infoButton:ImageButton=itemView.findViewById(R.id.info_button)

    init {
        buyNow?.setOnClickListener {
            itemClick.itemPosition(adapterPosition)

        }
    }
}