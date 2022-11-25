package com.reloop.reloop.auth

import android.app.Activity
import com.reloop.reloop.R
import com.reloop.reloop.utils.InternetCheck
import com.reloop.reloop.utils.Notify

object ChangePasswordAuth {
    fun authenticate(
        oldPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        activity: Activity
    ): Boolean {
        var check = false
        if (oldPassword.isEmpty() || oldPassword.length < 6) {
            if (oldPassword.isEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_old_password)
                )
                check = false
            } else if (oldPassword.length < 6) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.password_length_too_short)
                )
                check = false
            }

        } else if (newPassword.isEmpty() || newPassword.length < 6) {
            if (newPassword.isEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_new_password)
                )
                check = false
            } else if (newPassword.length < 6) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.password_length_too_short)
                )
                check = false
            }
        }

        else if (confirmNewPassword.isEmpty() || confirmNewPassword.length < 6) {
            if (confirmNewPassword.isEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_confirm_new_password)
                )
                check = false
            } else if (confirmNewPassword.length < 6) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.password_length_too_short)
                )
                check = false
            }
        }
        else {
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