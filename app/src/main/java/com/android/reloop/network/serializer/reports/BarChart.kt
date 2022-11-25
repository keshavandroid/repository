package com.reloop.reloop.network.serializer.reports

import com.google.gson.annotations.SerializedName

class BarChart {
    @SerializedName("label")
    var label: String? = ""
    @SerializedName("data")
    var barChartData: BarChartData = BarChartData()
    @SerializedName("y")
    var yAxisValue: Float = 0f
}