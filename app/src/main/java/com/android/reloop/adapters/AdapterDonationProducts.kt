package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderDonationPopup
import com.reloop.reloop.adapters.viewholders.ViewHolderDonations
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.donations.DonationProducts

class AdapterDonationProducts(
    var dataList: ArrayList<DonationProducts?>,
    var itemClick: AlertDialogCallback
) :
    RecyclerView.Adapter<ViewHolderDonationPopup>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderDonationPopup {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_donation_products, viewGroup, false)
        return ViewHolderDonationPopup(view)
    }

    override fun getItemCount() = dataList.size
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderDonationPopup, position: Int) {
        holder.heading?.text = "${dataList[position]?.name}: ${com.reloop.reloop.utils.Utils.commaConversion(dataList[position]?.points)} Points"
        holder.redeemButton?.setOnClickListener {
            itemClick.callDialog(dataList[position]?.id!!)
        }
    }
}