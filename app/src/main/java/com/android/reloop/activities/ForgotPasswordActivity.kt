package com.reloop.reloop.activities

import android.os.Bundle
import android.os.Handler
import android.view.View
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.activity_forgot_password.*
import retrofit2.Call
import retrofit2.Response

class ForgotPasswordActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        MainApplication.hideActionBar(supportActionBar)
        setListeners()
        populateData()
    }

    private fun setListeners() {
        request.setOnClickListener(this)
        back.setOnClickListener(this)
    }

    private fun populateData() {

    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.request -> {
                if (email?.text.toString().isEmpty()) {
                    Notify.alerterRed(this, "Enter Email")
                } else {
                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.FORGOT_PASSWORD)
                        ?.autoLoading(this)
                        ?.enque(
                            Network().apis()?.forgot(
                                email?.text.toString()
                            )
                        )
                        ?.execute()
                }
            }
            R.id.back -> {
                onBackPressed()
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.FORGOT_PASSWORD -> {
                val baseResponse = Utils.getBaseResponse(response)

                Notify.alerterGreen(
                    this,
                    baseResponse?.message
                )
                val handler = Handler()
                handler.postDelayed(
                    {
                        finish()
                    }
                    , 1500
                )
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        when (tag) {
            RequestCodes.API.FORGOT_PASSWORD -> {
                Notify.alerterRed(
                    this,
                    "${response?.message}"
                )
            }
        }
    }
}
