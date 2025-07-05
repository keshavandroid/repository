package com.reloop.reloop.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.reloop.activities.ContinueAsActivity
import com.android.reloop.adapters.AdapterFullScreenImage
import com.android.reloop.adapters.AdapterSlidingImageViewPager
import com.android.reloop.customviews.CustomViewPager2
import com.reloop.reloop.R
import com.reloop.reloop.activities.LoginActivity
import com.reloop.reloop.adapters.AdapterDonationProducts
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.customviews.CustomViewPager
import com.reloop.reloop.fragments.RecycleFragment
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.NotifyCallback
import com.reloop.reloop.network.serializer.donations.DonationProducts
import com.reloop.reloop.tinydb.TinyDB


object AlertDialogs {

    private var dialog: Dialog? = Dialog(MainApplication.applicationContext())
    fun informationDialog(
        activity: Activity?,
        icon: String?,
        heading: String?,
        descriptionText: String?,
        placeholder: Int?
    ) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_info_popup_dialog)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val closeButton = dialog.findViewById(R.id.close) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        val iconSet = dialog.findViewById(R.id.imageView_category_icon) as ImageView
        val textHeading = dialog.findViewById(R.id.tv_heading_category) as TextView
        val description = dialog.findViewById(R.id.description) as TextView
        description.movementMethod = ScrollingMovementMethod()
        //description.text = descriptionText

        if(descriptionText != null) {
            description.text = Html.fromHtml(descriptionText)
        }
        textHeading.text = heading
        Utils.glideImageLoaderServer(iconSet, icon, placeholder)
        closeButton.setOnClickListener { dialog.dismiss() }
        crossImage.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    fun alertDialogRewards(activity: Activity?, messageString: String?) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_rewards_message)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        message.text = messageString
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun informationDialog(
        activity: Activity?,
        icon: String?,
        heading: String?,
        descriptionText: String?,
        placeholder: Int?,
        avatars_: ArrayList<String>?
    ) {
        var avatars = avatars_
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_slider_info_popup)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val closeButton = dialog.findViewById(R.id.close) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
//        val iconSet = dialog.findViewById(R.id.imageView_category_icon) as ImageView
        val textHeading = dialog.findViewById(R.id.tv_heading_category) as TextView
        val description = dialog.findViewById(R.id.description) as TextView
        val slider = dialog.findViewById(R.id.slider) as ViewPager
        if (avatars.isNullOrEmpty()) {
            avatars = ArrayList()
            if (icon != null) {
                avatars.add(icon)
            }
        }
        val viewpagerAdapter = AdapterSlidingImageViewPager(activity, avatars)
        slider.adapter = viewpagerAdapter
        description.movementMethod = ScrollingMovementMethod()
        //description.text = descriptionText
        description.text = Html.fromHtml(descriptionText)
        textHeading.text = heading
