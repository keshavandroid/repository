package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.activities.MapsActivity
import com.reloop.reloop.adapters.viewholders.AdapterSpinnerAddress
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.network.serializer.collectionrequest.CollectionRequest
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.orderhistory.DistrictWithOrder
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import retrofit2.Call
import retrofit2.Response
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ConfirmCollectionFragment : BaseFragment(), ParentToChild, OnNetworkResponse,
    View.OnClickListener {


    override fun onStart() {
        super.onStart()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    var childToParent: ChildToParent? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ChildToParent) {
            childToParent = parentFragment as? ChildToParent?
        } else {
            Notify.Toast("Child To Parent Callback Not Implemented")
        }
    }

    companion object {
        var selectedDateAvailable = true
        var collectionRequest: CollectionRequest? =
            CollectionRequest()
        var materialCategories: ArrayList<MaterialCategories>? = ArrayList()
        fun newInstance(
            materialCategories: ArrayList<MaterialCategories>?,
            collectionRequest: CollectionRequest?
        ): ConfirmCollectionFragment {
            this.collectionRequest = collectionRequest
            this.materialCategories = materialCategories
            return ConfirmCollectionFragment()
        }
    }

    private var dependenciesListing: Dependencies? = null

    var spinnerAddresses: Spinner? = null
    var term_condition_check: CheckBox? = null
    var name: TextView? = null
    var category: TextView? = null
    var location: CustomEditText? = null
    var schedule_date: TextView? = null
    var phoneNumber: TextView? = null
    var nameHeading: TextView? = null
    var term_condition: TextView? = null
    val user = User.retrieveUser()
    var address_header: TextView? = null

    //    var question1: TextView? = null
