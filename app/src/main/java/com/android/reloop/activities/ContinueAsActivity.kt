package com.android.reloop.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.reloop.network.ContinueAsSettings
import com.android.reloop.utils.Configuration
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.LoginActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Notify
import kotlinx.android.synthetic.main.fragment_continue_as.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.lang.reflect.Type

class ContinueAsActivity : BaseActivity(), OnNetworkResponse {
    private lateinit var dialog: Dialog
    private var organization_signup_info_value: String = ""
    private var household_signup_info_value: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_continue_as)
        MainApplication.hideActionBar(supportActionBar)
        clickListeners()
        continueSettingsAPI()
    }

    override fun onStop() {
        super.onStop()
//        recreate()
        buttonResetState(tvHouseholdBtn)
        buttonResetState(tvOrganizationsBtn)
    }

    private fun continueSettingsAPI() {
//        NetworkCall.make()
//            ?.setCallback(this@ContinueAsActivity)
//            ?.setTag(RequestCodes.API.CONTINUE_AS_SETTINGS)
//            ?.autoLoading(this@ContinueAsActivity)
//            ?.enque(
//                Network().apis()?.continueAsSettings()
//            )
//            ?.execute()
        showProgressDialog()
        var baseURL = ""
        baseURL = if (Configuration.isProduction) {
            "https://api.reloopapp.com/api/settings";
        } else if (Configuration.isStagingNew) {
            "https://staging.reloopapp.com/api/settings";
        } else {
            "http://reloop.teamtechverx.com/api/settings";
        }

        AndroidNetworking.get(baseURL)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    dismissProgressDialog()
                    try {
                        Log.d("==>>", "onResponse: $response")
                        val data: JSONObject = response.getJSONObject("data")
                        val jsonArray_signup_text: JSONArray = data.getJSONArray("signup_text")
                        val jsonArray_organization_signup_text: JSONArray =
                            data.getJSONArray("organization_signup_text")
                        val jsonArray_household_signup_info: JSONArray =
                            data.getJSONArray("household_signup_info")
                        val jsonArray_organization_signup_info: JSONArray =
                            data.getJSONArray("organization_signup_info")

                        val organization_signup_text_value =
                            jsonArray_organization_signup_text.getJSONObject(0).getString("value")
                        saveTextInSharedPref(organization_signup_text_value)
                        household_signup_info_value =
                            jsonArray_household_signup_info.getJSONObject(0).getString("value")
                        organization_signup_info_value =
                            jsonArray_organization_signup_info.getJSONObject(0).getString("value")

                        val signup_text_value =
                            jsonArray_signup_text.getJSONObject(0).getString("value")
                        Log.d("==>>", "signup_text_value: $signup_text_value")
                        tv_signup_text.text = signup_text_value


                    } catch (e: Exception) {
                        dismissProgressDialog()
                        Log.d("==>>", "onResponse: " + e.message)

                    }
                }

                override fun onError(error: ANError) {
                    dismissProgressDialog()
                    Log.d("==>>", "onError: " + error.message)
                }
            })
    }

    private fun saveTextInSharedPref(text: String) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(
            "continueASsharedPref",
            Context.MODE_PRIVATE
        )
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("organization_signup_text_value", text)
        editor.apply()
        editor.commit()
    }

    private fun showProgressDialog() {
        dialog = Dialog(this)
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.progress_dialog_layout)
            dialog.show()
        }
    }

    private fun dismissProgressDialog() {
        dialog.dismiss()
    }

    private fun clickListeners() {
        tvHouseholdBtn.setOnClickListener {
            showLoginActivity(false)
            buttonClickedState(tvHouseholdBtn)
        }
        tvOrganizationsBtn.setOnClickListener {
            showLoginActivity(true)
            buttonClickedState(tvOrganizationsBtn)
        }
        ib_info_household.setOnClickListener {
            AlertDialogs.showInfoPopup(
                this,
                tvHouseholdBtn.text.toString(),
                household_signup_info_value
            )
        }
        ib_info_organization.setOnClickListener {
            AlertDialogs.showInfoPopup(
                this,
                tvOrganizationsBtn.text.toString(),
                organization_signup_info_value
            )
        }
    }

    private fun buttonClickedState(textView: TextView) {
        try {
            textView.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.white
                )
            )
            textView.backgroundTintList =
                ContextCompat.getColorStateList(applicationContext, R.color.green_color_button)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buttonResetState(textView: TextView) {
        try {
            textView.setTextColor(
                ContextCompat.getColor(
                    applicationContext, R.color.green_color_button
                )
            )
            textView.backgroundTintList = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showLoginActivity(isOrganization: Boolean) {
        try {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("isOrganization", isOrganization)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        finish()
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        /*if (tag == RequestCodes.API.CONTINUE_AS_SETTINGS) {
            val baseResponse = Utils.getBaseResponse(response)
            Log.d("==>>", "onSuccess: $response")
            Log.d("==>>", "baseResponse onSuccess: $baseResponse")
            val gson = Gson()
            val listType: Type = object : TypeToken<List<ContinueAsSettings?>?>() {}.type
            val myType: Type = object : TypeToken<List<ContinueAsSettings>>() {}.type

            var dataList: ArrayList<ContinueAsSettings> = ArrayList()
            dataList = gson.fromJson(
                Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>),
                listType
            )
            val signupTextValue = dataList.get(0).data?.signupText
            Log.d("==>>", "signupTextValue: $signupTextValue")

        }*/
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(this, response?.message)
    }
}