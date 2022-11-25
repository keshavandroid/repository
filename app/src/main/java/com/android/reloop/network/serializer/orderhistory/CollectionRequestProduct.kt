package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CollectionRequestProduct:Serializable {
    @SerializedName("id")
    val id: Int?=0
    @SerializedName("request_id")
    val request_id: Int?=0
    @SerializedName("category_name")
    val category_name: String?=""
    @SerializedName("weight")
    val weight: String?=""
    @SerializedName("created_at")
    val created_at: String?=""
    @SerializedName("updated_at")
    val updated_at: String?=""
    @SerializedName("material_category")
    val materialCategory:MaterialCategory?= MaterialCategory()

}