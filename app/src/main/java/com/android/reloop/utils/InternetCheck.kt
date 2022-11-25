package com.reloop.reloop.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.reloop.reloop.R

class InternetCheck {
    var activity: Activity? = null
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

    fun alertDialog(activity1: Activity?) {
        activity = activity1
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.message_dialog)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        val checkAgain = dialog.findViewById<Button>(R.id.yes)
        checkAgain.text = "Check Again"
        checkAgain.setOnClickListener { view: View? -> dialog.dismiss() }
        dialog.show()
    }

    companion object {
        private var instance: InternetCheck? = null
        @Synchronized
        fun getInstance(): InternetCheck? {
            if (instance == null) instance =
                InternetCheck()
            return instance
        }
    }
}