package com.android.reloop.network.serializer.cart

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class SubscriberDiscount {

    @SerializedName("order_delivery_discount")
    @Expose
    private var orderDeliveryDiscount: String? = null

    @SerializedName("products_order_discount")
    @Expose
    private var productsOrderDiscount: String? = null

    fun getOrderDeliveryDiscount(): String? {
        return orderDeliveryDiscount
    }

    fun setOrderDeliveryDiscount(orderDeliveryDiscount: String?) {
        this.orderDeliveryDiscount = orderDeliveryDiscount
    }

    fun getProductsOrderDiscount(): String? {
        return productsOrderDiscount
    }

    fun setProductsOrderDiscount(productsOrderDiscount: String?) {
        this.productsOrderDiscount = productsOrderDiscount
    }
}