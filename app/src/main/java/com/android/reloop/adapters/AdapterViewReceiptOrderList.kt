package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderOrderHistory
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequestProduct
import com.reloop.reloop.network.serializer.orderhistory.OrderItems
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils

class AdapterViewReceiptOrderList(
    var dataList: ArrayList<OrderItems>?,
    var itemClick: RecyclerViewItemClick,
    var requestCollection: ArrayList<CollectionRequestProduct>?
) :
    RecyclerView.Adapter<ViewHolderOrderHistory>() {
    var size: Int = 0

    init {
        size = if (dataList != null) {
            dataList!!.size
        } else {
            requestCollection!!.size
        }
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHistory {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_order_list_view_receipt, viewGroup, false)
        return ViewHolderOrderHistory(view, itemClick)
    }

    override fun getItemCount() = size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderOrderHistory, position: Int) {
        if (dataList != null) {
            holder.orderTitle?.text =
                "" + dataList?.get(position)?.quantity + " x " + dataList?.get(position)?.product?.name
            holder.orderPrice?.text =
                "${Constants.currencySign} ${Utils.commaConversion(dataList?.get(position)?.price)}"
        } else {
            holder.orderTitle?.text = requestCollection?.get(position)?.category_name
            if (!requestCollection?.get(position)?.weight.isNullOrEmpty()) {
                var unit = ""
                unit = when (requestCollection?.get(position)?.materialCategory?.unit) {
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
                holder.orderPrice?.text = "${requestCollection?.get(position)?.weight} $unit"
            } else {
                holder.orderPrice?.visibility = View.GONE
            }
        }

    }
}