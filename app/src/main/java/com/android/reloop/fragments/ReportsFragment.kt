package com.reloop.reloop.fragments


import android.app.DownloadManager
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.constants.EnvironmentalStatsDescriptionsIDs
import com.android.reloop.model.AddressLocations
import com.android.reloop.model.UserDependDetailsModel
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.android.reloop.network.serializer.reports.PieChartParams
import com.android.reloop.network.serializer.reports.PieChartParamsSpecific
import com.android.reloop.network.serializer.reports.PieChartParamsSpecificNew
import com.android.reloop.network.serializer.reports.ReportsParams
import com.android.reloop.network.serializer.reports.ReportsParamsNew
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.LoginActivity
import com.reloop.reloop.adapters.AdapterHomeRecyclerView
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.model.ModelHomeCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription
import com.reloop.reloop.network.serializer.organization.Organization
import com.reloop.reloop.network.serializer.reports.PieChartMain
import com.reloop.reloop.network.serializer.reports.Reports
import com.reloop.reloop.network.serializer.reports.ReportsMain
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.reloop.reloop.utils.ValueFormatterCustom
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor


/**
 * A simple [Fragment] subclass.
 */
class ReportsFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse ,
    OnChartValueSelectedListener, AlertDialogCallback {

    var mFormat = DecimalFormat("###,###,##0", DecimalFormatSymbols(Locale.US))
    companion object {
        fun newInstance(): ReportsFragment {
            return ReportsFragment()
        }
    }

    private var addressTextName: String = ""
    var userType = MainApplication.userType()

    var reportsFilter = 0
    var barChart: BarChart? = null
    var pieChart: PieChart? = null
    private lateinit var linearLayoutManager: LinearLayoutManager
    var rvHomeCategories: RecyclerView? = null
    var dataList: ArrayList<ModelHomeCategories> = ArrayList()

    var pdfDataList: ArrayList<ModelHomeCategories> = ArrayList()


    var environmentDesc: ArrayList<EnvironmentalStatsDescription> = ArrayList()
    var filter: ImageButton? = null
    var dropDowns: LinearLayout? = null
    var subdropDowns: LinearLayout? = null

    //    var todayFilter: Spinner? = null
    var organizationFilter: Spinner? = null
    var sublocationnFilter: Spinner? = null

    var listConnectedOrgs: ArrayList<Organization> = ArrayList()
    var useraddress: ArrayList<Addresses> = ArrayList()
    var subaddress: ArrayList<AddressLocations> = ArrayList()
    var subaddressname: ArrayList<String> = ArrayList()
    var finaladdress: ArrayList<String> = ArrayList()



    var txtConnectedOrgs: TextView? = null
    var txtConnectedHouseholds: TextView? = null
    var txtTotalKgs: TextView?= null

    var filterDay: TextView? = null
    var filterWeek: TextView? = null
    var filterMonth: TextView? = null
    var filterYear: TextView? = null
    var filter_label: TextView? = null
    var heading_pieChart: TextView? = null
    var heading_time_period: TextView? = null
    var txtClearFilterReport: TextView? = null

    var imgExportReport: ImageView? = null
    var imgDownloadPDF: ImageView? = null

    var tvCigaretteButts: TextView? = null
    var tvCoffeeCapsule: TextView? = null
    var tvSoapBars: TextView? = null

    var cardCoffeeCapsule: CardView?=null
    var cardSoapBars: CardView?=null


    var filterType = arrayOf("daily", "weekly", "monthly", "yearly")
    var barChartLabels: ArrayList<String> = ArrayList()
    var currentFilter = -1
    var reportsMain: ReportsMain = ReportsMain()
    var pieChartReportMain: PieChartMain = PieChartMain()

    var previous: ImageButton? = null
    var infoButton: ImageButton? = null

    var info_button_coffee_capsule: ImageButton? = null
    var info_button_soap_bars: ImageButton? = null

    var next: ImageButton? = null
    var no_data_tv: TextView? = null

    var address_id: Int? = null
    var sub_address_id: Int? = null

    var filter_option: Int? = null
    var filterDate: String? = ""
    var filterSpinner = ""

    var is_master_organization = false
    var isScreenOpened = false
    var isBarClicked = false

    var isFirstCall = false


    private var selectedYIndex: Float? = null

    private var selectedFilterName : String = ""

    val CHARTCOLORS = intArrayOf(
        ColorTemplate.rgb("#158DCC"),
        ColorTemplate.rgb("#158DCC"),
        ColorTemplate.rgb("#158DCC"),
        ColorTemplate.rgb("#158DCC")
    )
    val PIECHARTCOLORS = MainApplication.applicationContext().resources.getIntArray(R.array.rainbow)

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            currentFilter = -1

            isScreenOpened = false

            /*if(User.retrieveUser()?.organization?.is_master == 1){
                Log.d("IS_MASTER","True")
                defaultFilterForMasterOrg(2)//MONTHLY FOR MASTER ORG ONLY
            }else{
                Log.d("IS_MASTER","False")
                filterMonth?.performClick()
            }*/

            filterMonth?.performClick()
            populateOrganizationSpinnerData("")

            //uncomment this to show total KGs recycled without filter
            //txtTotalKgs!!.text = ""+tinyDB.getString("KGS_RECYCLED").toString()

            setVisibilityOfViews()

        }
    }

    private fun getDashboard() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DASHBOARD)
            //?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.dashboard())
            ?.execute()
    }

    private fun getReportsData(
        filter: String,
        date: String?,
        index: Int,
        filter_option: Int?,
        address_id: Int?) {

        if(filter_option!=null && filter_option == 5 && address_id!=null) {
            if (!NetworkCall.inProgress()) {
                isBarClicked = false
                currentFilter = index
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.REPORTS)
                    ?.autoLoading(requireActivity())
//                ?.enque(Network().apis()?.getBarChartData(filter, date, filter_option, address_id))
                    ?.enque(
                        Network().apis()?.getBarChartData(ReportsParams(filter, date, filter_option, address_id))
                    )
                    ?.execute()
                filterSpinner = filter
                filterDate = date
            }
        }
        else if(filter_option!=null && filter_option == 4 && address_id!=null)
        {
            if (!NetworkCall.inProgress()) {
                isBarClicked = false
                currentFilter = index
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.REPORTS)
                    ?.autoLoading(requireActivity())
//                ?.enque(Network().apis()?.getBarChartData(filter, date, filter_option, address_id))
                    ?.enque(
                        Network().apis()?.getBarChartDataNew(ReportsParamsNew(filter, date, filter_option, address_id,sub_address_id))
                    )
                    ?.execute()
                filterSpinner = filter
                filterDate = date
            }
        }

        else{
            if (!NetworkCall.inProgress()) {
                isBarClicked = false
                currentFilter = index
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.REPORTS)
                    ?.autoLoading(requireActivity())
//                ?.enque(Network().apis()?.getBarChartData(filter, date, filter_option, address_id))
                    ?.enque(
                        Network().apis()?.getBarChartData(ReportsParams(filter, date, filter_option, address_id))
                    )
                    ?.execute()
                filterSpinner = filter
                filterDate = date
            }
        }

    }

    private fun getPieChartData(
        filter: String,
        start: String,
        end: String,
        xAxisValue: String
    ) {

        isBarClicked = true

        //display date(time period) based on click of barchart
        if(filter.equals("daily")){
            heading_time_period?.text = "Day " + xAxisValue
        }else if(filter.equals("monthly")){
            heading_time_period?.text = "Month " + xAxisValue
        }else if(filter.equals("weekly")){
            heading_time_period?.text = "Week " + xAxisValue
        }else if(filter.equals("yearly")){
            heading_time_period?.text = "Year " + xAxisValue
        }


        //new CHANGE ========== START
        if(filter_option!=null && filter_option == 5 && address_id!=null){ //for Addresses in the dropdowns
            if (!NetworkCall.inProgress()) {
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.PIE_CHART)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.getPieChartDataSpecific(PieChartParamsSpecific(filter, start, end, filter_option,address_id)))
                    ?.execute()
            }
        }
        else if(filter_option!=null && filter_option == 4 && address_id!=null){ //for Addresses in the dropdowns
            if (!NetworkCall.inProgress()) {
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.PIE_CHART)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.getPieChartDataSpecificNew(PieChartParamsSpecificNew(filter, start, end, filter_option,address_id,sub_address_id)))
                    ?.execute()
            }
        }
        else{
            if (!NetworkCall.inProgress()) {
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.PIE_CHART)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.getPieChartData(PieChartParams(filter, start, end, filter_option)))
                    ?.execute()
            }
        }

        //new CHANGE ========== END

        //OLD HERE
        /*if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.PIE_CHART)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getPieChartData(PieChartParams(filter, start, end, filter_option)))
                ?.execute()
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_reports, container, false)
        initViews(view)
        setListeners()
        populateData()

        /*val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("shared preferences", MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("address", "")
        val type = object : TypeToken<ArrayList<Addresses>>() {}.type
        useraddress = gson.fromJson(json, type) as ArrayList<Addresses>*/

       // Log.e("useraddress"," === " + Gson().toJson(useraddress))
       // Log.e("useraddress"," === " + Gson().toJson(User.retrieveUser()!!.addresses))
        val prefs = requireContext().getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE)

        val gson = Gson()
        val json = prefs.getString("address_arr","testNoData")
        Log.e("useraddress json"," === " + json)
        if(json != null && json.isNotEmpty()) {
            useraddress = if(json.equals("testNoData")) {
                arrayListOf()
            } else{
                val type = object : TypeToken<ArrayList<Addresses>>() {}.type
                gson.fromJson(json, type) as ArrayList<Addresses>
            }
        }
        Log.d("TAG", "onSuccess: Address Arr saved in pref == "+ useraddress)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initViews(view: View?) {

        imgExportReport = view?.findViewById(R.id.imgExportReport)
        imgDownloadPDF = view?.findViewById(R.id.imgDownloadPDF)

        txtConnectedHouseholds = view?.findViewById(R.id.txtConnectedHouseholds)
        txtConnectedOrgs = view?.findViewById(R.id.txtConnectedOrgs)
        txtTotalKgs = view?.findViewById(R.id.txtTotalKgs)

        barChart = view?.findViewById(R.id.bar_chart)
        pieChart = view?.findViewById(R.id.pie_chart)
        rvHomeCategories = view?.findViewById(R.id.rvHomeCategories)
        filter = view?.findViewById(R.id.filter)
        dropDowns = view?.findViewById(R.id.dropDowns)
//        todayFilter = view?.findViewById(R.id.today_filter)
        organizationFilter = view?.findViewById(R.id.organization_filter)
        filterDay = view?.findViewById(R.id.filterDay)
        filterWeek = view?.findViewById(R.id.filterWeek)
        filterMonth = view?.findViewById(R.id.filterMonth)
        filterYear = view?.findViewById(R.id.filterYear)
        filter_label = view?.findViewById(R.id.filter_label)
        txtClearFilterReport = view?.findViewById(R.id.txtClearFilterReport)
        next = view?.findViewById(R.id.next)
        previous = view?.findViewById(R.id.previous)
        no_data_tv = view?.findViewById(R.id.no_data_tv)
        heading_pieChart = view?.findViewById(R.id.heading_pieChart)
        heading_time_period = view?.findViewById(R.id.heading_time_period)
        tvCigaretteButts = view?.findViewById(R.id.tvCigaretteButts)
        tvCoffeeCapsule = view?.findViewById(R.id.tvCoffeeCapsule)
        tvSoapBars = view?.findViewById(R.id.tvSoapBars)
        infoButton = view?.findViewById(R.id.info_button)

        info_button_coffee_capsule = view?.findViewById(R.id.info_button_coffee_capsule)
        info_button_soap_bars = view?.findViewById(R.id.info_button_soap_bars)

        cardCoffeeCapsule = view?.findViewById(R.id.cardCoffeeCapsule)
        cardSoapBars = view?.findViewById(R.id.cardSoapBars)

        subdropDowns = view?.findViewById(R.id.subdropDowns)
        sublocationnFilter = view?.findViewById(R.id.sp_sublocations)

        /*if (!NetworkCall.inProgress()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.USER_PROFILE_DEPENDENCIES)
                //?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.userProfiledependencies())
                ?.execute()
        }*/


        setVisibilityOfViews()
    }

    fun setVisibilityOfViews(){
        try {
            val tinyDB: TinyDB?
            tinyDB = TinyDB(MainApplication.applicationContext())
            val coffee_visibility = tinyDB.getString("coffee_visibility").toString()
            val soap_visibility = tinyDB.getString("soap_visibility").toString()

            val reportButtonHouseHold = tinyDB.getString("reports_button_household").toString()
            val reportButtonOrganization = tinyDB.getString("reports_button_organization").toString()

            Log.d("ISCALLED","111")

            if (MainApplication.userType() == Constants.UserType.household) {
                if(coffee_visibility.equals("VISIBLE")) { //VISIBLE
                    cardCoffeeCapsule!!.visibility = View.VISIBLE
                }else{
                    cardCoffeeCapsule!!.visibility = View.GONE
                }

                if(soap_visibility.equals("VISIBLE")) { //VISIBLE
                    cardSoapBars!!.visibility = View.VISIBLE
                }else{
                    cardSoapBars!!.visibility = View.GONE
                }

                if(reportButtonHouseHold.equals("VISIBLE")) { //VISIBLE
                    imgExportReport!!.visibility = View.VISIBLE
                    imgDownloadPDF!!.visibility = View.VISIBLE
                }else{
                    imgExportReport!!.visibility = View.GONE
                    imgDownloadPDF!!.visibility = View.GONE
                }
            }else{
                cardCoffeeCapsule!!.visibility = View.VISIBLE
                cardSoapBars!!.visibility = View.VISIBLE

                if(reportButtonOrganization.equals("VISIBLE")) { //VISIBLE
                    imgExportReport!!.visibility = View.VISIBLE
                    imgDownloadPDF!!.visibility = View.VISIBLE
                }else{
                    imgExportReport!!.visibility = View.GONE
                    imgDownloadPDF!!.visibility = View.GONE
                }
            }

        }catch (e: Exception){
            Log.d("ISCALLED","222")
            e.printStackTrace()
        }
    }


    private fun setListeners() {
        filter?.setOnClickListener(this)
        filterDay?.setOnClickListener(this)
        filterMonth?.setOnClickListener(this)
        filterYear?.setOnClickListener(this)
        filterWeek?.setOnClickListener(this)
        next?.setOnClickListener(this)
        previous?.setOnClickListener(this)
        infoButton?.setOnClickListener(this)

        info_button_coffee_capsule?.setOnClickListener(this)
        info_button_soap_bars?.setOnClickListener(this)


        txtClearFilterReport?.setOnClickListener(this)
        imgExportReport?.setOnClickListener(this)
        imgDownloadPDF?.setOnClickListener(this)

    }

    private fun populateData() {
        // populateTodaySpinnerData()
        if (userType == Constants.UserType.household) {
            organizationFilter?.visibility = View.GONE
            filter?.visibility = View.GONE

            dropDowns?.visibility = View.GONE //NEW AD - removed the filter button

            txtConnectedHouseholds?.visibility = View.GONE
            txtConnectedOrgs?.visibility = View.GONE

        }

    }

    private fun populateOrganizationSpinnerData(isClear: String) {
        val arrayList: ArrayList<String>? = ArrayList()
        arrayList?.clear()
        if(is_master_organization){
            arrayList?.add("Select Filter")
            arrayList?.add("Master Organization")
            arrayList?.add("Total Households")
            arrayList?.add("Total Branches")

            Log.e("arrayList listConnectedOrgs"," === " + Gson().toJson(listConnectedOrgs))
            //new Option added for connectedOrgs == AD change
            if(!listConnectedOrgs.isEmpty()){
                for(i in listConnectedOrgs.indices){
                    arrayList?.add(listConnectedOrgs.get(i).name.toString())
                }
            }

        }else{
            arrayList?.add("Select Filter")
            arrayList?.add("Total Households")
            arrayList?.add("Total Organizations")
            arrayList?.add("Total (organizations + households)")

            Log.e("arrayList useraddress"," === " + Gson().toJson(User.retrieveUser()?.addresses))
            Log.e("arrayList useraddress"," === " + Gson().toJson(User.retrieveUser()?.addresses?.size))
            //old ADDRESS in organization filter dropdown == AD change
            if (!User.retrieveUser()?.addresses.isNullOrEmpty()) {
                for (i in User.retrieveUser()?.addresses?.indices!!) {
                    var street = ""
                    var buildingName = ""
                    var unitNumberValue = ""
                    var district = ""
                    var city = ""
                    if (!User.retrieveUser()?.addresses?.get(i)?.building_name.isNullOrEmpty()) {
                        buildingName = "${User.retrieveUser()?.addresses?.get(i)?.building_name}"
                    }
                    if (!User.retrieveUser()?.addresses?.get(i)?.street.isNullOrEmpty()) {
                        street = "${User.retrieveUser()?.addresses?.get(i)?.street}"
                    }
                    if (!User.retrieveUser()?.addresses?.get(i)?.unit_number.isNullOrEmpty()) {
                        unitNumberValue = "${User.retrieveUser()?.addresses?.get(i)?.unit_number}"
                    }

                    if (!User.retrieveUser()?.addresses?.get(i)?.district?.name.isNullOrEmpty()) {
                        district = "${User.retrieveUser()?.addresses?.get(i)?.district?.name}"
                    }

                    if (!User.retrieveUser()?.addresses?.get(i)?.city?.name.isNullOrEmpty()) {
                        city = "${User.retrieveUser()?.addresses?.get(i)?.city?.name}"
                    }

                    if(buildingName.isNotEmpty() || street.isNotEmpty() || unitNumberValue.isNotEmpty() || district.isNotEmpty() || city.isNotEmpty()) {
                        val addressText = "$unitNumberValue, $buildingName, $street, ${district}, ${city}"
                        arrayList?.add(addressText)
                    }

                    Log.e("arrayList sub"," === " + arrayList)
                }
            }
        }


        finaladdress = arrayList!!
        Log.e("arrayList"," === " + arrayList)
        val setAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireActivity(), R.layout.spinner_item_textview,
            arrayList
        )
        setAdapter.setDropDownViewResource(R.layout.spinner_dropdown_textview)
        organizationFilter?.adapter = setAdapter
