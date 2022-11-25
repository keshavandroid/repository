package com.reloop.reloop.utils

import android.app.Activity
import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.NotifyCallback
import com.reloop.reloop.interfaces.RemoveItemCart
import com.reloop.reloop.network.serializer.shop.Category
import com.tapadoo.alerter.Alerter


object Notify {
    const val SUCCESS_COLOR = "#22A109"
    const val FAILURE_COLOR = "#EC0909"
    //    private static Snackbar snackbar;

    //    private static Snackbar snackbar;
    fun Toast(message: String?) {
        Toast.makeText(
            MainApplication.applicationContext(),
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    fun alerterRed(
        activity: Context?,
        message: String?
    ) {
        if (activity != null) Alerter.create(activity as Activity?)
            .setText(message!!)
            .setIcon(R.drawable.warning_sign)
            .setBackgroundColorRes(R.color.light_orange)
            .show()
    }

    fun alerterGreen(activity: Activity?, message: String?) {
        if (activity != null) Alerter.create(activity)
            .setText(message!!)
            .setTextAppearance(R.color.white)
            .setBackgroundColorRes(R.color.light_green)
            .show()
    }

    fun alerterRedButton(
        activity: Activity?,
        message: String?,
        item: Category?,
        removeItemCart: RemoveItemCart?
    ) {
        Alerter.create(activity)
            .setText(message!!)
            .setDismissable(true)
            .setTextAppearance(R.style.AlertTextAppearance_Title)
            .enableInfiniteDuration(true)
            .setBackgroundColorRes(R.color.light_orange)
            .setTextAppearance(R.color.white)
            .setIcon(R.drawable.warning_sign)
            .enableSwipeToDismiss()
            .addButton("Yes", R.style.AlertStyleButtons, View.OnClickListener {
                removeItemCart?.removeItem(item)
                if (Alerter.isShowing) {
                    Alerter.hide()
                }
            })
            .addButton("No", R.style.AlertStyleButtons, View.OnClickListener {
                if (Alerter.isShowing) {
                    Alerter.hide()
                }
            })
            .show()
    }

    fun hyperlinkAlert(
        activity: Activity?,
        message: String,
        link: String,
        callback: AlertDialogCallback,
        value: Int
    ) {
        val content = SpannableString(link)
        content.setSpan(UnderlineSpan(), 0, link.length, 0)
        Alerter.create(activity)
            .setText(message)
            .setDuration(5000)
            .setBackgroundColorRes(R.color.light_orange)
            .setTextAppearance(R.color.white)
            .setIcon(R.drawable.warning_sign)
            .addButton(content, R.style.AlertStyleButtons2, View.OnClickListener {
                callback.callDialog(value)
                if (Alerter.isShowing) {
                    Alerter.hide()
                }
            })
            .enableSwipeToDismiss()
            .show()
    }

    fun alerterOrganizationConfirmation(
        activity: Activity?,
        message: String?,
        notifyCallback: NotifyCallback
    ) {
        Alerter.create(activity)
            .setText(message!!)
            .setDismissable(true)
            .setTextAppearance(R.style.AlertTextAppearance_Title)
            .enableInfiniteDuration(true)
            .setBackgroundColorRes(R.color.light_orange)
            .setTextAppearance(R.color.white)
            .setIcon(R.drawable.warning_sign)
            .enableSwipeToDismiss()
            .addButton("Yes", R.style.AlertStyleButtons, View.OnClickListener {
                if (Alerter.isShowing) {
                    Alerter.hide()
                    notifyCallback.callNotify(Constants.userPartOfOrganization)
                }
            })
            .addButton("No", R.style.AlertStyleButtons, View.OnClickListener {
                if (Alerter.isShowing) {
                    Alerter.hide()
                    notifyCallback.callNotify(Constants.userNotPartOfOrganization)
                }
            })
            .show()
    }
}