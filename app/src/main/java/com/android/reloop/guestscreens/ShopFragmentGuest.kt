package com.reloop.reloop.fragments


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.fragments.EditProfileFragment
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity.Companion.replaceFragment
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.AdapterShopFragment
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ShopFragmentItemClick
import com.reloop.reloop.model.ModelShopCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


/**
 * A simple [Fragment] subclass.
 */
class ShopFragmentGuest : BaseFragment(), OnNetworkResponse, ShopFragmentItemClick ,
    AlertDialogCallback {

    companion object {
        private var from: String = ""
        var TAG = "ShopFragment"
        fun newInstance(s: String): ShopFragmentGuest {

            from = s
            return ShopFragmentGuest()
        }
    }

    private var pos: Int ? = null
    private var typei: Int? = null
    private var getPlansAll: GetPlans? = null

    var childToParent: ChildToParent? = null
    private var tvNoDataFound: TextView? = null
    private var llRoot: LinearLayout? = null
    var recyclerView: RecyclerView? = null
    var dataListServices: ArrayList<ModelShopCategories> = ArrayList()
    var dataListProducts: ArrayList<ModelShopCategories> = ArrayList()
    var product_listRecyclerView: RecyclerView? = null

    var linearLayoutManager: LinearLayoutManager? = null
    var apiList: List<Category>? = null
    var apiListServices: ArrayList<Category>? = ArrayList()
    var apiListProducts: ArrayList<Category>? = ArrayList()


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (apiList == null || apiList?.size == 0)
                populateData()
        }
    }


    override fun onResume() {
        super.onResume()
        if (apiList == null || apiList?.size == 0)
            populateData()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_shop_guest, container, false)
        initViews(view)
        return view
    }


    private fun initViews(view: View?) {
        recyclerView = view?.findViewById(R.id.rV_shop_categories)
        product_listRecyclerView = view?.findViewById(R.id.product_list)
        tvNoDataFound = view?.findViewById(R.id.tvNoDataFound)
        llRoot = view?.findViewById(R.id.ll_root)
    }

    private fun populateData() {

        //original
        if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.autoLoading(requireActivity())
                ?.setTag(RequestCodes.API.CATEGORIES)
                ?.enque(Network().apis()?.categories())
                ?.execute()
        }

    }

    private fun populateRecyclerViewData() {
        dataListServices.clear()
        val listIconsServices: ArrayList<Int> = ArrayList()
//        val listIconsProducts: ArrayList<Int> = ArrayList()
//        listIconsProducts.clear()
        listIconsServices.clear()
        if (!apiList.isNullOrEmpty()) {
            for (i in apiList!!.indices) {
                when (i) {
                    0 -> {
                        listIconsServices.add(R.drawable.icon_monthly_subscription)
                    }
                    1 -> {
                        listIconsServices.add(R.drawable.icon_added_service)
                    }
                    2 -> {
                        listIconsServices.add(R.drawable.icon_recycling_boxes)
                    }
                    3 -> {
                        listIconsServices.add(R.drawable.icon_environmental_products)
                    }
                    else -> {
                        if (apiList!![i].type == Constants.serviceType) {
                            if (apiList!![i].service_type == 2) {
                                listIconsServices.add(R.drawable.icon_added_service)
                            } else
                                listIconsServices.add(R.drawable.icon_monthly_subscription)
                        } else {
                            listIconsServices.add(R.drawable.icon_recycling_boxes)
                        }

                    }

                }
            }
            for (i in apiList?.indices!!) {
                if (apiList!![i].type == Constants.serviceType) {
                    apiListServices?.add(apiList!![i])
                    dataListServices.add(
                        ModelShopCategories(
                            listIconsServices[i], apiList!![i].name!!, apiList!![i].type
                        )
                    )
                } else
                    if (apiList!![i].type == Constants.productType) {
                        apiListProducts?.add(apiList!![i])
                        dataListProducts.add(ModelShopCategories(listIconsServices[i], apiList!![i].name!!, apiList!![i].type))
                    }

            }
        } else {
            llRoot?.visibility = View.GONE
            tvNoDataFound?.visibility = View.VISIBLE
        }

        val gson1 = Gson()
        val jsonCurProduct = gson1.toJson(apiList)
        val jsonCurProduct1 = gson1.toJson(dataListServices)


        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("subscription", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putString("list", jsonCurProduct)
        editor.putString("list1", jsonCurProduct1)
        editor.commit()


//            }
        // Set CustomAdapter as the adapter for RecyclerView.
        linearLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = linearLayoutManager
        product_listRecyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = AdapterShopFragment(dataListServices, this)
        product_listRecyclerView?.adapter = AdapterShopFragment(dataListProducts, this)

        if(from.equals("fromPopup"))
        {
            typei= 1
            pos = 1

            checkUserInfo()
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.CATEGORIES -> {
                val baseResponse = Utils.getBaseResponse(response)

                val gson = Gson()
                val listType: Type = object : TypeToken<List<Category?>?>() {}.type
                apiList = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)

                populateRecyclerViewData()
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
        Notify.alerterRed(activity, response?.message)
    }

    private fun handleUserPlansScenario(getPlans: GetPlans?) {
        //------------------------Check If User has Bought Trips---------------------------

        try {
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

                    Log.e("TAG", "===street====" + User.retrieveUser()?.addresses?.get(0)?.street)
                    if (User.retrieveUser()?.first_name.isNullOrEmpty()
                        || User.retrieveUser()?.last_name.isNullOrEmpty()
                        || User.retrieveUser()?.addresses.isNullOrEmpty()
                        || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                        || User.retrieveUser()?.addresses?.get(0)?.building_name.isNullOrEmpty()
                        || User.retrieveUser()?.phone_number.isNullOrEmpty()
//                        || User.retrieveUser()?.gender.isNullOrEmpty()
//                        || User.retrieveUser()?.birth_date.isNullOrEmpty() //Earlier mandatory, now optional
                    ) {
                        Notify.hyperlinkAlert(
                            activity,
                            getString(R.string.update_profile_msg),
                            getString(R.string.update_profile_heading),
                            this, 2
                        )
                        replaceFragment(
                            childFragmentManager,
                            Constants.Containers.shopFragmentContainer,
                            EditProfileFragment.newInstance(),
                            Constants.TAGS.EditProfileFragment
                        )
                        //activity?.onBackPressed()
                    } else {
                        goToNextScreen()
                    }
                } else {
                    if (User.retrieveUser()?.organization?.name.isNullOrEmpty()
                        || User.retrieveUser()?.addresses.isNullOrEmpty()
                        || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                        || User.retrieveUser()?.addresses?.get(0)?.building_name.isNullOrEmpty()
                        || User.retrieveUser()?.phone_number.isNullOrEmpty()
                    ) {
                        Notify.hyperlinkAlert(activity, getString(R.string.update_profile_msg), getString(R.string.update_profile_heading), this, 2)
                        // activity?.onBackPressed()
                        replaceFragment(
                            childFragmentManager,
                            Constants.Containers.shopFragmentContainer,
                            EditProfileFragment.newInstance(),
                            Constants.TAGS.EditProfileFragment
                        )
                    } else {
                        //Perform Function
                        goToNextScreen()
                    }
                }

            } else {
                //Notify.hyperlinkAlert(activity, "Please Subscribe through the ReLoop Store", "Go to Reloop Store", this, 1)

                goToNextScreen()
            }
        }
        catch (e : IndexOutOfBoundsException)
        {
            e.printStackTrace()
        }
        catch (e : NullPointerException)
        {
            e.printStackTrace()
        }
    }

    private fun goToNextScreen() {
        val fragment = ShopDetailFragment.newInstance()
        val args = Bundle()
        if (typei == Constants.serviceType) {

            //check profile updated or not
              args.putString(
                  Constants.DataConstants.shopFragmentHeading,
                  dataListServices[pos!!].heading
              )
              args.putInt(
                  Constants.DataConstants.shopFragmentIcon,
                  dataListServices[pos!!].icons!!
              )
              args.putInt(
                  Constants.DataConstants.position,
                  pos!!
              )
              args.putInt(
                  Constants.DataConstants.Api.productID,
                  apiListServices?.get(pos!!)?.id!!
              )
              args.putInt(
                  Constants.DataConstants.Api.productType,
                  apiListServices?.get(pos!!)?.type!!
              )
              args.putInt(
                  Constants.DataConstants.Api.serviceType,
                  apiListServices?.get(pos!!)?.service_type!!
              )
        }
        else {
            args.putString(
                Constants.DataConstants.shopFragmentHeading,
                dataListProducts[pos!!].heading
            )
            args.putInt(
                Constants.DataConstants.shopFragmentIcon,
                dataListProducts[pos!!].icons!!
            )
            args.putInt(
                Constants.DataConstants.position,
                pos!!
            )
            args.putInt(
                Constants.DataConstants.Api.productID,
                apiListProducts?.get(pos!!)?.id!!
            )
            args.putInt(
                Constants.DataConstants.Api.productType,
                apiListProducts?.get(pos!!)?.type!!
            )
        }
        fragment.arguments = args
        replaceFragment(
            childFragmentManager,
            Constants.Containers.shopFragmentContainer,
            fragment,
            Constants.TAGS.ShopDetailFragment
        )
    }

    override fun itemPosition(position: Int, type: Int?) {
        AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

    }

    fun checkUserInfo() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlan())
            ?.execute()
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
