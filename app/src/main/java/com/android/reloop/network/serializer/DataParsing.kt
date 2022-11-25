package com.reloop.reloop.network.serializer

import com.google.gson.annotations.SerializedName

class DataParsing {
    @SerializedName("buy_plan") val buyPlanId: ArrayList<String>? = null
    @SerializedName("buy_product") val buyProductId: ArrayList<String>? = null
    @SerializedName("collection_request") val collection_request: ArrayList<String>? = null
}