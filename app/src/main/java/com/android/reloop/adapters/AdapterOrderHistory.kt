package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.interfaces.SupportClick
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderRequestHistory
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
    var combineList: ArrayList<Any>,
    val supportClick: SupportClick
) :
    RecyclerView.Adapter<ViewHolderRequestHistory>() {

    var size: Int = 0

    init {
        //size = dataList!!.size + userCollectionRequests!!.size//old
        size = combineList.size//New
    }


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderRequestHistory {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_request_history, viewGroup, false)
        return ViewHolderRequestHistory(view, itemClick)
    }

    override fun getItemCount() = size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderRequestHistory, position: Int) {
        holder.referenceId?.visibility = View.GONE
        holder.name?.visibility = View.GONE

        //old
        /*if (position < dataList!!.size) {
            setUserOrder(holder)
        } else {
            setCollectionRequest(holder)
        }*/


        //new added
        if(combineList.get(position) is UserOrders){ //Orders

            val userOrdersData: UserOrders = combineList.get(position) as UserOrders

            setUserOrderCombine(holder,userOrdersData)


        }else{ //Collections

            val collectionRequestsData: CollectionRequests = combineList.get(position) as CollectionRequests

            setCollectionRequestCombine(holder,collectionRequestsData)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setCollectionRequest(holder: ViewHolderRequestHistory) {
        holder.orderDate?.text =
            Utils.getFormattedDisplayDate(userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.created_at)

        if(userCollectionRequests?.get(holder.adapterPosition-dataList!!.size)?.collection_date != null){
            holder.llScheduleDate!!.visibility = View.VISIBLE
            holder.schedule_date!!.setText(Utils.getFormattedDisplayDateCollection(userCollectionRequests?.get(holder.adapterPosition-dataList!!.size)?.collection_date))
        }else{
            holder.llScheduleDate!!.visibility = View.GONE
        }

        holder.orderTitle?.text = userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.request_number
        holder.orderDescription?.text = "Collection Request"
        holder.orderPrice?.text = ""
        showStatus(
            userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.status,
            holder.orderStatus,"collection"
        )
        holder.support?.setOnClickListener {
            supportClick.openFragment(holder.adapterPosition, "-1")
        }
        holder.address?.visibility = View.GONE
//        holder.address?.text = userCollectionRequests?.get(holder.adapterPosition - dataList!!.size)?.location
    }

    @SuppressLint("SetTextI18n")
    private fun setCollectionRequestCombine(
        holder: ViewHolderRequestHistory,
        collectionRequestsData: CollectionRequests
    ) {
        holder.orderDate?.text =
            Utils.getFormattedDisplayDate(collectionRequestsData.created_at)

        if(collectionRequestsData.collection_date != null){
            holder.llScheduleDate!!.visibility = View.VISIBLE
            holder.schedule_date!!.setText(Utils.getFormattedDisplayDateCollection(collectionRequestsData.collection_date))
        }else{
            holder.llScheduleDate!!.visibility = View.GONE
        }


        holder.orderTitle?.text =
            collectionRequestsData.request_number

//        holder.orderDescription?.text = "Collection Request" //OLD BEFOR DROP OFF FLOW

        //NEW AFTER DROP OFF FLOW
        if(collectionRequestsData.type.equals("request")){
            holder.orderDescription?.text = "Collection Request"

            holder.llScheduleDate!!.visibility = View.VISIBLE

        }else{
            holder.orderDescription?.text = "Drop-Off Request"
            holder.llScheduleDate!!.visibility = View.GONE
        }

        holder.orderPrice?.text = ""
        showStatus(collectionRequestsData.status, holder.orderStatus,"collection")

        holder.support?.setOnClickListener {
            supportClick.openFragment(holder.adapterPosition, "-1")
        }
        holder.address?.visibility = View.GONE
//        holder.address?.text = collectionRequestsData?.get(holder.adapterPosition - dataList!!.size)?.location
    }

    @SuppressLint("SetTextI18n")
    private fun setUserOrder(
        holder: ViewHolderRequestHistory
    ) {
        holder.orderDate?.text = Utils.getFormattedDisplayDate(dataList?.get(holder.adapterPosition)?.created_at)

        if(dataList?.get(holder.adapterPosition)?.delivery_date != null){
            holder.llScheduleDate!!.visibility = View.VISIBLE
            holder.schedule_date!!.setText(Utils.getFormattedDisplayDateCollection(dataList?.get(holder.adapterPosition)?.delivery_date))
        }else{
            holder.llScheduleDate!!.visibility = View.GONE
        }

        holder.orderTitle?.text = dataList?.get(holder.adapterPosition)?.order_number
        holder.orderDescription?.text = "Products"
        holder.orderPrice?.text =
            "${Utils.commaConversion(dataList?.get(holder.adapterPosition)?.total)} ${Constants.currencySign}"
        showStatus(dataList?.get(holder.adapterPosition)?.status, holder.orderStatus,"order")
        holder.support?.setOnClickListener {
            if (dataList?.get(holder.adapterPosition)?.status != null)
                supportClick.openFragment(holder.adapterPosition, dataList?.get(holder.adapterPosition)?.status!!)
        }
        holder.address?.visibility = View.GONE
//        holder.address?.text = dataList?.get(holder.adapterPosition)?.location
    }

    @SuppressLint("SetTextI18n")
    private fun setUserOrderCombine(
        holder: ViewHolderRequestHistory,
        userOrdersData: UserOrders
    ) {
        holder.orderDate?.text =
            Utils.getFormattedDisplayDate(userOrdersData.created_at)

        if(userOrdersData.delivery_date != null){
            holder.llScheduleDate!!.visibility = View.VISIBLE
            holder.schedule_date!!.setText(Utils.getFormattedDisplayDateCollection(userOrdersData.delivery_date))
        }else{
            holder.llScheduleDate!!.visibility = View.GONE
        }

        holder.orderTitle?.text = userOrdersData.order_number
        holder.orderDescription?.text = "Products"
        holder.orderPrice?.text =
            "${Utils.commaConversion(userOrdersData.total)} ${Constants.currencySign}"
        showStatus(userOrdersData.status, holder.orderStatus,"order")
        holder.support?.setOnClickListener {
            if (userOrdersData.status != null)
                supportClick.openFragment(holder.adapterPosition, userOrdersData.status!!)
        }
        holder.address?.visibility = View.GONE
//        holder.address?.text = userOrdersData?.get(holder.adapterPosition)?.location
    }

    @SuppressLint("SetTextI18n")
    private fun showStatus(status: String?, orderStatus: TextView?, s: String) {
        when (status) {
            OrderHistoryEnum.ASSIGNED -> {
                orderStatus?.text = MainApplication.applicationContext().getString(R.string.assigned)
                orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_in_progress)
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
                orderStatus?.text = MainApplication.applicationContext().getString(R.string.pending)
                orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_in_progress)
            }
            OrderHistoryEnum.CANCELLED -> {
                orderStatus?.text = MainApplication.applicationContext().getString(R.string.cancelled)
                orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_cancel)
            }
            OrderHistoryEnum.REFUND_REQUEST -> {
                orderStatus?.text = MainApplication.applicationContext().getString(R.string.request_refund)
                orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_cancel)
            }
            OrderHistoryEnum.ORDER_REFUNDED -> {
                orderStatus?.text = MainApplication.applicationContext().getString(R.string.order_refund)
                orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_status_cancel)
            }
            OrderHistoryEnum.ORDER_VERIFIED -> {
                if(s.equals("collection")) {
                    orderStatus?.text = MainApplication.applicationContext().getString(R.string.order_verified)
                    orderStatus?.background = MainApplication.applicationContext().getDrawable(R.drawable.shape_order_history_completed)
                }
            }
            "-1" -> {
                orderStatus?.visibility = View.GONE
            }
        }
    }
}