package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.interfaces.RecyclerViewItemClick

class ViewHolderOrderHistory(itemView: View, itemClick: RecyclerViewItemClick) :
    RecyclerView.ViewHolder(itemView) {
    var orderTitle: TextView? = itemView.findViewById(R.id.order_name)
    var orderDescription: TextView? = itemView.findViewById(R.id.order_description)
    var orderDate: TextView? = itemView.findViewById(R.id.order_date)
    var orderStatus: TextView? = itemView.findViewById(R.id.order_status)
    var orderPrice: TextView? = itemView.findViewById(R.id.order_price)
    var viewReceipt: Button? = itemView.findViewById(R.id.view_receipt)
    var referenceId: TextView? = itemView.findViewById(R.id.referenceId)
    var name: TextView? = itemView.findViewById(R.id.name)
    var support: TextView? = itemView.findViewById(R.id.support)
    var address: TextView? = itemView.findViewById(R.id.address)
    var details: TextView? = itemView.findViewById(R.id.details)


    init {
        itemView.setOnClickListener {
            itemClick.itemPosition(adapterPosition)

        }
    }
}