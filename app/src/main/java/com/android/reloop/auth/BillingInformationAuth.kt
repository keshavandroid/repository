package com.reloop.reloop.auth

import android.app.Activity
import com.reloop.reloop.R
import com.reloop.reloop.utils.InternetCheck
import com.reloop.reloop.utils.Notify

object BillingInformationAuth {
    fun authenticate(
        cardNumber: String,
        cardExpiry: String,
        cardCVV: String,
        activity: Activity
    ): Boolean {
        var check = false
        if (cardNumber.isEmpty() || cardNumber.length != 16) {
            if (cardNumber.isEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_card_number)
                )
                check = false
            } else if (cardNumber.length != 16) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.card_number_not_valid)
                )
                check = false
            }

        } else if (cardExpiry.isEmpty() || cardExpiry.length != 5) {
            if (cardExpiry.isEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_card_expiry_info)
                )
                check = false
            } else if (cardExpiry.length != 5) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.card_expiry_info_not_valid)
                )
                check = false
            }
        } else if (cardCVV.isEmpty() || cardCVV.length != 3) {
            if (cardCVV.isEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_card_cvv)
                )
                check = false
            } else if (cardCVV.length != 3) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.card_cvv_not_valid)
                )
                check = false
            }
        } else {
            check = if (InternetCheck.getInstance()?.isNetworkAvailable(activity)!!) {
                true
            } else {
                InternetCheck.getInstance()?.alertDialog(activity)
                false
            }
        }
        return check
    }
}