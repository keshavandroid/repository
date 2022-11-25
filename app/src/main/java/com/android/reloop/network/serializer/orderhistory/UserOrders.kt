package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UserOrders : Serializable {
    @SerializedName("id")
    var id: Int? = 0

    @SerializedName("order_number")
    var order_number: String? = ""

    @SerializedName("total")
    var total: Double? = 0.0

    @SerializedName("status")
    var status: Int? = 0

    @SerializedName("created_at")
    var created_at: String? = ""

    @SerializedName("location")
    var location: String? = ""

    @SerializedName("latitude")
    var latitude: Double? = 0.0

    @SerializedName("longitude")
    var longitude: Double? = 0.0

    @SerializedName("city")
    var city: City? = City()

    @SerializedName("district")
    var district: District? = District()

    @SerializedName("order_items")
    var order_items: ArrayList<OrderItems>? = ArrayList()

    @SerializedName("address")
    var address: String? = ""

    @SerializedName("map_location")
    var map_location: String? = ""


    @SerializedName("refunded_amount")
    var refunded_amount: String? = ""

    @SerializedName("delivery_fee")
    var delivery_fee: String? = ""

}