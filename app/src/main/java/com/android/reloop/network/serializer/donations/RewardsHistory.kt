package com.reloop.reloop.network.serializer.donations

import com.google.gson.annotations.SerializedName

class RewardsHistory {

    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("user_id")
    var user_id: Int? = 0
    @SerializedName("donation_product_id")
    var donation_product_id: Int? = 0
    @SerializedName("redeemed_points")
    var redeemed_points: Int? = 0
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""
    @SerializedName("donation_product")
    var donationProduct: DonationProducts? = DonationProducts()
}