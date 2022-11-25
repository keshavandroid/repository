package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.os.Bundle
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
import com.reloop.reloop.R
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
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.utils.*
import retrofit2.Call
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 */
class ViewReceiptFragment : BaseFragment(), RecyclerViewItemClick, View.OnClickListener,
    OnNetworkResponse, AlertDialogCallback {

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
//    var supportButton: Button? = null
    var orderName: TextView? = null
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
    var hideList = false
    var collectionRequestType = 1
    var productOrderType = 2
    var refunded_price_layout: RelativeLayout? = null
    var refundedPrice: TextView? = null
    var delivery_price_layout: RelativeLayout? = null
    var deliveryPrice: TextView? = null
    var subtotal_price_layout: RelativeLayout? = null
    var subTotalPrice: TextView? = null


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
        orderName = view?.findViewById(R.id.order_name)
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


    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        cancelOrder?.setOnClickListener(this)
//        supportButton?.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    private fun populateData() {
        if (userCollectionRequest == null) {
            hideList = false
            setTrackList(userOrder?.status,"order")
            modelViewReceipt = ModelViewReceipt(
                userOrder?.order_number,
                userOrder?.created_at,
                userOrder?.location
                , userOrder?.status, userOrder?.order_number, userOrder?.order_items,
                trackList, userOrder?.id!!
            )
            totalPrice?.text =
                "${Constants.currencySign} ${Utils.commaConversion(userOrder?.total)}"
            setOrderStatus(modelViewReceipt?.status,"order")
            adapter = AdapterViewReceiptOrderList(userOrder?.order_items, this, null)
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
                deliveryPrice?.text =
                    "${Constants.currencySign} ${Utils.commaConversion(userOrder?.delivery_fee?.toDouble())}"
            } else {
                userOrder?.delivery_fee = "0"
            }
            subtotal_price_layout?.visibility = View.VISIBLE
            subTotalPrice?.text =
                "${Constants.currencySign} ${Utils.commaConversion(userOrder?.total?.minus(userOrder?.delivery_fee?.toDouble()!!))}"
        }
        else {
            when (userCollectionRequest?.confirm) {
                0 -> {
                    hideList = false
                }
                1 -> {
                    hideList = true
                }
            }
            setTrackList(userCollectionRequest?.status,"collection")
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
            if (!hideList) {
                userCollectionRequest?.request_collection = ArrayList()
            }
            adapter =
                AdapterViewReceiptOrderList(null, this, userCollectionRequest?.request_collection)
            orderDateTime?.text =
                "Submission Date " + Utils.getFormattedDisplayDate(modelViewReceipt?.orderDateTime)
            order_date_schedule?.text =
                "Schedule Date " + Utils.getFormattedDisplayDateCollection(userCollectionRequest?.collection_date)
        }

        orderName?.text = modelViewReceipt?.orderTitle
        orderAddress?.text = modelViewReceipt?.address
        linearLayoutManager = LinearLayoutManager(activity)
        recyclerView?.layoutManager = linearLayoutManager


        recyclerView?.adapter = adapter
    }

    private fun setOrderStatus(status: Int?, s: String) {
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

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                requireActivity().onBackPressed()
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

    private fun setTrackList(status: Int?, type: String) {
        trackList.clear()

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

        if(type.equals("collection")) {
            trackList.add(
                ModelTrackOrder(
                    COMPLETED,
                    "Order " + MainApplication.applicationContext().getString(R.string.collected)
                )
                //collected
            )
        }
        else if(type.equals("order"))
        {
            trackList.add(
                ModelTrackOrder(
                    COMPLETED,
                    "Order " + MainApplication.applicationContext().getString(R.string.delivered)
                )
                //collected
            )
        }

        if(type.equals("collection")) {
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
}
