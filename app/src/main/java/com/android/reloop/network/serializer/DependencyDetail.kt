package com.reloop.reloop.network.serializer

import com.google.gson.annotations.SerializedName

class DependencyDetail {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("name")
    var name: String? = ""
    @SerializedName("city_id")
    var city_id: Int? = 0
    @SerializedName("status")
    var status: Int? = 0
}