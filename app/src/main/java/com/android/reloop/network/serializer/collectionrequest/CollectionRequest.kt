package com.reloop.reloop.network.serializer.collectionrequest

import android.net.Uri

class CollectionRequest {
    var material_categories: ArrayList<MaterialCategoryID?>? = ArrayList()
    var collection_date: String? = null
    var collection_type: Int? = 0
    var first_name: String? = null
    var last_name: String? = null
    var location: String? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var phone_number: String? = null
    var city_id: Int? = null
    var district_id: Int? = null
    var street: String? = null
    var map_location = ""
    var address_title = ""

    //    var questions: ArrayList<Questionnaire?>? = ArrayList()
//    var card_number: String? = null
//    var exp_month: String? = null
//    var exp_year: String? = null
//    var cvv: Int? = null
//    var total: Int? = null
    var organization_name: String? = null
    var user_comments: String = ""

    //new added
    var imageUri: Uri?=null

    var imageUris: ArrayList<Uri>? = ArrayList()


}