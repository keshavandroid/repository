package com.reloop.reloop.network.serializer.shop

import com.google.gson.annotations.SerializedName


data class Category(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: Int?,
    @SerializedName("status") val status: Int?,
    @SerializedName("created_at") val created_at: String?,
    @SerializedName("updated_at") val updated_at: String?,
    @SerializedName("category_id") val category_id: Int?,
    @SerializedName("price") var price: Double?,
    @SerializedName("stripe_product_id") val stripe_product_id: String?,
    @SerializedName("avatar") private val avatar_: String?,
    @SerializedName("avatars") val avatars: ArrayList<String>?,
    @SerializedName("quantity") var limit: Int? = 0,
    @SerializedName("addCartQuantity") var quantity: Int? = 1,
    @SerializedName("service_type") val service_type: Int?,
    var originalPrice: Double? = 0.0,
    var discountedPrice: Double? = 0.0
) {
    var avatarToShow: String? = avatar_
        get(): String? {
            avatarToShow = if (avatar_.isNullOrEmpty() && !avatars.isNullOrEmpty()) {
                avatars[0]
            } else {
                avatar_
            }
            return field
        }
}