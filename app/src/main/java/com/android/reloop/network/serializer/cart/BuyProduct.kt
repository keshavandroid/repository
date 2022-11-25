package com.reloop.reloop.network.serializer.cart

import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.shop.Product

class BuyProduct {
    var card_number: String? = null
    var exp_month: String? = null
    var exp_year: String? = null
    var cvv: String? = null
    var subtotal: Double? = null
    var points_discount: Double? = 0.0
    var coupon_id: Int? = null
    var total: Double? = null
    var price: Double? = null
    var first_name: String? = null
    var last_name: String? = null
    var email: String? = null
    var phone_number: String? = null
    var location: String? = null
    var map_location: String? = null
    var city_id: Int? = null
    var district_id: Int? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var products: ArrayList<Product>? = ArrayList()
    var organization_name: String? = null
    var cartListUpdated: ArrayList<Category?>? = ArrayList()
    var discountRedeem: Double? = 0.0
    var discountCoupon: Int? = 0
}