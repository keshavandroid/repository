package com.reloop.reloop.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.reloop.fragments.EditProfileFragment
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.BaseActivity.Companion.replaceFragment
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.activities.WebViewActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.StepView
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DataParsing
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.shop.BuyPlan
import com.reloop.reloop.network.serializer.shop.OneTimeServices
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_subscription_cycle_two_step.*
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Response


/**
 * A simple [Fragment] subclass.
 */
class ServicePurchasingFragment : BaseFragment(), StepView, View.OnClickListener,
    ChildToParent, OnNetworkResponse, AlertDialogCallback {

    companion object {
        var parentToChild: ParentToChild? = null
        fun newInstance(): ServicePurchasingFragment {
            return ServicePurchasingFragment()
        }

        var TAG = "SubscriptionCycleTwoStepFragment"
        var BUY_PLAN = 100
    }

    private var getPlansAll: GetPlans? = null
    var userContainSingleCollectionRequest: Boolean = false
    var currentStep: Int? = -1
    var imageStep3: ImageView? = null
    var textStep3: TextView? = null
    var imageStep4: ImageView? = null
    var textStep4: TextView? = null

    var back: Button? = null
    var next: Button? = null
    var create: Button? = null

    var buyPlanId: String? =null
    var buyPlan: BuyPlan? = null
    var productId: Int? = -1
    var productType: Int? = -1
    var planId: String? = null
    var planPrice: Double? = -1.0
    var serviceType: Int? = 0
    var llrecycle : LinearLayout ? = null
    var btnRecycle: Button? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? =
            inflater.inflate(R.layout.fragment_subscription_cycle_two_step, container, false)
        productId = arguments?.getInt(Constants.DataConstants.Api.productID)
        productType = arguments?.getInt(Constants.DataConstants.Api.productType)
        planId = arguments?.getString(Constants.DataConstants.Api.planID)
        planPrice = arguments?.getDouble(Constants.DataConstants.Api.planPrice)
        serviceType = arguments?.getInt(Constants.DataConstants.Api.serviceType)


        Log.e("TAG","====service type in detail======" + serviceType)

        buyPlan = BuyPlan()
        buyPlan?.plan_id = planId
        buyPlan?.subscription_id = productId
        buyPlan?.subscription_type = serviceType
        buyPlan?.total = planPrice
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {

        imageStep3 = view?.findViewById(R.id.img_step3)
        imageStep4 = view?.findViewById(R.id.img_step4)


        textStep3 = view?.findViewById(R.id.text_step3)
        textStep4 = view?.findViewById(R.id.text_step4)

        next = view?.findViewById(R.id.next)
        back = view?.findViewById(R.id.back)
        create = view?.findViewById(R.id.create)

        llrecycle = view?.findViewById(R.id.buttonsrecycle)
        btnRecycle = view?.findViewById(R.id.btn_recycle)


        if(serviceType == 1)
        {
           textStep4?.text = MainApplication.applicationContext().getString(R.string.confirmation)
           userContainSingleCollectionRequest = false
        }
        else{
            textStep4?.text = MainApplication.applicationContext().getString(R.string.scheduling)
            userContainSingleCollectionRequest = true
        }
    }

    private fun setListeners() {
        ProductPurchasingFragment.stepView = this
        next?.setOnClickListener(this)
        back?.setOnClickListener(this)
        create?.setOnClickListener(this)
        btnRecycle?.setOnClickListener(this)
    }

    private fun populateData() {
        BaseActivity.replaceFragmentBackStackNull(
            childFragmentManager,
            Constants.Containers.containerSubscriptionCycle,
            BillingInformationFragment.newInstance(buyPlan, null)
        )
    }

    //------------------------------Update StepView UI-----------------------------
    @SuppressLint("SetTextI18n")
    override fun StepNumber(stepNumber: Int) {

        Log.e("TAG","====setp number called==")
        currentStep = stepNumber
        when (stepNumber) {

            Constants.recycleStep3 -> {

                BaseActivity.stepViewUpdateUI(
                    imageStep3,
                    R.drawable.icon_billing_en,
                    textStep3,
                    requireActivity().getColor(R.color.green_color_button)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep4,
                    R.drawable.icon_request_submitted_un,
                    textStep4,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                back?.visibility = View.VISIBLE
                next?.text = "Pay ${Utils.commaConversion(planPrice)} ${Constants.currencySign}"
            }
            Constants.recycleStep4 -> {
                BaseActivity.stepViewUpdateUI(
                    imageStep3,
                    R.drawable.icon_billing_un,
                    textStep3,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep4,
                    R.drawable.icon_request_submitted_en,
                    textStep4,
                    requireActivity().getColor(R.color.green_color_button)
                )
                back?.visibility = View.GONE
                next?.visibility = View.GONE
                create?.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.next -> {
                if (parentToChild != null) {
                    parentToChild?.callChild()
                }
            }
            R.id.back -> {
                requireActivity().onBackPressed()
            }
            R.id.create -> {
                showOrderHistory()
                //HomeActivity.clearAllFragments(true)

                checkUserInfo()
              /*  val fm: FragmentManager? = fragmentManager
                val fragm: HomeFragment = fm?.findFragmentById(R.id.container_home) as HomeFragment
                fragm.checkUserInfo()*/
            }

            R.id.btn_recycle ->
            {
                back?.visibility = View.GONE
                stepView.visibility = View.GONE
                create?.visibility = View.GONE
                llrecycle?.visibility = View.GONE
                HomeActivity.clearAllFragments(true)
                //Utils.clearAllFragmentStack(activity?.supportFragmentManager)

                LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                checkUserInfo()
              /*  if(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                    checkUserInfo()
                }
                else
                {
                    EasyPermissions.requestPermissions(
                        requireActivity(),
                        getString(R.string.write_external_storage),
                        RequestCodes.RC_STORAGE_PERM,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }*/
               /* replaceFragment(childFragmentManager,
                    Constants.Containers.containerSubscriptionCycle,
                    RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest),
                    Constants.TAGS.RecycleFragment)
                */
            }
        }
    }
    fun checkUserInfo() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlan())
            ?.execute()
    }

    private fun showOrderHistory() {
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerSubscriptionCycleParent,
            SubscriptionFragment.newInstance(),
            Constants.TAGS.OrderHistoryFragment
        )
    }

    @SuppressLint("SetTextI18n")
    override fun callParent(model: Any?) {
        buyPlan = model as? BuyPlan

        if (buyPlan?.changePrice == 1) {
            buyPlan?.changePrice = 0
            next?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
        } else {
            val buyPlanMap = HashMap<String, Any?>()
            buyPlanMap["card_number"] = buyPlan?.card_number
            buyPlanMap["exp_month"] = buyPlan?.exp_month
            buyPlanMap["exp_year"] = buyPlan?.exp_year
            buyPlanMap["cvv"] = buyPlan?.cvv
            buyPlanMap["subscription_id"] = buyPlan?.subscription_id
            buyPlanMap["subscription_type"] = buyPlan?.subscription_type
            buyPlanMap["total"] = buyPlan?.total
            if (buyPlan?.coupon_id != 0) {
                buyPlanMap["coupon_id"] = buyPlan?.coupon_id
            }
            if (buyPlan?.plan_id != null)
                buyPlanMap["plan_id"] = buyPlan?.plan_id

          /*  val URL = "card_number=${URLEncoder.encode(
                buyPlan?.card_number,
                "UTF-8"
            )}&expiry_date=${URLEncoder.encode(buyPlan?.exp_year + buyPlan?.exp_month, "UTF-8")}" +
                    "&card_security_code=${URLEncoder.encode(buyPlan?.cvv.toString(), "UTF-8")}&" +
                    "user_id=${URLEncoder.encode(
                        User.retrieveUser()?.id.toString(),
                        "UTF-8"
                    )}&total=${URLEncoder.encode(
                        buyPlan?.total.toString(),
                        "UTF-8"
                    )}&coupon_id=${URLEncoder.encode(
                        buyPlan?.coupon_id.toString(),
                        "UTF-8"
                    )}&subtotal=${URLEncoder.encode(
                        "",
                        "UTF-8"
                    )}&subscription_id=${URLEncoder.encode(
                        buyPlan?.subscription_id.toString(),
                        "UTF-8"
                    )}" +
                    "&subscription_type=${URLEncoder.encode(
                        buyPlan?.subscription_type.toString(),
                        "UTF-8"
                    )}"

            if (buyPlan?.plan_id != null)
                "&plan_id=${URLEncoder.encode(buyPlan?.plan_id, "UTF-8")}"*/
            val URL = "card_number=${buyPlan?.card_number}&expiry_date=${buyPlan?.exp_year + buyPlan?.exp_month}" +
                    "&card_security_code=${buyPlan?.cvv.toString()}&" +
                    "user_id=${User.retrieveUser()?.id.toString()}&total=${buyPlan?.total.toString()}" +
                    "&coupon_id=${buyPlan?.coupon_id.toString()}&subtotal=${""}&subscription_id=${buyPlan?.subscription_id.toString()}" +
                    "&subscription_type=${buyPlan?.subscription_type.toString()}" + "&plan_id=${buyPlan?.plan_id}"

            Log.e(ProductPurchasingFragment.TAG,"====SECURITY CODE SERVICE===" +buyPlan?.cvv.toString())

            startActivityForResult(Intent(activity, WebViewActivity::class.java).putExtra(WebViewActivity.URL, URL), BUY_PLAN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BUY_PLAN && resultCode == RESULT_OK && data !=null) {
            Log.e("TAG","===onActivityResult===")
            if (serviceType == 1) {
                Log.e("TAG","===onActivityResult if===")

                HomeActivity.clearAllFragments(true)
               /* val fragment = ConfirmationSubscriptionFragment.newInstance()
                val args = Bundle()
                args.putInt(
                    Constants.DataConstants.subscriptionCycle,
                    Constants.subscriptionCycleTwo
                )
                args.putString(
                    Constants.DataConstants.purchaseID,
                    data.extras?.getString(WebViewActivity.REFERENCE_ID)
                )
                fragment.arguments = args*/

                /*replaceFragment(childFragmentManager,
                    Constants.Containers.containerSubscriptionCycle,
                    fragment,
                    Constants.TAGS.ConfirmationSubscriptionFragment
                )*/

                create?.text = "Track Subscription"
                llrecycle?.visibility = View.VISIBLE
                HomeActivity.clearAllFragments(true)

                back?.visibility = View.GONE
                next?.visibility = View.GONE
                stepView.visibility = View.GONE
                create?.visibility = View.GONE

                val fragment = ConfirmationSubscriptionFragment.newInstance()
                val args = Bundle()
                args.putInt(
                    Constants.DataConstants.subscriptionCycle,
                    Constants.subscriptionCycleTwo
                )
                args.putString(
                    Constants.DataConstants.purchaseID,
                    data.extras?.getString(WebViewActivity.REFERENCE_ID)
                )
                fragment.arguments = args
                replaceFragment(childFragmentManager,
                    Constants.Containers.containerSubscriptionCycle,
                    fragment,
                    Constants.TAGS.ConfirmationSubscriptionFragment)
            }
            else{
                Log.e("TAG","===onActivityResult else ===")
                llrecycle?.visibility = View.GONE
                HomeActivity.clearAllFragments(true)

                back?.visibility = View.GONE
                next?.visibility = View.GONE
                stepView.visibility = View.GONE
                create?.visibility = View.GONE

               /* val fragment = ConfirmationSubscriptionFragment.newInstance()
                val args = Bundle()
                args.putInt(
                    Constants.DataConstants.subscriptionCycle,
                    Constants.subscriptionCycleTwo
                )
                args.putString(
                    Constants.DataConstants.purchaseID,
                    data.extras?.getString(WebViewActivity.REFERENCE_ID)
                )
                fragment.arguments = args
                replaceFragment(childFragmentManager,
                    Constants.Containers.containerSubscriptionCycle,
                    fragment,
                    Constants.TAGS.ConfirmationSubscriptionFragment
                )*/

                /* replaceFragment(
                     childFragmentManager,
                     Constants.Containers.containerSubscriptionCycle,
                     RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest),
                     Constants.TAGS.RecycleFragment
                 )*/

                LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                checkUserInfo()
             /*   if(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                    checkUserInfo()
                }
                else
                {
                    EasyPermissions.requestPermissions(
                        requireActivity(),
                        getString(R.string.write_external_storage),
                        RequestCodes.RC_STORAGE_PERM,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }*/
            }
        }else if(requestCode == BUY_PLAN && resultCode == RESULT_CANCELED && data != null){
            Notify.alerterRed(activity,data.extras?.getString(WebViewActivity.MESSAGE)?.replace("+", " "))
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.BUY_PLAN -> {
                Log.e("TAG","===buy plan CALLED===")
                val baseResponse = Utils.getBaseResponse(response)
                val dataParsing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    DataParsing::class.java)

                    if (currentStep == Constants.recycleStep3) {
                        val fragment = ConfirmationSubscriptionFragment.newInstance()
                        val args = Bundle()

                        args.putInt(Constants.DataConstants.subscriptionCycle, Constants.subscriptionCycleTwo)

                        buyPlanId = dataParsing.buyPlanId?.get(0)

                        args.putString(Constants.DataConstants.purchaseID, dataParsing.buyPlanId?.get(0))
                        fragment.arguments = args

                       /* replaceFragment(
                            childFragmentManager,
                            Constants.Containers.containerSubscriptionCycle,
                            fragment,
                            Constants.TAGS.ConfirmationSubscriptionFragment
                        )*/
                        create?.text = "Track Subscription"
                        llrecycle?.visibility = View.VISIBLE

                        LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                        checkUserInfo()

                      /*  if(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                        {
                            LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                            checkUserInfo()
                        }
                        else
                        {
                            EasyPermissions.requestPermissions(requireActivity(), getString(R.string.write_external_storage),
                                RequestCodes.RC_STORAGE_PERM,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        }*/
                    }
            }

            RequestCodes.API.GET_PLAN -> {
                val baseResponse = Utils.getBaseResponse(response)
                val getPlans = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), GetPlans::class.java)
                Log.e("TAG","===response1===" + getPlans)
                getPlansAll = getPlans
                handleUserPlansScenario(getPlans)
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        when (tag) {
            RequestCodes.API.BUY_PLAN -> {
                Notify.alerterRed(activity, response?.message)
            }
        }
    }

    private fun handleUserPlansScenario(getPlans: GetPlans?) {
        //------------------------Check If User has Bought Trips---------------------------

        Log.e("TAG","====userContainSingleCollectionRequest======" + userContainSingleCollectionRequest)

        val userContainsTripsMonthly: UserPlans? = getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.NORMAL }
        Log.e("TAG","=====subscription type====" +  Constants.RecycleCategoryType.NORMAL)
        val userContainsTripsBulky: UserPlans? =
            getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.BULKY_ITEM }
        Log.e("TAG","=====subscription type====" +  Constants.RecycleCategoryType.BULKY_ITEM)

        val userContainsTripsSingleCollection: UserPlans? =
            getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.SINGLE_COLLECTION }
        Log.e("TAG","=====subscription type====" +  Constants.RecycleCategoryType.SINGLE_COLLECTION)

        val userContainsFreeTrips: UserPlans? = getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.FREE_SERVICE }
        Log.e("TAG","=====subscription type====" +  Constants.RecycleCategoryType.FREE_SERVICE)
        if (userContainsTripsMonthly != null || userContainsTripsBulky != null || userContainsTripsSingleCollection != null || userContainsFreeTrips != null) {
            if (MainApplication.userType() == Constants.UserType.household) {

                Log.e("TAG","===street====" +User.retrieveUser()?.addresses?.get(0)?.street)
                if (User.retrieveUser()?.first_name.isNullOrEmpty()
                    || User.retrieveUser()?.last_name.isNullOrEmpty()
                    || User.retrieveUser()?.addresses.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.building_name.isNullOrEmpty()
                    || User.retrieveUser()?.phone_number.isNullOrEmpty()
                    || User.retrieveUser()?.gender.isNullOrEmpty()
                    || User.retrieveUser()?.birth_date.isNullOrEmpty()
                ) {
                    Notify.hyperlinkAlert(activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2
                    )
                    replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerSubscriptionCycle,
                        EditProfileFragment.newInstance(),
                        Constants.TAGS.EditProfileFragment)
                    //activity?.onBackPressed()
                } else {
                    //goToNextScreen(getPlans)

                    replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerSubscriptionCycle,
                        RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest),
                        Constants.TAGS.RecycleFragment
                    )
                }
            } else {
                if (User.retrieveUser()?.organization?.name.isNullOrEmpty()
                    || User.retrieveUser()?.addresses.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.building_name.isNullOrEmpty()
                    || User.retrieveUser()?.phone_number.isNullOrEmpty())
                {
                    Notify.hyperlinkAlert(
                        activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2)
                   // activity?.onBackPressed()
                    replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerSubscriptionCycle,
                        EditProfileFragment.newInstance(),
                        Constants.TAGS.EditProfileFragment
                    )
                } else {
                    //Perform Function
                    //goToNextScreen(getPlans)
                    replaceFragment(childFragmentManager,
                        Constants.Containers.containerSubscriptionCycle,
                        RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest),
                        Constants.TAGS.RecycleFragment)
                }
            }

        } else {
            Notify.hyperlinkAlert(activity,
                "Please Subscribe through the ReLoop Store",
                "Go to Reloop Store",
                this, 1
            )
        }
    }

    private fun goToNextScreen(getPlans: GetPlans) {
        //--------------------------------Check Which Plans User Has Bought------------------------
        val userContainsSingleCollection: UserPlans? =
            getPlans.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.SINGLE_COLLECTION }
        val userContainsSameDayRequest: UserPlans? =
            getPlans.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.SAME_DAY }
        val userContainsNextDayRequest: UserPlans? =
            getPlans.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.NEXT_DAY }

        /*---------------------------------Calculate New Price if User has Bought SingleCollection
        and User does or does not contain SameDay and Next Day Request-------------------------*/
        if (getPlans.oneTimeServices!!.size > 2) {

            val OTS: OneTimeServices? =
                getPlans.oneTimeServices?.find { it.category_type == Constants.RecycleCategoryType.SINGLE_COLLECTION }
            if (userContainsSingleCollection != null && userContainsSameDayRequest == null) {
                getPlans.oneTimeServices?.find {
                    it.category_type == Constants.RecycleCategoryType.SAME_DAY
                }?.price = getPlans.oneTimeServices?.find {
                    it.category_type == Constants.RecycleCategoryType.SAME_DAY
                }?.price?.minus(OTS?.price!!)
//                    getPlans.oneTimeServices?.get(0)?.price = getPlans.oneTimeServices?.get(0)?.price?.minus(getPlans.oneTimeServices?.get(2)?.price!!)
            }

            if (userContainsSingleCollection != null && userContainsNextDayRequest == null) {
                getPlans.oneTimeServices?.find {
                    it.category_type == Constants.RecycleCategoryType.NEXT_DAY
                }?.price = getPlans.oneTimeServices?.find {
                    it.category_type == Constants.RecycleCategoryType.NEXT_DAY
                }?.price?.minus(OTS?.price!!)
                /*getPlans.oneTimeServices?.get(1)?.price =
                    getPlans.oneTimeServices?.get(1)?.price?.minus(
                        getPlans.oneTimeServices?.get(2)?.price!!
                    )*/
            }
        }

        userContainSingleCollectionRequest = userContainsSingleCollection != null
        if (currentStep == Constants.recycleStep3) {
            val fragment = ConfirmationSubscriptionFragment.newInstance()
            val args = Bundle()

            args.putInt(Constants.DataConstants.subscriptionCycle, Constants.subscriptionCycleTwo)
            args.putString(Constants.DataConstants.purchaseID, buyPlanId)
            fragment.arguments = args
            replaceFragment(
                childFragmentManager,
                Constants.Containers.containerSubscriptionCycle,
                fragment,
                Constants.TAGS.ConfirmationSubscriptionFragment
            )
        }
    }

    override fun callDialog(model: Any?) {
        if (model as Int == 1) {
            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
            HomeActivity.bottomNav.selectedItemId = R.id.navigation_shop
        } else if (model == 2) {
            HomeActivity.settingClicked = true
            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
            HomeActivity.bottomNav.selectedItemId = R.id.navigation_settings
        }
    }
}
