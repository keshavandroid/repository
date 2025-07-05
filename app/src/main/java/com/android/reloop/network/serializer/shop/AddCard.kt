package com.reloop.reloop.network.serializer.shop

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AddCard {
    @SerializedName("card_number")
    @Expose
    var card_number: String? = ""
    @SerializedName("exp_month")
    @Expose
    var exp_month: String? = ""
    @SerializedName("exp_year")
    @Expose
    var exp_year: String? = ""
    @SerializedName("card_security_code")
    @Expose
    var cvv: String? = ""
    @SerializedName("card_holder")
    @Expose
    var card_holder: String? = ""



}