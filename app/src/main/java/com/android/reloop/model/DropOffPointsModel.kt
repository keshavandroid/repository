package com.android.reloop.model

import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.network.serializer.DropOffPoints

import java.io.Serializable

class DropOffPointsModel  : Serializable {

    @SerializedName("dropOffPoints")
    var dropOffPoints: ArrayList<DropOffPoints>? = ArrayList()

    @SerializedName("dropOffDependencies")
    var dropOffDependencies: Dependencies = Dependencies()
}