//        val onNet: OnNetworkResponse? = this
        organizationFilter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.e("selected pos"," === " + position)
                if(position == 0){
                    selectedFilterName = ""
                    subdropDowns!!.visibility = View.GONE
                    subaddress.clear()
                    subaddressname.clear()
                }

                if(is_master_organization){
                    if (position == 1) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()
                        filter_option = Constants.FilterOption.ORGANIZATION
                        address_id = null
                        subdropDowns!!.visibility = View.GONE
                    }
                    if (position == 2) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()
                        filter_option = Constants.FilterOption.HOUSEHOLD
                        address_id = null
                        subdropDowns!!.visibility = View.GONE
                    }
                    if (position == 3) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()
                        filter_option = Constants.FilterOption.TOTAL_BRANCHES
                        address_id = null
                        subdropDowns!!.visibility = View.GONE
                    }
                    /*if (position == 3) {
                        filter_option = Constants.FilterOption.ALL
                        address_id = null
                    }*/

                    //new Option added for connectedOrgs == AD change
                    if(position > 3){ //OLD WAS 2 before adding TOTAL_BRANCHES
                        filter_option = Constants.FilterOption.CONNECTED_ORGS
                        Log.e("addressID0"," === ")
                        subdropDowns?.visibility = View.GONE
                        for (i in listConnectedOrgs.indices){
                            if(parent!!.getItemAtPosition(position) == listConnectedOrgs.get(i).name){
                                selectedFilterName = listConnectedOrgs.get(i).name.toString()
                                address_id = listConnectedOrgs.get(i).id

                                break
                            }
                        }
                    }

                    if (position != 0) {
                        txtClearFilterReport!!.visibility = View.VISIBLE
                        getReportsData(filterSpinner, filterDate, currentFilter, filter_option, address_id)
                    }

                }
                else{ // OLD only part of else condition is there
                    if (position == 1) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()

                        filter_option = Constants.FilterOption.HOUSEHOLD
                        address_id = null

                        subaddress.clear()
                        subaddressname.clear()
                        subdropDowns!!.visibility = View.GONE


                    }
                    if (position == 2) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()

                        filter_option = Constants.FilterOption.ORGANIZATION
                        address_id = null

                        subaddress.clear()
                        subaddressname.clear()
                        subdropDowns!!.visibility = View.GONE

                    }
                    if (position == 3) {
                        selectedFilterName = parent?.getItemAtPosition(position).toString()

                        filter_option = Constants.FilterOption.ALL
                        address_id = null

                        subaddress.clear()
                        subaddressname.clear()
                        subdropDowns!!.visibility = View.GONE


                    }
                    //new Option added for connectedOrgs == AD change
                    if (position > 3) {
                        subaddress.clear()
                        subaddressname.clear()
                        filter_option = Constants.FilterOption.ADDRESS
                        var temArrayList = ArrayList<String>()
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
                            addressTextName =
                                "$unitNumberValue, $buildingName, $street, ${User.retrieveUser()?.addresses?.get(i)?.district?.name}, ${User.retrieveUser()?.addresses?.get(i)?.city?.name}"
                            temArrayList.add(addressTextName)
                        }
                        // i is for increment and j is for addresses
                        for ((i, j) in temArrayList.withIndex()) {
                            if (j.equals(arrayList[position], true)) {
                                address_id = "${User.retrieveUser()?.addresses?.get(i)?.id}".toInt()
                                sub_address_id = null
                                break
                            }
                        }
                    }

                    if (position != 0) {
                        txtClearFilterReport!!.visibility = View.VISIBLE
                        getReportsData(filterSpinner, filterDate, currentFilter, filter_option, address_id)

                        subaddress.clear()
                        subaddressname.clear()
                        subaddressname.add("Select sub location")
                        //for(j in User.retrieveUser()?.addresses?.indices!!)

                        if(!useraddress.isNullOrEmpty() && useraddress.size > 0) {
                            for (j in useraddress.indices) {
                                //val sublocations = User.retrieveUser()?.addresses?.get(j)?.addressLocations
                                val sublocations = useraddress.get(j).addressLocations
                                Log.e(
                                    "useraddress subloc list",
                                    " === " + Gson().toJson(sublocations)
                                )
                                if (!sublocations.isNullOrEmpty() && sublocations.size > 0) {
                                    for (k in sublocations.indices) {
                                        if (sublocations.get(k).address_id == address_id) {
                                            subaddress.add(sublocations.get(k))
                                            subaddressname.add(sublocations.get(k).location.toString())
                                        }
                                    }
                                }
                            }

                            Log.e("subaddressID0 list", " === " + Gson().toJson(subaddress))
                        }

                        if(!subaddress.isEmpty() && subaddress.size > 0 && position > 3)
                        {
                            Log.e("subaddressID0 list"," === " + Gson().toJson(position))

                            Handler().postDelayed(object : Runnable {
                                override fun run() {
                                    //Handler().postDelayed(this, 2000)
                                    subdropDowns!!.visibility = View.VISIBLE
                                    Log.e("subaddressID0 list"," === " + Gson().toJson(position))

                                }
                            }, 1000)

                            val setAdaptersub: ArrayAdapter<String> = ArrayAdapter(
                                requireActivity(), R.layout.spinner_item_textview,
                                subaddressname)
                            setAdaptersub.setDropDownViewResource(R.layout.spinner_dropdown_textview)
                            sublocationnFilter?.adapter = setAdaptersub
//        val onNet: OnNetworkResponse? = this
                            sublocationnFilter?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onNothingSelected(parent: AdapterView<*>?) {
                                }

                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    positionsub: Int,
                                    id: Long
                                ) {

                                    if(positionsub == 0)
                                    {
                                        txtClearFilterReport!!.visibility = View.VISIBLE
                                    }
                                    else {
                                        sub_address_id = subaddress.get(positionsub).id
                                        Log.e("subaddressID0", " === " + sub_address_id)

                                        txtClearFilterReport!!.visibility = View.VISIBLE
                                        getReportsData(
                                            filterSpinner,
                                            filterDate, currentFilter, filter_option, address_id
                                        )
                                        address_id = null
                                    }
                                }
                            }
                        }
                        else{
                            subdropDowns!!.visibility = View.GONE
                            txtClearFilterReport!!.visibility = View.VISIBLE
                            getReportsData(
                                filterSpinner,
                                filterDate, currentFilter, filter_option, address_id
                            )
                        }
                    }
                }
            }
        }

        if(is_master_organization){
            if(isClear.equals("clear")){
                organizationFilter!!.setSelection(0) // Clear filter

            }else{
                organizationFilter!!.setSelection(3) // FOR TOTAL BRANCHES
                subdropDowns?.visibility = View.GONE
            }
        }
    }

        /*private fun populateTodaySpinnerData() {
        val arrayList: ArrayList<String>? = ArrayList()
        arrayList?.clear()
        arrayList?.add("Today")
        arrayList?.add("Yesterday")
        arrayList?.add("2 Days Ago")
        val setAdapter: ArrayAdapter<String> = ArrayAdapter(
            activity!!, R.layout.spinner_item_textview,
            arrayList!!
        )
        setAdapter.setDropDownViewResource(R.layout.spinner_dropdown_textview)
        todayFilter?.adapter = setAdapter

    }*/


    private fun populateRecyclerViewData(
        environmentalStats: EnvironmentalStats?,
        environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>?
    ) {
        dataList.clear()
        dataList.add(
            ModelHomeCategories(
                "Saving",
                R.drawable.icon_tree_new,
                environmentalStats?.trees_saved!!,
                "",
                "Trees", EnvironmentalStatsDescriptionsIDs.TREES_SAVED_ID,
                "Trees Saved"
            )
        )
        dataList.add(
            ModelHomeCategories(
                "Saving",
                R.drawable.icon_barrel_new,
                environmentalStats.oil_saved,
                "L",
                "of Oil", EnvironmentalStatsDescriptionsIDs.OIL_SAVED_ID,
                "Oil Saved"
            )
        )
        dataList.add(
            ModelHomeCategories(
                "Saving",
                R.drawable.icon_electricity_new,
                environmentalStats.electricity_saved,
                "kwh",
                "of Electricity", EnvironmentalStatsDescriptionsIDs.ELECTRICITY_SAVED_ID,
                "Electricity Saved"
            )
        )
        dataList.add(
            ModelHomeCategories(
                "Reducing",
                R.drawable.icon_co2_emission_new,
                environmentalStats.co2_emission_reduced,
                "kgs",
                getString(R.string.co2_heading), EnvironmentalStatsDescriptionsIDs.CO2_EMISSION_REDUCED_ID,
                "Co2 Emission Reduced"
            )
        )
        dataList.add(
            ModelHomeCategories(
                "Saving",
                R.drawable.icon_landfill_space_new,
                environmentalStats.landfill_space_saved,
                getString(R.string.unit_ftcube),
                "of Landfill Space", EnvironmentalStatsDescriptionsIDs.LAND_FILL_SAVED_ID,
                "Landfill Space Saved"
            )
        )
        dataList.add(
            ModelHomeCategories(
                "Saving",
                R.drawable.icon_water_drops_new,
                environmentalStats.water_saved,
                "L",
                "of Water", EnvironmentalStatsDescriptionsIDs.WATER_SAVED_ID,
                "Water Saved"
                )
        )
        dataList.add(
            ModelHomeCategories(
                "Producing",
                R.drawable.icon_biofuel_new,
                environmentalStats.biodiesel_produced,
                "L",
                "of Biodiesel", EnvironmentalStatsDescriptionsIDs.BIODIESEL_PRODUCED_ID,
                "Biodiesel Produced"
            )
        )
        dataList.add(
            ModelHomeCategories(
                "Creating",
                R.drawable.ic_compost_new,
                environmentalStats.compost_created,
                "kgs",
                "of Compost", EnvironmentalStatsDescriptionsIDs.COMPOST_CREATED_ID,
                "Compost Created"
            )
        )

        environmentDesc.clear()
        environmentDesc = environmentalStatsDescriptions!!;

        linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)
        rvHomeCategories?.layoutManager = linearLayoutManager
        rvHomeCategories?.adapter = AdapterHomeRecyclerView(
            dataList,
            environmentDesc,
            activity
        )

        storePDFdataList(dataList,environmentalStats)

    }

    fun storePDFdataList(dataList: ArrayList<ModelHomeCategories>,environmentalStats: EnvironmentalStats?){
        pdfDataList.clear()
        pdfDataList.addAll(dataList)
        pdfDataList.add(
            ModelHomeCategories(
                "Supporting",
                R.drawable.certificate_icons_farming_land,
                environmentalStats!!.farming_land,
                getString(R.string.unit_ft2),
                "of Farming land", 12,
                "Farming Land"
            )
        )


    }

    fun addDatalist(
        environmentalStats: EnvironmentalStats?,
        environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>?) {

        Log.d("DATA0",""+GsonBuilder().setPrettyPrinting().create().toJson(environmentalStats))

        rvHomeCategories?.adapter!!.run {
            dataList.clear()
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_tree_new,
                    environmentalStats?.trees_saved!!,
                    getString(R.string.unit_tree),
                    getString(R.string.tree_heading), 2,
                    getString(R.string.tree_heading)
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_barrel_new,
                    environmentalStats.oil_saved!!,
                    getString(R.string.unit_liters),
                    getString(R.string.oil_saved_heading), 3,
                    getString(R.string.oil_saved_heading)
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_electricity_new,
                    environmentalStats.electricity_saved!!,
                    getString(R.string.unit_electricity),
                    getString(R.string.electricity_saved_heading), 4,
                    getString(R.string.electricity_saved_heading)
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Reducing",
                    R.drawable.icon_co2_emission_new,
                    environmentalStats.co2_emission_reduced!!,
                    getString(R.string.unit_kilograms),
                    getString(R.string.co2_emission_heading), 1,
                    getString(R.string.co2_emission_heading)
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_landfill_space_new,
                    environmentalStats.landfill_space_saved!!,
                    getString(R.string.unit_ftcube),
                    getString(R.string.landfill_heading), 6,
                    getString(R.string.landfill_heading)

                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Saving",
                    R.drawable.icon_water_drops_new,
                    environmentalStats.water_saved!!,
                    getString(R.string.unit_liters),
                    getString(R.string.water_saved), 5,
                    getString(R.string.water_saved)

                )
            )

            dataList.add(
                ModelHomeCategories(
                    "Producing",
                    R.drawable.icon_biofuel_new,
                    environmentalStats.biodiesel_produced,
                    getString(R.string.unit_liters),
                    getString(R.string.biodiesel_produced),
                    EnvironmentalStatsDescriptionsIDs.BIODIESEL_PRODUCED_ID,
                    getString(R.string.biodiesel_produced)
                )
            )
            dataList.add(
                ModelHomeCategories(
                    "Creating",
                    R.drawable.ic_compost_new,
                    environmentalStats.compost_created,
                    getString(R.string.unit_kilograms),
                    getString(R.string.compost_created),
                    EnvironmentalStatsDescriptionsIDs.COMPOST_CREATED_ID,
                    getString(R.string.compost_created)
                )
            )

            environmentDesc.clear()
            environmentDesc = environmentalStatsDescriptions!!;

            notifyDataSetChanged(); // notify changed

            storePDFdataList(dataList,environmentalStats)


        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.filter -> {
                if (filter?.drawable?.constantState == activity?.getDrawable(R.drawable.icon_filter_en)?.constantState) {
                    dropDowns?.visibility = View.GONE
                    filter?.setImageResource(R.drawable.icon_filter_un)
                } else {
                    dropDowns?.visibility = View.VISIBLE
                    filter?.setImageResource(R.drawable.icon_filter_en)
                }
            }

            R.id.txtClearFilterReport -> {
                //RESET filter and display MONTH data
                txtClearFilterReport!!.visibility = View.GONE
                currentFilter = -1
                selectedFilterName = ""

                filter_option = null
                address_id = null

                filterMonth?.performClick()
                populateOrganizationSpinnerData("clear")
            }

            R.id.filterDay -> {
                updateUI(0)
            }
            R.id.filterWeek -> {
                updateUI(1)

            }
            R.id.filterMonth -> {
                updateUI(2)
            }
            R.id.filterYear -> {
                updateUI(3)
            }
            R.id.next -> {

                try {
                    //Apply logic if "next" date bigger than "current date" than disable the next arrow button.
                    val nextDateString = reportsMain.reports!!.header.next
                    val format = "yyyy-MM-dd"
                    val nextDate = stringToDate(nextDateString.toString(), format)
                    if(nextDate!=null){
                        val isGreater = isDateGreaterThanCurrent(nextDate)
                        if(!isGreater){

                            //NEW HERE
                            if (currentFilter != -1) {
                                getReportsData(
                                    filterType[currentFilter],
                                    reportsMain.reports?.header?.next,
                                    currentFilter, filter_option, address_id
                                )
                            }
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }


                //ORIGINAL HERE
                /*if (currentFilter != -1) {
                    getReportsData(
                        filterType[currentFilter],
                        reportsMain.reports?.header?.next,
                        currentFilter, filter_option, address_id
                    )
                }*/
            }
            R.id.previous -> {
                if (currentFilter != -1) {
                    getReportsData(
                        filterType[currentFilter],
                        reportsMain.reports?.header?.previous,
                        currentFilter, filter_option, address_id
                    )
                }
            }
            R.id.info_button -> {
                showInfoPopup()
            }

            R.id.info_button_coffee_capsule ->{
                showInfoPopupCapsule()
            }

            R.id.info_button_soap_bars ->{
                showInfoPopupSoap()
            }

            R.id.imgExportReport -> {

                if(reportsMain.reports !=null){
                    exportReport(reportsMain.reports!!)
                }

            }

            R.id.imgDownloadPDF -> {

                //getDashboard()

                if(txtTotalKgs!!.text.toString().equals("0.0") || txtTotalKgs!!.text.toString().equals("0")){
                    Notify.alerterRed(activity, "No data found in selected range")
                }
                else{

                    if(isBarClicked){
                        BaseActivity.replaceFragment(childFragmentManager,
                            Constants.Containers.reportsFragmentContainer,
                            PdfDownloadFragment.newInstance(reportsMain.reports!!,pdfDataList,
                                pieChartReportMain.reports!!,
                                reportsMain.userProfile!!.reloop_client_logo,selectedFilterName,
                                heading_time_period!!.text.toString(), txtTotalKgs!!.text.toString()),
                            Constants.TAGS.PdfDownloadFragment)
                    }
                    else{
                        BaseActivity.replaceFragment(childFragmentManager,
                            Constants.Containers.reportsFragmentContainer,
                            PdfDownloadFragment.newInstance(reportsMain.reports!!,pdfDataList,reportsMain.reports!!.pieChartValues,
                                reportsMain.userProfile!!.reloop_client_logo,selectedFilterName,
                                heading_time_period!!.text.toString(),txtTotalKgs!!.text.toString()),
                            Constants.TAGS.PdfDownloadFragment)
                    }
                }
            }
        }
    }

    fun stringToDate(dateString: String, format: String): LocalDate? {
        val formatter = DateTimeFormatter.ofPattern(format)
        return try {
            LocalDate.parse(dateString, formatter)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isDateGreaterThanCurrent(date: LocalDate): Boolean {
        val currentDate = LocalDate.now()
        return date.isAfter(currentDate)
    }

    fun exportReport(reports: Reports) {
//        val hssfWorkbook = HSSFWorkbook()
        val hssfWorkbook = XSSFWorkbook()

        val hssfSheet = hssfWorkbook.createSheet("Summary")

        //---------------
        //---------------

        // Create a cell style with a specific background color
        /*val backgroundColorStyle = hssfWorkbook.createCellStyle()
        backgroundColorStyle.fillForegroundColor = IndexedColors.YELLOW.index
        backgroundColorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)*/


        //---------------
        //---------------

        //USER INFORMATION ========================START==============================
        val row0 = hssfSheet.createRow(0)
        val cellUserInfoTitle = row0.createCell(0)
        cellUserInfoTitle.setCellValue("User Information")



        val row1 = hssfSheet.createRow(1)
        if (!User.retrieveUser()?.first_name.isNullOrEmpty()){
            row1.createCell(0).setCellValue("Name")
            row1.createCell(1).setCellValue(User.retrieveUser()?.first_name.toString() + " " +
                    User.retrieveUser()?.last_name.toString())
        }

        val row2 = hssfSheet.createRow(2)
        if (MainApplication.userType() == Constants.UserType.household) {
            row2.createCell(0).setCellValue("Type")
            row2.createCell(1).setCellValue("Household")
        }else{
            row2.createCell(0).setCellValue("Type")
            row2.createCell(1).setCellValue("Organization")
        }

        val row3 = hssfSheet.createRow(3)
        if (!User.retrieveUser()?.email.isNullOrEmpty()){
            row3.createCell(0).setCellValue("Email")
            row3.createCell(1).setCellValue(User.retrieveUser()?.email.toString())
        }

        val row4 = hssfSheet.createRow(4)
        if (!User.retrieveUser()?.phone_number.isNullOrEmpty()){
            row4.createCell(0).setCellValue("Phone")
            row4.createCell(1).setCellValue(User.retrieveUser()?.phone_number.toString())
        }

        val row5 = hssfSheet.createRow(5)
        if (!User.retrieveUser()?.addresses.isNullOrEmpty()
            ||!User.retrieveUser()?.addresses?.get(0)?.street.isNullOrEmpty()){
            row5.createCell(0).setCellValue("Address")

//            row5.createCell(1).setCellValue(User.retrieveUser()?.addresses!!.get(0).location)

            try {
                row5.createCell(1).setCellValue(User.retrieveUser()?.addresses!!.get(0).unit_number +","+
                        User.retrieveUser()?.addresses!!.get(0).building_name +","+
                        User.retrieveUser()?.addresses!!.get(0).street +","+
                        User.retrieveUser()?.addresses!!.get(0).district!!.name +","+
                        User.retrieveUser()?.addresses?.get(0)?.city!!.name
                )
            }catch (e : Exception){
                e.printStackTrace()
            }



        }

        val row6 = hssfSheet.createRow(6)
        if(!selectedFilterName.isEmpty()){
            row6.createCell(0).setCellValue("Scope")
            row6.createCell(1).setCellValue(selectedFilterName)
        }
        //USER INFORMATION ========================END==============================



        //DATE START============================================
        val row8 = hssfSheet.createRow(8)
        row8.createCell(0).setCellValue("Date")
        row8.createCell(1).setCellValue(heading_time_period!!.text.toString())
        //DATE END============================================

        //TOTAL KG START============================================
        val row9 = hssfSheet.createRow(9)
        row9.createCell(0).setCellValue("Waste Recycled")
        row9.createCell(1).setCellValue(""+txtTotalKgs!!.text.toString() + " kgs" )
        //TOTAL KG END============================================



        //Material Composition ========================START==============================
        val materialComposTitle = hssfSheet.createRow(16)
        materialComposTitle.createCell(0).setCellValue("Material Composition")

        val hssfRowTitle = hssfSheet.createRow(17)
        hssfRowTitle.createCell(0).setCellValue("No.")
        hssfRowTitle.createCell(1).setCellValue("Material")
        hssfRowTitle.createCell(2).setCellValue("Data")
        hssfRowTitle.createCell(3).setCellValue("Unit")
        hssfRowTitle.createCell(4).setCellValue("Percentage")



        var row = 18
        var srNo = 1

        if(isBarClicked){
            if(!pieChartReportMain.reports!!.pieChartLabels.isEmpty()){
                var total = 0.0
                mFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))

                for (i in pieChartReportMain.reports!!.pieChartValues.indices) {

                    if(pieChartReportMain.reports!!.pieChartValues[i] > 0) {
                        total = total.plus(pieChartReportMain.reports!!.pieChartValues[i].toFloat())
                    }
                }

                //Material Composition
                for (a in pieChartReportMain.reports!!.pieChartLabels.indices) {
                    val hssfRow = hssfSheet.createRow(row)
                    hssfRow.createCell(0).setCellValue(srNo.toDouble()) // Index number
                    hssfRow.createCell(1).setCellValue(pieChartReportMain.reports!!.pieChartLabels.get(a)) // Material name
                    hssfRow.createCell(2).setCellValue(
                        pieChartReportMain.reports!!.pieChartValues.get(a).let { Utils.commaConversion(it) } // Data
                    )

                    hssfRow.createCell(3).setCellValue(
                        getUnitFromValue(
                            pieChartReportMain.reports!!.pieChartUnits.get(a)) // Unit
                    )

                    //FOR PERCENTAGE %
                    try {

                        if(pieChartReportMain.reports!!.pieChartValues[a] > 0) {
                            val percnt = (pieChartReportMain.reports!!.pieChartValues[a].toFloat() / total) * 100
                            val formattedpercnt = mFormat.format(percnt)
                            Log.e("TAG", "====percent====" + formattedpercnt)

                            hssfRow.createCell(4).setCellValue(""+formattedpercnt + "%") // Percentage %
                        }
                    } catch (e : IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    row++
                    srNo++
                }
            }
        }else{
            if(!reports.pieChartValues.pieChartLabels.isEmpty()){
                var total = 0.0
                mFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))

                for (i in reports.pieChartValues.pieChartValues.indices) {

                    if(reports.pieChartValues.pieChartValues[i] > 0) {
                        total = total.plus(reports.pieChartValues.pieChartValues[i].toFloat())
                    }
                }

                //Material Composition
                for (a in reports.pieChartValues.pieChartLabels.indices) {
                    val hssfRow = hssfSheet.createRow(row)
                    hssfRow.createCell(0).setCellValue(srNo.toDouble()) // Index number
                    hssfRow.createCell(1).setCellValue(reports.pieChartValues.pieChartLabels.get(a)) // Material name
                    hssfRow.createCell(2).setCellValue(
                        reports.pieChartValues.pieChartValues.get(a).let { Utils.commaConversion(it) } // Data
                    )

                    hssfRow.createCell(3).setCellValue(
                        getUnitFromValue(
                            reports.pieChartValues.pieChartUnits.get(a)) // Unit
                    )

                    //FOR PERCENTAGE %
                    try {

                        if(reports.pieChartValues.pieChartValues[a] > 0) {
                            val percnt = (reports.pieChartValues.pieChartValues[a].toFloat() / total) * 100
                            val formattedpercnt = mFormat.format(percnt)
                            Log.e("TAG", "====percent====" + formattedpercnt)

                            hssfRow.createCell(4).setCellValue(""+formattedpercnt + "%") // Percentage %
                        }
                    } catch (e : IndexOutOfBoundsException) {
                        e.printStackTrace()
                    }

                    row++
                    srNo++
                }

            }
        }
        //Material Composition ========================END==============================


        //Environmental Stats ========================START=============================
        row+=3
        val envstatsTitle = hssfSheet.createRow(row)
        envstatsTitle.createCell(0).setCellValue("Environmental Stats")

        row+=1
        val envStatsHeading = hssfSheet.createRow(row)
        envStatsHeading.createCell(0).setCellValue("No.")
        envStatsHeading.createCell(1).setCellValue("Title")
        envStatsHeading.createCell(2).setCellValue("Data")
        envStatsHeading.createCell(3).setCellValue("Unit")


        var rowEnvStats = row+1
        var srNoEnvStats = 1
        if(!dataList.isEmpty()){

            //Material Composition
            for (a in dataList.indices) {
                val hssfRow = hssfSheet.createRow(rowEnvStats)
                hssfRow.createCell(0).setCellValue(srNoEnvStats.toDouble()) // Index number
                hssfRow.createCell(1).setCellValue(dataList.get(a).headingReport) // Title
                hssfRow.createCell(2).setCellValue(
                    dataList.get(a).points?.let { Utils.commaConversion(it) }
                ) // env points
                hssfRow.createCell(3).setCellValue(dataList.get(a).unit) // Unit


                rowEnvStats++
                srNoEnvStats++
            }

            //for coffee capsules and soap bars ====================START===================

            val rowCoffee = hssfSheet.createRow(rowEnvStats)
            rowCoffee.createCell(0).setCellValue(srNoEnvStats.toDouble()) // Index number
            rowCoffee.createCell(1).setCellValue("Coffee capsules saved") // Title
            rowCoffee.createCell(2).setCellValue(reportsMain.environmentalStats?.coffee_capsule?.toInt().toString()) // Title

            rowEnvStats++
            srNoEnvStats++

            val rowSoap = hssfSheet.createRow(rowEnvStats)
            rowSoap.createCell(0).setCellValue(srNoEnvStats.toDouble()) // Index number
            rowSoap.createCell(1).setCellValue("Soap bars saved") // Title
            rowSoap.createCell(2).setCellValue(reportsMain.environmentalStats?.soap_bars?.toInt().toString()) // Title
            //for coffee capsules and soap bars ====================END===================



        }
        //Environmental Stats ========================END=============================




        hssfSheet.setColumnWidth(0,4500)
        hssfSheet.setColumnWidth(1,8000)
        hssfSheet.setColumnWidth(2,3000)


        //---------
        //---------
        val path: File =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator)
        val file = File(path.toString())
        file.mkdirs()
        val fileName: String = path.toString() + "/" + "reloop_" + System.currentTimeMillis() + ".xlsx"
        try {
            val fileOutputStream = FileOutputStream(fileName)
            hssfWorkbook.write(fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

//            Toast.makeText(requireContext(),"Report Generated. \n $path",Toast.LENGTH_SHORT).show()
/*            Notify.alerterGreen(
                requireActivity(),
                "Report downloaded in download folder"
            )*/
            Notify.hyperlinkAlertGreen(
                activity,
                "Report downloaded in download folder",
                "Open",
                this, 2
            )


        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun openDownloadedFileFolder() {

        try {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun showInfoPopup() {
        val envStatsDescList = reportsMain.environmentalStatsDescriptions
        if (envStatsDescList != null) {
            for ((i, desc) in envStatsDescList.withIndex()) {
                if (envStatsDescList[i].id == EnvironmentalStatsDescriptionsIDs.CIGARETTE_BUTT_SAVED_ID) {
                    AlertDialogs.informationDialog(activity, "", desc.title, desc.description, R.drawable.cigarette_butt)
                    break
                }
            }
        }
    }

    private fun showInfoPopupCapsule() {
        val envStatsDescList = reportsMain.environmentalStatsDescriptions
        if (envStatsDescList != null) {
            for ((i, desc) in envStatsDescList.withIndex()) {
                if (envStatsDescList[i].id == EnvironmentalStatsDescriptionsIDs.COFFEE_CAPSULE_ID) {
                    AlertDialogs.informationDialog(activity, "", desc.title, desc.description, R.drawable.coffee_capsule)
                    break
                }
            }
        }
        }

    private fun showInfoPopupSoap() {
        val envStatsDescList = reportsMain.environmentalStatsDescriptions
        if (envStatsDescList != null) {
            for ((i, desc) in envStatsDescList.withIndex()) {
                if (envStatsDescList[i].id == EnvironmentalStatsDescriptionsIDs.SOAP_BAR_ID) {
                    AlertDialogs.informationDialog(activity, "", desc.title, desc.description, R.drawable.soap_bar)
                    break
                }
            }
        }
    }

    private fun defaultFilterForMasterOrg(i: Int){
        filterDay?.background = null
        filterDay?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        filterWeek?.background = null
        filterWeek?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        filterMonth?.background = null
        filterMonth?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        filterYear?.background = null
        filterYear?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        reportsFilter = Constants.ReportsFilter.Month



        selectedFilterName = "Total Branches"
        filter_option = Constants.FilterOption.TOTAL_BRANCHES
        address_id = null

        getReportsData(filterType[i], "", i, filter_option, address_id)

        filterMonth?.background = activity?.getDrawable(R.drawable.button_shape)
        filterMonth?.setTextColor(requireActivity().getColor(R.color.colorWhite))


    }

    private fun updateUI(i: Int) {
        filterDay?.background = null
        filterDay?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        filterWeek?.background = null
        filterWeek?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        filterMonth?.background = null
        filterMonth?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        filterYear?.background = null
        filterYear?.setTextColor(requireActivity().getColor(R.color.color_chart_heading_change))

        when (i) {
            0 -> {
                reportsFilter = Constants.ReportsFilter.Daily
                if (currentFilter != i)
                    getReportsData(filterType[i], "", i, filter_option, address_id)
                filterDay?.background = activity?.getDrawable(R.drawable.button_shape)
                filterDay?.setTextColor(requireActivity().getColor(R.color.colorWhite))

            }
            1 -> {
                reportsFilter = Constants.ReportsFilter.Week
                if (currentFilter != i)
                    getReportsData(filterType[i], "", i, filter_option, address_id)
                filterWeek?.background = activity?.getDrawable(R.drawable.button_shape)
                filterWeek?.setTextColor(requireActivity().getColor(R.color.colorWhite))
            }
            2 -> {
                reportsFilter = Constants.ReportsFilter.Month
                if (currentFilter != i)
                    getReportsData(filterType[i], "", i, filter_option, address_id)
                filterMonth?.background = activity?.getDrawable(R.drawable.button_shape)
                filterMonth?.setTextColor(requireActivity().getColor(R.color.colorWhite))
            }
            3 -> {
                reportsFilter = Constants.ReportsFilter.Year
                if (currentFilter != i) getReportsData(filterType[i], "", i, filter_option, address_id)
                filterYear?.background = activity?.getDrawable(R.drawable.button_shape)
                filterYear?.setTextColor(requireActivity().getColor(R.color.colorWhite))
            }
        }
    }


    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        if (tag == RequestCodes.API.USER_PROFILE_DEPENDENCIES){
            try {

                var userprofiledata = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    UserDependDetailsModel::class.java
                )
                Log.e("userprofile : ",Gson().toJson(userprofiledata))


                val userModel = userprofiledata!!.userProfile
                userModel?.addresses =  userprofiledata.userProfile?.addresses

                Log.e("Report Editprofile userModel : ",Gson().toJson(userModel))
                if (userModel != null) {
                    try {
                        userModel.save(userModel, context,false)

                    } catch (e: Exception) {
                        Log.e("Edit Profile", e.toString())
                        activity?.onBackPressed()
                    }
                }

            } catch (e: Exception) {
                Log.e("Edit Profile", e.toString())
                activity?.onBackPressed()
            }
        }
        else if (tag == RequestCodes.API.REPORTS) {
            reportsMain = Gson().fromJson(
                Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                ReportsMain::class.java
            )
            filter_label?.text = reportsMain.reports?.header?.heading

            //Pie Chart date on top
            heading_time_period?.text = reportsMain.reports?.header?.heading

            tvCigaretteButts?.text = "${
                reportsMain.environmentalStats?.cigarette_butts_saved?.toInt().toString()
            } ${resources.getString(R.string.cigarette_butts)}"

            tvCoffeeCapsule?.text = "${
                reportsMain.environmentalStats?.coffee_capsule?.toInt().toString()
            } ${resources.getString(R.string.coffee_capsule)}"

            tvSoapBars?.text = "${
                reportsMain.environmentalStats?.soap_bars?.toInt().toString()
            } ${resources.getString(R.string.soap_bars)}"




            if(!reportsMain.connectedHouseholdAccounts.isNullOrEmpty()){
                txtConnectedHouseholds!!.text = "Connected households: "+ reportsMain.connectedHouseholdAccounts.toString()
            }

            if(!reportsMain.connectedOrgsAccounts.isNullOrEmpty()){
                txtConnectedOrgs!!.text = "Connected organizations: "+ reportsMain.connectedOrgsAccounts.toString()
            }



            barChartSetting(reportsMain.reports)
            pieChartSetting(reportsMain.reports)
            previous?.visibility = View.VISIBLE
            next?.visibility = View.VISIBLE
            populateRecyclerViewData(
                reportsMain.environmentalStats,
                reportsMain.environmentalStatsDescriptions
            )

            //check if is_master = 1 for organization account AD
            try {
                if(reportsMain.userProfile!=null && reportsMain.userProfile!!.organization!=null){
                    if(reportsMain.userProfile!!.organization!!.is_master == 1){
                        is_master_organization = true
                        listConnectedOrgs = ArrayList()

                        //check if connectedOrgs exists
                        if(reportsMain.connectedOrgs!=null && !reportsMain.connectedOrgs!!.isEmpty()){

                            for (i in reportsMain.connectedOrgs!!.indices) {
                                val organization = Organization()
                                organization.id = reportsMain.connectedOrgs!!.get(i).id
                                organization.name = reportsMain.connectedOrgs!!.get(i).organization!!.name
                                Log.d("listConnectedOrgs name","====" + organization.name)

                                listConnectedOrgs.add(organization)
                            }
                        }
                    } else{
                        is_master_organization = false
                    }
                }

                //NEW CONDITION ADDED
                if(!isScreenOpened){
                    isScreenOpened = true
                    Log.d("IS_FIRST","111")
                    populateOrganizationSpinnerData("")
                }


            }catch (e: Exception){
                e.printStackTrace()
            }


        }
        else if (tag == RequestCodes.API.PIE_CHART) {
            pieChartReportMain = Gson().fromJson(
                Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                PieChartMain::class.java
            )
            //update pie chart on click of bar chart
            newPieChartSetting(pieChartReportMain.reports!!)


            //update environmental stats
            addDatalist(
                pieChartReportMain.environmentalStats,
                pieChartReportMain.environmentalStatsDescriptions
            )

        }
        else if(tag == RequestCodes.API.DASHBOARD)
        {
                try {
                    val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dashboard::class.java)
                    Log.e("TAG","===response2===" + dashboard)

                    val userModel = User.retrieveUser()

                    userModel?.first_name = dashboard.userProfile?.first_name
                    userModel?.last_name = dashboard.userProfile?.last_name
                    userModel?.organization?.name =  dashboard.userProfile?.hh_organization_name



                    Log.e("TAG","===response===" + userModel?.first_name)
                    Log.e("TAG","===response===" + userModel?.last_name)

                    userModel?.save(userModel, context,false)

                    if(txtTotalKgs!!.text.toString().equals("0.0") || txtTotalKgs!!.text.toString().equals("0")){
                        Notify.alerterRed(activity, "No data found in selected range")
                    }
                    else{

                        if(isBarClicked){
                            BaseActivity.replaceFragment(childFragmentManager,
                                Constants.Containers.reportsFragmentContainer,
                                PdfDownloadFragment.newInstance(reportsMain.reports!!,pdfDataList,
                                    pieChartReportMain.reports!!,
                                    reportsMain.userProfile!!.reloop_client_logo,selectedFilterName,
                                    heading_time_period!!.text.toString(), txtTotalKgs!!.text.toString()),
                                Constants.TAGS.PdfDownloadFragment)
                        }
                        else{
                            BaseActivity.replaceFragment(childFragmentManager,
                                Constants.Containers.reportsFragmentContainer,
                                PdfDownloadFragment.newInstance(reportsMain.reports!!,pdfDataList,reportsMain.reports!!.pieChartValues,
                                    reportsMain.userProfile!!.reloop_client_logo,selectedFilterName,
                                    heading_time_period!!.text.toString(),txtTotalKgs!!.text.toString()),
                                Constants.TAGS.PdfDownloadFragment)
                        }
                    }

                } catch (e: Exception) {
                    Log.e("Home Fragment", e.toString())
                }
            }

    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    private fun setTotalKgsRecycled(reports: Reports?){
        var totalPie = 0f

        for (index in reports!!.barChartValues.indices){
            totalPie += reports!!.barChartValues.get(index).yAxisValue
        }

        val formattedTotalPie = String.format("%.2f", totalPie)

        txtTotalKgs!!.text = ""+formattedTotalPie.toString()
    }


    private fun barChartSetting(reports: Reports?) {

        Log.d("BAR_DATA",""+GsonBuilder().setPrettyPrinting().create().toJson(reports?.barChartValues))

        setTotalKgsRecycled(reports)

        val barDataSet = BarDataSet(getData(reports?.barChartValues), "")
//        barDataSet.barBorderWidth = 1f
        barDataSet.setColors(*CHARTCOLORS)
        barDataSet.formSize = 0f

        val barData = BarData(barDataSet)
//        barData.barWidth=10f
        val xAxis: XAxis = barChart?.xAxis!!
        val yAxisLeft: YAxis? = barChart?.axisLeft
        yAxisLeft?.axisMinimum = 0f
        yAxisLeft?.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String? {
//                val precision = DecimalFormat("0.00")
//                return (floor(precision.format(value).toDouble()).toString())
                return (floor(value.toDouble()).toInt()).toString()
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM

//        xAxis.axisMinimum = 0f //OLD
        xAxis.axisMinimum = -0.5f //NEW (to fix the first bar chart cut)


        val formatter = IndexAxisValueFormatter(barChartLabels)
        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter
//        barChart?.axisLeft?.spaceTop=10f
//        xAxis.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String? {
//                return (floor(value.toDouble()).toInt()).toString()
//            }
//        }
        barChart?.data = barData
        barChart?.axisRight?.isEnabled = false
//        barChart?.setFitBars(true) // originaly not added
        barChart?.animateXY(1000, 1000)
        barChart?.invalidate()
        barChart?.description?.text = ""
        barChart?.description?.isEnabled = false
//        barChart?.setViewPortOffsets(10f, 10f, 10f, 10f)


        //bar chart click AD
        barChart?.setOnChartValueSelectedListener(object:OnChartValueSelectedListener{
            override fun onValueSelected(e: Entry?, h: Highlight?) {

                if(e==null){
                    return
                }

                if(h==null){
                    return
                }

                //We check for Selected Y Index is non-null (user tapped on non-filled or filled area of bar)
                //Also, we check for selected highlight is non-null and whether user tapped filled area of bar
                if (selectedYIndex != null && h.yPx <= selectedYIndex!!) {
                    // User tapped on filled bar area

                    try {
                        Log.d("BAR_DATA",
                            "Value: " + e.getY() +
                                    ", xIndex: " + e.getX() +
                                    ", DataSet index: " + h.getDataSetIndex());

                        val xAxisValue: String = barChart!!.getXAxis().getValueFormatter()
                            .getFormattedValue(e.x, barChart!!.getXAxis())

                        txtTotalKgs!!.text = ""+e.getY().toString()



                        val index = String.format("%.0f", e.getX())

                        val startDate = reportsMain.reports?.barChartValues?.get(index.toInt())?.barChartData?.start

                        if (reportsFilter == Constants.ReportsFilter.Daily) {
                            Log.d("BAR_DATA","Start DAILY => " + startDate)

                            getPieChartData("daily",startDate.toString(),"",xAxisValue)

                        }else if(reportsFilter == Constants.ReportsFilter.Month){
                            Log.d("BAR_DATA","Start MONTHLY => " + startDate)

                            val cal: Calendar = Calendar.getInstance()
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                            cal.setTime(sdf.parse(startDate))

                            cal.add(Calendar.MONTH, 1);
                            cal.set(Calendar.DAY_OF_MONTH, 1);
                            cal.add(Calendar.DATE, -1);

                            val endDate = sdf.format(cal.getTime());

                            Log.d("BAR_DATA","END DATE => " + endDate)
                            getPieChartData("monthly",startDate.toString(),endDate.toString(),xAxisValue)


                        }else if(reportsFilter == Constants.ReportsFilter.Week){
                            Log.d("BAR_DATA","Start WEEKLY => " + startDate)

                            val cal: Calendar = Calendar.getInstance()
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                            cal.setTime(sdf.parse(startDate))

                            cal.add(Calendar.DATE, 6);
                            val endDate = sdf.format(cal.getTime());

                            Log.d("BAR_DATA","END DATE => " + endDate)
                            getPieChartData("weekly",startDate.toString(),endDate.toString(),xAxisValue)


                        }else if(reportsFilter == Constants.ReportsFilter.Year){
                            Log.d("BAR_DATA","Start YEARLY => " + startDate)

                            val cal: Calendar = Calendar.getInstance()
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                            cal.setTime(sdf.parse(startDate))

                            cal.add(Calendar.YEAR, 0) //0 gives you the current year from the date you passed, 1 gives you next year, -1 gives you previous year
                            cal.set(Calendar.DAY_OF_YEAR, 1)
                            cal.set(Calendar.MONTH, 11)
                            cal.set(Calendar.DAY_OF_MONTH, 31)
                            val endDate = sdf.format(cal.getTime());

                            Log.d("BAR_DATA","END DATE => " + endDate)
                            getPieChartData("yearly",startDate.toString(),endDate.toString(),xAxisValue)

                        }

                        txtClearFilterReport!!.visibility = View.VISIBLE

                    }catch (e: Exception){
                        Log.d("BAR_DATA"," Crash : "+e.printStackTrace())
                        e.printStackTrace()
                    }
                }
            }

            override fun onNothingSelected() {

            }

        })


        barChart?.setOnChartGestureListener(object : OnChartGestureListener {
            override fun onChartGestureStart(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartGestureEnd(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {
            }

            override fun onChartLongPressed(me: MotionEvent?) {
            }

            override fun onChartDoubleTapped(me: MotionEvent?) {
            }

            override fun onChartSingleTapped(me: MotionEvent?) {
                // Touched chart entry
                val entry: Entry? = barChart?.getEntryByTouchPoint(me!!.x, me.y)
                selectedYIndex = if (entry != null) {
                    //Entry is non-null which means user clicked on bar area (filled or non-filled)
                    me!!.y
                } else {
                    //Entry is null which means user clicked outside of bar area
                    null
                }
            }

            override fun onChartFling(
                me1: MotionEvent?,
                me2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ) {
            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
            }

            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            }

        })


    }

    private fun getData(barChartValues: ArrayList<com.reloop.reloop.network.serializer.reports.BarChart>?): ArrayList<BarEntry> {
        val entries: ArrayList<BarEntry> = ArrayList()
        barChartLabels = ArrayList()
        if (barChartValues != null) {
            for (i in barChartValues.indices) {
                entries.add(BarEntry(i.toFloat(), barChartValues[i].yAxisValue))
                barChartValues[i].label?.let { barChartLabels.add(it) }
            }
        }
        if (reportsFilter == Constants.ReportsFilter.Daily) {
            barChartLabels.clear()
            barChartLabels.add("Mon")
            barChartLabels.add("Tue")
            barChartLabels.add("Wed")
            barChartLabels.add("Thur")
            barChartLabels.add("Fri")
            barChartLabels.add("Sat")
            barChartLabels.add("Sun")


        }
        return entries
    }

    private fun pieChartSetting(reports: Reports?) {
        val pieDataSet = PieDataSet(setPieChartData(reports?.pieChartValues), "")
        pieDataSet.setColors(*PIECHARTCOLORS)
        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(ValueFormatterCustom(pieChart))
        pieChart?.setUsePercentValues(true)
        pieChart?.data = pieData

        val l: Legend = pieChart?.getLegend()!!
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.isWordWrapEnabled = true
        l.xEntrySpace = 20f
        l.setDrawInside(false)

        pieChart?.data?.setValueTextSize(9f)
        pieChart?.setMinAngleForSlices(10f)
        pieChart?.description?.text = ""
        pieChart?.setEntryLabelColor(Color.WHITE)
        pieChart?.setEntryLabelTextSize(12f)
        pieChart?.data?.setValueTextColor(Color.WHITE)
        pieChart?.animateXY(1000, 1000)
        //pieChart?.legend?.orientation = Legend.LegendOrientation.VERTICAL
        //pieChart?.legend?.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        pieChart?.setDrawEntryLabels(false)
        pieChart?.invalidate()
        pieChart?.setOnChartValueSelectedListener(this)
    }

    private fun newPieChartSetting(reports: com.reloop.reloop.network.serializer.reports.PieChart) {
        val pieDataSet = PieDataSet(setPieChartData(reports), "")
        pieDataSet.setColors(*PIECHARTCOLORS)
        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(ValueFormatterCustom(pieChart))
        pieChart?.setUsePercentValues(true)
        pieChart?.data = pieData

        val l: Legend = pieChart?.getLegend()!!
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.isWordWrapEnabled = true
        l.xEntrySpace = 20f
        l.setDrawInside(false)

        pieChart?.data?.setValueTextSize(9f)
        pieChart?.setMinAngleForSlices(10f)
        pieChart?.description?.text = ""
        pieChart?.setEntryLabelColor(Color.WHITE)
        pieChart?.setEntryLabelTextSize(12f)
        pieChart?.data?.setValueTextColor(Color.WHITE)
        pieChart?.animateXY(1000, 1000)
        //pieChart?.legend?.orientation = Legend.LegendOrientation.VERTICAL
        //pieChart?.legend?.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
        pieChart?.setDrawEntryLabels(false)
        pieChart?.invalidate()
        pieChart?.setOnChartValueSelectedListener(this)
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.d("PieChart", e?.y.toString())
        Log.d("PieChart", (e as PieEntry).label)
        //pieChart?.setEntryLabelColor(Color.WHITE)
    }

    override fun onNothingSelected() {

    }

    private fun setPieChartData(pieChartValues: com.reloop.reloop.network.serializer.reports.PieChart?): ArrayList<PieEntry> {

        val entries: ArrayList<PieEntry> = ArrayList()
        if (pieChartValues?.pieChartValues?.size!! > 0) {
            pieChart?.visibility = View.VISIBLE
            heading_pieChart?.visibility = View.VISIBLE

            heading_time_period?.visibility = View.VISIBLE

            no_data_tv?.visibility = View.GONE

          /*  entries.add(PieEntry(0.1f))
            entries.add(PieEntry(0.1f))
            entries.add(PieEntry(0.1f))
            entries.add(PieEntry(61f))
            entries.add(PieEntry(24f))*/
            //entries.add(PieEntry(0.5f))
            //entries.add(PieEntry(0.6f))
            //entries.add(PieEntry(0.7f))

            var total = 0.0
            //  mFormat = DecimalFormat("###,###,##0")
            mFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))

            for (i in pieChartValues.pieChartValues.indices) {

                if(pieChartValues.pieChartValues[i] > 0) {
                    total = total.plus(pieChartValues.pieChartValues[i].toFloat())
                }
            }
            Log.e("TAG","====total====" + total)

            try {
                for (i in pieChartValues.pieChartValues.indices) {

                    if(pieChartValues.pieChartValues[i] > 0) {

                    val percnt = (pieChartValues.pieChartValues[i].toFloat() / total) * 100

                    val formattedpercnt = mFormat.format(percnt)

                    Log.e("TAG", "====percent====" + formattedpercnt)
                    //entries.add(PieEntry(pieChartValues.pieChartValues[i].toFloat(), pieChartValues.pieChartLabels[i]))

                    entries.add(
                        PieEntry(
                            pieChartValues.pieChartValues[i].toFloat(),
                            pieChartValues.pieChartLabels[i] + " (" + pieChartValues.pieChartValues[i].toFloat() + getUnitFromValue(
                                pieChartValues.pieChartUnits[i]) + " - " + formattedpercnt + "%" + ")"))
                    }
                }
            }
            catch (e : IndexOutOfBoundsException)
            {
                e.printStackTrace()
            }

        } else {
            no_data_tv?.visibility = View.VISIBLE
            pieChart?.visibility = View.GONE
//            heading_pieChart?.visibility = View.GONE

            heading_time_period?.visibility = View.GONE

        }
        return entries
    }


    private fun getUnitFromValue(i: Int): String? {
        var value = ""
        if(i == 1)
        {
            value = "kgs"
        }
        else if(i == 2)
        {
            value = "liter"
        }
        else if(i == 3)
        {
            value = "pieces"
        }
        return value
    }

    override fun callDialog(model: Any?) {
        if (model as Int == 2) {
            openDownloadedFileFolder()
        }

    }
}
