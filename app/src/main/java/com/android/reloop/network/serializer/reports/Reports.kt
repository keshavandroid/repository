package com.reloop.reloop.network.serializer.reports

import com.google.gson.annotations.SerializedName

class Reports {
    @SerializedName("header")
    var header:ReportsHeader= ReportsHeader()
    @SerializedName("bar")
    var barChartValues: ArrayList<BarChart> = ArrayList()
    @SerializedName("pie")
    var pieChartValues:PieChart= PieChart()
}