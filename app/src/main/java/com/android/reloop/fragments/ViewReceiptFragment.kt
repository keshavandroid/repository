package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.activities.FullScreenImagesActivity
import com.android.reloop.adapters.CollectionImagesPagerAdapter
import com.android.reloop.customviews.InfiniteCirclePageIndicator
import com.android.reloop.customviews.InfinitePagerAdapter
import com.android.reloop.customviews.InfiniteViewPager
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterOrderTackTracking
import com.reloop.reloop.adapters.AdapterViewReceiptOrderList
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.model.ModelTrackOrder
import com.reloop.reloop.model.ModelViewReceipt
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.network.serializer.shop.OneTimeServices
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.*
import retrofit2.Call
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 */
class ViewReceiptFragment : BaseFragment(), RecyclerViewItemClick, View.OnClickListener,
    OnNetworkResponse, AlertDialogCallback, CollectionImagesPagerAdapter.ItemClickListener {

    companion object {
        var userOrder: UserOrders? = UserOrders()
        var userCollectionRequest: CollectionRequests? = CollectionRequests()
        var parentToChild: ParentToChild? = null
        fun newInstance(
            userOrder: UserOrders?,
            userCollectionRequest: CollectionRequests?, parentToChild: ParentToChild?
        ): ViewReceiptFragment {
            this.userOrder = userOrder
            this.userCollectionRequest = userCollectionRequest
            this.parentToChild = parentToChild
            return ViewReceiptFragment()
        }

        var TAG = "ViewReceiptFragment"
    }

    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var modelViewReceipt: ModelViewReceipt? = null
    var back: Button? = null
    var cancelOrder: Button? = null
    var editOrder: Button? = null

    //    var supportButton: Button? = null
    var orderName: TextView? = null

    var support: TextView?=null

    var _status = "0"
    var _orderId: Int? = 0
    var _orderName: String? = ""
    var _type:String? = ""


    var orderAddress: TextView? = null
    var order_date_schedule: TextView? = null
    var orderDateTime: TextView? = null
    var orderStatus: TextView? = null
    var trackList: ArrayList<ModelTrackOrder> = ArrayList()
    var recyclerViewTrack: RecyclerView? = null
    var statusId: Int? = -1
    var totalPrice: TextView? = null
    var adapter: AdapterViewReceiptOrderList? = null
    var total_price_layout: RelativeLayout? = null
    var divider: LinearLayout? = null

    //originally added
/*
    var hideList = false
*/
    var collectionRequestType = 1
    var productOrderType = 2
    var refunded_price_layout: RelativeLayout? = null
    var refundedPrice: TextView? = null
    var delivery_price_layout: RelativeLayout? = null
    var deliveryPrice: TextView? = null
    var subtotal_price_layout: RelativeLayout? = null
    var subTotalPrice: TextView? = null
    var userComment: TextView? = null

    //Photos Viewpager Driver
    var viewpagerDriver: InfiniteViewPager? = null
    var adapterDriver: InfinitePagerAdapter? = null
    var dotsDriver: InfiniteCirclePageIndicator? = null
    var handlerDriver = Handler()
    var imagesDriver: ArrayList<String>? = ArrayList()
    var llDriverPhotos: LinearLayout? = null


    //Photos Viewpager Supervisor
    var viewpagerSupervisor: InfiniteViewPager? = null
    var adapterSupervisor: InfinitePagerAdapter? = null
    var dotsSupervisor: InfiniteCirclePageIndicator? = null
    var handlerSupervisor = Handler()
    var imagesSupervisor: ArrayList<String>? = ArrayList()
    var llSupervisorPhotos: LinearLayout? = null

    //Photos Viewpager Collection
    var viewpagerCollection: InfiniteViewPager? = null
    var adapterCollection: InfinitePagerAdapter? = null
    var dotsCollection: InfiniteCirclePageIndicator? = null
    var handlerCollection = Handler()
    var imagesCollection: ArrayList<String>? = ArrayList()
    var llCollectionPhotos: LinearLayout? = null

    //FOR VISIBILITY OF IMAGE SLIDER
    private var driver_house_vis: String =""
    private var driver_org_vis: String =""
    private var sup_house_vis: String =""
    private var sup_org_vis: String =""

    private var house_col_vis: String =""
    private var org_col_vis: String =""




    //Edit collection request
    var editCollectionRequest: CollectionRequests? = null
    var userContainSingleCollectionRequest: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_view_receipt, container, false)
        statusId = arguments?.getInt(Constants.DataConstants.trackOrder)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        recyclerView = view?.findViewById(R.id.order_list)
        editOrder = view?.findViewById(R.id.editOrder)
        orderName = view?.findViewById(R.id.order_name)
        support = view?.findViewById(R.id.support)
        orderAddress = view?.findViewById(R.id.order_address)
        order_date_schedule = view?.findViewById(R.id.order_date_schedule)
        orderDateTime = view?.findViewById(R.id.order_date_time)
        orderStatus = view?.findViewById(R.id.order_status)
        back = view?.findViewById(R.id.back)
        totalPrice = view?.findViewById(R.id.totalPrice)
        recyclerViewTrack = view?.findViewById(R.id.order_list_tacking)
        total_price_layout = view?.findViewById(R.id.total_price_layout)
        divider = view?.findViewById(R.id.divider)
        cancelOrder = view?.findViewById(R.id.cancelOrder)
