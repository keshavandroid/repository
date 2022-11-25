package com.reloop.reloop.network.serializer.billing

import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.google.gson.annotations.SerializedName

class Billing {
    @SerializedName("userSubscriptionsList")
    var userSubscriptionsList: ArrayList<UserSubscriptionsList>? = ArrayList()
    @SerializedName("userOrdersList")
    var userOrdersList: ArrayList<UserOrders>? = ArrayList()
}