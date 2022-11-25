package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName

class DistrictWithOrder {
    @SerializedName("id")
    val id: Int? = 0

    @SerializedName("city_id")
    val city_id: Int? = 0

    @SerializedName("name")
    val name: String? = ""

    @SerializedName("status")
    val status: Int? = 0

    @SerializedName("order_acceptance_days")
    val order_acceptance_days: ArrayList<String>? = ArrayList()

    @SerializedName("created_at")
    val created_at: String? = ""

    @SerializedName("updated_at")
    val updated_at: String? = ""
}