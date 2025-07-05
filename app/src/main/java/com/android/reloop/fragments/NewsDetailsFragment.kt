package com.android.reloop.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.android.reloop.adapters.CustomPagerAdapter
import com.android.reloop.adapters.SliderAdapter
import com.android.reloop.customviews.InfiniteCirclePageIndicator
import com.android.reloop.customviews.InfinitePagerAdapter
import com.android.reloop.customviews.InfiniteViewPager
import com.android.reloop.network.serializer.Campain.News.NewsDetails
import com.android.reloop.utils.ViewPagerDots
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.smarteist.autoimageslider.SliderView
import kotlinx.android.synthetic.main.fragment_news_details.*
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat

class NewsDetailsFragment : Fragment(), View.OnClickListener, CustomPagerAdapter.ItemClickListener,
    OnNetworkResponse {
    var newsListNew: ArrayList<String>? = ArrayList()
    private var NewsList = ArrayList<NewsDetails?>()
    var mIsVisibleToUser: Boolean = false
    var back: Button? = null
    var tvtitle : TextView ?= null
    var tvdesc : TextView ?= null
    var tvDate : TextView ?= null

//    var layoutDots: LinearLayout? = null
//    var viewpager: ViewPager? = null

    var viewpager: InfiniteViewPager? = null
    var mPagerAdapter: InfinitePagerAdapter? = null
    var layoutDots: InfiniteCirclePageIndicator? = null
    val imageHandler = Handler()


    private var mContext: Context? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_news_details, container, false)
        initViews(view)
        setListeners()
        callNewsDetailApi()
        return view
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
    }

    private fun callNewsDetailApi() {

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.NEWS_DETAILS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getNewsDetails(newsId.toString()))
            ?.execute()
    }

    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        tvtitle = view?.findViewById(R.id.tv_title)
        tvdesc = view?.findViewById(R.id.tv_description)
        tvDate = view?.findViewById(R.id.tvDate)
        layoutDots = view?.findViewById(R.id.layoutDots)
        viewpager = view?.findViewById(R.id.viewPager)
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            R.id.back -> {
                activity?.onBackPressed()
            }
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

    companion object {
        private var newsId: String?= null
        fun newInstance(bundle: Bundle): NewsDetailsFragment {
            if (bundle != null) {
                newsId = bundle.getString("newsId")
            }
            Log.e("TAG","=====news id===" + newsId)
            return NewsDetailsFragment()
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        Log.e("TAG","===response===" + baseResponse)
        when (tag) {
            RequestCodes.API.NEWS_DETAILS -> {
                if (baseResponse?.data != null) {
                    val aboutApp = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse.data as? LinkedTreeMap<*, *>),
                        NewsDetails::class.java
                    )
                    if (aboutApp != null)
                    {
                        tvtitle?.setText(aboutApp.getTitle())
                        tvdesc?.setText(aboutApp.getScript())

                        if(aboutApp.getShowdate() == 1 && aboutApp.getDate() != null)
                        {
                            val parser =  SimpleDateFormat("yyyy-MM-dd")
                            val formatter = SimpleDateFormat("dd-MM-yyyy")
                            val formattedDate = formatter.format(parser.parse(aboutApp.getDate().toString()))
                            tvDate?.setText(formattedDate)
                        }

                        NewsList = aboutApp.getNewsImages() as ArrayList<NewsDetails?>
                        for (i in aboutApp.getNewsImages()!!.indices)
                        {
                            newsListNew?.add(aboutApp.getNewsImages()!![i]?.getImage().toString())
                        }
                        //setSliderNew(newsListNew) //old slider
                        setInfiniteViewpager(newsListNew) //new slider
/*
                        for(j in NewsList.indices)
                        {
                            newsListNew?.add(aboutApp.getNewsImages()!![j]?.getImage().toString())
                        }

                        if(newsListNew!!.isNotEmpty() && newsListNew!!.size > 0)
                        {
                            setSliderNew(newsListNew)
                        }*/
                    }
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    /*private fun setSliderNew(listnews: ArrayList<String>?) {

        val myCustomPagerAdapter = CustomPagerAdapter(requireContext(), listnews!!,1,layoutDots!!)
        viewpager?.setAdapter(myCustomPagerAdapter)
        myCustomPagerAdapter.setClicklistner(this)
        ViewPagerDots.addBottomDots(0, mContext!!, layoutDots!!, listnews.size)

        viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(i: Int) {
                ViewPagerDots.addBottomDots(i, mContext!!, layoutDots!!, listnews.size)
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
                    ViewPagerDots.addBottomDots(currentPage + 1, mContext!!, layoutDots!!, listnews.size)
                } else {
                    viewpager!!.setCurrentItem(0, true)
                    ViewPagerDots.addBottomDots(0, mContext!!, layoutDots!!, listnews.size)
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

}