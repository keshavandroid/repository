package com.reloop.reloop.network.serializer

import com.reloop.reloop.network.serializer.orderhistory.City
import com.reloop.reloop.network.serializer.orderhistory.District
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories

class Payment {
    var payment_intend_id: String? = ""
    var status: String? = ""

}