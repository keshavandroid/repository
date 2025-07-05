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
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequestImages
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.OrderItems
import com.reloop.reloop.network.serializer.orderhistory.RequestImage
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
class CollectionBinDetailsFragment : BaseFragment(), RecyclerViewItemClick, View.OnClickListener,
   AlertDialogCallback, CollectionImagesPagerAdapter.ItemClickListener {

    companion object {

        var parentToChild: ParentToChild? = null
        var request_images: ArrayList<RequestImage>? = ArrayList()
        var catname:String? = ""
        var catweight:String? = ""
        var catunit:Int? = 0
        fun newInstance(bundle: Bundle): CollectionBinDetailsFragment {
            if (bundle != null) {

                request_images = bundle.getSerializable("images") as ArrayList<RequestImage>?
                catname = bundle.getString("catName")
                catweight = bundle.getString("catWeight")
                catunit = bundle.getInt("unit")
            }
            Log.e("TAG", "=====request_images===" + request_images)
            Log.e("TAG", "=====catName===" + catname)
            Log.e("TAG", "=====catWeight===" + catweight)
            Log.e("TAG", "=====catunit===" + catunit)

            return CollectionBinDetailsFragment()
        }

        var TAG = "CollectionBinDetailsFragment"
    }

    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var back: Button? = null
    var tvphotos : TextView?= null
    var tvcatname : TextView?= null
    var tvcatweight : TextView?= null
    var tvnoimage: TextView? = null
    var llphotos : LinearLayout?= null

    //Photos Viewpager Supervisor
    var viewpagerSupervisor: InfiniteViewPager? = null
    var adapterSupervisor: InfinitePagerAdapter? = null
    var dotsSupervisor: InfiniteCirclePageIndicator? = null
    var handlerSupervisor = Handler()
    var imagesSupervisor: ArrayList<String>? = ArrayList()
    var llSupervisorPhotos: LinearLayout? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_collection_bins_details, container, false)

        initViews(view)
        setListeners()
        //populateData()
        return view
    }

    private fun initViews(view: View?) {

        back = view?.findViewById(R.id.back)
        viewpagerSupervisor = view?.findViewById(R.id.viewPagerSupervisor)
        dotsSupervisor = view?.findViewById(R.id.dotsSupervisor)
        llSupervisorPhotos = view?.findViewById(R.id.llSupervisorPhotos)
        tvcatname = view?.findViewById(R.id.txtCatName)
        tvcatweight = view?.findViewById(R.id.tvweight)
        llphotos = view?.findViewById(R.id.llphotos)
        tvnoimage = view?.findViewById(R.id.tvnoimage)
        tvphotos = view?.findViewById(R.id.tvphotos)


        tvcatname?.setText(catname)
        //tvcatweight?.setText(catweight)

        val unit = when (catunit){
            1 -> {
                "Kg"
            }
            2 -> {
                "Liter"
            }
            3 -> {
                "Pieces"
            }
            else -> {
                ""
            }
        }

        if(catweight == "null")
        {
            tvcatweight?.text = "Weight : " + "${"0"} $unit"
        }
        else{
            tvcatweight?.text = "Weight : " + "${catweight} $unit"
        }

        if(!request_images.isNullOrEmpty() && request_images!!.size > 0)
        {
            tvphotos?.visibility = View.VISIBLE
            llphotos?.visibility = View.VISIBLE
            tvnoimage?.visibility = View.GONE
            loadSuperVisorImages()
        }
        else{
            tvphotos?.visibility = View.GONE
            llphotos?.visibility = View.GONE
            tvnoimage?.visibility = View.VISIBLE
        }
    }

    private fun setListeners() {
        back?.setOnClickListener(this)

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


    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()


        handlerSupervisor.removeCallbacksAndMessages(null)
        handlerSupervisor.removeCallbacks(runnableSlideSupervisor)

    }


    private fun autoSlideHandlerSup() {
        handlerSupervisor = Handler()
        handlerSupervisor.postDelayed(runnableSlideSupervisor, 3000)
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


    @SuppressLint("SetTextI18n")
    private fun populateData() {

        if(!request_images.isNullOrEmpty() && request_images!!.size > 0)
        {
            llSupervisorPhotos?.visibility =  View.VISIBLE
            loadSuperVisorImages()
        }
        else{
            llSupervisorPhotos?.visibility =  View.GONE
        }
    }

    private fun loadSuperVisorImages(){
        if(!request_images.isNullOrEmpty()){
            imagesSupervisor?.clear()

            if(request_images!!.size > 0) {
                // campainList = campaignList.get(0)?.getCampaignImages()
                for (i in request_images!!.indices) {
                    imagesSupervisor?.add(request_images!![i].getImage().toString())
                }
            }

            setInfiniteViewpagerSupervisor(imagesSupervisor)
        }else{
            llSupervisorPhotos!!.visibility = View.GONE
        }
    }


    override fun itemPosition(position: Int) {
        //open new screen collection details

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                requireActivity().onBackPressed()
            }
        }
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

    override fun callDialog(model: Any?) {

    }
}
