package com.reloop.reloop.auth

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.reloop.reloop.R
import com.reloop.reloop.utils.InternetCheck
import com.reloop.reloop.utils.Notify

object EditProfileHouseHoldAuth {
    fun authenticate(
        name: String?,
        last_Name: String?,
        email: String?,
        phoneNumber: String?,
        activity: Activity
    ): Boolean {
        var check = false
        if (name.isNullOrEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_first_name)
            )
            check = false
        } else if (last_Name.isNullOrEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_last_name)
            )
            check = false

        } else if (email.isNullOrEmpty() || email.length < 6) {
            if (email.isNullOrEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_email_err_msg)
                )
                check = false
            } else if (email.length < 6) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.email_length_err_msg)
                )
                check = false
            }

        } else if (phoneNumber.isNullOrEmpty() || phoneNumber.length < 15) {
            if (phoneNumber.isNullOrEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_phone_number)
                )
            } else if (phoneNumber.length < 15) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.phone_number_not_valid)
                )
            }
            check = false
        } else {
            if (InternetCheck.getInstance()?.isNetworkAvailable(activity)!!) {
                check = true
            } else {
                InternetCheck.getInstance()?.alertDialog(activity)
                check = false
            }
        }
        return check
    }
}