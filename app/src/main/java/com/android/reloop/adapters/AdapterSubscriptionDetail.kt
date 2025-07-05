package com.reloop.reloop.adapters

import CouponVerification
import android.annotation.SuppressLint
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
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
    var activity: FragmentActivity?,
    var couponVerification: CouponVerification,
    var data: String
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
        //holder.heading?.text = "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"

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

            holder.heading?.text = "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"
         //   holder.buyNow?.text = MainApplication.applicationContext().getString(R.string.add_to_cart)
//            icon=R.drawable.icon_placeholder_generic
        } else {

            Log.e("TAG", "=====hh data Details =====" + data)

            if(data.equals("null"))
            {
                if (serviceType == 1) {
                    holder.buyNow?.text =
                        MainApplication.applicationContext().getString(R.string.subscribe)
                    holder.heading?.text =
                        "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"
                }
                else{
                    holder.buyNow?.text =
                        MainApplication.applicationContext().getString(R.string.proceed)
                    holder.heading?.text =
                        "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"
                }
            }else {
                if (serviceType == 1) {
                    holder.buyNow?.text =
                        MainApplication.applicationContext().getString(R.string.subscribe)

                    if (couponVerification.couponDetails != null) {
                        val price = getPrice(
                            position,
                            couponVerification,
                            dataList!![position].price,
                            holder.heading
                        )
                        Log.e("TAG", "=====discounted price =====" + price)

                        val totalPrice =
                            Utils.commaConversion(dataList!![position].price) + " " + Constants.currencySign

                        /*val spannableOriginalPrice = SpannableString(totalPrice)
                    spannableOriginalPrice.setSpan(StrikethroughSpan(), 0, totalPrice.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val finalString = SpannableStringBuilder()
                        .append(spannableOriginalPrice)
                        .append("\n")
                        .append(price +" ")
                        .append("${Constants.currencySign}").toString()

                    holder.heading?.text = Html.fromHtml(finalString)*/

                        val spannableOriginalPrice1 = SpannableString(totalPrice)
                        spannableOriginalPrice1.setSpan(
                            StrikethroughSpan(),
                            0,
                            totalPrice.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val concatted = TextUtils.concat(
                            spannableOriginalPrice1,
                            "\n" + price + " " + "${Constants.currencySign}"
                        )
                        holder.heading?.setText(concatted)

                        //holder.heading?.text = finalString
                        //holder.heading?.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG)
                    } else {
                        holder.heading?.text = "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"
                    }
                } else if (serviceType == 4) {
                    holder.buyNow?.text = MainApplication.applicationContext().getString(R.string.proceed)
                    if (couponVerification.couponDetails != null) {
                        val price = getPrice(
                            position,
                            couponVerification,
                            dataList!![position].price,
                            holder.heading
                        )
                        Log.e("TAG", "=====discounted price =====" + price)

                        val totalPrice = Utils.commaConversion(dataList!![position].price) + " " + Constants.currencySign

                        val spannableOriginalPrice1 = SpannableString(totalPrice)
                        spannableOriginalPrice1.setSpan(
                            StrikethroughSpan(),
                            0,
                            totalPrice.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        val concatted = TextUtils.concat(
                            spannableOriginalPrice1,
                            "\n" + price + " " + "${Constants.currencySign}"
                        )
                        holder.heading?.setText(concatted)

                    } else {
                        holder.heading?.text = "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"
                    }
                } else {
                    holder.buyNow?.text =
                        MainApplication.applicationContext().getString(R.string.proceed)
                    holder.heading?.text =
                        "${Utils.commaConversion(dataList!![position].price)} ${Constants.currencySign}"
                }
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

            Log.e("TAG","=====serviceType111===" + serviceType)

            if(serviceType != 1) { //PRODUCT
                val cartItem: Category? =
                    HomeActivity.cartList?.find { it?.id == dataList?.get(position)?.id }
                if (cartItem != null) {

                    Log.d("DATA_SUB","111")

                    Notify.alerterRed(activity, activity?.resources?.getString(R.string.already_added_to_cart_msg))
                } else {
                    itemClick.itemPosition(position)
                    holder.buyNow!!.setText(activity?.resources?.getString(R.string.added_to_cart_msg))
                    holder.buyNow!!.setBackgroundResource(R.drawable.button_shape_rectangle_green)
                }
            } else{ //service
                //OLD
                /*val cartItem: Category? = HomeActivity.cartList?.find { it?.id == dataList?.get(position)?.id }
                if (cartItem != null) {
                    Notify.alerterRed(activity, activity?.resources?.getString(R.string.already_added_to_cart_msg))
                } else {
                    itemClick.itemPosition(position)
                }*/

                //NEW
                itemClick.itemPosition(position)


            }

            //new condition added AD
            if(serviceType == 2){
                holder.buyNow?.text = MainApplication.applicationContext().getString(R.string.proceed)
                holder.buyNow!!.setBackgroundResource(R.drawable.button_shape_rectangle)
            }
        }
    }

    private fun getPrice(
        position: Int,
        couponVerification: CouponVerification,
        price: Double?,
        heading: TextView?
    ): String {
        var prices = price
        if (couponVerification.couponDetails?.type == Constants.DiscountType.PERCENTAGE) {

            //NEW
            if (price!!.minus(((price.toDouble() / 100) * couponVerification.couponDetails!!.amount)) <= 0) {
                prices = 1.0
                return showHHDiscountStrikethrough(Utils.commaConversion(prices),price,heading).toString()
            } else{
                prices = price.minus(((price.toDouble() / 100) * couponVerification.couponDetails!!.amount))
                return showHHDiscountStrikethrough(Utils.commaConversion(prices),price,heading).toString()
            }

        } else {
            if (price?.minus(couponVerification.couponDetails?.amount!!)!! <= 0) {
                prices = 1.0
                return showHHDiscountStrikethrough(Utils.commaConversion(prices),price,heading).toString()

            } else{
                prices = price.minus(couponVerification.couponDetails!!.amount)
                return showHHDiscountStrikethrough(Utils.commaConversion(prices),price,heading).toString()
            }
        }
    }

    private fun showHHDiscountStrikethrough(finalPrice: String, total: Double,heading: TextView?) : String{
        //SHOW hhDiscount in service detail view
        var finalString =""
        val originalPrice = Utils.commaConversion(total)
        if (originalPrice != null) {
            val discountedPrice = finalPrice
           /* val totalPrice = Utils.commaConversion(total)+ " "+Constants.currencySign

            val spannableOriginalPrice = SpannableString(totalPrice)
            spannableOriginalPrice.setSpan(StrikethroughSpan(), 0, totalPrice.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            finalString = SpannableStringBuilder()
                .append(spannableOriginalPrice)
                .append("\n")
                .append(discountedPrice +" ")
                .append("${Constants.currencySign}").toString()*/
            finalString = discountedPrice

           /* val spannableOriginalPrice = SpannableString(totalPrice)
            spannableOriginalPrice.setSpan(StrikethroughSpan(), 0, totalPrice.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            finalString = SpannableStringBuilder()
                .append(spannableOriginalPrice)
                .append("\n")
                .append(discountedPrice +" ")
                .append("${Constants.currencySign}").toString()*/
            //tv_heading_category!!.setText(finalString)
        }
        return finalString
    }
}