package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.interfaces.SupportClick
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterOrderHistoryNew
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequestProduct
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.OrderHistory
import com.reloop.reloop.network.serializer.orderhistory.UserOrders
import com.reloop.reloop.network.serializer.organization.Organization
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_subscription.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class OrderHistoryFragment : BaseFragment(), View.OnClickListener, RecyclerViewItemClick,
    OnNetworkResponse, ParentToChild, SupportClick, AlertDialogCallback {

    private var iscollectionBinAvaialble: Boolean? = false
    val sumList = ArrayList<Double>()
    private var addressTextName: String = ""

    private lateinit var dialog: Dialog

    var back: Button? = null

    companion object {
        fun newInstance(): OrderHistoryFragment {
            return OrderHistoryFragment()
        }

        var TAG = "OrderHistoryFragment"

    }

    var linearLayoutManager: LinearLayoutManager? = null
    var recyclerView: RecyclerView? = null
    var tvNoOrders: TextView? = null
    var orderList: OrderHistory? = null

    var imgExportReport: ImageView? = null
    var imgExportReportbins: ImageView? = null
    var llroot: LinearLayout? = null

    var combineList: ArrayList<Any>? = ArrayList()

    var userOrders: ArrayList<UserOrders>? = ArrayList()
    var userCollectionRequests: ArrayList<CollectionRequests>? = ArrayList()

    var lldownloads: LinearLayout? = null
    var op1: TextView? = null
    var op2: TextView? = null
    var op3: TextView? = null

    //FILTER
    var organizationFilter: Spinner? = null
    var is_master_organization = false
    var listConnectedOrgs: ArrayList<Organization> = ArrayList()
    private var selectedFilterName: String = ""
    var filter_option: Int? = null
    var address_id: Int? = null
    var txtClearFilterReport: TextView? = null
    var currentFilter = -1
    val user = User.retrieveUser()
    var isScreenOpened = false
    var dropDowns: LinearLayout? = null

    var preLoadImages: MutableList<String>? = mutableListOf()

    private var scrolledItems = 0
    private val batchSize = 10
    private var isLoading = false
    var useraddress: ArrayList<Addresses> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_order_history, container, false)

        if (user != null && user.organization != null) {
            if (user.organization!!.is_master == 1) {
                is_master_organization = true
            } else {
                is_master_organization = false
            }
        }
        initViews(view)
        hideShowViews()
        setListeners()
//        populateData()
        populateDataFirstTime()

        return view
    }

    private fun hideShowViews() {
        if (MainApplication.userType() == Constants.UserType.household) {
            dropDowns!!.visibility = View.GONE
            txtClearFilterReport!!.visibility = View.GONE
        } else {
            dropDowns!!.visibility = View.VISIBLE
            txtClearFilterReport!!.visibility = View.VISIBLE
        }
    }

    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        recyclerView = view?.findViewById(R.id.rvOrderHistory)
        tvNoOrders = view?.findViewById(R.id.tvNoOrders)
        llroot = view?.findViewById(R.id.llrootlay)

        imgExportReport = view?.findViewById(R.id.imgExportReport)
        imgExportReportbins = view?.findViewById(R.id.imgExportReportbins)

        organizationFilter = view?.findViewById(R.id.organization_filter)
        lldownloads = view?.findViewById(R.id.lldownsload)
        op1 = view?.findViewById(R.id.op1)
        op2 = view?.findViewById(R.id.op2)
        op3 = view?.findViewById(R.id.op3)

        txtClearFilterReport = view?.findViewById(R.id.txtClearFilterReport)
        dropDowns = view?.findViewById(R.id.dropDowns)


        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        val reportButtonHouseHold = tinyDB.getString("reports_button_household").toString()
        val reportButtonOrganization = tinyDB.getString("reports_button_organization").toString()

        if (MainApplication.userType() == Constants.UserType.household) {
            if (reportButtonHouseHold.equals("VISIBLE")) { //VISIBLE
                imgExportReport!!.visibility = View.VISIBLE
            } else {
                imgExportReport!!.visibility = View.GONE
            }

        } else {
            if (reportButtonOrganization.equals("VISIBLE")) { //VISIBLE
                imgExportReport!!.visibility = View.VISIBLE
            } else {
                imgExportReport!!.visibility = View.GONE
            }
        }
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        txtClearFilterReport?.setOnClickListener(this)
        llroot?.setOnClickListener(this)

        imgExportReport!!.setOnClickListener {
            if (iscollectionBinAvaialble == true) {
                lldownloads!!.visibility = View.VISIBLE
                op1!!.setOnClickListener {

                    lldownloads!!.visibility = View.GONE
                    exportReport() // without  bins
                }
                op2!!.setOnClickListener {

                    lldownloads!!.visibility = View.GONE
                    exportReportwithBins() //with bins
                }
                op3!!.setOnClickListener {

                    lldownloads!!.visibility = View.GONE
                }

            } else {
                lldownloads!!.visibility = View.GONE
                exportReport() // without  bins

            }
        }
        imgExportReportbins!!.setOnClickListener {
            exportReportwithBins() //with bins
        }
    }

    private fun populateDataFirstTime() {
        showDialog()
        if (MainApplication.userType() == Constants.UserType.organization) {
            if (is_master_organization) {
                NetworkCall.make()?.setCallback(this)?.setTag(RequestCodes.API.ORDER_LISTING)
                    //?.autoLoading(requireActivity())
                    ?.enque(
                        Network().apis()?.ordersListingFilter(6, null)
                    )?.execute()
            } else {
                NetworkCall.make()?.setCallback(this)?.setTag(RequestCodes.API.ORDER_LISTING)
                    //?.autoLoading(requireActivity())
                    ?.enque(
                        Network().apis()?.ordersListing()
                    )?.execute()
            }
        } else {
            NetworkCall.make()?.setCallback(this)?.setTag(RequestCodes.API.ORDER_LISTING)
                //?.autoLoading(requireActivity())
                ?.enque(
                    Network().apis()?.ordersListing()
                )?.execute()
        }
    }

    private fun populateData() {
        showDialog()
        NetworkCall.make()?.setCallback(this)?.setTag(RequestCodes.API.ORDER_LISTING)
            //?.autoLoading(requireActivity())
            ?.enque(
                Network().apis()?.ordersListing()
            )?.execute()
    }

    private fun populateOrganizationSpinnerData(isClear: String) {
        val arrayList: ArrayList<String>? = ArrayList()
        arrayList?.clear()
        if (is_master_organization) {
            arrayList?.add("Select Filter")
            arrayList?.add("Master Organization")
            arrayList?.add("Total Branches")

            Log.e("arrayList listConnectedOrgs"," === " + Gson().toJson(listConnectedOrgs))
            //new Option added for connectedOrgs == AD change
            if (!listConnectedOrgs.isEmpty()) {
                for (i in listConnectedOrgs.indices) {
                    arrayList?.add(listConnectedOrgs.get(i).name.toString())
                }
            }


        } else {
            arrayList?.add("Select Filter") // now pos == 0
            //arrayList?.add("Total Households")  // hide for now - pos==1
            arrayList?.add("Total Organizations") //now pos == 1
            //arrayList?.add("Total (organizations + households)") // hide for now - pos==3

            //old ADDRESS in organization filter dropdown == AD change
            if (!User.retrieveUser()?.addresses.isNullOrEmpty()) {
                for (i in User.retrieveUser()?.addresses?.indices!!) {
                    var street = ""
                    var buildingName = ""
                    var unitNumberValue = ""
                    if (!User.retrieveUser()?.addresses?.get(i)?.building_name.isNullOrEmpty()) {
                        buildingName = "${User.retrieveUser()?.addresses?.get(i)?.building_name}"
                    }
                    if (!User.retrieveUser()?.addresses?.get(i)?.street.isNullOrEmpty()) {
                        street = "${User.retrieveUser()?.addresses?.get(i)?.street}"
                    }
                    if (!User.retrieveUser()?.addresses?.get(i)?.unit_number.isNullOrEmpty()) {
                        unitNumberValue = "${User.retrieveUser()?.addresses?.get(i)?.unit_number}"
                    }
                    val addressText =
                        "$unitNumberValue, $buildingName, $street, ${User.retrieveUser()?.addresses?.get(i)?.district?.name}, ${User.retrieveUser()?.addresses?.get(i)?.city?.name}"
                    arrayList?.add(addressText)

                    Log.e("arrayList sub"," === " + arrayList)
                }
            }
        }


        val setAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireActivity(), R.layout.spinner_item_textview, arrayList!!)
        setAdapter.setDropDownViewResource(R.layout.spinner_dropdown_textview)
        organizationFilter?.adapter = setAdapter
