package com.android.reloop.network.serializer.Campain.CampaignDetails

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class NewsDetailImage {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("news_id")
    @Expose
    private var newsId: Int? = null

    @SerializedName("image")
    @Expose
    private var image: String? = null

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

}