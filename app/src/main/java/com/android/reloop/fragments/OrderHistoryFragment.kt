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
import com.android.reloop.interfaces.SupportClick
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterOrderHistory
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.OrderHistory
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import retrofit2.Call
import retrofit2.Response


/**
 * A simple [Fragment] subclass.
 */
class OrderHistoryFragment : BaseFragment(), View.OnClickListener, RecyclerViewItemClick,
    OnNetworkResponse, ParentToChild, SupportClick {


    var back: Button? = null

    companion object {
        fun newInstance(): OrderHistoryFragment {
            return OrderHistoryFragment()
        }

        var TAG = "OrderHistoryFragment"

    }

    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var tvNoOrders : TextView ? = null
    var orderList: OrderHistory? = null
    var userOrders: ArrayList<UserOrders>? = ArrayList()
    var userCollectionRequests: ArrayList<CollectionRequests>? = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_order_history, container, false)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        recyclerView = view?.findViewById(R.id.rvOrderHistory)
        tvNoOrders = view?.findViewById(R.id.tvNoOrders)
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
    }

    private fun populateData() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.ORDER_LISTING)
            ?.autoLoading(requireActivity())
            ?.enque(
                Network().apis()?.ordersListing()
            )
            ?.execute()
    }

    private fun populateRecyclerViewData(
        orderList: ArrayList<UserOrders>?,
        userCollectionRequests: ArrayList<CollectionRequests>?
    ) {
        linearLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = AdapterOrderHistory(orderList, this, userCollectionRequests, this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
        }
    }

    override fun itemPosition(position: Int) {
        val fragment: Fragment
        fragment = if (position < userOrders!!.size) {
            ViewReceiptFragment.newInstance(userOrders?.get(position), null, this)
        } else {
            ViewReceiptFragment.newInstance(
                null,
                userCollectionRequests?.get(position - userOrders!!.size), this
            )
        }
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerOrderHistory,
            fragment,
            Constants.TAGS.ViewReceiptFragment
        )
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.ORDER_LISTING -> {
                val baseResponse = Utils.getBaseResponse(response)
                orderList = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    OrderHistory::class.java
                )
                userOrders = orderList?.getUserOrders
                userCollectionRequests = orderList?.getUserCollectionRequests
                if (userOrders == null) {
                    userOrders = ArrayList()
                }
                if (userCollectionRequests == null) {
                    userCollectionRequests = ArrayList()
                }

                if (userOrders!!.size > 0 || userCollectionRequests!!.size > 0) {
                    recyclerView?.visibility = View.VISIBLE
                    tvNoOrders?.visibility = View.GONE
                    populateRecyclerViewData(userOrders, userCollectionRequests)
                }
                else{
                    recyclerView?.visibility = View.GONE
                    tvNoOrders?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        recyclerView?.visibility = View.GONE
        tvNoOrders?.visibility = View.VISIBLE
    }

    override fun callChild() {
        populateData()
    }

    override fun openFragment(position: Int, status_: Int) {
        var status = status_
        var orderId: Int? = 0
        var orderName: String? = ""
        if (status == -1) {
            status = 0
            orderId = userCollectionRequests?.get(position - userOrders?.size!!)?.id
            orderName = userCollectionRequests?.get(position - userOrders?.size!!)?.request_number
        } else {
            orderId = userOrders?.get(position)?.id
            orderName = userOrders?.get(position)?.order_number
        }
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerOrderHistory,
            ContactUsFragment.newInstance(
                "" + orderId,
                orderName!!, status
            ),
            Constants.TAGS.ContactUsFragment
        )
    }
}
