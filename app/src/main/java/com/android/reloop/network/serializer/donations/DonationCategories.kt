package com.reloop.reloop.network.serializer.donations

import com.google.gson.annotations.SerializedName

class DonationCategories {

    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("name")
    var name: String? = ""
    @SerializedName("description")
    var description: String? = ""
    @SerializedName("status")
    var status: Int? = 0
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""
    @SerializedName("avatar")
    var avatar: String? = ""
    @SerializedName("points")
    var points: Int? = 0

}