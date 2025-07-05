package com.reloop.reloop.network.serializer.billing

import com.google.gson.annotations.SerializedName

class UserSubscriptionsList {
    @SerializedName("subscription_number")
    var subscription_number: String? = ""
    @SerializedName("subscription_type")
    var subscription_type: Int? = 0
    /*@SerializedName("status")
    var status: Int? = 0*/

    @SerializedName("status")
    var status: String? = ""

    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("name")
    var name: String? = ""
    @SerializedName("trips")
    var trips: Int? = 0
    @SerializedName("total")
    var total: Double? = 0.0
}