//        Utils.glideImageLoaderServer(iconSet, icon, placeholder)
        closeButton.setOnClickListener { dialog.dismiss() }
        crossImage.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    fun showImageFullScreen(activity: Activity?, position: Int, avatars: ArrayList<String>?) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_image_full_screen)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.setCancelable(false)
        val crossImage = dialog.findViewById(R.id.back_btn) as ImageButton
        val slider = dialog.findViewById(R.id.slider) as CustomViewPager2
        val viewpagerAdapter = AdapterFullScreenImage(activity, avatars)
        slider?.adapter = viewpagerAdapter
        slider.currentItem = position
        crossImage.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    fun termConditionDialog(activity: Activity?, termAndConditions: String?) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_term_condition_popup)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val close = dialog.findViewById(R.id.close) as Button
        val next = dialog.findViewById(R.id.next) as Button
        val bodyText = dialog.findViewById(R.id.body_text) as TextView
        val agreementCheck = dialog.findViewById(R.id.agreementCheck) as CheckBox
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        //bodyText.text = termAndConditions
        bodyText.text = Html.fromHtml(termAndConditions)
        bodyText.movementMethod = ScrollingMovementMethod()
        next.setOnClickListener {
            if (agreementCheck.isChecked) {
                val tinyDB: TinyDB?
                tinyDB = TinyDB(MainApplication.applicationContext())
                tinyDB.putBoolean(Constants.agreementCheck, true)
                if (RecycleFragment.isAddedFragment) {
                    RecycleFragment.next?.performClick()
                }
                dialog.dismiss()
            } else {
                Notify.Toast("Please Agree to Term and Conditions First")
            }
        }
        crossImage.setOnClickListener { dialog.dismiss() }
        close.setOnClickListener { dialog.dismiss() }
        dialog.show()

    }

    @SuppressLint("SetTextI18n")
    fun redeemPointsDialog(
        activity: Activity?,
        discount: Float,
        subtotalPrice: Double?,
        totalUserPointsToRedeem: Double,
        callback: AlertDialogCallback
    ) {
        dialog = Dialog(activity!!)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setContentView(R.layout.row_redeem_points_popup)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCancelable(false)

        val userPoints = dialog?.findViewById(R.id.totalUserPoints) as TextView
        val redeem = dialog?.findViewById(R.id.redeem) as Button
        val cancel = dialog?.findViewById(R.id.cancel) as Button
        val crossImage = dialog?.findViewById(R.id.cross) as ImageButton
        val points_to_redeem = dialog?.findViewById(R.id.points_to_redeem) as CustomEditText
        val reloop_point_detail = dialog?.findViewById(R.id.reloop_point_detail) as TextView
        val discount_price = dialog?.findViewById(R.id.discount_price) as TextView
        var price = 0.0


        userPoints.text = totalUserPointsToRedeem.let { Utils.commaConversion(it) }

        points_to_redeem.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {

            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.isNotEmpty() && s.toString().toInt() <= totalUserPointsToRedeem) {
                    userPoints.text = Utils.commaConversion(totalUserPointsToRedeem.minus(s.toString().toInt()))
                    price = (s.toString().toInt() * discount).toDouble()
                    discount_price.text = "${Utils.commaConversion(price)} ${Constants.currencySign}"
                } else {
                    if (s.isNotEmpty() && s.toString().toDouble() > totalUserPointsToRedeem) {
                        Notify.Toast("Not Enough Points to Redeem")
                        userPoints.text = "0"
                    } else
                        userPoints.text = Utils.commaConversion(totalUserPointsToRedeem)
                }
            }
        })


        reloop_point_detail.text = "1 Reloop Point = $discount ${Constants.currencySign}"



        redeem.setOnClickListener {
            when {
                points_to_redeem.text.toString().isEmpty() -> {
                    Notify.Toast("Enter Points to Redeem")
                }
                price > subtotalPrice!! -> {
                    Notify.Toast("Cannot Redeem Points Amount is Greater than Price")
                }
                points_to_redeem.text.toString().isNotEmpty() && points_to_redeem.text.toString()
                    .toDouble() > totalUserPointsToRedeem -> {
                    Notify.Toast("Not Enough Points to Redeem")
                }
                else -> {
                    val mapPrices = HashMap<String, Double>()
                    mapPrices["discount_price"] = price
                    mapPrices["reward_points"] = points_to_redeem.text.toString().toDouble()
                    Notify.alerterGreen(activity, activity.resources.getString(R.string.successfully_redeem_points))
                    val update = Handler(Looper.getMainLooper())
                    update.postDelayed(
                        {
                            callback.callDialog(mapPrices)
                        },2000)

                    dialog?.dismiss()

                }
            }
        }
        crossImage.setOnClickListener { dialog?.dismiss() }
        cancel.setOnClickListener { dialog?.dismiss() }
        dialog?.show()
    }

    fun donateRedeemPointsPopup(
        activity: Activity?,
        list: ArrayList<DonationProducts?>,
        itemClick: AlertDialogCallback,
        name: String?
    ) {
        dialog = Dialog(activity!!)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.setContentView(R.layout.row_donation_redeem_points_popup)
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCancelable(false)
        val donationProductList = dialog?.findViewById(R.id.donation_products) as RecyclerView
        val cancel = dialog?.findViewById(R.id.cancel) as Button
        val crossImage = dialog?.findViewById(R.id.cross) as ImageButton
        val tvHeadingCategory = dialog?.findViewById(R.id.tv_heading_category) as TextView
        tvHeadingCategory.text = name
        donationProductList.layoutManager = LinearLayoutManager(activity)
        donationProductList.adapter = AdapterDonationProducts(list, itemClick)
        crossImage.setOnClickListener { dialog?.dismiss() }
        cancel.setOnClickListener { dialog?.dismiss() }
        dialog?.show()
    }

    fun alertDialog(activity: Activity?, callback: AlertDialogCallback, messageString: String?) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_logout)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm = dialog.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        message.text = messageString
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            callback.callDialog(null)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun alertDialogGuestAccount(activity: Activity?, callback: AlertDialogCallback, messageString: String?) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_guest_account)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm = dialog.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        message.text = messageString
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
//            callback.callDialog(null)
            try {
                val intent = Intent(activity, ContinueAsActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    fun alertDialogFragment(activity: Context?, callback: AlertDialogCallback, messageString: String?) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_logout)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm = dialog.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        message.text = messageString
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            callback.callDialog(1)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun alertDialogInput(activity: Activity?, callback: AlertDialogCallback) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_input_dialog)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val organization_code = dialog.findViewById(R.id.organization_code) as CustomEditText
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val verify = dialog.findViewById(R.id.verify) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
//        organization_code.setText("OrgId1591263510")
        verify.setOnClickListener {
            if (organization_code.text.toString().isEmpty()) {
                Notify.Toast(activity?.getString(R.string.enter_reloop_org_id))
            } else {
                callback.callDialog(organization_code.text.toString())
                dialog.dismiss()
            }
        }
        dialog.show()
    }


    fun dismissDialog() {
        dialog?.dismiss()
    }

    fun alertDialogVerifyOrganization(
        activity: Activity?,
        notifyCallback: NotifyCallback,
        messageString: String?
    ) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_logout)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm = dialog.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        confirm.text = "Yes"
        cancel.text = "No"
        message.text = messageString
        cancel.setOnClickListener {
            notifyCallback.callNotify(Constants.userNotPartOfOrganization)
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            notifyCallback.callNotify(Constants.userPartOfOrganization)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun unSubscribePopup(
        activity: Activity?,
        notifyCallback: NotifyCallback,
        unsubScribeType: Int
    ) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_logout)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val subHeading = dialog.findViewById(R.id.tv_sub_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm = dialog.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        confirm.text = "Unsubscribe"
        cancel.text = "Back"
        message.text = "Are you sure you want to unsubscribe?"
//        subHeading.text = "Please Note that you will lose any remaining trips"
        subHeading.text = "Please note that any remaining trips cannot be used. You may use them before unsubscribing."
        subHeading.visibility = View.VISIBLE
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            notifyCallback.callNotify(unsubScribeType)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun deleteOrgIDPopup(
        activity: Activity?,
        notifyCallback: NotifyCallback,
        messageString: String?
    ) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_logout)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog?.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog?.findViewById(R.id.cancel) as Button
        val confirm = dialog?.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        confirm.text = "Yes"
        cancel.text = "No"
        message.text = messageString
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            notifyCallback.callNotify(Constants.deleteOrganizationId)
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showInfoPopup(
        activity: Activity?,
        titleString: String?,
        messageString: String?
    ) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.show_info_popup_dialog)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val tvTitleInfo = dialog?.findViewById(R.id.tv_title_info) as TextView
        val tvMessageInfo = dialog?.findViewById(R.id.tv_message_info) as TextView
        val cancel = dialog?.findViewById(R.id.close) as Button
        cancel.text = "Close"
        tvTitleInfo.text = titleString
        tvMessageInfo.text = messageString
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}