package com.reloop.reloop.network.serializer.collectionrequest

import com.google.gson.annotations.SerializedName

class UserPlans {

    @SerializedName("id")
    val id: Int? = null
    @SerializedName("subscription_number")
    val subscription_number: String? = null
    @SerializedName("status")
    val status: Int? = null
    @SerializedName("trips")
    val trips: Int? = null
    @SerializedName("start_date")
    val start_date: String? = null
    @SerializedName("end_date")
    val end_date: String? = null
    @SerializedName("subscription_type")
    val subscription_type: Int? = null
}