package com.reloop.reloop.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderTrackOrder
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.model.ModelTrackOrder

class AdapterOrderTackTracking(
    var dataList: ArrayList<ModelTrackOrder>
) :
    RecyclerView.Adapter<ViewHolderTrackOrder>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderTrackOrder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_track_order_list, viewGroup, false)
        return ViewHolderTrackOrder(view)
    }

    override fun getItemCount() = dataList.size
    override fun onBindViewHolder(holder: ViewHolderTrackOrder, position: Int) {
        holder.heading?.text = dataList[position].heading
        if (dataList[position].check!!) {
            holder.imageIcon?.setImageDrawable(MainApplication.applicationContext().getDrawable(R.drawable.icon_track_indicator_en))
        } else {
            holder.imageIcon?.setImageDrawable(MainApplication.applicationContext().getDrawable(R.drawable.icon_track_indicator_un))
        }
        if (position == dataList.size - 1) {
            holder.view?.visibility = View.GONE
        }

    }
}