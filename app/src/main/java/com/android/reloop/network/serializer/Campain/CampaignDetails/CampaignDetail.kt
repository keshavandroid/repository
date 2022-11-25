package com.android.reloop.network.serializer.Campain.CampaignDetails

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class CampaignDetail {

    @SerializedName("message")
    @Expose
    private var message: String? = null

    @SerializedName("code")
    @Expose
    private var code: Int? = null

    @SerializedName("status")
    @Expose
    private var status: Boolean? = null

    @SerializedName("data")
    @Expose
    private var data: Data? = null

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun getCode(): Int? {
        return code
    }

    fun setCode(code: Int?) {
        this.code = code
    }

    fun getStatus(): Boolean? {
        return status
    }

    fun setStatus(status: Boolean?) {
        this.status = status
    }

    fun getData(): Data? {
        return data
    }

    fun setData(data: Data?) {
        this.data = data
    }
}