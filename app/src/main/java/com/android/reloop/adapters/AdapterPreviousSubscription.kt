package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderPreviousSubscriptions
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.subscription.SubscriptionData
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class AdapterPreviousSubscription(
    var dataList: ArrayList<SubscriptionData>?, val recyclerViewItemClick: RecyclerViewItemClick
) :
    RecyclerView.Adapter<ViewHolderPreviousSubscriptions>() {

    var isMonthly: Boolean = false

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderPreviousSubscriptions {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_subscription_adapter, viewGroup, false)
        return ViewHolderPreviousSubscriptions(view, recyclerViewItemClick)
    }

    override fun getItemCount() = dataList!!.size

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolderPreviousSubscriptions, position_: Int) {
        val position = holder.adapterPosition

        if(dataList!!.get(position).yearly_renew != null){
            holder.txtYearlyRenew!!.visibility = View.VISIBLE
            holder.txtYearlyRenew?.text = "Next Payment : " + Utils.getFormattedServerDate(dataList?.get(position)?.yearly_renew)
        }else{
            holder.txtYearlyRenew!!.visibility = View.GONE
        }

        holder.cardView?.setCardBackgroundColor(MainApplication.applicationContext().getColor(R.color.color_background))

        holder.subscriptionAmount?.text = "${Constants.currencySign} ${Utils.commaConversion(dataList?.get(position)?.subscription?.price)}"
        if (dataList?.get(position)?.start_date == null && dataList?.get(position)?.end_date == null) {
            holder.subscriptionDate?.visibility = View.GONE
            holder.subscriptionDateActive?.text = Utils.getFormattedServerDate(dataList?.get(position)?.created_at)
//            holder.subscriptionDate?.text =
//                Utils.getFormattedServerDate(dataList?.get(position)?.created_at)
        } else {

            if (dataList?.get(position)?.start_date.isNullOrEmpty()) {
                holder.subscriptionDateActive?.text = Utils.getFormattedServerDate(dataList?.get(position)?.created_at)
            } else {
                holder.subscriptionDateActive?.text = Utils.getFormattedServerDate(dataList?.get(position)?.start_date)
            }
            if (dataList?.get(position)?.end_date.isNullOrEmpty()) {
                holder.subscriptionDate?.visibility = View.GONE
            } else {
                holder.subscriptionDate?.text = Utils.getFormattedServerDate(dataList?.get(position)?.end_date)
            }
            holder.subscriptionDate?.text = Utils.getFormattedServerDate(dataList?.get(position)?.end_date)
        }
        holder.subscription_id?.text = dataList?.get(position)?.subscription_number
        holder.subscriptionRemainingDays?.text = "Remaining Trips: ${dataList?.get(position)?.trips}"

        var expireDateCheck = false
        try {
            //New AD
            if(!dataList.isNullOrEmpty()){
                if(!dataList?.get(position)?.end_date.isNullOrEmpty()){
                    val dateFormat: DateFormat = SimpleDateFormat(Utils.dateFormat)
                    val endDateString = Utils.getFormattedServerDate(dataList?.get(position)?.end_date)
                    val currentDate = Calendar.getInstance()
                    val endDate = dateFormat.parse(endDateString!!)
                    if (endDate!!.before(currentDate.time)) {
                        expireDateCheck = true
                    }
                }
            }

            //Original
            /*val dateFormat: DateFormat = SimpleDateFormat(Utils.dateFormat)
            val endDateString = Utils.getFormattedServerDate(dataList?.get(position)?.end_date)
            val currentDate = Calendar.getInstance()
            val endDate = dateFormat.parse(endDateString!!)
            if (endDate!!.before(currentDate.time)) {
                expireDateCheck = true
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //ORIGINAL HERE
        /*if (dataList?.get(position)?.status == 7){
            holder.itemView?.visibility = View.GONE
            val params = holder.itemView.layoutParams
            params.height = 0
            params.width = 0
            holder.itemView.layoutParams = params
        }else {
            holder.itemView?.visibility = View.VISIBLE
        }*/

        if (dataList?.get(position)?.status == OrderHistoryEnum.ACTIVE) {
            holder.subscriptionStatus?.text = "Active"
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext().getDrawable(R.drawable.shape_order_history_completed)

        }

//NEW
        else if (dataList?.get(position)?.status == OrderHistoryEnum.EXPIRED){

            //NEW CHANGE UNSUBSCRIBE BUTTON ISSUE === CHANGE for "unscubscribe" button.....below line is originally added
//            dataList?.get(position)?.status = OrderHistoryEnum.EXPIRED


            holder.subscriptionStatus?.text = "Expired"
            holder.subscriptionStatus?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_empty_order_history)
            holder.subscriptionDateActive?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
        }

        else if (dataList?.get(position)?.status == OrderHistoryEnum.COMPLETED_SUB){

            //NEW CHANGE UNSUBSCRIBE BUTTON ISSUE === CHANGE for "unscubscribe" button.....below line is originally added
//            dataList?.get(position)?.status = OrderHistoryEnum.EXPIRED


            holder.subscriptionStatus?.text = "Used"
            holder.subscriptionStatus?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_empty_order_history)
            holder.subscriptionDateActive?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
        }

        //ORIGINAL
        /*else if (dataList?.get(position)?.status == OrderHistoryEnum.EXPIRED || dataList?.get(
                position
            )?.trips == 0 || (expireDateCheck && dataList?.get(position)?.status != OrderHistoryEnum.SUB_CANCELLED)
        ) {

            //NEW CHANGE UNSUBSCRIBE BUTTON ISSUE === CHANGE for "unscubscribe" button.....below line is originally added
//            dataList?.get(position)?.status = OrderHistoryEnum.EXPIRED


            var status = ""
            status = if (expireDateCheck) {
                "Ended"
            } else {
                "Used"
            }
            holder.subscriptionStatus?.text = status
            holder.subscriptionStatus?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_empty_order_history)
            holder.subscriptionDateActive?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
        }*/

        else if (dataList?.get(position)?.status == OrderHistoryEnum.SUB_CANCELLED) {
            holder.subscriptionStatus?.text = "Unsubscribed"
            holder.subscriptionStatus?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_empty_order_history)
            holder.subscriptionDateActive?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
        } else if (dataList?.get(position)?.status == OrderHistoryEnum.PENDING) {
            holder.subscriptionStatus?.text = "Valid"
            holder.subscriptionStatus?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.white)
            )
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_valid_subsscriptions)
            /*  holder.subscriptionDateActive?.setTextColor(
                  MainApplication.applicationContext().getColor(R.color.colorPrimary)
              )*/
        }else if (dataList?.get(position)?.status == OrderHistoryEnum.SUB_REFUNDED) {
            holder.subscriptionStatus?.text = "Refunded"
            holder.subscriptionStatus?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_empty_order_history)
            holder.subscriptionDateActive?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
        }
        else if (dataList?.get(position)?.status == OrderHistoryEnum.SUB_REFUND_REQUEST) {
            holder.subscriptionStatus?.text = "Renewed"
            holder.subscriptionStatus?.setTextColor(MainApplication.applicationContext().getColor(R.color.color_headline_text))
            holder.subscriptionStatus?.background =
                MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_empty_order_history)
            holder.subscriptionDateActive?.setTextColor(
                MainApplication.applicationContext().getColor(R.color.color_headline_text)
            )
        }

        //Log.d("UnSubscribeBtn","=========================================== : "+ position)

        //--------------Status Login For hiding/showing unsubscribe Button
        if (dataList?.get(position)?.free_subscription_expiry.isNullOrEmpty()) {

            //Log.d("UnSubscribeBtn","111")

            holder.unSubscribe?.visibility = View.VISIBLE
            holder.expiry_date?.visibility = View.INVISIBLE
        } else {

            //Log.d("UnSubscribeBtn","222")

            holder.unSubscribe?.visibility = View.INVISIBLE
            holder.expiry_date?.visibility = View.VISIBLE
            holder.expiry_date?.text = Utils.getFormattedServerDate(dataList?.get(position)?.free_subscription_expiry)
        }
        if (dataList?.get(position)?.status == OrderHistoryEnum.ACTIVE || dataList?.get(position)?.status == OrderHistoryEnum.PENDING) {
            if (dataList?.get(position)?.end_date.isNullOrEmpty()) {
                holder.unSubscribe?.visibility = View.GONE
            } else {
                if (dataList?.get(position)?.free_subscription_expiry.isNullOrEmpty()) {
                    holder.unSubscribe?.visibility = View.VISIBLE
                    holder.expiry_date?.visibility = View.GONE
                } else {
                    holder.unSubscribe?.visibility = View.INVISIBLE
                    holder.expiry_date?.visibility = View.VISIBLE
                }
            }
        } else if (dataList?.get(position)?.status == OrderHistoryEnum.SUB_CANCELLED || dataList?.get(position)?.status == OrderHistoryEnum.EXPIRED || dataList?.get(position)?.status == OrderHistoryEnum.COMPLETED_SUB) {
            holder.unSubscribe?.visibility = View.GONE
        }


        //NEW ADDED UNSUBSCRIBE BUTTON ISSUE === CHANGE for "unscubscribe" button
        if (dataList?.get(position)?.status == OrderHistoryEnum.COMPLETED_SUB){
            if (!dataList?.get(position)?.start_date.isNullOrEmpty()) {
                if (dataList?.get(position)?.free_subscription_expiry.isNullOrEmpty()) {
                    //Log.d("UnSubscribeBtn","444")

                    holder.unSubscribe?.visibility = View.VISIBLE
                } else {
                    //Log.d("UnSubscribeBtn","555")

                    holder.unSubscribe?.visibility = View.INVISIBLE
                }
            }
        }

        if (dataList?.get(0)?.status == OrderHistoryEnum.SUB_REFUNDED) {
            holder.unSubscribe?.visibility = View.GONE
        }


        //---------------------------------------
        holder.subscriptionTitle?.text = dataList?.get(position)?.subscription?.name
        Utils.glideImageLoaderServer(
            holder.subscriptionIcon,
            dataList?.get(position)?.subscription?.avatar, R.drawable.icon_placeholder_generic
        )
//        val freeSubscription: SubscriptionData? = dataList?.find { !it.free_subscription_expiry.isNullOrEmpty() }

//        dataList[position].subscriptionIcon?.let { holder.subscriptionIcon?.setImageResource(it) }

    }
}