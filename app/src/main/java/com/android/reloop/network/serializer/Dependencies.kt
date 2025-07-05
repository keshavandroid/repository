package com.reloop.reloop.network.serializer

import com.android.reloop.network.serializer.dashboard.SettingValuesModel
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories

class Dependencies() {

    @SerializedName("cities")
    var cities: ArrayList<DependencyDetail>? = ArrayList()

    @SerializedName("materialCategories")
    var materialCategories: ArrayList<MaterialCategories>? = ArrayList()

    @SerializedName("districts")
    var districts: ArrayList<DependencyDetail>? = ArrayList()
    @SerializedName("sectors")
    var sectors: ArrayList<DependencyDetail>? = ArrayList()
    @SerializedName("organizations")
    var organizations: ArrayList<DependencyDetail>? = ArrayList()

    @SerializedName("settings")
    var whatsapp_Number: ArrayList<SettingValuesModel> = null ?: ArrayList()
}