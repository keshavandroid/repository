package com.reloop.reloop.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterBilling
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.billing.Billing
import com.reloop.reloop.network.serializer.billing.UserProductList
import com.reloop.reloop.network.serializer.billing.UserSubscriptionsList
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import retrofit2.Call
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 */
class BillingFragment : BaseFragment(), View.OnClickListener, RecyclerViewItemClick,
    OnNetworkResponse {


    var back: Button? = null

    companion object {
        fun newInstance(): BillingFragment {
            return BillingFragment()
        }

        var TAG = "BillingFragment"

    }

    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var billing: Billing = Billing()
    var tvNoBilling : TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater.inflate(R.layout.fragment_billing, container, false)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        recyclerView = view?.findViewById(R.id.rvBilling)
        tvNoBilling = view?.findViewById(R.id.tvNoBilling)
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
    }

    private fun populateData() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.BILLING_LISTING)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.billingListing())
            ?.execute()

    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
        }
    }

    override fun itemPosition(position: Int) {
        if (position < billing.userOrdersList!!.size) {
            val fragment: Fragment
            fragment = ViewReceiptFragment.newInstance(billing.userOrdersList?.get(position), null,null)
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerBillingFragment,
                fragment,
                Constants.TAGS.OrderHistoryFragment
            )
        } else {
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerBillingFragment,
                SubscriptionFragment.newInstance(),
                Constants.TAGS.SubscriptionFragment
            )
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.BILLING_LISTING -> {
                val baseResponse = Utils.getBaseResponse(response)
                billing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    Billing::class.java
                )
                if (billing.userOrdersList == null) {
                    billing.userOrdersList = ArrayList()
                }
                if (billing.userSubscriptionsList == null) {
                    billing.userSubscriptionsList = ArrayList()
                }

                if (billing.userOrdersList!!.size > 0 || billing.userSubscriptionsList!!.size > 0) {
                    recyclerView?.visibility = View.VISIBLE
                    tvNoBilling?.visibility = View.GONE
                    populateRecyclerViewData(billing.userSubscriptionsList, billing.userOrdersList)
                }
                else{
                    recyclerView?.visibility = View.GONE
                    tvNoBilling?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        recyclerView?.visibility = View.GONE
        tvNoBilling?.visibility = View.VISIBLE
    }

    private fun populateRecyclerViewData(
        userSubscriptionsList: ArrayList<UserSubscriptionsList>?,
        userOrdersList: ArrayList<UserOrders>?
    ) {
        linearLayoutManager = LinearLayoutManager(context)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = AdapterBilling(userSubscriptionsList, this, userOrdersList)
    }

}
