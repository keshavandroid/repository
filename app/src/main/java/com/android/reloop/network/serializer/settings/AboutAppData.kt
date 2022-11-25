package com.reloop.reloop.network.serializer.settings

import com.google.gson.annotations.SerializedName

class AboutAppData {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("title")
    var title: String? = ""
    @SerializedName("body")
    var body: String? = ""
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""

}