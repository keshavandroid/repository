package com.reloop.reloop.network.serializer

import com.android.reloop.network.serializer.dashboard.SettingValuesModel
import com.google.gson.annotations.SerializedName

class Dependencies() {

    @SerializedName("cities")
    var cities: ArrayList<DependencyDetail>? = ArrayList()
    @SerializedName("districts")
    var districts: ArrayList<DependencyDetail>? = ArrayList()
    @SerializedName("sectors")
    var sectors: ArrayList<DependencyDetail>? = ArrayList()
    @SerializedName("organizations")
    var organizations: ArrayList<DependencyDetail>? = ArrayList()

    @SerializedName("settings")
    var whatsapp_Number: ArrayList<SettingValuesModel> = null ?: ArrayList()
}