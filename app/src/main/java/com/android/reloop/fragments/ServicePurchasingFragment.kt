package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.reloop.fragments.EditProfileFragment
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.BaseActivity.Companion.replaceFragment
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.activities.WebViewActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.fragments.NewBillingInformationFragment.Companion.totalAmount
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.StepView
import com.reloop.reloop.model.ModelShopCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DataParsing
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.shop.BuyPlan
import com.reloop.reloop.network.serializer.shop.BuyPlanMonthly
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.shop.OneTimeServices
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_subscription_cycle_two_step.*
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
    var buyPlanMonthly: BuyPlanMonthly? = null



    var productId: Int? = -1
    var productType: Int? = -1
    var planId: String? = null
    var planPrice: Double? = -1.0
    var serviceType: Int? = 0
    var llrecycle : LinearLayout ? = null
    var stepViewHs : HorizontalScrollView ? = null
    var btnRecycle: Button? = null
    var nextFlag: Boolean = false
    var stripePayButton: Button? = null

    var planImage: String?=null
    var planTripValue: String?=null
    var planDescription: String?=null
    var planIcon: Int?=null

    //DISCOUNT
//    private var hhDiscount_ID: String =""
//    private var hhDiscount_PR: String =""
//    var discountedPriceString: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_subscription_cycle_two_step, container, false)

        productId = arguments?.getInt(Constants.DataConstants.Api.productID)
        productType = arguments?.getInt(Constants.DataConstants.Api.productType)
        planId = arguments?.getString(Constants.DataConstants.Api.planID)
        planPrice = arguments?.getDouble(Constants.DataConstants.Api.planPrice)
        serviceType = arguments?.getInt(Constants.DataConstants.Api.serviceType)

        planImage = arguments?.getString(Constants.DataConstants.Api.planImage)
        planTripValue = arguments?.getString(Constants.DataConstants.Api.planTripValue)
        planDescription = arguments?.getString(Constants.DataConstants.Api.planDescription)
        planIcon = arguments?.getInt(Constants.DataConstants.Api.planIcon)

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

        stripePayButton = view?.findViewById(R.id.stripePayButton)

        stepViewHs = view?.findViewById(R.id.stepView)


        if(serviceType == 1 || serviceType == 4) //for MonthlyService (1) For YearlyService(4)
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

        //old
        /*BaseActivity.replaceFragmentBackStackNull(
            childFragmentManager,
            Constants.Containers.containerSubscriptionCycle,
            BillingInformationFragment.newInstance(buyPlan, null)
        )*/

        if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.GET_PROFILE)
                //?.autoLoading(requireActivity()) //originally added
                ?.enque(Network().apis()?.getUserProfile())
                ?.execute()
        }

        stripePayButton?.visibility = View.VISIBLE
