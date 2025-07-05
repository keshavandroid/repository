package com.android.reloop.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.orderhistory.OrderProduct
import java.io.Serializable

class SingleCard : Serializable {

    @SerializedName("id")
    var id: String? = null

    @SerializedName("card")
    var itemCard: ItemCard? = ItemCard()

    var isSelected: Boolean = false

    var isDelete: Boolean = false
    var itemClickable = true

    var isDefault: Boolean = false


}