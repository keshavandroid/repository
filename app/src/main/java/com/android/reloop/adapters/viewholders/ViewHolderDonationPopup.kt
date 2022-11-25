package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.RecyclerViewItemClick

class ViewHolderDonationPopup (itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    var heading: TextView? = itemView.findViewById(R.id.tv_heading_category)
    var imageIcon: ImageView? = itemView.findViewById(R.id.img_donation)
    var redeemButton: Button? = itemView.findViewById(R.id.redeemButton)
}