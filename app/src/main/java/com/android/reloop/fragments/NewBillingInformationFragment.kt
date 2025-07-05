package com.reloop.reloop.fragments


import CouponVerification
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.adapters.BillingPaymentMethodsAdapter
import com.android.reloop.bottomsheet.SubscriptionBottomsheet
import com.android.reloop.fragments.PaymentMethodsFragment
import com.android.reloop.model.PaymentMethods
import com.android.reloop.model.SavedCard
import com.android.reloop.utils.LogManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.model.ModelShopCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DataParsing
import com.reloop.reloop.network.serializer.Payment
import com.reloop.reloop.network.serializer.PaymentIntentModel
import com.reloop.reloop.network.serializer.cart.BuyProduct
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.couponverification.CouponCategory
import com.reloop.reloop.network.serializer.couponverification.CouponSendData
import com.reloop.reloop.network.serializer.shop.BuyPlan
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.shop.Product
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.*
import com.stripe.android.*
import com.stripe.android.googlepaylauncher.GooglePayLauncher
import com.stripe.android.model.*
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.android.synthetic.main.fragment_subscription_cycle_two_step.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


/**
 * A simple [Fragment] subclass.
 */
class NewBillingInformationFragment : BaseFragment(), ParentToChild, View.OnClickListener,
    BillingPaymentMethodsAdapter.ItemClickListener,SubscriptionBottomsheet.ItemClickListener,
    AlertDialogCallback,
    OnNetworkResponse {



    private lateinit var dialog: Dialog

    var childToParent: ChildToParent? = null
    var cardNumber: CustomEditText? = null
    var cardExpiry: CustomEditText? = null
    var cardCVV: CustomEditText? = null
    var cardHolder: CustomEditText? = null
    var headingDiscountCoupon: TextView? = null
    var pinView: CustomEditText? = null
    var pinViewService: CustomEditText? = null
    var applyCoupon: Button? = null
    var applyCouponService: Button? = null
    var rememberMe: CheckBox? = null
    var message: TextView? = null
    var rememberMeService: CheckBox? = null
    var messageService: TextView? = null

    var txtAddressName: TextView?=null
    var txtAddressEmail: TextView?=null
    var txtAddressPhone: TextView?=null
    var txtAddressAddress: TextView?=null

    var subTotalPrice:TextView?=null
    var discountedPrice:TextView?=null
    var deliveryFee:TextView?=null
    var totalPrice:TextView?=null
    var discount_label : RelativeLayout ?= null

    var llProductData: LinearLayout?=null
    var cardServiceData: CardView?=null
    var heading_selected_service: TextView?=null
    var imageView_category_icon: ImageView?=null
    var info_button: ImageView?=null
    var tv_heading_category: TextView ?= null
    var tv_description: TextView ?= null

    var buyPlanId: String? =null
    //payment methods
    var rv_pm: RecyclerView? = null
    var tvNopm: TextView? = null
    var pmList: ArrayList<PaymentMethods>? = ArrayList()

    var savedCardList: SavedCard?=null


    var pmListNew: ArrayList<PaymentMethods>? = ArrayList()
    var linearLayoutManager: LinearLayoutManager? = null
    var ivcard: ImageView? = null
    var tvcardNo: TextView? = null
    var lllist: LinearLayout? = null
    var rlPaymentMethod: RelativeLayout? = null
    var frmCard: FrameLayout? = null
    var iv_card: ImageView?= null
    var txtOtherSavedMethods: TextView?= null
    var rlAddBillingPaymentMethod: RelativeLayout?= null
    var radio_button: RadioButton?= null
    var default_card_details: LinearLayout?= null
    var billingAdapter: BillingPaymentMethodsAdapter?= null
    var paymentMethodType: String ="defaultCard"
    var rcvCardStatus: String = ""
    var relPaymentMethodBack: RelativeLayout?=null
    var llCoupanCodeservice: LinearLayout?=null

    //Default card
    var defaultCardId: String = ""
    var defaultCardNumber: String = ""
    var defaultCardExpiry: String = ""

    //selected card
    var selectedCardId : String = ""
    var selectedCardNumber : String = ""
    var selectedCardExpiry : String = ""

    //GOOGLE PAY
    private var mGooglePayButton: RelativeLayout? = null
    private lateinit var clientSecret: String
    private lateinit var paymentIntent: String
    private var getPlansAll: GetPlans? = null
    var nextFlag: Boolean = true

    //STRIPE PAY
    private var stripePayButton: Button? = null
    lateinit var paymentSheet: PaymentSheet
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var googlePayConfig: PaymentSheet.GooglePayConfiguration
    lateinit var billingDetails: PaymentSheet.BillingDetails
    lateinit var paymentIntentClientSecret: String
    private var paymentIntentIdStripe: String = ""
    var payment_method = ""

    private var stripePayButtonService:Button ? = null

    private var googlepay_visibility: String =""

    //DISCOUNT
    private var hhDiscount_ID: String =""
    private var hhDiscount_PR: String =""
    private var hhDiscount_CODE: String =""



    //MONTHLY
    lateinit var pi_monthly_service: String
    lateinit var pi_id_monthly: String
    lateinit var stripe_subscription_id : String


    companion object {
        var buyPlan: BuyPlan? = null
        var buyProduct: BuyProduct? = null
        var serviceType: Int? = null
        var llrecycle : LinearLayout ? = null
        var totalAmount : String ? = null
        var stepViewHs : HorizontalScrollView ? = null
        var backButton : Button?=null

        var planImage: String?=null
        var planTripValue: String?=null
        var planDescription: String?=null
        var planIcon: Int?=null
        var stripePayButton: Button?=null
        var nextButton : Button?=null


        fun newInstance(
            buyPlan: BuyPlan?,
            buyProduct: BuyProduct?,
            serviceType: Int?,
            llrecycle: LinearLayout?,
            totalAmount: String,
            stepViewHs: HorizontalScrollView?,
            backButton : Button?,
            planImage : String?,
            planTripValue : String?,
            planDescription : String?,
            planIcon: Int?,
            stripePayButton: Button?,
            nextButton: Button?

        ): NewBillingInformationFragment {
            this.buyPlan = buyPlan
            this.buyProduct = buyProduct
            this.serviceType = serviceType
            this.llrecycle = llrecycle
            this.totalAmount = totalAmount
            this.stepViewHs = stepViewHs
            this.backButton = backButton
            this.planImage = planImage
            this.planTripValue = planTripValue
            this.planDescription = planDescription
            this.planIcon = planIcon
            this.stripePayButton = stripePayButton
            this.nextButton = nextButton
            return NewBillingInformationFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ChildToParent) {
            childToParent = parentFragment as? ChildToParent?
        } else {
            Notify.Toast("Child To Parent Callback Not Implemented")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? =
            inflater.inflate(R.layout.fragment_new_billing_information, container, false)
        if (ProductPurchasingFragment.stepView != null) {
            ProductPurchasingFragment.stepView?.StepNumber(Constants.recycleStep3)
        }
        ServicePurchasingFragment.parentToChild = this
        ProductPurchasingFragment.parentToChild = this
        initViews(view)
        setListeners()
        populateData()

        setBillingDetailData()


        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //STRIPE
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        stripePayButton?.setOnClickListener{

            if(serviceType==1 || serviceType==4){ //for MonthlyService (1) For YearlyService(4)
                //same data for monthly and yearly
                getPlanStatus()
            }else{
                getPaymentIntentStripePay()
            }

        }
    }



    fun presentPaymentSheet() {

        //check if google pay visibility is visbile or not

        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        googlepay_visibility = tinyDB.getString("googlepay_visibility").toString()

        if(googlepay_visibility.equals("VISIBLE")){ //VISIBLE

            googlePayConfig = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Production, //Use "Environment.Test" for testing purpose
                countryCode = "AE")

            billingDetails = PaymentSheet.BillingDetails(address = PaymentSheet.Address(country = "AE"),)

            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                PaymentSheet.Configuration(
                    merchantDisplayName = "Reloop",
                    customer = customerConfig,
                    googlePay = googlePayConfig,

                    defaultBillingDetails = billingDetails,

                    // Set `allowsDelayedPaymentMethods` to true if your business handles
                    // delayed notification payment methods like US bank accounts.
                    allowsDelayedPaymentMethods = true
                )
            )

        }else{ //IN_VISIBLE

            billingDetails = PaymentSheet.BillingDetails(
                address = PaymentSheet.Address(
                    country = "AE"
                ),
            )

            paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                PaymentSheet.Configuration(
                    merchantDisplayName = "Reloop",
                    customer = customerConfig,
                    defaultBillingDetails = billingDetails,

                    // Set `allowsDelayedPaymentMethods` to true if your business handles
                    // delayed notification payment methods like US bank accounts.
                    allowsDelayedPaymentMethods = true
                )
            )
        }

    }

    fun presentPaymentSheetMonthly() {

        PaymentConfiguration.init(requireContext(), Constants.PK_STRIPE)

        //check if google pay visibility is visbile or not

        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        googlepay_visibility = tinyDB.getString("googlepay_visibility").toString()

        if(googlepay_visibility.equals("VISIBLE")){ //VISIBLE

            googlePayConfig = PaymentSheet.GooglePayConfiguration(
                environment = PaymentSheet.GooglePayConfiguration.Environment.Production, //Use "Environment.Test" for testing purpose
                countryCode = "AE"
            )

            billingDetails = PaymentSheet.BillingDetails(
                address = PaymentSheet.Address(
                    country = "AE"
                ),
            )

            paymentSheet.presentWithPaymentIntent(
                pi_monthly_service,
                PaymentSheet.Configuration(
                    merchantDisplayName = "Reloop",
                    customer = customerConfig,
                    googlePay = googlePayConfig,

                    defaultBillingDetails = billingDetails,

                    // Set `allowsDelayedPaymentMethods` to true if your business handles
                    // delayed notification payment methods like US bank accounts.
                    allowsDelayedPaymentMethods = true
                )
            )

        }else{ //IN_VISIBLE

            billingDetails = PaymentSheet.BillingDetails(
                address = PaymentSheet.Address(country = "AE"),)

            paymentSheet.presentWithPaymentIntent(
                pi_monthly_service,
                PaymentSheet.Configuration(
                    merchantDisplayName = "Reloop",
                    customer = customerConfig,
                    defaultBillingDetails = billingDetails,

                    // Set `allowsDelayedPaymentMethods` to true if your business handles
                    // delayed notification payment methods like US bank accounts.
                    allowsDelayedPaymentMethods = true
                )
            )
        }

    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
                Log.d("PAYMENT_INTENT"," Monthly Service : TRANSACTION id Canceled : "+ paymentIntentIdStripe)
                callPaymentFailApi(paymentIntentIdStripe,"cancelled")
            }
            is PaymentSheetResult.Failed -> {
                print("Error: ${paymentSheetResult.error}")
                Log.d("PAYMENT_INTENT"," Monthly Service : TRANSACTION id Failed : "+ paymentIntentIdStripe)
                callPaymentFailApi(paymentIntentIdStripe,"failed")
            }
            is PaymentSheetResult.Completed -> {
                print("Completed")


                if(serviceType==1 || serviceType==4) { //for MonthlyService (1) For YearlyService(4)
                    //SEND same data for monthly and yearly
                    Log.d("PAYMENT_INTENT"," Monthly Service : TRANSACTION id : "+ paymentIntentIdStripe)
                    buyPlan?.transaction_id = paymentIntentIdStripe
                    buyPlan?.payment_method = ""
                    buyPlan?.is_monthly = 1
                    buyPlan?.stripe_subscription_id = stripe_subscription_id

                    if (childToParent != null) {
                        childToParent?.callParent(buyPlan)
                    }
                }else{ //onetime service and product
                    getStripePaymentMethodID()
                }
            }
        }
    }

    private fun callPaymentFailApi(paymentIntentIdStripe: String, status: String) {
        val details: Payment? = Payment()
        details?.payment_intend_id = paymentIntentIdStripe
        details?.status = status
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.UPDATE_PAYMENT_INTEND)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.updatepaymentIntend(details))
            ?.execute()
    }

    private fun getPaymentIntentGooglePay(){
        Log.d("CLIENT_SEC"," TOTAL AMOUNT : "+ totalAmount)

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PAYMENT_INTENT)
            ?.enque(Network().apis()?.getPaymentIntentGooglePay(totalAmount))
            ?.execute()
    }

    private fun getPaymentIntentStripePay(){
        Log.d("CLIENT_SEC"," TOTAL AMOUNT : "+ totalAmount)

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PAYMENT_INTENT_STRIPE)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPaymentIntentStripePay(totalAmount))
            ?.execute()
    }

    private fun onGooglePayReady(isReady: Boolean) {
        // implemented below
        mGooglePayButton!!.isEnabled = isReady
    }

    private fun onGooglePayResult(result: GooglePayLauncher.Result) {

        // implemented below
        when (result) {
            GooglePayLauncher.Result.Completed -> {
                // Payment succeeded, show a receipt view
                if(buyProduct != null) {
                    //buyProduct?.products = java.util.ArrayList()
                    if (HomeActivity.cartList?.indices != null) {
                        for (i in HomeActivity.cartList?.indices!!) {
                            val product = Product()
                            product.id = HomeActivity.cartList?.get(i)?.id
                            product.qty = HomeActivity.cartList?.get(i)?.quantity
                            //buyProduct?.products?.add(product)
                        }
                    }
                    var deliveryFeeString = ""
                    if (CartInformationFragment.deliveryFeeValue != -1) {
                        deliveryFeeString = "&" + CartInformationFragment.deliverFeeKey + "=${CartInformationFragment.deliveryFeeValue}"
                        buyProduct?.delivery_fee = deliveryFeeString
                    }
                    buyProduct?.transaction_id = paymentIntentIdStripe
                    buyProduct?.payment_method = ""

                    Log.e("TAG","===BuyProduct======" + Gson().toJson(buyProduct))

//                    buyProduct?.payment_type = "google-pay"

                    /*var URL = "card_number=${buyProduct?.card_number}&expiry_date=${buyProduct?.exp_year + buyProduct?.exp_month}" +
                            "&card_security_code=${buyProduct?.cvv.toString()}&is_new_card=${buyProduct?.is_new_card}" +
                            "&user_id=${User.retrieveUser()?.id.toString()}&total=${buyProduct?.total.toString()}&delivery_fee=${buyProduct?.delivery_fee}" +
                            "&coupon_id=${buyProduct?.coupon_id.toString()}&subtotal=${buyProduct?.subtotal.toString()}&points_discount=${buyProduct?.points_discount.toString()}&first_name=${buyProduct?.first_name.toString()}&" +
                            "last_name=${buyProduct?.last_name.toString()}&email=${buyProduct?.email.toString()}&phone_number=${buyProduct?.phone_number.toString()}" +
                            "&location=${buyProduct?.location.toString().replace(",", "")}&latitude=${buyProduct?.latitude.toString()}&longitude=${buyProduct?.longitude.toString()}" +
                            "&city_id=${buyProduct?.city_id.toString()}&district_id=${buyProduct?.district_id.toString()}&organization_name=${buyProduct?.organization_name.toString()}"+"&map_location=${buyProduct?.map_location.toString()}"+"&user_card_id=${buyProduct?.user_card_id.toString()}" +
                                  "&transaction_id=${buyProduct?.transaction_id}"

                    for (i in 0 until buyProduct?.products?.size!!) {
                        URL += "&products[${i}][id]=${buyProduct?.products?.get(i)?.id.toString()}"
                        URL += "&products[${i}][qty]=${buyProduct?.products?.get(i)?.qty.toString()}"
                    }
*/

                    /*NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.BUY_PRODUCT)
                        ?.autoLoading(requireActivity())
                        ?.enque(Network().apis()?.buyProductNew(buyProduct, hashMap))
                        ?.execute()*/

                } else{

                    //original
                    /*val buyPlanMap = HashMap<String, Any?>()
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


                    buyPlan?.transaction_id = paymentIntent
                    buyPlan?.payment_type = "google-pay"

                    val URL = "card_number=${buyPlan?.card_number}&expiry_date=${buyPlan?.exp_year + buyPlan?.exp_month}" +
                            "&card_security_code=${buyPlan?.cvv.toString()}&is_new_card=${buyPlan?.is_new_card}" +
                            "&user_id=${User.retrieveUser()?.id.toString()}&total=${buyPlan?.total.toString()}" +
                            "&coupon_id=${buyPlan?.coupon_id.toString()}&subtotal=${""}&subscription_id=${buyPlan?.subscription_id.toString()}" +
                            "&subscription_type=${buyPlan?.subscription_type.toString()}" + "&plan_id=${buyPlan?.plan_id}" +"&transaction_id=${buyPlan?.transaction_id}" +"&payment_type=${buyPlan?.payment_type}"


                    Log.e(ProductPurchasingFragment.TAG,"====SECURITY CODE SERVICE===" +buyPlan?.cvv.toString())


                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.BUY_SERVICE)
                        ?.autoLoading(requireActivity())
                        ?.enque(Network().apis()?.buyService(buyPlan))
                        ?.execute()
*/
                    //new
                    getPlanStatus()

                }

            }
            GooglePayLauncher.Result.Canceled -> {
                // User canceled the operation
            }
            is GooglePayLauncher.Result.Failed -> {
                // Operation failed; inspect `result.error` for the exception
            }
        }
    }

    fun getPlanStatus() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN_STATUS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlanStatus())
            ?.execute()
    }

    fun getMonthlyServiceIntent() {

        if(buyPlan!=null){
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.MONTHLY_SERVICE_PAYMENT_INTENT)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getMonthlyServicePaymentIntent(
                    buyPlan!!.subscription_id,
                    buyPlan!!.coupon_id))
                ?.execute()
        }
    }

    private fun initViews(view: View?) {

        //GOOGLE PAY
        mGooglePayButton = view?.findViewById(R.id.googlePayButton)

        //STRIPE PAY
        stripePayButton = view?.findViewById(R.id.stripePayButton)
        stripePayButton!!.visibility = View.GONE

        stripePayButton?.text = "Pay $totalAmount ${Constants.currencySign}"

        txtAddressName = view?.findViewById(R.id.txtAddressName)
        txtAddressEmail = view?.findViewById(R.id.txtAddressEmail)
        txtAddressPhone = view?.findViewById(R.id.txtAddressPhone)
        txtAddressAddress = view?.findViewById(R.id.txtAddressAddress)

        subTotalPrice= view?.findViewById(R.id.subTotalPrice)
        discountedPrice= view?.findViewById(R.id.discountedPrice)
        deliveryFee= view?.findViewById(R.id.deliveryFee)
        totalPrice= view?.findViewById(R.id.totalPrice)
        discount_label= view?.findViewById(R.id.discount_label)

        llProductData = view?.findViewById(R.id.llProductData)
        cardServiceData = view?.findViewById(R.id.cardServiceData)
        heading_selected_service = view?.findViewById(R.id.heading_selected_service)

        imageView_category_icon = view?.findViewById(R.id.imageView_category_icon)
        info_button = view?.findViewById(R.id.info_button)
        tv_heading_category = view?.findViewById(R.id.tv_heading_category)
        tv_description = view?.findViewById(R.id.tv_description)



        cardNumber = view?.findViewById(R.id.card_number)
        cardExpiry = view?.findViewById(R.id.card_expiry)
        cardCVV = view?.findViewById(R.id.card_cvv)
        cardHolder = view?.findViewById(R.id.card_holder)
        applyCoupon = view?.findViewById(R.id.applyCoupon)
        applyCouponService = view?.findViewById(R.id.applyCouponService)
        pinView = view?.findViewById(R.id.pin_view)
        pinViewService = view?.findViewById(R.id.pin_view_service)
        headingDiscountCoupon = view?.findViewById(R.id.heading_discount_coupon)
        rememberMe = view?.findViewById(R.id.remember_me)
        message = view?.findViewById(R.id.message)
        rememberMeService = view?.findViewById(R.id.remember_me_service)
        messageService = view?.findViewById(R.id.message_service)

        if (buyPlan?.subscription_type == 1){
            rememberMe?.visibility = View.VISIBLE
            message?.visibility = View.VISIBLE
            message?.text = "* You can stop the subscription at any time!"

            rememberMeService?.visibility = View.VISIBLE
            messageService?.visibility = View.VISIBLE
            messageService?.text = "* You can stop the subscription at any time!"

        }

        //payment methods
        tvcardNo = view?.findViewById(R.id.tv_card_no)
        rv_pm = view?.findViewById(R.id.rv_paymentmethods)
        tvNopm = view?.findViewById(R.id.tvNoPaymentMethods)
        lllist = view?.findViewById(R.id.lllist)
        rlPaymentMethod = view?.findViewById(R.id.rlPaymentMethod)
        frmCard = view?.findViewById(R.id.frmCard)
        iv_card = view?.findViewById(R.id.iv_card)
        rlAddBillingPaymentMethod = view?.findViewById(R.id.rlAddBillingPaymentMethod)
        radio_button = view?.findViewById(R.id.radio_button)
        default_card_details = view?.findViewById(R.id.default_card_details)
        txtOtherSavedMethods = view?.findViewById(R.id.txtOtherSavedMethods)


        relPaymentMethodBack = view?.findViewById(R.id.relPaymentMethodBack)
        llCoupanCodeservice = view?.findViewById(R.id.llCoupanCodeservice)

    }

    private fun setListeners() {
        applyCoupon?.setOnClickListener(this)
        applyCouponService?.setOnClickListener(this)
        rlPaymentMethod?.setOnClickListener(this)
        rlAddBillingPaymentMethod?.setOnClickListener(this)
        relPaymentMethodBack?.setOnClickListener(this)
        info_button?.setOnClickListener(this)
    }

    private fun setBillingDetailData(){

        try {
            if(buyProduct == null){

                //DISCOUNT ADDED FOR MONTHLY and YEARLY services only
                val tinyDB: TinyDB?
                tinyDB = TinyDB(MainApplication.applicationContext())
                hhDiscount_ID = tinyDB.getString("hhDiscount_ID").toString()
                hhDiscount_PR = tinyDB.getString("hhDiscount_PR").toString()
                hhDiscount_CODE = tinyDB.getString("hhDiscount_CODE").toString()


                if(serviceType==1 || serviceType==4) {// MONTHLY AND YEARLY

                    if (hhDiscount_ID.equals("NULL",ignoreCase = true)){
                        if(!totalAmount.isNullOrEmpty()){
                            tv_heading_category!!.setText(totalAmount+" "+ Constants.currencySign)
                        }
                    }else{

                        if(!totalAmount.isNullOrEmpty()){

                            buyPlan?.hh_discount_id = hhDiscount_ID
                            applyCouponCodeForHHDiscount(hhDiscount_CODE)

                            if(!totalAmount.isNullOrEmpty()){
                                //tv_heading_category!!.setText("$totalAmount\n${Constants.currencySign}")
                                tv_heading_category!!.setText(totalAmount+" "+ Constants.currencySign)
                            }

//                            val originalPrice = totalAmount!!.toDoubleOrNull()
//                            val discountPercentage = hhDiscount_PR.toDoubleOrNull()
//                            if (originalPrice != null && discountPercentage != null) {
//                                val discountedPrice = originalPrice * (1 - (discountPercentage / 100))
//
//                                val spannableOriginalPrice = SpannableString(totalAmount)
//                                spannableOriginalPrice.setSpan(StrikethroughSpan(), 0, totalAmount!!.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                                val discountedPriceString = String.format("%.2f", discountedPrice)
//
//                                val finalString = SpannableStringBuilder()
//                                    .append(spannableOriginalPrice)
//                                    .append("\n")
//                                    .append(discountedPriceString)
//                                    .append("\n")
//                                    .append("${Constants.currencySign}")
//
//                                tv_heading_category!!.setText(finalString)
//
//                            }

                        }

                    }

                }else{
                    if(!totalAmount.isNullOrEmpty()){
                        //tv_heading_category!!.setText("$totalAmount\n${Constants.currencySign}")
                        tv_heading_category!!.setText(totalAmount+" "+ Constants.currencySign)
                    }
                }

                /*if(!totalAmount.isNullOrEmpty()){
                    tv_heading_category!!.setText("$totalAmount\n${Constants.currencySign}")
                }*/


                llProductData!!.visibility = View.GONE
                cardServiceData!!.visibility = View.VISIBLE
                heading_selected_service!!.visibility = View.VISIBLE

                if(!planTripValue.isNullOrEmpty()){
                    tv_description!!.setText(planTripValue)
                }

                if(!planImage.isNullOrEmpty()){

                    planIcon.let {
                        Utils.glideImageLoaderServer(
                            imageView_category_icon,
                            planImage, it
                        )
                    }
                }

            }else{

                Log.d("PRODUCT_MODEL",""+GsonBuilder().setPrettyPrinting().create().toJson(buyProduct))

                llProductData!!.visibility = View.VISIBLE
                cardServiceData!!.visibility = View.GONE
                heading_selected_service!!.visibility = View.GONE
                //Address
                if(!buyProduct!!.first_name.isNullOrEmpty() && !buyProduct!!.last_name.isNullOrEmpty()){
                    txtAddressName!!.text = buyProduct!!.first_name + " " + buyProduct!!.last_name
                }

                if(!buyProduct!!.email.isNullOrEmpty()){
                    txtAddressEmail!!.text = buyProduct!!.email
                }

                if(!buyProduct!!.phone_number.isNullOrEmpty()){
                    txtAddressPhone!!.text = buyProduct!!.phone_number
                }

                if(!buyProduct!!.location.isNullOrEmpty() && !buyProduct!!.map_location.isNullOrEmpty()){
                    txtAddressAddress!!.text = buyProduct!!.location + " "+ buyProduct!!.map_location
                }

                //Order Summary

/*                if(buyProduct!!.subtotal != null){
                    subTotalPrice!!.text = "${Utils.commaConversion(buyProduct!!.subtotal)} ${Constants.currencySign}"
                }*/

                var subtotal = 0.0
                var discountedprice  = 0.0
                var deliveryFees  = 0

                if(CartInformationFragment.billingSubTotal != null){
                    subTotalPrice?.text = "${Utils.commaConversion(CartInformationFragment.billingSubTotal)} ${Constants.currencySign}"
                    subtotal = CartInformationFragment.billingSubTotal!!

                }

                if (CartInformationFragment.deliveryFeeValue != -1) {
                    deliveryFee!!.text = "${Utils.commaConversion(CartInformationFragment.deliveryFeeValue)} ${Constants.currencySign}"
                    deliveryFees  = CartInformationFragment.deliveryFeeValue

                }

                if(CartInformationFragment.billingDiscountAmount!=null){
                    discountedPrice?.text = "${Utils.commaConversion(CartInformationFragment.billingDiscountAmount)} ${Constants.currencySign}"
                    discountedprice  = CartInformationFragment.billingDiscountAmount!!
                }

                if(buyProduct!!.total != null){

                    if (buyProduct!!.total!! <= 0) {
                        totalPrice!!.text = "${Utils.commaConversion(1.0)} ${Constants.currencySign}"
                    }
                    else if(buyProduct!!.total!! < 2 || buyProduct!!.total == 2.0){
                        totalPrice!!.text = "${Utils.commaConversion(2.0)} ${Constants.currencySign}"
                    }
                    else {
                        val finaltotal = subtotal.minus(discountedprice) + deliveryFees
                        //totalPrice!!.text = "${Utils.commaConversion(buyProduct!!.total)} ${Constants.currencySign}"
                        totalPrice!!.text = "${Utils.commaConversion(finaltotal)} ${Constants.currencySign}"
                    }
                }

            }
        }catch (e: Exception){
            e.printStackTrace()
        }


    }

    @SuppressLint("SetTextI18n")
    private fun populateData() {
        cardExpiry?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, start: Int, removed: Int, added: Int) {
                if (start == 1 && start + added == 2 && p0?.contains('/') == false) {
                    cardExpiry?.setText("$p0/")
                } else if (start == 3 && start - removed == 2 && p0?.contains('/') == true) {
                    cardExpiry?.setText(p0.toString().replace("/", ""))
                }
                cardExpiry?.setSelection(cardExpiry?.text!!.length)
            }
        })
        //--------------For Testing------------
