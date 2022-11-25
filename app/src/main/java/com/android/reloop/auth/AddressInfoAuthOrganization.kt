package com.reloop.reloop.auth

import android.app.Activity
import com.reloop.reloop.R
import com.reloop.reloop.utils.Notify

object AddressInfoAuthOrganization {
    fun authenticate(
        organizationName: String,
        email: String,
        phoneNumber: String,
        location: String,
        cityId: Int?,
        districtId: Int,
        activity: Activity
    ): Boolean {
        var check = false
        if (organizationName.isEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_organization)
            )
            check = false
        } else if (email.isEmpty() || email.length < 6) {
            if (email.isEmpty()) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.enter_email)
                )
                check = false
            } else if (email.length < 6) {
                Notify.alerterRed(
                    activity,
                    activity.resources.getString(R.string.email_length_err_msg)
                )
                check = false
            }
        } else if (phoneNumber.isEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_phone_number)
            )
            check = false
        } else if (phoneNumber.length < 15) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.phone_number_not_valid)
            )
        } else if (location.isEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.no_address_found)
            )
            check = false
        } else if (cityId == 0) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.select_city)
            )
            check = false
        } else if (districtId < 0) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.select_district)
            )
            check = false
        } else {
            check = true
        }
        return check
    }
}