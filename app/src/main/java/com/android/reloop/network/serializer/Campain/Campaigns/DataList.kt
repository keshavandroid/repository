package com.android.reloop.network.serializer.Campain.Campaigns

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class DataList {
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

    @SerializedName("campaign_images")
    @Expose
    private var campaignImages: List<CampaignImage?>? = null

    @SerializedName("news")
    @Expose
    private var news: List<News?>? = null

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

    fun getCampaignImages(): List<CampaignImage?>? {
        return campaignImages
    }

    fun setCampaignImages(campaignImages: List<CampaignImage?>?) {
        this.campaignImages = campaignImages
    }

    fun getNews(): List<News?>? {
        return news
    }

    fun setNews(news: List<News?>?) {
        this.news = news
    }
}