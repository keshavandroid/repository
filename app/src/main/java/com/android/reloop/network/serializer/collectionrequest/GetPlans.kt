package com.reloop.reloop.network.serializer.collectionrequest

import com.reloop.reloop.network.serializer.shop.OneTimeServices
import com.google.gson.annotations.SerializedName

class GetPlans  {
    @SerializedName("UserPlans")
    var userPlans:ArrayList<UserPlans>?=ArrayList()
    @SerializedName("OneTimeServices")
    var oneTimeServices:ArrayList<OneTimeServices>?= ArrayList()


}