package com.android.reloop.network.serializer.Campain.CampaignDetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Data {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("title")
    @Expose
    private var title: String? = null

    @SerializedName("description")
    @Expose
    private var description: String? = null

    @SerializedName("image")
    @Expose
    private var image: String? = null

    @SerializedName("status")
    @Expose
    private var status: Int? = null

    @SerializedName("is_enable_barcode")
    @Expose
    private var isEnableBarcode: Int? = null

    @SerializedName("is_enable_news")
    @Expose
    private var isEnableNews: Int? = null

    @SerializedName("is_enable_join")
    @Expose
    private var isEnableJoin: Int? = null

    @SerializedName("campaign_for")
    @Expose
    private var campaignFor: String? = null

    @SerializedName("has_joined")
    @Expose
    private var hasJoined: Boolean? = null

    @SerializedName("campaign_images")
    @Expose
    private var campaignImages: List<CampaignDetailImage?>? = null

    @SerializedName("news")
    @Expose
    private var news: List<NewsDetails?>? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getDescription(): String? {
        return description
    }

    fun setDescription(description: String?) {
        this.description = description
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

    fun getStatus(): Int? {
        return status
    }

    fun setStatus(status: Int?) {
        this.status = status
    }

    fun getIsEnableBarcode(): Int? {
        return isEnableBarcode
    }

    fun setIsEnableBarcode(isEnableBarcode: Int?) {
        this.isEnableBarcode = isEnableBarcode
    }

    fun getIsEnableNews(): Int? {
        return isEnableNews
    }

    fun setIsEnableNews(isEnableNews: Int?) {
        this.isEnableNews = isEnableNews
    }

    fun getIsEnableJoin(): Int? {
        return isEnableJoin
    }

    fun setIsEnableJoin(isEnableJoin: Int?) {
        this.isEnableJoin = isEnableJoin
    }

    fun getCampaignFor(): String? {
        return campaignFor
    }

    fun setCampaignFor(campaignFor: String?) {
        this.campaignFor = campaignFor
    }

    fun gethasJoined(): Boolean? {
        return hasJoined
    }

    fun sethasJoined(hasJoined: Boolean?) {
        this.hasJoined = hasJoined
    }

    fun getCampaignImages(): List<CampaignDetailImage?>? {
        return campaignImages
    }

    fun setCampaignImages(campaignImages: List<CampaignDetailImage?>?) {
        this.campaignImages = campaignImages
    }

    fun getNews(): List<NewsDetails?>? {
        return news
    }

    fun setNews(news: List<NewsDetails?>?) {
        this.news = news
    }

}