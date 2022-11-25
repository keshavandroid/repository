package com.reloop.reloop.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.activity_reset_password.*
import retrofit2.Call
import retrofit2.Response


class ResetPasswordActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {

    var token = ""
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasExtra("reset_token")) {
            Notify.Toast("Has Token")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        MainApplication.hideActionBar(supportActionBar)
        val appLinkAction = intent.action
        val appLinkData = intent.data
        if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
            val args =
                appLinkData.queryParameterNames
            getDetailFromDeepLink(args, appLinkData)
        }


        val userModel = User.retrieveUser()
        if (!userModel?.api_token.isNullOrEmpty()) {
            showHomeScreen()
            finish()
        }
        setListeners()

    }

    private fun getDetailFromDeepLink(
        args: Set<String>,
        appLinkData: Uri
    ) {
        if (args.contains("token") || args.contains("token")) {
            token = appLinkData.getQueryParameter("token").toString()
        } else {
            Log.e("Reset Password", "Cannot Find Reset Token")
        }

    }

    private fun setListeners() {
        change_password?.setOnClickListener(this)
        back?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.change_password -> {
                when {
                    new_password?.text.toString().isEmpty() -> {
                        Notify.alerterRed(this, getString(R.string.enter_new_password))
                    }
                    confirm_new_password?.text.toString().isEmpty() -> {
                        Notify.alerterRed(this, getString(R.string.enter_confirm_new_password))
                    }
                    new_password?.text.toString() != confirm_new_password?.text.toString() -> {
                        Notify.alerterRed(this, getString(R.string.password_match_err_msg))
                    }
                    else -> {
                        if (token.isEmpty()) {
                            Notify.alerterRed(this, "Cannot Reset Password")
                        } else {
                            NetworkCall.make()
                                ?.setCallback(this)
                                ?.setTag(RequestCodes.API.RESET_PASSWORD)
                                ?.autoLoading(this)
                                ?.enque(
                                    Network().apis()?.resetPassword(
                                        token,
                                        new_password?.text.toString(),
                                        confirm_new_password?.text.toString()
                                    )
                                )
                                ?.execute()
                            change_password?.isClickable = false
                        }
                    }
                }
            }
            R.id.back -> {
                finish()
            }
        }
    }

    private fun showHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    /*    private fun handleIntent(intent: Intent) {
            val appLinkAction = intent.action
            val appLinkData = intent.data

                if (Intent.ACTION_VIEW == appLinkAction && appLinkData != null) {
                    val args = appLinkData.queryParameterNames
                    getDetailFromDeepLink(args, appLinkData)
                } else {
                    val saveUri: AppLinkData = AppLinkData.init()
                    if (saveUri.appLinkData.isEmpty()) {
                        Log.e("Landing Screen", "appLinkData Data Is Null")
                    } else {
                        val savedInModel = Uri.parse(saveUri.appLinkData)
                        Log.e("Uri", "appLinkData=$savedInModel")
                        val args =
                            savedInModel.queryParameterNames
                        getDetailFromDeepLink(args, savedInModel)
                    }
                }
        }*/
    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.RESET_PASSWORD -> {
                val baseResponse = Utils.getBaseResponse(response)

                Notify.alerterGreen(
                    this,
                    baseResponse?.message
                )
                val handler = Handler()
                handler.postDelayed(
                    {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    , 1500
                )
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        change_password?.isClickable = true
        Notify.alerterRed(this, response?.message)
    }
}
