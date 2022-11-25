package com.reloop.reloop.app

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.reloop.utils.LogFileSyncTask
import com.android.reloop.utils.LogManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.service.NotificationOpenHandler
import com.reloop.reloop.service.NotificationReceivedHandler
import com.onesignal.OneSignal
import com.reloop.reloop.BuildConfig


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
        LogManager.getLogManager().writeLog("${MainApplication} :VERSION NAME  : ${BuildConfig.VERSION_NAME}")
        LogManager.getLogManager().writeLog("${MainApplication} :VERSION CODE  : ${BuildConfig.VERSION_CODE}")
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

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        inBackground = true
        if(LogManager.isInitialized())
        {
            LogManager.getLogManager().sendLogs(true)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        inBackground = false
    }
}