package com.reloop.reloop.service

import android.app.Application
import android.content.Intent
import android.util.Log
import com.android.reloop.activities.SplashActivity
import com.reloop.reloop.app.MainApplication
import com.onesignal.OSNotificationAction
/*import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal.NotificationOpenedHandler*/

class NotificationOpenHandler(private val application: Application) /*:
    NotificationOpenedHandler */
{

    /*override fun notificationOpened(result: OSNotificationOpenResult) {
//        Debug.waitForDebugger()
        val data = result.notification.payload.additionalData
//        /*     Notification notification = new Gson().fromJson(data.toString(), Notification.class
        val actionType = result.action.type
        if (actionType == OSNotificationAction.ActionType.Opened) {
//            Log.e("OneSignalExample", "Button pressed with id: " + result.action.actionID);
            if (MainApplication.checkInBackground()) {
                startApp()
            }
            else
            {
                Log.e("Application", "Already in ForeGround" )
            }
        }
    }*/

    /*fun startApp() {
        val intent = Intent(application, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or  Intent.FLAG_ACTIVITY_CLEAR_TASK  or Intent.FLAG_ACTIVITY_NEW_TASK
        application.startActivity(intent)
    }*/
    */


}