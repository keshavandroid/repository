package com.reloop.reloop.network.serializer.dashboard

import com.google.gson.annotations.SerializedName

class EnvironmentalStatsDescription {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("title")
    var title: String? = ""
    @SerializedName("description")
    var description: String? = ""
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""
}