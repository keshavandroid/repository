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

    @SerializedName("user_subscription_id")
    var user_subscription_id: String? = ""


    /*@SerializedName("status")
    var status: Int? = 0*/

    @SerializedName("status")
    var status: String? = ""



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

    @SerializedName("delivery_date")
    var delivery_date: String? = ""


    @SerializedName("refunded_amount")
    var refunded_amount: String? = ""

    @SerializedName("delivery_fee")
    var delivery_fee: String? = ""

    @SerializedName("user_comments")
    var user_comments: String? = ""



    @SerializedName("first_name")
    var first_name: String? = ""

    @SerializedName("last_name")
    var last_name: String? = ""

    @SerializedName("email")
    var email: String? = ""

    @SerializedName("phone_number")
    var phone_number: String? = ""


}