//        supportButton = view?.findViewById(R.id.supportButton)
        refunded_price_layout = view?.findViewById(R.id.refunded_price_layout)
        refundedPrice = view?.findViewById(R.id.refundedPrice)
        delivery_price_layout = view?.findViewById(R.id.delivery_price_layout)
        subtotal_price_layout = view?.findViewById(R.id.subtotal_price_layout)
        deliveryPrice = view?.findViewById(R.id.deliveryPrice)
        subTotalPrice = view?.findViewById(R.id.subTotalPrice)
        userComment = view?.findViewById(R.id.order_comment)

        viewpagerDriver = view?.findViewById(R.id.viewPagerDriver)
        dotsDriver = view?.findViewById(R.id.dotsDriver)
        llDriverPhotos = view?.findViewById(R.id.llDriverPhotos)

        viewpagerSupervisor = view?.findViewById(R.id.viewPagerSupervisor)
        dotsSupervisor = view?.findViewById(R.id.dotsSupervisor)
        llSupervisorPhotos = view?.findViewById(R.id.llSupervisorPhotos)

        viewpagerCollection = view?.findViewById(R.id.viewpagerCollection)
        dotsCollection = view?.findViewById(R.id.dotsCollection)
        llCollectionPhotos = view?.findViewById(R.id.llCollectionPhotos)

    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        cancelOrder?.setOnClickListener(this)
