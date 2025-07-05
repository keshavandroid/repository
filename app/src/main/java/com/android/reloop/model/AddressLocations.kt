package com.android.reloop.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AddressLocations : Serializable {

    @SerializedName("id")
    val id: Int? = 0

    @SerializedName("address_id")
    val address_id: Int? = 0

    @SerializedName("location")
    val location: String? = ""

    @SerializedName("created_at")
    val created_at: String? = ""

    @SerializedName("updated_at")
    val updated_at: String? = ""

    @SerializedName("deleted_at")
    val deleted_at: String? = ""
}