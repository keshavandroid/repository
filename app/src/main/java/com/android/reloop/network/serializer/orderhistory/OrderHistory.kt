package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class OrderHistory : Serializable {
    @SerializedName("getUserOrders")
    var getUserOrders: ArrayList<UserOrders>? = ArrayList()
    @SerializedName("getUserCollectionRequests")
    var getUserCollectionRequests: ArrayList<CollectionRequests>? = ArrayList()
}