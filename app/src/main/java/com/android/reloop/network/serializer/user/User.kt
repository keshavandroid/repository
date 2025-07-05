package com.reloop.reloop.network.serializer.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.organization.Organization
import com.reloop.reloop.utils.Constants
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class User : Serializable {
    @SerializedName("id")
    val id: Int? = 0
    @SerializedName("organization_id")
    val organization_id: Int? = 0
    @SerializedName("stripe_customer_id")
    val stripe_customer_id: String? = ""

    @SerializedName("stripe_id")
    val stripe_id: String? = ""

    @SerializedName("driver_photo_visibility")
    var driver_photo_visibility: String? = ""

    @SerializedName("supervisor_photo_visibility")
    var supervisor_photo_visibility: String? = ""

    @SerializedName("first_name")
    var first_name: String? = ""
    @SerializedName("last_name")
    var last_name: String? = ""
    @SerializedName("email")
    val email: String? = ""
    @SerializedName("phone_number")
    val phone_number: String? = ""
    @SerializedName("hh_organization_name")
    val hh_organization_name: String? = ""
    @SerializedName("birth_date")
    val birth_date: String? = ""
    @SerializedName("avatar")
    val avatar: String? = ""

    //Organization logo
    @SerializedName("reloop_client_logo")
    val reloop_client_logo: String? = ""

    @SerializedName("user_type")
    var user_type: Int? = 0
    @SerializedName("login_type")
    val login_type: Int? = 0
    @SerializedName("trips")
    val trips: Int? = 0
    @SerializedName("reward_points")
    var reward_points: Int? = 0
    @SerializedName("status")
    val status_user: Int? = 0
    @SerializedName("reports")
    val reports: Int? = 0
    @SerializedName("verified_at")
    val verified_at: String? = ""
    @SerializedName("signup_token")
    val signup_token: String? = ""
    @SerializedName("api_token")
    var api_token: String? = ""
    @SerializedName("created_at")
    val created_at: String? = ""
    @SerializedName("updated_at")
    val updated_at: String? = ""
    @SerializedName("addresses")
    var addresses: ArrayList<Addresses>? = ArrayList()
    @SerializedName("organization")
    var organization: Organization? = Organization()
    @SerializedName("gender")
    val gender: String? = ""

    companion object {
        fun retrieveUser(): User? {

            val sharedPref: SharedPreferences =
                MainApplication.applicationContext().getSharedPreferences(
                    Constants.PREF_NAME,
                    Constants.PRIVATE_MODE
                )
            val gson = Gson()
            val json: String? = sharedPref.getString(Constants.DataConstants.user, "")
            if (gson.fromJson(json, User::class.java) != null)
                return gson.fromJson(json, User::class.java)
            else {
                return null
            }
        }

        @SuppressLint("CommitPrefEdits")
        fun clearUser() {

            val sharedPref: SharedPreferences =
                MainApplication.applicationContext().getSharedPreferences(
                    Constants.PREF_NAME,
                    Constants.PRIVATE_MODE
                )
            val editor: SharedPreferences.Editor = sharedPref.edit()
            editor.clear()
            editor.apply()
        }
    }

    init {

        id
        organization_id
        stripe_customer_id
        first_name
        last_name
        email
        phone_number
        birth_date
        avatar
        user_type
        trips
        reward_points
        status_user
        verified_at
        signup_token
        api_token
        created_at
        updated_at
        addresses
        organization

    }

    fun save(user: User, context: Context?,fromLogin : Boolean) {
        val sharedPref: SharedPreferences? =
            context?.getSharedPreferences(
                Constants.PREF_NAME,
                Constants.PRIVATE_MODE
            )
        Log.d(User::class.java.simpleName, "save: FROM LOGIN ? $fromLogin")

        //NEW CODE to use api token from login api only and do not replace if in other apis FOR STAGING PURPOSE
        if (!fromLogin) {
            if (retrieveUser() != null && retrieveUser()?.api_token?.isNotEmpty()!!){
                user.api_token = retrieveUser()!!.api_token
            }
        }
        val prefsEditor: SharedPreferences.Editor? = sharedPref?.edit()
        val gson = Gson()
        val json = gson.toJson(user)
        prefsEditor?.putString(Constants.DataConstants.user, json)
        prefsEditor?.apply()
    }


}