//        val onNet: OnNetworkResponse? = this
        organizationFilter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                if (position == 0) {

                    txtClearFilterReport!!.visibility = View.GONE
                    currentFilter = -1
                    selectedFilterName = ""

                    filter_option = null
                    address_id = null

                    if (!isClear.equals("FirstCall", ignoreCase = true)) {
                        populateData()
                    }
                }

                if (is_master_organization) {

                    if (position == 1) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()
                        filter_option = Constants.FilterOption.ORGANIZATION
                        address_id = null

                        getFilterData(currentFilter, filter_option, address_id)

                    }

                    if (position == 2) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()
                        filter_option = Constants.FilterOption.TOTAL_BRANCHES
                        address_id = null

                        if (!isClear.equals("FirstCall", ignoreCase = true)) {
                            getFilterData(currentFilter, filter_option, address_id)
                        }
                    }

                    //new Option added for connectedOrgs == AD change
                    if (position > 2) {
                        filter_option = Constants.FilterOption.CONNECTED_ORGS
                        Log.e("addressID0", " === ")

                        for (i in listConnectedOrgs.indices) {
                            if (parent!!.getItemAtPosition(position) == listConnectedOrgs.get(i).name) {
                                selectedFilterName = listConnectedOrgs.get(i).name.toString()
                                address_id = listConnectedOrgs.get(i).id
                                break
                            }
                        }

                        if (position != 0) {
                            txtClearFilterReport!!.visibility = View.VISIBLE
                            getFilterData(currentFilter, filter_option, address_id)
                        }

                    }

                }
                else {

                    //conditions for new options

                    if (position == 1) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()

                        filter_option = Constants.FilterOption.ORGANIZATION
                        address_id = null
                    }

                    //new Option added for connectedOrgs == AD change
                    if(position > 1) {

                        filter_option = Constants.FilterOption.ADDRESS

                        val temArrayList = ArrayList<String>()
                        for (i in User.retrieveUser()?.addresses?.indices!!) {
                            var street = ""
                            var buildingName = ""
                            var unitNumberValue = ""
                            if (!User.retrieveUser()?.addresses?.get(i)?.building_name.isNullOrEmpty()) {
                                buildingName =
                                    "${User.retrieveUser()?.addresses?.get(i)?.building_name}"
                            }
                            if (!User.retrieveUser()?.addresses?.get(i)?.street.isNullOrEmpty()) {
                                street = "${User.retrieveUser()?.addresses?.get(i)?.street}"
                            }
                            if (!User.retrieveUser()?.addresses?.get(i)?.unit_number.isNullOrEmpty()) {
                                unitNumberValue = "${User.retrieveUser()?.addresses?.get(i)?.unit_number}"
                            }
                            addressTextName = "$unitNumberValue, $buildingName, $street, ${
                                User.retrieveUser()?.addresses?.get(i)?.district?.name}, ${User.retrieveUser()?.addresses?.get(i)?.city?.name}"
                            temArrayList.add(addressTextName)
                        }
                        // i is for increment and j is for addresses
                        for ((i, j) in temArrayList.withIndex()) {
                            if (j.equals(arrayList[position], true)) {
                                address_id = "${User.retrieveUser()?.addresses?.get(i)?.id}".toInt()
                                break
                            }
                        }
                    }

                    // OLD only part of else condition is there
                    //correct coditions for all options
                    /*if (position == 1) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()

                        filter_option = Constants.FilterOption.HOUSEHOLD
                        address_id = null
                    }

                    if (position == 2) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()

                        filter_option = Constants.FilterOption.ORGANIZATION
                        address_id = null
                    }

                    if (position == 3) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()

                        filter_option = Constants.FilterOption.ALL
                        address_id = null
                    }

                    //new Option added for connectedOrgs == AD change
                    if(position > 3) {

                        filter_option = Constants.FilterOption.ADDRESS

                        val temArrayList = ArrayList<String>()
                        for (i in User.retrieveUser()?.addresses?.indices!!) {
                            var street = ""
                            var buildingName = ""
                            var unitNumberValue = ""
                            if (!User.retrieveUser()?.addresses?.get(i)?.building_name.isNullOrEmpty()) {
                                buildingName =
                                    "${User.retrieveUser()?.addresses?.get(i)?.building_name}"
                            }
                            if (!User.retrieveUser()?.addresses?.get(i)?.street.isNullOrEmpty()) {
                                street = "${User.retrieveUser()?.addresses?.get(i)?.street}"
                            }
                            if (!User.retrieveUser()?.addresses?.get(i)?.unit_number.isNullOrEmpty()) {
                                unitNumberValue =
                                    "${User.retrieveUser()?.addresses?.get(i)?.unit_number}"
                            }
                            addressTextName = "$unitNumberValue, $buildingName, $street, ${
                                    User.retrieveUser()?.addresses?.get(i)?.district?.name}, ${User.retrieveUser()?.addresses?.get(i)?.city?.name}"
                            temArrayList.add(addressTextName)
                        }
                        // i is for increment and j is for addresses
                        for ((i, j) in temArrayList.withIndex()) {
                            if (j.equals(arrayList[position], true)) {
                                address_id = "${User.retrieveUser()?.addresses?.get(i)?.id}".toInt()
                                break
                            }
                        }
                    }*/

                    if (position != 0) {
                        txtClearFilterReport!!.visibility = View.VISIBLE
                        getFilterData(currentFilter, filter_option, address_id)
                        address_id = null
                    }
                }
            }
        }

        if (is_master_organization) {
            if (isClear.equals("clear")) {
                organizationFilter!!.setSelection(0) // Clear filter
            } else {
                organizationFilter!!.setSelection(2) // FOR TOTAL BRANCHES
            }
        }
    }

    private fun getFilterData(
        index: Int, filter_option: Int?, address_id: Int?
    ) {

        if (!NetworkCall.inProgress()) {
            showDialog()
            currentFilter = index

            if(is_master_organization) {
                NetworkCall.make()?.setCallback(this)?.setTag(RequestCodes.API.ORDER_LISTING)
//                ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.ordersListingFilter(filter_option, address_id))
                    ?.execute()
            }
            else{
                NetworkCall.make()?.setCallback(this)?.setTag(RequestCodes.API.ORDER_LISTING)
//                ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.ordersListingFilterAddress(filter_option, address_id))
                    ?.execute()
            }
        }
    }


    private fun populateRecyclerViewData(combineList: ArrayList<Any>) {
        //new added
        Log.d("SPPEDTEST", "111")

        Collections.sort(combineList, object : Comparator<Any?> {
            override fun compare(o1: Any?, o2: Any?): Int {
                var res = 0
                if (o1 is UserOrders && o2 is UserOrders) {
                    res = (o1 as UserOrders).created_at!!.compareTo((o2 as UserOrders).created_at!!)
                } else if (o1 is CollectionRequests && o2 is CollectionRequests) {
                    res =
                        (o1 as CollectionRequests).created_at!!.compareTo((o2 as CollectionRequests).created_at!!)
                } else if (o1 is UserOrders && o2 is CollectionRequests) {
                    res =
                        (o1 as UserOrders).created_at!!.compareTo((o2 as CollectionRequests).created_at!!)
                } else if (o1 is CollectionRequests && o2 is UserOrders) {
                    res =
                        (o1 as CollectionRequests).created_at!!.compareTo((o2 as UserOrders).created_at!!)
                }

                return res
            }
        })

        Log.d("SPPEDTEST", "222")

//        Log.d("COMBINE"," LIST : " + GsonBuilder().setPrettyPrinting().create().toJson(combineList))


        linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager!!.reverseLayout = true
        linearLayoutManager!!.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        //OLD
//        recyclerView?.adapter = AdapterOrderHistory(orderList, this, userCollectionRequests,combineList, this)//old

        //NEW
        recyclerView?.adapter = AdapterOrderHistoryNew(this, combineList, this)//old


        if (recyclerView?.adapter?.itemCount == 0) {
            dismissProgressDialog()
        } else {
            recyclerView!!.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (recyclerView!!.childCount > 0) {
                        // RecyclerView items are loaded and displayed
                        dismissProgressDialog()
                        recyclerView!!.viewTreeObserver.removeOnPreDrawListener(this)
                    }
                    return true
                }
            })
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }

            R.id.llrootlay -> {

                lldownloads !!.visibility = View.GONE
            }

            R.id.txtClearFilterReport -> {
                txtClearFilterReport!!.visibility = View.GONE
                currentFilter = -1
                selectedFilterName = ""

                filter_option = null
                address_id = null

//                populateData()
//                populateDataFirstTime()

                populateOrganizationSpinnerData("clear")
            }
        }
    }

    private fun showDialog() {
        dialog = Dialog(requireContext())
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.progress_dialog_layout)
            dialog.show()
        }
    }

    private fun dismissProgressDialog() {
        dialog.dismiss()
    }

    fun exportReport() {

        try {

            //        val hssfWorkbook = HSSFWorkbook()
            val hssfWorkbook = XSSFWorkbook()

            val hssfSheet = hssfWorkbook.createSheet("Requests")

            //---------------
            //---------------

            // Create a cell style with a specific background color
            /*val backgroundColorStyle = hssfWorkbook.createCellStyle()
            backgroundColorStyle.fillForegroundColor = IndexedColors.YELLOW.index
            backgroundColorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)*/

            //---------------
            //---------------

            //Order HISTORY ========================START==============================

            val hssfRowTitle = hssfSheet.createRow(1)
            hssfRowTitle.createCell(0).setCellValue("ID")
            hssfRowTitle.createCell(1).setCellValue("Request Number")
            hssfRowTitle.createCell(2).setCellValue("Name")
            hssfRowTitle.createCell(3).setCellValue("Type")
            hssfRowTitle.createCell(4).setCellValue("Email")
            hssfRowTitle.createCell(5).setCellValue("Phone")
            hssfRowTitle.createCell(6).setCellValue("Referral")
            hssfRowTitle.createCell(7).setCellValue("Request Status")
            hssfRowTitle.createCell(8).setCellValue("Subscription Number")
            hssfRowTitle.createCell(9).setCellValue("Request City")
            hssfRowTitle.createCell(10).setCellValue("Request District")
            hssfRowTitle.createCell(11).setCellValue("Location")
            hssfRowTitle.createCell(12).setCellValue("Collection Date")
//        hssfRowTitle.createCell(13).setCellValue("Material Categories") // OLD To show material categories in single cell

            var materialColumnNo = 13 // start with 14 to add one column in between


            //Material Category Title
            if (orderList != null) {
                for (pos in 0 until orderList!!.materialCategories!!.size) {
                    hssfRowTitle.createCell(materialColumnNo)
                        .setCellValue(orderList!!.materialCategories!!.get(pos).name)
                    materialColumnNo++
                }
            }


            //User Stats Title
//        materialColumnNo++ // for ONE Extra empty column

            hssfRowTitle.createCell(materialColumnNo).setCellValue("co2_emission_reduced")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("trees_saved")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("oil_saved")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("electricity_saved")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("water_saved")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("landfill_space_saved")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("compost_created")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("cigarette_butts_saved")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("biodiesel_produced")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("farming_land")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("coffee_capsule")
            materialColumnNo++
            hssfRowTitle.createCell(materialColumnNo).setCellValue("soap_bars")

            var row = 2
            var srNo = 1

            if (!combineList.isNullOrEmpty()) {
                for (position in combineList!!.indices) {
                    val hssfRow = hssfSheet.createRow(row)

                    if (combineList!!.get(position) is UserOrders) { //Orders

                        //USE this to show "products" in excel sheet report

                        /*val userOrdersData: UserOrders = combineList!!.get(position) as UserOrders
                        hssfRow.createCell(0).setCellValue(userOrdersData.id.toString())
                        hssfRow.createCell(1).setCellValue(userOrdersData.order_number.toString())
                        hssfRow.createCell(2).setCellValue(userOrdersData.first_name.toString() + " "+userOrdersData.last_name.toString())
                        hssfRow.createCell(3).setCellValue("Household") //static
                        hssfRow.createCell(4).setCellValue(userOrdersData.email.toString())
                        hssfRow.createCell(5).setCellValue(userOrdersData.phone_number.toString())
                        hssfRow.createCell(6).setCellValue("-")
                        hssfRow.createCell(7).setCellValue(getRequestStatus(userOrdersData.status,"order"))
                        hssfRow.createCell(8).setCellValue("-")
                        hssfRow.createCell(9).setCellValue(userOrdersData.city!!.name)
                        hssfRow.createCell(10).setCellValue(userOrdersData.district!!.name)
                        hssfRow.createCell(11).setCellValue(userOrdersData.location.toString())
                        hssfRow.createCell(12).setCellValue(userOrdersData.delivery_date.toString())

                        //OLD
                        var allCategories = ""
                        if(!userOrdersData.order_items.isNullOrEmpty()){
                            for(category in userOrdersData.order_items!!.indices){
                                allCategories = allCategories +"\n" + userOrdersData.order_items!!.get(category).product!!.name +
                                        userOrdersData.order_items!!.get(category).quantity
                            }

                        }
                        hssfRow.createCell(13).setCellValue(allCategories)
    */

                        //NEW
                        /*if(orderList != null && !userOrdersData.order_items.isNullOrEmpty()){
                            var materialCellNo = 14

                            for (cellPos in 0 until orderList!!.materialCategories!!.size){

                                hssfRow.createCell(materialCellNo).setCellValue(""+cellPos)
                                materialCellNo++
                            }
                        }*/

                    } else { //Collections

                        val collectionRequestsData: CollectionRequests =
                            combineList!!.get(position) as CollectionRequests
                        hssfRow.createCell(0).setCellValue(collectionRequestsData.id.toString())
                        hssfRow.createCell(1).setCellValue(collectionRequestsData.request_number.toString())

                        if (MainApplication.userType() == Constants.UserType.household) {
                            hssfRow.createCell(2).setCellValue(collectionRequestsData.first_name.toString() + " " + collectionRequestsData.last_name.toString())
                            hssfRow.createCell(3).setCellValue("Household")
                        } else {
                            if (!User.retrieveUser()?.organization?.name.isNullOrEmpty()) {
                                hssfRow.createCell(2).setCellValue(User.retrieveUser()?.organization?.name)
                            }
                            hssfRow.createCell(3).setCellValue("Organization")
                        }

                        hssfRow.createCell(4).setCellValue("" + User.retrieveUser()?.email)

                        hssfRow.createCell(5).setCellValue(collectionRequestsData.phone_number.toString())
                        hssfRow.createCell(6).setCellValue("-")

  //                    hssfRow.createCell(7).setCellValue(getRequestStatus(collectionRequestsData.status,"collection"))
                        hssfRow.createCell(7).setCellValue("" + collectionRequestsData.status)

                        hssfRow.createCell(8).setCellValue(collectionRequestsData.user_subscription_id.toString())
                        hssfRow.createCell(9).setCellValue(collectionRequestsData.city!!.name)
                        hssfRow.createCell(10).setCellValue(collectionRequestsData.district!!.name)
                        hssfRow.createCell(11).setCellValue(collectionRequestsData.location.toString())
                        hssfRow.createCell(12).setCellValue(collectionRequestsData.collection_date.toString())

                        // OLD To show material categories in single cell
                        /*var allCategories = ""
                        if(!collectionRequestsData.request_collection.isNullOrEmpty()){
                            for(category in collectionRequestsData.request_collection!!.indices){

                                if (!collectionRequestsData.request_collection?.get(category)?.weight.isNullOrEmpty()) {
                                    var unit = ""
                                    unit = when (collectionRequestsData.request_collection?.get(category)?.materialCategory?.unit) {
                                        1 -> {
                                            "Kg"
                                        }
                                        2 -> {
                                            "Liter"
                                        }
                                        3 -> {
                                            "Pieces"
                                        }
                                        else -> {
                                            ""
                                        }
                                    }

                                    allCategories = allCategories + "\n" + collectionRequestsData.request_collection!!.get(category).category_name +" "+ collectionRequestsData.request_collection?.get(category)?.weight +" " + unit
                                }else{
                                    allCategories = allCategories + "\n" + collectionRequestsData.request_collection!!.get(category).category_name
                                }

                            }

                        }
                        hssfRow.createCell(13).setCellValue(allCategories)*/

                        // NEW Material category horizontal list
                        var materialCellNo = 13 // start with 14 to add one column in between
                        if (orderList != null && !collectionRequestsData.request_collection.isNullOrEmpty()) {


                            for (cellPos in 0 until orderList!!.materialCategories!!.size) {
                                hssfRow.createCell(materialCellNo).setCellValue("0")
                                for (materialPos in 0 until collectionRequestsData.request_collection!!.size) {
                                    if (collectionRequestsData.request_collection!!.get(materialPos).materialCategoryId == orderList!!.materialCategories!!.get(
                                            cellPos).id) {

                                        if (!collectionRequestsData.request_collection!!.get(materialPos).weight.isNullOrEmpty()) {
                                            hssfRow.createCell(materialCellNo).setCellValue(
                                                "" + collectionRequestsData.request_collection!!.get(materialPos).weight)
                                        } else {
                                            hssfRow.createCell(materialCellNo).setCellValue("0")
                                        }
                                    }
                                }
                                materialCellNo++
                            }
                        }


//                    materialCellNo++ // for ONE Extra empty column
                        //User-Stats Data ==================
                        if (orderList != null && !collectionRequestsData.request_collection.isNullOrEmpty()) {

                            //NEW sum of all user stats
                            val totalUserStats =
                                getSumOfUserStats(collectionRequestsData.request_collection!!)
                            if (!totalUserStats.isEmpty()) {
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(0))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(1))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(2))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(3))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(4))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(5))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(6))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(7))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(8))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(9))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(10))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo)
                                    .setCellValue("" + totalUserStats.get(11))

                                /*materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ totalUserStats.get(12))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ totalUserStats.get(13))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ totalUserStats.get(14))
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ totalUserStats.get(15))*/
                            }

                            //ORIGINAL ONLY first position displayed
                            /*if(!collectionRequestsData.request_collection!!.get(0).user_stats.isNullOrEmpty()){
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).co2_emission_reduced)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).trees_saved)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).oil_saved)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).electricity_saved)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).water_saved)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).landfill_space_saved)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).compost_created)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).cigarette_butts_saved)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).biodiesel_produced)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).farming_land)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).coffee_capsule)
                                materialCellNo++
                                hssfRow.createCell(materialCellNo).setCellValue(""+ collectionRequestsData.request_collection!!.get(0).user_stats!!.get(0).soap_bars)
                            }*/
                        }

                        //NEW HERE ........Added this because there is empty rows in-between collection requests

                        row++
                        srNo++

                    }


                    //OLD HERE ........commented this because there is empty rows in-between collection requests
                    /*row++
                    srNo++*/
                }
            }


            //Order HISTORY ========================END==============================


