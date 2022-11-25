package com.reloop.reloop.network.serializer.subscription

import com.google.gson.annotations.SerializedName

class SubscriptionDetail {

    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("category_id")
    var category_id: Int? = 0
    @SerializedName("stripe_product_id")
    var stripe_product_id: String? = ""
    @SerializedName("name")
    var name: String? = ""
    @SerializedName("price")
    var price: Double? = null
    @SerializedName("description")
    var description: String? = null
    @SerializedName("avatar")
    var avatar: String? = ""
    @SerializedName("request_allowed")
    var request_allowed: Int? = null
    @SerializedName("category_type")
    var category_type: Int? = null
    @SerializedName("status")
    var status: Int? = null
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""
}