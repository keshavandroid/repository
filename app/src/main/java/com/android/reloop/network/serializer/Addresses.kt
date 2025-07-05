package com.reloop.reloop.network.serializer

import com.android.reloop.model.AddressLocations
import com.reloop.reloop.network.serializer.orderhistory.City
import com.reloop.reloop.network.serializer.orderhistory.District
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories

class Addresses {
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

    //new added
    @SerializedName("title")
    var title: String? = ""

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
    @SerializedName("address_name")
    var address_name: String? = ""

    @SerializedName("city")
    var city: City? = City()
    @SerializedName("district")
    var district: District? = District()

    @SerializedName("address_locations")
    var addressLocations: ArrayList<AddressLocations>? = ArrayList()

    /*@SerializedName("categories")
    var materialCategories: ArrayList<MaterialCategories>? = ArrayList()*/

}