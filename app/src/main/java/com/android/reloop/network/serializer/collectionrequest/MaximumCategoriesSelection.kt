package com.android.reloop.network.serializer.collectionrequest

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class MaximumCategoriesSelection : Serializable {
    @SerializedName("id")
    var id: Int = null ?: 0

    @SerializedName("name")
    var name: String = null ?: ""

    @SerializedName("key")
    var key: String = null ?: ""

    @SerializedName("value")
    var value: Int = null ?: 3

}