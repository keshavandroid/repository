package com.android.reloop.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.reloop.adapters.CampainNewsAdapter
import com.android.reloop.adapters.CustomPagerAdapter
import com.android.reloop.customviews.InfiniteCirclePageIndicator
import com.android.reloop.customviews.InfinitePagerAdapter
import com.android.reloop.customviews.InfiniteViewPager
import com.android.reloop.network.serializer.Campain.CampaignDetails.Data
import com.android.reloop.network.serializer.Campain.CampaignDetails.NewsDetails
import com.android.reloop.utils.ViewPagerDots
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.reloop.reloop.BuildConfig
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.fragments.NewBillingInformationFragment
import com.reloop.reloop.fragments.SubscriptionFragment
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.model.ModelShopCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_subscription_cycle_two_step.*
import retrofit2.Call
import retrofit2.Response
import java.io.IOException


class CampainDetailFragment : Fragment(), View.OnClickListener, AlertDialogCallback,
    CampainNewsAdapter.ItemClickListener, CustomPagerAdapter.ItemClickListener, OnNetworkResponse,
    BarcodeCallback {

    private var mContext: Context? = null
    private val TAG = CampainDetailFragment.javaClass.simpleName
    private var code: String = ""
    var back: Button? = null
    var btnJoin: Button? = null
    var mIsVisibleToUser: Boolean = false
    var hasjoined: Boolean = false
    var isjoined: Int = 0
    private lateinit var linearLayoutManager: LinearLayoutManager
    var rvNews: RecyclerView? = null
    var tvCampainName: TextView? = null
    var tvCampainDetail: TextView? = null
    var tvDate: TextView? = null
    var tvParticipants: TextView? = null
    var tvTotalRecycledKg: TextView? = null
    var barcodeScanner: DecoratedBarcodeView? = null
    var rlBarcode: RelativeLayout? = null
    var llbarcode: LinearLayout? = null
    var llNews: LinearLayout? = null
    var campainListNew: ArrayList<String>? = ArrayList()
    var newsListNew: ArrayList<String>? = ArrayList()
    private var NewsList = ArrayList<NewsDetails?>()
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    var cameraSurfaceView: SurfaceView? = null
    var rlmain: RelativeLayout? = null
    var rlbarcode: RelativeLayout? = null
    var ivclose: ImageView? = null

//    var layoutDots: LinearLayout ? = null
//    var viewpager: ViewPager? = null

    var viewpager: InfiniteViewPager? = null
    var mPagerAdapter: InfinitePagerAdapter? = null
    var layoutDots: InfiniteCirclePageIndicator? = null
    val imageHandler = Handler()


    companion object {
        private var campaignId: String? = null

        fun newInstance(bundle: Bundle): CampainDetailFragment {
            if (bundle != null) {
                campaignId = bundle.getString("campaignId")
            }
            Log.e("TAG", "=====campaign id===" + campaignId)
            return CampainDetailFragment()
        }

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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_campain_detail, container, false)
        initViews(view)
        setListeners()

        callCampainDetailApi()
        return view
    }

    private fun callCampainDetailApi() {

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.CAMPAIGNS_DETAILS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getCampaignDetails(campaignId.toString()))
            ?.execute()
    }

   /* private fun initRecycleView(newsListNew: ArrayList<String>)*/
   private fun initRecycleView(newsListNew: ArrayList<NewsDetails?>){

        linearLayoutManager = LinearLayoutManager(requireContext())
        rvNews?.layoutManager = linearLayoutManager
        rvNews?.setLayoutManager(LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false))
        rvNews?.setHasFixedSize(true);
        val adapter = CampainNewsAdapter(requireContext(), newsListNew)
        rvNews?.adapter = adapter
        adapter.setClicklistner(this)
        adapter.notifyDataSetChanged()
    }

    private fun setListeners() {
        rlBarcode?.setOnClickListener(this)
        back?.setOnClickListener(this)
        btnJoin?.setOnClickListener(this)
        ivclose?.setOnClickListener(this)
    }

    private fun initViews(view: View?) {

        back = view?.findViewById(R.id.back)
        rvNews = view?.findViewById(R.id.rvNews)
        tvCampainName = view?.findViewById(R.id.tv_campainName)
        tvCampainDetail = view?.findViewById(R.id.tv_campainDetails)
        tvDate = view?.findViewById(R.id.tvDate)
        tvParticipants = view?.findViewById(R.id.tvparticipants)
        tvTotalRecycledKg = view?.findViewById(R.id.tvweightscale)
        rlBarcode = view?.findViewById(R.id.rlBarcode)
        llbarcode = view?.findViewById(R.id.llBarcode)
        llNews = view?.findViewById(R.id.llNews)
        rlmain = view?.findViewById(R.id.rl_main)
        rlbarcode = view?.findViewById(R.id.rlbarcode)
        cameraSurfaceView = view?.findViewById(R.id.cameraSurfaceView)
        ivclose = view?.findViewById(R.id.ivClose)
        btnJoin = view?.findViewById(R.id.btn_join)
        barcodeScanner = view?.findViewById(R.id.barcodeScanner)
        layoutDots = view?.findViewById(R.id.layoutDots)
        viewpager = view?.findViewById(R.id.viewPager)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.rlBarcode -> {
                /* BaseActivity.replaceFragment(
                     childFragmentManager,
                     Constants.Containers.containerCampainDetail,
                     ProductDetailFragment.newInstance(),
                     Constants.TAGS.ProductDetailFragment
                 )*/
                if (hasjoined) {
                    if (ContextCompat.checkSelfPermission(requireContext(),
                            android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        askForCameraPermission()
                    }
                    else {
                        rlbarcode?.visibility = View.VISIBLE
                        rlmain?.visibility = View.GONE
                        setupControls()
                    }
                } else {
                    //AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.barcode_scan_info_text_msg))
                    showDialog()
                }
            }
            R.id.back -> {
                activity?.onBackPressed()
                viewpager?.beginFakeDrag()
            }
            R.id.btn_join -> {

                //call api to join
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.JOIN_CAMPAIGNS)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.joinCampaign(campaignId.toString()))
                    ?.execute()
            }
            R.id.ivClose -> {
                rlbarcode?.visibility = View.GONE
                rlmain?.visibility = View.VISIBLE
            }
        }
    }

   private fun showDialog() {

        val dialog = Dialog(requireActivity())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_logout)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val message = dialog.findViewById(R.id.tv_heading_category) as TextView
        val cancel = dialog.findViewById(R.id.cancel) as Button
        val confirm = dialog.findViewById(R.id.confirm) as Button
        val crossImage = dialog.findViewById(R.id.cross) as ImageButton
        message.text = activity?.getString(R.string.barcode_scan_info_text_msg)
        confirm.text = activity?.getString(R.string.join)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        crossImage.setOnClickListener {
            dialog.dismiss()
        }
        confirm.setOnClickListener {
            //call api to join campaign
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.JOIN_CAMPAIGNS)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.joinCampaign(campaignId.toString()))
                ?.execute()
            dialog.dismiss()
        }
      dialog.show()
   }

    /* override fun itemclick(bean: ModelCampainNews, position: Int, type: String) {
         BaseActivity.replaceFragment(
             childFragmentManager,
             Constants.Containers.NewsDetail,
             NewsDetailsFragment.newInstance(),
             Constants.TAGS.NewsDetailsFragment
         )
     }*/
    override fun itemclick(position: Int) {

        val bundle = Bundle()
        bundle.putString("newsId", NewsList.get(position)?.getId().toString())
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerCampainDetail,
            NewsDetailsFragment.newInstance(bundle),
            Constants.TAGS.NewsDetailsFragment
        )
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        Log.e("TAG", "===response===" + baseResponse)
        when (tag) {
            RequestCodes.API.CAMPAIGNS_DETAILS -> {
                if (baseResponse?.data != null) {
                    val aboutApp =
                        Gson().fromJson(Utils.jsonConverterObject(baseResponse.data as? LinkedTreeMap<*, *>),
                            Data::class.java)
                    if (aboutApp != null) {
                        tvCampainName?.setText(aboutApp.getTitle())
                        tvCampainDetail?.setText(aboutApp.getDescription())

                        campainListNew?.clear()

                        for (i in aboutApp.getCampaignImages()!!.indices) {
                            campainListNew?.add(aboutApp.getCampaignImages()!![i]?.getImage().toString())
                        }
                        //setSliderNew(campainListNew) //old slider
                        setInfiniteViewpager(campainListNew)// new slider

                        hasjoined = aboutApp.gethasJoined()!!
                        Log.e("TAG", "====has joined===" + hasjoined)
                        if(aboutApp.getIsEnableJoin() == 1)
                        {
                            if (hasjoined) {
                                btnJoin?.visibility = View.GONE
                            } else {
                                btnJoin?.visibility = View.VISIBLE
                            }
                        }
                        else{
                            btnJoin?.visibility = View.GONE
                        }

                        if (aboutApp.getIsEnableBarcode() == 1) {
                            llbarcode?.visibility = View.VISIBLE
                        } else {
                            llbarcode?.visibility = View.GONE
                        }

                        if (aboutApp.getIsEnableNews() == 1) {
                            llNews?.visibility = View.VISIBLE
                            NewsList = aboutApp.getNews() as ArrayList<NewsDetails?>

                            if (NewsList.isNotEmpty() && NewsList.size > 0) {
                                initRecycleView(NewsList)
                            }

                           /* for (j in NewsList.indices) {
                                newsListNew?.add(aboutApp.getNews()!![j]?.getImage().toString())
                            }

                            if (newsListNew!!.isNotEmpty() && newsListNew!!.size > 0) {
                                initRecycleView(newsListNew!!)
                            }*/
                        } else {
                            llNews?.visibility = View.GONE
                        }
                    }
                }
            }
            RequestCodes.API.JOIN_CAMPAIGNS -> {
                val baseResponse = Utils.getBaseResponse(response)
                val message = baseResponse?.message
                Log.e("TAG", "===message===" + message)
                Notify.alerterGreen(activity, message)
                hasjoined = true
                btnJoin?.visibility = View.GONE

                showSuccessDialog()

            }
        }
    }

    private fun showSuccessDialog() {
        val dialog = Dialog(requireContext())
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.row_campaign_joined)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)

        val btnInitDontaion = dialog.findViewById(R.id.btnInitDontaion) as Button


        btnInitDontaion.setOnClickListener {
            dialog.dismiss()

            activity?.onBackPressed()
            viewpager?.beginFakeDrag()
        }

        dialog.show()
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    /*private fun setSliderNew(list: ArrayList<String>?) {
        val myCustomPagerAdapter = CustomPagerAdapter(requireContext(), list!!,1,layoutDots!!)
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
        val myCustomPagerAdapter = CustomPagerAdapter(requireContext(), list!!,1)

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

    override fun itemclickSlide(position: Int) {
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private fun setupControls() {

        if (BuildConfig.DEBUG) {
            showBarcodeScanner()
            return
        }
        barcodeDetector = BarcodeDetector.Builder(context).build()
        var barcodeFactory = com.android.reloop.utils.BarcodeTrackerFactory
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
                    Log.e("TAG", "value-else")
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

    override fun onDestroy() {
        super.onDestroy()
        //cameraSource.stop()
    }

    override fun callDialog(model: Any?) {

    }

    override fun barcodeResult(result: BarcodeResult?) {
        Log.v("Barcode result:", result!!.text) // Prints scan results





        Log.v("Barcode result:",
            result.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)

        if (result!= null && !code.equals(result.text)) {
            playSound()

            try {
                Notify.alerterGreen(activity, result!!.text)
            }catch (e: Exception){
                e.printStackTrace()
            }

            Log.d(TAG, "barcodeResult: called " + result.text)
            code = result.text.toString()

            val bundle = Bundle()
            bundle.putString("barcode",code)
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerCampainDetail,
                ProductDetailFragment.newInstance(bundle),
                Constants.TAGS.ProductDetailFragment)

            rlbarcode?.visibility = View.GONE
            rlmain?.visibility = View.VISIBLE

        }
    }

    private fun playSound() {
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }
    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {

    }
}