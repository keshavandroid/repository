package com.android.reloop.network.serializer.dashboard

import com.google.gson.annotations.SerializedName

class SettingValuesModel {
    @SerializedName("id")
    var id: Int = null ?: 0

    @SerializedName("name")
    var name: String = null ?: ""

    @SerializedName("key")
    var key: String = null ?: ""

    @SerializedName("value")
    var value: String = null ?: ""
}