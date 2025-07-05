package com.reloop.reloop.network.serializer.user

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.organization.Organization
import com.reloop.reloop.utils.Constants
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class Discount : Serializable {

    @SerializedName("id")
    val id: Int? = 0

    @SerializedName("user_id")
    val user_id: Int? = 0

    @SerializedName("is_used")
    val is_used: Int? = 0

    @SerializedName("organization_id")
    val organization_id: Int? = 0

    @SerializedName("discount_type")
    val discount_type: String? = ""

    @SerializedName("discount_validity")
    val discount_validity: String? = ""

    @SerializedName("discount_code")
    val discount_code: String? = ""

    @SerializedName("created_at")
    var created_at: String? = ""

    @SerializedName("updated_at")
    var updated_at: String? = ""


}