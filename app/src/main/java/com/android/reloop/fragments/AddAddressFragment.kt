package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.reloop.searchablespinner.SearchableSpinner
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.MapsActivity
import com.reloop.reloop.adapters.AdapterSpinnerCustom
import com.reloop.reloop.adapters.AdapterSpinnerSimple
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.*
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_add_address.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import retrofit2.Call
import retrofit2.Response
import java.util.*


class AddAddressFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse {


    companion object {
        var address: Addresses? = null
        var dependenciesListing: Dependencies? = null
        fun newInstance(dependenciesListing: Dependencies?): AddAddressFragment {
            this.dependenciesListing = dependenciesListing
            return AddAddressFragment()
        }

        fun newInstance(
            address: Addresses?,
            dependenciesListing: Dependencies?
        ): AddAddressFragment {
            this.address = address
            this.dependenciesListing = dependenciesListing
            return AddAddressFragment()
        }
    }

    var radioGroupHousehold: RadioGroup? = null
    var radioGroupOrganization: RadioGroup? = null
    var latitude: Double? = null
    var longitude: Double? = null
    var citySpinner: Spinner? = null
    var districtSpinner: SearchableSpinner? = null
    var cityTextView: TextView? = null
    var userStatus: Int? = null
    private var mParentListener: OnChildFragmentInteractionListener? = null
    var save: Button? = null
    var back: Button? = null
    var address_header: TextView? = null

    //    var no_of_bedrooms: CustomEditText? = null
    var no_of_occupants: CustomEditText? = null
    var street: CustomEditText? = null
    var no_of_floors: CustomEditText? = null
    var no_of_unit: CustomEditText? = null
    var office: RadioButton? = null
    var warehouse: RadioButton? = null
    var shop: RadioButton? = null
    var villa: RadioButton? = null
    var apartment: RadioButton? = null
    var location: CustomEditText? = null

    var etTitle: CustomEditText?=null
    var title_heading: TextView?=null

