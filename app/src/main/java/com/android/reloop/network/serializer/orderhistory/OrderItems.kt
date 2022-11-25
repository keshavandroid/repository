package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class OrderItems: Serializable {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("user_id")
    var user_id: Int? = 0
    @SerializedName("order_id")
    var order_id: Int? = 0
    @SerializedName("product_id")
    var product_id: Int? = 0
    @SerializedName("price")
    var price: Double? = 0.0
    @SerializedName("quantity")
    var quantity: Int? = 0
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""
    @SerializedName("product")
    var product: OrderProduct? = OrderProduct()

}