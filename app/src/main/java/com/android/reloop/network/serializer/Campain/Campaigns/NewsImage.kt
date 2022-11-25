package com.android.reloop.network.serializer.Campain.Campaigns

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class NewsImage {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("news_id")
    @Expose
    private var newsId: Int? = null

    @SerializedName("image")
    @Expose
    private var image: String? = null

    @SerializedName("created_at")
    @Expose
    private var createdAt: Any? = null

    @SerializedName("updated_at")
    @Expose
    private var updatedAt: Any? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getNewsId(): Int? {
        return newsId
    }

    fun setNewsId(newsId: Int?) {
        this.newsId = newsId
    }

    fun getImage(): String? {
        return image
    }

    fun setImage(image: String?) {
        this.image = image
    }

    fun getCreatedAt(): Any? {
        return createdAt
    }

    fun setCreatedAt(createdAt: Any?) {
        this.createdAt = createdAt
    }

    fun getUpdatedAt(): Any? {
        return updatedAt
    }

    fun setUpdatedAt(updatedAt: Any?) {
        this.updatedAt = updatedAt
    }
}