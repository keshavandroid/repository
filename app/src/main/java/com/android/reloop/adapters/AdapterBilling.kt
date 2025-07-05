package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderOrderHistory
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.billing.UserSubscriptionsList
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils

class AdapterBilling(
    var subscriptionsList: ArrayList<UserSubscriptionsList>?,
    var itemClick: RecyclerViewItemClick,
    var userOrdersList: ArrayList<UserOrders>?,
    var combineList: ArrayList<Any>,

    ) : RecyclerView.Adapter<ViewHolderOrderHistory>() {
    var size: Int = 0

    init {
        //OLD
        //size = userOrdersList!!.size + subscriptionsList!!.size

        //NEW
        size = combineList.size


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

        //OLD
        /*if (position < userOrdersList!!.size) {
            setOrderList(holder)
        } else {
            setSubscriptionList(holder)
        }*/

        //NEW
        if(combineList.get(position) is UserOrders){ //Orders
            val userOrdersData: UserOrders = combineList.get(position) as UserOrders
            setOrderListCombine(holder,userOrdersData)
            Log.d("DATATYPE","Orders...")
        }else{ //Collections
            val userSubscriptionsList: UserSubscriptionsList = combineList.get(position) as UserSubscriptionsList
            setSubscriptionListCombine(holder,userSubscriptionsList)
            Log.d("DATATYPE","Collections...")
        }


    }

    @SuppressLint("SetTextI18n")
    private fun setOrderList(holder: ViewHolderOrderHistory) {

        //Push this changes in live (not added in 11.9.3)
        if(userOrdersList?.get(holder.adapterPosition)?.status == OrderHistoryEnum.ORDER_REFUNDED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "REFUNDED"
        }else{
            holder.orderStatus?.visibility = View.GONE
            holder.orderStatus?.text = "COMPLETED"
        }

        holder.name?.visibility = View.GONE
        holder.referenceId?.text = userOrdersList?.get(holder.adapterPosition)?.order_number
        holder.orderDate?.text = Utils.getFormattedDisplayDate(
            userOrdersList?.get(holder.adapterPosition)?.created_at
        )
        if (!userOrdersList?.get(holder.adapterPosition)?.order_items.isNullOrEmpty()) {
            holder.orderTitle?.text =
                userOrdersList?.get(holder.adapterPosition)?.order_items?.get(0)?.product?.name
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

        //Push this changes in live (not added in 11.9.3)
        if(subscriptionsList?.get(position)?.status == OrderHistoryEnum.SUB_REFUNDED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "REFUNDED"
        }else{
            holder.orderStatus?.visibility = View.GONE
            holder.orderStatus?.text = "COMPLETED"
        }

        holder.referenceId?.text = subscriptionsList?.get(position)?.subscription_number
        holder.name?.text = subscriptionsList?.get(position)?.name
        holder.orderDate?.text = Utils.getFormattedDisplayDate(subscriptionsList?.get(position)?.created_at)

        if (!subscriptionsList?.get(position)?.name.isNullOrEmpty()) {
            holder.orderDescription?.text = "Subscription with " + subscriptionsList?.get(position)?.trips + " Trips"
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

    @SuppressLint("SetTextI18n")
    private fun setOrderListCombine(holder: ViewHolderOrderHistory,userOrdersData: UserOrders) {

        //Push this changes in live (not added in 11.9.3)



        if(userOrdersData.status == OrderHistoryEnum.NOT_ASSIGNED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "CONFIRMED"
        }else if(userOrdersData.status == OrderHistoryEnum.ASSIGNED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "ASSIGNED"
        }else if(userOrdersData.status == OrderHistoryEnum.TRIP_INITIATED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "DISPATCHED"
        }else if(userOrdersData.status == OrderHistoryEnum.COMPLETED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "COMPLETED"
        }else if(userOrdersData.status == OrderHistoryEnum.CANCELLED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "CANCELLED"
        }else if(userOrdersData.status == OrderHistoryEnum.ORDER_REFUNDED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "REFUNDED"
        }else if(userOrdersData.status == OrderHistoryEnum.REFUND_REQUEST){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "REFUND REQUEST"
        }else if(userOrdersData.status == OrderHistoryEnum.ORDER_VERIFIED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "VERIFIED"
        }


        holder.name?.visibility = View.GONE
        holder.referenceId?.text = userOrdersData.order_number

        //ORIGINAL
//        holder.orderDate?.text = Utils.getFormattedDisplayDate(userOrdersData.created_at)

        //NEW
        holder.orderDate?.text = userOrdersData.created_at

        if (!userOrdersData.order_items.isNullOrEmpty()) {
            holder.orderTitle?.text =
                userOrdersData.order_items?.get(0)
                    ?.product?.name
        } else {
            holder.orderTitle?.text = ""
        }
        holder.support?.visibility = View.GONE
        if (userOrdersData.total == null) {
            holder.orderPrice?.text = ""
        } else {
            holder.orderPrice?.text =
                "${Constants.currencySign} ${Utils.commaConversion(userOrdersData.total)}"
        }
        holder.orderDescription?.text = "Products"
    }

    @SuppressLint("SetTextI18n")
    private fun setSubscriptionListCombine(holder: ViewHolderOrderHistory,userSubscriptionsList: UserSubscriptionsList) {



        val position = holder.adapterPosition - combineList.size

        //Push this changes in live (not added in 11.9.3)
        if(userSubscriptionsList.status == OrderHistoryEnum.ACTIVE){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "ACTIVE"
        }else if(userSubscriptionsList.status == OrderHistoryEnum.PENDING){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "PENDING"
        }else if(userSubscriptionsList.status == OrderHistoryEnum.COMPLETED_SUB){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "COMPLETED"
        }else if(userSubscriptionsList.status == OrderHistoryEnum.EXPIRED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "EXPIRED"
        }else if(userSubscriptionsList.status == OrderHistoryEnum.SUB_CANCELLED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "CANCELLED"
        }else if(userSubscriptionsList.status == OrderHistoryEnum.SUB_REFUNDED){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "REFUNDED"
        }else if(userSubscriptionsList.status == OrderHistoryEnum.SUB_REFUND_REQUEST){
            holder.orderStatus?.visibility = View.VISIBLE
            holder.orderStatus?.text = "RENEWED"
        }





        holder.referenceId?.text = userSubscriptionsList.subscription_number
        holder.name?.text = userSubscriptionsList.name

        //ORIGINAL
//        holder.orderDate?.text = Utils.getFormattedDisplayDate(userSubscriptionsList.created_at)

        //NEW
        holder.orderDate?.text = userSubscriptionsList.created_at

        if (!userSubscriptionsList.name.isNullOrEmpty()) {
            holder.orderDescription?.text =
                "Subscription with " + userSubscriptionsList.trips + " Trips"
        } else {
            if (userSubscriptionsList.total?.toInt() == 5) {
                holder.orderDescription?.text = "Next Day Charge"
            }
            if (userSubscriptionsList.total?.toInt() == 10) {
                holder.orderDescription?.text = "Same Day Charge"
            }
        }
        holder.support?.visibility = View.GONE

        holder.orderPrice?.text =
            "${Constants.currencySign} ${Utils.commaConversion(userSubscriptionsList.total)}"
        when (userSubscriptionsList.subscription_type) {
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