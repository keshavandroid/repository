package com.android.reloop.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.adapters.CampainListAdapter
import com.android.reloop.model.JoinedCampaign
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.donations.RewardsHistory
import com.reloop.reloop.network.serializer.orderhistory.OrderHistory
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.utils.*
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_settings.*
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


class CampaignsListFragment : BaseFragment(),View.OnClickListener, CampainListAdapter.ItemClickListener ,
    AlertDialogCallback, OnNetworkResponse , ParentToChild {

    var rv_campaigns: RecyclerView? = null
    var tvNoCampaign: TextView? = null
    var back: Button? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var campaignList: ArrayList<JoinedCampaign>? = ArrayList()
    var exitId : String = ""

    companion object {

        fun newInstance(): CampaignsListFragment {
            return CampaignsListFragment()
        }

        var TAG = "CampaignsListFragment"
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.fragment_campaigns_list, container, false)

        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun setListeners() {

        back?.setOnClickListener(this)
    }

    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        rv_campaigns = view?.findViewById(R.id.rv_campigns)
        tvNoCampaign = view?.findViewById(R.id.tvNoCampaign)

    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.back->{
                activity?.onBackPressed()
            }
        }
    }

    private fun populateData() {

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.JOINED_CAMPAIGN_LISTING)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.joinedCampaignListing())
            ?.execute()
    }

    override fun itemclick(position: Int) {
        Log.e(TAG,"===item click position===" + position)
        Log.e(TAG,"===item click id===" + campaignList?.get(position)?.getCampaignId().toString())
        val bundle = Bundle()
        bundle.putString("campaignId", campaignList?.get(position)?.getCampaignId().toString())
        BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.campaignListContainer,
            CampainDetailFragment.newInstance(bundle),
            Constants.TAGS.CampainDetailFragment)
    }

    override fun exitCampain(position: Int) {
        Log.e(TAG,"===item click id===" + campaignList?.get(position)?.getId().toString())
        exitId = campaignList?.get(position)?.getCampaignId().toString()
        AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.exit_campaign_text_msg))
    }

    override fun callDialog(model: Any?) {
        //exit from campaign and go back
        callExitCampainApi()
    }

    private fun callExitCampainApi() {
        Log.e(TAG,"===exit id===" + exitId)
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.EXIT_CAMPAIGNS)
            ?.autoLoading(requireActivity())
//            ?.enque(Ne twork().apis()?.getCampaignDetails(exitId))
            ?.enque(Network().apis()?.exitCampaign(exitId))
            ?.execute()
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.JOINED_CAMPAIGN_LISTING -> {
                val baseResponse = Utils.getBaseResponse(response)
                val gson = Gson()
                val listType: Type = object : TypeToken<List<JoinedCampaign?>?>() {}.type
                campaignList = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)

                Log.e(TAG,"===campaign list===" + campaignList?.size)

                if(campaignList?.size!! > 0) {
                    rv_campaigns?.visibility = View.VISIBLE
                    tvNoCampaign?.visibility = View.GONE
                    populateRecyclerViewData(campaignList)
                } else{
                    Notify.alerterRed(activity, "No campaigns joined!")
                    rv_campaigns?.visibility = View.GONE
                    tvNoCampaign?.visibility = View.VISIBLE
                }

                /*  if (campgins == null) {
                  campgins = ArrayList()
                  }
                  if (userCollectionRequests == null) {
                      userCollectionRequests = ArrayList()
                  }
               populateRecyclerViewData(userOrders, userCollectionRequests)*/
            }

            RequestCodes.API.EXIT_CAMPAIGNS -> {
                val baseResponse = Utils.getBaseResponse(response)
                val message = baseResponse?.message
                Log.e(TAG,"===campaign list===" + campaignList?.size)
                Notify.alerterGreen(activity, message)
                populateData()
            }
        }
    }

    private fun populateRecyclerViewData(campaignList: java.util.ArrayList<JoinedCampaign>?) {
       Log.e(TAG,"===populateData===" + campaignList?.size)
       linearLayoutManager = LinearLayoutManager(context)
       rv_campaigns?.layoutManager = linearLayoutManager
       val adapter = CampainListAdapter(requireContext(), campaignList!!)
       rv_campaigns?.adapter = adapter
       adapter.setClicklistner(this)
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        requireActivity().onBackPressed()
    }

    override fun callChild() {
        populateData()
    }
}