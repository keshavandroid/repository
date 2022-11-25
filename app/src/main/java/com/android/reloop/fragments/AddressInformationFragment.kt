package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.reloop.reloop.R
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.activities.MapsActivity
import com.reloop.reloop.adapters.viewholders.AdapterSpinnerAddress
import com.reloop.reloop.auth.AddressInfoAuthHousehold
import com.reloop.reloop.auth.AddressInfoAuthOrganization
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.cart.BuyProduct
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.Utils


/**
 * A simple [Fragment] subclass.
 */
class AddressInformationFragment : BaseFragment(), ParentToChild, View.OnClickListener,
    AlertDialogCallback {

    companion object {
        var buyProduct: BuyProduct? = null

        fun newInstance(buyProduct: BuyProduct?): AddressInformationFragment {
            this.buyProduct = buyProduct
            return AddressInformationFragment()
        }

    }

    var childToParent: ChildToParent? = null
    var firstName: CustomEditText? = null
    var lastName: CustomEditText? = null
    var email: CustomEditText? = null
    var phoneNumber: CustomEditText? = null
    var location: CustomEditText? = null
    var user: User? = User.retrieveUser()
    var organizationName: CustomEditText? = null
    var spinnerAddresses: Spinner? = null
    var defaultAddress: Addresses? = null
    var message: TextView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ChildToParent) {
            childToParent = parentFragment as? ChildToParent?
        } else {
            Notify.Toast("Child To Parent Callback Not Implemented")
            activity?.onBackPressed()
        }
    }

//    var districtFilterNames: ArrayList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (ProductPurchasingFragment.stepView != null) {
            ProductPurchasingFragment.stepView!!.StepNumber(Constants.recycleStep2)
        }
        ProductPurchasingFragment.parentToChild = this
        val view = inflater.inflate(R.layout.fragment_address_information, container, false)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        firstName = view?.findViewById(R.id.first_name)
        lastName = view?.findViewById(R.id.last_name)
        email = view?.findViewById(R.id.email)
        phoneNumber = view?.findViewById(R.id.phone_number)
        location = view?.findViewById(R.id.location)
//        citySpinner = view?.findViewById(R.id.city)
//        districtSpinner = view?.findViewById(R.id.district)
        organizationName = view?.findViewById(R.id.organization_name)
        spinnerAddresses = view?.findViewById(R.id.address_organization)
        message = view?.findViewById(R.id.message)
