package com.reloop.reloop.network.serializer.billing

import com.reloop.reloop.network.serializer.orderhistory.OrderItems
import com.google.gson.annotations.SerializedName

class UserProductList {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("order_number")
    var order_number: String? = ""
    @SerializedName("total")
    var total: Int? = 0
    @SerializedName("status")
    var status: Int? = 0
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("order_items")
    var order_items: ArrayList<OrderItems>? = ArrayList()
}