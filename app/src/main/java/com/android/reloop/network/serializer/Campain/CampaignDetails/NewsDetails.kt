package com.android.reloop.network.serializer.Campain.CampaignDetails

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class NewsDetails {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("campaign_id")
    @Expose
    private var campaignId: Int? = null

    @SerializedName("title")
    @Expose
    private var title: String? = null

    @SerializedName("date")
    @Expose
    private var date: String? = null

    @SerializedName("show_date")
    @Expose
    private var showdate: Int? = null

    @SerializedName("script")
    @Expose
    private var script: String? = null

    @SerializedName("image")
    @Expose
    private var image: String? = null

    @SerializedName("status")
    @Expose
    private var status: Int? = null

    @SerializedName("news_images")
    @Expose
    private var newsImages: List<NewsDetailImage?>? = null

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

    fun getTitle(): String? {
        return title
    }

    fun setTitle(title: String?) {
        this.title = title
    }

    fun getDate(): String? {
        return date
    }

    fun setDate(date: String?) {
        this.date = date
    }

    fun getScript(): String? {
        return script
    }

    fun setScript(script: String?) {
        this.script = script
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

    fun getShowdate(): Int? {
        return showdate
    }

    fun setShowdate(showdate: Int?) {
        this.showdate = showdate
    }

    fun getStatus(): Int? {
        return status
    }

    fun setStatus(status: Int?) {
        this.status = status
    }

    fun getNewsImages(): List<NewsDetailImage?>? {
        return newsImages
    }

    fun setNewsImages(newsImages: List<NewsDetailImage?>?) {
        this.newsImages = newsImages
    }

}