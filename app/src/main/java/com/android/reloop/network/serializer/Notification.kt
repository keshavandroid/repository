package com.reloop.reloop.network.serializer

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Notification : Serializable {
    @SerializedName("title")
    @Expose
    var title: String? = null
    @SerializedName("notificationId")
    @Expose
    var notificationId: Int? = null
    @SerializedName("message")
    @Expose
    var message: String? = null
    @SerializedName("read")
    @Expose
    var read: Boolean? = null
    @SerializedName("eventType")
    @Expose
    var eventType: Int? = null
    @SerializedName("notificationType")
    @Expose
    var notificationType: Int? = null
    @SerializedName("eventId")
    @Expose
    var eventId: Int? = null
    @SerializedName("created")
    @Expose
    var created: String? = null
    @SerializedName("senderCount")
    @Expose
    var senderCount: Int? = null
    @SerializedName("android_badgeCount")
    @Expose
    var badgeCount = 0

}