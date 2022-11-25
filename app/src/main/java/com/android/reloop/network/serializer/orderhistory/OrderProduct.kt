package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class OrderProduct: Serializable {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("name")
    var name: String? = ""
}