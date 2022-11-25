package com.reloop.reloop.fragments


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.constants.EnvironmentalStatsDescriptionsIDs
import com.android.reloop.network.serializer.reports.ReportsParams
import com.android.reloop.utils.DecimalRemover
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.adapters.AdapterHomeRecyclerView
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.model.ModelHomeCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStats
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription
import com.reloop.reloop.network.serializer.reports.Reports
import com.reloop.reloop.network.serializer.reports.ReportsMain
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import kotlinx.android.synthetic.main.fragment_view_receipt.*
import retrofit2.Call
import retrofit2.Response
import java.text.DecimalFormat
import kotlin.math.floor


/**
 * A simple [Fragment] subclass.
 */
class ReportsFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse ,
    OnChartValueSelectedListener {

    var mFormat = DecimalFormat("###,###,##0")
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
    var filter: ImageButton? = null
    var dropDowns: LinearLayout? = null

    //    var todayFilter: Spinner? = null
    var organizationFilter: Spinner? = null

    var filterDay: TextView? = null
    var filterWeek: TextView? = null
    var filterMonth: TextView? = null
    var filterYear: TextView? = null
    var filter_label: TextView? = null
    var heading_pieChart: TextView? = null
    var tvCigaretteButts: TextView? = null
    var filterType = arrayOf("daily", "weekly", "monthly", "yearly")
    var barChartLabels: ArrayList<String> = ArrayList()
    var currentFilter = -1
    var reportsMain: ReportsMain = ReportsMain()
    var previous: ImageButton? = null
    var infoButton: ImageButton? = null
    var next: ImageButton? = null
    var no_data_tv: TextView? = null

    var address_id: Int? = null
    var filter_option: Int? = null
    var filterDate: String? = ""
    var filterSpinner = ""

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
            filterMonth?.performClick()
            populateOrganizationSpinnerData()
        }
    }

    private fun getReportsData(
        filter: String,
        date: String?,
        index: Int,
        filter_option: Int?,
        address_id: Int?
    ) {
        if (!NetworkCall.inProgress()) {
            currentFilter = index
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.REPORTS)
                ?.autoLoading(requireActivity())
//                ?.enque(Network().apis()?.getBarChartData(filter, date, filter_option, address_id))
                ?.enque(
                    Network().apis()?.getBarChartData(ReportsParams(filter, date, filter_option, address_id)))
                ?.execute()
            filterSpinner = filter
            filterDate = date
        }
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
        return view
    }

    private fun initViews(view: View?) {
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
        next = view?.findViewById(R.id.next)
        previous = view?.findViewById(R.id.previous)
        no_data_tv = view?.findViewById(R.id.no_data_tv)
        heading_pieChart = view?.findViewById(R.id.heading_pieChart)
        tvCigaretteButts = view?.findViewById(R.id.tvCigaretteButts)
        infoButton = view?.findViewById(R.id.info_button)
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
    }

    private fun populateData() {
//        populateTodaySpinnerData()
        if (userType == Constants.UserType.household) {
            organizationFilter?.visibility = View.GONE
            filter?.visibility = View.GONE
        }

    }

    private fun populateOrganizationSpinnerData() {
        val arrayList: ArrayList<String>? = ArrayList()
        arrayList?.clear()
        arrayList?.add("Select Filter")
        arrayList?.add("Total Households")
        arrayList?.add("Total Organizations")
        arrayList?.add("Total (organizations + households)")
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
//                val addressText =""+User.retrieveUser()?.addresses?.get(i)?.latitude+", "+ User.retrieveUser()?.addresses?.get(i)?.longitude
                arrayList?.add(addressText)
//                User.retrieveUser()?.addresses?.get(i)?.location?.let { arrayList?.add(it) }
            }
        }
        val setAdapter: ArrayAdapter<String> = ArrayAdapter(
            requireActivity(), R.layout.spinner_item_textview,
            arrayList!!
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
                if (position == 1) {
                    filter_option = Constants.FilterOption.HOUSEHOLD
                    address_id = null
                }
                if (position == 2) {
                    filter_option = Constants.FilterOption.ORGANIZATION
                    address_id = null
                }
                if (position == 3) {
                    filter_option = Constants.FilterOption.ALL
                    address_id = null
                }
                if (position > 3) {
                    filter_option = Constants.FilterOption.ADDRESS
                    var temArrayList = ArrayList<String>()
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
                        addressTextName =
                            "$unitNumberValue, $buildingName, $street, ${
                                User.retrieveUser()?.addresses?.get(
                                    i
                                )?.district?.name
                            }, ${User.retrieveUser()?.addresses?.get(i)?.city?.name}"
                        temArrayList.add(addressTextName)
                    }
                    // i is for increment and j is for addresses
                    for ((i, j) in temArrayList.withIndex()) {
                        if (j.equals(arrayList[position], true)) {
                            address_id = "${User.retrieveUser()?.addresses?.get(i)?.id}".toInt()
                            break
                        }
                    }
                    /*val address: Addresses? =
                        User.retrieveUser()?.addresses?.find {
                            it.building_name?.contains(
                                arrayList[position],
                                true
                            )!!
                        }
                    if (address != null) {
                        address_id = address.id
                    }*/
                    /*for (i in User.retrieveUser()?.addresses?.indices!!) {
                        if (i == arrayList[position].toInt()) {
                            address_id = "${User.retrieveUser()?.addresses?.get(i)?.id}".toInt()
                            break
                        }
                    }*/

                }

                if (position != 0) {
                    getReportsData(
                        filterSpinner,
                        filterDate, currentFilter, filter_option, address_id
                    )
                }
            }


        }
    }