//            cardNumber?.setText("4111111111111111")
//            cardExpiry?.setText("05/21")
//            cardCVV?.setText("123")

        if (buyPlan == null) {
            headingDiscountCoupon?.visibility = View.GONE
            pinView?.visibility = View.GONE
            applyCoupon?.visibility = View.GONE

        }

        if(buyProduct == null){
            //relPaymentMethodBack?.visibility = View.GONE // change

            llCoupanCodeservice?.visibility = View.VISIBLE // Coupan Code visible for service purchase
        }else{
            //relPaymentMethodBack?.visibility = View.VISIBLE // change

            llCoupanCodeservice?.visibility = View.GONE // Coupan code not visible for product purchase
        }


        rememberMe?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                Notify.alerterRed(activity, "Auto-renew is required to proceed")
                rememberMe?.isChecked  = true
            }
        }

        rememberMeService?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                Notify.alerterRed(activity, "Auto-renew is required to proceed")
                rememberMeService?.isChecked  = true
            }
        }

        /*radio_button?.setOnClickListener{

        }*/

        default_card_details?.setOnClickListener{

            if(radio_button?.isChecked == false){
                radio_button?.isChecked = true

                paymentMethodType = "defaultCard"
                rcvCardStatus = "defaultCard"
                if(pmListNew!=null){
                    populateRecyclerViewData(pmListNew!!)
                }
            }
        }

        //payment methods
        pmListNew?.clear()
        /*NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.PAYMENT_METHOD_LISTING)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.paymentMethodListing())
            ?.execute()*/

