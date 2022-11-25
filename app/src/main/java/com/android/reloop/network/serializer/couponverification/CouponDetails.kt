package com.reloop.reloop.network.serializer.couponverification

import com.google.gson.annotations.SerializedName

class CouponDetails
{
    @SerializedName("id")
    val id: Int? = 0
    @SerializedName("code")
    val code: Any? = null
    @SerializedName("type")
    val type: Int? = 0
    @SerializedName("amount")
    val amount: Double = 0.0
    @SerializedName("apply_for_user")
    var apply_for_user :Int?=0
    @SerializedName("coupon_user_type")
    var coupon_user_type :Int?=0
    @SerializedName("list_user_id")
    var list_user_id :Int?=0
    @SerializedName("apply_for_category")
    var apply_for_category :Int?=0
    @SerializedName("coupon_category_type")
    var coupon_category_type :Int?=0
    @SerializedName("list_category_id")
    var list_category_id :Int?=0
    @SerializedName("created_at")
    val created_at: String? = ""
    @SerializedName("updated_at")
    val updated_at: String? = ""
}