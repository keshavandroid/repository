package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.viewholders.ViewHolderMonthlySubscription
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.Utils

class AdapterSubscriptionDetail(
    var dataList: List<Category>?,
    var itemClick: RecyclerViewItemClick,
    var itemType: Int?,
    var serviceType: Int?,
    var icon: Int?,
    var activity: FragmentActivity?
) :
    RecyclerView.Adapter<ViewHolderMonthlySubscription>() {
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): ViewHolderMonthlySubscription {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_monthly_subscription, viewGroup, false)
        return ViewHolderMonthlySubscription(view, itemClick)
    }

    override fun getItemCount() = dataList!!.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderMonthlySubscription, position: Int) {
        holder.heading?.text = "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"
        holder.description?.text = dataList!![position].name

        Log.e("TAG","=====serviceType===" + serviceType)

        if (itemType == Constants.productType ) {

            val cartItem: Category? = HomeActivity.cartList?.find { it?.id == dataList?.get(position)?.id }
            if (cartItem != null) {
                holder.buyNow!!.setText(activity?.resources?.getString(R.string.added_to_cart_msg))
                holder.buyNow!!.setBackgroundResource(R.drawable.button_shape_rectangle_green)
            } else {
                holder.buyNow?.text = MainApplication.applicationContext().getString(R.string.add_to_cart)
            }
         //   holder.buyNow?.text = MainApplication.applicationContext().getString(R.string.add_to_cart)
//            icon=R.drawable.icon_placeholder_generic
        } else {

            if(serviceType == 1) {
                holder.buyNow?.text = MainApplication.applicationContext().getString(R.string.subscribe)
            }
            else{
                holder.buyNow?.text = MainApplication.applicationContext().getString(R.string.proceed)
            }
        }
        icon.let {
            Utils.glideImageLoaderServer(
                holder.imageIcon,
                dataList?.get(position)?.avatarToShow, it
            )
        }
        holder.infoButton.setOnClickListener {
            AlertDialogs.informationDialog(
                activity,
                dataList?.get(position)?.avatarToShow,
                dataList?.get(position)?.name,
                dataList?.get(position)?.description,
                icon,dataList?.get(position)?.avatars
            )
        }

        holder.itemView.setOnClickListener {
            holder.infoButton.performClick()
        }

        holder.buyNow?.setOnClickListener {

            if(serviceType != 1) {
                val cartItem: Category? =
                    HomeActivity.cartList?.find { it?.id == dataList?.get(position)?.id }
                if (cartItem != null) {
                    Notify.alerterRed(
                        activity,
                        activity?.resources?.getString(R.string.already_added_to_cart_msg)
                    )
                } else {
                    itemClick.itemPosition(position)
                    holder.buyNow!!.setText(activity?.resources?.getString(R.string.added_to_cart_msg))
                    holder.buyNow!!.setBackgroundResource(R.drawable.button_shape_rectangle_green)
                }
            }
            else{
                val cartItem: Category? = HomeActivity.cartList?.find { it?.id == dataList?.get(position)?.id }
                if (cartItem != null) {
                    Notify.alerterRed(activity, activity?.resources?.getString(R.string.already_added_to_cart_msg))
                } else {
                    itemClick.itemPosition(position)
                }
            }
        }
    }
}