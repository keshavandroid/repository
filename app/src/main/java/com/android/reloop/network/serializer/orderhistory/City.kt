package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName

class City {
    @SerializedName("id")
    val id: Int?=0
    @SerializedName("name")
    val name: String?=""
    @SerializedName("status")
    val status: Int?=0
    @SerializedName("created_at")
    val created_at: String?=""
    @SerializedName("updated_at")
    val updated_at: String?=""
}