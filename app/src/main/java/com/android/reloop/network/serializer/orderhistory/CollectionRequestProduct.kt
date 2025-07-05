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

    //NEW added for Edit Collection request
    @SerializedName("material_category_id")
    val materialCategoryId: Int?=0

    @SerializedName("user_stats")
    var user_stats: ArrayList<UserStats>? = ArrayList()

    @SerializedName("collection_bins")
    var collection_bins: ArrayList<CollectionBinsList>? = ArrayList()

}

class CollectionBin {
    @SerializedName("id")
    val id: Int?=0
    @SerializedName("request_collection_id")
    val request_collection_id: Int?=0
}
