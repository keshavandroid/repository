package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.reloop.adapters.CustomPagerAdapter
import com.android.reloop.constants.EnvironmentalStatsDescriptionsIDs
import com.android.reloop.fragments.CampainDetailFragment
import com.android.reloop.network.serializer.Campain.Campaigns.CampaignImage
import com.android.reloop.network.serializer.Campain.Campaigns.GetCampaigns
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.android.reloop.network.serializer.dashboard.SettingsModel
import com.android.reloop.utils.LogManager
import com.android.reloop.utils.ViewPagerDots
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity.Companion.replaceFragment
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.AdapterHomeRecyclerView
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.model.ModelHomeCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription
import com.reloop.reloop.network.serializer.shop.OneTimeServices
import com.reloop.reloop.network.serializer.user.User
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
class HomeFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse, AlertDialogCallback ,CustomPagerAdapter.ItemClickListener{
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
        var settings = SettingsModel()
    }
    private var mContext: Context? = null
    private var campaignList = ArrayList<GetCampaigns?>()
    var campainList: ArrayList<CampaignImage>? = ArrayList()
    var campainListNew: ArrayList<String>? = ArrayList()

    private lateinit var linearLayoutManager: LinearLayoutManager
    var rvHomeCategories: RecyclerView? = null
    var dataList: ArrayList<ModelHomeCategories> = ArrayList()
    var recycle: Button? = null
    var btnTrackRequest: TextView? = null
    var totalUserPoints: TextView? = null
    var userContainSingleCollectionRequest: Boolean = false
    var mIsVisibleToUser: Boolean = false
    var layoutDots: LinearLayout ? = null
    var viewpager: ViewPager ? = null

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
        if (isResumed) {
            Log.e("TAG","===isresumed called====")
            if (mIsVisibleToUser) {
                val handler = Handler()
                handler.postDelayed(
                    {
                        getDashboard(true)
                    }, 500
                )

            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_home, container, false)

        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        initViews(view)

        Log.e("TAG","===onCreateView called====")

        setListeners()
        /*  totalUserPoints?.text =
              "${User.retrieveUser()?.reward_points?.let { Utils.commaConversion(it) }}"*/
        activity?.getString(R.string.track_request)?.let { Utils.setUnderLineText(btnTrackRequest, it) }

        //setSlider(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getDashboard(false)
    }

    private fun callListingApi() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.CAMPAIGNS_LISTING)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getCampaigns())
            ?.execute()
    }

    private fun setSliderNew(list: ArrayList<String>?) {
        Log.e("TAG","====home list size===" + list?.size)
        val myCustomPagerAdapter = CustomPagerAdapter(requireContext(), list!!,1,layoutDots!!)
        viewpager?.setAdapter(myCustomPagerAdapter)
        myCustomPagerAdapter.setClicklistner(this)
        ViewPagerDots.addBottomDots(0,mContext!!, layoutDots!!, list.size)

        viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                ViewPagerDots.addBottomDots(i, mContext!!, layoutDots!!, list.size)
            }
            override fun onPageScrollStateChanged(i: Int) {}
        })

        Handler().postDelayed(object : Runnable {
            override fun run() {
                Handler().postDelayed(this, 3000)

                val currentPage: Int = viewpager!!.getCurrentItem()
                //Log.e("TAG","====currentItemPage===" + (currentPage))
                val size: Int = viewpager!!.getAdapter()!!.getCount()
                //Log.e("TAG","====total size===" + size)
                if (currentPage < (size - 1)) {
                    //Log.e("TAG","====currentPage===" + currentPage  + "===size===" + (size - 1))
                    viewpager!!.setCurrentItem(currentPage + 1, true)
                    ViewPagerDots.addBottomDots(currentPage + 1, mContext!!, layoutDots!!, list.size)
                    //Log.e("TAG","====currentPage===" + (currentPage + 1))
                } else {
                    viewpager!!.setCurrentItem(0, true)
                    ViewPagerDots.addBottomDots(0, mContext!!, layoutDots!!, list.size)
                }
            }
        }, 3000)

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private fun getDashboard(autoLoading: Boolean) {
        if (!NetworkCall.inProgress()) {
            if (autoLoading) {
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.DASHBOARD)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.dashboard())
                    ?.execute()
            } else {
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.DASHBOARD)
                    ?.enque(Network().apis()?.dashboard())
                    ?.execute()
            }
        }
    }

    private fun initViews(view: View?) {

        rvHomeCategories = view?.findViewById(R.id.rvHomeCategories)
        recycle = view?.findViewById(R.id.btn_recycle)
        btnTrackRequest = view?.findViewById(R.id.btn_trackRequest)
        totalUserPoints = view?.findViewById(R.id.totalUserPoints)
        layoutDots = view?.findViewById(R.id.layoutDots)
        viewpager = view?.findViewById(R.id.viewPager)
    }

    private fun setListeners() {
        recycle?.setOnClickListener(this)
        btnTrackRequest?.setOnClickListener(this)
    }

    private fun populateData(
        environmentalStats: EnvironmentalStats?,
        environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>?,
        totalRecycledKg: Double?
    ) {
        populateRecyclerViewData(environmentalStats, environmentalStatsDescriptions)
        totalUserPoints?.text = Utils.commaConversion(totalRecycledKg)
    }

    private fun populateRecyclerViewData(
        environmentalStats: EnvironmentalStats?,
        environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>?
    ) {
        try{
        dataList.clear()
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_tree,
                environmentalStats?.trees_saved!!,
                getString(R.string.unit_tree),
                getString(R.string.tree_heading), EnvironmentalStatsDescriptionsIDs.TREES_SAVED_ID
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_barrel,
                environmentalStats.oil_saved,
                getString(R.string.unit_liters),
                getString(R.string.oil_saved_heading), EnvironmentalStatsDescriptionsIDs.OIL_SAVED_ID
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_electricity,
                environmentalStats.electricity_saved,
                getString(R.string.unit_electricity),
                getString(R.string.electricity_saved_heading), EnvironmentalStatsDescriptionsIDs.ELECTRICITY_SAVED_ID
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_co2_emission,
                environmentalStats.co2_emission_reduced,
                getString(R.string.unit_kilograms),
                getString(R.string.co2_emission_heading), EnvironmentalStatsDescriptionsIDs.CO2_EMISSION_REDUCED_ID
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_landfill_space,
                environmentalStats.landfill_space_saved,
                getString(R.string.unit_ftcube),
                getString(R.string.landfill_heading), EnvironmentalStatsDescriptionsIDs.LAND_FILL_SAVED_ID
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_water_drops,
                environmentalStats.water_saved,
                getString(R.string.unit_liters),
                getString(R.string.water_saved), EnvironmentalStatsDescriptionsIDs.WATER_SAVED_ID)
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.ic_biofuel,
                environmentalStats.biodiesel_produced,
                getString(R.string.unit_liters),
                getString(R.string.biodiesel_produced),
                EnvironmentalStatsDescriptionsIDs.BIODIESEL_PRODUCED_ID
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.ic_compost,
                environmentalStats.compost_created,
                getString(R.string.unit_kilograms),
                getString(R.string.compost_created),
                EnvironmentalStatsDescriptionsIDs.COMPOST_CREATED_ID
            )
        )
            linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)
            rvHomeCategories?.layoutManager = linearLayoutManager
            rvHomeCategories?.adapter = AdapterHomeRecyclerView(dataList, environmentalStatsDescriptions, activity)
        }
        catch (e : RuntimeException)
        {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_recycle -> {

                recycle?.isClickable = false
                checkUserInfo()
               /* if(EasyPermissions.hasPermissions(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE))
                {
                    LogManager.getLogManager().writeLog("Event Home Page : Recycle Button Pressed")
                    recycle?.isClickable = false
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

            R.id.btn_trackRequest -> {
                replaceFragment(
                    childFragmentManager,
                    Constants.Containers.homeFragmentContainer,
                    OrderHistoryFragment.newInstance(),
                    Constants.TAGS.OrderHistoryFragment
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.GET_PLAN -> {
                val getPlans = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), GetPlans::class.java)
                LogManager.getLogManager().writeLog("Event Home Page : Plan Service Result : ${Gson().toJson(getPlans)}")
                handleUserPlansScenario(getPlans)
                val update = Handler()
                update.postDelayed(
                    {
                        recycle?.isClickable = true
                    }, 1000
                )
            }
            RequestCodes.API.DASHBOARD -> {
                try {
                    val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dashboard::class.java)
                    Log.e("TAG","===response2===" + dashboard)
                    val userModel = User.retrieveUser()
                    userModel?.reward_points = dashboard.rewardPoints
                    userModel?.save(userModel, context)
                    settings = dashboard.settings
                    populateData(dashboard.environmentalStats, dashboard.environmentalStatsDescriptions, dashboard.totalRecycledKg)

                    //open when campaign show
                  /* if (!settings.campaigns_visibility.isNullOrEmpty())
                    {
                        Log.e("TAG","===campaign visibility===" + settings.campaigns_visibility.get(0).value)
                        if(settings.campaigns_visibility.get(0).value.equals("1"))
                        {
                            callListingApi()
                            //sliderView?.visibility = View.VISIBLE
                        }
                        else{
                            viewpager?.visibility = View.GONE
                            layoutDots?.visibility = View.GONE
                        }
                    }
                    else{
                       viewpager?.visibility = View.GONE
                       layoutDots?.visibility = View.GONE
                    }*/

                } catch (e: Exception) {
                    Log.e("Home Fragment", e.toString())
                }
            }

            RequestCodes.API.CAMPAIGNS_LISTING -> {
                //val campaignList = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), GetCampaigns::class.java)
                //val campaignList = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), Array<GetCampaigns>::class.java)
                val gson = Gson()
                val listType: Type =
                    object : TypeToken<List<GetCampaigns?>?>() {}.type
                campaignList = gson.fromJson(
                    Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>),
                    listType
                )
                campainListNew?.clear()
                Log.e("TAG","===response3===" + campaignList)
               // val update = Handler()
                //update.postDelayed(
                   // {
                        // campainList = campaignList.get(0)?.getCampaignImages()
                        for (i in campaignList.indices)
                        {
                            campainListNew?.add(campaignList[i]?.getImage().toString())
                        }
                        //campainList = userOrders?.get(0)?.getCampaignImages() as ArrayList<CampaignImage>?

                        if (campainList == null) {
                            campainList = ArrayList()
                        }

                        viewpager?.visibility = View.VISIBLE
                        layoutDots?.visibility = View.VISIBLE
                        //setSlider(campainList)
                        setSliderNew(campainListNew)
                    //}, 100 )
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

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        if(tag == RequestCodes.API.GET_PLAN)
        {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

            LogManager.getLogManager().writeLog("Event Home Page : Plan Service Result : ${gson.toJson(response)}")
        }
        recycle?.isClickable = true
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
                    || User.retrieveUser()?.gender.isNullOrEmpty()
                    || User.retrieveUser()?.birth_date.isNullOrEmpty()
                ) {
                    Notify.hyperlinkAlert(activity, getString(R.string.update_profile_msg), getString(R.string.update_profile_heading), this, 2)
                    recycle?.isClickable = true
                } else {
                    goToNextScreen(getPlans)
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
                    recycle?.isClickable = true
                    return
                } else {
                    //Perform Function
                    goToNextScreen(getPlans)
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

        replaceFragment(childFragmentManager, Constants.Containers.homeFragmentContainer,
            RecycleFragment.newInstance(getPlans, userContainSingleCollectionRequest), Constants.TAGS.RecycleFragment)
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

    override fun itemclickSlide(position: Int) {

        try {
            val bundle = Bundle()
            bundle.putString("campaignId", campaignList.get(position)?.getId().toString())
            replaceFragment(
                childFragmentManager,
                Constants.Containers.homeFragmentContainer,
                CampainDetailFragment.newInstance(bundle),
                Constants.TAGS.CampainDetailFragment
            )
        }
        catch (e: IndexOutOfBoundsException)
        {
            e.printStackTrace()
        }
    }

    fun callRecyclerFragment() {

        recycle?.isClickable = false
        checkUserInfo()
    }
}
