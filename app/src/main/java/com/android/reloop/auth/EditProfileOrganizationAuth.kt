package com.reloop.reloop.auth

import android.app.Activity
import android.net.Uri
import com.reloop.reloop.R
import com.reloop.reloop.utils.InternetCheck
import com.reloop.reloop.utils.Notify

object EditProfileOrganizationAuth {
    fun authenticate(
        organizationName: String?,
        email: String?,
        phoneNumber: String?,
        noOfEmployees: Int?,
        noOfBranches: Int?,
        sectorId: Int,
        activity: Activity
    ): Boolean {
        var check = false
        if (organizationName.isNullOrEmpty()) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_organization_name)
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
        } else if (noOfEmployees == 0) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_no_of_employess)
            )
            check = false
        } else if (noOfBranches == 0) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.enter_no_of_branches)
            )
            check = false
        } else if (sectorId < 0) {
            Notify.alerterRed(
                activity,
                activity.resources.getString(R.string.selectSector)
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