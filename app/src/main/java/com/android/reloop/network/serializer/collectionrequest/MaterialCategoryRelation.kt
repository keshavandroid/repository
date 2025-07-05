package com.reloop.reloop.network.serializer.collectionrequest

import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.subscription.SubscriptionDetail
import java.io.Serializable

class MaterialCategoryRelation: Serializable {

    @SerializedName("RecyclingFamilies")
    var recyclingFamiliesList: ArrayList<RecyclingFamilies>? = ArrayList()

    @SerializedName("MaterialCategories")
    var materialCategoriesList: ArrayList<MaterialCategories>? = ArrayList()

}