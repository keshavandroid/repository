package com.android.reloop.model

import com.android.reloop.network.serializer.cart.SubscriberDiscount
import com.android.reloop.network.serializer.shop.DeliveryFeeModel
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.network.serializer.user.User
import java.io.Serializable

class CartModel  : Serializable {

    @SerializedName("deliveryFee")
    var deliveryFee: ArrayList<DeliveryFeeModel> = ArrayList()
    @SerializedName("subscriberDiscount")
    var subscriberDiscount: SubscriberDiscount? = null
}