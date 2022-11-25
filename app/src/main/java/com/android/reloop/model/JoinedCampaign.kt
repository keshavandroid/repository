package com.android.reloop.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class JoinedCampaign : Serializable {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("user_id")
    @Expose
    private var userId: Int? = null

    @SerializedName("campaign_id")
    @Expose
    private var campaignId: Int? = null

    @SerializedName("exit_date")
    @Expose
    internal var exitDate: Any? = null

    @SerializedName("created_at")
    @Expose
    internal var createdAt: String? = null

    @SerializedName("campaign")
    @Expose
    private var campaign: Campaign? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getUserId(): Int? {
        return userId
    }

    fun setUserId(userId: Int?) {
        this.userId = userId
    }

    fun getCampaignId(): Int? {
        return campaignId
    }

    fun setCampaignId(campaignId: Int?) {
        this.campaignId = campaignId
    }

    fun getExitDate(): Any? {
        return exitDate
    }

    fun setExitDate(exitDate: Any?) {
        this.exitDate = exitDate
    }

    fun getCreatedAt(): String? {
        return createdAt
    }

    fun setCreatedAt(createdAt: String?) {
        this.createdAt = createdAt
    }

    fun getCampaign(): Campaign? {
        return campaign
    }

    fun setCampaign(campaign: Campaign?) {
        this.campaign = campaign
    }
    class Campaign {
        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("title")
        @Expose
        var title: String? = null

        @SerializedName("description")
        @Expose
        var description: String? = null

        @SerializedName("image")
        @Expose
        var image: String? = null

        @SerializedName("status")
        @Expose
        var status: Int? = null

        @SerializedName("is_enable_barcode")
        @Expose
        var isEnableBarcode: Int? = null

        @SerializedName("is_enable_news")
        @Expose
        var isEnableNews: Int? = null

        @SerializedName("is_enable_join")
        @Expose
        var isEnableJoin: Int? = null

        @SerializedName("campaign_for")
        @Expose
        var campaignFor: String? = null

        @SerializedName("price")
        @Expose
        var price: Any? = null
    }
}