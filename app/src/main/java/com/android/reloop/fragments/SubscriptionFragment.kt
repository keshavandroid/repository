package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.fragments.EditProfileFragment
import com.android.reloop.fragments.NewPaymentMethodsFragment
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.AdapterPreviousSubscription
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.NotifyCallback
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.shop.OneTimeServices
import com.reloop.reloop.network.serializer.subscription.SubscriptionData
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */

class SubscriptionFragment : BaseFragment(), OnNetworkResponse, View.OnClickListener,
    RecyclerViewItemClick, NotifyCallback,AlertDialogCallback {

    companion object {

        fun newInstance(): SubscriptionFragment {
            return SubscriptionFragment()
        }

        var TAG = "SubscriptionFragment"
    }
    var userContainSingleCollectionRequest: Boolean = false
    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null

    var subAdapter: AdapterPreviousSubscription? = null
    var selectedArrayList: ArrayList<SubscriptionData> = ArrayList()

    var subscriptionTitle: TextView? = null
    var subscriptionIcon: ImageView? = null
    var subscriptionDate: TextView? = null
    var subscriptionRemainingDays: TextView? = null
    var linkChangePM: TextView? = null
    var subscriptionAmount: TextView? = null
    var subscriptionStatus: TextView? = null
    var subscription_id: TextView? = null
    var cardView: CardView? = null
    var subscriptionList: ArrayList<SubscriptionData>? = null
    var back: Button? = null
    var btnRecycle: Button? = null
    var subscriptionDateActive: TextView? = null
    var unSubscribe: TextView? = null
    var expiry_date: TextView? = null
    var rlCurrentSubscription: RelativeLayout? = null

    var txtYearlyRenew: TextView? = null

    var idMainSubscription = 0

    private lateinit var dialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_subscription, container, false)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        txtYearlyRenew = view?.findViewById(R.id.txtYearlyRenew)
        recyclerView = view?.findViewById(R.id.previousSubscriptionList)
        subscriptionTitle = view?.findViewById(R.id.subscription_title)
        subscriptionStatus = view?.findViewById(R.id.subscription_status)
        subscriptionAmount = view?.findViewById(R.id.subscription_amount)
        subscriptionRemainingDays = view?.findViewById(R.id.subscription_remaining_days)
        subscriptionDate = view?.findViewById(R.id.subscription_date)
        subscriptionIcon = view?.findViewById(R.id.subscription_icon)
        subscription_id = view?.findViewById(R.id.subscription_id)
        unSubscribe = view?.findViewById(R.id.unsubscribe)
        linkChangePM = view?.findViewById(R.id.linkChangePM)
        expiry_date = view?.findViewById(R.id.expiry_date)
        cardView = view?.findViewById(R.id.cardView)
        back = view?.findViewById(R.id.back)
        btnRecycle = view?.findViewById(R.id.btn_recycle)
        cardView?.visibility = View.GONE
        subscriptionDateActive = view?.findViewById(R.id.subscription_date_active)
        rlCurrentSubscription = view?.findViewById(R.id.rlCurrentSubscription)
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        unSubscribe?.setOnClickListener(this)
        linkChangePM?.setOnClickListener(this)
        btnRecycle?.setOnClickListener(this)
    }

    private fun showProgressDialog() {

        requireActivity().runOnUiThread{
            dialog = Dialog(requireContext())
            if (dialog.window != null) {
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.progress_dialog_layout)
                dialog.show()
            }
        }

    }

    private fun dismissProgressDialog() {
        dialog.dismiss()
    }

    private fun populateData() {

        showProgressDialog()

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.USER_SUBSCRIPTION)
//            ?.autoLoading(requireActivity())//originally added
            ?.enque(Network().apis()?.userSubscriptions())
            ?.execute()

    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.USER_SUBSCRIPTION -> {
                val baseResponse = Utils.getBaseResponse(response)
                val gson = Gson()
                val listType: Type = object : TypeToken<ArrayList<SubscriptionData?>?>() {}.type
                subscriptionList = gson.fromJson(
                    Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>),
                    listType
                )
                if (subscriptionList != null && subscriptionList?.size!! > 0) {
                    cardView?.visibility = View.VISIBLE
                    val freeSubscription: SubscriptionData? = subscriptionList?.find { !it.free_subscription_expiry.isNullOrEmpty() }

                    if (freeSubscription != null && freeSubscription.status == OrderHistoryEnum.ACTIVE && subscriptionList?.get(0)!!.subscription_type!=Constants.RecycleCategoryType.FREE_SERVICE) {
                        //------------------------Set Header Subscription Label -----------------
                        idMainSubscription = freeSubscription.id!!
                        subscriptionAmount?.text =
                            "${Constants.currencySign} ${Utils.commaConversion(freeSubscription?.subscription?.price)}"
                        subscriptionTitle?.text = "${freeSubscription?.subscription?.name}"
                        if (freeSubscription?.start_date == null && freeSubscription?.end_date == null) {
                            subscriptionDateActive?.text =
                                Utils.getFormattedServerDate(freeSubscription?.created_at)
                            subscriptionDate?.visibility = View.GONE
                        } else {
                            if (freeSubscription?.start_date.isNullOrEmpty()) {
                                subscriptionDateActive?.text =
                                    Utils.getFormattedServerDate(freeSubscription?.created_at)
                            } else {
                                subscriptionDateActive?.text =
                                    Utils.getFormattedServerDate(freeSubscription?.start_date)
                            }
                            if (freeSubscription?.free_subscription_expiry.isNullOrEmpty()) {
                                subscriptionDate?.visibility = View.GONE
                                expiry_date?.visibility = View.GONE
                            } else {
                                subscriptionDate?.text =
                                    Utils.getFormattedServerDate(freeSubscription?.end_date)
                            }

                        }
                        subscriptionRemainingDays?.text = "Remaining Trips: ${freeSubscription?.trips}"
                        subscription_id?.text = freeSubscription?.subscription_number
                        Utils.glideImageLoaderServer(
                            subscriptionIcon,
                            freeSubscription?.subscription?.avatar,
                            R.drawable.icon_subscription_page_en
                        )
                        var expireDateCheck = false
                        try {
                            val dateFormat: DateFormat = SimpleDateFormat(Utils.dateFormat)
                            val endDateString =
                                Utils.getFormattedServerDate(freeSubscription?.free_subscription_expiry)
                            val currentDate = Calendar.getInstance()
                            val endDate = dateFormat.parse(endDateString!!)
                            if (endDate!!.before(currentDate.time)) {
                                expireDateCheck = true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (freeSubscription.status == OrderHistoryEnum.SUB_REFUND_REQUEST){
                            rlCurrentSubscription?.visibility = View.GONE
                        }

                        if (freeSubscription.status == OrderHistoryEnum.ACTIVE && !expireDateCheck) {

                            Log.d("STATUS_SUB"," 444 ");

                            subscriptionStatus?.text = "Active"

                        } else if (freeSubscription.status == OrderHistoryEnum.EXPIRED || freeSubscription.trips == 0 || (expireDateCheck && freeSubscription?.status != OrderHistoryEnum.SUB_CANCELLED)
                        ) {

                            //ORIGINAL ADDED
//                            freeSubscription.status = OrderHistoryEnum.EXPIRED


                            var status = ""
                            status = if (expireDateCheck) {
                                "Ended"
                            } else {
                                "Used"
                            }

                            Log.d("STATUS_SUB"," 333 " + status);

                            subscriptionStatus?.text = status
                            subscriptionStatus?.setTextColor(requireActivity().getColor(R.color.color_headline_text))
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_empty_order_history)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext()
                                    .getColor(R.color.color_headline_text)
                            )
                        } else if (freeSubscription.status == OrderHistoryEnum.PENDING) {

                            Log.d("STATUS_SUB"," 222 ");

                            subscriptionStatus?.text = "Valid"
                            subscriptionStatus?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.white)
                            )
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_valid_subsscriptions)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.colorPrimary)
                            )
                        }

                        //--------------Status Login For hiding/showing unsubscribe Button
                        unSubscribe?.visibility = View.INVISIBLE
                        expiry_date?.visibility = View.VISIBLE
                        expiry_date?.text = Utils.getFormattedServerDate(freeSubscription?.free_subscription_expiry)
