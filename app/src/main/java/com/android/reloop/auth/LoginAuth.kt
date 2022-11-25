package com.reloop.reloop.auth

import android.app.Activity
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.utils.InternetCheck
import com.reloop.reloop.utils.Notify


object LoginAuth {
    fun authenticate(email: String, password: String, activity: Activity): Boolean {
        var check = false
        if (email.isEmpty() || email.length < 6) {
            if (email.isEmpty()) {
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

        } else if (password.isEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.password_err_msg)
            )
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