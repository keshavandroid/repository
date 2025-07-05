package com.reloop.reloop.fragments


import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.fragments.CampainDetailFragment
import com.android.reloop.interfaces.SupportClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterCollectionBins
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.orderhistory.CollectionBins
import com.reloop.reloop.network.serializer.orderhistory.CollectionBinsList
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequestImages
import com.reloop.reloop.network.serializer.orderhistory.RequestImage
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import retrofit2.Call
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class CollectionBinsFragment : BaseFragment(), View.OnClickListener, RecyclerViewItemClick,
    OnNetworkResponse, ParentToChild, SupportClick, AlertDialogCallback {

    private var collectionBinsList:ArrayList<CollectionBinsList>? = ArrayList()
    var back: Button? = null
    var childToParent: ChildToParent? = null
    var request_images: ArrayList<RequestImage>? = ArrayList()

    companion object {

        var catunit: Int? = 0
        var pos: Int? = 0
        var reqid: String? = ""

            fun newInstance(bundle: Bundle): CollectionBinsFragment {
                if (bundle != null) {
                    reqid = bundle.getString("reqId")
                    pos = bundle.getInt("pos")
                    catunit = bundle.getInt("unit")
                }
                Log.e("TAG", "=====req id===" + reqid)
                Log.e("TAG", "=====pos===" + pos)
                Log.e("TAG", "=====unit===" + catunit)

                return CollectionBinsFragment()
        }

        var TAG = "CollectionBinsFragment"

    }

    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var categoryName: TextView? = null
    var categoryWeight: TextView? = null

    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_collection_bins, container, false)


        initViews(view)
        setListeners()

        populateData()


        return view
    }


    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        recyclerView = view?.findViewById(R.id.rvCollectionBins)
        categoryName = view?.findViewById(R.id.txtCatName)
        categoryWeight = view?.findViewById(R.id.tvweight)

    }

    private fun setListeners() {
        back?.setOnClickListener(this)
    }


    private fun populateData() {
        //showDialog()
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.COLLECTION_BIN_DETAILS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.collectionBinDetails(reqid.toString()))
            ?.execute()
    }


    private fun populateRecyclerViewData(
        combineList: ArrayList<CollectionBinsList>?,
        categoryname: String?) {
        //new added
        Log.d("SPPEDTEST","111")

        Collections.sort(combineList, object : Comparator<Any?> {
            override fun compare(o1: Any?, o2: Any?): Int {
                var res = 0
                if (o1 is CollectionBinsList && o2 is CollectionBinsList) {
                    res = (o1 as CollectionBinsList).getCreatedAt()!!.compareTo((o2 as CollectionBinsList).getCreatedAt()!!)
                }
                return res
            }
        })

        Log.d("SPPEDTEST","222")

//        Log.d("COMBINE"," LIST : " + GsonBuilder().setPrettyPrinting().create().toJson(combineList))

        linearLayoutManager = LinearLayoutManager(activity)
        //linearLayoutManager!!.reverseLayout = true
        //linearLayoutManager!!.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        //OLD
//        recyclerView?.adapter = AdapterOrderHistory(orderList, this, userCollectionRequests,combineList, this)//old

        //NEW
        recyclerView?.adapter = AdapterCollectionBins( this,combineList,
            categoryname.toString(),this)//old

        if (recyclerView?.adapter?.itemCount == 0) {
            //dismissProgressDialog()
        } else {
            recyclerView!!.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (recyclerView!!.childCount > 0) {
                        // RecyclerView items are loaded and displayed
                        //dismissProgressDialog()
                        recyclerView!!.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    return true
                }
            })
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
        }
    }

    //new
    override fun itemPosition(position: Int) {

        val bundle = Bundle()
        bundle.putSerializable("images", collectionBinsList?.get(position)?.getRequestImages())
        bundle.putString("catName",categoryName?.text.toString())
        bundle.putString("catWeight", collectionBinsList?.get(position)?.getWeight().toString())
        //bundle.putString("catWeight", "Weight : "+collectionBinsList?.get(position)?.getWeight().toString()+" Kg")
        bundle.putInt("unit", collectionBinsList?.get(position)?.getLocationBin()?.materialCategory?.unit!!)

        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.collectionBinsFragment,
            CollectionBinDetailsFragment.newInstance(bundle),
            Constants.TAGS.ContactUsFragment
        )
    }

    private fun preloadAllImages(imagesUrls: List<String>) {

//        Log.d("IMAGES_SCROLL",""+GsonBuilder().setPrettyPrinting().create().toJson(imagesUrls))
        Log.d("IMAGES_SCROLL",""+imagesUrls.size)


        for (url in imagesUrls) {
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

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.COLLECTION_BIN_DETAILS -> {
                val collectionBins = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    CollectionBins::class.java)

                val categoryname = collectionBins.category_name
                val weight = collectionBins.weight

                categoryName?.setText("Category Name : " + categoryname)
                //request_images = collectionBins.request?.getRequestImages() as ArrayList<RequestImage>?

                if (!weight.toString().isNullOrEmpty()) {
                    //var unit = ""

                    //unit = when (collectionBins.collection_bins?.get(pos!!)?.getLocationBin()?.materialCategory?.unit) {
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
                    categoryWeight?.text = "Weight : " + "${weight} $unit"
                } else {
                    categoryWeight?.visibility = View.GONE
                }

                collectionBinsList = collectionBins.collection_bins
                if(collectionBinsList?.isNotEmpty()!!)
                {
                    populateRecyclerViewData(collectionBinsList,categoryname)
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        recyclerView?.visibility = View.GONE
    }

    override fun callDialog(model: Any?) {
        TODO("Not yet implemented")
    }

    override fun callChild() {
        TODO("Not yet implemented")
    }

    override fun openFragment(position: Int, status: String) {
        TODO("Not yet implemented")
    }
}
