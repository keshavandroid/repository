package com.reloop.reloop.network.serializer.reports

import com.google.gson.annotations.SerializedName

class PieChart {
    @SerializedName("labels")
    var pieChartLabels: ArrayList<String> = ArrayList()
    @SerializedName("data")
    var pieChartValues: ArrayList<Double> = ArrayList()
    @SerializedName("units")
    var pieChartUnits: ArrayList<Int> = ArrayList()
    @SerializedName("icons")
    var pieChartIcons: ArrayList<String> = ArrayList()
}