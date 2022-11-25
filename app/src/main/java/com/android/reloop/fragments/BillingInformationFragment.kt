package com.reloop.reloop.fragments


import CouponVerification
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.reloop.reloop.R
import com.reloop.reloop.auth.BillingInformationAuth
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.shop.BuyPlan
import com.reloop.reloop.network.serializer.cart.BuyProduct
import com.reloop.reloop.network.serializer.couponverification.CouponCategory
import com.reloop.reloop.network.serializer.couponverification.CouponSendData
import com.reloop.reloop.utils.*
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import retrofit2.Call
import retrofit2.Response
import kotlin.math.roundToInt


/**
 * A simple [Fragment] subclass.
 */
class BillingInformationFragment : BaseFragment(), ParentToChild, View.OnClickListener,
    OnNetworkResponse {

    var childToParent: ChildToParent? = null
    var cardNumber: CustomEditText? = null
    var cardExpiry: CustomEditText? = null
    var cardCVV: CustomEditText? = null
    var headingDiscountCoupon: TextView? = null
    var pinView: CustomEditText? = null
    var applyCoupon: Button? = null
    var rememberMe: CheckBox? = null
    var message: TextView? = null



    companion object {
        var buyPlan: BuyPlan? = null
        var buyProduct: BuyProduct? = null

        fun newInstance(buyPlan: BuyPlan?, buyProduct: BuyProduct?): BillingInformationFragment {
            this.buyPlan = buyPlan
            this.buyProduct = buyProduct
            return BillingInformationFragment()
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
            inflater.inflate(R.layout.fragment_billing_information, container, false)
        if (ProductPurchasingFragment.stepView != null) {
            ProductPurchasingFragment.stepView?.StepNumber(Constants.recycleStep3)
        }
        ServicePurchasingFragment.parentToChild = this
        ProductPurchasingFragment.parentToChild = this
        initViews(view)
        setListeners()
        populateData()
        return view


    }

    private fun initViews(view: View?) {
        cardNumber = view?.findViewById(R.id.card_number)
        cardExpiry = view?.findViewById(R.id.card_expiry)
        cardCVV = view?.findViewById(R.id.card_cvv)
        applyCoupon = view?.findViewById(R.id.applyCoupon)
        pinView = view?.findViewById(R.id.pin_view)
        headingDiscountCoupon = view?.findViewById(R.id.heading_discount_coupon)
        rememberMe = view?.findViewById(R.id.remember_me)
        message = view?.findViewById(R.id.message)
        if (buyPlan?.subscription_type == 1){
            rememberMe?.visibility = View.VISIBLE
            message?.visibility = View.VISIBLE
            message?.text = "* You can stop the subscription at any time!"
        }
    }

    private fun setListeners() {
        applyCoupon?.setOnClickListener(this)
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
        rememberMe?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                Notify.alerterRed(activity, "Auto-renew is required to proceed")
                rememberMe?.isChecked  = true
            }
        }

    }

    override fun callChild() {
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
                Log.e("TAG","====cvv2=====" + cardCVV?.text.toString())
                buyPlan?.exp_month = dateExpiry[0]
                buyPlan?.exp_year = dateExpiry[1]
                if (childToParent != null) {
                    childToParent?.callParent(buyPlan)
                }
            } else {
                buyProduct?.card_number = cardNumber?.text.toString()
                buyProduct?.cvv = cardCVV?.text.toString()
                Log.e("TAG","====cvv3=====" + cardCVV?.text.toString())
                buyProduct?.exp_month = dateExpiry[0]
                buyProduct?.exp_year = dateExpiry[1]
                if (childToParent != null) {
                    childToParent?.callParent(buyProduct)
                }
            }
        }

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
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.COUPON_VERIFICATION -> {
                val couponVerification = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    CouponVerification::class.java
                )
                if (couponVerification?.validForCategory == null) {
                    Notify.alerterRed(
                        activity,
                        getString(R.string.caoupon_not_valid_for_this)
                    )
                    return
                } else {
                    if (!couponVerification?.validForCategory?.type.isNullOrEmpty()
                        && couponVerification?.validForCategory?.id.isNullOrEmpty()
                    ) {
                        val serviceType: Int? =
                            couponVerification?.validForCategory?.type?.find { it == Constants.serviceType }
                        val allTypes: Int? =
                            couponVerification?.validForCategory?.type?.find { it == Constants.all }
                        if (serviceType != null || allTypes != null) {
                            Notify.alerterGreen(
                                activity,
                                baseResponse?.message
                            )
                            setData(couponVerification)
                            disableApplyAndSendCallback()

                        }
                    } else if (!couponVerification?.validForCategory?.id.isNullOrEmpty()
                        && couponVerification?.validForCategory?.type.isNullOrEmpty()
                    ) {
                        if (couponVerification?.validForCategory?.id?.get(0) == buyPlan?.subscription_type) {
                            setData(couponVerification)
                            disableApplyAndSendCallback()
                        } else {
                            if (couponVerification?.validForCategory == null) {
                                Notify.alerterRed(
                                    activity,
                                    getString(R.string.caoupon_not_valid_for_this)
                                )
                            }
                        }
                    } else {
                        if (couponVerification?.validForCategory == null) {
                            Notify.alerterRed(activity, getString(R.string.caoupon_not_valid_for_this))
                        }
                    }
                }
            }
        }
    }

    private fun setData(couponVerification: CouponVerification) {
        buyPlan?.coupon_id = couponVerification.couponDetails?.id
        if (couponVerification.couponDetails?.type == Constants.DiscountType.PERCENTAGE) {
            if (buyPlan?.total!!.minus(((buyPlan?.total!!.toDouble() / 100) * couponVerification.couponDetails!!.amount).roundToInt()) <= 0) {
                buyPlan?.total = 1.0
            } else
                buyPlan?.total = buyPlan?.total!!.minus(((buyPlan?.total!!.toDouble() / 100) * couponVerification.couponDetails!!.amount).roundToInt())
        } else {
            if (buyPlan?.total?.minus(couponVerification.couponDetails?.amount!!)!! <= 0) {
                buyPlan?.total = 1.0
            } else
                buyPlan?.total = buyPlan?.total?.minus(couponVerification.couponDetails!!.amount)
        }
    }

    private fun disableApplyAndSendCallback() {
        applyCoupon?.isClickable = false
        pinView?.isFocusable = false
        pinView?.isClickable = false
        pinView?.isEnabled = false
        if (buyPlan?.total!! <= 0) {
            buyPlan?.total = 1.0
        }
        buyPlan?.changePrice = 1
        if (childToParent != null) {
            childToParent?.callParent(buyPlan)
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }
}
