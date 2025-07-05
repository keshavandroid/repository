package com.android.reloop.network.serializer.dashboard

import com.android.reloop.network.serializer.Campain.Campaigns.GetCampaigns
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription
import com.reloop.reloop.network.serializer.user.Discount
import com.reloop.reloop.network.serializer.user.User

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

    @SerializedName("userProfile")
    var userProfile: User? = User()

    @SerializedName("hhDiscount")
    var hhDiscount: Discount? = Discount()

    @SerializedName("campaigns")
    var campaigns:ArrayList<GetCampaigns?>? = ArrayList()

}