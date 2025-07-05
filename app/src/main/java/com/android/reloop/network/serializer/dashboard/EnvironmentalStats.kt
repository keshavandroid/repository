package com.reloop.reloop.network.serializer.dashboard

import com.google.gson.annotations.SerializedName

class EnvironmentalStats {
    @SerializedName("id")
    var id: Int? = 0

    @SerializedName("user_id")
    var user_id: Int? = 0

    @SerializedName("co2_emission_reduced")
    var co2_emission_reduced: Double = null ?: 0.0

    @SerializedName("trees_saved")
    var trees_saved: Double = null ?: 0.0

    @SerializedName("oil_saved")
    var oil_saved: Double = null ?: 0.0

    @SerializedName("electricity_saved")
    var electricity_saved: Double = null ?: 0.0

    @SerializedName("water_saved")
    var water_saved: Double = null ?: 0.0

    @SerializedName("landfill_space_saved")
    var landfill_space_saved: Double = null ?: 0.0

    @SerializedName("compost_created")
    var compost_created: Double = null ?: 0.0

    @SerializedName("cigarette_butts_saved")
    var cigarette_butts_saved: Double = null ?: 0.0

    @SerializedName("farming_land")
    var farming_land: Double = null ?: 0.0

    @SerializedName("coffee_capsule")
    var coffee_capsule: Double = null ?: 0.0

    @SerializedName("soap_bars")
    var soap_bars: Double = null ?: 0.0



    @SerializedName("biodiesel_produced")
    var biodiesel_produced: Double = null ?: 0.0

    @SerializedName("created_at")
    var created_at: String? = ""

    @SerializedName("updated_at")
    var updated_at: String? = ""
}