package com.reloop.reloop.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.interfaces.SupportClick

import com.reloop.reloop.R
import com.reloop.reloop.adapters.AdapterRewardsHistory
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.donations.RewardsHistory
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.utils.Constants
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type

/**
 * A simple [Fragment] subclass.
 */
class RewardsHistoryFragment : BaseFragment(), OnNetworkResponse,View.OnClickListener,
    RecyclerViewItemClick, SupportClick {

    companion object {

        fun newInstance(): RewardsHistoryFragment {
            return RewardsHistoryFragment()
        }
    }
    var rv_rewards_history: RecyclerView? = null
    var back: Button? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var tvNoOrders : TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_rewards_history, container, false)
        initView(view)
        setListeners()
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.REWARDS_HISTORY)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.rewardsHistory())
            ?.execute()
        return view
    }

    private fun initView(view: View?) {
        back = view?.findViewById(R.id.back)
        rv_rewards_history = view?.findViewById(R.id.rv_rewards_history)
        tvNoOrders = view?.findViewById(R.id.tvNoBilling)
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
    }

    private fun populateData(apiList: ArrayList<RewardsHistory?>?) {
        linearLayoutManager = LinearLayoutManager(context)
        rv_rewards_history?.layoutManager = linearLayoutManager
        rv_rewards_history?.adapter = AdapterRewardsHistory(apiList, this)
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.REWARDS_HISTORY -> {
//                Notify.alerterGreen(activity, baseResponse?.message)
                val gson = Gson()
                val listType: Type = object : TypeToken<List<RewardsHistory?>?>() {}.type
                val apiList:ArrayList<RewardsHistory?>? = gson.fromJson(
                    Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)

                if (apiList!!.size >0) {
                    rv_rewards_history?.visibility = View.VISIBLE
                    tvNoOrders?.visibility = View.GONE
                    populateData(apiList)
                }
                else{
                    rv_rewards_history?.visibility = View.GONE
                    tvNoOrders?.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        rv_rewards_history?.visibility = View.GONE
        tvNoOrders?.visibility = View.VISIBLE
    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.back->{
                activity?.onBackPressed()
            }
        }
    }

    override fun itemPosition(position: Int) {

    }

    override fun openFragment(position: Int, status: String) {
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerRewardHistory,
            ContactUsFragment.newInstance("", "", "-1",""),
            Constants.TAGS.ContactUsFragment
        )
    }
}
