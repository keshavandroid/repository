package com.android.reloop.model

import com.android.reloop.network.serializer.dashboard.SettingsModel
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import java.io.Serializable


class CommanDaysData :Serializable {

    @SerializedName("days")
    var days: ArrayList<String>? = null

    @SerializedName("settings")
    var dataSettings: DataSettings = DataSettings()
}

