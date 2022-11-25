package com.reloop.reloop.network.serializer.collectionrequest

import com.google.gson.annotations.SerializedName

class MaterialCategories {

    @SerializedName("id")
    var id: Int? = null
    @SerializedName("name")
    var name: String? = null
    @SerializedName("description")
    var description: String? = null
    @SerializedName("avatar")
    var avatar: String? = null
    @SerializedName("status")
    var status: Int? = 0
    @SerializedName("quantity")
    var quantity: Int? = 0
    @SerializedName("unit")
    var unit: Int? = 0
    @SerializedName("reward_points")
    var reward_points: Int? = null
    @SerializedName("created_at")
    var created_at: String? = null
    @SerializedName("updated_at")
    var updated_at: String? = null
    var selected:Boolean?=false
    @SerializedName("collection_acceptance_days") val collection_acceptance_days: ArrayList<String>? = null
}