package com.android.reloop.network.serializer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FacebookLoginModel : Serializable {

    @SerializedName("id")
    @Expose
    var id: String = null ?: ""

    @SerializedName("first_name")
    @Expose
    var first_name: String = null ?: ""

    @SerializedName("last_name")
    @Expose
    var last_name: String = null ?: ""

    @SerializedName("email")
    @Expose
    var email: String = null ?: ""
}