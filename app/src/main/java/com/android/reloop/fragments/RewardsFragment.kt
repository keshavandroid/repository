package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.adapters.AdapterDonations
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.donations.DonationCategories
import com.reloop.reloop.network.serializer.donations.DonationProducts
import com.reloop.reloop.network.serializer.donations.DonationsRemainingPoints
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type

/**
 * A simple [Fragment] subclass.
 */
class RewardsFragment : BaseFragment(), RecyclerViewItemClick, OnNetworkResponse,
    AlertDialogCallback {

    companion object {
        fun newInstance(): RewardsFragment {
            return RewardsFragment()
        }
    }

    var recyclerView: RecyclerView? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var dataList: ArrayList<DonationCategories> = ArrayList()
    var totalPoints: TextView? = null
    var itemPosition: Int = 0
    var gotoStore: TextView? = null

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (dataList.size == 0)
                populateData()
            totalPoints?.text =
                User.retrieveUser()?.reward_points?.let { Utils.commaConversion(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_rewards, container, false)
        initViews(view)
        setListeners()
        return view
    }

    private fun initViews(view: View?) {
        recyclerView = view?.findViewById(R.id.donations)
        ViewCompat.setNestedScrollingEnabled(recyclerView!!, false);

        totalPoints = view?.findViewById(R.id.totalUserPoints)
        gotoStore = view?.findViewById(R.id.gotoStore)
        setClickSpan(requireActivity().getString(R.string.reloop_store))
    }

    private fun setClickSpan(textToUnderLine: String) {
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.colorTheme)
                ds.isUnderlineText = true
            }

            override fun onClick(widget: View) {
                Log.e("UTils", "click Span")
                if (HomeActivity.bottomNav.selectedItemId != R.id.navigation_shop) {
                    HomeActivity.bottomNav.selectedItemId = R.id.navigation_shop
                }
            }
        }
        val tvt = gotoStore?.text.toString()
        var ofe = tvt.indexOf(textToUnderLine, 0)
        val wordToSpan = SpannableString(gotoStore?.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToUnderLine, ofs)
            if (ofe == -1) break else {
                wordToSpan.setSpan(
                    clickableSpan,
                    ofe,
                    ofe + textToUnderLine.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                gotoStore?.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
        gotoStore?.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setListeners() {

    }

    @SuppressLint("SetTextI18n")
    private fun populateData() {
        totalPoints?.text = User.retrieveUser()?.reward_points?.let { Utils.commaConversion(it) }
        if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.DONATION_CATEGORIES)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.donationCategories())
                ?.execute()
        }
    }

    override fun itemPosition(position: Int) {
        itemPosition = position
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DONATION_PRODUCTS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.donationProducts(dataList[position].id))
            ?.execute()
    }


    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.DONATION_CATEGORIES -> {

                val gson = Gson()
                val listType: Type = object : TypeToken<List<DonationCategories?>?>() {}.type
                dataList = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)
                if (dataList.size <= 0) {
                    dataList = ArrayList()
                }

                linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)
                recyclerView?.layoutManager = linearLayoutManager
                recyclerView?.adapter = activity?.let { AdapterDonations(dataList, this, it) }
            }
            RequestCodes.API.DONATION_PRODUCTS -> {

                val gson = Gson()
                val listType: Type = object : TypeToken<ArrayList<DonationProducts>>() {}.type
                val apiList: ArrayList<DonationProducts?> = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)
                AlertDialogs.donateRedeemPointsPopup(requireActivity(), apiList, this, dataList[itemPosition].name)
            }
            RequestCodes.API.DONATIONS -> {
                val donationsRemainingPoints = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    DonationsRemainingPoints::class.java)

                val userModel = User.retrieveUser()
                userModel?.reward_points = donationsRemainingPoints.remainingPoints
                userModel?.save(userModel, context,false)
                totalPoints?.text = User.retrieveUser()?.reward_points?.let { Utils.commaConversion(it) }
//              Notify.alerterGreen(activity, baseResponse?.message)
//              AlertDialogs.dismissDialog()

                AlertDialogs.alertDialogRewards(activity, baseResponse?.message) //new
                AlertDialogs.dismissDialog()
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        AlertDialogs.dismissDialog()
    }

    override fun callDialog(model: Any?) {
        val position = model as? Int
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DONATIONS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.donations(position))
            ?.execute()
    }
}
