package com.reloop.reloop.network.serializer.reports

import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.user.User

class ReportsMain {

    @SerializedName("userProfile")
    var userProfile: User? = User()

    @SerializedName("connectedOrgs")
    var connectedOrgs: ArrayList<User>? = ArrayList()

    @SerializedName("userReports")
    var reports: Reports? = Reports()

    @SerializedName("environmentalStats")
    var environmentalStats: EnvironmentalStats? = EnvironmentalStats()

    @SerializedName("rewardPoints")
    var rewardPoints: Int? = 0

    @SerializedName("environmentalStatsDescription")
    var environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>? = ArrayList()

    @SerializedName("connectedOrgsAccounts")
    var connectedOrgsAccounts: String? = ""

    @SerializedName("connectedHouseholdAccounts")
    var connectedHouseholdAccounts: String? = ""
}