//                        unSubscribe?.text =  Utils.getFormattedServerDate(freeSubscription?.free_subscription_expiry)
                        /* unSubscribe?.setText("Free")
                         unSubscribe?.isEnabled = false*/
                        /*     if (freeSubscription?.status == OrderHistoryEnum.ACTIVE || freeSubscription?.status == OrderHistoryEnum.PENDING
                             ) {
                                 if (freeSubscription?.end_date.isNullOrEmpty()) {
                                     unSubscribe?.visibility = View.GONE
                                 } else
                                     unSubscribe?.visibility = View.VISIBLE
                             } else if (freeSubscription?.status == OrderHistoryEnum.CANCELLED || freeSubscription?.status == OrderHistoryEnum.EXPIRED || freeSubscription?.status == OrderHistoryEnum.COMPLETED_SUB
                             ) {
                                 unSubscribe?.visibility = View.GONE
                             }*/
                        //---------------------------------------
                        subscriptionList?.remove(freeSubscription)
                    }
                    else {
                        //------------------------Set Header Subscription Label -----------------
                        idMainSubscription = subscriptionList?.get(0)?.id!!
                        subscriptionAmount?.text =
                            "${Constants.currencySign} ${
                                Utils.commaConversion(
                                    subscriptionList?.get(
                                        0
                                    )?.subscription?.price
                                )
                            }"
                        subscriptionTitle?.text = "${subscriptionList?.get(0)?.subscription?.name}"
                        if (subscriptionList?.get(0)?.start_date == null && subscriptionList?.get(0)?.end_date == null) {
                            subscriptionDateActive?.text =
                                Utils.getFormattedServerDate(subscriptionList?.get(0)?.created_at)
                            subscriptionDate?.visibility = View.GONE
                        } else {
                            if (subscriptionList?.get(0)?.start_date.isNullOrEmpty()) {
                                subscriptionDateActive?.text =
                                    Utils.getFormattedServerDate(subscriptionList?.get(0)?.created_at)
                            } else {
                                subscriptionDateActive?.text =
                                    Utils.getFormattedServerDate(subscriptionList?.get(0)?.start_date)
                            }
                            if (subscriptionList?.get(0)?.end_date.isNullOrEmpty()) {
                                subscriptionDate?.visibility = View.GONE
                            } else {
                                subscriptionDate?.text =
                                    Utils.getFormattedServerDate(subscriptionList?.get(0)?.end_date)
                            }

                        }
                        subscriptionRemainingDays?.text =
                            "Remaining Trips: ${subscriptionList?.get(0)?.trips}"
                        subscription_id?.text = subscriptionList?.get(0)?.subscription_number
                        Utils.glideImageLoaderServer(
                            subscriptionIcon,
                            subscriptionList?.get(0)?.subscription?.avatar,
                            R.drawable.icon_placeholder_generic
                        )
                        var expireDateCheck = false
                        try {

                            //New AD

                            if(subscriptionList!=null && !subscriptionList.isNullOrEmpty()){
                                if(!subscriptionList!!.get(0).end_date.isNullOrEmpty()){
                                    val dateFormat: DateFormat = SimpleDateFormat(Utils.dateFormat)
                                    val endDateString = Utils.getFormattedServerDate(subscriptionList?.get(0)?.end_date)
                                    val currentDate = Calendar.getInstance()
                                    val endDate = dateFormat.parse(endDateString!!)
                                    if (endDate!!.before(currentDate.time)) {
                                        expireDateCheck = true
                                    }
                                }
                            }



                            //Original
                            /*val dateFormat: DateFormat = SimpleDateFormat(Utils.dateFormat)
                            val endDateString = Utils.getFormattedServerDate(subscriptionList?.get(0)?.end_date)
                            val currentDate = Calendar.getInstance()
                            val endDate = dateFormat.parse(endDateString!!)
                            if (endDate!!.before(currentDate.time)) {
                                expireDateCheck = true
                            }*/



                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        if (freeSubscription?.status == OrderHistoryEnum.SUB_REFUND_REQUEST){
                            rlCurrentSubscription?.visibility = View.GONE
                        }
                        if (subscriptionList?.get(0)?.status == OrderHistoryEnum.ACTIVE && !expireDateCheck) {

                            Log.d("STATUS_SUB"," 111 ");

                            subscriptionStatus?.text = "Active"

                        } else if (subscriptionList?.get(0)?.status == OrderHistoryEnum.EXPIRED || subscriptionList?.get(
                                0
                            )?.trips == 0 || (expireDateCheck && subscriptionList?.get(0)?.status != OrderHistoryEnum.SUB_CANCELLED)
                        ) {

                            //ORIGINAL ADDED
//                            subscriptionList?.get(0)?.status = OrderHistoryEnum.EXPIRED

                            var status = ""
                            status = if (expireDateCheck) {
                                "Ended"
                            } else {
                                "Used"
                            }

                            Log.d("STATUS_SUB"," 666 " + status);

                            subscriptionStatus?.text = status
                            subscriptionStatus?.setTextColor(requireActivity().getColor(R.color.color_headline_text))
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_empty_order_history)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext()
                                    .getColor(R.color.color_headline_text)
                            )
                        } else if (subscriptionList?.get(0)?.status == OrderHistoryEnum.PENDING) {

                            Log.d("STATUS_SUB"," 555 ");

                            subscriptionStatus?.text = "Valid"
                            subscriptionStatus?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.white)
                            )
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_valid_subsscriptions)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.colorPrimary)
                            )
                        }
                        if (subscriptionList?.get(0)?.free_subscription_expiry.isNullOrEmpty()) {
                            unSubscribe?.visibility = View.VISIBLE
                            expiry_date?.visibility = View.INVISIBLE
                        } else {
                            unSubscribe?.visibility = View.INVISIBLE
                            expiry_date?.visibility = View.VISIBLE
                            expiry_date?.text = Utils.getFormattedServerDate(subscriptionList?.get(0)?.free_subscription_expiry)
                        }


                        //OLD AD  unsubscribe Button =======================
                        /*if (subscriptionList?.get(0)?.status == OrderHistoryEnum.ACTIVE || subscriptionList?.get(
                                0
                            )?.status == OrderHistoryEnum.PENDING
                        ) {
                            Log.d("DATA_SUB","111")
                            if (subscriptionList?.get(0)?.end_date.isNullOrEmpty()) {
                                unSubscribe?.visibility = View.GONE
                                Log.d("DATA_SUB","222")
                            } else
                                if (subscriptionList?.get(0)?.free_subscription_expiry.isNullOrEmpty()) {
                                    Log.d("DATA_SUB","333")
                                    unSubscribe?.visibility = View.VISIBLE
                                    expiry_date?.visibility = View.GONE
                                } else {
                                    Log.d("DATA_SUB","444")
                                    unSubscribe?.visibility = View.INVISIBLE
                                    expiry_date?.visibility = View.VISIBLE
                                }
                        } else if (subscriptionList?.get(0)?.status == OrderHistoryEnum.SUB_CANCELLED || subscriptionList?.get(
                                0
                            )?.status == OrderHistoryEnum.EXPIRED || subscriptionList?.get(0)?.status == OrderHistoryEnum.COMPLETED_SUB
                        ) {
                            Log.d("DATA_SUB","555")
                            unSubscribe?.visibility = View.GONE
                        }*/



                        // NEW AD CHANGE for unsubscribe button ======================================
                        if (subscriptionList?.get(0)?.status == OrderHistoryEnum.SUB_CANCELLED) {
                            unSubscribe?.visibility = View.GONE
                        } else {
                            if (!subscriptionList?.get(0)?.start_date.isNullOrEmpty()) {
                                if (subscriptionList?.get(0)?.free_subscription_expiry.isNullOrEmpty()) {
                                    unSubscribe?.visibility = View.VISIBLE
                                } else {
                                    unSubscribe?.visibility = View.GONE
                                }
                            } else {
                                unSubscribe?.visibility = View.GONE
                            }
                        }

                        if (subscriptionList?.get(0)?.status == OrderHistoryEnum.SUB_REFUNDED) {
                            unSubscribe?.visibility = View.GONE
                        }
                        // NEW CHANGE for unsubscribe button ======================================






                        //CHANGE SUBSCRIPTION STATUS LABEL NEW ===========================================
                        if(subscriptionList!!.get(0).status == OrderHistoryEnum.PENDING){
                            Log.d("STATUS_SUB"," PENDING ");

                            subscriptionStatus?.text = "Valid"
                            subscriptionStatus?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.white)
                            )
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_valid_subsscriptions)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.colorPrimary)
                            )
                        }else if(subscriptionList!!.get(0).status == OrderHistoryEnum.ACTIVE){
                            subscriptionStatus?.text = "Active"

                            subscriptionStatus?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.white)
                            )
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_order_history_completed)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext().getColor(R.color.colorPrimary)
                            )

                        }else if(subscriptionList!!.get(0).status == OrderHistoryEnum.COMPLETED_SUB){
                            subscriptionStatus?.text = "Used"

                            subscriptionStatus?.setTextColor(requireActivity().getColor(R.color.color_headline_text))
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_empty_order_history)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext()
                                    .getColor(R.color.color_headline_text)
                            )

                        }else if(subscriptionList!!.get(0).status == OrderHistoryEnum.EXPIRED){
                            subscriptionStatus?.text = "Expired"

                            subscriptionStatus?.setTextColor(requireActivity().getColor(R.color.color_headline_text))
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_empty_order_history)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext()
                                    .getColor(R.color.color_headline_text)
                            )

                        }else if(subscriptionList!!.get(0).status == OrderHistoryEnum.SUB_CANCELLED){
                            subscriptionStatus?.text = "Unsubscribed"

                            subscriptionStatus?.setTextColor(requireActivity().getColor(R.color.color_headline_text))
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_empty_order_history)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext()
                                    .getColor(R.color.color_headline_text)
                            )

                        }else if(subscriptionList!!.get(0).status == OrderHistoryEnum.SUB_REFUNDED){
                            subscriptionStatus?.text = "Refunded"

                            subscriptionStatus?.setTextColor(requireActivity().getColor(R.color.color_headline_text))
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_empty_order_history)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext()
                                    .getColor(R.color.color_headline_text)
                            )

                        }else if(subscriptionList!!.get(0).status == OrderHistoryEnum.SUB_REFUND_REQUEST){
                            subscriptionStatus?.text = "Renewed"

                            subscriptionStatus?.setTextColor(requireActivity().getColor(R.color.color_headline_text))
                            subscriptionStatus?.background =
                                MainApplication.applicationContext()
                                    .getDrawable(R.drawable.shape_empty_order_history)
                            subscriptionDateActive?.setTextColor(
                                MainApplication.applicationContext()
                                    .getColor(R.color.color_headline_text)
                            )

                        }


                        //SHOW "Next Payment date" if yearly_renew is not null
                        if(!subscriptionList!!.get(0).yearly_renew.isNullOrEmpty()){
                            txtYearlyRenew!!.visibility = View.VISIBLE
                            txtYearlyRenew?.text = "Next Payment : " + Utils.getFormattedServerDate(subscriptionList?.get(0)?.yearly_renew)
                        }else{
                            txtYearlyRenew!!.visibility = View.GONE
                        }




                        //---------------------------------------
                        //------------------Manage Other List---------------------------
                        subscriptionList?.removeAt(0)
                    }



                    if (subscriptionList == null) {
                        subscriptionList = ArrayList()
                    }
                    recyclerViewData(subscriptionList)
                }
            }
            RequestCodes.API.UNSUBSCRIBE -> {
                populateData()
            }
            RequestCodes.API.GET_PLAN -> {
                val baseResponse = Utils.getBaseResponse(response)
                val getPlans = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    GetPlans::class.java)
                LogManager.getLogManager().writeLog("Event Home Page : Plan Service Result : ${Gson().toJson(getPlans)}")
                handleUserPlansScenario(getPlans)

                val update = Handler()
                update.postDelayed(
                    {
                        btnRecycle?.isClickable = true
                    }, 1000
                )
            }
        }
    }


    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {

        if(tag == RequestCodes.API.GET_PLAN)
        {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

            LogManager.getLogManager().writeLog("Event Home Page : Plan Service Result : ${gson.toJson(response)}")
        }
        btnRecycle?.isClickable = true
        Notify.alerterRed(activity, response?.message)
    }

    private fun handleUserPlansScenario(getPlans: GetPlans?) {
        //------------------------Check If User has Bought Trips---------------------------
        val userContainsTripsMonthly: UserPlans? =
            getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.NORMAL }
        val userContainsTripsBulky: UserPlans? =
            getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.BULKY_ITEM }

        val userContainsTripsSingleCollection: UserPlans? =
            getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.SINGLE_COLLECTION }

        val userContainsFreeTrips: UserPlans? =
            getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.FREE_SERVICE }
        if (userContainsTripsMonthly != null || userContainsTripsBulky != null || userContainsTripsSingleCollection != null || userContainsFreeTrips != null) {
            if (MainApplication.userType() == Constants.UserType.household) {
                if (User.retrieveUser()?.first_name.isNullOrEmpty()
                    || User.retrieveUser()?.last_name.isNullOrEmpty()
                    || User.retrieveUser()?.addresses.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.building_name.isNullOrEmpty()
                    || User.retrieveUser()?.phone_number.isNullOrEmpty()
//                    || User.retrieveUser()?.gender.isNullOrEmpty()
//                        || User.retrieveUser()?.birth_date.isNullOrEmpty() //Earlier mandatory, now optional
                ) {
                    Notify.hyperlinkAlert(activity, getString(R.string.update_profile_msg), getString(R.string.update_profile_heading), this, 2)
                    btnRecycle?.isClickable = true
                } else {
                    //goToNextScreen(getPlans)

                    val newlist = getPlans.userPlans!!.reversed()
                   Log.e(TAG,"===Event Home Page :newlist : ${Gson().toJson(newlist)}")
                    if(newlist.get(0).trips == 0)
                    {
                        Notify.hyperlinkAlert(
                            activity,
                            "Please Subscribe through the ReLoop Store",
                            "Go to Reloop Store",
                            this, 1
                        )
                    }else{
                        goToNextScreen(getPlans)
                    }
                    /*Log.e("TAG","=====subscription type ====" + getPlans.userPlans!!.get(0).subscription_type)
                    Log.e("TAG","=====trips ====" + getPlans.userPlans!!.get(0).trips)
                    if(getPlans.userPlans!!.get(0).subscription_type == Constants.RecycleCategoryType.NORMAL && getPlans.userPlans!!.get(0).trips ==0 )
                    {
                        //showPurchaseInfoDialog(getPlans)
                        Notify.hyperlinkAlert(
                            activity,
                            "You are already subscribed for a monthly service. If you require extra trips, you can request a one-time service to avoid paying for another monthly subscription",
                            "Go to Reloop Store",
                            this, 1
                        )
                    }
                    else {
                        goToNextScreen(getPlans)
                    }*/
                }
            } else {
                if (User.retrieveUser()?.organization?.name.isNullOrEmpty()
                    || User.retrieveUser()?.addresses.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                    || User.retrieveUser()?.addresses?.get(0)?.building_name.isNullOrEmpty()
                    || User.retrieveUser()?.phone_number.isNullOrEmpty()
                ) {
                    Notify.hyperlinkAlert(
                        activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2
                    )
                    btnRecycle?.isClickable = true
                    return
                } else {
                    //Perform Function
                    //goToNextScreen(getPlans)
                    val newlist = getPlans.userPlans!!.reversed()
                    Log.e(TAG,"===Event Home Page :newlist : ${Gson().toJson(newlist)}")
                    if(newlist.get(0).trips == 0)
                    {
                        Notify.hyperlinkAlert(
                            activity,
                            "Please Subscribe through the ReLoop Store",
                            "Go to Reloop Store",
                            this, 1)
                    }else{
                        goToNextScreen(getPlans)
                    }
                    /*Log.e("TAG","=====subscription type ====" + getPlans.userPlans!!.get(0).subscription_type)
                    Log.e("TAG","=====trips ====" + getPlans.userPlans!!.get(0).trips)
                    if(getPlans.userPlans!!.get(0).subscription_type == Constants.RecycleCategoryType.NORMAL && getPlans.userPlans!!.get(0).trips ==0 )
                    {
                        //showPurchaseInfoDialog(getPlans)
                        Notify.hyperlinkAlert(
                            activity,
                            "You are already subscribed for a monthly service. If you require extra trips, you can request a one-time service to avoid paying for another monthly subscription",
                            "Go to Reloop Store",
                            this, 1
                        )
                    }
                    else {
                        goToNextScreen(getPlans)
                    }*/
                }
            }

        } else {
            Notify.hyperlinkAlert(
                activity,
                "Please Subscribe through the ReLoop Store",
                "Go to Reloop Store",
                this, 1
            )
        }
    }

    private fun goToNextScreen(getPlans: GetPlans) {

        LogManager.getLogManager().writeLog("Event Home Page : Showing Recycle Page")
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

            val OTS: OneTimeServices? = getPlans.oneTimeServices?.find { it.category_type == Constants.RecycleCategoryType.SINGLE_COLLECTION }
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
                        getPlans.oneTimeServices?.get(2)?.price!!)*/
            }
        }

        userContainSingleCollectionRequest = userContainsSingleCollection != null

        BaseActivity.replaceFragment(childFragmentManager,
            Constants.Containers.subscrptionFragmentContainer,
            RecycleFragment.newInstance(getPlans, userContainSingleCollectionRequest,null),
            Constants.TAGS.RecycleFragment)
    }

    private fun recyclerViewData(subscriptionList: ArrayList<SubscriptionData>?) {


        Log.d("SIZE_SUB",""+ subscriptionList!!.size)
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = linearLayoutManager

        //OLD
//        recyclerView?.adapter = AdapterPreviousSubscription(subscriptionList, this)

        //NEW
        recyclerView?.adapter = AdapterPreviousSubscription(filterSubscriptions(subscriptionList!!), this)


        /*recyclerView!!.viewTreeObserver.addOnPreDrawListener(object :
            ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (recyclerView!!.childCount > 0) {
                    // RecyclerView items are loaded and displayed
                    dismissProgressDialog()
                    recyclerView!!.viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })*/

        if (recyclerView?.adapter?.itemCount == 0) {
            dismissProgressDialog()
        } else {
            recyclerView!!.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (recyclerView!!.childCount > 0) {
                        // RecyclerView items are loaded and displayed
                        dismissProgressDialog()
                        recyclerView!!.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    return true
                }
            })
        }

    }

    fun filterSubscriptions(subscriptioList: ArrayList<SubscriptionData>): ArrayList<SubscriptionData> {
        val selectedArray: ArrayList<SubscriptionData> = ArrayList()
        for (listItem in subscriptioList) {
            if (listItem.status != OrderHistoryEnum.SUB_REFUND_REQUEST) {
                selectedArray.add(listItem)
            }

        }
        return selectedArray
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
            R.id.unsubscribe -> {
                AlertDialogs.unSubscribePopup(
                    requireActivity(),
                    this, idMainSubscription
                )
            }
            R.id.linkChangePM -> {
                BaseActivity.replaceFragment(childFragmentManager,
                    Constants.Containers.subscrptionFragmentContainer,
                    NewPaymentMethodsFragment.newInstance(),
                    Constants.TAGS.RecycleFragment)
            }
            R.id.btn_recycle ->
            {
                LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                btnRecycle?.isClickable = false
                checkUserInfo()
                /*if(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                    btnRecycle?.isClickable = false
                    checkUserInfo()
                }
                else
                {
                    EasyPermissions.requestPermissions(
                        requireActivity(),
                        getString(R.string.write_external_storage),
                        RequestCodes.RC_STORAGE_PERM,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }*/
            }

        }
    }

    private fun checkUserInfo() {
        LogManager.getLogManager().writeLog("Event Home Page : Executing Plan Service Call Token : ${User.retrieveUser()?.api_token}")
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlan())
            ?.execute()
    }

    override fun itemPosition(position: Int) {
        AlertDialogs.unSubscribePopup(
            requireActivity(),
            this, subscriptionList?.get(position)?.id!!
        )
    }

    fun unSubscribeApi(id: Int?) {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.UNSUBSCRIBE)
            //?.autoLoading(requireActivity())
            ?.autoLoading(activity)
            ?.enque(
                Network().apis()?.unSubscribe(id.toString())
            )
            ?.execute()
    }

    override fun callNotify(model: Any?) {
        var type = model as Int
        unSubscribeApi(type)
    }

    override fun callDialog(model: Any?) {
        if (model as Int == 1) {
            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
            HomeActivity.bottomNav.selectedItemId = R.id.navigation_shop
        } else if (model == 2) {
//            HomeActivity.settingClicked = true
//            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
//            HomeActivity.bottomNav.selectedItemId = R.id.navigation_settings


            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.subscrptionFragmentContainer,
                EditProfileFragment.newInstance(),
                Constants.TAGS.EditProfileFragment
            )


        }
    }

}
