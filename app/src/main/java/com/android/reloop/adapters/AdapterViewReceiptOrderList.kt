package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.viewholders.ViewHolderOrderHistory
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.fragments.CollectionBinsFragment
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequestProduct
import com.reloop.reloop.network.serializer.orderhistory.OrderItems
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils

class AdapterViewReceiptOrderList(
    var dataList: ArrayList<OrderItems>?,
    var itemClick: RecyclerViewItemClick,
    var requestCollection: ArrayList<CollectionRequestProduct>?,
    var orderStatus: String?

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
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_order_list_view_receipt, viewGroup, false)
        return ViewHolderOrderHistory(view, itemClick)
    }

    override fun getItemCount() = size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderOrderHistory, position: Int) {

        Log.e("TAG","=====id=====" + requestCollection?.get(position)?.id)
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

            if(requestCollection?.get(position)?.collection_bins!!.isNotEmpty() && requestCollection?.get(position)?.collection_bins!!.size > 0)
            {
                holder.details?.visibility = View.VISIBLE
            }
            else{
                holder.details?.visibility = View.GONE
            }

            //NEW CHANGE AD FOR ONLY SHOWING Weight Kg when order status is verified/confirmed
            if(orderStatus != null){
                if(orderStatus == OrderHistoryEnum.ORDER_VERIFIED){
                    holder.orderPrice?.visibility = View.VISIBLE
                }else{
                    holder.orderPrice?.visibility = View.GONE
                }
            }

            holder.details?.setOnClickListener {
                Log.e("TAG","=====id click=====" + requestCollection?.get(position)?.id)
                itemClick.itemPosition(position)
                //itemClick.itemPosition(requestCollection?.get(position)?.id!!)
            }
        }
    }
}