package com.reloop.reloop.app

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.reloop.utils.LogManager
import com.facebook.FacebookSdk
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.service.NotificationOpenHandler
import com.reloop.reloop.service.NotificationReceivedHandler
import com.onesignal.OneSignal


open class MainApplication : Application(), LifecycleObserver {


    private val ONESIGNAL_APP_ID: String = "c123a3b5-f1b0-4df7-a09e-10dd90636819"

    companion object {
        var activity: Activity? = Activity()
        var inBackground = true
        private var instance: MainApplication? = MainApplication()
        fun applicationContext(): Context {
            if (instance == null) {
                instance = MainApplication()
            }
            return instance!!.applicationContext
        }

        fun userType(): Int {
            return User.retrieveUser()?.user_type!!
        }

        fun getCurrentActivity(): Activity? {
            if (activity == null) {
                activity = Activity()
            }
            return activity
        }

        fun setCurrentActivity(activity: Activity) {
            this.activity = activity
        }

        fun hideActionBar(actionBar: ActionBar?) {
            actionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar?.hide()
        }

        fun checkInBackground(): Boolean {
            return inBackground
        }
    }


    override fun onCreate() {
        super.onCreate()
        instance = this

        LogManager.getLogManager().writeLog("${MainApplication} :APP LAUNCHED")
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.DEBUG, OneSignal.LOG_LEVEL.DEBUG)
        //OneSignal
        /*OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .setNotificationOpenedHandler(NotificationOpenHandler(this))
            .setNotificationReceivedHandler(NotificationReceivedHandler())
            .init()*/
        OneSignal.initWithContext(this)
        OneSignal.setNotificationWillShowInForegroundHandler { notificationReceivedEvent ->
            notificationReceivedEvent.complete(null)
        }
        OneSignal.setAppId(ONESIGNAL_APP_ID);

        FirebaseApp.initializeApp(applicationContext)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        FacebookSdk.setClientToken("6324c264c381f3398197395fb41ac410");
        FacebookSdk.sdkInitialize(getApplicationContext());
//        FacebookSdk.setGraphApiVersion("v14.0"); // Specify the desired version here

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        inBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        inBackground = false
    }


}