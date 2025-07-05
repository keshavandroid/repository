package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.adapters.HomeImagesPagerAdapter
import com.android.reloop.constants.EnvironmentalStatsDescriptionsIDs
import com.android.reloop.customviews.InfiniteCirclePageIndicator
import com.android.reloop.customviews.InfinitePagerAdapter
import com.android.reloop.customviews.InfiniteViewPager
import com.android.reloop.fragments.CampainDetailFragment
import com.android.reloop.fragments.DropOffPinFragment
import com.android.reloop.fragments.EditProfileFragment
import com.android.reloop.guestscreens.DropOffPinFragmentGuest
import com.android.reloop.network.serializer.Campain.Campaigns.CampaignImage
import com.android.reloop.network.serializer.Campain.Campaigns.GetCampaigns
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.android.reloop.network.serializer.dashboard.SettingsModel
import com.android.reloop.utils.LogFileSyncTask
import com.android.reloop.utils.LogManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity.Companion.replaceFragment
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.AdapterHomeRecyclerViewNew
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
import com.reloop.reloop.tinydb.TinyDB
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
class HomeFragmentGuest : BaseFragment(), View.OnClickListener, OnNetworkResponse, AlertDialogCallback ,HomeImagesPagerAdapter.ItemClickListener{
    companion object {
        private var isOpenDropOff: Boolean = false
        private var dropOffId: String = ""

        fun newInstance(openDropOff: Boolean,dropOffIdStr: String): HomeFragmentGuest {
            isOpenDropOff = openDropOff
            dropOffId = dropOffIdStr
            return HomeFragmentGuest()
        }
        var settings = SettingsModel()
    }
    private var mContext: Context? = null
    private var campaignList = ArrayList<GetCampaigns?>()
    var campainList: ArrayList<CampaignImage>? = ArrayList()
    var campainListNew: ArrayList<String>? = ArrayList()
    var preLoadImages: ArrayList<String>? = ArrayList()

    private lateinit var linearLayoutManager: LinearLayoutManager
    var rvHomeCategories: RecyclerView? = null
    var dataList: ArrayList<ModelHomeCategories> = ArrayList()
    var recycle: Button? = null
    var btn_start_dropoff: Button?=null
    var btnTrackRequest: TextView? = null
    var totalUserPoints: TextView? = null
    var txtRewardsPoint: TextView?=null
    var tvPersonName: TextView? = null
    var userContainSingleCollectionRequest: Boolean = false
    var mIsVisibleToUser: Boolean = false

    var cardKGRecycled: CardView ?= null
    var cardPointsEarned: CardView ?= null

    //var layoutDots: LinearLayout ? = null
//    var viewpager: ViewPager ? = null

    var viewpager: InfiniteViewPager? = null
    var rlSliderImages: RelativeLayout? =null
    var mPagerAdapter: InfinitePagerAdapter? = null
    var layoutDots: InfiniteCirclePageIndicator? = null
    var imageHandler = Handler()

    private lateinit var dialog: Dialog


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
        if (isResumed) {
            Log.e("TAG","===isresumed called====")
            if (mIsVisibleToUser) {
                val handler = Handler()
                handler.postDelayed(
                    {
                        //getDashboard(true)
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
//        val view: View? = inflater.inflate(R.layout.fragment_home, container, false) //OLD design

        val view: View? = inflater.inflate(R.layout.fragment_shop_main_screen_guest, container, false)//NEW design

        if (Build.VERSION.SDK_INT > 9) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        initViews(view)
        if (!User.retrieveUser()?.first_name.isNullOrEmpty()){
            tvPersonName!!.text = "Hey, "+User.retrieveUser()?.first_name.toString() + "!"
        }

//        showProgressDialog()

        populateRecyclerViewData(null,null)

        setListeners()
        /*  totalUserPoints?.text =
              "${User.retrieveUser()?.reward_points?.let { Utils.commaConversion(it) }}"*/
        activity?.getString(R.string.track_request)?.let { Utils.setUnderLineText(btnTrackRequest, it) }

        //setSlider(view)

        return view
    }

    private fun showProgressDialog() {
        dialog = Dialog(requireActivity())
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.e("TAG","===onViewCreated called====")

//        getDashboard(false)



    }

    private fun callListingApi() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.CAMPAIGNS_LISTING)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getCampaigns())
            ?.execute()
    }