//        setStripeButtonText()

        stripePayButton?.text = "Pay ${ Utils.commaConversion(planPrice)} ${Constants.currencySign}"

        stripePayButton?.setOnClickListener {
            val fragment: NewBillingInformationFragment =
                childFragmentManager.fragments[0] as NewBillingInformationFragment
            fragment.performPayClick(requireActivity(), stripePayButton!!)

        }

        //New AD ORIGINAL
        BaseActivity.replaceFragmentBackStackNull(
            childFragmentManager,
            Constants.Containers.containerSubscriptionCycle,
            NewBillingInformationFragment.newInstance(buyPlan,
                null,
                serviceType,
                llrecycle,
                Utils.commaConversion(planPrice),
                stepViewHs,
                back,
                planImage,
                planTripValue,
                planDescription,
                planIcon,
                stripePayButton,
                next
            )
        )

        // TEST START -----------------------------------

        //TEST END -----------------------------------
    }

    fun setStripeButtonText(){

      //DISCOUNT ADDED FOR MONTHLY and YEARLY services only

        /*val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        hhDiscount_ID = tinyDB.getString("hhDiscount_ID").toString()
        hhDiscount_PR = tinyDB.getString("hhDiscount_PR").toString()

        if (serviceType == 1 || serviceType == 4) { // MONTHLY AND YEARLY
            if (hhDiscount_ID.equals("NULL",ignoreCase = true)){
                if(planPrice!=null){
                    stripePayButton?.text = "Pay ${ Utils.commaConversion(planPrice)} ${Constants.currencySign}"
                }
            }else{
                if(planPrice!=null){

                    val originalPrice = planPrice!!.toDouble()
                    val discountPercentage = hhDiscount_PR.toDoubleOrNull()
                    if (originalPrice != null && discountPercentage != null) {
                        val discountedPrice = originalPrice * (1 - (discountPercentage / 100))
                        discountedPriceString = String.format("%.2f", discountedPrice)

                        stripePayButton?.text = "Pay ${discountedPriceString} ${Constants.currencySign}"

                    }

                }

            }
        }else{
            stripePayButton?.text = "Pay ${ Utils.commaConversion(planPrice)} ${Constants.currencySign}"
        }*/
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
                next?.visibility = View.GONE


                //ORIGINAL ADDED next button
//                next?.text = "Pay ${Utils.commaConversion(planPrice)} ${Constants.currencySign}"

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
                stripePayButton?.visibility = View.GONE

                create?.visibility = View.VISIBLE
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.next -> {
             /*   if (parentToChild != null) {
                    parentToChild?.callChild()
                }*/

                nextFlag = true
                getPlanStatus()
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
                stepViewHs?.visibility = View.GONE
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
            //?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlan())
            ?.execute()
    }

    fun getPlanStatus() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN_STATUS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlanStatus())
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

            //original AD
            //next?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"

            //new added AD
            if(buyPlan?.total!! < 2){
                Notify.alerterRed(activity, getString(R.string.value_must_be_minimum_two))
                buyPlan?.total = 2.0
                next?.text = "Pay 2.0 ${Constants.currencySign}"


                //NEW ADDED FOR COUPON
                stripePayButton?.text = "Pay 2.0 ${Constants.currencySign}"

            }else{
                next?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"


                //NEW ADDED FOR COUPON
                stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
            }

            Log.d("COUPON_ADD","PAY ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}")


        } else {

            Log.d("BUY-NEW-SERVICE",""+GsonBuilder().setPrettyPrinting().create().toJson(buyPlan))


            buyPlanMonthly = BuyPlanMonthly() //5 values for monthly

            buyPlanMonthly!!.subscription_id = buyPlan!!.subscription_id

            if(buyPlan!!.subscription_type == 1 || buyPlan!!.subscription_type == 4){ //1 for Monthly, 4 for Yearly
                buyPlanMonthly!!.subscription_type = 1 // PASS 1 FOR MONTHLY AND YEARLY BOTH

                if (buyPlan!!.hh_discount_id!=null){
                    buyPlanMonthly!!.hh_discount_id = buyPlan!!.hh_discount_id
                }

            }else{
                buyPlanMonthly!!.subscription_type = buyPlan!!.subscription_type //ORIGINAL
            }

            buyPlanMonthly!!.total = buyPlan!!.total
            buyPlanMonthly!!.coupon_id = buyPlan!!.coupon_id
            buyPlanMonthly!!.transaction_id = buyPlan!!.transaction_id
            buyPlanMonthly!!.is_monthly = buyPlan!!.is_monthly

            if(buyPlan!!.is_monthly == 1){
                buyPlanMonthly!!.stripe_subscription_id = buyPlan!!.stripe_subscription_id
            }

            Log.d("BUY_PLAN",""+GsonBuilder().setPrettyPrinting().create().toJson(buyPlanMonthly))

            //Final call
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.BUY_SERVICE)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.buyService(buyPlanMonthly)) // old was only buyPlan
                ?.execute()

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BUY_PLAN && resultCode == RESULT_OK && data !=null) {
            Log.e("TAG","===onActivityResult===")
            if (serviceType == 1 || serviceType == 4) { //for MonthlyService (1) For YearlyService(4)
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


                stepViewHs?.visibility = View.GONE
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

                stepViewHs?.visibility = View.GONE
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
                //checkUserInfo()

                replaceFragment(
                    childFragmentManager,
                    Constants.Containers.containerSubscriptionCycle,
                    RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest,null),
                    Constants.TAGS.RecycleFragment
                )
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
            RequestCodes.API.GET_PROFILE-> {
                val baseResponse = Utils.getBaseResponse(response)
                val userModel = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    User::class.java
                )
                if (userModel.user_type == 0 || userModel.user_type == null) {
                    userModel?.user_type = Constants.UserType.household
                }

                //val token  =  baseResponse?.token
                userModel.api_token = User.retrieveUser()!!.api_token
                if (!userModel.api_token.isNullOrEmpty()) {
                    userModel.save(userModel, context,false)
                }

            }
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

            RequestCodes.API.BUY_SERVICE -> {
                buyServiceSuccess(response)
            }

            RequestCodes.API.GET_PLAN -> {
                val baseResponse = Utils.getBaseResponse(response)
                val getPlans = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), GetPlans::class.java)
                Log.e("TAG","===response1===" + getPlans)
                getPlansAll = getPlans
                handleUserPlansScenario(getPlans,"GET_PLAN")
            }

            RequestCodes.API.GET_PLAN_STATUS -> {
                val baseResponse = Utils.getBaseResponse(response)
                val getPlans = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), GetPlans::class.java)
                Log.e("TAG","===response1===" + getPlans)
                getPlansAll = getPlans


                if(getPlans.userPlans != null && getPlans.userPlans!!.isEmpty()) {
                    if (parentToChild != null) {
                        parentToChild?.callChild()
                    }
                } else{
                    handleUserPlansScenario(getPlans,"GET_PLAN_STATUS")
                }
            }

        }
    }

    fun buyServiceSuccess(response: Response<Any?>) {

        Log.e("TAG","===buy plan CALLED=== SERVICE FRAGMENT")


        val baseResponse = Utils.getBaseResponse(response)
        val dataParsing = Gson().fromJson(
            Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
            DataParsing::class.java)

        if (serviceType == 1 || serviceType == 4) { //for MonthlyService (1) For YearlyService(4)
            Log.e("TAG","===onActivityResult if===")

            HomeActivity.clearAllFragments(true)

            create?.text = "Track Subscription"
            llrecycle?.visibility = View.VISIBLE
            HomeActivity.clearAllFragments(true)

            back?.visibility = View.GONE
            next?.visibility = View.GONE


            stepViewHs?.visibility = View.GONE
            create?.visibility = View.GONE

            val fragment = ConfirmationSubscriptionFragment.newInstance()
            val args = Bundle()


            args.putInt(Constants.DataConstants.subscriptionCycle, Constants.subscriptionCycleTwo)

            buyPlanId = dataParsing.buyPlanId?.get(0)

            args.putString(Constants.DataConstants.purchaseID, dataParsing.buyPlanId?.get(0))
            fragment.arguments = args
            fragment.arguments = args
            replaceFragment(childFragmentManager,
                Constants.Containers.containerSubscriptionCycle,
                fragment,
                Constants.TAGS.ConfirmationSubscriptionFragment)
        }
        else{

            Notify.alerterGreen(activity, getString(R.string.str_payment_charged_success))


            Log.e("TAG","===onActivityResult else ===")
            llrecycle?.visibility = View.GONE
            HomeActivity.clearAllFragments(true)

            back?.visibility = View.GONE
            next?.visibility = View.GONE


            stepViewHs?.visibility = View.GONE
            create?.visibility = View.GONE

            stripePayButton!!.visibility = View.GONE

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
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        when (tag) {
            RequestCodes.API.BUY_PLAN -> {
                Notify.alerterRed(activity, response?.message)
            }
            RequestCodes.API.BUY_SERVICE -> {
                Notify.alerterRed(activity, response?.message)
            }
        }
    }

    private fun handleUserPlansScenario(getPlans: GetPlans?, callType: String) {
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
//                    || User.retrieveUser()?.gender.isNullOrEmpty()
//                        || User.retrieveUser()?.birth_date.isNullOrEmpty() //Earlier mandatory, now optional
                ) {
                    Notify.hyperlinkAlert(activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2)
                   /* replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerSubscriptionCycle,
                        EditProfileFragment.newInstance(),
                        Constants.TAGS.EditProfileFragment)*/
                    //activity?.onBackPressed()
                } else {
                    //goToNextScreen(getPlans)

                    if(nextFlag)
                    {
                        Log.e("TAG","=====subscription type ====" + serviceType)
                        Log.e("TAG","=====subscription type ====" + getPlans.userPlans!!.get(0).subscription_type)
                        Log.e("TAG","=====trips ====" + getPlans.userPlans!!.get(0).trips)
                        //if(getPlans.userPlans!!.get(0).subscription_type == Constants.RecycleCategoryType.NORMAL && getPlans.userPlans!!.get(0).trips ==0 )
                        if(serviceType == 1 || serviceType == 4) { //for MonthlyService (1) For YearlyService(4)
                            showPurchaseInfoDialog()
                            /*Notify.hyperlinkAlert(
                                activity,
                                "You are already subscribed for a monthly service. If you require extra trips, you can request a one-time service to avoid paying for another monthly subscription",
                                "Go to Reloop Store",
                                this, 1
                            )*/

                        }
                        else {

                            //AD CHANGE LOOP CALL API buy-new-service  === NEW
                            if(callType.equals("GET_PLAN")){
                                replaceFragment(
                                    childFragmentManager,
                                    Constants.Containers.containerSubscriptionCycle,
                                    RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest,null),
                                    Constants.TAGS.RecycleFragment
                                )
                            }else{
                                if (parentToChild != null) {
                                    parentToChild?.callChild()
                                }
                            }

                            //AD CHANGE LOOP CALL API buy-new-service === OLD
                            /*if (parentToChild != null) {
                                parentToChild?.callChild()
                            }*/
                        }
                    } else{
                        replaceFragment(
                            childFragmentManager,
                            Constants.Containers.containerSubscriptionCycle,
                            RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest,null),
                            Constants.TAGS.RecycleFragment
                        )
                    }
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
                  /*  replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerSubscriptionCycle,
                        EditProfileFragment.newInstance(),
                        Constants.TAGS.EditProfileFragment
                    )*/
                } else {
                    //Perform Function
                    //goToNextScreen(getPlans)

                    if(nextFlag)
                    {
                        Log.e("TAG","=====subscription type ====" + serviceType)
                        Log.e("TAG","=====subscription type ====" + getPlans.userPlans!!.get(0).subscription_type)
                        Log.e("TAG","=====trips ====" + getPlans.userPlans!!.get(0).trips)
                        //if(getPlans.userPlans!!.get(0).subscription_type == Constants.RecycleCategoryType.NORMAL && getPlans.userPlans!!.get(0).trips ==0 )
                        if(serviceType == 1 || serviceType == 4) { //for MonthlyService (1) For YearlyService(4)
                            showPurchaseInfoDialog()
                            /*Notify.hyperlinkAlert(
                                activity,
                                "You are already subscribed for a monthly service. If you require extra trips, you can request a one-time service to avoid paying for another monthly subscription",
                                "Go to Reloop Store",
                                this, 1
                            )*/

                        } else {
                            //AD CHANGE LOOP CALL API buy-new-service  === NEW
                            if(callType.equals("GET_PLAN")){
                                replaceFragment(
                                    childFragmentManager,
                                    Constants.Containers.containerSubscriptionCycle,
                                    RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest,null),
                                    Constants.TAGS.RecycleFragment
                                )
                            }else{
                                if (parentToChild != null) {
                                    parentToChild?.callChild()
                                }
                            }

                            //AD CHANGE LOOP CALL API buy-new-service === OLD
                            /*if (parentToChild != null) {
                                parentToChild?.callChild()
                            }*/
                        }
                    }
                    else{
                        replaceFragment(
                            childFragmentManager,
                            Constants.Containers.containerSubscriptionCycle,
                            RecycleFragment.newInstance(getPlansAll, userContainSingleCollectionRequest,null),
                            Constants.TAGS.RecycleFragment
                        )
                    }
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

    private fun showPurchaseInfoDialog() {
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_subscription_message)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm1 = dialog.findViewById(R.id.confirm1) as Button
        val confirm2 = dialog.findViewById(R.id.confirm2) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        message.text = Html.fromHtml(getString(R.string.subscription_message))

        //message.text = "You are already subscribed for a monthly service. If you require extra trips, you can request a one-time service to avoid paying for another monthly subscription"
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }

        confirm1.setOnClickListener {
            dialog.dismiss()
            nextFlag = false
            //requireActivity().onBackPressed()

            llrecycle?.visibility = View.GONE
            HomeActivity.clearAllFragments(true)

            back?.visibility = View.GONE
            next?.visibility = View.GONE

            stripePayButton?.visibility = View.GONE

            stepViewHs?.visibility = View.GONE
            create?.visibility = View.GONE

           /* replaceFragment(
                childFragmentManager,
                Constants.Containers.containerSubscriptionCycle,
                ShopFragment.newInstance("fromPopup"),
                Constants.TAGS.ShopFragment)*/

            val gson = Gson()
            var productFromShared: List<Category?> = ArrayList()
            var dataListServices: ArrayList<ModelShopCategories> = ArrayList()
            val sharedPref: SharedPreferences =
               requireContext().getSharedPreferences("subscription", Context.MODE_PRIVATE)
            val jsonPreferences = sharedPref.getString("list", "")
            val jsonPreferences1 = sharedPref.getString("list1", "")

            val type = object : TypeToken<List<Category?>?>() {}.type
            val type1 = object : TypeToken<List<ModelShopCategories?>?>() {}.type

            productFromShared = gson.fromJson(jsonPreferences, type)
            dataListServices = gson.fromJson(jsonPreferences1, type1)
            Log.e("TAG","====productFromShared===" + productFromShared)
            Log.e("TAG","====dataListServices===" + dataListServices)

            sendtodetailscreen(productFromShared,dataListServices)

        }

        confirm2.setOnClickListener {
            dialog.dismiss()
            nextFlag = false
           /* if (parentToChild != null) {
                parentToChild?.callChild()
            }*/

            llrecycle?.visibility = View.GONE
            HomeActivity.clearAllFragments(true)

            back?.visibility = View.GONE
            next?.visibility = View.GONE

            stripePayButton?.visibility = View.GONE

            stepViewHs?.visibility = View.GONE
            create?.visibility = View.GONE

            replaceFragment(
                childFragmentManager,
                Constants.Containers.containerSubscriptionCycle,
                SubscriptionFragment.newInstance(),
                Constants.TAGS.SubscriptionFragment
            )
        }


        dialog.show()
    }

    private fun sendtodetailscreen(
        productFromShared: List<Category?>?,
        dataListServices: ArrayList<ModelShopCategories>,
    ) {
        val fragment = ShopDetailFragment.newInstance()
        val args = Bundle()
            //check profile updated or not
            args.putString(
                Constants.DataConstants.shopFragmentHeading,
                dataListServices[1].heading
            )
            args.putInt(
                Constants.DataConstants.shopFragmentIcon,
                dataListServices[1].icons!!
            )
            args.putInt(
                Constants.DataConstants.position,1
            )
            args.putInt(
                Constants.DataConstants.Api.productID,
                productFromShared?.get(1)?.id!!
            )
            args.putInt(
                Constants.DataConstants.Api.productType,
                productFromShared.get(1)?.type!!
            )
            args.putInt(
                Constants.DataConstants.Api.serviceType,
                productFromShared.get(1)?.service_type!!
            )
        fragment.arguments = args
        replaceFragment(
            childFragmentManager,
            Constants.Containers.containerSubscriptionCycle,
            fragment,
            Constants.TAGS.ShopDetailFragment
        )
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
