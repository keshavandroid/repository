package com.reloop.reloop.network.serializer.shop

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BuyPlan {
    @SerializedName("card_number")
    @Expose
    var card_number: String? = ""
    @SerializedName("exp_month")
    @Expose
    var exp_month: String? = ""
    @SerializedName("exp_year")
    @Expose
    var exp_year: String? = ""
    @SerializedName("cvv")
    @Expose
    var cvv: String? = ""
    @SerializedName("plan_id")
    @Expose
    var plan_id: String? = ""
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
    var changePrice: Int? = 0


}