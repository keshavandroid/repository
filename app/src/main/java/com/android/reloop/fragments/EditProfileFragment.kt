package com.android.reloop.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterAddress
import com.reloop.reloop.adapters.AdapterSpinnerSimple
import com.reloop.reloop.auth.EditProfileHouseHoldAuth
import com.reloop.reloop.auth.EditProfileOrganizationAuth
import com.reloop.reloop.fragments.AddAddressFragment
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.fragments.ChangePasswordFragment
import com.reloop.reloop.interfaces.*
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.network.serializer.DependencyDetail
import com.reloop.reloop.network.serializer.organization.Organization
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import com.theartofdev.edmodo.cropper.CropImage
import com.tsongkha.spinnerdatepicker.DatePicker
import com.tsongkha.spinnerdatepicker.DatePickerDialog
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Response

import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 */
class EditProfileFragment : BaseFragment(), View.OnClickListener, RecyclerViewItemClick,
    AddAddressFragment.OnChildFragmentInteractionListener, OnNetworkResponse, DeleteItem,
    AlertDialogCallback, NotifyCallback, PickerCallback, DatePickerDialog.OnDateSetListener {


    companion object {
        fun newInstance(): EditProfileFragment {
            return EditProfileFragment()
        }
    }

    private val TAG: String = "EditProfileFragment"
    var save: Button? = null
    var adapterAddress: AdapterAddress? = null
    var fragmentView: View? = null
    var imageUri: Uri? = null
    private var dependenciesListing: Dependencies? = Dependencies()
    var user: User? = null

    //    var updateProfile: UpdateProfile? = null
    var addressList: ArrayList<Addresses>? = null
    var deleteAddressPosition: Int = -1
    var organizationModel: Organization? = Organization()
    var showLoader = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        user = User.retrieveUser()
        addressList = user?.addresses
        initViews(fragmentView)
        setListeners(fragmentView)
        populateData(fragmentView)
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DEPENDENCIES)
            ?.autoLoading(requireActivity())
            ?.enque(
                Network().apis()?.dependencies()
            )
            ?.execute()

        return fragmentView
    }

    @SuppressLint("SetTextI18n")
    private fun initViews(view: View?) {
        when (user?.user_type!!) {
            Constants.UserType.household -> {
                view?.no_of_branches_heading?.visibility = View.GONE
                view?.no_of_branches?.visibility = View.GONE
                view?.sector_heading?.visibility = View.GONE
                view?.sector?.visibility = View.GONE
                view?.no_of_employees?.visibility = View.GONE
                view?.no_of_employees_heading?.visibility = View.GONE
                view?.organization?.visibility = View.VISIBLE
                view?.organization_heading?.visibility = View.VISIBLE
                view?.first_name?.setText(user?.first_name)
                view?.last_name?.setText(user?.last_name)
                view?.email?.setText(user?.email)
                view?.phone_number?.setText(user?.phone_number)
                view?.organization_id_heading?.visibility = View.VISIBLE
                view?.organization_id?.visibility = View.VISIBLE
                /*   if (user?.addresses != null && user?.addresses?.size!! > 0) {
                       view?.location?.setText(user?.addresses?.get(0)?.location)
                   }*/
                if (addressList?.size!! > 0) {
                    fragmentView?.add_new_address?.visibility = View.GONE
                    fragmentView?.layoutAddAddress?.visibility = View.GONE
                }
                view?.dob_heading?.visibility = View.VISIBLE
                view?.dob_edit?.visibility = View.VISIBLE
                view?.gender_heading?.visibility = View.VISIBLE
                view?.gender_group?.visibility = View.VISIBLE
            }
            Constants.UserType.organization -> {
                view?.first_name_heading?.text = "Organization Name*"
                view?.first_name?.hint = "Enter Organization"
                view?.last_name_heading?.visibility = View.GONE
                view?.last_name?.visibility = View.GONE
                view?.no_of_branches_heading?.visibility = View.VISIBLE
                view?.no_of_branches?.visibility = View.VISIBLE
                view?.sector_heading?.visibility = View.VISIBLE
                view?.sector?.visibility = View.VISIBLE
                view?.no_of_employees?.visibility = View.VISIBLE
                view?.no_of_employees_heading?.visibility = View.VISIBLE
                view?.organization?.visibility = View.GONE
                view?.organization_heading?.visibility = View.GONE
                view?.organization_id_heading?.visibility = View.GONE
                view?.cardViewOrg?.visibility = View.GONE
                view?.organization_id?.visibility = View.GONE
                view?.first_name?.setText(user?.organization?.name)
                view?.last_name?.visibility = View.GONE
                view?.email?.setText(user?.email)
                view?.dob_heading?.visibility = View.GONE
                view?.dob_edit?.visibility = View.GONE
                view?.gender_heading?.visibility = View.GONE
                view?.gender_group?.visibility = View.GONE
                view?.`deleteOrgID`?.visibility = View.GONE

                /*     if (user?.addresses != null && user?.addresses?.size!! > 0) {
                         view?.location?.setText(user?.addresses?.get(0)?.location)
                     }*/
                view?.phone_number?.setText(user?.phone_number)
                //--------Set No Of Employees Spinner----------------
                val noOfEmployeesAdapter = AdapterSpinnerSimple(
                    R.layout.spinner_item_textview_drawable,
                    Constants.getEmployeesNumberSelectList(),
                    activity?.getDrawable(R.drawable.icon_employees_un)!!,
                    false
                )
                view?.no_of_employees?.adapter = noOfEmployeesAdapter
                //--------Set No Of Branches Spinner----------------
                val noOfBranchesAdapter = AdapterSpinnerSimple(
                    R.layout.spinner_item_textview_drawable,
                    Constants.getBranchesNumberListSelectList(),
                    activity?.getDrawable(R.drawable.icon_branches_office_un)!!,
                    false
                )
                view?.no_of_branches?.adapter = noOfBranchesAdapter
                view?.no_of_branches?.setSelection(
                    Constants.getBranchesNumberListSelectList().indexOf(
                        user?.organization?.no_of_branches
                    )
                )
                view?.no_of_employees?.setSelection(
                    Constants.getEmployeesNumberSelectList().indexOf(
                        user?.organization?.no_of_employees
                    )
                )
            }
        }
        if (user?.user_type == Constants.UserType.organization) {
            view?.first_name_heading?.text = Utils.colorMyText(
                view?.first_name_heading?.text.toString(), 17, 18,
                Color.RED
            )
        } else
            view?.first_name_heading?.text = Utils.colorMyText(
                view?.first_name_heading?.text.toString(), 10, 11,
                Color.RED
            )
        view?.last_name_heading?.text = Utils.colorMyText(
            view?.last_name_heading?.text.toString(), 9, 10,
            Color.RED
        )
        view?.email_heading?.text = Utils.colorMyText(
            view?.email_heading?.text.toString(), 5, 6,
            Color.RED
        )
        view?.mobile_number_heading?.text = Utils.colorMyText(
            view?.mobile_number_heading?.text.toString(), 13, 14,
            Color.RED
        )
        view?.no_of_employees_heading?.text = Utils.colorMyText(
            view?.no_of_employees_heading?.text.toString(), 16, 17,
            Color.RED
        )
        view?.no_of_branches_heading?.text = Utils.colorMyText(
            view?.no_of_branches_heading?.text.toString(), 15, 16,
            Color.RED
        )
        view?.dob_heading?.text = Utils.colorMyText(
            view?.dob_heading?.text.toString(), 13, 14,
            Color.RED
        )
        view?.gender_heading?.text = Utils.colorMyText(
            view?.gender_heading?.text.toString(), 6, 7,
            Color.RED
        )
        view?.sector_heading?.text = Utils.colorMyText(
            view?.sector_heading?.text.toString(), 6, 7,
            Color.RED
        )
        view?.addressLabel?.text = Utils.colorMyText(
            view?.addressLabel?.text.toString(), 7, 8,
            Color.RED
        )

    }

    private fun setListeners(view: View?) {
        view?.save?.setOnClickListener(this)
        view?.add_new_address?.setOnClickListener(this)
        view?.layoutAddAddress?.setOnClickListener(this)
        view?.change_password?.setOnClickListener(this)
        view?.img_edit_profile?.setOnClickListener(this)
//        view?.location?.setOnClickListener(this)
        view?.cancel?.setOnClickListener(this)
        view?.organization_id?.setOnClickListener(this)
        view?.dob_edit?.setOnClickListener(this)
        view?.deleteOrgID?.setOnClickListener(this)
    }

    private fun populateData(view: View?) {

//        view?.phone_number?.setRawInputType(Configuration.KEYBOARD_);
        //--------------Set AddressList RV--------
        if (addressList == null) {
            addressList = ArrayList()
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        view?.recyclerView?.layoutManager = linearLayoutManager
        adapterAddress = AdapterAddress(addressList, this, this, dependenciesListing)
        view?.recyclerView?.adapter = adapterAddress
        organizationModel = user?.organization
        if (!user?.birth_date.isNullOrEmpty()) {
            view?.dob_edit?.setText(Utils.getFormattedDisplayDateCollection(user?.birth_date))
        }
        if (user?.gender.equals("Male", true)) {
            view?.male?.isChecked = true
        } else if (user?.gender.equals("Female", true)) {
            view?.female?.isChecked = true
        }
        //------------------------------------------
        if (user?.avatar != null && user?.avatar!!.isNotEmpty()) {
            Utils.glideImageLoaderServer(
                view?.img_profile,
                user?.avatar,
                R.drawable.icon_placeholder_profile
            )
        }
        view?.phone_number?.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (fragmentView?.phone_number?.text!!.length < 7) {
                    fragmentView?.phone_number?.setText("+971-5")
                    fragmentView?.phone_number?.setSelection(fragmentView?.phone_number!!.text!!.length)
                }
            }
        }
        if (!user?.hh_organization_name.isNullOrEmpty()) {
            fragmentView?.organization?.setText(user?.hh_organization_name)
        }

        view?.phone_number?.addTextChangedListener(object : TextWatcher {
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
                    fragmentView?.phone_number?.setText("+971-5")
                    fragmentView?.phone_number?.setSelection(fragmentView?.phone_number!!.text!!.length)
                } else if (s.length == 7 && count > before) {
                    fragmentView?.phone_number?.setText(fragmentView?.phone_number!!.text.toString() + "-")
                    fragmentView?.phone_number?.setSelection(fragmentView?.phone_number!!.text!!.length)
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.save -> {
                saveProfile(true)
            }
            R.id.add_new_address -> {
                if (dependenciesListing != null) {
                    val fragment = AddAddressFragment.newInstance(dependenciesListing)
                    val args = Bundle()
                    args.putInt(
                        Constants.DataConstants.userStatus,
                        user?.user_type!!
                    )
                    fragment.arguments = args
                    BaseActivity.replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerEditProfile,
                        fragment,
                        Constants.TAGS.AddAddressFragment
                    )
                } else {
                    Notify.alerterRed(activity, "Cannot Add New Address")
                }
            }
            R.id.layoutAddAddress -> {
                if (dependenciesListing != null) {
                    val fragment = AddAddressFragment.newInstance(dependenciesListing)
                    val args = Bundle()
                    args.putInt(
                        Constants.DataConstants.userStatus,
                        user?.user_type!!
                    )
                    fragment.arguments = args
                    BaseActivity.replaceFragment(
                        childFragmentManager,
                        Constants.Containers.containerEditProfile,
                        fragment,
                        Constants.TAGS.AddAddressFragment
                    )
                } else {
                    Notify.alerterRed(activity, "Cannot Add New Address")
                }
            }
            R.id.change_password -> {
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.containerEditProfile,
                    ChangePasswordFragment.newInstance(),
                    Constants.TAGS.ChangePasswordFragment
                )
            }
            R.id.img_edit_profile -> {
                storageTask()
            }
            R.id.cancel -> {
                activity?.onBackPressed()
            }
            R.id.dob_edit -> {
//                Utils.showDatePickerWithLimit(activity, this, true, false)
                showSpinnerDatePicker()
            }
            R.id.organization_id -> {
                AlertDialogs.alertDialogInput(
                    activity,
                    this
                )
            }
            R.id.deleteOrgID -> {
                AlertDialogs.deleteOrgIDPopup(
                    activity,
                    this,
                    "Are you sure you want to delete Organization ID " + fragmentView?.organization_id?.text.toString() + " ?"
                )
            }
        }
    }

    private fun showSpinnerDatePicker() {
        try {
            val calendar = Calendar.getInstance()
            val currentTimeMillis = System.currentTimeMillis()
            calendar.timeInMillis = currentTimeMillis
            val mYear: Int = calendar.get(Calendar.YEAR)
            val mMonth: Int = calendar.get(Calendar.MONTH)
            val mDay: Int = calendar.get(Calendar.DAY_OF_MONTH)

            val calPreviousDate = Calendar.getInstance()
            var previousYear = 0
            var previousMonth = 0
            var previousDay = 0
            if (!dob_edit.text.isNullOrEmpty()) {
                calPreviousDate.time = getDateFromString(dob_edit.text.toString())!!
                previousYear = calPreviousDate.get(Calendar.YEAR)
                previousMonth = calPreviousDate.get(Calendar.MONTH)
                previousDay = calPreviousDate.get(Calendar.DAY_OF_MONTH)
            } else {
                previousYear = 2000
                previousMonth = 0
                previousDay = 1
            }
            SpinnerDatePickerDialogBuilder()
                .context(requireContext())
                .showTitle(true)
                .showDaySpinner(true)
                .callback(this)
                .defaultDate(previousYear, previousMonth, previousDay)
                .maxDate(mYear, mMonth, mDay)
                .minDate(0, 0, 0)
                .build()
                .show()

        } catch (e: Exception) {
            Log.d(TAG, "showSpinnerDatePicker: ${e.message}")
        }
    }

    fun getDateFromString(date: String?): Date? {
        val dateFormat = arrayListOf("MMM dd, yyyy")

        var dt: Date? = null
        if (date != null) {
            for (sdf in dateFormat) {
                try {
                    dt = Date(SimpleDateFormat(sdf).parse(date).time)
                    break
                } catch (pe: ParseException) {
                    pe.printStackTrace()
                }
            }
        }
        return dt
    }

    override fun deleteItem(position: Int) {
        val address: Addresses? = addressList?.find { it.default == 1 }
        if (addressList?.get(position)?.id == address?.id) {
            Notify.alerterRed(activity, "Cannot Delete Default Address")
        } else {
            AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.deleted_this_address))
            deleteAddressPosition = position
        }
    }

    override fun itemPosition(position: Int) {
        if (dependenciesListing != null && addressList?.get(position) != null) {
            val fragment =
                AddAddressFragment.newInstance(addressList?.get(position), dependenciesListing)
            val args = Bundle()
            args.putInt(
                Constants.DataConstants.userStatus,
                user?.user_type!!
            )
            fragment.arguments = args
            BaseActivity.replaceFragment(
                childFragmentManager,
                Constants.Containers.containerEditProfile,
                fragment,
                Constants.TAGS.AddAddressFragment
            )
        } else {
            Notify.alerterRed(activity, "Cannot Add New Address")
        }
    }

    override fun messageFromChildToParent() {
        saveProfile(false)
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_PROFILE)
            ?.autoLoading(requireActivity())
            ?.enque(
                Network().apis()?.getUserProfile()
            )
            ?.execute()
    }

    private fun storageTask() {
        if (hasStoragePermission()) {
            CropImage.activity()
                .start(requireActivity(), this)
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.write_external_storage),
                RequestCodes.RC_STORAGE_PERM,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (result != null && result.uri != null) {
                imageUri = result.uri
                Utils.glideImageLoaderUriLocal(requireActivity(), fragmentView?.img_profile!!, imageUri!!)
            }
            if (resultCode == RESULT_OK) {
                if (result != null && result.uri != null) {
                    imageUri = result.uri
                    Utils.glideImageLoaderUriLocal(
                        requireActivity(),
                        fragmentView?.img_profile!!,
                        imageUri!!
                    )
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Log.e(Constants.TAGS.EditProfileFragment, "$error")
            }
        }
//        else if (resultCode == Constants.mapsCode) {
//            val address: String? = data?.getStringExtra(Constants.DataConstants.location)
//            fragmentView?.location?.setText(address)
//        }

    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.DEPENDENCIES -> {
                val dependencyDetail = DependencyDetail()

                try {
                    dependenciesListing = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dependencies::class.java
                    )
                } catch (e: Exception) {
                    Log.e("Edit Profile", e.toString())
                    activity?.onBackPressed()
                }

                if (dependenciesListing?.organizations.isNullOrEmpty()) {
                    dependenciesListing?.organizations = ArrayList()
                }
                dependencyDetail.name = "Select Organization"
                dependencyDetail.id = 0
                dependenciesListing?.organizations?.add(0, dependencyDetail)
                populateSpinnerData(dependenciesListing?.sectors)
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.GET_PROFILE)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.getUserProfile())
                    ?.execute()
            }
            RequestCodes.API.UPDATE_PROFILE -> {
                if (showLoader)
                    Notify.alerterGreen(activity, baseResponse?.message)
                // your json value here
                val userModel = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    User::class.java
                )
                userModel.save(userModel, context)
                //Set Delete Orgnization Id Button
                if (!userModel?.organization?.org_external_id.isNullOrEmpty() && userModel.user_type == Constants.UserType.household) {
                    fragmentView?.organization_id?.setText(userModel?.organization?.name)
                    fragmentView?.deleteOrgID?.visibility = View.VISIBLE
                } else {
                    fragmentView?.deleteOrgID?.visibility = View.GONE
                }
            }
            RequestCodes.API.GET_PROFILE -> {
                addressList?.clear()
                if (addressList == null) {
                    addressList = ArrayList()
                }
                adapterAddress?.notify(addressList, dependenciesListing)
                try {
                    val userModel = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        User::class.java
                    )
                    if (userModel.user_type == 0 || userModel.user_type == null) {
                        userModel?.user_type = Constants.UserType.household
                    }
                    if (!userModel.api_token.isNullOrEmpty()) {
                        userModel.save(userModel, context)
                        user = userModel
                    }
                    if (!userModel?.organization?.org_external_id.isNullOrEmpty() && userModel.user_type == Constants.UserType.household) {
                        fragmentView?.organization_id?.setText(userModel?.organization?.name)
                        fragmentView?.deleteOrgID?.visibility = View.VISIBLE
                    } else {
                        fragmentView?.deleteOrgID?.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("Edit Profile", e.toString())
                    activity?.onBackPressed()
                }

                addressList = user?.addresses
                if (addressList == null) {
                    addressList = ArrayList()
                }
//                if (user?.addresses != null && user?.addresses?.size!! > 0) {
//                    fragmentView?.location?.setText(user?.addresses?.get(0)?.location)
//                }
                adapterAddress?.notify(addressList, dependenciesListing)
                when (user?.user_type) {
                    Constants.UserType.household -> {
                        if (addressList?.size!! > 0) {
                            fragmentView?.add_new_address?.visibility = View.GONE
                            fragmentView?.layoutAddAddress?.visibility = View.GONE

                        }
                    }
                }
            }
            RequestCodes.API.DELETE_ADDRESS -> {
                if (deleteAddressPosition >= 0) {
                    addressList?.removeAt(deleteAddressPosition)
                    adapterAddress?.notify(addressList, dependenciesListing)
                    user?.addresses = addressList
                    if (user == null) {
                        user = User()
                    }
                    if (user?.addresses.isNullOrEmpty()) {
                        user?.addresses = ArrayList()
                    }
                    user?.save(user!!, context)
                }
            }
            RequestCodes.API.ORGANIZATION_CODE -> {
                organizationModel = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    Organization::class.java
                )
                AlertDialogs.alertDialogVerifyOrganization(
                    activity,
                    this,
                    "Are you part of " + organizationModel?.name + " ?"
                )
