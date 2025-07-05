package com.reloop.reloop.network.serializer.reports

import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription
import com.google.gson.annotations.SerializedName

class PieChartMain {

    @SerializedName("userReports")
    var reports: PieChart? = PieChart()

    @SerializedName("environmentalStats")
    var environmentalStats: EnvironmentalStats? = EnvironmentalStats()

    @SerializedName("rewardPoints")
    var rewardPoints: Int? = 0

    @SerializedName("environmentalStatsDescription")
    var environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>? = ArrayList()
}