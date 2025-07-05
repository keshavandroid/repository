package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.user.User
import java.io.Serializable

class OrderHistory : Serializable {

    @SerializedName("hasCollectionBins")
    var hasCollectionBins: Boolean? = false

    @SerializedName("getUserOrders")
    var getUserOrders: ArrayList<UserOrders>? = ArrayList()
    @SerializedName("getUserCollectionRequests")
    var getUserCollectionRequests: ArrayList<CollectionRequests>? = ArrayList()

    @SerializedName("materialCategories")
    var materialCategories: ArrayList<MaterialCategories>? = ArrayList()

    //NEW
    @SerializedName("connectedOrgs")
    var connectedOrgs: ArrayList<User>? = ArrayList()
}