//    var question2: TextView? = null
//    var groupQuestion1: RadioGroup? = null
//    var groupQuestion2: RadioGroup? = null
//    var question1Id: Int? = 0
//    var question2Id: Int? = 0
//    var question1Answer = "Yes"
//    var question2Answer = "Yes"
    var defaultAddress: Addresses? = null
    var scrollView: ScrollView? = null
    var positionAdress = 0
    var comments: CustomEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_confirm_collection, container, false)
        if (RecycleFragment.stepView != null) {
            RecycleFragment.stepView!!.StepNumber(Constants.recycleStep3)
        }
        RecycleFragment.parentToChild = this
        initViews(view)
        setListeners()
        populateData(view)
        return view
    }

    private fun initViews(view: View?) {
        name = view?.findViewById(R.id.name)
        category = view?.findViewById(R.id.category)
        location = view?.findViewById(R.id.location)
        schedule_date = view?.findViewById(R.id.schedule_date)
        phoneNumber = view?.findViewById(R.id.phone_number)
        nameHeading = view?.findViewById(R.id.name_heading)
        spinnerAddresses = view?.findViewById(R.id.address_organization)
        scrollView = view?.findViewById(R.id.scrollView)
        comments = view?.findViewById(R.id.comments)
        term_condition_check = view?.findViewById(R.id.term_condition_check)
        term_condition = view?.findViewById(R.id.term_condition)
        address_header = view?.findViewById(R.id.address_header)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        comments?.setOnTouchListener(OnTouchListener { v, event ->
            if (comments?.hasFocus()!!) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_SCROLL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        return@OnTouchListener true
                    }
                }
            }
            false
        })
        location?.setOnClickListener(this)
        term_condition?.setOnClickListener(this)

    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun populateData(view: View?) {
        Utils.setUnderLineText(term_condition, term_condition?.text.toString())
        if (!user?.addresses.isNullOrEmpty()) {
            defaultAddress = user?.addresses?.find { it.default == 1 }
            if (defaultAddress == null) {
                defaultAddress = user?.addresses?.find { it.default == 0 }
            }
        }
        phoneNumber?.text = user?.phone_number
        name?.text = "${user?.first_name} ${user?.last_name}"
        //-------------------Addresses  Spinner---------------------
        val hideArrow = user?.user_type != Constants.UserType.organization
        val address = AdapterSpinnerAddress(
            R.layout.spinner_item_textview_drawable,
            user?.addresses,
            activity?.getDrawable(R.drawable.icon_address_location_un)!!, hideArrow
        )
        spinnerAddresses?.adapter = address
        spinnerAddresses?.visibility = View.VISIBLE
        address_header?.visibility = View.VISIBLE

        if (user?.user_type == Constants.UserType.organization) {
            nameHeading?.text = "Organization Name"
            name?.text = "${user.organization?.name}"
            if (RecycleFragment.changeAddressDistrictID == 0) {
                if (!user.addresses.isNullOrEmpty()) {
                    for (i in user.addresses?.indices!!) {
                        if (user.addresses!![i].default == 1) {
                            spinnerAddresses?.setSelection(i)
                            break
                        }
                    }
                }
            } else {
                if (!user.addresses.isNullOrEmpty()) {
                    for (i in user.addresses?.indices!!) {
                        if (user.addresses!![i].district_id == RecycleFragment.changeAddressDistrictID) {
                            spinnerAddresses?.setSelection(i)
                            break
                        }
                    }
                }
            }
            val callback: OnNetworkResponse = this
            spinnerAddresses?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    positionAdress = position
                    location?.setText(user.addresses?.get(position)?.location)
//                    location?.text = user.addresses?.get(position)?.location
                    NetworkCall.make()
                        ?.setCallback(callback)
                        ?.setTag(RequestCodes.API.ORDER_ACCEPTANCE_DAYS)
                        ?.autoLoading(activity!!)
                        ?.enque(
                            Network().apis()
                                ?.orderAcceptanceDays(user.addresses?.get(position)?.district_id)
                        )
                        ?.execute()
                }

            }
        } else {
//            spinnerAddresses?.background?.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            spinnerAddresses?.isClickable = false
            spinnerAddresses?.isEnabled = false
            location?.setText(defaultAddress?.location)
        }
        val categoryName = StringBuilder()
        val categoryDescription = StringBuilder()
        var prefix = ""
        for (i in 0 until collectionRequest?.material_categories?.size!!) {
            for (j in materialCategories!!.indices) {
                if (materialCategories?.get(j)?.id == collectionRequest?.material_categories!![i]?.id) {
                    categoryName.append(prefix)
                    categoryDescription.append(prefix)
                    categoryName.append(SelectCategoriesFragment.materialCategories?.get(j)?.name)
                    categoryDescription.append(SelectCategoriesFragment.materialCategories?.get(j)?.description)
                    prefix = ","
                    break
                }
            }
        }
        category?.text = categoryName.toString()
        schedule_date?.text =
            Utils.getFormattedDisplayDateCollection(collectionRequest?.collection_date)
    }


    override fun callChild() {
        if (selectedDateAvailable) {
            if (term_condition_check?.isChecked!!) {
                if (user?.user_type == Constants.UserType.organization) {
                    collectionRequest?.city_id = user.addresses?.get(positionAdress)?.city_id
                    collectionRequest?.district_id = user.addresses?.get(positionAdress)?.district_id
                    collectionRequest?.latitude = user.addresses?.get(positionAdress)?.latitude
                    collectionRequest?.longitude = user.addresses?.get(positionAdress)?.longitude
                    collectionRequest?.map_location = location?.text.toString()
                    collectionRequest?.location =
                        user.addresses?.get(positionAdress)?.unit_number + ", " + user.addresses?.get(positionAdress)?.building_name + ", " +
                                user.addresses?.get(positionAdress)?.street + ", " + user.addresses?.get(positionAdress)?.district?.name + ", " + user.addresses?.get(positionAdress)?.city?.name
                } else {
                    collectionRequest?.city_id = defaultAddress?.city_id
                    collectionRequest?.district_id = defaultAddress?.district_id
                    collectionRequest?.latitude = defaultAddress?.latitude
                    collectionRequest?.longitude = defaultAddress?.longitude
                    collectionRequest?.map_location = location?.text.toString()
                    collectionRequest?.location = defaultAddress?.unit_number + ", " + defaultAddress?.building_name + ", " +
                         defaultAddress?.street + ", " + defaultAddress?.district?.name + ", " + defaultAddress?.city?.name
                }
                collectionRequest?.street = user?.addresses?.get(positionAdress)?.street
                collectionRequest?.first_name = user?.first_name
                collectionRequest?.last_name = user?.last_name
                collectionRequest?.organization_name = user?.organization?.name
                collectionRequest?.phone_number = user?.phone_number
                collectionRequest?.user_comments = comments?.text.toString()
                if (childToParent != null) {
                    childToParent?.callParent(collectionRequest)
                }
            } else {
                Notify.alerterRed(activity, "Please Agree To Term and Conditions")
            }
        } else {
            Notify.alerterRed(activity, "Please Go Back And Select Date Again")
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.DEPENDENCIES -> {
                val baseResponseNew = Gson().fromJson(
                    Utils.jsonConverterObject(response.body() as LinkedTreeMap<*, *>),
                    BaseResponse::class.java
                )
                dependenciesListing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponseNew.data as LinkedTreeMap<*, *>),
                    Dependencies::class.java
                )
            }
            RequestCodes.API.ORDER_ACCEPTANCE_DAYS -> {
                val districtDetail = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    DistrictWithOrder::class.java
                )
                val collectionDay = Utils.getDayFromDate(collectionRequest?.collection_date)
                if (!districtDetail?.order_acceptance_days.isNullOrEmpty()) {
                    for (i in districtDetail?.order_acceptance_days!!.indices) {
                        if (collectionDay?.equals(districtDetail.order_acceptance_days[i], true)!!
                            || districtDetail.order_acceptance_days[i].equals("All", true))
                        {
                            selectedDateAvailable = true
                            break
                        } else {
                            selectedDateAvailable = false
                        }
                    }
                }
                if (!selectedDateAvailable) {
                    Notify.alerterRed(activity, "Please Go Back And Select Date Again")
                }
                RecycleFragment.changeAddressDistrictID = user?.addresses?.get(positionAdress)?.district_id
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.location -> {
                val intent = Intent(activity, MapsActivity::class.java)
                val bundle = Bundle()
                if (user?.user_type == Constants.UserType.organization) {
                    bundle.putDouble(Constants.DataConstants.latitude, user.addresses?.get(positionAdress)?.latitude!!)
                    bundle.putDouble(Constants.DataConstants.longitude, user.addresses?.get(positionAdress)?.longitude!!)
                } else {
                    bundle.putDouble(Constants.DataConstants.latitude, defaultAddress?.latitude!!)
                    bundle.putDouble(Constants.DataConstants.longitude, defaultAddress?.longitude!!)
                }
                bundle.putInt(Constants.DataConstants.removeSaveButton, 1)
                intent.putExtra(Constants.DataConstants.bundle, bundle)
                startActivity(intent)
            }
            R.id.term_condition -> {
                BaseActivity.replaceFragment(
                    activity?.supportFragmentManager!!,
                    Constants.Containers.recycleRequestParent,
                    TermConditionFragment.newInstance(),
                    Constants.TAGS.TermConditionFragment)
                HomeActivity.openTermCondition = true
            }
        }
    }
}
