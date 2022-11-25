package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderOrderHistory
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.donations.RewardsHistory
import com.reloop.reloop.utils.Utils

class AdapterRewardsHistory(
    var apiList:ArrayList<RewardsHistory?>?,
    var itemClick: RecyclerViewItemClick
) : RecyclerView.Adapter<ViewHolderOrderHistory>() {
    var size: Int = 0

    init {
        size = apiList!!.size
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHistory {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_rewards_history, viewGroup, false)
        return ViewHolderOrderHistory(view, itemClick)
    }

    override fun getItemCount() = size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderOrderHistory, position: Int) {
        holder.orderTitle?.text = apiList?.get(position)?.donationProduct?.name
        holder.orderDescription?.text = apiList?.get(position)?.donationProduct?.description
        holder.orderDate?.text = Utils.getFormattedDisplayDate(apiList?.get(position)?.created_at)
        holder.orderPrice?.text = ""+apiList?.get(position)?.redeemed_points+" Points"
    }


}