package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.interfaces.SupportClick
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.orderhistory.CollectionBinsList


class AdapterCollectionBins(
    var itemClick: RecyclerViewItemClick,
    var combineList: ArrayList<CollectionBinsList>?,
    var category : String,
    val supportClick: SupportClick
) :
    RecyclerView.Adapter<AdapterCollectionBins.ViewHolderCollectionBins>() {

    var size: Int = 0

    init {
        //size = dataList!!.size + userCollectionRequests!!.size//old
        size = combineList!!.size//New
    }


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderCollectionBins {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_row_collection_bins, viewGroup, false)
        return ViewHolderCollectionBins(view, itemClick)
    }

    override fun getItemCount() = size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderCollectionBins, position: Int) {

        val data = combineList!!.get(position)

        /*if(!data.getRequestImages().isNullOrEmpty() && data.getRequestImages()?.size!! > 0)
        {
            holder.support?.visibility = View.VISIBLE
        }
        else{
            holder.support?.visibility = View.GONE
        }*/
        holder.support?.setOnClickListener {
            itemClick.itemPosition(position)
        }

        holder.cat_name?.setText(category)
        holder.order_location?.setText(data.getLocationBin()?.addressLocation?.location)
        holder.orderStatus?.setText(data.getStatus())
        if(data.getStatus().equals("Collected"))
        {
            holder.orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_history_completed)
        }
        else{
            holder.orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_history_cancel)
        }

        if (!data.getWeight().toString().isNullOrEmpty()) {

            var unit = ""
            unit = when (data.getLocationBin()?.materialCategory?.unit) {
                1 -> {
                    "Kg"
                }
                2 -> {
                    "Liter"
                }
                3 -> {
                    "Pieces"
                }
                else -> {
                    ""
                }
            }
            if(data.getWeight() == null)
            {
                holder.catWeight?.text = "Weight : " + "${"0"} $unit"
            }
            else{
                holder.catWeight?.text = "Weight : " + "${data.getWeight().toString()} $unit"
            }

        } else {
            holder.catWeight?.visibility = View.GONE
        }
    }

    class ViewHolderCollectionBins(itemView: View, itemClick: RecyclerViewItemClick) :
        RecyclerView.ViewHolder(itemView) {
        var cat_name: TextView? = itemView.findViewById(R.id.cat_name)
        var order_location: TextView? = itemView.findViewById(R.id.order_location)
        var orderStatus: TextView? = itemView.findViewById(R.id.order_status)
        var support: TextView? = itemView.findViewById(R.id.details)
        var catWeight : TextView?= itemView.findViewById(R.id.weight)

    }
}