package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.AdapterSubscriptionDetail
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


/**
 * A simple [Fragment] subclass.
 */
class ShopDetailFragment : BaseFragment(), RecyclerViewItemClick, View.OnClickListener,
    OnNetworkResponse {

    companion object {
        var TAG = "ShopDetailFragment"


        fun newInstance(): ShopDetailFragment {
            return ShopDetailFragment()
        }

    }
    var mIsVisibleToUser: Boolean = false
    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var back: Button? = null
    var tvHeading: TextView? = null
    var heading: String? = ""
    var icon: Int? = -1
    var positionDetail: Int? = -1
    var productId: Int? = -1
    var productType: Int? = -1
    var apiList: List<Category>? = null
    var adapter: AdapterSubscriptionDetail? = null
    var next: Button? = null
    var serviceType: Int? = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_detail_subscription, container, false)
        heading = arguments?.getString(Constants.DataConstants.shopFragmentHeading)
        icon = arguments?.getInt(Constants.DataConstants.shopFragmentIcon)
        positionDetail = arguments?.getInt(Constants.DataConstants.position)
        productId = arguments?.getInt(Constants.DataConstants.Api.productID)
        productType = arguments?.getInt(Constants.DataConstants.Api.productType)
        serviceType = arguments?.getInt(Constants.DataConstants.Api.serviceType)

        Log.e(TAG,"===shop detail open===")
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    override fun onDetach() {
        Log.e(TAG,"===onDetach ==")
        super.onDetach()
    }

    override fun onAttach(context: Context) {
        Log.e(TAG,"===onAttach ==")
        super.onAttach(context)
    }

    override fun onResume() {
        Log.e(TAG,"===onResume ==")
        super.onResume()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        Log.e("TAG","====setUserVisibleHint ===" + isVisibleToUser)
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
        if (isResumed) {
            if (mIsVisibleToUser) {
                val handler = Handler()
                handler.postDelayed(
                    {
                        populateData()
                    }, 500
                )
            }
        }
    }

    private fun initViews(view: View?) {
        recyclerView = view?.findViewById(R.id.rvMonthlySubscription)
        back = view?.findViewById(R.id.back)
        tvHeading = view?.findViewById(R.id.tv_heading_category)
        next = view?.findViewById(R.id.next)
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        next?.setOnClickListener(this)
    }

    private fun populateData() {
        tvHeading?.text = heading
        if (productType == Constants.productType) {
            next?.visibility = View.VISIBLE
            next?.text = getString(R.string.go_to_cart)
        } else {
            next?.visibility = View.GONE
        }
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.PRODUCT_LIST)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.products(productId, productType))
            ?.execute()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun populateRecyclerViewData() {
        /*   if (apiList?.indices != null) {
               for (i in apiList?.indices!!) {
                   apiList?.get(i)?.icon = icon
               }
           }*/
        linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)
        recyclerView?.layoutManager = linearLayoutManager
        adapter = AdapterSubscriptionDetail(apiList, this, productType,serviceType, icon, activity)
        recyclerView?.adapter = adapter
    }

    override fun itemPosition(position: Int) {
        if (productType == Constants.productType) {
            if (apiList?.get(position)?.limit!! > 0) {
                if (HomeActivity.cartList != null) {
                    val cartItem: Category? = HomeActivity.cartList?.find { it?.id == apiList?.get(position)?.id }
                    if (cartItem != null) {
                        Notify.alerterRed(activity, getString(R.string.already_added_to_cart_msg))
                    } else {
                        Notify.alerterGreen(activity, getString(R.string.add_to_cart_msg))
                        HomeActivity.cartList?.add(apiList?.get(position))
                        HomeActivity.refreshCart()
                    }
                }
            } else {
                Notify.alerterRed(activity, getString(R.string.out_of_stock_err_msg))
            }
        } else if (productType == Constants.serviceType) {
            val fragment = ServicePurchasingFragment.newInstance()
            val args = Bundle()
            args.putInt(
                Constants.DataConstants.Api.productID,
                apiList?.get(position)?.id!!
            )
            args.putInt(
                Constants.DataConstants.Api.productType,
                apiList?.get(position)?.category_id!!
            )
            args.putString(
                Constants.DataConstants.Api.planID,
                apiList?.get(position)?.stripe_product_id
            )
            args.putDouble(
                Constants.DataConstants.Api.planPrice,
                apiList?.get(position)?.price!!
            )
            args.putInt(
                Constants.DataConstants.Api.serviceType,
                serviceType!!
            )
            fragment.arguments = args
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerMonthlySubscriptionFragment,
                fragment,
                Constants.TAGS.ServicePurchasingFragment
            )
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                requireActivity().onBackPressed()
            }
            R.id.next -> {
                if (HomeActivity.cartList?.size == 0) {
                    Notify.alerterRed(activity, "Add Items To Cart First")
                } else {
                    BaseActivity.replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerMonthlySubscriptionFragment,
                        ProductPurchasingFragment.newInstance(),
                        ProductPurchasingFragment.TAG
                    )
                }
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.PRODUCT_LIST -> {
                val baseResponse = Utils.getBaseResponse(response)
                val gson = Gson()
                val listType: Type =
                    object : TypeToken<List<Category?>?>() {}.type
                apiList = gson.fromJson(
                    Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>),
                    listType
                )
                populateRecyclerViewData()
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    fun openpage(id: Int?, serviceType: Int?, requireActivity: FragmentActivity) {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.PRODUCT_LIST)
            ?.autoLoading(requireActivity)
            ?.enque(Network().apis()?.products(id, serviceType))
            ?.execute()
        }

    fun refreshView() {

        populateData()
    }


}