    /*private val sliderRunnable =
        Runnable { viewpager?.setCurrentItem(viewpager?.getCurrentItem()!! + 1) }*/

    /*private fun setSliderNew(list: ArrayList<String>?) {
        Log.e("TAG","====home list size===" + list?.size)
        val myCustomPagerAdapter = HomeImagesPagerAdapter(requireContext(), list!!,1,layoutDots!!)
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

    }*/

    private fun setInfiniteViewpager(list: ArrayList<String>?) {
        Log.e("TAG","====home list size===" + list?.size)
        viewpager?.setOffscreenPageLimit(3)
        val myCustomPagerAdapter = HomeImagesPagerAdapter(requireContext(), list!!,1)

        mPagerAdapter = InfinitePagerAdapter(
            myCustomPagerAdapter
        )
        mPagerAdapter!!.setOneItemMode()
        viewpager?.setAdapter(mPagerAdapter)

        myCustomPagerAdapter.setClicklistner(this)


        layoutDots!!.isSnap = true
        layoutDots!!.setViewPager(viewpager)

        //hide dots when there is one image
        if(list.size==1){
            layoutDots!!.visibility = View.GONE
        }else{

            autoSlideHandler()



            /*imageHandler.postDelayed(object : Runnable {
                override fun run() {
                    imageHandler.postDelayed(this, 3000)


                    val currentPage: Int = viewpager!!.getCurrentItem()
                    val size: Int = viewpager!!.getAdapter()!!.getCount()
                    if (currentPage < (size - 1)) {
                        viewpager!!.setCurrentItem(currentPage + 1, true)
                    } else {
                        viewpager!!.setCurrentItem(0, true)
                    }
                }
            }, 3000)*/


        }


    }

    private fun autoSlideHandler() {
        imageHandler = Handler()
        imageHandler.postDelayed(runnableSlide, 3000)
    }

