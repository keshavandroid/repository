package com.android.reloop.network.serializer.shop

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DeliveryFeeModel(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: Int
) : Serializable {
}