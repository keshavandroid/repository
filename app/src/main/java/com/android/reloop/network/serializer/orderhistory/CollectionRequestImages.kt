package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.user.User
import java.io.Serializable

class CollectionRequestImages:Serializable {
    @SerializedName("id")
    val id: Int?=0
    @SerializedName("request_id")
    val request_id: Int?=0
    @SerializedName("image")
    val image: String?=""
    @SerializedName("created_at")
    val created_at: String?=""
    @SerializedName("updated_at")
    val updated_at: String?=""

    //new added after image shown in request detail screen
    @SerializedName("user")
    val user: User? = User()


}