//                Notify.alerterOrganizationConfirmation(
//                    activity,
//                    ,
//                    this
//                )
            }
            RequestCodes.API.REMOVE_ORGANIZATION -> {
                Notify.alerterGreen(activity, baseResponse?.message)
                fragmentView?.organization_id?.setText("")
                fragmentView?.deleteOrgID?.visibility = View.GONE

            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    private fun populateSpinnerData(
        sectors: ArrayList<DependencyDetail>?
    ) {
        fragmentView?.sector?.setTitle("")
        fragmentView?.sector?.setPositiveButton("")
        //-------------------Sector Spinner---------------------
        if (!sectors.isNullOrEmpty()) {

            val names: ArrayList<String> = ArrayList()
            names.clear()
            for (i in sectors.indices) {
                names.add(sectors[i].name.toString())
            }
            val sectorAdapter = AdapterSpinnerSimple(
                R.layout.spinner_item_textview_drawable,
                names,
                activity?.getDrawable(R.drawable.icon_sector_un)!!, true
            )
            fragmentView?.sector?.adapter = sectorAdapter
            for (i in sectors.indices) {
                if (sectors[i].id == user?.organization?.sector_id) {
                    fragmentView?.sector?.setSelection(i)
                    break
                }
            }
        }
    }

    private fun saveProfile(showLoader: Boolean) {
        this.showLoader = showLoader
        if (user?.user_type == Constants.UserType.household) {
            val authSuccessful: Boolean = EditProfileHouseHoldAuth.authenticate(
                first_name?.text.toString(),
                last_name?.text.toString(),
                email?.text?.toString(),
                phone_number?.text.toString(),
                requireActivity()
            )
            if (authSuccessful) {
                if (user?.addresses.isNullOrEmpty()
                    || user?.addresses?.get(0)?.street.isNullOrEmpty()
                    || user?.addresses?.get(0)?.building_name.isNullOrEmpty()
                    || user?.addresses?.get(0)?.type.isNullOrEmpty()
                    || dob_edit?.text.isNullOrEmpty()
                    || (!male.isChecked && !female.isChecked)
                ) {
                    Notify.alerterRed(activity, "Your profile is incomplete")
                } else {
                    editProfileHouseHold(showLoader)
                }
            }

        } else if (user?.user_type == Constants.UserType.organization) {
            var selectedItemPos = sector?.selectedItemPosition
            if (selectedItemPos == null) {
                selectedItemPos = -1
            }
            val authSuccessful: Boolean = EditProfileOrganizationAuth.authenticate(
                first_name?.text.toString(),
                email?.text?.toString(),
                phone_number?.text.toString(),
                no_of_employees?.selectedItemPosition,
                no_of_branches?.selectedItemPosition,
                selectedItemPos,
                requireActivity()
            )
            if (authSuccessful) {
                if (user?.addresses.isNullOrEmpty()
                    || user?.addresses?.get(0)?.street.isNullOrEmpty()
                    || user?.addresses?.get(0)?.building_name.isNullOrEmpty()
                    || user?.addresses?.get(0)?.type.isNullOrEmpty()
                ) {
                    Notify.alerterRed(activity, "Your profile is incomplete")
                } else {
                    editProfileOrganization(showLoader)
                }
            }
        }
    }

    private fun editProfileOrganization(showLoader: Boolean) {
        val hashMap: HashMap<String?, RequestBody?>? = HashMap()
        hashMap?.put(
            "type", RequestBody.create(
                "text/plain".toMediaTypeOrNull(), user?.user_type.toString()
            )
        )
        hashMap?.put(
            "no_of_employees",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                Constants.getEmployeesNumberSelectList()[fragmentView?.no_of_employees?.selectedItemPosition!!]
            )
        )
        hashMap?.put(
            "no_of_branches",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                Constants.getBranchesNumberListSelectList()[fragmentView?.no_of_branches?.selectedItemPosition!!]
            )
        )
        hashMap?.put(
            "phone_number", RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                fragmentView?.phone_number?.text.toString()
            )
        )
