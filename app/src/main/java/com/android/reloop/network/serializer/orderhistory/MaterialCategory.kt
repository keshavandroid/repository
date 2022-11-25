package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName

class MaterialCategory
{
    @SerializedName("id")
    val id: Int?=0
    @SerializedName("name")
    val name: String?=""
    @SerializedName("unit")
    val unit: Int?=0
}