//        getSavedCards()

    }

    fun getSavedCards(){
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_SAVED_CARDS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getSavedCards())
            ?.execute()
    }

    override fun callChild() {

        Log.d("paymentMethod0",""+paymentMethodType)

        //NEW STRIPE PAYMENT SHEET
        if (buyProduct == null) {

            if (childToParent != null) {
                childToParent?.callParent(buyPlan)
            }
        }else{

            if (childToParent != null) {
                childToParent?.callParent(buyProduct)
            }
        }

        //OLD CARD PAYMENT
        /*if(paymentMethodType.equals("defaultCard",ignoreCase = true)){

            if (buyProduct == null) {

                //default card SERVICE PURCHASE
                val delimiterSlash = "/"
                val defaultDateExpiry = defaultCardExpiry.split(delimiterSlash)
                buyPlan?.user_card_id = defaultCardId  // for selected card from the list
                buyPlan?.card_number = defaultCardNumber
                buyPlan?.exp_month = defaultDateExpiry[0]
                buyPlan?.exp_year = defaultDateExpiry[1]
                buyPlan?.is_new_card = "0"


                if (childToParent != null) {
                    childToParent?.callParent(buyPlan)
                }
            }else{
                //default card
                val delimiterSlash = "/"
                val defaultDateExpiry = defaultCardExpiry.split(delimiterSlash)
                buyProduct?.user_card_id = defaultCardId  // for selected card from the list
                buyProduct?.card_number = defaultCardNumber
                buyProduct?.exp_month = defaultDateExpiry[0]
                buyProduct?.exp_year = defaultDateExpiry[1]
                buyProduct?.is_new_card = "0"

                if (childToParent != null) {
                    childToParent?.callParent(buyProduct)
                }
            }


        } else if(paymentMethodType.equals("selectedCard",ignoreCase = true)){


            if (buyProduct == null) {

                //selected from the list
                val delimiterSlash = "/"
                val selectedDateExpired = selectedCardExpiry.split(delimiterSlash)
                buyPlan?.user_card_id = selectedCardId  // for selected card from the list
                buyPlan?.card_number = selectedCardNumber
                buyPlan?.exp_month = selectedDateExpired[0]
                buyPlan?.exp_year = selectedDateExpired[1]
                buyPlan?.is_new_card = "0"

                if (childToParent != null) {
                    childToParent?.callParent(buyPlan)
                }
            }else{

                //selected from the list
                val delimiterSlash = "/"
                val selectedDateExpired = selectedCardExpiry.split(delimiterSlash)
                buyProduct?.user_card_id = selectedCardId  // for selected card from the list
                buyProduct?.card_number = selectedCardNumber
                buyProduct?.exp_month = selectedDateExpired[0]
                buyProduct?.exp_year = selectedDateExpired[1]
                buyProduct?.is_new_card = "0"

                if (childToParent != null) {
                    childToParent?.callParent(buyProduct)
                }
            }



        }else if (paymentMethodType.equals("newCard",ignoreCase = true)){
            //payment with new card

            val auhSuccessful = BillingInformationAuth.authenticate(
                cardNumber?.text.toString(), cardExpiry?.text.toString()
                , cardCVV?.text.toString(), requireActivity()
            )
            Log.e("TAG","====cvv1=====" + cardCVV?.text.toString())

            if (auhSuccessful) {
                val delimiter1 = "/"
                val dateExpiry = cardExpiry?.text.toString().split(delimiter1)
                if (buyProduct == null) {
                    buyPlan?.card_number = cardNumber?.text.toString()
                    buyPlan?.cvv = cardCVV?.text.toString()
                    buyPlan?.card_holder = cardHolder?.text.toString()

                    Log.e("TAG","====cvv2=====" + cardCVV?.text.toString())
                    buyPlan?.exp_month = dateExpiry[0]
                    buyPlan?.exp_year = dateExpiry[1]
                    if (childToParent != null) {
                        childToParent?.callParent(buyPlan)
                    }
                } else {
                    buyProduct?.card_number = cardNumber?.text.toString()
                    buyProduct?.cvv = cardCVV?.text.toString()
                    buyProduct?.card_holder = cardHolder?.text.toString()
                    Log.e("TAG","====cvv3=====" + cardCVV?.text.toString())
                    buyProduct?.exp_month = dateExpiry[0]
                    buyProduct?.exp_year = dateExpiry[1]


                    if (childToParent != null) {
                        childToParent?.callParent(buyProduct)
                    }
                }
            }
        }*/


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.applyCoupon -> {
                if (pinView?.text?.isEmpty() == true || pinView?.text?.length!! < 5) {
                    if (pinView?.text.toString().isEmpty()) {
                        Notify.alerterRed(activity, "Enter Coupon Code")
                    } else {
                        Notify.alerterRed(activity, "Coupon Code is Not Valid")
                    }
                } else {
                    val category: ArrayList<CouponCategory>? = ArrayList()
                    val categoryCoupon = CouponCategory()
                    categoryCoupon.id = buyPlan?.subscription_type
                    categoryCoupon.type = Constants.serviceType
                    category?.add(categoryCoupon)
                    val couponVerification = CouponSendData()
                    couponVerification.coupon = pinView?.text.toString()
                    couponVerification.category = category
                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.COUPON_VERIFICATION)
                        ?.autoLoading(requireActivity())
                        ?.enque(Network().apis()?.couponVerification(couponVerification))
                        ?.execute()
                }
            }
            R.id.applyCouponService -> {


                if(serviceType==1 || serviceType==4) {// MONTHLY AND YEARLY
                    if (hhDiscount_ID.equals("NULL", ignoreCase = true)) {
                        if (pinViewService?.text?.isEmpty() == true || pinViewService?.text?.length!! < 5) {
                            if (pinViewService?.text.toString().isEmpty()) {
                                Notify.alerterRed(activity, "Enter Coupon Code")
                            } else {
                                Notify.alerterRed(activity, "Coupon Code is Not Valid")
                            }
                        } else {
                            val category: ArrayList<CouponCategory>? = ArrayList()
                            val categoryCoupon = CouponCategory()
                            categoryCoupon.id = buyPlan?.subscription_type
                            categoryCoupon.type = Constants.serviceType
                            category?.add(categoryCoupon)
                            val couponVerification = CouponSendData()
                            couponVerification.coupon = pinViewService?.text.toString()
                            couponVerification.category = category
                            NetworkCall.make()
                                ?.setCallback(this)
                                ?.setTag(RequestCodes.API.COUPON_VERIFICATION)
                                ?.autoLoading(requireActivity())
                                ?.enque(Network().apis()?.couponVerification(couponVerification))
                                ?.execute()
                        }
                    }else{
                        Notify.alerterRed(activity, "Coupon code is already applied on this service")
                    }
                }else{
                    if (pinViewService?.text?.isEmpty() == true || pinViewService?.text?.length!! < 5) {
                        if (pinViewService?.text.toString().isEmpty()) {
                            Notify.alerterRed(activity, "Enter Coupon Code")
                        } else {
                            Notify.alerterRed(activity, "Coupon Code is Not Valid")
                        }
                    } else {
                        val category: ArrayList<CouponCategory>? = ArrayList()
                        val categoryCoupon = CouponCategory()
                        categoryCoupon.id = buyPlan?.subscription_type
                        categoryCoupon.type = Constants.serviceType
                        category?.add(categoryCoupon)
                        val couponVerification = CouponSendData()
                        couponVerification.coupon = pinViewService?.text.toString()
                        couponVerification.category = category
                        NetworkCall.make()
                            ?.setCallback(this)
                            ?.setTag(RequestCodes.API.COUPON_VERIFICATION)
                            ?.autoLoading(requireActivity())
                            ?.enque(Network().apis()?.couponVerification(couponVerification))
                            ?.execute()
                    }
                }
            }

            R.id.info_button -> {

                if(!planImage.isNullOrEmpty() && !planTripValue.isNullOrEmpty() && !planDescription.isNullOrEmpty()){
                    AlertDialogs.informationDialog(
                        activity,
                        planImage,
                        planTripValue,
                        planDescription,
                        null,null
                    )
                }

            }

            /*R.id.rlPaymentMethod -> {
                rlPaymentMethod?.visibility = View.GONE
                frmCard?.visibility = View.VISIBLE
            }*/
            R.id.rlAddBillingPaymentMethod -> {
                paymentMethodType = "newCard"
                frmCard?.visibility = View.VISIBLE
                lllist?.visibility = View.GONE
            }
            R.id.relPaymentMethodBack -> {
                if(paymentMethodType.equals("newCard",ignoreCase = true)){
                    lllist?.visibility = View.VISIBLE
                    frmCard?.visibility = View.GONE

                    paymentMethodType = rcvCardStatus


                }/*else{ // change
                    rlPaymentMethod?.visibility = View.VISIBLE
                    frmCard?.visibility = View.GONE
                }*/

            }

        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.COUPON_VERIFICATION -> {
                val couponVerification = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>), CouponVerification::class.java)
                if (couponVerification?.validForCategory == null) {
                    Notify.alerterRed(activity, getString(R.string.caoupon_not_valid_for_this))
                    return
                } else {
                    if (!couponVerification?.validForCategory?.type.isNullOrEmpty()
                        && couponVerification?.validForCategory?.id.isNullOrEmpty()) {
                        val serviceType: Int? =
                            couponVerification?.validForCategory?.type?.find { it == Constants.serviceType }
                        val allTypes: Int? = couponVerification?.validForCategory?.type?.find { it == Constants.all }
                        if (serviceType != null || allTypes != null) {

                            if(hhDiscount_ID != null)
                            {
                                Notify.alerterGreen(activity, "Your organization discount is applied successfully!")
                            }
                            else {
                                Notify.alerterGreen(activity, baseResponse?.message)
                            }
                            setData(couponVerification)
                            disableApplyAndSendCallback()
                        }
                    }
                    else if (!couponVerification?.validForCategory?.id.isNullOrEmpty()
                        && couponVerification?.validForCategory?.type.isNullOrEmpty()) {
                        if (couponVerification?.validForCategory?.id?.get(0) == buyPlan?.subscription_type) {
                            setData(couponVerification)
                            disableApplyAndSendCallback()
                        }
                        else {
                            if (couponVerification?.validForCategory == null) {
                                Notify.alerterRed(activity, getString(R.string.caoupon_not_valid_for_this))
                            }
                        }
                    } else {
                        if (couponVerification?.validForCategory == null) {
                            Notify.alerterRed(activity, getString(R.string.caoupon_not_valid_for_this))
                        }
                    }
                }
            }
            RequestCodes.API.GET_PAYMENT_INTENT -> {
                val baseResponse = Utils.getBaseResponse(response)
                val dataParsing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    PaymentIntentModel::class.java
                )

                if(!dataParsing.payment_intend_client_secret.isNullOrEmpty()){
                    clientSecret = dataParsing.payment_intend_client_secret
                    paymentIntent = dataParsing.payment_intend_id.toString()
                    Log.d("CLIENT_SEC",""+dataParsing.payment_intend_client_secret)
                    requireActivity().runOnUiThread { mGooglePayButton!!.setEnabled(true)
                    }
                }
            }
            RequestCodes.API.MONTHLY_SERVICE_PAYMENT_INTENT -> {
                val baseResponse = Utils.getBaseResponse(response)
                val dataParsing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    PaymentIntentModel::class.java
                )

                Log.d("MONTHLY_TEST","111")

                if(baseResponse!!.status == true){
                    Log.d("MONTHLY_TEST","222")
                    if(!dataParsing.client_secret.isNullOrEmpty()){
                        Log.d("MONTHLY_TEST","333")
                        pi_monthly_service = dataParsing.client_secret.toString()
                        paymentIntentIdStripe = dataParsing.pi_id_monthly.toString()
                        stripe_subscription_id = dataParsing.stripe_subscription_id.toString()

                        customerConfig = PaymentSheet.CustomerConfiguration(
                            dataParsing.stripe_customer_id.toString(),
                            dataParsing.ephemeralKey_secret.toString()
                        )
                        Log.d("MONTHLY_TEST",""+pi_monthly_service)

                        if(!pi_monthly_service.isEmpty()){
                            presentPaymentSheetMonthly()
                        }
                    }else{

                        try {
                            if(!dataParsing.stripe_error.isNullOrEmpty()){
                                Notify.alerterRed(
                                    activity, dataParsing.stripe_error.get(0).toString()
                                )
                            }
                        }catch (e: Exception){
                            e.printStackTrace()
                        }

                    }
                }
            }

            RequestCodes.API.GET_PAYMENT_INTENT_STRIPE -> {
        val baseResponse = Utils.getBaseResponse(response)
        val dataParsing = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), PaymentIntentModel::class.java)

        if(!dataParsing.payment_intend_client_secret.isNullOrEmpty()){
//                    clientSecret = dataParsing.payment_intend_client_secret
//                    paymentIntent = dataParsing.payment_intend_id.toString()
//                    Log.d("CLIENT_SEC",""+dataParsing.payment_intend_client_secret)
//                    requireActivity().runOnUiThread { mGooglePayButton!!.setEnabled(true)
//                    }

                    paymentIntentIdStripe = dataParsing.payment_intend_id.toString()
                    paymentIntentClientSecret = dataParsing.payment_intend_client_secret
                    customerConfig = PaymentSheet.CustomerConfiguration(
                        dataParsing.stripe_customer_id.toString(),
                        dataParsing.ephemeralKey_secret.toString()
                    )

                    PaymentConfiguration.init(requireContext(),Constants.PK_STRIPE)

                    if(!paymentIntentClientSecret.isEmpty()){
                        presentPaymentSheet()
                    }

                }
            }

            RequestCodes.API.GET_SAVED_CARDS ->{
                val baseResponse = Utils.getBaseResponse(response)

                savedCardList = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    SavedCard::class.java)


                Log.d(PaymentMethodsFragment.TAG,"SAVED_CARDS" + GsonBuilder().setPrettyPrinting().create().toJson(savedCardList))


            }

            //payment methods
            RequestCodes.API.PAYMENT_METHOD_LISTING -> {
                val baseResponse = Utils.getBaseResponse(response)
                Log.e(PaymentMethodsFragment.TAG,"===response===" + baseResponse)
                val gson = Gson()
                val listType: Type = object : TypeToken<List<PaymentMethods?>?>() {}.type
                pmList = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)

                Log.e(PaymentMethodsFragment.TAG,"===payment method list===" + pmList?.size)

                if(pmList?.size!! > 0) {

                    lllist?.visibility = View.VISIBLE

                    //rlPaymentMethod?.visibility = View.GONE //change

                    relPaymentMethodBack?.visibility = View.VISIBLE // change

                    paymentMethodType = "defaultCard"
                    rcvCardStatus = "defaultCard"


//                    tvNopm?.visibility = View.GONE

                    //update?.visibility = View.VISIBLE

                    for (i in pmList!!.indices) {
                        if (pmList!![i].getIsDefault() == 1) {
                            Log.e(PaymentMethodsFragment.TAG,"===default card id===" + pmList!![i].getId())

                            //set default card values
                            defaultCardId = pmList!![i].getId().toString()
                            defaultCardExpiry = pmList!![i].getExpiryDate().toString()
                            defaultCardNumber = pmList!![i].getNumber().toString()

                            tvcardNo?.setText("**** **** **** " + pmList!![i].getNumber())
                            pmListNew?.remove(pmList!![i])

                            if(pmList!![i].getBrand().equals("visa", ignoreCase = true)){
                                iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_visa_card))
                            }else if(pmList!![i].getBrand().equals("MasterCard", ignoreCase = true)){
                                iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_master_card))
                            }else{
                                iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_card_grey))
                            }
                        }
                        else{
                            pmListNew?.add(pmList!![i])
                        }
                    }
                    Log.e(PaymentMethodsFragment.TAG,"===payment method list new ===" + pmListNew?.size)
                    populateRecyclerViewData(pmListNew!!)

                } else{

                    //Notify.alerterRed(activity, "No record found!")
                    lllist?.visibility = View.GONE
//                    tvNopm?.visibility = View.VISIBLE

                    //update?.visibility = View.GONE

                    paymentMethodType = "newCard"
                    rcvCardStatus = "newCard"

                    //new added ADDD

                    //original
                    /*if(buyProduct==null){
                        rlPaymentMethod?.visibility = View.GONE
                        frmCard?.visibility = View.VISIBLE
                    }else{
                        rlPaymentMethod?.visibility = View.VISIBLE
                    }*/

                    //new
                    frmCard?.visibility = View.VISIBLE

                    relPaymentMethodBack?.visibility = View.GONE // change

                }

                if(serviceType == 2 || serviceType == 0) {

                    // ORIGINAL GOOGLE PAY===========START===============
                    /*mGooglePayButton?.visibility = View.VISIBLE
                    getPaymentIntentGooglePay()*/
                    // ORIGINAL GOOGLE PAY==========END================



                    // ORIGINAL STRIPE PAY===========START===============


//                    getPaymentIntentStripePay()
                    // ORIGINAL GOOGLE PAY==========END================



                } else{
                    mGooglePayButton?.visibility = View.GONE
                }
            }

            RequestCodes.API.BUY_PRODUCT -> {
                try {
                    val baseResponse = Utils.getBaseResponse(response)
                    val dataParsing = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        DataParsing::class.java
                    )

                    val fragment = ConfirmationSubscriptionFragment.newInstance()
                    val args = Bundle()
                    args.putString(
                        Constants.DataConstants.purchaseID,
                        dataParsing.buyProductId?.get(0)
                    )
                    args.putInt(
                        Constants.DataConstants.subscriptionCycle,
                        Constants.subscriptionCycleOne
                    )
                    buyProduct?.points_discount?.let {
                        args.putInt(
                            Constants.DataConstants.rewardPoints,
                            it.toInt()
                        )
                    }
                    fragment.arguments = args

                    BaseActivity.replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerNewBilling,
                        fragment,
                        Constants.TAGS.ConfirmationSubscriptionFragment
                    )


                    HomeActivity.cartList?.clear()
                    HomeActivity.refreshCart()
                    CartInformationFragment.deliverFeeKey = ""
                    CartInformationFragment.deliveryFeeValue = -1
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

            RequestCodes.API.BUY_SERVICE -> {
                Log.e("TAG","===buy plan CALLED=== NEW BILLING")


                val baseResponse = Utils.getBaseResponse(response)
                val dataParsing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    DataParsing::class.java)

                    Notify.alerterGreen(activity, getString(R.string.str_payment_charged_success))

                    Log.e("TAG","===onActivityResult else ===")

                    llrecycle?.visibility = View.GONE
                    HomeActivity.clearAllFragments(true)

                    back?.visibility = View.GONE
                    next?.visibility = View.GONE
                    stepView.visibility = View.GONE
                    create?.visibility = View.GONE

                    LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                    checkUserInfo()

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
                Log.e("TAG","===response1=== G_P_S" + getPlans)
                getPlansAll = getPlans

                if(getPlans.userPlans != null && getPlans.userPlans!!.isEmpty()) {

                    //for monthly service old flow -- double payment happend in this
                    // from backend
//                    getPaymentIntentStripePay()

                    //From front-end
//                    createPaymentIntentStripe()

                    //new flow CardInputWidget
                    /*val subscriptionBottomsheet: SubscriptionBottomsheet = SubscriptionBottomsheet().newInstance(this,"Payment Method",savedCardList)
                    subscriptionBottomsheet.show(childFragmentManager, subscriptionBottomsheet.tag)*/

                    //Specifically monthly service intent
                    getMonthlyServiceIntent()

                } else{
                    handleUserPlansScenario(getPlans,"GET_PLAN_STATUS")
                }
            }

        }
    }

    private fun applyCouponCodeForHHDiscount(hhDiscount_CODE: String){
        val category: ArrayList<CouponCategory>? = ArrayList()
        val categoryCoupon = CouponCategory()
        categoryCoupon.id = buyPlan?.subscription_type
        categoryCoupon.type = Constants.serviceType
        category?.add(categoryCoupon)
        val couponVerification = CouponSendData()
        couponVerification.coupon = hhDiscount_CODE
        couponVerification.category = category
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.COUPON_VERIFICATION)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.couponVerification(couponVerification))
            ?.execute()
    }

    private fun populateRecyclerViewData(pmList: ArrayList<PaymentMethods>) {
        linearLayoutManager = LinearLayoutManager(context)
        rv_pm?.layoutManager = linearLayoutManager
        billingAdapter = BillingPaymentMethodsAdapter(requireContext(), pmList)
        rv_pm?.adapter = billingAdapter
        billingAdapter!!.setClicklistner(this)

        if(pmList.size > 0){
            txtOtherSavedMethods?.visibility = View.VISIBLE
        }else{
            txtOtherSavedMethods?.visibility = View.GONE
        }

    }

    override fun itemclick(position: Int,paymentMethods: PaymentMethods) {
        paymentMethodType = "selectedCard"
        rcvCardStatus = "selectedCard"

        Log.e(PaymentMethodsFragment.TAG,"===item click id===" + pmList?.get(position)?.getId().toString())
        //selectedCardId = pmList?.get(position)?.getId().toString() // old
        selectedCardId = paymentMethods.getId().toString() // new

        //new added AD
//        selectedCardNumber = pmList?.get(position)?.getNumber().toString()
//        selectedCardExpiry = pmList?.get(position)?.getExpiryDate().toString()

        selectedCardNumber = paymentMethods.getNumber().toString()
        selectedCardExpiry = paymentMethods.getExpiryDate().toString()

        radio_button?.isChecked = false

    }

    override fun deletePaymentMethod(position: Int) {
        Log.e(PaymentMethodsFragment.TAG,"===delete item click id===" + pmList?.get(position)?.getId().toString())
        selectedCardId = pmList?.get(position)?.getId().toString()
        AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.delete_payment_method_text_msg))
    }

    override fun callDialog(model: Any?) {
        //delete payment method and go back
        Log.e(PaymentMethodsFragment.TAG,"===deleted id===" + selectedCardId)
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DELETE_PAYMENT_METHOD)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.deletePaymentMethod(selectedCardId))
            ?.execute()
    }

    private fun setData(couponVerification: CouponVerification) {

        Log.d("COUPON_ADD","111")

        buyPlan?.coupon_id = couponVerification.couponDetails?.id

        if (couponVerification.couponDetails?.type == Constants.DiscountType.PERCENTAGE) {

            //OLD
            /*if (buyPlan?.total!!.minus(((buyPlan?.total!!.toDouble() / 100) * couponVerification.couponDetails!!.amount).roundToInt()) <= 0) {
                buyPlan?.total = 1.0
                stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
            } else{
                buyPlan?.total = buyPlan?.total!!.minus(((buyPlan?.total!!.toDouble() / 100) * couponVerification.couponDetails!!.amount).roundToInt())
                stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
            }*/

            //NEW
            if (buyPlan?.total!!.minus(((buyPlan?.total!!.toDouble() / 100) * couponVerification.couponDetails!!.amount)) <= 0) {
                buyPlan?.total = 1.0
                stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
                showHHDiscountStrikethrough(Utils.commaConversion(buyPlan?.total))
            } else{
                buyPlan?.total = buyPlan?.total!!.minus(((buyPlan?.total!!.toDouble() / 100) * couponVerification.couponDetails!!.amount))
                stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
                showHHDiscountStrikethrough(Utils.commaConversion(buyPlan?.total))
            }

        } else {
            if (buyPlan?.total?.minus(couponVerification.couponDetails?.amount!!)!! <= 0) {
                buyPlan?.total = 1.0
                stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
                showHHDiscountStrikethrough(Utils.commaConversion(buyPlan?.total))

            } else{
                buyPlan?.total = buyPlan?.total?.minus(couponVerification.couponDetails!!.amount)
                stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
                showHHDiscountStrikethrough(Utils.commaConversion(buyPlan?.total))

            }
        }
    }

    private fun showHHDiscountStrikethrough(finalPrice: String){
        //SHOW hhDiscount in service detail view
        val originalPrice = totalAmount!!.toDoubleOrNull()
        if (originalPrice != null) {
            val discountedPrice = finalPrice
            val totalPrice = totalAmount + " "+Constants.currencySign
            val spannableOriginalPrice = SpannableString(totalPrice)
            spannableOriginalPrice.setSpan(
                StrikethroughSpan(),
                0,
                totalPrice.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
         /*   val finalString = SpannableStringBuilder()
                .append(spannableOriginalPrice)
                .append("\n")
                .append(discountedPrice)
                .append("\n")
                .append("${Constants.currencySign}")*/
            val finalString = SpannableStringBuilder()
                .append(spannableOriginalPrice)
                .append("\n")
                .append(discountedPrice +" ")
                .append("${Constants.currencySign}")
            tv_heading_category!!.setText(finalString)
        }
    }

    private fun disableApplyAndSendCallback() {
        applyCoupon?.isClickable = false
        pinView?.isFocusable = false
        pinView?.isClickable = false
        pinView?.isEnabled = false

        applyCouponService?.isClickable = false
        pinViewService?.isFocusable = false
        pinViewService?.isClickable = false
        pinViewService?.isEnabled = false


        if (buyPlan?.total!! <= 0) {
            buyPlan?.total = 1.0
            stripePayButton?.text = "Pay ${Utils.commaConversion(buyPlan?.total)} ${Constants.currencySign}"
        }

        buyPlan?.changePrice = 1
        if (childToParent != null) {
            childToParent?.callParent(buyPlan)
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    fun checkUserInfo() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlan())
            ?.execute()
    }

    private fun handleUserPlansScenario(getPlans: GetPlans?, callType: String) {
        //------------------------Check If User has Bought Trips---------------------------

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
                        this, 2)
                   /* BaseActivity.replaceFragment(
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

                        if(serviceType == 1 || serviceType == 4) { //for MonthlyService (1) For YearlyService(4)
                            showPurchaseInfoDialog()

                            /*Notify.hyperlinkAlert(
                                activity,
                                "You are already subscribed for a monthly service. If you require extra trips, you can request a one-time service to avoid paying for another monthly subscription",
                                "Go to Reloop Store",
                                this, 1
                            )*/

                        }else{
                            //AD CHANGE LOOP CALL API buy-new-service  === NEW
                            if(callType.equals("GET_PLAN")){
                                BaseActivity.replaceFragment(
                                    childFragmentManager,
                                    Constants.Containers.containerSubscriptionCycle,
                                    RecycleFragment.newInstance(getPlansAll,
                                        true,null),
                                    Constants.TAGS.RecycleFragment
                                )
                            }
                            else{

                                //OLD TODAY

                               /*

                                if (ServicePurchasingFragment.parentToChild != null) {
                                    ServicePurchasingFragment.parentToChild?.callChild()
                                }*/

                                //NEW TODAY
                                getPaymentIntentStripePay()
                            }

                        }

                    } else{
                        BaseActivity.replaceFragment(
                            childFragmentManager,
                            Constants.Containers.containerSubscriptionCycle,
                            RecycleFragment.newInstance(getPlansAll,
                                true,null),
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
                   /* BaseActivity.replaceFragment(
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

                        }else{
                            //AD CHANGE LOOP CALL API buy-new-service  === NEW
                            if(callType.equals("GET_PLAN")){
                                BaseActivity.replaceFragment(
                                    childFragmentManager,
                                    Constants.Containers.containerSubscriptionCycle,
                                    RecycleFragment.newInstance(getPlansAll,
                                        true,null),
                                    Constants.TAGS.RecycleFragment
                                )
                            }else{

                                Log.d("PAYMENT_INTENT"," Service : TRANSACTION id : "+ paymentIntentIdStripe)

                                //OLD TODAY
                                /*
                                if (ServicePurchasingFragment.parentToChild != null) {
                                    ServicePurchasingFragment.parentToChild?.callChild()
                                }*/

                                //NEW TODAY
                                getPaymentIntentStripePay()


                            }
                        }


                    }
                    else{
                        BaseActivity.replaceFragment(
                            childFragmentManager,
                            Constants.Containers.containerSubscriptionCycle,
                            RecycleFragment.newInstance(getPlansAll,
                                true,null),
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

            backButton?.visibility = View.GONE
            nextButton?.visibility = View.GONE

            stripePayButton?.visibility = View.GONE

            stepViewHs!!.visibility = View.GONE

            if(stripePayButtonService!=null){
                stripePayButtonService?.visibility = View.GONE
            }

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

            backButton?.visibility = View.GONE
            nextButton?.visibility = View.GONE

            stripePayButton?.visibility = View.GONE

            stepViewHs!!.visibility = View.GONE

            if(stripePayButtonService!=null){
                stripePayButtonService?.visibility = View.GONE
            }

            create?.visibility = View.GONE

            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerNewBilling,
                SubscriptionFragment.newInstance(),
                Constants.TAGS.SubscriptionFragment
            )
        }

        dialog.show()
    }

    private fun sendtodetailscreen(
        productFromShared: List<Category?>?,
        dataListServices: ArrayList<ModelShopCategories>
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
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerNewBilling,
            fragment,
            Constants.TAGS.ShopDetailFragment
        )
    }


    fun getStripePaymentMethodID(){
        //showProgressDialog()

        if(paymentIntentIdStripe.isNotEmpty()){

            var baseURL = ""
            baseURL = "https://api.stripe.com/v1/payment_intents/";


            AndroidNetworking.get(baseURL + paymentIntentIdStripe)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Authorization", "Bearer " + Constants.SK_STRIPE)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        //dismissProgressDialog()

                        try {
                            Log.d("==>>", "onResponse: $response")
//                        val data: JSONObject = response.getJSONObject()


                            if(response.has("payment_method")){
                                payment_method = response.getString("payment_method")
                            }else{
                                payment_method= ""
                            }

                            Log.d("==>>", "payment_method: $payment_method")
                            if(buyProduct != null) {
                                buyProduct?.transaction_id = paymentIntentIdStripe
                                buyProduct?.payment_method = payment_method

                                if (childToParent != null) {
                                    childToParent?.callParent(buyProduct)
                                }


                                /*if (ProductPurchasingFragment.parentToChild != null) {
                                    ProductPurchasingFragment.parentToChild?.callChild()
                                }*/
                            } else{

                                //new
                                Log.d("PAYMENT_INTENT"," Service : TRANSACTION id : "+ paymentIntentIdStripe)
                                buyPlan?.transaction_id = paymentIntentIdStripe
                                buyPlan?.payment_method = payment_method
                                /*if (ServicePurchasingFragment.parentToChild != null) {
                                    ServicePurchasingFragment.parentToChild?.callChild()
                                }*/

                                if (childToParent != null) {
                                    childToParent?.callParent(buyPlan)
                                }

                            }



                        } catch (e: java.lang.Exception) {
                            Log.d("==>>", "onResponse: " + e.message)

                        }
                    }

                    override fun onError(error: ANError) {
                        //dismissProgressDialog()
                        Log.d("==>>", "onError: " + error.message)
                    }
                })
        }

    }

    private fun showProgressDialog() {
        dialog = Dialog(requireContext())
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

    fun performPayClick(activity: FragmentActivity,stripePayButtonService: Button?) {

        this.stripePayButtonService = stripePayButtonService
        Log.d("service_type","dd"+ serviceType)

        Log.e("NewBillingInformationFragment","====pay button clicked======")
        if(serviceType == 1 || serviceType==4){ //for MonthlyService (1) For YearlyService(4)
            //OLD flow
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.GET_PLAN_STATUS)
                ?.autoLoading(activity)
                ?.enque(Network().apis()?.getPlanStatus())
                ?.execute()
        }else{
            Log.d("PAY_INTENT","CALLED...")
            if(buyPlan!=null){
                Log.d("PAY_INTENT","CALLED... ONE_TIME")
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.GET_PAYMENT_INTENT_STRIPE)
                    ?.autoLoading(activity)
                    ?.enque(Network().apis()?.getPaymentIntentStripePay(buyPlan!!.total.toString()))
                    ?.execute()
            }else{
                Log.d("PAY_INTENT","CALLED... PRODUCT")
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.GET_PAYMENT_INTENT_STRIPE)
                    ?.autoLoading(activity)
                    ?.enque(Network().apis()?.getPaymentIntentStripePay(totalAmount))
                    ?.execute()
            }



        }

        //stripePayButton?.performClick()
    }

    override fun onPaymentMethodID(pmId: String) {
        Log.i("PM_ID"," : "+ pmId)

        //ORIGINAL FLOW
        buyPlan?.transaction_id = ""
        buyPlan?.payment_method = pmId


        buyPlan?.is_monthly = 1


        if (childToParent != null) {
            childToParent?.callParent(buyPlan)
        }

    }



}
