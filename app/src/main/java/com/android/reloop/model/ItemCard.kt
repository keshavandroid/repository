package com.android.reloop.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ItemCard: Serializable {

    @SerializedName("brand")
    var brand: String? = null

    @SerializedName("last4")
    var last4: String? = null



}