package com.reloop.reloop.fragments


import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.reloop.reloop.R
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.settings.AboutAppData
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import kotlinx.android.synthetic.main.fragment_about_reloop.view.*
import retrofit2.Call
import retrofit2.Response


/**
 * A simple [Fragment] subclass.
 */
class AboutReloopFragment : BaseFragment(), OnNetworkResponse, View.OnClickListener {

    companion object {
        fun newInstance(): AboutReloopFragment {
            return AboutReloopFragment()
        }
    }

    var view_: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view_ = inflater.inflate(R.layout.fragment_about_reloop, container, false)
        populateData()
        setListeners()
        return view_
    }

    private fun setListeners() {
        view_?.back?.setOnClickListener(this)
    }

    private fun populateData() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.ABOUT_US)
            ?.autoLoading(activity!!)
            ?.enque(
                Network().apis()?.aboutUs()
            )
            ?.execute()
    }


    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.ABOUT_US -> {
                val baseResponse = Utils.getBaseResponse(response)

                if (baseResponse?.data != null) {
                    val aboutApp = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse.data as? LinkedTreeMap<*, *>),
                        AboutAppData::class.java
                    )
                    if (aboutApp != null)
                        view_?.about_reloop?.text = aboutApp.body
                    view_?.about_reloop?.movementMethod = ScrollingMovementMethod()
                }

//                Notify.alerterGreen(activity, baseResponse?.message)
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
        }
    }
}
