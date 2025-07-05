package com.android.reloop.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.network.serializer.orderhistory.OrderItems
import java.io.Serializable

class SavedCard: Serializable {


    @SerializedName("data")
    var cardData: ArrayList<SingleCard>? = ArrayList()



}