//        autosizeColumns(hssfSheet)

            hssfSheet.setColumnWidth(0, 3500)
            hssfSheet.setColumnWidth(1, 4000)
            hssfSheet.setColumnWidth(2, 4000)
            hssfSheet.setColumnWidth(3, 4000)
            hssfSheet.setColumnWidth(4, 7000)
            hssfSheet.setColumnWidth(5, 6000)
            hssfSheet.setColumnWidth(6, 2500)
            hssfSheet.setColumnWidth(7, 4000)
            hssfSheet.setColumnWidth(8, 3000)
            hssfSheet.setColumnWidth(9, 6000)
            hssfSheet.setColumnWidth(10, 6000)
            hssfSheet.setColumnWidth(11, 8000)
            hssfSheet.setColumnWidth(12, 4000)
//        hssfSheet.setColumnWidth(13,8000)
            hssfSheet.setColumnWidth(14, 4000)
            hssfSheet.setColumnWidth(15, 2000)
            hssfSheet.setColumnWidth(16, 2000)
            hssfSheet.setColumnWidth(17, 5000)


            //---------
            //---------
            val path: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator)
            val file = File(path.toString())
            file.mkdirs()
            val fileName: String =
                path.toString() + "/" + "reloop_" + System.currentTimeMillis() + ".xlsx"
            try {
                val fileOutputStream = FileOutputStream(fileName)
                hssfWorkbook.write(fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()

//            Toast.makeText(requireContext(),"Report Generated. \n $path", Toast.LENGTH_SHORT).show()


                /*Notify.alerterGreen(
                    requireActivity(),
                    "Report downloaded in download folder"
                )*/

                Notify.hyperlinkAlertGreen(
                    activity, "Report downloaded in download folder", "Open", this, 2
                )


            } catch (e: IOException) {
                e.printStackTrace()
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportReportwithBins() {
        showDialog()
        var uiHandler = Handler(Looper.getMainLooper())
        Thread({
            try {
                var hssfWorkbook = createWorkBook()
                //---------
                //---------
                val path: File =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator)
                val file = File(path.toString())
                file.mkdirs()
                val fileName: String =
                    path.toString() + "/" + "reloop_" + System.currentTimeMillis() + ".xlsx"
                try {
                    val fileOutputStream = FileOutputStream(fileName)
                    hssfWorkbook.write(fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    //Toast.makeText(requireContext(),"Report Generated. \n $path", Toast.LENGTH_SHORT).show()

                    /*Notify.alerterGreen(
                        requireActivity(),
                        "Report downloaded in download folder"
                    )*/

                    uiHandler.post {
                        Notify.hyperlinkAlertGreen(
                            activity, "Report downloaded in download folder", "Open", this, 2
                        )
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                uiHandler.post {
                    dismissProgressDialog()
                }
            }
        }).start()


    }

    private fun createWorkBook(): XSSFWorkbook {
        //        val hssfWorkbook = HSSFWorkbook()
        val hssfWorkbook = XSSFWorkbook()

        val hssfSheet = hssfWorkbook.createSheet("Requests")


        //Order HISTORY ========================START==============================

        var rowIndex = 0
        generateColumns(hssfSheet.createRow(rowIndex))
        rowIndex++;
        userCollectionRequests?.forEach { collectionRequest ->
            var _binCollection =
                collectionRequest.request_collection?.filter { !it.collection_bins.isNullOrEmpty() }
            if (!_binCollection.isNullOrEmpty()) {
                var hssfRow = hssfSheet.createRow(rowIndex)
                fillDefaultRow(hssfRow, collectionRequest)
                rowIndex++;
                var columnIndex = 13;
                //Bin Location
                columnIndex++;
                //Bin Weight
                columnIndex++;
                //Bin Status
                columnIndex++;
                //Bin Category Name
                columnIndex++;
                orderList!!.materialCategories?.forEach { materialCategory->
                    //Specific Category Sum
                    var sumOfWeight = 0f
                    //Iterate Collection Items and Find Weight for that particular category
                    collectionRequest.request_collection?.forEach { collectionItem ->
                        if(collectionItem.materialCategoryId == materialCategory.id) {
                            try {
                                var collectionItemWeight = collectionItem.weight?.toFloat() ?: 0f
                                sumOfWeight += collectionItemWeight
                            }
                            catch (e : Exception) {}
                        }
                    }
                    if(sumOfWeight > 0f) hssfRow.createCell(columnIndex).setCellValue("$sumOfWeight")
                    else hssfRow.createCell(columnIndex).setCellValue("0")
                    columnIndex++
                }
                collectionRequest.request_collection?.forEach { collectionItem ->
                    var categoryName = collectionItem.category_name
                    collectionItem.collection_bins?.forEach { collectionBin ->
                        var hssfRow = hssfSheet.createRow(rowIndex)
                        rowIndex++;
                        fillDefaultRow(hssfRow, collectionRequest)
                        var columnIndex = 13;
                        //Bin Location
                        hssfRow.createCell(columnIndex)
                            .setCellValue("${collectionBin.getLocationBin()?.qrTitle}")
                        columnIndex++;
                        //Bin Weight
                        hssfRow.createCell(columnIndex).setCellValue(collectionBin.getWeight()?:"0")
                        columnIndex++;
                        //Bin Status
                        hssfRow.createCell(columnIndex).setCellValue("${collectionBin.getStatus()}")
                        columnIndex++;
                        //Bin Category Name
                        hssfRow.createCell(columnIndex).setCellValue("${categoryName}")
                        columnIndex++;

                        orderList!!.materialCategories?.forEach {
                            hssfRow.createCell(columnIndex).setCellValue(if(collectionItem.materialCategoryId == it.id) collectionBin.getWeight() ?: "0" else "0")
                            columnIndex++
                        }
                    }
                }
            }
            else {
                collectionRequest.request_collection?.forEach { collectionItem ->
                    var hssfRow = hssfSheet.createRow(rowIndex)
                    rowIndex++;
                    fillDefaultRow(hssfRow, collectionRequest)
                    var columnIndex = 13;
                    //Bin Location
                    columnIndex++;
                    //Bin Weight
                    columnIndex++;
                    //Bin Status
                    columnIndex++;
                    //Bin Category Name
                    columnIndex++;
                    orderList!!.materialCategories?.forEach {
                        hssfRow.createCell(columnIndex).setCellValue(if(collectionItem.materialCategoryId == it.id) collectionItem.weight ?: "0" else "0")
                        columnIndex++
                    }
                }
            }
        }


        hssfSheet.setColumnWidth(0, 3500)
        hssfSheet.setColumnWidth(1, 4000)
        hssfSheet.setColumnWidth(2, 4000)
        hssfSheet.setColumnWidth(3, 4000)
        hssfSheet.setColumnWidth(4, 7000)
        hssfSheet.setColumnWidth(5, 6000)
        hssfSheet.setColumnWidth(6, 2500)
        hssfSheet.setColumnWidth(7, 4000)
        hssfSheet.setColumnWidth(8, 3000)
        hssfSheet.setColumnWidth(9, 6000)
        hssfSheet.setColumnWidth(10, 6000)
        hssfSheet.setColumnWidth(11, 8000)
        hssfSheet.setColumnWidth(12, 4000)
        hssfSheet.setColumnWidth(13,8000)
        hssfSheet.setColumnWidth(14, 4000)
        hssfSheet.setColumnWidth(15, 3000)
        hssfSheet.setColumnWidth(16, 3000)
        hssfSheet.setColumnWidth(17, 5000)
        return hssfWorkbook;
    }

    private fun findCategoryMatch(
        materialCategoryId: Int?,
        materialCategories: java.util.ArrayList<MaterialCategories>?
    ): String {
        materialCategories?.forEach { item ->
            if (item.id == materialCategoryId) {
                return item.name ?: ""
            }
        }
        return ""
    }

    private fun generateColumns(hssfRowTitle: XSSFRow) {
        var columnIndex = 0
        hssfRowTitle.createCell(columnIndex).setCellValue("ID")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Request Number")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Name")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Type")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Email")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Phone")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Referral")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Request Status")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Subscription Number")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Request City")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Request District")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Location")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Collection Date")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Bin Location")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Bin Weight")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Bin Status")
        columnIndex++
        hssfRowTitle.createCell(columnIndex).setCellValue("Bin Category Name")
        columnIndex++
        orderList!!.materialCategories?.forEach {
            hssfRowTitle.createCell(columnIndex).setCellValue(it.name)
            columnIndex++
        }

        //"Was the parking easy?","Did the user clean and segregate properly?","Was building access easy?","User Comments","Additional Comments"
    }

    private fun fillDefaultRow(hssfRow: XSSFRow, data: CollectionRequests) {
        val user = User.retrieveUser()
        val isHousehold = MainApplication.userType() == Constants.UserType.household

        hssfRow.createCell(0).setCellValue(data.id.toString())
        hssfRow.createCell(1).setCellValue(data.request_number.toString())

        // Column 2: Name or Organization Name
        val nameOrOrg = if (isHousehold) {
            "${data.first_name ?: ""} ${data.last_name ?: ""}".trim()
        } else {
            user?.organization?.name ?: ""
        }
        hssfRow.createCell(2).setCellValue(nameOrOrg)

        // Column 3: Type
        hssfRow.createCell(3).setCellValue(if (isHousehold) "Household" else "Organization")

        // Column 4: Email
        hssfRow.createCell(4).setCellValue(user?.email ?: "")

        // Column 5: Phone Number
        hssfRow.createCell(5).setCellValue(data.phone_number ?: "")

        // Column 6: Reserved/Placeholder
        hssfRow.createCell(6).setCellValue("-")

        // Column 7: Status
        hssfRow.createCell(7).setCellValue(data.status ?: "")

        // Column 8: Subscription ID
        hssfRow.createCell(8).setCellValue(data.user_subscription_id?.toString() ?: "")

        // Column 9: City
        hssfRow.createCell(9).setCellValue(data.city?.name ?: "")

        // Column 10: District
        hssfRow.createCell(10).setCellValue(data.district?.name ?: "")

        // Column 11: Location
        hssfRow.createCell(11).setCellValue(data.location ?: "")

        // Column 12: Collection Date
        hssfRow.createCell(12).setCellValue(data.collection_date?.toString() ?: "")
    }


    private fun exportReportblankdetails(
        hssfRow: XSSFRow,
        collectionRequestsData: CollectionRequests,
    ): Boolean {

        hssfRow.createCell(0).setCellValue(collectionRequestsData.id.toString())
        hssfRow.createCell(1).setCellValue(collectionRequestsData.request_number.toString())

        if (MainApplication.userType() == Constants.UserType.household) {
            hssfRow.createCell(2)
                .setCellValue(collectionRequestsData.first_name.toString() + " " + collectionRequestsData.last_name.toString())
            hssfRow.createCell(3).setCellValue("Household")
        } else {
            if (!User.retrieveUser()?.organization?.name.isNullOrEmpty()) {
                hssfRow.createCell(2).setCellValue(User.retrieveUser()?.organization?.name)
            }
            hssfRow.createCell(3).setCellValue("Organization")
        }
        hssfRow.createCell(4).setCellValue("" + User.retrieveUser()?.email)
        hssfRow.createCell(5).setCellValue(collectionRequestsData.phone_number.toString())
        hssfRow.createCell(6).setCellValue("-")
//                    hssfRow.createCell(7).setCellValue(getRequestStatus(collectionRequestsData.status,"collection"))
        hssfRow.createCell(7).setCellValue("" + collectionRequestsData.status)
        hssfRow.createCell(8).setCellValue(collectionRequestsData.user_subscription_id.toString())
        hssfRow.createCell(9).setCellValue(collectionRequestsData.city!!.name)
        hssfRow.createCell(10).setCellValue(collectionRequestsData.district!!.name)
        hssfRow.createCell(11).setCellValue(collectionRequestsData.location.toString())
        hssfRow.createCell(12).setCellValue(collectionRequestsData.collection_date.toString())

        // NEW Material category horizontal list
        var materialCellNo = 13 // start with 14 to add one column in between
        if (orderList != null && !collectionRequestsData.request_collection.isNullOrEmpty()) {

            for (cellPos in 0 until orderList!!.materialCategories!!.size) {
                hssfRow.createCell(materialCellNo).setCellValue("0")
                for (materialPos in 0 until collectionRequestsData.request_collection!!.size) {
                    if (collectionRequestsData.request_collection!!.get(materialPos).materialCategoryId == orderList!!.materialCategories!!.get(
                            cellPos
                        ).id
                    ) {

                        if (!collectionRequestsData.request_collection!!.get(materialPos).weight.isNullOrEmpty()) {
                            hssfRow.createCell(materialCellNo).setCellValue(
                                "" + collectionRequestsData.request_collection!!.get(materialPos).weight
                            )
                        } else {
                            hssfRow.createCell(materialCellNo).setCellValue("0")
                        }
                    }
                }
                materialCellNo++
            }
        }

        //materialCellNo++ // for ONE Extra empty column
        //User-Stats Data ==================
        if (orderList != null && !collectionRequestsData.request_collection.isNullOrEmpty()) {

            //NEW sum of all user stats
            val totalUserStats = getSumOfUserStats(collectionRequestsData.request_collection!!)
            if (!totalUserStats.isEmpty()) {
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(0))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(1))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(2))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(3))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(4))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(5))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(6))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(7))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(8))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(9))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(10))
                materialCellNo++
                hssfRow.createCell(materialCellNo).setCellValue("" + totalUserStats.get(11))
            }
        }

        hssfRow.createCell(14).setCellValue("")

        hssfRow.createCell(15).setCellValue("")

        hssfRow.createCell(16).setCellValue("")

        hssfRow.createCell(17).setCellValue("")

        return true
    }

    private fun autosizeColumns(sheet: Sheet) {
        // Ensure there is at least one row before autosizing columns
        if (sheet.physicalNumberOfRows > 1) {
            // Iterate over all columns and auto-size each one
            val firstRow: Row = sheet.getRow(1)
            for (i in 1 until firstRow.getLastCellNum()) {
                sheet.autoSizeColumn(i)
            }
        }
    }


    fun getSumOfUserStats(requestCollection: ArrayList<CollectionRequestProduct>): ArrayList<Double> {
        sumList.clear()
        var one = 0.0
        var two = 0.0
        var three = 0.0
        var four = 0.0
        var five = 0.0
        var six = 0.0
        var seven = 0.0
        var eight = 0.0
        var nine = 0.0
        var ten = 0.0
        var eleven = 0.0
        var twelve = 0.0

        for (i in 0 until requestCollection.size) {
            if (!requestCollection.get(i).user_stats.isNullOrEmpty()) {
                val value1 =
                    requestCollection.get(i).user_stats!!.get(0).co2_emission_reduced!!.toDoubleOrNull()
                        ?: 0.0
                val value2 =
                    requestCollection.get(i).user_stats!!.get(0).trees_saved!!.toDoubleOrNull()
                        ?: 0.0
                val value3 =
                    requestCollection.get(i).user_stats!!.get(0).oil_saved!!.toDoubleOrNull() ?: 0.0
                val value4 =
                    requestCollection.get(i).user_stats!!.get(0).electricity_saved!!.toDoubleOrNull()
                        ?: 0.0
                val value5 =
                    requestCollection.get(i).user_stats!!.get(0).water_saved!!.toDoubleOrNull()
                        ?: 0.0
                val value6 =
                    requestCollection.get(i).user_stats!!.get(0).landfill_space_saved!!.toDoubleOrNull()
                        ?: 0.0
                val value7 =
                    requestCollection.get(i).user_stats!!.get(0).compost_created!!.toDoubleOrNull()
                        ?: 0.0
                val value8 =
                    requestCollection.get(i).user_stats!!.get(0).cigarette_butts_saved!!.toDoubleOrNull()
                        ?: 0.0
                val value9 =
                    requestCollection.get(i).user_stats!!.get(0).biodiesel_produced!!.toDoubleOrNull()
                        ?: 0.0
                val value10 =
                    requestCollection.get(i).user_stats!!.get(0).farming_land!!.toDoubleOrNull()
                        ?: 0.0
                val value11 =
                    requestCollection.get(i).user_stats!!.get(0).coffee_capsule!!.toDoubleOrNull()
                        ?: 0.0
                val value12 =
                    requestCollection.get(i).user_stats!!.get(0).soap_bars!!.toDoubleOrNull() ?: 0.0


                one = one + value1
                two = two + value2
                three = three + value3
                four = four + value4
                five = five + value5
                six = six + value6
                seven = seven + value7
                eight = eight + value8
                nine = nine + value9
                ten = ten + value10
                eleven = eleven + value11
                twelve = twelve + value12

            } else {/*one = 0.0
                two =0.0
                three =0.0
                four =0.0
                five =0.0
                six =0.0
                seven =0.0
                eight =0.0
                nine =0.0
                ten =0.0
                eleven =0.0
                twelve =0.0*/
            }
        }

        sumList.add(one)
        sumList.add(two)
        sumList.add(three)
        sumList.add(four)
        sumList.add(five)
        sumList.add(six)
        sumList.add(seven)
        sumList.add(eight)
        sumList.add(nine)
        sumList.add(ten)
        sumList.add(eleven)
        sumList.add(twelve)

        return sumList

    }


    @SuppressLint("SetTextI18n")
    private fun getRequestStatus(status: String?, s: String): String {
        when (status) {
            OrderHistoryEnum.ASSIGNED -> {
                return MainApplication.applicationContext().getString(R.string.assigned)
            }

            OrderHistoryEnum.TRIP_INITIATED -> {
                return MainApplication.applicationContext().getString(R.string.trip_initiated)
            }

            OrderHistoryEnum.COMPLETED -> {
                if (s.equals("order")) {
                    return MainApplication.applicationContext().getString(R.string.delivered)
                } else {
                    //collected
                    return MainApplication.applicationContext().getString(R.string.collected)
                }
            }

            OrderHistoryEnum.NOT_ASSIGNED -> {
                //pending
                return MainApplication.applicationContext().getString(R.string.pending)
            }

            OrderHistoryEnum.CANCELLED -> {
                return MainApplication.applicationContext().getString(R.string.cancelled)
            }

            OrderHistoryEnum.REFUND_REQUEST -> {
                return MainApplication.applicationContext().getString(R.string.request_refund)
            }

            OrderHistoryEnum.ORDER_REFUNDED -> {
                return MainApplication.applicationContext().getString(R.string.order_refund)
            }

            OrderHistoryEnum.ORDER_VERIFIED -> {
                if (s.equals("collection")) {
                    return MainApplication.applicationContext().getString(R.string.order_verified)
                }
            }

            "-1" -> {
                return "N/A"
            }
        }
        return "N/A"
    }

    //OLD
    /*override fun itemPosition(position: Int) {
        val fragment: Fragment
        fragment = if (position < userOrders!!.size) {
            ViewReceiptFragment.newInstance(userOrders?.get(position), null, this)
        } else {
            ViewReceiptFragment.newInstance(
                null,
                userCollectionRequests?.get(position - userOrders!!.size), this
            )
        }
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerOrderHistory,
            fragment,
            Constants.TAGS.ViewReceiptFragment
        )
    }*/

    //new
    override fun itemPosition(position: Int) {
        val fragment: Fragment
        fragment = if (combineList!!.get(position) is UserOrders) {//Orders
            val userOrdersData: UserOrders = combineList!!.get(position) as UserOrders

            ViewReceiptFragment.newInstance(userOrdersData, null, this)
        } else {//collection
            val collectionRequestsData: CollectionRequests =
                combineList!!.get(position) as CollectionRequests

            ViewReceiptFragment.newInstance(
                null, collectionRequestsData, this
            )
        }

        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerOrderHistory,
            fragment,
            Constants.TAGS.ViewReceiptFragment
        )
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.ORDER_LISTING -> {
                Log.d("SPPEDTEST", "999")

                //OLD
                val baseResponse = Utils.getBaseResponse(response)
                orderList = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    OrderHistory::class.java
                )

                iscollectionBinAvaialble = orderList!!.hasCollectionBins

                /*val baseResponse = Utils.getBaseResponseReloop(response)
                orderList = baseResponse?.data?.let { data ->
                    val json = Utils.jsonConverterObjectReloop(data as? LinkedTreeMap<*, *>)
                    Gson().fromJson(json, object : TypeToken<OrderHistory>() {}.type)
                }*/

                Log.d("SPPEDTEST", "888")

                userOrders!!.clear()
                userCollectionRequests!!.clear()

                userOrders = orderList?.getUserOrders
                userCollectionRequests = orderList?.getUserCollectionRequests

                Log.d("SPPEDTEST", "000")

                combineList!!.clear()
                combineList!!.addAll(orderList?.getUserOrders!!)
                combineList!!.addAll(orderList?.getUserCollectionRequests!!)

                if (userOrders == null) {
                    userOrders = ArrayList()
                }
                if (userCollectionRequests == null) {
                    userCollectionRequests = ArrayList()
                }

                if (userOrders!!.size > 0 || userCollectionRequests!!.size > 0) {
                    recyclerView?.visibility = View.VISIBLE
                    tvNoOrders?.visibility = View.GONE
                    populateRecyclerViewData(combineList!!)
                } else {
                    dismissProgressDialog()
                    recyclerView?.visibility = View.GONE
                    tvNoOrders?.visibility = View.VISIBLE
                }

                //PRELOAD driver,user and supervisor images
                if (!userCollectionRequests.isNullOrEmpty()) {
                    preLoadImages?.clear()
                    for (i in userCollectionRequests!!.indices) {
                        for (j in 0 until userCollectionRequests!!.get(i).driver_images!!.size) {
                            preLoadImages?.add(userCollectionRequests!!.get(i).driver_images!!.get(j).image.toString())
                        }

                        for (k in 0 until userCollectionRequests!!.get(i).supervisor_images!!.size) {
                            preLoadImages?.add(
                                userCollectionRequests!!.get(i).supervisor_images!!.get(
                                    k
                                ).image.toString()
                            )
                        }

                        for (k in 0 until userCollectionRequests!!.get(i).user_images!!.size) {
                            preLoadImages?.add(userCollectionRequests!!.get(i).user_images!!.get(k).image.toString())
                        }

                    }

                }


                //FILTER
                try {
                    if (orderList!!.connectedOrgs != null && !orderList!!.connectedOrgs!!.isEmpty()) {
                        //check if connectedOrgs exists
                        for (i in orderList!!.connectedOrgs!!.indices) {
                            val organization = Organization()
                            organization.id = orderList!!.connectedOrgs!!.get(i).id
                            organization.name =
                                orderList!!.connectedOrgs!!.get(i).organization!!.name
                            listConnectedOrgs.add(organization)
                        }
                    }

                    //NEW CONDITION ADDED
                    if (!isScreenOpened) {
                        Log.d("IS_FIRST", "111")
                        populateOrganizationSpinnerData("FirstCall")
                        isScreenOpened = true
                    }


                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {

                    /*CoroutineScope(Dispatchers.Main).launch {
                        preloadAllImages(preLoadImages!!)
                    }*/


                    loadNextBatch()

                    // Set up scroll listener
                    recyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)

                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val visibleItemCount = layoutManager.childCount
                            val totalItemCount = layoutManager.itemCount
                            val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                            // Check if we need to load more data
                            if (!isLoading && (pastVisibleItems + visibleItemCount) >= totalItemCount) {
                                loadNextBatch()
                            }
                        }
                    })
                }
            }
        }
    }

    private fun loadNextBatch() {
        if (scrolledItems >= preLoadImages!!.size || isLoading) return

        isLoading = true

        // Calculate the next threshold
        val nextThreshold = (scrolledItems / batchSize + 1) * batchSize

        // Check if the scrolled items have reached the next threshold
        if (scrolledItems < nextThreshold) {
            val endIndex = minOf(nextThreshold, preLoadImages!!.size)
            val nextBatch = preLoadImages!!.subList(scrolledItems, endIndex)
            scrolledItems = endIndex

            // Send the next batch to your function
            sendBatchToFunction(nextBatch)
        }

        isLoading = false
    }

    private fun sendBatchToFunction(batch: List<String>) {
        // Your function to handle the batch of items
        Log.d("MyActivity", "Sending batch: $batch")
        // Implement your function here
        CoroutineScope(Dispatchers.Main).launch {
            preloadInBackground(batch)
        }
    }


    private suspend fun preloadInBackground(images: List<String>) {
        withContext(Dispatchers.IO) {
            preloadAllImages(images)
        }
    }

    private fun preloadAllImages(imagesUrls: List<String>) {

//        Log.d("IMAGES_SCROLL",""+GsonBuilder().setPrettyPrinting().create().toJson(imagesUrls))
        Log.d("IMAGES_SCROLL", "" + imagesUrls.size)


        for (url in imagesUrls!!) {
            preloadImage(url)
        }
    }

    private fun preloadImage(url: String) {
        Glide.with(this).load(url).diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Handle exceptions differently if you want
                    return true
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return true
                }
            }).preload()

    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        recyclerView?.visibility = View.GONE
        tvNoOrders?.visibility = View.VISIBLE
    }

    override fun callChild() {
        populateData()
    }

    //New
    override fun openFragment(position: Int, status_: String) {
        var status = status_
        var orderId: Int? = 0
        var orderName: String? = ""
        if (status == "-1") {

            val collectionRequestsData: CollectionRequests =
                combineList!!.get(position) as CollectionRequests

            status = "0"
            orderId = collectionRequestsData.id
            orderName = collectionRequestsData.request_number
        } else {

            val userOrdersData: UserOrders = combineList!!.get(position) as UserOrders

            orderId = userOrdersData.id
            orderName = userOrdersData.order_number
        }
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerOrderHistory,
            ContactUsFragment.newInstance(
                "" + orderId, orderName!!, status, ""
            ),
            Constants.TAGS.ContactUsFragment
        )
    }

    override fun callDialog(model: Any?) {
        if (model as Int == 2) {
            openDownloadedFileFolder()
        }
    }

    private fun openDownloadedFileFolder() {

        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    //Old
    /*override fun openFragment(position: Int, status_: Int) {
        var status = status_
        var orderId: Int? = 0
        var orderName: String? = ""
        if (status == -1) {
            status = 0
            orderId = userCollectionRequests?.get(position - userOrders?.size!!)?.id
            orderName = userCollectionRequests?.get(position - userOrders?.size!!)?.request_number
        } else {
            orderId = userOrders?.get(position)?.id
            orderName = userOrders?.get(position)?.order_number
        }
        BaseActivity.replaceFragment(
            childFragmentManager,
            Constants.Containers.containerOrderHistory,
            ContactUsFragment.newInstance(
                "" + orderId,
                orderName!!, status
            ),
            Constants.TAGS.ContactUsFragment
        )
    }*/
}
