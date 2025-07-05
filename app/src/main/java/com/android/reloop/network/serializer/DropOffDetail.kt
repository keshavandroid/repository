package com.reloop.reloop.network.serializer

import com.reloop.reloop.network.serializer.orderhistory.City
import com.reloop.reloop.network.serializer.orderhistory.District
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import java.io.Serializable

class DropOffDetail: Serializable {
    @SerializedName("id")
    var id: Int? = 0
    @SerializedName("user_id")
    var user_id: Int? = 0
    @SerializedName("city_id")
    var city_id: Int? = 0
    @SerializedName("district_id")
    var district_id: Int? = 0
    @SerializedName("location")
    var location: String? = ""

    @SerializedName("address")
    var address: String? = ""

    @SerializedName("latitude")
    var latitude: Double? = 0.0
    @SerializedName("longitude")
    var longitude: Double? = 0.0
    @SerializedName("type")
    var type: String? = ""
    @SerializedName("no_of_bedrooms")
    var no_of_bedrooms: Int? = 0
    @SerializedName("no_of_occupants")
    var no_of_occupants: Int? = 0
    @SerializedName("street")
    var street: String? = ""
    @SerializedName("floor")
    var floor: String? = ""
    @SerializedName("unit_number")
    var unit_number: String? = ""
    @SerializedName("default")
    var default: Int? = 0
    @SerializedName("created_at")
    var created_at: String? = ""
    @SerializedName("updated_at")
    var updated_at: String? = ""
    @SerializedName("building_name")
    var building_name: String? = ""


    @SerializedName("require_photo")
    var require_photo: Int? = 0
    @SerializedName("require_location")
    var require_location: Int? = 0
    @SerializedName("require_barcode")
    var require_barcode: Int? = 0

    @SerializedName("photo")
    var photo: String? = ""

    @SerializedName("description")
    var description: String? = ""

    @SerializedName("title")
    var title: String? = ""

    @SerializedName("is_favourite")
    var is_favourite: Boolean = false

    @SerializedName("allowed_categories")
    var allowed_categories: String? = ""

    @SerializedName("city")
    var city: City? = City()
    @SerializedName("district")
    var district: District? = District()

    @SerializedName("categories")
    var materialCategories: ArrayList<MaterialCategories>? = ArrayList()



}