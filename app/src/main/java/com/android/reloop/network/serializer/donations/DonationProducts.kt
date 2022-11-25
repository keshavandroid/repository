package com.reloop.reloop.network.serializer.donations

import com.google.gson.annotations.SerializedName

class DonationProducts {

    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("category_id")
    var category_id: Int? = 0
    @SerializedName("name")
    var name: String? = ""
    @SerializedName("redeem_points")
    var points: Int? = 0
    @SerializedName("description")
    var description: String? = ""
    @SerializedName("status")
    var status: Int? = 0
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""

}