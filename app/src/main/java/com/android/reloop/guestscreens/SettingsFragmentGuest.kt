package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.reloop.activities.ContinueAsActivity
import com.android.reloop.fragments.*
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.android.reloop.network.serializer.dashboard.SettingsModel
import com.android.reloop.utils.Configuration
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import com.onesignal.OneSignal
import com.reloop.reloop.activities.LoginActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.tinydb.TinyDB
import retrofit2.Call
import retrofit2.Response


/**
 * A simple [Fragment] subclass.
 */
class SettingsFragmentGuest : BaseFragment(), View.OnClickListener, AlertDialogCallback,
    OnNetworkResponse {

    companion object {

        fun newInstance(): SettingsFragmentGuest {
            return SettingsFragmentGuest()
        }
        var settings = SettingsModel()

        var TAG = "SettingsFragment"
    }

    var orderHistory: TextView? = null
    var aboutReloop: TextView? = null
    var termCondition: TextView? = null
    var contactUS: TextView? = null
    var billing: TextView? = null
    var editProfile: TextView? = null
    var subscription: TextView? = null
    var changePassword: TextView? = null
    var rewards_history: TextView? = null
    var campaings: TextView? = null
    var dropOffPin: TextView? = null
    var txtLoginNow: TextView? = null

    var paymentMethods: TextView? = null

    var logout: LinearLayout? = null
    var app_name: TextView? = null
    var build_no: TextView? = null
    var version_code: TextView? = null
    var branch_name: TextView? = null
    var app_info: LinearLayout? = null

    private var campaignVisibility: String =""
    private var dropOffVisibility: String =""



    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isResumed) {
            if (isVisibleToUser) {

               /* callDashboard()

                if (HomeActivity.settingClicked) {
                    checkLoginType()
                    HomeActivity.settingClicked = false
                }*/
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_settings_guest, container, false)
        initViews(view)
        setListeners()
//        populateData()
//        callDashboard()
        return view
    }

    private fun callDashboard() {
        /*if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.DASHBOARD)
                ?.enque(Network().apis()?.dashboard())
                ?.execute()
        }*/

        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())

        campaignVisibility = tinyDB.getString("campaign_visibility").toString()
        if(campaignVisibility.equals("VISIBLE")){
            campaings?.visibility = View.VISIBLE
        }else{
            campaings?.visibility = View.GONE
        }

        dropOffVisibility = tinyDB.getString("dropoff_visibility").toString()

        //use this to show drop-off flow and set visibility visible in xml file


    }

    private fun initViews(view: View?) {
        txtLoginNow = view?.findViewById(R.id.txtLoginNow)
        orderHistory = view?.findViewById(R.id.order_history)
        aboutReloop = view?.findViewById(R.id.about_reloop)
        contactUS = view?.findViewById(R.id.contact_us)
        termCondition = view?.findViewById(R.id.term_condition)
        billing = view?.findViewById(R.id.billing)
        editProfile = view?.findViewById(R.id.edit_profile)
        subscription = view?.findViewById(R.id.subscription)
        changePassword = view?.findViewById(R.id.change_password)
        logout = view?.findViewById(R.id.logout)
        branch_name = view?.findViewById(R.id.branch_name)
        version_code = view?.findViewById(R.id.version_code)
        build_no = view?.findViewById(R.id.build_no)
        app_name = view?.findViewById(R.id.app_name)
        app_info = view?.findViewById(R.id.app_info)
        rewards_history = view?.findViewById(R.id.rewards_history)
        campaings = view?.findViewById(R.id.campaigns)
        dropOffPin = view?.findViewById(R.id.drop_off_pin)
        paymentMethods = view?.findViewById(R.id.paymentMethods)





    }

    private fun setListeners() {
        orderHistory?.setOnClickListener(this)
        aboutReloop?.setOnClickListener(this)
        termCondition?.setOnClickListener(this)
        contactUS?.setOnClickListener(this)
        billing?.setOnClickListener(this)
        editProfile?.setOnClickListener(this)
        subscription?.setOnClickListener(this)
        logout?.setOnClickListener(this)
        changePassword?.setOnClickListener(this)
        rewards_history?.setOnClickListener(this)
        campaings?.setOnClickListener(this)
        dropOffPin?.setOnClickListener(this)
        paymentMethods?.setOnClickListener(this)
        txtLoginNow?.setOnClickListener(this)

    }

    private fun populateData() {
        if (Configuration.isDevelopment) {
            showVersionName()
        } else {
            app_info?.visibility = View.GONE
        }
    }

    private fun showLoginActivity() {
        try {
            val intent = Intent(requireContext(), ContinueAsActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.txtLoginNow -> {
                showLoginActivity()
            }
            R.id.order_history -> {
                /*BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.settingsFragmentContainer,
                    OrderHistoryFragment.newInstance(),
                    Constants.TAGS.OrderHistoryFragment
                )*/
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.about_reloop -> {
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.settingsFragmentContainer,
                    AboutReloopFragment.newInstance(),
                    Constants.TAGS.AboutReloopFragment
                )
            }
            R.id.term_condition -> {
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.settingsFragmentContainer,
                    TermConditionFragment.newInstance(),
                    Constants.TAGS.TermConditionFragment
                )
            }
            R.id.contact_us -> {
                /*BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.settingsFragmentContainer,
                    ContactUsFragment.newInstance("", "", "-1",""),
                    Constants.TAGS.ContactUsFragment
                )*/
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.billing -> {
                /*BaseActivity.replaceFragment(childFragmentManager,
                    Constants.Containers.settingsFragmentContainer,
                    BillingFragment.newInstance(),
                    Constants.TAGS.BillingFragment
                )*/
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.edit_profile -> {
                /*BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.settingsFragmentContainer,
                    EditProfileFragment.newInstance(),
                    Constants.TAGS.EditProfileFragment
                )*/
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.subscription -> {
                /*BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.settingsFragmentContainer,
                    SubscriptionFragment.newInstance(),
                    Constants.TAGS.SubscriptionFragment
                )*/
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.change_password -> {
                
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.logout -> {
                AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.logout_text_msg))
            }
            R.id.rewards_history -> {
                
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.campaigns -> {
                
                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

            }
            R.id.drop_off_pin -> {
               

                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))




            }
            R.id.paymentMethods -> {
               

                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))


            }
        }
    }

    override fun callDialog(model: Any?) {

        if(model == null){
            try {
                var oneSignalPlayerId = ""
                val oneSignalUserID = OneSignal.getDeviceState()!!.userId
                oneSignalPlayerId = oneSignalUserID ?: ""
                /*OneSignal.idsAvailable { userId, registrationId ->
                    oneSignalPlayerId = userId
                }*/

                LoginManager.getInstance().logOut() //facebook

                GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut() //google


                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.LOGOUT)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.logout(oneSignalPlayerId))
                    ?.execute()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            editProfile?.performClick()
        }



    }

    private fun checkLoginType() {

        if (User.retrieveUser()?.login_type!! == Constants.LoginTypes.FACEBOOK
            || User.retrieveUser()?.login_type!! == Constants.LoginTypes.GOOGLE
            || User.retrieveUser()?.login_type!! == Constants.LoginTypes.APP_LOGIN
        ) {
            if (User.retrieveUser()?.login_type!! == Constants.LoginTypes.FACEBOOK
                || User.retrieveUser()?.login_type!! == Constants.LoginTypes.GOOGLE
            ) {
                changePassword?.visibility = View.GONE
            }
            if (User.retrieveUser()?.first_name.isNullOrEmpty()
                || User.retrieveUser()?.last_name.isNullOrEmpty()
                || User.retrieveUser()?.addresses?.size == 0
                || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                || User.retrieveUser()?.phone_number.isNullOrEmpty()
            ) {
                val handler = Handler()
                handler.postDelayed(
                    {
                        editProfile?.performClick()
                    }, 1000 // old 5000
                )
            }
        }
    }

    private fun checkSocialLogin() {

        if (User.retrieveUser()?.login_type!! == Constants.LoginTypes.FACEBOOK
            || User.retrieveUser()?.login_type!! == Constants.LoginTypes.GOOGLE
        ) {
            changePassword?.visibility = View.GONE
            if (User.retrieveUser()?.first_name.isNullOrEmpty()
                || User.retrieveUser()?.last_name.isNullOrEmpty()
                || User.retrieveUser()?.addresses?.size == 0
                || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                || User.retrieveUser()?.phone_number.isNullOrEmpty()
            ) {
                val handler = Handler()
                handler.postDelayed(
                    {
                        editProfile?.performClick()
                    }, 1000 //old 5000
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showVersionName() {
        val manager: PackageManager? = activity?.getPackageManager()
        var info: PackageInfo? = null
        try {
            info = manager?.getPackageInfo(
                activity?.packageName.toString(),
                PackageManager.GET_ACTIVITIES
            )
            app_name?.text = resources.getString(R.string.app_name) + " " + "2020"
            version_code?.text = "Build " + info!!.versionCode
            build_no?.text = "Version " + info.versionName
            branch_name?.text = " " + Configuration.environmentName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.LOGOUT -> {
                Utils.logOut(activity)
            }
            /*RequestCodes.API.DASHBOARD -> {
                try {
                    val baseResponse = Utils.getBaseResponse(response)

                    val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dashboard::class.java)

                    settings = dashboard.settings

                    //open when campaign show
                    if (!settings.campaigns_visibility.isNullOrEmpty())
                    {
                        Log.e("TAG","===campaign visibility===" + settings.campaigns_visibility.get(0).value)
                        if(settings.campaigns_visibility.get(0).value.equals("1")) {
                            campaings?.visibility = View.VISIBLE
                            Log.d("campaignVisibility", "VISIBLE")
                        } else{
                            campaings?.visibility = View.GONE
                            Log.d("campaignVisibility", "INVISIBLE")
                        }
                    }
                    else{

                    }

                } catch (e: Exception) {
                    Log.e("Home Fragment", e.toString())
                }
            }*/
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }
}
