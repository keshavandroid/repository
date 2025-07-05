package com.reloop.reloop.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderDonations
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.donations.DonationCategories
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Utils

class AdapterDonations(
    var dataList: ArrayList<DonationCategories>,
    var itemClick: RecyclerViewItemClick,
    var activity: Activity
) :
    RecyclerView.Adapter<ViewHolderDonations>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderDonations {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.new_row_donations, viewGroup, false)
        return ViewHolderDonations(view, itemClick)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolderDonations, position: Int) {
        holder.heading?.text = dataList[position].name
        var placeholder = R.drawable.icon_placeholder_generic
        holder.infoButton.setOnClickListener {

            AlertDialogs.informationDialog(activity, dataList[position].avatar, dataList[position].name, dataList[position].description, placeholder)
        }
        if (position == 0) {
            placeholder = R.drawable.treeplanting
        } else if (position == 1) {
            placeholder = R.drawable.charity
        }
        Utils.glideImageLoaderServer(holder.imageIcon, dataList[position].avatar, placeholder)
    }
}