package com.reloop.reloop.fragments


import CouponVerification
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.network.serializer.cart.SubscriberDiscount
import com.android.reloop.network.serializer.shop.DeliveryFeeModel
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.AdapterCart
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.interfaces.*
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.cart.BuyProduct
import com.reloop.reloop.network.serializer.cart.RedeemPoints
import com.reloop.reloop.network.serializer.couponverification.CouponCategory
import com.reloop.reloop.network.serializer.couponverification.CouponSendData
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


/**
 * A simple [Fragment] subclass.
 */
class CartInformationFragment : BaseFragment(), View.OnClickListener, ParentToChild,
    OnNetworkResponse, ItemClickQuantity, RemoveItemCart, AlertDialogCallback {


    companion object {
        var parentToChild: ParentToChild? = null
        var cartListSafe: ArrayList<Category?>? = ArrayList()
        var buyProduct: BuyProduct? = null
        fun newInstance(model: BuyProduct?): CartInformationFragment {
            this.buyProduct = model
            return CartInformationFragment()
        }

        var deliveryFeeOrderLimit = 0
        var deliveryFeeValue = -1
        var deliverFeeKey = ""
//        private const val TOTAL_AMOUNT = "TOTAL_AMOUNT"
//        private const val DISCOUNTED_AMOUNT = "DISCOUNTED_AMOUNT"
//        private const val SUBTOTAL_AMOUNT = "SUBTOTAL_AMOUNT"
//        private const val CARTLIST = "SUBTOTAL_AMOUNT"
    }

    private var coupondiscountvalue: Double? = 0.0
    private var reedempointdiscount: Double? = 0.0
    var cartRefresh : Boolean = false
    private var finaldeliverdiscount: Double = 0.0
    private var finalsubtotal: Double = 0.0
    var deliveryFeeList: ArrayList<DeliveryFeeModel> = ArrayList()
    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var btnRedeem: Button? = null

    var childToParent: ChildToParent? = null
    var total: TextView? = null
    var subTotal: TextView? = null
    var applyCoupon: Button? = null
    var pinView: CustomEditText? = null
    var adapter: AdapterCart? = null
    var couponVerification: CouponVerification? = null
    var totalUserPoints: TextView? = null
    var totalUserPointsToRedeem = 0.0
    var discountedPrice: TextView? = null
    var deliveryFee: TextView? = null

    var tempsubtotalAmount: Double? = null
    var subtotalAmount: Double? = null
    var totalAmount: Double? = 0.0
    var discountedPriceAmount: Double? = 0.0
    var discount_label: RelativeLayout? = null
    var message: TextView? = null
    var deliveryDiscount: Int? = null
    var productOrderDiscount: Int? = null
    var couponAmount: Double? = 0.0
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ChildToParent) {
            childToParent = parentFragment as? ChildToParent?
        } else {
            Notify.Toast("Child To Parent Callback Not Implemented")
        }
    }

