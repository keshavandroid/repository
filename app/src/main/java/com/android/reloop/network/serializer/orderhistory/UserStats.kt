package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName

class UserStats
{
    @SerializedName("id")
    val id: Int?=0
    @SerializedName("user_id")
    val user_id: Int?=0
    @SerializedName("request_collection_id")
    val request_collection_id: Int?=0

    @SerializedName("co2_emission_reduced")
    val co2_emission_reduced: String?=""
    @SerializedName("trees_saved")
    val trees_saved: String?=""
    @SerializedName("oil_saved")
    val oil_saved: String?=""
    @SerializedName("electricity_saved")
    val electricity_saved: String?=""
    @SerializedName("water_saved")
    val water_saved: String?=""
    @SerializedName("landfill_space_saved")
    val landfill_space_saved: String?=""
    @SerializedName("compost_created")
    val compost_created: String?=""
    @SerializedName("cigarette_butts_saved")
    val cigarette_butts_saved: String?=""
    @SerializedName("biodiesel_produced")
    val biodiesel_produced: String?=""
    @SerializedName("farming_land")
    val farming_land: String?=""
    @SerializedName("coffee_capsule")
    val coffee_capsule: String?=""
    @SerializedName("soap_bars")
    val soap_bars: String?=""

}