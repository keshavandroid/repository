package com.reloop.reloop.service

import com.reloop.reloop.network.serializer.Notification
import com.google.gson.Gson
import com.onesignal.OSNotification
import com.onesignal.OSNotificationOpenedResult
import com.onesignal.OneSignal

class NotificationReceivedHandler : OneSignal.OSNotificationOpenedHandler {
/*
    override fun notificationReceived(notification: OSNotification) {
        val data = notification.payload.additionalData
        val notpayload =
            Gson().fromJson(
                data.toString(),
                Notification::class.java
            )
        */
/*if(notpayload.getNotificationType() == NotificationType.RequestAction){
            PushReceived pushReceived = new PushReceived();
            pushReceived.setType(NotificationType.RequestAction);
            EventBus.getDefault().post(pushReceived);
        }else if (notpayload.getNotificationType() == NotificationType.RequestToAdd){
            PushReceived pushReceived = new PushReceived();
            pushReceived.setType(NotificationType.RequestToAdd);
            EventBus.getDefault().post(pushReceived);
        }
        else
            EventBus.getDefault().post(new PushReceived());
        ShortcutBadger.applyCount(MainApplication.getAppContext(), notpayload.getBadgeCount());*//*

    }
*/

    override fun notificationOpened(p0: OSNotificationOpenedResult?) {
        val data = p0?.notification?.additionalData

        val notpayload =
            Gson().fromJson(
                data.toString(),
                Notification::class.java
            )

    }
}