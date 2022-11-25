package com.reloop.reloop.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.reloop.reloop.R
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.utils.Constants

class RequestSubmitConfirmationFragment : BaseFragment() {

    companion object {
        fun newInstance(): RequestSubmitConfirmationFragment {
            return RequestSubmitConfirmationFragment()
        }
    }
    var purchaseID:String?=""
    var idNumber:TextView?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_request_submitted_confirmation, container, false)
        if (RecycleFragment.stepView != null) {
            RecycleFragment.stepView!!.StepNumber(Constants.recycleStep4)
        }
        HomeActivity.clearAllFragments(true)
        purchaseID = arguments?.getString(Constants.DataConstants.purchaseID)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        idNumber=view?.findViewById(R.id.idNumber)
    }

    private fun setListeners() {

    }

    private fun populateData() {
        idNumber?.text = purchaseID
        RecycleFragment.changeAddressDistrictID=0
    }

}
