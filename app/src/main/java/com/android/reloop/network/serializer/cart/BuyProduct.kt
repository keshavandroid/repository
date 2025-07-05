package com.reloop.reloop.network.serializer.cart

import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.shop.Product
import org.json.JSONObject

class BuyProduct {
    var card_number: String? = null
    var exp_month: String? = null
    var exp_year: String? = null
    var cvv: String? = null
    var card_holder: String? = ""
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
    var city_id: Int? = 0
    var district_id: Int? = 0
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var products: ArrayList<Product>? = ArrayList()
    //val products: List<List<Product>> = ArrayList()
    var organization_name: String? = null
    var cartListUpdated: ArrayList<Category?>? = ArrayList()
    var discountRedeem: Double? = 0.0
    var discountCoupon: Int? = 0

    var delivery_fee: String? = null
    var is_new_card: String = "1"

    //new added
    var user_card_id:String?= null
    var transaction_id:String?= null
//    var payment_type:String?= "stripe"

    var payment_method:String?= null
}