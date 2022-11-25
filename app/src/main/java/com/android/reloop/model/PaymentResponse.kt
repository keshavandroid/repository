package com.android.reloop.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PaymentResponse {
    @SerializedName("response_code")
    @Expose
    private var responseCode: String? = null

    @SerializedName("response_message")
    @Expose
    private var responseMessage: String? = null
    @SerializedName("merchant_extra")
    @Expose
    private var merchentExtra: String? = null

    fun getResponseCode(): String? {
        return responseCode
    }

    fun setResponseCode(responseCode: String?) {
        this.responseCode = responseCode
    }

    fun getResponseMessage(): String? {
        return responseMessage
    }

    fun setResponseMessage(responseMessage: String?) {
        this.responseMessage = responseMessage
    }

    fun getMerchentExtra(): String? {
        return merchentExtra
    }

    fun setMerchentExtra(merchentExtra: String?) {
        this.merchentExtra = merchentExtra
    }


}