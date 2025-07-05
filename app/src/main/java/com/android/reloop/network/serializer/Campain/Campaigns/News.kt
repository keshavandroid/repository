package com.android.reloop.network.serializer.Campain.Campaigns

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class News {

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

    @SerializedName("script")
    @Expose
    private var script: String? = null

    @SerializedName("status")
    @Expose
    private var status: Int? = null

    @SerializedName("created_at")
    @Expose
    private var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    private var updatedAt: String? = null

    @SerializedName("deleted_at")
    @Expose
    private var deletedAt: Any? = null

    @SerializedName("image")
    @Expose
    private var image: String? = null

    @SerializedName("news_images")
    @Expose
    private var newsImages: List<NewsImage?>? = null

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

    fun getStatus(): Int? {
        return status
    }

    fun setStatus(status: Int?) {
        this.status = status
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

    fun getCreatedAt(): String? {
        return createdAt
    }

    fun setCreatedAt(createdAt: String?) {
        this.createdAt = createdAt
    }

    fun getUpdatedAt(): String? {
        return updatedAt
    }

    fun setUpdatedAt(updatedAt: String?) {
        this.updatedAt = updatedAt
    }

    fun getDeletedAt(): Any? {
        return deletedAt
    }

    fun setDeletedAt(deletedAt: Any?) {
        this.deletedAt = deletedAt
    }

    fun getNewsImages(): List<NewsImage?>? {
        return newsImages
    }

    fun setNewsImages(newsImages: List<NewsImage?>?) {
        this.newsImages = newsImages
    }

}