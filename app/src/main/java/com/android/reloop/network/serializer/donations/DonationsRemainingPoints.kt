package com.reloop.reloop.network.serializer.donations

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DonationsRemainingPoints {
    @SerializedName("remainingPoints")
    @Expose
    val remainingPoints: Int?=0

}