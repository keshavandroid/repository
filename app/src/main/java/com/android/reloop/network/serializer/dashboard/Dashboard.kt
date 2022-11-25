package com.android.reloop.network.serializer.dashboard

import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription

class Dashboard {
    @SerializedName("environmentalStats")
    var environmentalStats: EnvironmentalStats? = EnvironmentalStats()

    @SerializedName("rewardPoints")
    var rewardPoints: Int? = 0

    @SerializedName("environmentalStatsDescriptions")
    var environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>? = ArrayList()

    @SerializedName("totalRecycledKg")
    var totalRecycledKg: Double? = 0.0

    @SerializedName("settings")
    var settings: SettingsModel = SettingsModel()

}