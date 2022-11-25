package com.reloop.reloop.network.serializer.shop

import com.google.gson.annotations.SerializedName

class OneTimeServices {
    @SerializedName("id")
    val id: Int? = null
    @SerializedName("name")
    val name: String? = null
    @SerializedName("price")
    var price: Int? = null
    @SerializedName("category_type")
    val category_type: Int? = null
}