package com.reloop.reloop.network.serializer

import com.reloop.reloop.network.serializer.orderhistory.City
import com.reloop.reloop.network.serializer.orderhistory.District
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import java.io.Serializable

class FavDropOffPoints: Serializable {
    @SerializedName("id")
    var id: Int? = 0

    @SerializedName("user_id")
    var user_id: Int? = 0

    @SerializedName("drop_off_point_id")
    var drop_off_point_id: Int? = 0

    @SerializedName("created_at")
    var created_at: String? = ""

    @SerializedName("updated_at")
    var updated_at: String? = ""

    @SerializedName("drop_off_details")
    var drop_off_details: DropOffDetail? = DropOffDetail()

    var isFavouriteSelected = false

}