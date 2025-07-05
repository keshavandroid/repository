package com.android.reloop.model

import com.android.reloop.network.serializer.dashboard.SettingsModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import java.io.Serializable


class DataSettings :Serializable {

    @SerializedName("message")
    var message: String? = null
    @SerializedName("code")
    var code: Int? = null
    @SerializedName("status")
    var status: Boolean? = null
    @SerializedName("data")
    var settings: SettingsModel = SettingsModel()
}

