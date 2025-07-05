package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.reloop.model.Productsnew
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.activities.WebViewActivity
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.StepView
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DataParsing
import com.reloop.reloop.network.serializer.cart.BuyProduct
import com.reloop.reloop.network.serializer.shop.Product
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_subscription_cycle.*
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ProductPurchasingFragment : BaseFragment(), StepView, View.OnClickListener
    , ChildToParent, OnNetworkResponse {

    companion object {
        var parentToChild: ParentToChild? = null
        fun newInstance(): ProductPurchasingFragment {
            return ProductPurchasingFragment()
        }

        var TAG = "Subscription Cycle Fragment"
        var stepView: StepView? = null
//        private const val STATE_SAVE_STATE = "save_state"
//        private const val STATE_KEEP_FRAGS = "keep_frags"
//        private const val STATE_HELPER = "helper"
    }

    var currentStep: Int? = -1
    var imageStep1: ImageView? = null
    var textStep1: TextView? = null
    var imageStep2: ImageView? = null
    var textStep2: TextView? = null
    var imageStep3: ImageView? = null
    var textStep3: TextView? = null
    var imageStep4: ImageView? = null
    var textStep4: TextView? = null

    var back: Button? = null
    var next: Button? = null
    var create: Button? = null

    var buyProduct: BuyProduct? = null
    var mainLayout: FrameLayout? = null
    var mainLayout1: FrameLayout? = null
    var stripePayButton: Button? = null

    //    private lateinit var stateHelper: FragmentStateHelper
/*    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stateHelper = FragmentStateHelper(childFragmentManager)


        if (savedInstanceState == null) {
        } else {
//            state_switch.isChecked = savedInstanceState.getBoolean(STATE_SAVE_STATE)
//            keep_switch.isChecked = savedInstanceState.getBoolean(STATE_KEEP_FRAGS)

            val helperState = savedInstanceState.getBundle(STATE_HELPER)
            if (helperState != null) {
                stateHelper.restoreHelperState(helperState)
            }
        }
    }*/



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater.inflate(R.layout.fragment_subscription_cycle, container, false)
        buyProduct = BuyProduct()
        /*     buyProduct?.plan_id = planId
             buyProduct?.subscription_id = productId
             buyProduct?.subscription_type = productType*/
        initViews(view)
        setListeners()
        populateData()

        return view
    }



    private fun initViews(view: View?) {


        mainLayout = view?.findViewById(R.id.container_subscription_cycle_parent)
        mainLayout1 = view?.findViewById(R.id.container_monthly_subscription_fragment)

        imageStep1 = view?.findViewById(R.id.img_step1)
        imageStep2 = view?.findViewById(R.id.img_step2)
        imageStep3 = view?.findViewById(R.id.img_step3)
        imageStep4 = view?.findViewById(R.id.img_step4)

        textStep1 = view?.findViewById(R.id.text_step1)
        textStep2 = view?.findViewById(R.id.text_step2)
        textStep3 = view?.findViewById(R.id.text_step3)
        textStep4 = view?.findViewById(R.id.text_step4)

        next = view?.findViewById(R.id.next)
        back = view?.findViewById(R.id.back)
        create = view?.findViewById(R.id.create)

        stripePayButton = view?.findViewById(R.id.stripePayButton)

    }

    private fun setListeners() {

        ProductPurchasingFragment.stepView = this
        next?.setOnClickListener(this)
        back?.setOnClickListener(this)
        create?.setOnClickListener(this)
    }

    private fun populateData() {
        BaseActivity.replaceFragmentBackStackNull(
            childFragmentManager,
            Constants.Containers.containerSubscriptionCycle,
            CartInformationFragment.newInstance(buyProduct)
        )
    }

    //------------------------------Update StepView UI-----------------------------
    @SuppressLint("SetTextI18n")
    override fun StepNumber(stepNumber: Int) {
        currentStep = stepNumber
        when (stepNumber) {
            Constants.recycleStep1 -> {
                BaseActivity.stepViewUpdateUI(
                    imageStep1,
                    R.drawable.icon_cart_en,
                    textStep1,
                    requireActivity().getColor(R.color.green_color_button)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep2,
                    R.drawable.icon_address_location_cycle_un,
                    textStep2,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                back?.visibility = View.VISIBLE
                next?.visibility = View.VISIBLE
                stripePayButton?.visibility = View.GONE
            }

            Constants.recycleStep2 -> {

                BaseActivity.stepViewUpdateUI(
                    imageStep1,
                    R.drawable.icon_cart_un,
                    textStep1,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep2,
                    R.drawable.icon_address_location_cycle_en,
                    textStep2,
                    requireActivity().getColor(R.color.green_color_button)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep3,
                    R.drawable.icon_billing_un,
                    textStep3,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                back?.visibility = View.VISIBLE
                next?.visibility = View.VISIBLE
                stripePayButton?.visibility = View.GONE
                next?.text = getString(R.string.next)
            }
            Constants.recycleStep3 -> {

                BaseActivity.stepViewUpdateUI(
                    imageStep2,
                    R.drawable.icon_address_location_cycle_un,
                    textStep2,
                    requireActivity().getColor(R.color.text_color_heading)
                )
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

                stripePayButton?.visibility = View.VISIBLE //New here


                //ORIGINAL ADDED next button
//                next?.text = "Pay ${Utils.commaConversion(buyProduct?.total)} ${Constants.currencySign}"

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
                parentToChild?.callChild()
            }
            R.id.back -> {
                activity?.onBackPressed()
            }
            R.id.create -> {
                showOrderHistory()
                HomeActivity.clearAllFragments(true)
                /* for (i in 0..childFragmentManager.backStackEntryCount) {
                     childFragmentManager.popBackStack()
                 }
                 for (i in 0..fragmentManager!!.backStackEntryCount) {
                     fragmentManager!!.popBackStack()
                 }*/
            }
        }
    }

    private fun showOrderHistory() {
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerSubscriptionCycleParent,
            OrderHistoryFragment.newInstance(),
            Constants.TAGS.OrderHistoryFragment
        )
    }

    override fun callParent(model: Any?) {
        buyProduct = model as? BuyProduct
        if (currentStep == Constants.recycleStep1) {
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerSubscriptionCycle,
                AddressInformationFragment.newInstance(buyProduct),
                Constants.TAGS.AddressInformationFragment
            )
        }
        else if (currentStep == Constants.recycleStep2) {

//            stripePayButton?.visibility = View.VISIBLE //old here


            stripePayButton?.text = "Pay ${ Utils.commaConversion(buyProduct?.total) } ${Constants.currencySign}"

            stripePayButton?.setOnClickListener {

                val fragment: NewBillingInformationFragment =
                    childFragmentManager.fragments[0] as NewBillingInformationFragment
                fragment.performPayClick(requireActivity(),null)

            }


            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerSubscriptionCycle,

//                BillingInformationFragment.newInstance(null, buyProduct),// original
 //               Constants.TAGS.BillingInformationFragment // original

                NewBillingInformationFragment.newInstance(
                    null,
                    buyProduct,
                    0,
                    null,
                    Utils.commaConversion(buyProduct?.total),
                null,
                    null,null,null,null,null,null,null),//new AD
                Constants.TAGS.NewBillingInformationFragment
            )
        }

        else if (currentStep == Constants.recycleStep3) {


            if (CartInformationFragment.deliveryFeeValue != -1) {
                buyProduct?.delivery_fee = CartInformationFragment.deliveryFeeValue.toString()
            }


            buyProduct?.products = ArrayList()
            if (HomeActivity.cartList?.indices != null) {
                for (i in HomeActivity.cartList?.indices!!) {

                    Log.e(TAG,"===productId======" + HomeActivity.cartList?.get(i)?.id)
                    Log.e(TAG,"===productqty======" + HomeActivity.cartList?.get(i)?.quantity)

                    val product = Product()
                    Log.e(TAG,"===productId======" + HomeActivity.cartList?.get(i)?.id)
                    product.id = HomeActivity.cartList?.get(i)?.id
                    product.qty = HomeActivity.cartList?.get(i)?.quantity
                    buyProduct?.products?.add(product)

                }
            }

            Log.e("TAG","===BuyProduct address======" + Gson().toJson(User.retrieveUser()?.addresses))

            val address = User.retrieveUser()?.addresses
            for(i in address!!.indices)
            {
                if(address.get(i).location!!.contains(buyProduct?.map_location.toString()))
                {
                    buyProduct?.city_id = address.get(i).city_id
                    buyProduct?.district_id = address.get(i).district_id
                }
            }

            Log.e("TAG","===BuyProduct city======" + Gson().toJson(buyProduct?.city_id))
            Log.e("TAG","===BuyProduct distirct======" + Gson().toJson(buyProduct?.district_id))


            val couponid = buyProduct!!.coupon_id
            if(!buyProduct!!.coupon_id.toString().isNullOrEmpty() && !buyProduct!!.coupon_id.toString().equals("null")) {
                buyProduct!!.coupon_id = couponid
            }

            /*  var URL = "card_number=${buyProduct?.card_number}&expiry_date=${buyProduct?.exp_year + buyProduct?.exp_month}" +
                        "&card_security_code=${buyProduct?.cvv.toString()}&" + "&is_new_card=${"1"}&"+
                        "user_id=${User.retrieveUser()?.id.toString()}&total=${buyProduct?.total.toString()}" +
                        "&coupon_id=${buyProduct?.coupon_id.toString()}&subtotal=${buyProduct?.subtotal.toString()}&points_discount=${buyProduct?.points_discount.toString()}&first_name=${buyProduct?.first_name.toString()}&" +
                        "last_name=${buyProduct?.last_name.toString()}&email=${buyProduct?.email.toString()}&phone_number=${buyProduct?.phone_number.toString()}" +
                        "&location=${buyProduct?.location.toString().replace(",", "")}&latitude=${buyProduct?.latitude.toString()}&longitude=${buyProduct?.longitude.toString()}" +
                        "&city_id=${buyProduct?.city_id.toString()}&district_id=${buyProduct?.district_id.toString()}&organization_name=${buyProduct?.organization_name.toString()}"+ deliveryFeeString+"&map_location=${buyProduct?.map_location.toString()}"*/

            /*var URL = "card_number=${buyProduct?.card_number}&expiry_date=${buyProduct?.exp_year + buyProduct?.exp_month}" +
                    "&card_security_code=${buyProduct?.cvv.toString()}&is_new_card=${buyProduct?.is_new_card}" +
                    "&user_id=${User.retrieveUser()?.id.toString()}&total=${buyProduct?.total.toString()}&delivery_fee=${buyProduct?.delivery_fee}" +
                    "&coupon_id=${buyProduct?.coupon_id.toString()}&subtotal=${buyProduct?.subtotal.toString()}&points_discount=${buyProduct?.points_discount.toString()}&first_name=${buyProduct?.first_name.toString()}&" +
                    "last_name=${buyProduct?.last_name.toString()}&email=${buyProduct?.email.toString()}&phone_number=${buyProduct?.phone_number.toString()}" +
                    "&location=${buyProduct?.location.toString().replace(",", "")}&latitude=${buyProduct?.latitude.toString()}&longitude=${buyProduct?.longitude.toString()}" +
                    "&city_id=${buyProduct?.city_id.toString()}&district_id=${buyProduct?.district_id.toString()}&organization_name=${buyProduct?.organization_name.toString()}"+"&map_location=${buyProduct?.map_location.toString()}"+"&user_card_id=${buyProduct?.user_card_id.toString()}"

            for (i in 0 until buyProduct?.products?.size!!) {
                URL += "&products[${i}][id]=${buyProduct?.products?.get(i)?.id.toString()}"
                URL += "&products[${i}][qty]=${buyProduct?.products?.get(i)?.qty.toString()}"
            }*/

            Log.e("TAG","===BuyProduct======" + Gson().toJson(buyProduct))

            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.BUY_PRODUCT)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.buyProductNew(buyProduct!!)) //buy product json
                ?.execute()

            //startActivityForResult(Intent(activity, WebViewActivity::class.java).putExtra(WebViewActivity.URL, URL), ServicePurchasingFragment.BUY_PLAN)
            /*NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.BUY_PRODUCT)
                ?.autoLoading(requireActivity())
                ?.enque(
                    Network().apis()?.buyProduct(buyProduct)
                )
                ?.execute()*/
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ServicePurchasingFragment.BUY_PLAN && resultCode == Activity.RESULT_OK) {
            val fragment = ConfirmationSubscriptionFragment.newInstance()
            val args = Bundle()
            args.putString(Constants.DataConstants.purchaseID, data?.extras?.getString(WebViewActivity.REFERENCE_ID))
            args.putInt(Constants.DataConstants.subscriptionCycle, Constants.subscriptionCycleOne)
            buyProduct?.points_discount?.let {
                args.putInt(Constants.DataConstants.rewardPoints, it.toInt())
            }
            fragment.arguments = args
            BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.containerSubscriptionCycle, fragment, Constants.TAGS.ConfirmationSubscriptionFragment)

            HomeActivity.cartList?.clear()
            HomeActivity.refreshCart()
            CartInformationFragment.deliverFeeKey = ""
            CartInformationFragment.deliveryFeeValue = -1
        } else if (requestCode == ServicePurchasingFragment.BUY_PLAN && resultCode == Activity.RESULT_CANCELED) {
            try {
                Notify.alerterRed(activity, data?.extras?.getString(WebViewActivity.MESSAGE)?.replace("+", " "))
            }
            catch (e : NullPointerException)
            {
                e.printStackTrace()
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.BUY_PRODUCT -> {
                val baseResponse = Utils.getBaseResponse(response)
                val dataParsing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    DataParsing::class.java)

                Log.d("BUY_PRODUCT","STRIPE PRODUCT"+ GsonBuilder().setPrettyPrinting().create().toJson(dataParsing))

                val fragment = ConfirmationSubscriptionFragment.newInstance()
                val args = Bundle()
                args.putString(Constants.DataConstants.purchaseID, dataParsing.buyProductId?.get(0))
                args.putInt(Constants.DataConstants.subscriptionCycle, Constants.subscriptionCycleOne)
                buyProduct?.points_discount?.let {
                    args.putInt(Constants.DataConstants.rewardPoints, it.toInt())
                }
                fragment.arguments = args

                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.containerSubscriptionCycle,
                    fragment,
                    Constants.TAGS.ConfirmationSubscriptionFragment)

                HomeActivity.cartList?.clear()
                HomeActivity.refreshCart()
                CartInformationFragment.deliverFeeKey = ""
                CartInformationFragment.deliveryFeeValue = -1
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    override fun onAttach(context: Context) {
        setCartOpened(true)
        super.onAttach(context)
    }

    override fun onDetach() {
        setCartOpened(false)
        super.onDetach()
    }

    fun openListingPage() {
        activity?.onBackPressed()
    }

    fun openShopDetails() {
        Log.e(TAG,"=====onbackpressed called====")
        //activity?.onBackPressed()
        mainLayout?.visibility = View.GONE
        mainLayout1?.visibility = View.VISIBLE
    }
/*
    override fun onSaveInstanceState(outState: Bundle) {
        // Make sure we save the current tab's state too!
        saveCurrentState()

        outState.putBoolean(STATE_SAVE_STATE, true)
        outState.putBoolean(STATE_KEEP_FRAGS,true)
        outState.putBundle(STATE_HELPER, stateHelper.saveHelperState())

        super.onSaveInstanceState(outState)
    }

    private fun saveCurrentState() {
        stateHelper.saveState(cartInformationFragment, 1)
    }*/
}
