package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.interfaces.SupportClick
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderOrderHistory
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils


class AdapterOrderHistory(
    var dataList: ArrayList<UserOrders>?,
    var itemClick: RecyclerViewItemClick,
    var userCollectionRequests: ArrayList<CollectionRequests>?,
    val supportClick: SupportClick
) :
    RecyclerView.Adapter<ViewHolderOrderHistory>() {

    var size: Int = 0

    init {
        size = dataList!!.size + userCollectionRequests!!.size
    }


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHistory {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_order_history, viewGroup, false)
        return ViewHolderOrderHistory(view, itemClick)
    }

    override fun getItemCount() = size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderOrderHistory, position: Int) {
        holder.referenceId?.visibility = View.GONE
        holder.name?.visibility = View.GONE

        if (position < dataList!!.size) {
            setUserOrder(holder)
        } else {
            setCollectionRequest(holder)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCollectionRequest(holder: ViewHolderOrderHistory) {
//        val separated= userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.created_at?.split(" ")
//        holder.orderDate?.text = userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.created_at
        holder.orderDate?.text =
            Utils.getFormattedDisplayDate(userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.created_at)
        holder.orderTitle?.text =
            userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.request_number
        holder.orderDescription?.text =
            "Collection Request"
        holder.orderPrice?.text = ""
        showStatus(
            userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.status,
            holder.orderStatus,"collection"
        )
        holder.support?.setOnClickListener {
            supportClick.openFragment(holder.adapterPosition, -1)
        }
        holder.address?.visibility = View.GONE
//        holder.address?.text = userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.location
    }

    @SuppressLint("SetTextI18n")
    private fun setUserOrder(
        holder: ViewHolderOrderHistory
    ) {
        holder.orderDate?.text =
            Utils.getFormattedDisplayDate(dataList?.get(holder.adapterPosition)?.created_at)
        holder.orderTitle?.text = dataList?.get(holder.adapterPosition)?.order_number
        holder.orderDescription?.text = "Products"
        holder.orderPrice?.text =
            "${Utils.commaConversion(dataList?.get(holder.adapterPosition)?.total)} ${Constants.currencySign}"
        showStatus(dataList?.get(holder.adapterPosition)?.status, holder.orderStatus,"order")
        holder.support?.setOnClickListener {
            if (dataList?.get(holder.adapterPosition)?.status != null)
                supportClick.openFragment(
                    holder.adapterPosition,
                    dataList?.get(holder.adapterPosition)?.status!!
                )
        }
        holder.address?.visibility = View.GONE
//        holder.address?.text = dataList?.get(holder.adapterPosition)?.location
    }

    @SuppressLint("SetTextI18n")
    private fun showStatus(status: Int?, orderStatus: TextView?, s: String) {
        when (status) {
            OrderHistoryEnum.ASSIGNED -> {
                orderStatus?.text =
                    MainApplication.applicationContext().getString(R.string.assigned)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_in_progress)
            }
            OrderHistoryEnum.TRIP_INITIATED -> {
                orderStatus?.text = MainApplication.applicationContext().getString(R.string.trip_initiated)
                orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_in_progress)
            }
            OrderHistoryEnum.COMPLETED -> {
                if(s.equals("order"))
                {
                    orderStatus?.text = MainApplication.applicationContext().getString(R.string.delivered)
                    orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_history_completed)
                }
                else {
                    //collected
                    orderStatus?.text = MainApplication.applicationContext().getString(R.string.collected)
                    orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_in_progress)
                }
            }
            OrderHistoryEnum.NOT_ASSIGNED -> {
                //pending
                orderStatus?.text =
                    MainApplication.applicationContext().getString(R.string.pending)
                orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_in_progress)
            }
            OrderHistoryEnum.CANCELLED -> {
                orderStatus?.text =
                    MainApplication.applicationContext().getString(R.string.cancelled)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_cancel)
            }
            OrderHistoryEnum.REFUND_REQUEST -> {
                orderStatus?.text =
                    MainApplication.applicationContext().getString(R.string.request_refund)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_cancel)
            }
            OrderHistoryEnum.ORDER_REFUNDED -> {
                orderStatus?.text =
                    MainApplication.applicationContext().getString(R.string.order_refund)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_cancel)
            }
            OrderHistoryEnum.ORDER_VERIFIED -> {
                if(s.equals("collection")) {
                    orderStatus?.text =
                        MainApplication.applicationContext().getString(R.string.order_verified)
                    orderStatus?.background = MainApplication.applicationContext()
                        .getDrawable(R.drawable.shape_order_history_completed)
                }
            }
            -1 -> {
                orderStatus?.visibility = View.GONE
            }
        }
    }
}