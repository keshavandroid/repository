package com.android.reloop.network.serializer.Campain.CampaignDetails

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class CampaignDetailImage {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("campaign_id")
    @Expose
    private var campaignId: Int? = null

    @SerializedName("image")
    @Expose
    private var image: String? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getCampaignId(): Int? {
        return campaignId
    }

    fun setCampaignId(campaignId: Int?) {
        this.campaignId = campaignId
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

}