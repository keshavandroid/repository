package com.android.reloop.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.android.reloop.bottomsheet.CityBottomsheet
import com.android.reloop.bottomsheet.MaterialBottomsheet
import com.android.reloop.bottomsheet.SelectedFilterBottomsheet
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.fragments.OrderHistoryFragment
import com.reloop.reloop.fragments.RecycleFragment
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


class RequestSucceededFragment : BaseFragment(){

    var img_step5: ImageView? = null
    var text_step5: TextView?= null
    var txtID: TextView?= null
    var txtTrackReq: TextView?=null

    companion object {

        var dropID : String = ""

        fun newInstance(id: String): RequestSucceededFragment {
            this.dropID = id
            return RequestSucceededFragment()
        }
        var TAG = "DropOffPin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.fragment_request_success, container, false)

        initViews(view)
        setListeners()
        setData()
        return view
    }

    private fun setData() {
        txtID!!.setText("REQUEST ID : " + dropID)
    }

    private fun setListeners() {
        txtTrackReq!!.setOnClickListener{
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.requestSuccessContainer,
                OrderHistoryFragment.newInstance(),
                Constants.TAGS.OrderHistoryFragment
            )
        }

        img_step5?.setImageResource(R.drawable.ic_step_succeeded_clicked)
        text_step5?.setTextColor(requireActivity().getColor(R.color.green_color_button))

    }

    private fun initViews(view: View?) {
        img_step5 = view?.findViewById(R.id.img_step5)
        text_step5 = view?.findViewById(R.id.text_step5)
        txtID = view?.findViewById(R.id.txtID)
        txtTrackReq = view?.findViewById(R.id.txtTrackReq)

    }


}