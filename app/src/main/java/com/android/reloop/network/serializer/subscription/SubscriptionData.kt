package com.reloop.reloop.network.serializer.subscription

import com.google.gson.annotations.SerializedName

class SubscriptionData {

    @SerializedName("id")
    var id: Int? = 0

    @SerializedName("user_id")
    var user_id: Int? = 0

    @SerializedName("subscription_id")
    var subscription_id: Int? = 0

    @SerializedName("stripe_subscription_id")
    var stripe_subscription_id: String? = ""

    @SerializedName("subscription_number")
    var subscription_number: String? = ""

    @SerializedName("subscription_type")
    var subscription_type: Int? = null

    //OLD
    /*@SerializedName("status")
    var status: Int? = null*/

    //NEW
    @SerializedName("status")
    var status: String? = ""

    @SerializedName("start_date")
    var start_date: String? = ""

    @SerializedName("end_date")
    var end_date: String? = ""

    @SerializedName("yearly_renew")
    var yearly_renew: String? = ""



    @SerializedName("trips")
    var trips: Int? = 0

    @SerializedName("created_at")
    var created_at: String? = ""

    @SerializedName("updated_at")
    var updated_at: String? = ""

    @SerializedName("free_subscription_expiry")
    var free_subscription_expiry: String? = ""

    @SerializedName("subscription")
    var subscription: SubscriptionDetail? = SubscriptionDetail()

}