/*    private fun populateTodaySpinnerData() {
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
                R.drawable.icon_tree,
                environmentalStats?.trees_saved!!,
                getString(R.string.unit_tree),
                getString(R.string.tree_heading), 2
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_barrel,
                environmentalStats.oil_saved!!,
                getString(R.string.unit_liters),
                getString(R.string.oil_saved_heading), 3
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_electricity,
                environmentalStats.electricity_saved!!,
                getString(R.string.unit_electricity),
                getString(R.string.electricity_saved_heading), 4
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_co2_emission,
                environmentalStats.co2_emission_reduced!!,
                getString(R.string.unit_kilograms),
                getString(R.string.co2_emission_heading), 1
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_landfill_space,
                environmentalStats.landfill_space_saved!!,
                getString(R.string.unit_ftcube),
                getString(R.string.landfill_heading), 6
            )
        )
        dataList.add(
            ModelHomeCategories(
                R.drawable.icon_water_drops,
                environmentalStats.water_saved!!,
                getString(R.string.unit_liters),
                getString(R.string.water_saved), 5
            )
        )
        linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)
        rvHomeCategories?.layoutManager = linearLayoutManager
        rvHomeCategories?.adapter = AdapterHomeRecyclerView(
            dataList,
            environmentalStatsDescriptions,
            activity
        )


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
                if (currentFilter != -1) {
                    getReportsData(
                        filterType[currentFilter],
                        reportsMain.reports?.header?.next,
                        currentFilter, filter_option, address_id
                    )
                }
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
        if (tag == RequestCodes.API.REPORTS) {
            reportsMain = Gson().fromJson(
                Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                ReportsMain::class.java
            )
            filter_label?.text = reportsMain.reports?.header?.heading
            tvCigaretteButts?.text = "${
                reportsMain.environmentalStats?.cigarette_butts_saved?.toInt().toString()
            } ${resources.getString(R.string.cigarette_butts)}"
            barChartSetting(reportsMain.reports)
            pieChartSetting(reportsMain.reports)
            previous?.visibility = View.VISIBLE
            next?.visibility = View.VISIBLE
            populateRecyclerViewData(
                reportsMain.environmentalStats,
                reportsMain.environmentalStatsDescriptions
            )
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }


    private fun barChartSetting(reports: Reports?) {
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
        xAxis.axisMinimum = 0f

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
//        barChart?.setFitBars(true)
        barChart?.animateXY(1000, 1000)
        barChart?.invalidate()
        barChart?.description?.text = ""
        barChart?.description?.isEnabled = false
//        barChart?.setViewPortOffsets(10f, 10f, 10f, 10f)
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
            barChartLabels.add("Sun")
            barChartLabels.add("Mon")
            barChartLabels.add("Tue")
            barChartLabels.add("Wed")
            barChartLabels.add("Thur")
            barChartLabels.add("Fri")
            barChartLabels.add("Sat")

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
            mFormat = DecimalFormat("#.##")

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
            heading_pieChart?.visibility = View.GONE
        }
        return entries
    }

    private fun getUnitFromValue(i: Int): String? {
        var value = ""
        if(i == 1)
        {
            value = "kg"
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
}
