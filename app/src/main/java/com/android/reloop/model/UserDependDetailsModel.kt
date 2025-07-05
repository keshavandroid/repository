package com.android.reloop.model

import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.network.serializer.user.User
import java.io.Serializable

class UserDependDetailsModel  : Serializable {

    @SerializedName("userProfile")
    var userProfile: User? = null

    @SerializedName("dependencies")
    var dependencies: Dependencies? = null
}