    var runnableSlide: Runnable = object : Runnable {
        override fun run() {
            if (activity != null) {
                activity!!.runOnUiThread {
                    val currentPage: Int = viewpager!!.getCurrentItem()
                    val size: Int = viewpager!!.getAdapter()!!.getCount()
                    if (currentPage < (size - 1)) {
                        viewpager!!.setCurrentItem(currentPage + 1, true)
                    } else {
                        viewpager!!.setCurrentItem(0, true)
                    }
                }
            }
            imageHandler.postDelayed(this, 3000)
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onPause() {
        super.onPause()
        if(runnableSlide!=null){
            imageHandler.removeCallbacksAndMessages(null)
            imageHandler.removeCallbacks(runnableSlide!!)
        }

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

        cardKGRecycled = view?.findViewById(R.id.cardKGRecycled)
        cardPointsEarned = view?.findViewById(R.id.cardPointsEarned)

        rvHomeCategories = view?.findViewById(R.id.rvHomeCategories)
        recycle = view?.findViewById(R.id.btn_recycle)
        btn_start_dropoff = view?.findViewById(R.id.btn_start_dropoff)
        btnTrackRequest = view?.findViewById(R.id.btn_trackRequest)
        totalUserPoints = view?.findViewById(R.id.totalUserPoints)
        txtRewardsPoint = view?.findViewById(R.id.txtRewardsPoint)
        layoutDots = view?.findViewById(R.id.layoutDots)
        viewpager = view?.findViewById(R.id.viewPager)
        tvPersonName = view?.findViewById(R.id.tvPersonName)
        rlSliderImages = view?.findViewById(R.id.rlSliderImages)
    }

    private fun setListeners() {
        recycle?.setOnClickListener(this)
        btnTrackRequest?.setOnClickListener(this)
        btn_start_dropoff?.setOnClickListener(this)

        cardKGRecycled?.setOnClickListener(this)
        cardPointsEarned?.setOnClickListener(this)

    }

    private fun populateData(
        environmentalStats: EnvironmentalStats?,
        environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>?,
        totalRecycledKg: Double?,
        rewardsPoint: Int
    ) {
        populateRecyclerViewData(environmentalStats, environmentalStatsDescriptions)
        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        tinyDB.putString("KGS_RECYCLED", ""+Utils.commaConversion(totalRecycledKg))

        totalUserPoints?.text = Utils.commaConversion(totalRecycledKg)

        txtRewardsPoint?.text = Utils.commaConversion(rewardsPoint)
    }

    private fun populateRecyclerViewData(
        environmentalStats: EnvironmentalStats?,
        environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>?
    ) {
        try{
        dataList.clear()
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_tree_new,
                    0.0,
                    "",
                    "Trees", EnvironmentalStatsDescriptionsIDs.TREES_SAVED_ID,
                    ""
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_barrel_new,
                    0.0,
                    "L",
                    "of Oil", EnvironmentalStatsDescriptionsIDs.OIL_SAVED_ID,
                    ""
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_electricity_new,
                    0.0,
                    "kwh",
                    "of Electricity", EnvironmentalStatsDescriptionsIDs.ELECTRICITY_SAVED_ID,
                    ""
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Reducing",
                    R.drawable.icon_co2_emission_new,
                    0.0,
                    "kgs",
                    getString(R.string.co2_heading), EnvironmentalStatsDescriptionsIDs.CO2_EMISSION_REDUCED_ID,
                    ""
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_landfill_space_new,
                    0.0,
                    getString(R.string.unit_ftcube),
                    "of Landfill Space", EnvironmentalStatsDescriptionsIDs.LAND_FILL_SAVED_ID,
                    ""
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_water_drops_new,
                    0.0,
                    "L",
                    "of Water", EnvironmentalStatsDescriptionsIDs.WATER_SAVED_ID,
                    ""
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Producing",
                    R.drawable.icon_biofuel_new,
                    0.0,
                    "L",
                    "of Biodiesel", EnvironmentalStatsDescriptionsIDs.BIODIESEL_PRODUCED_ID,
                    ""
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Creating",
                    R.drawable.ic_compost_new,
                    0.0,
                    "kgs",
                    "of Compost", EnvironmentalStatsDescriptionsIDs.COMPOST_CREATED_ID,
                    ""
                )
            )
            linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)

            //NEW LINEAR HORIZONTAL
            /*linearLayoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )*/

            rvHomeCategories?.layoutManager = linearLayoutManager
            rvHomeCategories?.adapter = AdapterHomeRecyclerViewNew(dataList, environmentalStatsDescriptions, activity)
        }
        catch (e : RuntimeException)
        {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cardKGRecycled -> {
                Utils.clearAllFragmentStack(activity?.supportFragmentManager)
                HomeActivity.bottomNav.selectedItemId = R.id.navigation_reports
            }

            R.id.cardPointsEarned -> {
                Utils.clearAllFragmentStack(activity?.supportFragmentManager)
                HomeActivity.bottomNav.selectedItemId = R.id.navigation_rewards
            }


            R.id.btn_start_dropoff -> {
                replaceFragment(childFragmentManager,
                    Constants.Containers.homeFragmentContainer,
                    DropOffPinFragmentGuest.newInstance(""),
                    Constants.TAGS.DropOffPinFragment)
            }
            R.id.btn_recycle -> {

//                recycle?.isClickable = false
//                checkUserInfo()

                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))

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

                AlertDialogs.alertDialogGuestAccount(activity, this, activity?.getString(R.string.guest_acc_text))


                /*replaceFragment(
                    childFragmentManager,
                    Constants.Containers.homeFragmentContainer,
                    OrderHistoryFragment.newInstance(),
                    Constants.TAGS.OrderHistoryFragment
                )*/
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

