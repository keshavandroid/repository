package com.reloop.reloop.network.serializer.shop

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BuyPlanMonthly {

    @SerializedName("subscription_id")
    @Expose
    var subscription_id: Int? = 0

    @SerializedName("subscription_type")
    @Expose
    var subscription_type: Int? = 0

    @SerializedName("total")
    @Expose
    var total: Double? = 0.0

    @SerializedName("coupon_id")
    @Expose
    var coupon_id: Int? = 0

    var transaction_id:String?= null
    var is_monthly:Int?=0
    var stripe_subscription_id:String?=null

    var hh_discount_id:String?=null


}