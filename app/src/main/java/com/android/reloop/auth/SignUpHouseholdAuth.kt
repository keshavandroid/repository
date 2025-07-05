package com.reloop.reloop.auth

import android.app.Activity
import com.reloop.reloop.R
import com.reloop.reloop.utils.InternetCheck
import com.reloop.reloop.utils.Notify

object SignUpHouseholdAuth {
    fun authenticate(
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String,
        location: String,
        city: Int?,
        district: Int?,
        activity: Activity
    ): Boolean {
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
        } else if (confirmPassword.isEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.confirm_password_err_msg)
            )
            check = false
        } else if (password != confirmPassword) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.password_match_err_msg)
            )
            check = false
        } else if (phoneNumber.isEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_phone_number)
            )
            check = false
        }
        else if (phoneNumber.length < 15) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.phone_number_not_valid)
            )
            check = false
        }

            //Originaly added AD CHANGE
        /*else if (location.isEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.location_err_msg)
            )
            check = false
        }
        else if (city == 0) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.city_err_msg)
            )
            check = false
        } else if (district == 0) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.district_err_msg)
            )
            check = false
        }*/

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