//        district = view?.findViewById(R.id.district)
//        city = view?.findViewById(R.id.city)


    }

    private fun setListeners() {
        location?.setOnClickListener(this)
    }

    private fun populateData() {
        firstName?.setText(user?.first_name)
        lastName?.setText(user?.last_name)
        email?.setText(user?.email)
        phoneNumber?.setText(user?.phone_number)
        organizationName?.setText(user?.organization?.name)
//        district?.setTitle("")
//        district?.setPositiveButton("")
        //-----------------Phone Number Changes--------------
        phoneNumber?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (phoneNumber?.text!!.length <= 6)
                    phoneNumber?.setText("+971-5")
                phoneNumber?.setSelection(phoneNumber?.text!!.length)
            }
        }

        phoneNumber?.addTextChangedListener(object : TextWatcher {
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
                val after = s.toString()
                if (after.length <= 6 && count < before) {
                    phoneNumber?.setText("+971-5")
                    phoneNumber?.setSelection(phoneNumber?.text!!.length)
                } else if (s.length == 7 && count > before) {
                    phoneNumber?.setText(phoneNumber?.text.toString() + "-")
                    phoneNumber?.setSelection(phoneNumber?.text!!.length)
                }
            }
        })
        populateSpinnerData()
        if (!HomeFragment.settings.delivery_Details.isNullOrEmpty()) {
            message?.text ="* "+ HomeFragment.settings.delivery_Details[0].value
        }
    }

    private fun setAddressSpinner() {

        //-------------------Addresses  Spinner---------------------
        val hideArrow = user?.user_type != Constants.UserType.organization
        val address = AdapterSpinnerAddress(
            R.layout.spinner_item_textview_drawable,
            user?.addresses,
            activity?.getDrawable(R.drawable.icon_address_location_un)!!,
            hideArrow
        )
        spinnerAddresses?.adapter = address
        spinnerAddresses?.visibility = View.VISIBLE
        spinnerAddresses?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                try {
                    location?.setText(user?.addresses?.get(position)?.location)
                    buyProduct?.latitude = user?.addresses!![position].latitude
                    buyProduct?.longitude = user?.addresses!![position].longitude
//                    setCityAndDistrictSpinners(position)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }
        setAddressSpinnerSelection()
    }

//    private fun setCityAndDistrictSpinners(position: Int) {
//        if (dependenciesListing != null) {
//            for (i in dependenciesListing?.cities!!.indices) {
//                if (dependenciesListing?.cities!![i].id == user?.addresses?.get(position)?.city_id) {
////                    citySpinner?.setSelection(i)
//                    break
//                }
//            }
//            if (!districtFilterList.isNullOrEmpty())
//                for (i in districtFilterList.indices) {
//                    if (districtFilterList[i].id == user?.addresses?.get(position)?.district_id) {
//                        districtSpinner?.setSelection(i)
//                        break
//                    }
//                }
//        }
//    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.location -> {
                if (user?.addresses.isNullOrEmpty()) {
                    Notify.alerterRed(activity, "Location Not Available")
                } else {
                    val intent = Intent(activity, MapsActivity::class.java)
                    val bundle = Bundle()
                    if (user?.user_type == Constants.UserType.organization) {
                        bundle.putDouble(
                            Constants.DataConstants.latitude,
                            user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.latitude!!
                        )
                        bundle.putDouble(
                            Constants.DataConstants.longitude,
                            user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.longitude!!
                        )
                    } else {
                        bundle.putDouble(
                            Constants.DataConstants.latitude,
                            defaultAddress?.latitude!!
                        )
                        bundle.putDouble(
                            Constants.DataConstants.longitude,
                            defaultAddress?.longitude!!
                        )
                    }
                    bundle.putInt(Constants.DataConstants.removeSaveButton, 1)
                    intent.putExtra(Constants.DataConstants.bundle, bundle)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (resultCode == Constants.mapsCode) {
            val address: String? = data?.getStringExtra(Constants.DataConstants.location)
            location?.setText(address)
            buyProduct?.latitude = data?.getDoubleExtra(Constants.DataConstants.latitude, 0.0)
            buyProduct?.longitude = data?.getDoubleExtra(Constants.DataConstants.longitude, 0.0)
        }
    }


    private fun populateSpinnerData(
    ) {
        try {
            defaultAddress = user?.addresses?.find { it.default == 1 }
            if (defaultAddress == null) {
                defaultAddress = user?.addresses?.find { it.default == 0 }
            }
            if (user != null && user?.email?.isNotEmpty()!!) {
                setAddressSpinner()
                if (user?.user_type == Constants.UserType.organization) {
                    if (!defaultAddress?.location.isNullOrEmpty())
                        location?.setText(defaultAddress?.location)
                    organizationName?.visibility = View.VISIBLE
                    lastName?.visibility = View.GONE
                    firstName?.visibility = View.GONE
                } else {
                    spinnerAddresses?.isClickable = false
                    spinnerAddresses?.isEnabled = false
                    if (!user?.addresses.isNullOrEmpty())
                        location?.setText(user?.addresses?.get(0)?.location)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun callChild() {

        if (user?.user_type == Constants.UserType.household) {
            val authSuccessful: Boolean = AddressInfoAuthHousehold.authenticate(
                firstName?.text.toString(),
                lastName?.text.toString(),
                email?.text.toString(),
                phoneNumber?.text.toString(),
                "location",
                1,
                1,
                activity!!
            )
            if (authSuccessful) {
                if (user?.addresses.isNullOrEmpty()
                    || user?.addresses?.get(0)?.street.isNullOrEmpty()
                    || user?.addresses?.get(0)?.building_name.isNullOrEmpty()
                ) {
                    Notify.hyperlinkAlert(
                        activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2
                    )
                    return
                } else {
                    buyProduct?.first_name = firstName?.text.toString()
                    buyProduct?.last_name = lastName?.text.toString()
                    buyProduct?.email = email?.text.toString()
                    buyProduct?.phone_number = phoneNumber?.text.toString()
                    buyProduct?.map_location = location?.text.toString()
                    buyProduct?.location =
                        user?.addresses?.get(0)?.unit_number + ", " + user?.addresses?.get(0)?.building_name + ", " + user?.addresses?.get(
                            0
                        )?.street + ", " + user?.addresses?.get(0)?.district?.name + ", " + user?.addresses?.get(
                            0
                        )?.city?.name
                    buyProduct?.city_id =
                        user?.addresses?.get(0)?.city?.id
                    buyProduct?.district_id = user?.addresses?.get(0)?.district?.id
                    if (childToParent != null) {
                        childToParent?.callParent(buyProduct)
                    }
                }
            }
        } else {
            val authSuccessful: Boolean = AddressInfoAuthOrganization.authenticate(
                organizationName?.text.toString(),
                email?.text.toString(),
                phoneNumber?.text.toString(),
                "location",
                1,
                1,
                activity!!
            )
            if (authSuccessful) {
                if (user?.addresses.isNullOrEmpty()
                    || user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.street.isNullOrEmpty()
                    || user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.building_name.isNullOrEmpty()
                ) {
                    Notify.hyperlinkAlert(
                        activity,
                        getString(R.string.update_profile_msg),
                        getString(R.string.update_profile_heading),
                        this, 2
                    )
                    return
                } else {
                    buyProduct?.organization_name = organizationName?.text.toString()
                    buyProduct?.email = email?.text.toString()
                    buyProduct?.phone_number = phoneNumber?.text.toString()
                    buyProduct?.map_location = location?.text.toString()
                    buyProduct?.location =
                        user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.unit_number + ", " + user?.addresses?.get(
                            spinnerAddresses?.selectedItemPosition!!
                        )?.building_name + ", " +
                                user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.street + ", " + user?.addresses?.get(
                            spinnerAddresses?.selectedItemPosition!!
                        )?.district?.name + ", " + user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.city?.name
                    buyProduct?.city_id =
                        user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.city?.id
                    buyProduct?.district_id =
                        user?.addresses?.get(spinnerAddresses?.selectedItemPosition!!)?.district?.id
                    if (childToParent != null) {
                        childToParent?.callParent(buyProduct)
                    }
                }
            }
        }

    }

    private fun setAddressSpinnerSelection() {
        if (user?.addresses != null && user?.addresses!!.size > 0) {
            for (i in user?.addresses!!.indices) {
                if (user?.addresses!![i].id == defaultAddress?.id) {
                    spinnerAddresses?.setSelection(i)
                    buyProduct?.latitude = user?.addresses!![i].latitude
                    buyProduct?.longitude = user?.addresses!![i].longitude
                    break
                }
            }
        }
    }

    override fun callDialog(model: Any?) {
        if (model == 2) {
            HomeActivity.settingClicked = true
            Utils.clearAllFragmentStack(activity?.supportFragmentManager)
            HomeActivity.bottomNav.selectedItemId = R.id.navigation_settings
        }
    }
}