                    dismissProgressDialog()
                    val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dashboard::class.java)
                    Log.e("TAG","===response2===" + dashboard)
                    val userModel = User.retrieveUser()
                    userModel?.reward_points = dashboard.rewardPoints
                    userModel?.save(userModel, context,false)
                    settings = dashboard.settings
                    populateData(dashboard.environmentalStats, dashboard.environmentalStatsDescriptions, dashboard.totalRecycledKg,
                        dashboard.rewardPoints!!
                    )

                    //open when campaign show
                   if (!settings.campaigns_visibility.isNullOrEmpty())
                    {
                        Log.e("TAG","===campaign visibility===" + settings.campaigns_visibility.get(0).value)
                        if(settings.campaigns_visibility.get(0).value.equals("1"))
                        {

                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("campaign_visibility", "VISIBLE")

                            callListingApi()
                            //sliderView?.visibility = View.VISIBLE
                        }
                        else{

                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("campaign_visibility", "IN_VISIBLE")

                            viewpager?.visibility = View.GONE
                            layoutDots?.visibility = View.GONE
                            rlSliderImages?.visibility = View.GONE
                        }
                    }
                    else{
                       viewpager?.visibility = View.GONE
                       layoutDots?.visibility = View.GONE
                       rlSliderImages?.visibility = View.GONE
                    }



                    //Google pay visibility
                    if (!settings.googlepay_visibility.isNullOrEmpty()) {
                        Log.e("TAG","===googlepay visibility===" + settings.googlepay_visibility.get(0).value)
                        if(settings.googlepay_visibility.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("googlepay_visibility", "VISIBLE")

                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("googlepay_visibility", "IN_VISIBLE")
                        }
                    } else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("googlepay_visibility", "IN_VISIBLE")
                    }

                    //Recycling family names visibility
                    if (!settings.show_recycling_family_names.isNullOrEmpty()) {
                        if(settings.show_recycling_family_names.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("recycling_visibility", "VISIBLE")

                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("recycling_visibility", "IN_VISIBLE")
                        }
                    } else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("recycling_visibility", "IN_VISIBLE")
                    }


                    //PHOTO taking for collection request
                    if (!settings.photo_taking_for_collection_request.isNullOrEmpty()) {
                        if(settings.photo_taking_for_collection_request.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("photo_take_visibility", "VISIBLE")

                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("photo_take_visibility", "IN_VISIBLE")
                        }
                    } else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("photo_take_visibility", "IN_VISIBLE")
                    }


                    //coffee capsule and soap bars visibility
                    if (!settings.coffee_capsules_household_visibility.isNullOrEmpty() &&
                        !settings.soap_bars_household_visibility.isNullOrEmpty()) {

                        if(settings.coffee_capsules_household_visibility.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("coffee_visibility", "VISIBLE")
                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("coffee_visibility", "IN_VISIBLE")
                        }

                        if(settings.soap_bars_household_visibility.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("soap_visibility", "VISIBLE")
                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("soap_visibility", "IN_VISIBLE")
                        }

                    } else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("coffee_visibility", "IN_VISIBLE")
                        tinyDB.putString("soap_visibility", "IN_VISIBLE")
                    }

                    //report download button household/organization
                    if (!settings.reports_download_button_for_household.isNullOrEmpty() &&
                        !settings.reports_download_button_for_organization.isNullOrEmpty()) {

                        if(settings.reports_download_button_for_household.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("reports_button_household", "VISIBLE")
                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("reports_button_household", "IN_VISIBLE")
                        }

                        if(settings.reports_download_button_for_organization.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("reports_button_organization", "VISIBLE")
                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("reports_button_organization", "IN_VISIBLE")
                        }

                    } else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("reports_button_household", "IN_VISIBLE")
                        tinyDB.putString("reports_button_organization", "IN_VISIBLE")
                    }


                    if (!settings.dropoff_visibility.isNullOrEmpty()) {
                        Log.e("TAG","===campaign visibility===" + settings.dropoff_visibility.get(0).value)
                        if(settings.dropoff_visibility.get(0).value.equals("1")) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("dropoff_visibility", "VISIBLE")
                            setVisibilityOfBottomButtons()

                            //OPEN drop off screen if user clicked on the deeplink android here
                            if (!NetworkCall.inProgress()) {
                                if(isOpenDropOff){
                                    isOpenDropOff = false
                                    replaceFragment(childFragmentManager,
                                        Constants.Containers.homeFragmentContainer,
                                        DropOffPinFragment.newInstance(dropOffId),
                                        Constants.TAGS.DropOffPinFragment)
                                }
                            }

                        } else{
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("dropoff_visibility", "IN_VISIBLE")
                            setVisibilityOfBottomButtons()

                        }
                    } else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("dropoff_visibility", "IN_VISIBLE")
                        setVisibilityOfBottomButtons()

                    }









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



                if(campaignList.size>0){
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
                    rlSliderImages?.visibility = View.VISIBLE



//                setSliderNew(campainListNew) // old slider
                    setInfiniteViewpager(campainListNew) // NEW slider


                    preLoadImages?.clear()
                    try {
                        for (i in campaignList.indices) {

                            //For Campaign Detail images
                            for (j in 0 until campaignList.get(i)!!.getCampaignImages()!!.size){
                                preLoadImages?.add(campaignList.get(i)!!.getCampaignImages()!!.get(j).getImage().toString())
                            }

                            //For News Image
                            if(!campaignList.get(i)!!.getNews().isNullOrEmpty()){
                                for (k in 0 until campaignList.get(i)!!.getNews()!!.size){
                                    //Log.d("CAMPAIGNS_111"," news " + campaignList.get(i)!!.getNews()!!.get(k)!!.getImage())
                                    preLoadImages?.add(campaignList.get(i)!!.getNews()!!.get(k)!!.getImage().toString())
                                    if(!campaignList.get(i)!!.getNews()?.get(k)?.getNewsImages().isNullOrEmpty()){
                                        for (l in 0 until campaignList.get(i)!!.getNews()?.get(k)!!.getNewsImages()!!.size){
                                            //Log.d("CAMPAIGNS_111"," news_images " + campaignList.get(i)!!.getNews()!!.get(k)!!.getNewsImages()!!.get(l)!!.getImage())
                                            preLoadImages?.add(campaignList.get(i)!!.getNews()!!.get(k)!!.getNewsImages()!!.get(l)!!.getImage().toString())
                                        }
                                    }
                                }
                            }
                        }

                        //call this if listing api is call and user is coming from deeplinking
                        if(isOpenDropOff){
                            isOpenDropOff = false

                            if (MainApplication.userType() == Constants.UserType.household) {
                                if (User.retrieveUser()?.first_name.isNullOrEmpty()
                                    || User.retrieveUser()?.last_name.isNullOrEmpty()
                                    || User.retrieveUser()?.addresses.isNullOrEmpty()
                                    || User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()
                                    || User.retrieveUser()?.addresses?.get(0)?.building_name.isNullOrEmpty()
                                    || User.retrieveUser()?.phone_number.isNullOrEmpty()
//                                    || User.retrieveUser()?.gender.isNullOrEmpty()
//                        || User.retrieveUser()?.birth_date.isNullOrEmpty() //Earlier mandatory, now optional
                                ) {
                                    Notify.hyperlinkAlert(
                                        activity,
                                        getString(R.string.update_profile_msg),
                                        getString(R.string.update_profile_heading),
                                        this, 2
                                    )
                                    /*replaceFragment(
                                        childFragmentManager,
                                        Constants.Containers.shopFragmentContainer,
                                        EditProfileFragment.newInstance(),
                                        Constants.TAGS.EditProfileFragment
                                    )*/
                                } else {
                                    replaceFragment(childFragmentManager,
                                        Constants.Containers.homeFragmentContainer,
                                        DropOffPinFragment.newInstance(dropOffId),
                                        Constants.TAGS.DropOffPinFragment)
                                }
                            }else{
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
                                }else{
                                    replaceFragment(childFragmentManager,
                                        Constants.Containers.homeFragmentContainer,
                                        DropOffPinFragment.newInstance(dropOffId),
                                        Constants.TAGS.DropOffPinFragment)
                                }
                            }



                        }





                    }catch (e: Exception){
                        e.printStackTrace()
                        Log.d("LoginActivity","Image downloading failed in Home screen")
                    }finally {
                        preloadAllImages(preLoadImages)
                    }
                }


            }
        }
    }

    private fun preloadAllImages(imagesUrls: ArrayList<String>?) {
        for (url in imagesUrls!!) {
            preloadImage(url)
        }
    }

    private fun preloadImage(url: String) {
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Handle exceptions differently if you want
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }
            }).preload()

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
//                    || User.retrieveUser()?.gender.isNullOrEmpty()
//                        || User.retrieveUser()?.birth_date.isNullOrEmpty() //Earlier mandatory, now optional
                ) {
                    Notify.hyperlinkAlert(activity, getString(R.string.update_profile_msg), getString(R.string.update_profile_heading), this, 2)
                    recycle?.isClickable = true
                } else {

                    //goToNextScreen(getPlans)
                    val newlist = getPlans.userPlans!!.reversed()
                    Log.e(SubscriptionFragment.TAG,"===Event Home Page :newlist : ${Gson().toJson(newlist)}")
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

                   /* Log.e("TAG","=====subscription type ====" + getPlans.userPlans!!.get(0).subscription_type)
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
                    Notify.hyperlinkAlert( //old was hyperlinkAlert
                        activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2
                    )
                    recycle?.isClickable = true
                    return
                } else {
                    //Perform Function
                   // goToNextScreen(getPlans)
                    val newlist = getPlans.userPlans!!.reversed()
                    Log.e(SubscriptionFragment.TAG,"===Event Home Page :newlist : ${Gson().toJson(newlist)}")
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

                  /*  Log.e("TAG","=====subscription type ====" + getPlans.userPlans!!.get(0).subscription_type)
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

        }
        else {
            Notify.hyperlinkAlert(
                activity,
                "Please Subscribe through the ReLoop Store",
                "Go to Reloop Store",
                this, 1
            )
        }
    }

    private fun showPurchaseInfoDialog(plans: GetPlans) {
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_rewards_message)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm = dialog.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton

        confirm.visibility = View.VISIBLE
        confirm.setText("Go to Reloop Store")

        message.text = "You are already subscribed for a monthly service. If you require extra trips, you can request a one-time service to avoid paying for another monthly subscription"
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            dialog.dismiss()

            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
            HomeActivity.bottomNav.selectedItemId = R.id.navigation_shop
        }

        dialog.show()
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
            RecycleFragment.newInstance(getPlans, userContainSingleCollectionRequest,null), Constants.TAGS.RecycleFragment)
    }

    override fun callDialog(model: Any?) {
        if (model as Int == 1) {
            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
            HomeActivity.bottomNav.selectedItemId = R.id.navigation_shop
        } else if (model == 2) {
            HomeActivity.settingClicked = true
            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
            HomeActivity.bottomNav.selectedItemId = R.id.navigation_settings
        }else{

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

    private fun setVisibilityOfBottomButtons(){
        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())

        if (MainApplication.userType() == Constants.UserType.household) {
            btn_start_dropoff!!.visibility = View.VISIBLE

            val dropOffVisibility = tinyDB.getString("dropoff_visibility").toString()
            if(dropOffVisibility.equals("VISIBLE")){
                btn_start_dropoff!!.visibility = View.VISIBLE
            }else{
                btn_start_dropoff!!.visibility = View.GONE
            }

        }else{
            btn_start_dropoff!!.visibility = View.GONE
        }


    }

}