//        supportButton?.setOnClickListener(this)

        support?.setOnClickListener(this)
        editOrder?.setOnClickListener(this)

    }

    private fun setInfiniteViewpagerDriver(list: ArrayList<String>?) {
        Log.e("TAG","==== DriverImages SIZE===" + list?.size)
        viewpagerDriver?.setOffscreenPageLimit(3)
        val myCustomPagerAdapter = CollectionImagesPagerAdapter(requireContext(), list!!,1)

        adapterDriver = InfinitePagerAdapter(
            myCustomPagerAdapter
        )
        adapterDriver!!.setOneItemMode()
        viewpagerDriver?.setAdapter(adapterDriver)

        myCustomPagerAdapter.setClicklistner(this)

        dotsDriver!!.isSnap = true
        dotsDriver!!.setViewPager(viewpagerDriver)

        //hide dots when there is one image
        if(list.size==1){
            dotsDriver!!.visibility = View.GONE
        }else{
            autoSlideHandler()
        }
    }

    private fun setInfiniteViewpagerSupervisor(list: ArrayList<String>?) {
        Log.e("TAG","==== SupervisorImages SIZE===" + list?.size)
        viewpagerSupervisor?.setOffscreenPageLimit(3)
        val myCustomPagerAdapter = CollectionImagesPagerAdapter(requireContext(), list!!,1)

        adapterSupervisor = InfinitePagerAdapter(
            myCustomPagerAdapter
        )
        adapterSupervisor!!.setOneItemMode()
        viewpagerSupervisor?.setAdapter(adapterSupervisor)

        myCustomPagerAdapter.setClicklistner(this)

        dotsSupervisor!!.isSnap = true
        dotsSupervisor!!.setViewPager(viewpagerSupervisor)

        //hide dots when there is one image
        if(list.size==1){
            dotsSupervisor!!.visibility = View.GONE
        }else{
            autoSlideHandlerSup()
        }
    }

    private fun setInfiniteViewpagerCollection(list: ArrayList<String>?) {
        Log.e("TAG","==== CollectionImages SIZE===" + list?.size)
        viewpagerCollection?.setOffscreenPageLimit(3)
        val myCustomPagerAdapter = CollectionImagesPagerAdapter(requireContext(), list!!,1)

        adapterCollection = InfinitePagerAdapter(
            myCustomPagerAdapter
        )
        adapterCollection!!.setOneItemMode()
        viewpagerCollection?.setAdapter(adapterCollection)

        myCustomPagerAdapter.setClicklistner(this)

        dotsCollection!!.isSnap = true
        dotsCollection!!.setViewPager(viewpagerCollection)

        //hide dots when there is one image
        if(list.size==1){
            dotsCollection!!.visibility = View.GONE
        }else{
            autoSlideHandlerCollection()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handlerDriver.removeCallbacksAndMessages(null)
        handlerDriver.removeCallbacks(runnableSlideDriver)

        handlerSupervisor.removeCallbacksAndMessages(null)
        handlerSupervisor.removeCallbacks(runnableSlideSupervisor)

        handlerCollection.removeCallbacksAndMessages(null)
        handlerCollection.removeCallbacks(runnableSlideCollection)
    }

    private fun autoSlideHandler() {
        handlerDriver = Handler()
        handlerDriver.postDelayed(runnableSlideDriver, 3000)
    }

    private fun autoSlideHandlerSup() {
        handlerSupervisor = Handler()
        handlerSupervisor.postDelayed(runnableSlideSupervisor, 3000)
    }

    private fun autoSlideHandlerCollection() {
        handlerCollection = Handler()
        handlerCollection.postDelayed(runnableSlideCollection, 3000)
    }

    var runnableSlideDriver: Runnable = object : Runnable {
        override fun run() {
            if (activity != null) {
                activity!!.runOnUiThread {
                    val currentPage: Int = viewpagerDriver!!.getCurrentItem()
                    val size: Int = viewpagerDriver!!.getAdapter()!!.getCount()
                    if (currentPage < (size - 1)) {
                        viewpagerDriver!!.setCurrentItem(currentPage + 1, true)
                    } else {
                        viewpagerDriver!!.setCurrentItem(0, true)
                    }
                }
            }
            handlerDriver.postDelayed(this, 3000)
        }
    }

    var runnableSlideSupervisor: Runnable = object : Runnable {
        override fun run() {
            if (activity != null) {
                activity!!.runOnUiThread {
                    val currentPage: Int = viewpagerSupervisor!!.getCurrentItem()
                    val size: Int = viewpagerSupervisor!!.getAdapter()!!.getCount()
                    if (currentPage < (size - 1)) {
                        viewpagerSupervisor!!.setCurrentItem(currentPage + 1, true)
                    } else {
                        viewpagerSupervisor!!.setCurrentItem(0, true)
                    }
                }
            }
            handlerSupervisor.postDelayed(this, 3000)
        }
    }

    var runnableSlideCollection: Runnable = object : Runnable {
        override fun run() {
            if (activity != null) {
                activity!!.runOnUiThread {
                    val currentPage: Int = viewpagerCollection!!.getCurrentItem()
                    val size: Int = viewpagerCollection!!.getAdapter()!!.getCount()
                    if (currentPage < (size - 1)) {
                        viewpagerCollection!!.setCurrentItem(currentPage + 1, true)
                    } else {
                        viewpagerCollection!!.setCurrentItem(0, true)
                    }
                }
            }
            handlerCollection.postDelayed(this, 3000)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateData() {
        if (userCollectionRequest == null) { // products

            //For Support click
            _status = userOrder!!.status!!
            _orderId = userOrder!!.id
            _orderName = userOrder!!.order_number
            _type = "order"

//            hideList = false

            setTrackList(userOrder?.status,"order")
            modelViewReceipt = ModelViewReceipt(
                userOrder?.order_number,
                userOrder?.created_at,
                userOrder?.location
                , userOrder?.status, userOrder?.order_number, userOrder?.order_items,
                trackList, userOrder?.id!!
            )
            totalPrice?.text = "${Constants.currencySign} ${Utils.commaConversion(userOrder?.total)}"
            setOrderStatus(modelViewReceipt?.status,"order")
            adapter = AdapterViewReceiptOrderList(userOrder?.order_items, this, null,null)
            orderDateTime?.text =
                "Submission Date " + Utils.getFormattedDisplayDate(modelViewReceipt?.orderDateTime)
            order_date_schedule?.text = ""
            order_date_schedule?.visibility = View.GONE
            if (!userOrder?.refunded_amount.isNullOrEmpty()) {
                refunded_price_layout?.visibility = View.VISIBLE
                refundedPrice?.text =
                    "${Constants.currencySign} ${Utils.commaConversion(userOrder?.refunded_amount?.toDouble())}"
            }

            if (!userOrder?.delivery_fee.isNullOrEmpty() && userOrder?.delivery_fee != "0") {
                delivery_price_layout?.visibility = View.VISIBLE
                deliveryPrice?.text = "${Constants.currencySign} ${Utils.commaConversion(userOrder?.delivery_fee?.toDouble())}"
            } else {
                delivery_price_layout?.visibility = View.VISIBLE
                userOrder?.delivery_fee = "0"
                deliveryPrice?.text = "${Constants.currencySign} ${Utils.commaConversion(userOrder?.delivery_fee?.toDouble())}"
            }



            subtotal_price_layout?.visibility = View.VISIBLE
            subTotalPrice?.text =
                "${Constants.currencySign} ${Utils.commaConversion(userOrder?.total?.minus(userOrder?.delivery_fee?.toDouble()!!))}"

            Log.e(TAG,"===COMMENT===="+ userOrder!!.user_comments)
            if(userOrder!!.user_comments != null && userOrder!!.user_comments!!.length > 0) {
                userComment?.visibility = View.VISIBLE
                userComment?.text = "Comment :"+ userOrder!!.user_comments
            }
            else{
                userComment?.visibility = View.GONE
            }

        } else { // collection and drop-off requests




            /*when (userCollectionRequest?.confirm) {
                0 -> {
                    hideList = false
                }
                1 -> {
                    hideList = true
                }
            }*/





//            setTrackList(userCollectionRequest?.status,"collection")// OLD BEFORE DROP_OFF FLOW


            //LOAD IMAGES
            val tinyDB: TinyDB?
            tinyDB = TinyDB(MainApplication.applicationContext())
            driver_house_vis = tinyDB.getString("driver_house_vis").toString()
            driver_org_vis = tinyDB.getString("driver_org_vis").toString()
            sup_house_vis = tinyDB.getString("sup_house_vis").toString()
            sup_org_vis = tinyDB.getString("sup_org_vis").toString()

            house_col_vis = tinyDB.getString("house_col_vis").toString()
            org_col_vis = tinyDB.getString("org_col_vis").toString()


            if (MainApplication.userType() == Constants.UserType.household) {
                if(driver_house_vis.equals("VISIBLE")) {
                    if (User.retrieveUser()?.driver_photo_visibility.equals("yes",ignoreCase = true)){
                        llDriverPhotos!!.visibility = View.VISIBLE
                        loadDriverImages()
                    }else{
                        llDriverPhotos!!.visibility = View.GONE
                    }
                }else{
                    llDriverPhotos!!.visibility = View.GONE
                }

                if(sup_house_vis.equals("VISIBLE")) {
                    if (User.retrieveUser()?.supervisor_photo_visibility.equals("yes",ignoreCase = true)){
                        llSupervisorPhotos!!.visibility = View.VISIBLE
                        loadSuperVisorImages()
                    }else{
                        llSupervisorPhotos!!.visibility = View.GONE
                    }

                }else{
                    llSupervisorPhotos!!.visibility = View.GONE
                }

                if(house_col_vis.equals("VISIBLE")) {
                    llCollectionPhotos!!.visibility = View.VISIBLE
                    loadCollectionImages()
                }else{
                    llCollectionPhotos!!.visibility = View.GONE
                }

            }else{
                if(driver_org_vis.equals("VISIBLE")) {
                    if (User.retrieveUser()?.driver_photo_visibility.equals("yes",ignoreCase = true)){
                        llDriverPhotos!!.visibility = View.VISIBLE
                        loadDriverImages()
                    }else{
                        llDriverPhotos!!.visibility = View.GONE
                    }
                }else{
                    llDriverPhotos!!.visibility = View.GONE
                }

                if(sup_org_vis.equals("VISIBLE")) {
                    if (User.retrieveUser()?.supervisor_photo_visibility.equals("yes",ignoreCase = true)){
                        llSupervisorPhotos!!.visibility = View.VISIBLE
                        loadSuperVisorImages()
                    }else{
                        llSupervisorPhotos!!.visibility = View.GONE
                    }
                }else{
                    llSupervisorPhotos!!.visibility = View.GONE
                }

                if(org_col_vis.equals("VISIBLE")) {
                    llCollectionPhotos!!.visibility = View.VISIBLE
                    loadCollectionImages()
                }else{
                    llCollectionPhotos!!.visibility = View.GONE
                }
            }






            // NEW AFTER DROP_OFF FLOW
            if(userCollectionRequest!!.type.equals("request")){
                _type = "collection"
                setTrackList(userCollectionRequest?.status,"collection")
                order_date_schedule!!.visibility = View.VISIBLE

                //EDIT button visible for PENDING Collection request only
                if(userCollectionRequest!!.status == OrderHistoryEnum.NOT_ASSIGNED){
                    editOrder!!.visibility = View.VISIBLE
                }



            }else{
                _type = "drop-off"
                order_date_schedule!!.visibility = View.GONE
                setTrackList(userCollectionRequest?.status,"drop-off")

            }

            //For Support click
            _status = "-1"
            _orderId = userCollectionRequest!!.id
            _orderName = userCollectionRequest!!.request_number


            modelViewReceipt = ModelViewReceipt(
                userCollectionRequest?.request_number,
                userCollectionRequest?.created_at,
                userCollectionRequest?.location,
                userCollectionRequest?.status,
                userCollectionRequest?.request_number,
                userOrder?.order_items,
                trackList, userCollectionRequest?.id!!
            )

            totalPrice?.visibility = View.GONE
            total_price_layout?.visibility = View.GONE
            setOrderStatus(modelViewReceipt?.status,"collection")



            /*if (!hideList) {
                userCollectionRequest?.request_collection = ArrayList()
            }*/



            adapter = AdapterViewReceiptOrderList(null, this, userCollectionRequest?.request_collection, modelViewReceipt?.status!!)
            orderDateTime?.text =
                "Submission Date " + Utils.getFormattedDisplayDate(modelViewReceipt?.orderDateTime)
            order_date_schedule?.text =
                "Schedule Date " + Utils.getFormattedDisplayDateCollection(userCollectionRequest?.collection_date)


            Log.e(TAG,"===COMMENT===="+ userCollectionRequest!!.user_comments)
            if(userCollectionRequest!!.user_comments != null && userCollectionRequest!!.user_comments!!.length > 0) {
                userComment?.visibility = View.VISIBLE
                userComment?.text = "Comment : "+ userCollectionRequest!!.user_comments
            }
            else{
                userComment?.visibility = View.GONE
            }

        }

        orderName?.text = modelViewReceipt?.orderTitle
        orderAddress?.text = modelViewReceipt?.address
        linearLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = linearLayoutManager


        recyclerView?.adapter = adapter
    }

    private fun loadDriverImages(){
        if(!userCollectionRequest!!.driver_images.isNullOrEmpty()){
            imagesDriver?.clear()

            if(userCollectionRequest!!.driver_images!!.size > 0) {
                // campainList = campaignList.get(0)?.getCampaignImages()
                for (i in userCollectionRequest!!.driver_images!!.indices) {
                    imagesDriver?.add(userCollectionRequest!!.driver_images!![i].image.toString())
                }
            }

            setInfiniteViewpagerDriver(imagesDriver)
        }else{
            llDriverPhotos!!.visibility = View.GONE
        }
    }

    private fun loadSuperVisorImages(){
        if(!userCollectionRequest!!.supervisor_images.isNullOrEmpty()){
            imagesSupervisor?.clear()

            if(userCollectionRequest!!.supervisor_images!!.size > 0) {
                // campainList = campaignList.get(0)?.getCampaignImages()
                for (i in userCollectionRequest!!.supervisor_images!!.indices) {
                    imagesSupervisor?.add(userCollectionRequest!!.supervisor_images!![i].image.toString())
                }
            }

            setInfiniteViewpagerSupervisor(imagesSupervisor)
        }else{
            llSupervisorPhotos!!.visibility = View.GONE
        }
    }

    private fun loadCollectionImages(){
        if(!userCollectionRequest!!.user_images.isNullOrEmpty()){
            imagesCollection?.clear()

            if(userCollectionRequest!!.user_images!!.size > 0) {
                // campainList = campaignList.get(0)?.getCampaignImages()
                for (i in userCollectionRequest!!.user_images!!.indices) {
                    imagesCollection?.add(userCollectionRequest!!.user_images!![i].image.toString())
                }
            }

            setInfiniteViewpagerCollection(imagesCollection)
        }else{
            llCollectionPhotos!!.visibility = View.GONE
        }
    }

    private fun setOrderStatus(status: String?, s: String) {
        trackHistory()
        when (status) {
            OrderHistoryEnum.ASSIGNED -> {
                orderStatus?.text = activity?.getString(R.string.assigned)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_in_progress)
            }
            OrderHistoryEnum.TRIP_INITIATED -> {
                orderStatus?.text = activity?.getString(R.string.trip_initiated)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_in_progress)
                cancelOrder?.visibility = View.VISIBLE
            }
            OrderHistoryEnum.COMPLETED -> {
                //collected
                if(s.equals("order"))
                {
                    orderStatus?.text = activity?.getString(R.string.delivered)
                    orderStatus?.background = MainApplication.applicationContext()
                        .getDrawable(R.drawable.shape_order_history_completed)
                    cancelOrder?.visibility = View.GONE
                }
                else if(s.equals("collection")) {
                    orderStatus?.text = activity?.getString(R.string.collected)
                    orderStatus?.background = MainApplication.applicationContext()
                        .getDrawable(R.drawable.shape_order_status_in_progress)
                    cancelOrder?.visibility = View.GONE
//                if (userCollectionRequest == null)
//                    supportButton?.visibility = View.VISIBLE
                }

            }
            OrderHistoryEnum.NOT_ASSIGNED -> {
                  // orderStatus?.text = activity?.getString(R.string.pending)
                orderStatus?.text = activity?.getString(R.string.pending)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_in_progress)
            }
            OrderHistoryEnum.CANCELLED -> {
                orderStatus?.text = activity?.getString(R.string.cancelled)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_cancel)
                cancelOrder?.visibility = View.GONE
            }
            OrderHistoryEnum.REFUND_REQUEST -> {
                orderStatus?.text =
                    MainApplication.applicationContext().getString(R.string.request_refund)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_cancel)
                cancelOrder?.visibility = View.GONE
            }
            OrderHistoryEnum.ORDER_REFUNDED -> {
                orderStatus?.text =
                    MainApplication.applicationContext().getString(R.string.order_refund)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_status_cancel)
                cancelOrder?.visibility = View.GONE
            }
            OrderHistoryEnum.ORDER_VERIFIED -> {
                orderStatus?.text = MainApplication.applicationContext().getString(R.string.order_verified)
                orderStatus?.background = MainApplication.applicationContext()
                    .getDrawable(R.drawable.shape_order_history_completed)
                cancelOrder?.visibility = View.GONE
            }
        }
    }

    private fun trackHistory() {
        /*   for (i in modelViewReceipt?.trackList!!.indices) {
               modelViewReceipt!!.trackList?.get(i)!!.check = true
           }*/
        linearLayoutManager = LinearLayoutManager(activity)
        recyclerViewTrack?.layoutManager = linearLayoutManager
        recyclerViewTrack?.adapter = AdapterOrderTackTracking(modelViewReceipt?.trackList!!)
    }

    override fun itemPosition(position: Int) {

        Log.d("TAG","=====id=====" + position.toString())
        Log.d("TAG","=====id pos=====" + userCollectionRequest?.request_collection?.get(position)?.id)


        //open new screen collection details
        val bundle = Bundle()
        bundle.putString("reqId", userCollectionRequest?.request_collection?.get(position)?.id.toString())
        bundle.putInt("unit", userCollectionRequest?.request_collection?.get(position)?.materialCategory?.unit!!)
        bundle.putInt("pos", position)
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.ViewReceiptContainer,
            CollectionBinsFragment.newInstance(bundle),
            Constants.TAGS.CollectionBinsFragment)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                requireActivity().onBackPressed()
            }
            R.id.editOrder -> {
                getEditOrderDetail()
            }
            R.id.support -> {

                if (_status == "-1") {
                    _status = "0"
                }

                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.ViewReceiptContainer,
                    ContactUsFragment.newInstance(
                        "" + _orderId,
                        _orderName!!, _status, _type!!
                    ),
                    Constants.TAGS.ContactUsFragment
                )
            }
            R.id.cancelOrder -> {
                AlertDialogs.alertDialog(
                    activity,
                    this,
                    activity?.getString(R.string.cancel_this_order)
                )
            }
            R.id.supportButton -> {
              /*  BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.ViewReceiptContainer,
                    ContactUsFragment.newInstance(
                        "" + modelViewReceipt?.orderId,
                        orderName?.text.toString()
                    ),
                    Constants.TAGS.ContactUsFragment
                )*/
            }
        }
    }

    private fun getEditOrderDetail(){

        var id: Int? = 0
        if (userCollectionRequest != null) {
            id = userCollectionRequest?.id
        }

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_EDIT_ORDER)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.editOrderDetail(id))
            ?.execute()
    }

    private fun setTrackList(status: String?, type: String) {
        trackList.clear()

        Log.d("TTYPE"," >>> "+_type)
        Log.d("TTYPE"," === "+type)


        var NOT_ASSIGNED = false
        var ASSIGNED = false
        var TRIP_INITIATED = false
        var COMPLETED = false
        var VERIFIED = false
        when (status) {
            OrderHistoryEnum.NOT_ASSIGNED -> {
                NOT_ASSIGNED = true
            }
            OrderHistoryEnum.ASSIGNED -> {
                NOT_ASSIGNED = true
                ASSIGNED = true
            }
            OrderHistoryEnum.TRIP_INITIATED -> {
                NOT_ASSIGNED = true
                ASSIGNED = true
                TRIP_INITIATED = true
            }
            OrderHistoryEnum.COMPLETED -> {
                NOT_ASSIGNED = true
                ASSIGNED = true
                TRIP_INITIATED = true
                COMPLETED = true
            }
            OrderHistoryEnum.ORDER_VERIFIED -> {
                NOT_ASSIGNED = true
                ASSIGNED = true
                TRIP_INITIATED = true
                COMPLETED = true
                VERIFIED = true
            }
        }
        trackList.add(
            ModelTrackOrder(
                NOT_ASSIGNED,
                "Order " + MainApplication.applicationContext().getString(R.string.pending)
            )
            //  //orderStatus?.text = activity?.getString(R.string.pending)
        )
        trackList.add(
            ModelTrackOrder(
                ASSIGNED,
                "Driver " + MainApplication.applicationContext().getString(R.string.assigned)
            )
        )
        trackList.add(
            ModelTrackOrder(
                TRIP_INITIATED,
                "Driver " + MainApplication.applicationContext().getString(R.string.trip_initiated)
            )
        )

        if(_type.equals("collection")) {
            trackList.add(
                ModelTrackOrder(
                    COMPLETED,
                    "Order " + MainApplication.applicationContext().getString(R.string.collected)
                )
                //collected
            )
        }
        else if(_type.equals("order"))
        {
            trackList.add(
                ModelTrackOrder(
                    COMPLETED,
                    "Order " + MainApplication.applicationContext().getString(R.string.delivered)
                )
                //collected
            )
        }

        if(_type.equals("collection")) {
            trackList.add(
                ModelTrackOrder(
                    VERIFIED,
                    "Order " + MainApplication.applicationContext()
                        .getString(R.string.order_verified)
                )
            )
        }

        //TO check the DROP-OFF request only add two STATUS
        // 1.PENDING 2.VERIFIED

        if(_type.equals("drop-off")) {
            trackList.clear()

            trackList.add(
                ModelTrackOrder(
                    NOT_ASSIGNED,
                    "Order " + MainApplication.applicationContext().getString(R.string.pending)
                )
            )

            trackList.add(
                ModelTrackOrder(
                    VERIFIED,
                    "Order " + MainApplication.applicationContext()
                        .getString(R.string.order_verified)
                )
            )
        }

        if(_type.equals("drop-off")) {
            trackList.clear()

            trackList.add(
                ModelTrackOrder(
                    NOT_ASSIGNED,
                    "Order " + MainApplication.applicationContext().getString(R.string.pending)
                )
            )

            trackList.add(
                ModelTrackOrder(
                    VERIFIED,
                    "Order " + MainApplication.applicationContext()
                        .getString(R.string.order_verified)
                )
            )
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.CANCEL_ORDER -> {
                Notify.alerterGreen(activity, baseResponse?.message)
                if (parentToChild != null) {
                    parentToChild?.callChild()
                    setOrderStatus(OrderHistoryEnum.CANCELLED,"cancel")
                    setTrackList(OrderHistoryEnum.CANCELLED, "cancel")
                }
            }
            RequestCodes.API.GET_EDIT_ORDER -> {
                val baseResponse = Utils.getBaseResponse(response)
                editCollectionRequest = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    CollectionRequests::class.java
                )

                Log.d("EDIT_COLL",""+ GsonBuilder().setPrettyPrinting().create().toJson(editCollectionRequest))
                checkUserInfo()
            }
            RequestCodes.API.GET_PLAN -> {
                val baseResponse = Utils.getBaseResponse(response)
                val getPlans = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    GetPlans::class.java)
                LogManager.getLogManager().writeLog("Event Home Page : Plan Service Result : ${Gson().toJson(getPlans)}")
                handleUserPlansScenario(getPlans)

            }
        }
    }

    private fun checkUserInfo() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlan())
            ?.execute()
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

        Log.d("DATA_TEST",""+GsonBuilder().setPrettyPrinting().create().toJson(getPlans))


        if (userContainsTripsMonthly != null ||
            userContainsTripsBulky != null ||
            userContainsTripsSingleCollection != null ||
            userContainsFreeTrips != null
        ) {

            goToNextScreen(getPlans)

        } else {
            //OLD ORIGINAL
            /*Notify.hyperlinkAlert(
                activity,
                "Please Subscribe through the ReLoop Store",
                "Go to Reloop Store",
                this, 1
            )*/

            //NEW
            if(editCollectionRequest!=null){
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.ViewReceiptContainer,
                    RecycleFragment.newInstance(getPlans,userContainSingleCollectionRequest,
                        editCollectionRequest!!
                    ),
                    Constants.TAGS.RecycleFragment
                )
            }

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
            }

            if (userContainsSingleCollection != null && userContainsNextDayRequest == null) {
                getPlans.oneTimeServices?.find {
                    it.category_type == Constants.RecycleCategoryType.NEXT_DAY
                }?.price = getPlans.oneTimeServices?.find {
                    it.category_type == Constants.RecycleCategoryType.NEXT_DAY
                }?.price?.minus(OTS?.price!!)

            }
        }

        userContainSingleCollectionRequest = userContainsSingleCollection != null

        if(editCollectionRequest!=null){
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.ViewReceiptContainer,
                RecycleFragment.newInstance(getPlans,userContainSingleCollectionRequest,
                    editCollectionRequest!!
                ),
                Constants.TAGS.RecycleFragment
            )
        }

    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    override fun callDialog(model: Any?) {
        var id: Int? = 0
        var type: Int? = 0
        if (userCollectionRequest == null) {
            id = userOrder?.id
            type = productOrderType
        } else {
            id = userCollectionRequest?.id
            type = collectionRequestType
        }
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.CANCEL_ORDER)
            ?.autoLoading(requireActivity())
            ?.enque(
                Network().apis()?.cancel_order(id, type)
            )
            ?.execute()
    }

    override fun itemclickSlide(position: Int,imageURLs: ArrayList<String>) {
        Log.d("ItemClicked"," : "+position)
        /*val intent = Intent(requireContext(), FullScreenImagesActivity::class.java).apply {
            putExtra("IMAGE_URL", imageURL)
        }
        startActivity(intent)*/

        val intent = Intent(context, FullScreenImagesActivity::class.java)
        intent.putStringArrayListExtra("IMAGE_URLS", imageURLs)
        intent.putExtra("POSITION", position)
        startActivity(intent)

    }
}
