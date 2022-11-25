package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R

class ViewHolderTrackOrder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var heading: TextView? = itemView.findViewById(R.id.order_status_track)
    var imageIcon: ImageView? = itemView.findViewById(R.id.track_check_img)
    var view: View? = itemView.findViewById(R.id.view)



}