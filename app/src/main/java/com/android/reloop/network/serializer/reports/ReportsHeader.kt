package com.reloop.reloop.network.serializer.reports

import com.google.gson.annotations.SerializedName

class ReportsHeader {

    @SerializedName("start")
    var start: String? = ""
    @SerializedName("end")
    var end: String? = ""
    @SerializedName("prev")
    var previous: String? = ""
    @SerializedName("next")
    var next: String? = ""
    @SerializedName("text")
    var heading: String? = ""
}