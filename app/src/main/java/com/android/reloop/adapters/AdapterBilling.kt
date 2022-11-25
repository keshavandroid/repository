package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderOrderHistory
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.billing.UserSubscriptionsList
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils

class AdapterBilling(
    var subscriptionsList: ArrayList<UserSubscriptionsList>?,
    var itemClick: RecyclerViewItemClick,
    var userOrdersList: ArrayList<UserOrders>?
) : RecyclerView.Adapter<ViewHolderOrderHistory>() {
    var size: Int = 0

    init {
        size = userOrdersList!!.size + subscriptionsList!!.size
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
        holder.orderStatus?.visibility = View.GONE
        if (position < userOrdersList!!.size) {
            setOrderList(holder)
        } else {
            setSubscriptionList(holder)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setOrderList(holder: ViewHolderOrderHistory) {
        holder.name?.visibility = View.GONE
        holder.referenceId?.text = userOrdersList?.get(holder.adapterPosition)?.order_number
        holder.orderDate?.text = Utils.getFormattedDisplayDate(
            userOrdersList?.get(holder.adapterPosition)?.created_at
        )
        if (!userOrdersList?.get(holder.adapterPosition)?.order_items.isNullOrEmpty()) {
            holder.orderTitle?.text =
                userOrdersList?.get(holder.adapterPosition)?.order_items?.get(0)
                    ?.product?.name
        } else {
            holder.orderTitle?.text = ""
        }
        holder.support?.visibility = View.GONE
        if (userOrdersList?.get(holder.adapterPosition)?.total == null) {
            holder.orderPrice?.text = ""
        } else {
            holder.orderPrice?.text =
                "${Constants.currencySign} ${Utils.commaConversion(userOrdersList?.get(holder.adapterPosition)?.total)}"
        }
        holder.orderDescription?.text = "Products"
    }

    @SuppressLint("SetTextI18n")
    private fun setSubscriptionList(holder: ViewHolderOrderHistory) {
        val position = holder.adapterPosition - userOrdersList!!.size
        holder.referenceId?.text =
            subscriptionsList?.get(position)?.subscription_number
        holder.name?.text =
            subscriptionsList?.get(position)?.name
        holder.orderDate?.text =
            Utils.getFormattedDisplayDate(subscriptionsList?.get(position)?.created_at)

        if (!subscriptionsList?.get(position)?.name.isNullOrEmpty()) {
            holder.orderDescription?.text =
                "Subscription with " + subscriptionsList?.get(position)?.trips + " Trips"
        } else {
            if (subscriptionsList?.get(position)?.total?.toInt() == 5) {
                holder.orderDescription?.text = "Next Day Charge"
            }
            if (subscriptionsList?.get(position)?.total?.toInt() == 10) {
                holder.orderDescription?.text = "Same Day Charge"
            }
        }
        holder.support?.visibility = View.GONE

        holder.orderPrice?.text =
            "${Constants.currencySign} ${Utils.commaConversion(subscriptionsList?.get(position)?.total)}"
        when (subscriptionsList?.get(position)?.subscription_type) {
            0 -> {
                holder.orderTitle?.text = "Collection Request"
            }
            1 -> {
                holder.orderTitle?.text = "One Time Service"
            }
            2 -> {
                holder.orderTitle?.text = "One Time Service"
            }
            3 -> {
                holder.orderTitle?.text = "One Time Service"
            }
            5 -> {
                holder.orderTitle?.text = "Renewable Subscription"
            }
        }
    }
}