/*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(TOTAL_AMOUNT, totalAmount!!)
        outState.putInt(DISCOUNTED_AMOUNT, discountedPriceAmount!!)
        outState.putInt(SUBTOTAL_AMOUNT, subtotalAmount!!)

    }
*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (ProductPurchasingFragment.stepView != null) {
            ProductPurchasingFragment.stepView!!.StepNumber(Constants.recycleStep1)
        }
        val view: View? = inflater.inflate(R.layout.fragment_cart_information, container, false)
        ProductPurchasingFragment.parentToChild = this
        initViews(view)
        setListeners()

        return view
    }

    /*  @SuppressLint("SetTextI18n")
      override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
          super.onViewCreated(view, savedInstanceState)

          if (savedInstanceState != null) {
              totalAmount = savedInstanceState.getInt(TOTAL_AMOUNT)
              discountedPriceAmount = savedInstanceState.getInt(DISCOUNTED_AMOUNT)
              subtotalAmount = savedInstanceState.getInt(SUBTOTAL_AMOUNT)

              if (discountedPriceAmount!! > 0) {
                  discount_label?.visibility = View.VISIBLE
              }
              discountedPrice?.text = "$discountedPriceAmount ${Constants.currencySign}"
              subTotal?.text = "$subtotalAmount ${Constants.currencySign}"
              total?.text = "$totalAmount ${Constants.currencySign}"
          }

      }*/

    private fun initViews(view: View?) {
        recyclerView = view?.findViewById(R.id.rv_purchase_product_list)
        btnRedeem = view?.findViewById(R.id.btn_redeem)
        total = view?.findViewById(R.id.totalPrice)
        subTotal = view?.findViewById(R.id.subTotalPrice)
        applyCoupon = view?.findViewById(R.id.applyCoupon)
        pinView = view?.findViewById(R.id.pin_view)
        totalUserPoints = view?.findViewById(R.id.totalUserPoints)
        discountedPrice = view?.findViewById(R.id.discountedPrice)
        discount_label = view?.findViewById(R.id.discount_label)
        deliveryFee = view?.findViewById(R.id.deliveryFee)
        message = view?.findViewById(R.id.message)

        //call api to get subscriber order
        if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.GET_SUBSCRIBER_ORDER)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getSubscriberOrder())
                ?.execute()
        } else {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.GET_SUBSCRIBER_ORDER)
                ?.enque(Network().apis()?.getSubscriberOrder())
                ?.execute()
        }

        /* if (!NetworkCall.inProgress()) {
             NetworkCall.make()
                 ?.setCallback(this)
                 ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                 ?.autoLoading(requireActivity())
                 ?.enque(Network().apis()?.getDeliveryFee())
                 ?.execute()
         } else {
             NetworkCall.make()
                 ?.setCallback(this)
                 ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                 ?.enque(Network().apis()?.getDeliveryFee())
                 ?.execute()
         }*/
    }

    private fun setListeners() {
        btnRedeem?.setOnClickListener(this)
        applyCoupon?.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun populateData(deliveryFeeValue: Int) {
        finaldeliverdiscount = deliveryFeeValue.toDouble()
        discountedPriceAmount = 0.0
        totalUserPointsToRedeem = User.retrieveUser()?.reward_points!!.toDouble()
        if (buyProduct?.points_discount != null && buyProduct?.points_discount != 0.0) {
            totalUserPointsToRedeem = totalUserPointsToRedeem.minus(buyProduct?.points_discount!!)
            discountedPriceAmount = buyProduct?.discountRedeem
            discount_label?.visibility = View.VISIBLE
            btnRedeem?.isClickable = false
            reedempointdiscount= discountedPriceAmount?.plus(coupondiscountvalue!!)
            Log.e("TAG","===reedem point value===" + buyProduct?.discountRedeem)
        }
        totalUserPoints?.text = "${Utils.commaConversion(totalUserPointsToRedeem)} ReLoop Points"
        //discountedPrice?.text = "${Utils.commaConversion(discountedPriceAmount)} ${Constants.currencySign}"

        //---------------Setting Item Quantity and RecyclerView fro Cart List----------------

        if (HomeActivity.cartList?.indices != null) {
            cartListSafe = HomeActivity.cartList
            for (i in cartListSafe?.indices!!) {
                //--------------MAke Sure Quantity and original price is added in reference cart list
                if (HomeActivity.cartList?.get(i)?.quantity == null) {
                    cartListSafe?.get(i)?.quantity = 1
                } else {
                    cartListSafe?.get(i)?.quantity = HomeActivity.cartList?.get(i)?.quantity
                }
                if (cartListSafe?.get(i)?.originalPrice == null || cartListSafe?.get(i)?.originalPrice == 0.0) {
                    cartListSafe?.get(i)?.originalPrice = cartListSafe?.get(i)?.price
                }
                //-----------Check if Main List contain original price make sure reference gets that price as well
                if (HomeActivity.cartList?.get(i)?.originalPrice != null || HomeActivity.cartList?.get(i)?.originalPrice != 0.0) {
                    cartListSafe?.get(i)?.originalPrice = HomeActivity.cartList?.get(i)?.originalPrice
                    if (!buyProduct?.cartListUpdated.isNullOrEmpty()) {
                        cartListSafe?.get(i)?.price = buyProduct?.cartListUpdated?.get(i)?.price
                        couponIsApplied()
                    } else
                        cartListSafe?.get(i)?.price = HomeActivity.cartList?.get(i)?.originalPrice
                } else {
                    HomeActivity.cartList?.get(i)?.originalPrice = HomeActivity.cartList?.get(i)?.price
                    cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price
                }

            }
            HomeActivity.cartList = cartListSafe
            HomeActivity.refreshCart()
        }

        /*   if (!buyProduct?.cartListUpdated.isNullOrEmpty()) {
                  cartListSafe = buyProduct?.cartListUpdated
                  subtotalAmount = buyProduct?.subtotal
                  totalAmount = buyProduct?.total
                  if (buyProduct?.discountCoupon!=0)
                  {
                      discountedPriceAmount=discountedPriceAmount?.plus(buyProduct?.discountCoupon!!)
                  }
              }*/

        linearLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = linearLayoutManager
        adapter = AdapterCart(cartListSafe, this)
        recyclerView?.adapter = adapter
        getSubTotalPrice()
        if (!HomeFragment.settings.free_delivery_details.isNullOrEmpty()) {
            message?.text = "* "+HomeFragment.settings.free_delivery_details[0].value
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_redeem -> {
                if (totalUserPointsToRedeem > 10) {
                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.REDEEM_POINTS)
                        ?.autoLoading(requireActivity())
                        ?.enque(Network().apis()?.redeemPoints(1))
                        ?.execute()
                } else {
                    Notify.alerterRed(activity, getString(R.string.not_enough_points_msg))
                }
            }

            R.id.applyCoupon -> {
                if (totalAmount == 1.0) {
                    Notify.alerterRed(activity, getString(R.string.limi_reach))
                } else {
                    if (pinView?.text?.isEmpty() == true || pinView?.text?.length!! < 5) {
                        if (pinView?.text.toString().isEmpty()) {
                            Notify.alerterRed(activity, getString(R.string.enter_coupon_code))
                        } else {
                            Notify.alerterRed(activity, getString(R.string.coupon_code_not_valid))
                        }
                    }
                    else {
                        val list: ArrayList<CouponCategory> = ArrayList()
                        if (!cartListSafe.isNullOrEmpty()) {
                            for (i in cartListSafe?.indices!!) {
                                val categoryCoupon = CouponCategory()
                                categoryCoupon.id = cartListSafe!![i]?.category_id
                                categoryCoupon.type = Constants.productType
                                list.add(categoryCoupon)
                        }
                    }
                        val couponVerification = CouponSendData()
                        couponVerification.coupon = pinView?.text.toString()
                        couponVerification.category = list
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
    }

    override fun callChild() {
        //---------------Setting Total Item And Subtotal And Sending To Parent Fragment----------------
        if (couponVerification != null) {
            buyProduct?.cartListUpdated = cartListSafe
            couponVerification = null
        }
        buyProduct?.total = totalAmount
        buyProduct?.subtotal = subtotalAmount
        if (childToParent != null) {
            childToParent?.callParent(buyProduct)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)

        Log.e("TAG","===response===" + baseResponse)

        when (tag) {
            RequestCodes.API.COUPON_VERIFICATION -> {
                couponVerification = Gson().fromJson(
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
                        && couponVerification?.validForCategory?.id.isNullOrEmpty())
                    {
                        Notify.alerterGreen(activity, baseResponse.message)
                        val productType: Int? = couponVerification?.validForCategory?.type?.find { it == Constants.productType }
                        val allTypes: Int? = couponVerification?.validForCategory?.type?.find { it == Constants.all }
                        if (productType != null || allTypes != null) {
                            //--------------Getting Coupon Verification Id and Amount + Updating Total Amount
                            buyProduct?.coupon_id = couponVerification?.couponDetails?.id

                            updateDiscountedPrice()
                            //applyCouponCode(couponVerification!!)
                            couponIsApplied()
                        } else {
                            Notify.alerterRed(activity, getString(R.string.caoupon_not_valid_for_this))
                        }
                    } else if (!couponVerification?.validForCategory?.id.isNullOrEmpty()
                        && couponVerification?.validForCategory?.type.isNullOrEmpty()
                    ) {
                        Notify.alerterGreen(activity, baseResponse.message)
                        updateDiscountedPriceBasedOnID(couponVerification?.validForCategory?.id?.get(0))
                        //applyCouponCode(couponVerification!!)
                        couponIsApplied()
                    } else {
                        Notify.alerterRed(activity, getString(R.string.caoupon_not_valid_for_this))
                    }
                }
            }
            RequestCodes.API.REDEEM_POINTS -> {
                val discount = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    RedeemPoints::class.java)
                AlertDialogs.redeemPointsDialog(
                    activity,
                    discount.discount,
                    subtotalAmount,
                    totalUserPointsToRedeem,
                    this
                )
            }
            RequestCodes.API.GET_DELIVERY_FEE -> {
                try {
                    val gson = Gson()
                    val listType: Type =
                        object : TypeToken<ArrayList<DeliveryFeeModel>>() {}.type
                    deliveryFeeList = gson.fromJson(
                        Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>),
                        listType
                    )
                    if (deliveryFeeList.isNullOrEmpty()) {
                        //Do Nothing
                    } else if (deliveryFeeList.size >= 2) {
                        deliveryFeeValue = deliveryFeeList[0].value
                        deliverFeeKey = deliveryFeeList[0].key
                        Log.e("TAG","====delivery fee=====" + deliveryFeeValue)
                        deliveryFee?.text = "${Utils.commaConversion(deliveryFeeValue)} ${Constants.currencySign}"
                        deliveryFeeOrderLimit = deliveryFeeList[1].value

                        populateData(deliveryFeeValue)
                        //      getSubTotalPrice()
                        //      setDeliveryData()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            RequestCodes.API.GET_SUBSCRIBER_ORDER -> {
                try {
                    if(baseResponse?.data != null) {
                        val data = Gson().fromJson(
                            Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                            SubscriberDiscount::class.java
                        )
                        Log.e("TAG", "===response order===" + data)

                        if(data != null) {
                            deliveryDiscount = data.getOrderDeliveryDiscount()?.toInt()
                            productOrderDiscount = data.getProductsOrderDiscount()?.toInt()
                            Log.e("TAG", "===data1====" + deliveryDiscount + "====data2====" + productOrderDiscount)

                            if (!NetworkCall.inProgress()) {
                                NetworkCall.make()
                                    ?.setCallback(this)
                                    ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                                    ?.autoLoading(requireActivity())
                                    ?.enque(Network().apis()?.getDeliveryFee())
                                    ?.execute()
                            } else {
                                NetworkCall.make()
                                    ?.setCallback(this)
                                    ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                                    ?.enque(Network().apis()?.getDeliveryFee())
                                    ?.execute()
                            }
                            populateData(deliveryFeeValue)
                        }
                    }
                    else{

                        if (!NetworkCall.inProgress()) {
                            NetworkCall.make()
                                ?.setCallback(this)
                                ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                                ?.autoLoading(requireActivity())
                                ?.enque(Network().apis()?.getDeliveryFee())
                                ?.execute()
                        } else {
                            NetworkCall.make()
                                ?.setCallback(this)
                                ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                                ?.enque(Network().apis()?.getDeliveryFee())
                                ?.execute()
                        }

                    //populateData()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun applyCouponCode(couponVerification: CouponVerification) {
        getSubTotalPrice()
        Log.e("TAG","=====subtotal====" + finalsubtotal)
        if (couponVerification.couponDetails?.type == Constants.DiscountType.PERCENTAGE && couponVerification.couponDetails?.amount != null) {
            if (finalsubtotal != null) {
                val price = ((finalsubtotal.toDouble() / 100) * couponVerification.couponDetails?.amount!!)
                discountedPriceAmount = price

            }
        } else if (couponVerification.couponDetails?.type == Constants.DiscountType.FIXED) {
            val price = couponVerification.couponDetails?.amount!!
            discountedPriceAmount = price
        }
        if (finalsubtotal <= 0) {
            finalsubtotal = 1.0
        }

        setDeliveryData()
    }

    private fun couponIsApplied() {
        applyCoupon?.isClickable = false
        pinView?.isFocusable = false
        pinView?.isClickable = false
        pinView?.isEnabled = false
        discount_label?.visibility = View.VISIBLE
    }

    @SuppressLint("SetTextI18n")
    private fun updateDiscountedPrice() {
        Log.e("TAG","=======updateDiscountedPrice ===" +  finalsubtotal)
        Log.e("TAG","=======updated price ===" +  finalsubtotal.minus(discountedPriceAmount!!))
        tempsubtotalAmount = finalsubtotal.minus(discountedPriceAmount!!)
        finalsubtotal.minus(discountedPriceAmount!!)

        if (cartListSafe?.indices != null) {
            for (i in cartListSafe?.indices!!) {
                //var price = cartListSafe?.get(i)?.price
                Log.e("TAG","=======temp subtotal ===" +  tempsubtotalAmount)
                //var price = finalsubtotal  old code line
                var price = tempsubtotalAmount
                Log.e("TAG","=======amount=" +couponVerification?.couponDetails?.amount )
                if (couponVerification?.couponDetails?.type == Constants.DiscountType.PERCENTAGE && couponVerification?.couponDetails?.amount != null) {
                    if (price != null) {
                        price = ((price.toDouble() / 100) * couponVerification?.couponDetails?.amount!!)
                        //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                        Log.e("TAG","=======CALCULATED PRICE 1=" + price)

                        Log.e("TAG","=======CALCULATED PRICE1 final discount=" + tempsubtotalAmount?.minus(price))
                        //cartListSafe?.get(i)?.price = price
                        couponAmount = price.plus(reedempointdiscount!!)
                    }
                } else if (couponVerification?.couponDetails?.type == Constants.DiscountType.FIXED) {
                    price = couponVerification?.couponDetails?.amount!!
                    //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                    couponAmount = price.plus(reedempointdiscount!!)
                }
                if (cartListSafe?.get(i)?.price!! <= 0) {
                    cartListSafe?.get(i)?.price = 1.0
                }
            }
        }

        Log.e("TAG","=======discounted CALCULATED PRICE =" + discountedPriceAmount)
        //old
        if (discountedPriceAmount!! >= subtotalAmount!!) {
            discountedPriceAmount = subtotalAmount
        }

        if (totalAmount!! <= 0) {
            totalAmount = 1.0
        }
        //adapter?.notifyDataSetChanged()
        getSubTotalPrice()
    }

    private fun updateDiscountedPriceBasedOnID(categoryID: Int?) {
        Log.e("TAG","=======updateDiscountedPriceBasedOnID===" +  finalsubtotal)
        Log.e("TAG","=======updateDiscountedPrice ===" +  finalsubtotal)
        Log.e("TAG","=======updated price ===" +  finalsubtotal.minus(discountedPriceAmount!!))
        tempsubtotalAmount = finalsubtotal.minus(discountedPriceAmount!!)
        finalsubtotal.minus(discountedPriceAmount!!)

        if (cartListSafe?.indices != null) {
            for (i in cartListSafe?.indices!!) {
                Log.e("TAG","=======amount ID=" +couponVerification?.couponDetails?.amount )
                if (cartListSafe?.get(i)?.category_id == categoryID) {
                    //var price = cartListSafe?.get(i)?.price
                    //var price = cartListSafe?.get(i)?.price
                    Log.e("TAG","=======temp subtotal ===" +  tempsubtotalAmount)
                    //var price = finalsubtotal  old code line
                    var price = tempsubtotalAmount
                    Log.e("TAG","=======PRICE ID=" + price)
                    if (couponVerification?.couponDetails?.type == Constants.DiscountType.PERCENTAGE && couponVerification?.couponDetails?.amount != null) {
                        if (price != null) {
                            price = ((price.toDouble() / 100) * couponVerification?.couponDetails?.amount!!)
                            //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                            Log.e("TAG","=======CALCULATED PRICE=" + price)
                            //cartListSafe?.get(i)?.price = price
                            Log.e("TAG","=======CALCULATED PRICE final discount=" + tempsubtotalAmount?.minus(price))
                            //cartListSafe?.get(i)?.price = finalsubtotal.minus(price)
                            Log.e("TAG","=======PRICE=" + cartListSafe?.get(i)?.price)

                            couponAmount = price.plus(reedempointdiscount!!)
                        }
                    } else if (couponVerification?.couponDetails?.type == Constants.DiscountType.FIXED) {
                        price = couponVerification?.couponDetails?.amount!!
                        //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                        couponAmount = price.plus(reedempointdiscount!!)
                    }
                    if (cartListSafe?.get(i)?.price!! <= 0) {
                        cartListSafe?.get(i)?.price = 1.0
                    }
                }
            }
        }

        if (discountedPriceAmount!! >= subtotalAmount!!) {
            discountedPriceAmount = subtotalAmount
        }
        if (totalAmount!! <= 0) {
            totalAmount = 1.0
        }
        //adapter?.notifyDataSetChanged()
        getSubTotalPrice()
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getDeliveryFee())
                ?.execute()
        } else {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.GET_DELIVERY_FEE)
                ?.enque(Network().apis()?.getDeliveryFee())
                ?.execute()
        }
        populateData(deliveryFeeValue)
    }

    @SuppressLint("SetTextI18n")
    override fun itemPosition(position: Int, quantityValue: Int) {
        cartRefresh = true
        if (cartListSafe?.get(position)?.quantity == 1 && quantityValue != +1) {
            if (cartListSafe?.size == 1) {
                Notify.alerterRedButton(
                    activity,
                    getString(R.string.remove_last_item),
                    cartListSafe?.get(position),
                    this)
            } else {
                Notify.alerterRedButton(
                    activity,
                    getString(R.string.remove_item),
                    cartListSafe?.get(position),
                    this
                )
            }
        } else {
            cartRefresh = false
            if (cartListSafe?.get(position)?.quantity!! < cartListSafe?.get(position)?.limit!! || quantityValue == -1) {
                cartListSafe?.get(position)?.quantity = cartListSafe?.get(position)?.quantity?.plus(quantityValue)

                Log.e("TAG","====quantity=====" + cartListSafe?.get(position)?.quantity)
                Log.e("TAG","====price=====" + cartListSafe?.get(position)?.originalPrice)

                cartListSafe?.get(position)?.price =  cartListSafe?.get(position)?.originalPrice
                //cartListSafe!!.get(position)!!.quantity =  cartListSafe?.get(position)?.quantity?.plus(quantityValue - 1)
                adapter!!.notifyItemChanged(position)

                //adapter?.()notifyDataSetChanged

                getSubTotalPrice()

                /*if (!couponVerification?.validForCategory?.type.isNullOrEmpty()
                    && couponVerification?.validForCategory?.id.isNullOrEmpty()
                )
                {
                    Log.e("TAG","====coupon price called=====" + finalsubtotal)
                    finalsubtotal = 0.0
                    discountedPriceAmount =0.0
                    totalAmount = 0.0


                    updateDiscountedPrice()
                }
                else{
                    Log.e("TAG","====coupon priceId called=====")
                    finalsubtotal = 0.0
                    discountedPriceAmount =0.0
                    totalAmount = 0.0

                    updateDiscountedPriceBasedOnID(couponVerification?.validForCategory?.id?.get(0))
                }*/

                HomeActivity.cartList = cartListSafe
                HomeActivity.refreshCart()
            } else {
                Notify.alerterRed(activity, getString(R.string.out_of_stock_err_msg))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun  setDeliveryData() {
        /*   var totalQuantity = 0
           if (!cartListSafe.isNullOrEmpty()) {
               for (i in cartListSafe!!.indices) {
                   totalQuantity += cartListSafe?.get(i)!!.quantity!!
               }
           }*/
        if (subtotalAmount!! > deliveryFeeOrderLimit) {
            Log.e("TAG","===condition called===" + subtotalAmount +">>>>" + deliveryFeeOrderLimit)
            Log.e("TAG","===discounted total amoont===" + discountedPriceAmount)
            deliveryFeeValue = 0
            finaldeliverdiscount = 0.0
        } else {
            if (!deliveryFeeList.isNullOrEmpty())
                deliveryFeeValue = deliveryFeeList[0].value

            Log.e("TAG","===else deliveryFeeValue===" + deliveryFeeValue)
        }

        finalsubtotal = subtotalAmount!!.toDouble()
        Log.e("TAG","===productOrderDiscount===" + productOrderDiscount + "total===" + subtotalAmount)
        if(productOrderDiscount != null) {
            discount_label?.visibility = View.VISIBLE
            val percentage = (productOrderDiscount?.toDouble()!! / 100) * subtotalAmount!!
            Log.e("TAG", "===discounted subtotal===" + percentage)
            //finalsubtotal = subtotalAmount!!.toDouble() - percentage
            //val price = discountedPriceAmount?.plus(percentage)
            discountedPriceAmount = percentage
            Log.e("TAG", "===discounted===" + discountedPriceAmount)
            /* val finaldiscount = coupondiscountvalue?.plus(reedempointdiscount!!)?.plus(percentage)
             Log.e("TAG", "===final discounted===" + finaldiscount)
             discountedPrice?.text = "${Utils.commaConversion(finaldiscount)} ${Constants.currencySign}"*/

            Log.e("TAG", "===value after per===" + subtotalAmount?.minus(discountedPriceAmount!!))

            if (!couponVerification?.validForCategory?.type.isNullOrEmpty()
                && couponVerification?.validForCategory?.id.isNullOrEmpty()
            )
            {
              Log.e("TAG","====coupon price called=====" + finalsubtotal)
                /* finalsubtotal = 0.0
               discountedPriceAmount =0.0
               totalAmount = 0.0
               updateDiscountedPrice()*/

                tempsubtotalAmount =  subtotalAmount?.minus(discountedPriceAmount!!)
                var price = tempsubtotalAmount
                Log.e("TAG","=======amount=" +couponVerification?.couponDetails?.amount )
                if (couponVerification?.couponDetails?.type == Constants.DiscountType.PERCENTAGE && couponVerification?.couponDetails?.amount != null) {
                    if (price != null) {
                        price = ((price.toDouble() / 100) * couponVerification?.couponDetails?.amount!!)
                        //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                        Log.e("TAG","=======CALCULATED PRICE 1=" + price)

                        Log.e("TAG","=======CALCULATED PRICE1 final discount=" + tempsubtotalAmount?.minus(price))
                        //cartListSafe?.get(i)?.price = price
                        couponAmount = price.plus(reedempointdiscount!!)
                    }
                } else if (couponVerification?.couponDetails?.type == Constants.DiscountType.FIXED) {
                    price = couponVerification?.couponDetails?.amount!!
                    //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                    couponAmount = price.plus(reedempointdiscount!!)
                }
            }
            else{
                Log.e("TAG","====coupon priceId called=====")
               /* finalsubtotal = 0.0
                discountedPriceAmount =0.0
                totalAmount = 0.0

                updateDiscountedPriceBasedOnID(couponVerification?.validForCategory?.id?.get(0))*/
                var price = tempsubtotalAmount
                Log.e("TAG","=======PRICE ID=" + price)
                if (couponVerification?.couponDetails?.type == Constants.DiscountType.PERCENTAGE && couponVerification?.couponDetails?.amount != null) {
                    if (price != null) {
                        price = ((price.toDouble() / 100) * couponVerification?.couponDetails?.amount!!)
                        //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                        Log.e("TAG","=======CALCULATED PRICE=" + price)
                        //cartListSafe?.get(i)?.price = price
                        Log.e("TAG","=======CALCULATED PRICE final discount=" + tempsubtotalAmount?.minus(price))
                        //cartListSafe?.get(i)?.price = finalsubtotal.minus(price)

                        couponAmount = price.plus(reedempointdiscount!!)
                    }
                } else if (couponVerification?.couponDetails?.type == Constants.DiscountType.FIXED) {
                    price = couponVerification?.couponDetails?.amount!!
                    //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                    couponAmount = price.plus(reedempointdiscount!!)
                }
            }
        }
        else{
            if (!couponVerification?.validForCategory?.type.isNullOrEmpty()
                && couponVerification?.validForCategory?.id.isNullOrEmpty()
            )
            {
                Log.e("TAG","====coupon price called=====" + finalsubtotal)
                /* finalsubtotal = 0.0
               discountedPriceAmount =0.0
               totalAmount = 0.0

               updateDiscountedPrice()*/

                tempsubtotalAmount =  subtotalAmount?.minus(discountedPriceAmount!!)
                var price = tempsubtotalAmount
                Log.e("TAG","=======amount=" +couponVerification?.couponDetails?.amount )
                if (couponVerification?.couponDetails?.type == Constants.DiscountType.PERCENTAGE && couponVerification?.couponDetails?.amount != null) {
                    if (price != null) {
                        price = ((price.toDouble() / 100) * couponVerification?.couponDetails?.amount!!)
                        //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                        Log.e("TAG","=======CALCULATED PRICE 1=" + price)

                        Log.e("TAG","=======CALCULATED PRICE1 final discount=" + tempsubtotalAmount?.minus(price))
                        //cartListSafe?.get(i)?.price = price
                        couponAmount = price.plus(reedempointdiscount!!)
                    }
                } else if (couponVerification?.couponDetails?.type == Constants.DiscountType.FIXED) {
                    price = couponVerification?.couponDetails?.amount!!
                    //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                    couponAmount = price.plus(reedempointdiscount!!)
                }
            }
            else{
                Log.e("TAG","====coupon priceId called=====")
                /* finalsubtotal = 0.0
                 discountedPriceAmount =0.0
                 totalAmount = 0.0

                 updateDiscountedPriceBasedOnID(couponVerification?.validForCategory?.id?.get(0))*/
                var price = tempsubtotalAmount
                Log.e("TAG","=======PRICE ID=" + price)
                if (couponVerification?.couponDetails?.type == Constants.DiscountType.PERCENTAGE && couponVerification?.couponDetails?.amount != null) {
                    if (price != null) {
                        price = ((price.toDouble() / 100) * couponVerification?.couponDetails?.amount!!)
                        //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                        Log.e("TAG","=======CALCULATED PRICE=" + price)
                        //cartListSafe?.get(i)?.price = price
                        Log.e("TAG","=======CALCULATED PRICE final discount=" + tempsubtotalAmount?.minus(price))
                        //cartListSafe?.get(i)?.price = finalsubtotal.minus(price)

                        couponAmount = price.plus(reedempointdiscount!!)
                    }
                } else if (couponVerification?.couponDetails?.type == Constants.DiscountType.FIXED) {
                    price = couponVerification?.couponDetails?.amount!!
                    //cartListSafe?.get(i)?.price = cartListSafe?.get(i)?.price?.minus(price)
                    couponAmount = price.plus(reedempointdiscount!!)
                }
            }
            //discount_label?.visibility = View.GONE
            /*  val finaldiscount = coupondiscountvalue?.plus(reedempointdiscount!!)
              Log.e("TAG", "===final discounted===" + finaldiscount)
              discountedPrice?.text = "${Utils.commaConversion(finaldiscount)} ${Constants.currencySign}"*/
            //finalsubtotal = subtotalAmount!!.toDouble()
        }
        //deliveryFee?.text = "${Utils.commaConversion(deliveryFeeValue)} ${Constants.currencySign}"
        //subTotal?.text = "${Utils.commaConversion(subtotalAmount)} ${Constants.currencySign}"
        //total?.text = "${Utils.commaConver sion(totalAmount)} ${Constants.currencySign}"

        if(!cartRefresh) {
            if (deliveryDiscount != null && deliveryDiscount!! > 0) {
                Log.e("TAG", "===deliveryFeeValue ===" + deliveryFeeValue)
                finaldeliverdiscount = deliveryFeeValue.minus(deliveryDiscount!!.toDouble())
                Log.e("TAG", "===deliverdiscount ===" + finaldeliverdiscount)
            }
            else{
                finaldeliverdiscount = deliveryFeeValue.toDouble()
                Log.e("TAG", "===deliverdiscount ===" + finaldeliverdiscount)
            }
        }
        else{
            finaldeliverdiscount = deliveryFeeValue.toDouble()
            Log.e("TAG", "===deliverdiscount ===" + finaldeliverdiscount)
        }

        if(finaldeliverdiscount < 0)
        {
            finaldeliverdiscount = 0.0
        }
        Log.e("TAG","===discountedPriceAmount in deliveryData===" + discountedPriceAmount)

        discountedPriceAmount = couponAmount?.plus(discountedPriceAmount!!)

        Log.e("TAG","===finalsubtotal ===" + finalsubtotal)
        Log.e("TAG","===discountedPriceAmount ===" + discountedPriceAmount)

        totalAmount = finalsubtotal
        deliveryFeeValue = finaldeliverdiscount.toInt()
        //totalAmount = discountedPriceAmount?.let { finalsubtotal.minus(it) }

        Log.e("TAG","===deliveryFeeValue ===" + deliveryFeeValue)
        Log.e("TAG","===total ===" + totalAmount)
        if (discountedPriceAmount!! >= subtotalAmount!!) {
            discountedPriceAmount = subtotalAmount
        }
        if (totalAmount!! <= 0) {
            totalAmount = 1.0
        }
        else{
            totalAmount = finalsubtotal.minus(discountedPriceAmount!!)
        }

        if (deliveryFeeValue != 0) {
            subtotalAmount = finalsubtotal.plus(deliveryFeeValue.toDouble())
            totalAmount = totalAmount?.plus(deliveryFeeValue.toDouble())
        }
        Log.e("TAG","===totalAmount ===" + totalAmount)

        Log.e("TAG","===discounted totalAmount ===" + discountedPriceAmount)
        deliveryFee?.text = "${Utils.commaConversion(finaldeliverdiscount)} ${Constants.currencySign}"
        subTotal?.text = "${Utils.commaConversion(finalsubtotal)} ${Constants.currencySign}"
        discountedPrice?.text = "${Utils.commaConversion(discountedPriceAmount)} ${Constants.currencySign}"
        total?.text = "${Utils.commaConversion(totalAmount)} ${Constants.currencySign}"
    }

    @SuppressLint("SetTextI18n")
    private fun getSubTotalPrice() {
        if (cartListSafe != null) {
            subtotalAmount = 0.0
            discountedPriceAmount = 0.0

            for (i in cartListSafe?.indices!!) {
                if (cartListSafe?.get(i)?.price != cartListSafe?.get(i)?.originalPrice) {
                    Log.e("TAG","====originalprice===" +  cartListSafe?.get(i)?.originalPrice)
                    val price = cartListSafe?.get(i)?.originalPrice!! * cartListSafe?.get(i)?.quantity!!
                    subtotalAmount = subtotalAmount?.plus(price)
                    //discountedPriceAmount =  discountedPriceAmount?.plus((cartListSafe?.get(i)?.originalPrice!!.minus(cartListSafe?.get(i)?.price!!)))
                    //discountedPriceAmount = cartListSafe?.get(i)?.originalPrice!!.minus(cartListSafe?.get(i)?.price!!)
                    /* discountedPriceAmount = discountedPriceAmount?.plus((cartListSafe?.get(i)?.originalPrice!!.minus(cartListSafe?.get(i)?.price!!)) * cartListSafe?.get(i)?.quantity!! )*/
                    Log.e("TAG","====discountedPrice in SubTotal1===" + discountedPriceAmount)
                } else {
                    val price = cartListSafe?.get(i)?.originalPrice!! * cartListSafe?.get(i)?.quantity!!
                    subtotalAmount = subtotalAmount?.plus(price)
                }
            }
        }
        totalAmount = subtotalAmount
        //discountedPriceAmount = buyProduct?.discountRedeem?.let { discountedPriceAmount?.plus(it) }
        discountedPriceAmount = buyProduct?.discountRedeem?.plus(discountedPriceAmount!!)
        totalAmount = discountedPriceAmount?.let { subtotalAmount?.minus(it) }
        if (discountedPriceAmount!! >= subtotalAmount!!) {
            discountedPriceAmount = subtotalAmount
        }
        if (totalAmount!! <= 0) {
            totalAmount = 1.0
        }
        coupondiscountvalue = discountedPriceAmount?.plus(reedempointdiscount!!)
        //discountedPrice?.text = "${Utils.commaConversion(discountedPriceAmount)} ${Constants.currencySign}"

        //subTotal?.text = "${Utils.commaConversion(subtotalAmount)} ${Constants.currencySign}"
        //total?.text = "${Utils.commaConversion(totalAmount)} ${Constants.currencySign}"

        Log.e("TAG","====discountedPrice in SubTotal===" + discountedPriceAmount)
        setDeliveryData()
    }

    @SuppressLint("SetTextI18n")
    override fun removeItem(item: Category?) {
        //---------------Removing Item From Cart List and Updating Values + Shopping Cart Icon----------------

        val id = item?.id
        val type = item?.type
        cartListSafe?.remove(item)
        HomeActivity.cartList?.remove(item)
        HomeActivity.refreshCart()
        adapter?.notifyDataSetChanged()
        if (HomeActivity.cartList?.size == 0 || HomeActivity.cartList == null) {

            Log.e("TAG","====removing id===" + item!!.id)

            activity?.onBackPressed()

            //val parentFrag: ProductPurchasingFragment = getParentFragment() as ProductPurchasingFragment
            //parentFrag.openShopDetails()

        } else {
            getSubTotalPrice()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun callDialog(model: Any?) {
        val mapPrices = model as? HashMap<*, *>
        buyProduct?.discountRedeem = mapPrices?.get("discount_price") as Double
        discountedPriceAmount = discountedPriceAmount?.plus(mapPrices.get("discount_price") as Double)

        total?.text = "${Utils.commaConversion(totalAmount)} ${Constants.currencySign}"
        totalUserPointsToRedeem -= mapPrices.get("reward_points") as Double
        buyProduct?.points_discount = buyProduct?.points_discount?.plus(mapPrices["reward_points"] as Double)
        totalUserPoints?.text = "${Utils.commaConversion(totalUserPointsToRedeem)} ReLoop Points"
        btnRedeem?.isClickable = false
        discount_label?.visibility = View.VISIBLE

        Log.e("TAG","===reedem point===" +discountedPriceAmount)
        Log.e("TAG","===reedem point2===" + buyProduct?.discountRedeem)

        Log.e("TAG","===reedem subtotalAmount===" +subtotalAmount)
        Log.e("TAG","===finaldeliverdiscount===" +finaldeliverdiscount)

        subtotalAmount = subtotalAmount?.minus(deliveryFeeValue)
        if (discountedPriceAmount!! >= subtotalAmount!!) {
            discountedPriceAmount = subtotalAmount
        }

        totalAmount = discountedPriceAmount?.let { subtotalAmount?.minus(it) }
        if (totalAmount!! <= 0) {
            totalAmount = 1.0
        }
        Log.e("TAG","===reedem point===" +discountedPriceAmount)
        reedempointdiscount = buyProduct?.discountRedeem
        discountedPrice?.text = "${Utils.commaConversion(discountedPriceAmount)} ${Constants.currencySign}"
        //getSubTotalPrice()
    }
}