//        hashMap?.put(
//            "location", RequestBody.create(
//                MediaType.parse("text/plain"), fragmentView?.location?.text.toString()
//            )
//        )
        hashMap?.put(
            "email", RequestBody.create(
                "text/plain".toMediaTypeOrNull(), fragmentView?.email?.text.toString()
            )
        )
        hashMap?.put(
            "sector_id",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                dependenciesListing?.sectors?.get(fragmentView?.sector?.selectedItemPosition!!)?.id.toString()
            )
        )
        hashMap?.put(
            "organization_name", RequestBody.create(
                "text/plain".toMediaTypeOrNull(), fragmentView?.first_name?.text.toString()
            )
        )
        if (showLoader) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.UPDATE_PROFILE)
                ?.autoLoading(requireActivity())
                ?.enque(
                    Network().apis()?.updateUserProfile(
                        Utils.multiPartImageFile(imageUri), hashMap
                    )
                )
                ?.execute()
        } else {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.UPDATE_PROFILE)
                ?.enque(
                    Network().apis()?.updateUserProfile(
                        Utils.multiPartImageFile(imageUri), hashMap
                    )
                )
                ?.execute()
        }
    }

    private fun editProfileHouseHold(showLoader: Boolean) {
        val hashMap: HashMap<String?, RequestBody?>? = HashMap()
//                    hashMap?.put("profile_pic", Utils.imageRequestBody(imageUri))
        hashMap?.put(
            "type", RequestBody.create(
                "text/plain".toMediaTypeOrNull(), user?.user_type.toString()
            )
        )
        /* hashMap?.put(
             "location", RequestBody.create(
                 MediaType.parse("text/plain"), fragmentView?.location?.text.toString()
             )
         )*/
        hashMap?.put(
            "phone_number", RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                fragmentView?.phone_number?.text.toString()
            )
        )
        hashMap?.put(
            "email", RequestBody.create(
                "text/plain".toMediaTypeOrNull(), fragmentView?.email?.text.toString()
            )
        )
        hashMap?.put(
            "organization_id",
            RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                organizationModel?.id.toString()
            )
        )
        hashMap?.put(
            "first_name", RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                fragmentView?.first_name?.text.toString().trim()
            )
        )
        hashMap?.put(
            "last_name", RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                fragmentView?.last_name?.text.toString().trim()
            )
        )
        if (fragmentView?.dob_edit?.text.toString().isNotEmpty()) {
            hashMap?.put(
                "birth_date", RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    Utils.getSingleDateServer(fragmentView?.dob_edit?.text.toString().trim())!!
                )
            )
        }
        if (male.isChecked) {
            hashMap?.put(
                "gender", RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    "Male"
                )
            )
        } else {
            hashMap?.put(
                "gender", RequestBody.create(
                    "text/plain".toMediaTypeOrNull(),
                    "Female"
                )
            )
        }

        try {
            if (fragmentView?.organization?.text.toString().isNotEmpty()) {
                hashMap?.put(
                    "hh_organization_name", RequestBody.create(
                        "text/plain".toMediaTypeOrNull(),
                        fragmentView?.organization?.text.toString()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (showLoader) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.autoLoading(requireActivity())
                ?.setTag(RequestCodes.API.UPDATE_PROFILE)
                ?.enque(Network().apis()?.updateUserProfile(Utils.multiPartImageFile(imageUri), hashMap))
                ?.execute()
        } else {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.UPDATE_PROFILE)
                ?.enque(Network().apis()?.updateUserProfile(Utils.multiPartImageFile(imageUri), hashMap))
                ?.execute()
        }
    }

    override fun callDialog(model: Any?) {

        if (model == null) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.DELETE_ADDRESS)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.deleteAddress(addressList?.get(deleteAddressPosition)?.id))
                ?.execute()
        } else {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.ORGANIZATION_CODE)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.organizationVerification(model as String))
                ?.execute()
        }
    }

    override fun callNotify(model: Any?) {
        if (model == Constants.userPartOfOrganization) {
            Notify.alerterGreen(activity, "Organization ID Successfully Verified!")
            fragmentView?.organization_id?.setText(organizationModel?.org_external_id)
        }
        if (model == Constants.deleteOrganizationId) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.autoLoading(requireActivity())
                ?.setTag(RequestCodes.API.REMOVE_ORGANIZATION)
                ?.enque(
                    Network().apis()?.removeOrganization()
                )
                ?.execute()
        }
    }

    override fun onSelected(date: Any?) {
        dob_edit?.setText(date.toString())
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar: Calendar = GregorianCalendar(year, monthOfYear, dayOfMonth)
        val simpleDateFormat = SimpleDateFormat(Utils.dateFormatDisplay)
        dob_edit?.setText(simpleDateFormat.format(calendar.time))
    }

}
