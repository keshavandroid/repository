package com.reloop.reloop.network


import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.Window
import com.reloop.reloop.R
import com.reloop.reloop.app.BaseClass
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.serializer.BaseResponse
import com.android.reloop.utils.Configuration
import com.reloop.reloop.utils.Utils
import com.google.gson.stream.MalformedJsonException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException


object NetworkCall : BaseClass() {

    //    var viewDialog: ViewDialog? = null
    private var taggedObject: Any? = null
    private var callback: OnNetworkResponse? = null
    private var request: Call<Any>? = null
    private var progressDialog: Dialog? = null
    var isRunning = false
    var totalRequestCount = 1
    var progressDialogShown = 0

    @Synchronized
    fun make(): NetworkCall? {
        isRunning = true
        return NetworkCall
    }

    fun setCallback(callback: OnNetworkResponse?): NetworkCall? {
        this.callback = callback
        return this
    }

    fun autoLoading(activity: Activity?): NetworkCall? {
        if(activity == null) return this
        progressDialog = Dialog(activity)
        if (progressDialog?.window != null) {
            progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialog?.setCancelable(false)
            progressDialog?.setContentView(R.layout.progress_dialog_layout)
            progressDialogShown = progressDialogShown.plus(1)
            if (progressDialogShown == totalRequestCount)
                progressDialog?.show()
        }
        return this
    }

    fun inProgress(): Boolean {
        return isRunning
    }

    fun setTag(tag: Any?): NetworkCall? {
        taggedObject = tag
        return this
    }

    fun enque(call: Call<Any>?): NetworkCall? {
        request = call
        return this
    }
    fun executeCurrentThread(): Response<Any> {
        return request!!.execute()
    }
    fun execute(): NetworkCall? {
        request!!.enqueue(object : Callback<Any?> {
            override fun onResponse(call: Call<Any?>?, response: Response<Any?>) {
                progressDialog?.dismiss()
                progressDialogShown = 0
                isRunning = false;

                try{
                if (BaseResponse.isSuccess(response)) {
                    callback!!.onSuccess(call, response, taggedObject)
                }
                else if (BaseResponse.isForbiddenResponse(response)) {
                    callback!!.onFailure(
                        call,
                        BaseResponse(
                            "User is Not Authorized, Please Login Again ",
                            response.raw().code.toString().toInt()
                        ),
                        taggedObject
                    )
                    val update = Handler()
                    update.postDelayed(
                        {
                            Utils.logOut(MainApplication.getCurrentActivity())
                        }
                        , 2500
                    )
                }
                else if (response.body() == null || !BaseResponse.isSuccess(response)) {
                    val jsonObject: JSONObject?
                    var message = ""
                    try {
                        jsonObject = JSONObject(response.errorBody()!!.string())
                        message = jsonObject.getString("message")
                        val status = jsonObject.getBoolean("status")
                        val errorCode = jsonObject.getInt("code")
                        if (!jsonObject.isNull("data") && jsonObject.getJSONObject("data")
                                .length() > 0
                        ) {

                            val dataObject = jsonObject.getJSONObject("data")
                            val dataKey = dataObject.keys().next()
                            if (dataKey != null) {
                                message = dataObject.getJSONArray(dataKey)[0].toString()
                            }
                        }
                        callback!!.onFailure(call, BaseResponse(message, errorCode, status), taggedObject)
                    } catch (e: JSONException) {
                        if (BaseResponse.isUnAuthorized(response)) {
                            if (message.isNullOrEmpty()) {
                                message = response.raw().message.toString()
                            }
                            callback!!.onFailure(call, BaseResponse(message, response.raw().code.toString().toInt()), taggedObject)
                        }
                    }
                }
                }
                catch (e :NullPointerException)
                {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<Any?>, t: Throwable) {
                progressDialog?.dismiss()
                progressDialogShown = 0
                isRunning = false
                val response: BaseResponse =
                    if (Utils.isCause(SocketTimeoutException::class.java, t)) {
                        BaseResponse(string(R.string.timeout))
                    } else if (Utils.isCause(ConnectException::class.java, t)) {
                        BaseResponse(string(R.string.connectException))
                    } else if (Utils.isCause(
                            MalformedJsonException::class.java,
                            t
                        )
                    ) {
                        BaseResponse(string(R.string.invalid_data))
                    } else if (t is SSLHandshakeException || t is SSLException) {
                        BaseResponse(string(R.string.ssl))
                    } else if (t is IOException) {
                        BaseResponse(string(R.string.no_internet))
                    } else {
                        BaseResponse(if (Configuration.isDevelopment) t.message else string(R.string.text_somethingwentwrong))
                        //  response = new BaseResponse(t.getMessage());
                    }
                callback!!.onFailure(call, response, taggedObject)
            }

        })

        return this
    }

    fun executeLocation(): NetworkCall? {
        request!!.enqueue(object : Callback<Any?> {
            override fun onResponse(call: Call<Any?>?, response: Response<Any?>) {
                progressDialog?.dismiss()
                progressDialogShown = 0
                isRunning = false;

                try{
                    callback!!.onSuccess(call, response, taggedObject)
                }
                catch (e :NullPointerException)
                {
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<Any?>, t: Throwable) {
                progressDialog?.dismiss()
                progressDialogShown = 0
                isRunning = false
                val response =
                    if (Utils.isCause(SocketTimeoutException::class.java, t)) {
                        BaseResponse(string(R.string.timeout))
                    } else if (Utils.isCause(ConnectException::class.java, t)) {
                        BaseResponse(string(R.string.connectException))
                    } else if (Utils.isCause(
                            MalformedJsonException::class.java,
                            t
                        )
                    ) {
                        BaseResponse(string(R.string.invalid_data))
                    } else if (t is SSLHandshakeException || t is SSLException) {
                        BaseResponse(string(R.string.ssl))
                    } else if (t is IOException) {
                        BaseResponse(string(R.string.no_internet))
                    } else {
                        BaseResponse(if (Configuration.isDevelopment) t.message else string(R.string.text_somethingwentwrong))
                        //  response = new BaseResponse(t.getMessage());
                    }
                callback!!.onFailure(call, response, taggedObject)
            }

        })

        return this
    }


    //    Client ID
//    290412357838-iq44c1buq1k8ge3g9jm2qqh0hd3fekks.apps.googleusercontent.com
//    Client Secret
//    R5mGYtz4NHrlSETVvB4U21pR
}