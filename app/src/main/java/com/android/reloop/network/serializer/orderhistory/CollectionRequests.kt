package com.reloop.reloop.network.serializer.orderhistory

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CollectionRequests : Serializable {
    @SerializedName("id")
    val id: Int? = 0

    @SerializedName("user_id")
    val user_id: Int? = 0

    @SerializedName("driver_id")
    val driver_id: Int? = 0

    @SerializedName("supervisor_id")
    val supervisor_id: Int? = 0

    @SerializedName("city_id")
    val city_id: Int? = 0

    @SerializedName("district_id")
    val district_id: Int? = 0

    @SerializedName("request_number")
    val request_number: String? = ""

    @SerializedName("confirm")
    val confirm: Int? = -1

    @SerializedName("collection_date")
    val collection_date: String? = ""

    @SerializedName("collection_type")
    val collection_type: Int? = 0

  /*  @SerializedName("reward_points")
    val reward_points: Double = null ?: 0.0*/

    @SerializedName("status")
    var status: Int? = 0

    @SerializedName("driver_trip_status")
    var driver_trip_status: Int? = 0

    @SerializedName("first_name")
    val first_name: String? = ""

    @SerializedName("last_name")
    val last_name: String? = ""

    @SerializedName("organization_name")
    val organization_name: String? = ""

    @SerializedName("phone_number")
    val phone_number: String? = ""

    @SerializedName("location")
    val location: String? = ""

    @SerializedName("latitude")
    val latitude: Double? = 0.0

    @SerializedName("longitude")
    val longitude: Double? = 0.0

    @SerializedName("street")
    val street: String? = ""

    //TODO Add Question Answer Array Also Additional Comments
    @SerializedName("created_at")
    val created_at: String? = ""

    @SerializedName("request_collection")
    var request_collection: ArrayList<CollectionRequestProduct>? = ArrayList()

    @SerializedName("map_location")
    val map_location: String? = ""

    @SerializedName("city")
    val city: City? = City()

    @SerializedName("district")
    val district: District? = District()
}