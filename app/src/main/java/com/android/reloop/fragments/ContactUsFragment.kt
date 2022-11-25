package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.adapters.AdapterSpinnerSimple
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import retrofit2.Call
import retrofit2.Response
import java.net.URLEncoder


/**
 * A simple [Fragment] subclass.
 */
class ContactUsFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse ,
    AlertDialogCallback {

    companion object {
        var orderIdToSend = ""
        var orderIdToShow = ""
        var status = -1
        fun newInstance(orderId: String, orderIdToShow: String, status: Int): ContactUsFragment {
            orderIdToSend = orderId
            this.orderIdToShow = orderIdToShow
            this.status = status
            return ContactUsFragment()
        }
    }

    var email: CustomEditText? = null
    var subject: CustomEditText? = null
    var description: CustomEditText? = null

    var submit: Button? = null
    var back: Button? = null
    var labelSpinner: Spinner? = null
    var orderIDTV: TextView? = null
    var whatsApp: ConstraintLayout? = null
    var instagram: ConstraintLayout? = null
    var deletact: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater.inflate(R.layout.fragment_contact_us, container, false)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun initViews(view: View?) {
        submit = view?.findViewById(R.id.submit)
        email = view?.findViewById(R.id.email)
        subject = view?.findViewById(R.id.subject)
        description = view?.findViewById(R.id.description)
        back = view?.findViewById(R.id.back)
        labelSpinner = view?.findViewById(R.id.labels)
        orderIDTV = view?.findViewById(R.id.orderId)
        email?.setText(User.retrieveUser()?.email)
        whatsApp = view?.findViewById(R.id.whatsApp)
        instagram = view?.findViewById(R.id.instagram)
        deletact = view?.findViewById(R.id.deleteAct)

        //--------Set No Of Employees Spinner----------------
        val noOfEmployeesAdapter = AdapterSpinnerSimple(
            R.layout.spinner_item_textview_drawable,
            Constants.getContactUsLabels(),
            null,
            false
        )
        labelSpinner?.adapter = noOfEmployeesAdapter
        labelSpinner!!.setSelection(0)
        if (!orderIdToShow.isNullOrEmpty()) {
            orderIDTV?.text = "Order Id : $orderIdToShow"
        } else {
            orderIDTV?.visibility = View.GONE
        }
        if (HomeFragment.settings.whatsapp_Number.isNullOrEmpty()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.DASHBOARD)
                ?.enque(
                    Network().apis()?.dashboard()
                )
                ?.execute()
        }
    }

    private fun setListeners() {
        submit?.setOnClickListener(this)
        back?.setOnClickListener(this)
        whatsApp?.setOnClickListener(this)
        instagram?.setOnClickListener(this)
        deletact?.setOnClickListener(this)

    }

    private fun populateData() {
        email?.setText(User.retrieveUser()?.email)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.submit -> {
                when {
                    email?.text.toString().isEmpty() -> {
                        Notify.alerterRed(activity, "Enter Email")
                    }
                    /* subject?.text.toString().isEmpty() -> {
                         Notify.alerterRed(activity, "Enter Subject")
                     }*/
                    description?.text.toString().isEmpty() -> {
                        Notify.alerterRed(activity, "Enter Description")
                    }
                    else -> {
                        if (status == -1 || status == 0) {
                            //DO NOTHING
                        } else {
                            if (status != OrderHistoryEnum.COMPLETED && (labelSpinner!!.selectedItemPosition == 3 || labelSpinner!!.selectedItemPosition == 4)
                            ) {
                                Notify.alerterRed(activity, "Your Order is not completed")
                                return
                            } else {
                                //DO NOTHING
                            }
                        }

                        if (orderIdToSend.isNullOrEmpty()) {
                            NetworkCall.make()
                                ?.setCallback(this)
                                ?.setTag(RequestCodes.API.CONTACT_US)
                                ?.autoLoading(requireActivity())
                                ?.enque(
                                    Network().apis()?.contactUs(
                                        email?.text.toString(),
                                        Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition],
                                        description?.text.toString()
                                    )
                                )
                                ?.execute()
                        } else {
                            if (status == 0) {
                                NetworkCall.make()
                                    ?.setCallback(this)
                                    ?.setTag(RequestCodes.API.CONTACT_US)
                                    ?.autoLoading(requireActivity())
                                    ?.enque(
                                        Network().apis()?.contactUsCollection(
                                            email?.text.toString(),
                                            Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition],
                                            description?.text.toString(), orderIdToSend
                                        )
                                    )
                                    ?.execute()
                            } else
                                NetworkCall.make()
                                    ?.setCallback(this)
                                    ?.setTag(RequestCodes.API.CONTACT_US)
                                    ?.autoLoading(requireActivity())
                                    ?.enque(
                                        Network().apis()?.contactUs(
                                            email?.text.toString(),
                                            Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition],
                                            description?.text.toString(), orderIdToSend
                                        )
                                    )
                                    ?.execute()
                        }
                    }
                }
            }
            R.id.back -> {
                activity?.onBackPressed()
            }
            R.id.whatsApp -> {
                if (!HomeFragment.settings.whatsapp_Number.isNullOrEmpty())
                    sendMessageToWhatsAppContact(HomeFragment.settings.whatsapp_Number[0].value)
                else
                    Notify.alerterRed(requireActivity(), "Whatsapp Number Not available")
            }
            R.id.instagram -> {
                if (!HomeFragment.settings.instagram_Link.isNullOrEmpty())
//                    openInstagram("https://www.instagram.com/pakistanicinemaa/")
                    openInstagram(HomeFragment.settings.instagram_Link[0].value)
                else
                    Notify.alerterRed(requireActivity(), "Instagram Account Not available")
            }
            R.id.deleteAct -> {
                AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.delete_account_text_msg))
            }
        }
    }


    override fun callDialog(model: Any?) {
        //call api to delete account

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DELETE_ACCOUNT)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.deleteaccount())
            ?.execute()
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.CONTACT_US -> {
                val baseResponse = Utils.getBaseResponse(response)
                Notify.alerterGreen(activity, baseResponse?.message)
                Handler().postDelayed({
                    activity?.onBackPressed()
                }, 1000)


            }
            RequestCodes.API.DASHBOARD -> {
                val baseResponse = Utils.getBaseResponse(response)
                try {
                    val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dashboard::class.java
                    )
                    HomeFragment.settings = dashboard.settings
                } catch (e: Exception) {
                    Log.e("TAG", e.toString())
                }
            }
            RequestCodes.API.DELETE_ACCOUNT -> {
                val baseResponse = Utils.getBaseResponse(response)
                Notify.alerterGreen(activity, baseResponse?.message)
                Handler().postDelayed({
                    Utils.logOut(activity)
                }, 1000)
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    private fun sendMessageToWhatsAppContact(number: String) {
        val packageManager = requireActivity().packageManager
        val i = Intent(Intent.ACTION_VIEW)
        try {
            if (appInstalledOrNot("com.whatsapp")) {
                val url = "https://api.whatsapp.com/send?phone=$number"
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                if (i.resolveActivity(packageManager) != null) {
                    requireActivity().startActivity(i)
                }
            } else {
                Notify.alerterRed(activity, "App Not installed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = requireActivity().packageManager
        val app_installed: Boolean = try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

    fun openInstagram(url: String) {
        val uri = Uri.parse(url)
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/xxx")
                )
            )
        }
    }

}
