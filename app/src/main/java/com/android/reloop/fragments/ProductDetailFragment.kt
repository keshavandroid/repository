package com.android.reloop.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.android.reloop.adapters.CustomPagerAdapter
import com.android.reloop.adapters.ProductRecyclableAdapter
import com.android.reloop.adapters.SliderAdapter
import com.android.reloop.customviews.InfiniteCirclePageIndicator
import com.android.reloop.customviews.InfinitePagerAdapter
import com.android.reloop.customviews.InfiniteViewPager
import com.android.reloop.model.ModelProductRecyclable
import com.android.reloop.model.ModelSliderData
import com.android.reloop.network.serializer.Campain.ProductDetail
import com.android.reloop.utils.BarcodeTrackerFactory
import com.android.reloop.utils.ViewPagerDots
import com.bumptech.glide.Glide
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.reloop.reloop.BuildConfig
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.fragments.RecycleFragment
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.shop.OneTimeServices
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.smarteist.autoimageslider.SliderView
import kotlinx.android.synthetic.main.fragment_product_detail.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException

class ProductDetailFragment : Fragment() ,CustomPagerAdapter.ItemClickListener,ProductRecyclableAdapter.ItemClickListener,View.OnClickListener,
    OnNetworkResponse ,BarcodeCallback, AlertDialogCallback {

    val TAG = "ProductDetailFragment"
    var mIsVisibleToUser: Boolean = false
    var rvRecyclable: RecyclerView? = null
    var back: Button? = null
    var btnrecycleProduct: Button? = null
    var rlBarcode : RelativeLayout?= null
    var productImgList: ArrayList<String>? = ArrayList()
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    var userContainSingleCollectionRequest: Boolean = false
    private var scannedValue = ""
    private var code: String = ""

//    var layoutDots: LinearLayout? = null
//    var viewpager: ViewPager? = null

    var viewpager: InfiniteViewPager? = null
    var mPagerAdapter: InfinitePagerAdapter? = null
    var layoutDots: InfiniteCirclePageIndicator? = null
    val imageHandler = Handler()

    private var mContext: Context? = null

    // Urls of our images.
    var url1 = "https://www.learningcontainer.com/wp-content/uploads/2020/07/Sample-JPEG-file-download-for-Testing.png"
    var url2 = "https://qphs.fs.quoracdn.net/main-qimg-8e203d34a6a56345f86f1a92570557ba.webp"
    var url3 = "https://bizzbucket.co/wp-content/uploads/2020/08/Life-in-The-Metro-Blog-Title-22.png"

    companion object {
        private var barcode: String? = null

        fun newInstance(bundle: Bundle): ProductDetailFragment {
            if (bundle != null) {
                barcode = bundle.getString("barcode")
            }
            Log.e("TAG", "=====barcode ===" + barcode)
            return ProductDetailFragment()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_product_detail, container, false)
        initViews(view)
        setListeners()


        setSlider(view)

        //callFetchBarcodeDetailsApi(barcode.toString())
        callFetchBarcodeDetailsApi("123456")

        initRecycleView()

        return view
    }

    private fun setSlider(view: View?) {

        val sliderDataArrayList: ArrayList<ModelSliderData> = ArrayList()

        val sliderView: SliderView = view?.findViewById(R.id.slider)!!
        sliderDataArrayList.add(ModelSliderData(url1))
        sliderDataArrayList.add(ModelSliderData(url2))
        sliderDataArrayList.add(ModelSliderData(url3))

      /* val adapter = SliderAdapter(requireContext(), sliderDataArrayList,2)
         sliderView.autoCycleDirection = SliderView.LAYOUT_DIRECTION_LTR
         sliderView.setSliderAdapter(adapter)
         adapter.setClicklistner(this)
         sliderView.scrollTimeInSec = 3
         sliderView.isAutoCycle = true
         sliderView.startAutoCycle()*/
    }

    private fun initRecycleView() {

        val staggeredGridLayoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
        rvRecyclable?.layoutManager = staggeredGridLayoutManager
        val adapter = ProductRecyclableAdapter(requireContext(), arrayListOf())
        rvRecyclable?.setHasFixedSize(true)
        rvRecyclable?.adapter = adapter
        adapter.setClicklistner(this)
        adapter.notifyDataSetChanged()
    }

    private fun setListeners() {
        rlBarcode?.setOnClickListener(this)
        back?.setOnClickListener(this)
        btnrecycleProduct?.setOnClickListener(this)
    }

    private fun initViews(view: View?) {
        rvRecyclable = view?.findViewById(R.id.rvRecyclable)
        back = view?.findViewById(R.id.back)
        btnrecycleProduct = view?.findViewById(R.id.btn_recycle_Product)
        rlBarcode = view?.findViewById(R.id.rlBarcodep)
        layoutDots = view?.findViewById(R.id.layoutDots)
        viewpager = view?.findViewById(R.id.viewPager)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
        if (isResumed) {
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

    private fun callFetchBarcodeDetailsApi(code: String) {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.PRODUCT_BARCODES)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.fetchScannedBarCodeDetails(code))
            ?.execute()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.rlBarcodep -> {
               /* BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.containerCampainDetail,
                    ProductDetailFragment.newInstance(),
                    Constants.TAGS.ProductDetailFragment
                )*/

                if (ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForCameraPermission()
                } else {
                    rlbarcodep?.visibility = View.VISIBLE
                    setupControls()
                }
            }
            R.id.back -> {
                activity?.onBackPressed()
            }

            R.id.btn_recycle_Product -> {
                checkUserInfo()
            }
        }
    }

    fun checkUserInfo() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PLAN)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getPlan())
            ?.execute()
    }

    private fun setupControls() {

        if (BuildConfig.DEBUG) {
            showBarcodeScanner()
            return
        }
        barcodeDetector = BarcodeDetector.Builder(context).build()
        var barcodeFactory = BarcodeTrackerFactory
        barcodeDetector.setProcessor(MultiProcessor.Builder<Barcode>(barcodeFactory).build())

        cameraSource = CameraSource.Builder(context, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .build()

        /* barcodeDetector = BarcodeDetector.Builder(requireContext()).setBarcodeFormats(Barcode.ALL_FORMATS).build()

         cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
             .setRequestedPreviewSize(1920, 1080)
             .setAutoFocusEnabled(true) //you should add this feature
             .build()*/

        cameraSurfaceView?.getHolder()?.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(requireContext(), "Scanner has been closed", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue

                    //Don't forget to add this line printing value or finishing activity must run on main thread
                    requireActivity().runOnUiThread {
                        cameraSource.stop()
                        Log.e("TAG", "value- $scannedValue")
                        /*  BaseActivity.replaceFragment(
                              childFragmentManager,
                              Constants.Containers.containerCampainDetail,
                              ProductDetailFragment.newInstance(),
                              Constants.TAGS.ProductDetailFragment
                          )*/
                    }
                } else {
                    Log.e("TAG", "value- else")
                }
            }
        })
    }

    private fun showBarcodeScanner() {
        barcodeScanner!!.decodeContinuous(this)
        val settings: CameraSettings = barcodeScanner!!.getBarcodeView().getCameraSettings()

        if (barcodeScanner!!.getBarcodeView().isPreviewActive()) {
            barcodeScanner!!.pause()
        }else
            barcodeScanner!!.resume()

        //swap the id of the camera to be used
        settings.requestedCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        barcodeScanner!!.getBarcodeView().setCameraSettings(settings)
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun itemclick(bean: ModelProductRecyclable, position: Int, type: String) {

    }

    override fun itemclickSlide(position: Int) {

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        Log.e("TAG", "===response===" + baseResponse)
        when (tag) {
            RequestCodes.API.PRODUCT_BARCODES -> {

                val message = baseResponse?.message
                Log.e("TAG", "===PRODUCT_BARCODES===" + message)
                if (baseResponse?.data != null) {
                    val aboutApp = Gson().fromJson(
                            Utils.jsonConverterObject(baseResponse.data as? LinkedTreeMap<*, *>),
                            ProductDetail::class.java)
                    if (aboutApp != null) {
                        Log.e("TAG", "====product name===" + aboutApp.getProductName())

                        tv_ProductName.setText(aboutApp.getProductName())
                        tv_ProductDesc.setText(aboutApp.getDescription())

                        Glide.with(requireContext())
                            .load(aboutApp.getBrandLogo())
                            .fitCenter()
                            .into(iv1)

                        productImgList?.clear()
                        for (i in aboutApp.getProductImages()!!.indices) {
                            productImgList?.add(aboutApp.getProductImages()!![i]?.image
                                .toString())
                        }
                        //setSliderNew(productImgList) //old slider
                        setInfiniteViewpager(productImgList)//new slider

                        tv.setText(aboutApp.getRecyclablePercentage().toString() + "%")
                        circularProgressbar.setProgress(aboutApp.getRecyclablePercentage()!!,true)
                    }
                }
            }

            RequestCodes.API.GET_PLAN -> {
                val getPlans = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    GetPlans::class.java
                )
                Log.e("TAG","===response1===" + getPlans)
                handleUserPlansScenario(getPlans)
                val update = Handler()
                update.postDelayed(
                    {
                        //recycle?.isClickable = true
                    }, 1000
                )
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
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
                    Notify.hyperlinkAlert(
                        activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2
                    )
                    //recycle?.isClickable = true
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
                    //recycle?.isClickable = true
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
//                getPlans.oneTimeServices?.get(0)?.price = getPlans.oneTimeServices?.get(0)?.price?.minus(getPlans.oneTimeServices?.get(2)?.price!!)
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

        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.productDetailFragmentContainer,
            RecycleFragment.newInstance(getPlans, userContainSingleCollectionRequest,null),
            Constants.TAGS.RecycleFragment
        )
    }

    /*private fun setSliderNew(list: ArrayList<String>?) {

        val myCustomPagerAdapter = CustomPagerAdapter(requireContext(), list!!,2,layoutDots!!)
        viewpager?.setAdapter(myCustomPagerAdapter)
        myCustomPagerAdapter.setClicklistner(this)
        ViewPagerDots.addBottomDots(0, mContext!!, layoutDots!!, list.size)

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
                val size: Int = viewpager!!.getAdapter()!!.getCount()
                if (currentPage < size - 1) {
                    viewpager!!.setCurrentItem(currentPage + 1, true)
                    ViewPagerDots.addBottomDots(currentPage + 1, mContext!!, layoutDots!!, list.size)
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
        val myCustomPagerAdapter = CustomPagerAdapter(requireContext(), list!!,2)

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
            imageHandler.postDelayed(object : Runnable {
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
            }, 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        imageHandler.removeCallbacksAndMessages(null)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }


    override fun barcodeResult(result: BarcodeResult?) {
        Log.v("Barcode result:", result!!.text) // Prints scan results

        Log.v("Barcode result:", result.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)

        if (result!= null && !code.equals(result.text)) {
            playSound()

            try {
                Notify.alerterGreen(activity, result!!.text)
            }catch (e: Exception){
                e.printStackTrace()
            }

            Log.d(TAG, "barcodeResult: called " + result.text)
            code = result.text

            callFetchBarcodeDetailsApi("123456")

            rlbarcodep?.visibility = View.GONE
        }
    }

    private fun playSound() {
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }
    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

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