    var switch: Switch? = null
    var txtDefaultAddress: TextView? = null
    var headingFloors: TextView? = null
    var floors = ""
    var default_address_layout: RelativeLayout? = null
    val districtFilterList: ArrayList<DependencyDetail>? = ArrayList()
    var building: CustomEditText? = null
    var no_of_bedrooms: CustomEditText? = null
    var headingNoOfBedrooms: TextView? = null
    var property_type_heading: TextView? = null
    var unitNumberValue: String? = ""
    var buildingNameValue: String? = ""
    var streetClusterValue: String? = ""
    var districtFilterNames: ArrayList<String> = ArrayList()
    var fragmentView: View? = null
    var district_heading: TextView? = null
    var building_text_heading: TextView? = null
    var unit_no_heading: TextView? = null
    var street_heading: TextView? = null
    var location_heading: TextView? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mParentListener = if (parentFragment is OnChildFragmentInteractionListener) {
            parentFragment as OnChildFragmentInteractionListener?
        } else {
            throw RuntimeException("The parent fragment must implement OnChildFragmentInteractionListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentView = inflater.inflate(R.layout.fragment_add_address, container, false)
        userStatus = arguments?.getInt(Constants.DataConstants.userStatus)
        if (dependenciesListing == null || dependenciesListing?.cities.isNullOrEmpty()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.DEPENDENCIES)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.dependencies())
                ?.execute()
        }
        initViews(fragmentView)
        setListeners()
        populateData()
        setTextChangeListener()
        return fragmentView
    }

    private fun initViews(view: View?) {
        cityTextView = view?.findViewById(R.id.city_heading)
        save = view?.findViewById(R.id.save)
        back = view?.findViewById(R.id.back)
        citySpinner = view?.findViewById(R.id.city)
        districtSpinner = view?.findViewById(R.id.district)
        no_of_bedrooms = view?.findViewById(R.id.no_of_bedrooms)
        no_of_occupants = view?.findViewById(R.id.no_of_occupants)
        no_of_floors = view?.findViewById(R.id.no_of_floors)
        no_of_unit = view?.findViewById(R.id.no_of_unit)
        street = view?.findViewById(R.id.street)
        office = view?.findViewById(R.id.office)
        warehouse = view?.findViewById(R.id.warehouse)
        shop = view?.findViewById(R.id.shop)
        location = view?.findViewById(R.id.location)
        etTitle = view?.findViewById(R.id.etTitle)
        title_heading = view?.findViewById(R.id.title_heading)
        radioGroupHousehold = view?.findViewById(R.id.radioGroup_household)
        radioGroupOrganization = view?.findViewById(R.id.radioGroup_organization)
        switch = view?.findViewById(R.id.switch_)
        txtDefaultAddress = view?.findViewById(R.id.txtDefaultAddress)
        headingFloors = view?.findViewById(R.id.heading_floors)
        headingNoOfBedrooms = view?.findViewById(R.id.no_of_bedrooms_title)
        default_address_layout = view?.findViewById(R.id.default_address_layout)
        villa = view?.findViewById(R.id.villa)
        apartment = view?.findViewById(R.id.apartment)
        building = view?.findViewById(R.id.building)
        address_header = view?.findViewById(R.id.address_header)
        property_type_heading = view?.findViewById(R.id.property_type_heading)
        district_heading = view?.findViewById(R.id.district_heading)
        building_text_heading = view?.findViewById(R.id.building_text_heading)
        unit_no_heading = view?.findViewById(R.id.unit_no_heading)
        street_heading = view?.findViewById(R.id.street_heading)
        location_heading = view?.findViewById(R.id.location_heading)
        when (userStatus) {
            Constants.UserType.household -> {
                default_address_layout?.visibility = View.GONE
                radioGroupHousehold?.visibility = View.VISIBLE
//                radioGroupOrganization?.visibility = View.GONE //AKSHAY17
                property_type_heading?.visibility = View.VISIBLE
                address_header?.visibility = View.GONE

                title_heading?.visibility = View.GONE
                etTitle!!.visibility = View.GONE

            }
            Constants.UserType.organization -> {
                headingNoOfBedrooms?.visibility = View.GONE
                no_of_bedrooms?.visibility = View.GONE
                radioGroupHousehold?.visibility = View.GONE
//                radioGroupOrganization?.visibility = View.VISIBLE //AKSHAY17
                property_type_heading?.visibility = View.GONE
                address_header?.visibility = View.VISIBLE

                title_heading?.visibility = View.VISIBLE
                etTitle!!.visibility = View.VISIBLE

            }
        }

        title_heading?.text = Utils.colorMyText(title_heading?.text.toString(), 19, 20, Color.RED)
        property_type_heading?.text = Utils.colorMyText(property_type_heading?.text.toString(), 13, 14, Color.RED)
        cityTextView?.text = Utils.colorMyText(cityTextView?.text.toString(), 4, 5, Color.RED)
        district_heading?.text = Utils.colorMyText(district_heading?.text.toString(), 8, 9, Color.RED)
        building_text_heading?.text = Utils.colorMyText(building_text_heading?.text.toString(), 8, 9, Color.RED)
        unit_no_heading?.text = Utils.colorMyText(unit_no_heading?.text.toString(), 8, 9, Color.RED)
        location_heading?.text = Utils.colorMyText(location_heading?.text.toString(), 7, 8, Color.RED)
        street_heading?.text = Utils.colorMyText(street_heading?.text.toString(), 14, 15, Color.RED)
    }

    private fun setListeners() {
        save?.setOnClickListener(this)
        back?.setOnClickListener(this)
        location?.setOnClickListener(this)

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun populateData() {
        districtSpinner?.setTitle("")
        districtSpinner?.setPositiveButton("")
        districtFilterNames.clear()
        for (i in districtFilterList!!.indices) {
            districtFilterNames.add(districtFilterList[i].name.toString())
        }
        //-------------------District Spinner---------------------
        val districtAdapter = AdapterSpinnerSimple(
            R.layout.spinner_item_textview_drawable,
            districtFilterNames,
            activity?.getDrawable(R.drawable.icon_address_location_un)!!, false

        )
        districtSpinner?.adapter = districtAdapter
        var mLastClickTime: Long = 0
        districtSpinner?.setOnTouchListener { v, event ->
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnTouchListener false
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            event.action = MotionEvent.ACTION_UP
            districtSpinner?.onTouch(v, event)
            false
        }
        districtSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }
        }
        //-------------------City Spinner---------------------

        if(!dependenciesListing?.cities.isNullOrEmpty() && dependenciesListing?.cities!!.size > 0) {
            val cityAdapter = AdapterSpinnerCustom(
                R.layout.spinner_item_textview_drawable,
                dependenciesListing?.cities,
                activity?.getDrawable(R.drawable.icon_address_location_un)!!, false
            )
            citySpinner?.adapter = cityAdapter
            citySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (dependenciesListing?.districts != null) {
                        districtFilterList.clear()
                        for (i in dependenciesListing?.districts!!.indices) {
                            if (dependenciesListing?.districts!![i].city_id == dependenciesListing?.cities?.get(
                                    position
                                )?.id
                            ) {
                                districtFilterList.add(dependenciesListing?.districts!![i])
                            }
                        }
                    }
                    districtFilterNames.clear()
                    for (i in districtFilterList.indices) {
                        districtFilterNames.add(districtFilterList[i].name.toString())
                    }
                    districtAdapter.notifyDataSetChanged(districtFilterNames)
                    if (address?.district_id != null) {
                        Utils.setSpinnerDistrictBasedOnId(
                            address?.district_id,
                            districtFilterList,
                            districtSpinner
                        )
                    }
                }
            }
        }

        //-------------------


        no_of_floors?.setText(floors)
        if (address != null) {
            for (i in dependenciesListing?.cities!!.indices) {
                if (dependenciesListing?.cities!![i].id == address?.city_id) {
                    citySpinner?.setSelection(i)
                    break
                }
            }
            for (i in dependenciesListing?.districts!!.indices) {
                if (dependenciesListing?.districts!![i].id == address?.district_id) {
                    districtSpinner?.setSelection(i)
                    break
                }
            }
            if (address?.no_of_bedrooms != null)
                no_of_bedrooms?.setText("${address?.no_of_bedrooms}")
            if (!address?.unit_number.isNullOrEmpty()) {
                no_of_unit?.setText("${address?.unit_number}")
                unitNumberValue = address?.unit_number
            }
            if (!address?.floor.isNullOrEmpty()) {
                floors = address?.floor.toString()
                no_of_floors?.setText("${address?.floor}")
            }
            if (address?.no_of_occupants != null)
                no_of_occupants?.setText("${address?.no_of_occupants}")
            if (!address?.street.isNullOrEmpty()) {
                street?.setText("${address?.street}")
                streetClusterValue = address?.street
            }
            if (!address?.location.isNullOrEmpty())
                location?.setText("${address?.location}")


            //new added
            if (!address?.title.isNullOrEmpty())
                etTitle?.setText("${address?.title}")


            if (!address?.building_name.isNullOrEmpty()) {
                building?.setText("${address?.building_name}")
                buildingNameValue = address?.building_name
            }
            if (address?.latitude != null)
                latitude = address?.latitude
            if (address?.longitude != null)
                longitude = address?.longitude
            if (!address?.type.isNullOrEmpty()) {
                when (userStatus) {
                    Constants.UserType.household -> {
                        if (address?.type!!.equals("Villa", true)) {
                            villa?.isChecked = true
                            no_of_floors?.visibility = View.GONE
                            headingFloors?.visibility = View.GONE
                        } else if (address?.type!!.equals("Apartment", true)) {
                            apartment?.isChecked = true
                            no_of_floors?.visibility = View.VISIBLE
                            headingFloors?.visibility = View.VISIBLE
                        }
                    }
                    Constants.UserType.organization -> {
                        if (address?.type!!.equals("Office", true)) {
                            office?.isChecked = true
                        } else if (address?.type!!.equals("Warehouse", true)) {
                            warehouse?.isChecked = true
                        } else {
                            shop?.isChecked = true
                        }
                    }
                }
            }
            switch?.isChecked = address?.default == 1

            if(address?.default == 1){
                txtDefaultAddress?.setText("Default")
                switch?.isClickable = false
            }else{
                txtDefaultAddress?.setText("Set as default")
                switch?.isClickable = true
            }


//            switch?.isChecked = !(address?.default == null || address?.default == 0)
        }
        /* radioGroupOrganization?.setOnCheckedChangeListener { group, checkedId ->
             // checkedId is the RadioButton selected
             if (checkedId == R.id.office) {
                 no_of_floors?.visibility = View.GONE
                 headingFloors?.visibility = View.GONE
             } else {
                 no_of_floors?.visibility = View.VISIBLE
                 headingFloors?.visibility = View.VISIBLE
             }
         }*/
        radioGroupHousehold?.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton selected
            if (checkedId == R.id.villa) {
                no_of_floors?.visibility = View.GONE
                headingFloors?.visibility = View.GONE
            } else {
                no_of_floors?.visibility = View.VISIBLE
                headingFloors?.visibility = View.VISIBLE
            }
        }
    }


    interface OnChildFragmentInteractionListener {
        fun messageFromChildToParent()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
            R.id.location -> {
                val intent = Intent(activity, MapsActivity::class.java)
                val bundle = Bundle()
                if (!address?.location.isNullOrEmpty()) {
                    if (address?.latitude == null) {
                        address?.latitude = 0.0
                    }
                    if (address?.longitude == null) {
                        address?.longitude = 0.0
                    }
                    bundle.putDouble(Constants.DataConstants.latitude, address?.latitude!!)
                    bundle.putDouble(Constants.DataConstants.longitude, address?.longitude!!)
                    intent.putExtra(Constants.DataConstants.bundle, bundle)
                }
                startActivityForResult(intent, Constants.mapsCode)
            }
            R.id.save -> {
                if (address?.default == 1 && !switch!!.isChecked) {
                    Notify.alerterRed(activity, "Select Different Default address")
                } else {
                    addAddress()
                }
            }
        }
    }

    private fun addAddress() {
        when (userStatus) {
            Constants.UserType.household -> {
                val spinnerPosition = districtSpinner?.selectedItemPosition
                val citySpinnerPosition = citySpinner?.selectedItemPosition

                when {
                    radioGroupHousehold?.checkedRadioButtonId == -1 -> {
                        Notify.alerterRed(
                            activity,
                            "Select " + activity?.getString(R.string.property_type)
                        )
                    }
                    citySpinnerPosition!! < 0 -> {
                        Notify.alerterRed(activity, "Select " + activity?.getString(R.string.city))
                    }
                    spinnerPosition!! < 0 -> {
                        Notify.alerterRed(
                            activity,
                            "Select " + activity?.getString(R.string.district)
                        )
                    }
                    street?.text.toString().isEmpty() -> {
                        Notify.alerterRed(activity, "Enter " + activity?.getString(R.string.street))
                    }
                    building?.text.toString().isEmpty() -> {
                        Notify.alerterRed(activity, "Enter " + activity?.getString(R.string.building_name))
                    }
//                    no_of_bedrooms?.text.toString().isEmpty() -> {
//                        Notify.alerterRed(
//                            activity,
//                            "Enter " + activity?.getString(R.string.no_of_bedrooms)
//                        )
//                    }
                    /* no_of_floors?.text.toString().isEmpty() -> {
                         Notify.alerterRed(activity, "Enter "+activity?.getString(R.string.no_of_floors))
                     }*/
                    no_of_unit?.text.toString().isEmpty() -> {
                        Notify.alerterRed(
                            activity,
                            "Enter " + activity?.getString(R.string.unit_number)
                        )
                    }
                    location?.text.toString().isEmpty() -> {
                        //Notify.alerterRed(activity, "Enter " + activity?.getString(R.string.location))
                        Notify.alerterRed(activity, "Please select pin location")
                    }


                    latitude == 0.0 || latitude == null || longitude == 0.0 || longitude == null -> {
                        if (latitude == 0.0 || latitude == null) {
                            Notify.alerterRed(
                                activity,
                                getString(R.string.lat_value_not_available)
                            )
                        } else if (longitude == 0.0 || longitude == null) {
                            Notify.alerterRed(
                                activity,
                                getString(R.string.lng_value_not_available)
                            )
                        }
                    }
//                    no_of_occupants?.text.toString().isEmpty() -> {
//                        Notify.alerterRed(
//                            activity,
//                            "Enter " + activity?.getString(R.string.no_of_occupants)
//                        )
//                    }
                    else -> {
                        val postAddresses: PostAddress? = PostAddress()
                        if (radioGroupHousehold?.checkedRadioButtonId == R.id.villa) {
                            postAddresses?.type = "Villa"
                            floors = ""
                        } else if (radioGroupHousehold?.checkedRadioButtonId == R.id.apartment) {
                            postAddresses?.type = "Apartment"
                            floors = no_of_floors?.text.toString()
                        }
                        postAddresses?.id = address?.id
                        postAddresses?.city_id = dependenciesListing?.cities?.get(citySpinner?.selectedItemPosition!!)?.id
                        postAddresses?.district_id = districtFilterList?.get(districtSpinner?.selectedItemPosition!!)?.id
                        if (!no_of_bedrooms?.text.toString().isNullOrEmpty())
                            postAddresses?.no_of_bedrooms = no_of_bedrooms?.text.toString().toInt()
                        if (!no_of_occupants?.text.toString().isNullOrEmpty())
                            postAddresses?.no_of_occupants =
                                no_of_occupants?.text.toString().toInt()
                        postAddresses?.street = street?.text.toString()
                        postAddresses?.floor = floors
                        postAddresses?.location = location?.text.toString()

                        postAddresses?.latitude = latitude
                        postAddresses?.longitude = longitude
                        postAddresses?.unit_number = no_of_unit?.text.toString()
                        postAddresses?.building_name = building?.text.toString()
                        postAddresses?.default = 1
                        NetworkCall.make()
                            ?.setCallback(this)
                            ?.setTag(RequestCodes.API.UPDATE_ADDRESS)
                            ?.autoLoading(requireActivity())
                            ?.enque(Network().apis()?.updateAddress(postAddresses))
                            ?.execute()

                    }
                }
            }
            Constants.UserType.organization -> {
                val spinnerPosition = districtSpinner?.selectedItemPosition
                val citySpinnerPosition = citySpinner?.selectedItemPosition

                when {

                    /*radioGroupOrganization?.checkedRadioButtonId == -1 -> {
                        Notify.alerterRed(
                            activity,
                            "Select " + activity?.getString(R.string.property_type)
                        )
                    }*/
                    citySpinnerPosition!! < 0 -> {
                        Notify.alerterRed(activity, "Select " + activity?.getString(R.string.city))
                    }
                    spinnerPosition!! < 0 -> {
                        Notify.alerterRed(
                            activity,
                            "Select " + activity?.getString(R.string.district)
                        )
                    }
                    street?.text.toString().isEmpty() -> {
                        Notify.alerterRed(activity, "Enter " + activity?.getString(R.string.street))
                    }
                    building?.text.toString().isEmpty() -> {
                        Notify.alerterRed(
                            activity,
                            "Enter " + activity?.getString(R.string.building_name)
                        )
                    }
//                    no_of_floors?.text.toString().isEmpty() -> {
//                        Notify.alerterRed(
//                            activity,
//                            "Enter " + activity?.getString(R.string.no_of_floors)
//                        )
//                    }

                    no_of_unit?.text.toString().isEmpty() -> {
                        Notify.alerterRed(
                            activity,
                            "Enter " + activity?.getString(R.string.unit_number)
                        )
                    }
                    location?.text.toString().isEmpty() -> {
                        //Notify.alerterRed(activity, "Enter " + activity?.getString(R.string.location))
                        Notify.alerterRed(activity, "Please select pin location")
                    }

                    //new added
                    etTitle?.text.toString().isEmpty() -> {
                        Notify.alerterRed(
                            activity,
                            "Enter Title"
                        )
                    }

                    latitude == 0.0 || latitude == null || longitude == 0.0 || longitude == null -> {
                        if (latitude == 0.0 || latitude == null) {
                            Notify.alerterRed(
                                activity,
                                getString(R.string.lat_value_not_available)
                            )
                        } else if (longitude == 0.0 || longitude == null) {
                            Notify.alerterRed(
                                activity,
                                getString(R.string.lng_value_not_available)
                            )
                        }
                    }
                 /*   no_of_occupants?.text.toString().isEmpty() -> {
                        Notify.alerterRed(
                            activity,
                            "Enter " + activity?.getString(R.string.no_of_occupants)
                        )
                    }*/

                    else -> {
                        val postAddresses: PostAddress? = PostAddress()
                        if (radioGroupOrganization?.checkedRadioButtonId == R.id.office) {
                            postAddresses?.type = "Office"
                        } else if (radioGroupOrganization?.checkedRadioButtonId == R.id.warehouse) {
                            postAddresses?.type = "Warehouse"
                        } else {
                            postAddresses?.type = "Shop"
                        }
                        floors = no_of_floors?.text.toString()
                        postAddresses?.
                        id = address?.id
                        postAddresses?.city_id = dependenciesListing?.cities?.get(citySpinner?.selectedItemPosition!!)?.id
                        postAddresses?.district_id = districtFilterList?.get(districtSpinner?.selectedItemPosition!!)?.id
//                        postAddresses?.no_of_bedrooms = no_of_bedrooms?.text.toString().toInt()
                        if (!no_of_occupants?.text.toString().isNullOrEmpty())
                            postAddresses?.no_of_occupants =
                                no_of_occupants?.text.toString().toInt()
                        postAddresses?.street = street?.text.toString()
                        postAddresses?.floor = floors
                        postAddresses?.location = location?.text.toString()

                        postAddresses?.title = etTitle?.text.toString() // new added

                        postAddresses?.latitude = latitude
                        postAddresses?.longitude = longitude
                        postAddresses?.unit_number = no_of_unit?.text.toString()
                        postAddresses?.building_name = building?.text.toString()
                        if (switch?.isChecked == true) {
                            postAddresses?.default = 1
                        } else {
                            postAddresses?.default = 0
                        }
                        NetworkCall.make()
                            ?.setCallback(this)
                            ?.setTag(RequestCodes.API.UPDATE_ADDRESS)
                            ?.autoLoading(requireActivity())
                            ?.enque(Network().apis()?.updateAddress(postAddresses))
                            ?.execute()
                    }
                }
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.UPDATE_ADDRESS -> {
                Notify.alerterGreen(activity, baseResponse?.message)
                val handler = Handler()
                handler.postDelayed(
                    {
                        if (mParentListener != null) {
                            mParentListener?.messageFromChildToParent()
                            activity?.onBackPressed()
                        }
                    }, 1200
                )

            }
            RequestCodes.API.DEPENDENCIES -> {
                try {
                    dependenciesListing = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dependencies::class.java
                    )
                    populateData()
                } catch (e: Exception) {
                    Log.e("Edit Profile", e.toString())
                    activity?.onBackPressed()
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Constants.mapsCode) {
            val addressLocation: String? = data?.getStringExtra(Constants.DataConstants.location)
            location?.setText(addressLocation)
            latitude = data?.getDoubleExtra(Constants.DataConstants.latitude, 0.0)
            longitude = data?.getDoubleExtra(Constants.DataConstants.longitude, 0.0)

        }

    }

    override fun onDetach() {
        super.onDetach()
        address = null
    }

    private fun setTextChangeListener() {
        street?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                streetClusterValue = s.toString()
            }
        })
        building?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                buildingNameValue = s.toString()
            }
        })
        no_of_unit?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                unitNumberValue = s.toString()
            }
        })
    }
}
