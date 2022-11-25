package com.reloop.reloop.network.serializer.couponverification

import com.google.gson.annotations.SerializedName

class ValidForCategory {
    @SerializedName("type")
    var type :ArrayList<Int>?=ArrayList()
    @SerializedName("id")
    var id :ArrayList<Int>?=ArrayList()
}