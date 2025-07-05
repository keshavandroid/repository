package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.CollectionRequest
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.UserPlans
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.DistrictWithOrder
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.*
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class SelectionDayFragment : BaseFragment(), ParentToChild, AlertDialogCallback, OnNetworkResponse {

    var todayDate = ""
    var lastDateofmonth = ""
    var lastdayofmonth = ""
    var collectiontime = ""

    private var msg: String =""
    val EVENTTAG = "Day Selection Page"
    var childToParent: ChildToParent? = null
    var selectedDate = ""
    var oderDays: ArrayList<String>? = ArrayList()
    /* var sameDate = ""
     var nextDate = ""*/
    var defaultAddress: Addresses? = null
    var user: User? =
        User()

    @SuppressLint("SimpleDateFormat")
    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val dateFormatNew = "yyyy-MM-dd"
    var message: TextView? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ChildToParent) {
            childToParent = parentFragment as? ChildToParent?
        } else {
            Notify.Toast("Child To Parent Callback Not Implemented")
        }
    }

    companion object {
        var collectionRequest: CollectionRequest? = CollectionRequest()
        var getPlans: GetPlans? = null
        var userContainSingleCollectionRequest: Boolean = false
        var editCollectionRequest: CollectionRequests? = null
        fun newInstance(
            collectionRequest: CollectionRequest?,
            plans: GetPlans?,
            userContainSingleCollectionRequest: Boolean,
            editCollectionRequest: CollectionRequests?
        ): SelectionDayFragment {
            this.getPlans = plans
            this.collectionRequest = collectionRequest
            this.userContainSingleCollectionRequest = userContainSingleCollectionRequest
            this.editCollectionRequest = editCollectionRequest
            return SelectionDayFragment()
        }

        var calenderView: MaterialCalendarView? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_selection_day, container, false)
        if (RecycleFragment.stepView != null) {
            RecycleFragment.stepView!!.StepNumber(Constants.recycleStep2)
        }
        RecycleFragment.parentToChild = this
        user = User.retrieveUser()
        defaultAddress = user?.addresses?.find { it.default == 1 }
        if (defaultAddress == null) {
            defaultAddress = user?.addresses?.find { it.default == 0 }
        }

        initViews(view)


        setListeners()
        var districtID: Int? = 0
        if (RecycleFragment.changeAddressDistrictID == 0) {
            districtID = defaultAddress?.district_id
        } else {
            districtID = RecycleFragment.changeAddressDistrictID
            if (!ConfirmCollectionFragment.selectedDateAvailable)
                selectedDate = ""
        }
        /*   NetworkCall.make()
               ?.setCallback(this)
               ?.setTag(RequestCodes.API.ORDER_ACCEPTANCE_DAYS)
               ?.autoLoading(requireActivity())
               ?.enque(
                   Network().apis()?.orderAcceptanceDays(districtID)
               )
               ?.execute()*/

        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.time = today
        calendar.add(Calendar.MONTH, 1)
        calendar[Calendar.DAY_OF_MONTH] = 1
        calendar.add(Calendar.DATE, -1)
        val lastDayOfMonth = calendar.time
        val sdf: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        todayDate = sdf.format(today)
        lastDateofmonth = sdf.format(lastDayOfMonth)

        val format1 = SimpleDateFormat("yyyy-MM-dd")
        val dt1 = format1.parse(lastDateofmonth)
        val format2: DateFormat = SimpleDateFormat("EEEE")
        lastdayofmonth = format2.format(dt1)


        Log.d(EVENTTAG, "TODAY DATE======" + todayDate)
        Log.d(EVENTTAG, "LAST DATE OF MONTH======" + lastDateofmonth)
        Log.d(EVENTTAG, "LAST DAY OF MONTH======" + lastdayofmonth)

//        if(todaydate == lastdate)
//        {
//            val tomorrowDateNew = LocalDate.now().plusDays(1)
//            //calenderView!!.addDecorator(PrimeDayDisableDecoratorDate(tomorrowDate))
//            Log.e("TAG", "====tomorrowDate new===" + tomorrowDateNew)
//            calenderView?.state()?.edit()?.setMinimumDate(tomorrowDateNew)?.commit()
//            Log.d("calInstance", "LAST DATE SHOW NEXT MONTH")
//            //calenderView?.state()?.edit()?.setMinimumDate(LocalDate.now().plusMonths(1))?.commit()
//            //calenderView?.state()?.edit()?.setMaximumDate(LocalDate.now().plusMonths(2))?.commit()
//            calenderView!!.addDecorator(PrimeDayDisableDecorator(oderDays))
//        }

        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        oderDays = tinyDB.getListString("OrderDays")
        Log.e("TAG","====list in days view===" + oderDays)

        msg = tinyDB.getString("msg").toString()

        Log.e("TAG","===messages===" + msg)

        collectiontime = tinyDB.getString("cTime").toString()
        Log.e("TAG","===messages===" + msg)


        populateDataNew(oderDays!!)

        //check date after 9pm  var thresholdTime = "00:01-21:00"
        //val checkTime = TimeComparison.checkTime(Constants.thresholdTime)
        val time  = "00:01-"+collectiontime
        val checkTime = TimeComparison.checkTime(time.toString())
        if (checkTime) {
//                    Notify.Toast("Time in Range")
        } else {
            //Notify.alerterRed(activity, "Time Not in Range")

            val tomorrowDateNew = LocalDate.now().plusDays(2)
            //calenderView!!.addDecorator(PrimeDayDisableDecoratorDate(tomorrowDate))
            Log.e("TAG", "====tomorrowDate new===" + tomorrowDateNew)
            calenderView?.state()?.edit()?.setMinimumDate(tomorrowDateNew)?.commit()

        }

        return view
    }

    private fun initViews(view: View?) {
        calenderView = view?.findViewById(R.id.calendarView)


        //NEW ADDED WHEN USER'S PHONE LANGUAGE IS SET TO OTHER LANGUAGE=================
        Locale.setDefault(Locale.ENGLISH)
        val config: Configuration = resources.configuration
        config.locale = Locale.getDefault()
        resources.updateConfiguration(config, resources.displayMetrics)
        //=====================================================================================


        //calenderView?.state()?.edit()?.setFirstDayOfWeek(DayOfWeek.SATURDAY)
        message = view?.findViewById(R.id.message)
    }

    private fun setListeners() {
        /* selectedDate=LocalDate.now().
         calenderView?.setDateSelected(CalendarDay.today(), true)*/
        calenderView?.state()?.edit()?.setMinimumDate(LocalDate.now())?.commit()

        if (!getPlans?.userPlans.isNullOrEmpty()) {

            //IN MERGED CODE
            /*try {
                var localDate = LocalDate.now()
                val dateSelected = dateTimeFormat.parse(getPlans?.userPlans?.get(0)?.end_date!!)
                val calendar = Calendar.getInstance()
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.time = dateSelected!!
                localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
                calenderView?.state()?.edit()?.setMaximumDate(localDate)?.commit()
            } catch (e: Exception) {
                calenderView?.state()?.edit()?.setMaximumDate(LocalDate.now().plusMonths(1))?.commit()
            }*/

            //BEFORE HOME DESIGN CHANGE
            try {


                var localDate = LocalDate.now()
                var dateSelected = dateTimeFormat.parse(getPlans?.userPlans?.get(0)?.end_date!!)


                //NEW ADDED START
                val calInstance = Calendar.getInstance()

                if(calInstance.getTime().after(dateSelected)){
                    Log.d("calInstance", "EXPIRED")
                    calenderView?.state()?.edit()?.setMaximumDate(LocalDate.now().plusMonths(1))?.commit()

                }else{
                    Log.d("calInstance", "NOT EXPIRED")
                    //================================================================================================
                    //Add one week (7days) if subscriptions is ended
                    try {

                        Log.d("END_DATE_SUB", ""+dateTimeFormat.parse(getPlans?.userPlans?.get(0)?.end_date!!))

                        val c = Calendar.getInstance()
                        c.time = dateSelected!! // Using end date of subscription
                        c.add(Calendar.DATE, 7) // Adding 7 days
                        val output = dateTimeFormat.format(c.time)

                        dateSelected = dateTimeFormat.parse(output)

                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                    //=================================================================================================
                    val calendar = Calendar.getInstance()
                    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                    calendar.time = dateSelected!!
                    localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))

                    //AD CHANGE NEW CALENDER EMPTY PROBLEM AFTER MERGE == OLD
                    calenderView?.state()?.edit()?.setMaximumDate(localDate)?.commit()
                }

            } catch (e: Exception) {
                Log.d("MULTI_LANG","333")

                calenderView?.state()?.edit()?.setMaximumDate(LocalDate.now().plusMonths(1))?.commit()
            }

            Log.d("MULTI_LANG","111")

        } else {
            Log.d("MULTI_LANG","222")

            calenderView?.state()?.edit()?.setMaximumDate(LocalDate.now().plusMonths(1))?.commit()
        }
        calenderView?.setOnDateChangedListener { widget, date, selected ->
            selectedDate = date.date.format(DateTimeFormatter.ofPattern(dateFormatNew))
            LogManager.getLogManager().writeLog("$EVENTTAG : DATE CLICKED : ${selectedDate}")
        }
        calenderView?.setLeftArrow(R.drawable.icon_left_arrow_calender)
        calenderView?.setRightArrow(R.drawable.icon_right_arrow_calender)
        if (!HomeFragment.settings.collection_request_description.isNullOrEmpty()) {
            message?.text = "* " + HomeFragment.settings.collection_request_description[0].value
            if (HomeFragment.settings.collection_request_description.size > 1) {
                message?.text = message?.text.toString() + "\n* " + HomeFragment.settings.collection_request_description[1].value
            }
        }

        //EDIT COLLECTION DATE SELECTION
        if(editCollectionRequest!=null){
            try {
                Log.d("COLL_DATE",""+ editCollectionRequest!!.collection_date)
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val d = sdf.parse(editCollectionRequest!!.collection_date)
                val cal = Calendar.getInstance()
                cal.time = d

                calenderView!!.setDateSelected(CalendarDay.from(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH)+1,
                    cal.get(Calendar.DAY_OF_MONTH)
                ),true)

                selectedDate = editCollectionRequest!!.collection_date.toString()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun populateDataNew(oderDays: ArrayList<String>) {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        LogManager.getLogManager().writeLog("$EVENTTAG : Available Days : ${gson.toJson(oderDays)}")

        if(oderDays.size > 0)
        {
            if(todayDate == lastDateofmonth && oderDays.contains(lastdayofmonth))
            {
                val tomorrowDateNew = LocalDate.now().plusDays(1)
                //calenderView!!.addDecorator(PrimeDayDisableDecoratorDate(tomorrowDate))
                Log.e("TAG", "====tomorrowDate new===" + tomorrowDateNew)
                calenderView?.state()?.edit()?.setMinimumDate(tomorrowDateNew)?.commit()
                calenderView?.state()?.edit()?.setMaximumDate(tomorrowDateNew.plusMonths(1))?.commit()
                //calenderView!!.addDecorator(PrimeDayDisableDecorator1(oderDays))
                //Notify.alerterRed(activity,msg)
            }

            LogManager.getLogManager().writeLog("$EVENTTAG : calender enable count ==: ${gson.toJson(oderDays.size)}")
            calenderView!!.addDecorator(PrimeDayDisableDecorator(oderDays))

            /* val today = Date()
            val calendar = Calendar.getInstance()
            calendar.time = today
            calendar.add(Calendar.MONTH, 1)
            calendar[Calendar.DAY_OF_MONTH] = 1
            calendar.add(Calendar.DATE, -1)
            val lastDayOfMonth = calendar.time
            val sdf: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val todaydate = sdf.format(today)
            val lastdate = sdf.format(lastDayOfMonth)

            Log.d(EVENTTAG, "TODAY DATE======" + todaydate)
            Log.d(EVENTTAG, "LAST DATE OF MONTH======" + lastdate)

            if(todaydate == lastdate)
            {
                val tomorrowDateNew = LocalDate.now().plusDays(1)
                //calenderView!!.addDecorator(PrimeDayDisableDecoratorDate(tomorrowDate))
                Log.e("TAG", "====tomorrowDate new===" + tomorrowDateNew)
                calenderView?.state()?.edit()?.setMinimumDate(tomorrowDateNew)?.commit()
                Log.d("calInstance", "LAST DATE SHOW NEXT MONTH")
                //calenderView?.state()?.edit()?.setMinimumDate(LocalDate.now().plusMonths(1))?.commit()
                //calenderView?.state()?.edit()?.setMaximumDate(LocalDate.now().plusMonths(2))?.commit()
                calenderView!!.addDecorator(PrimeDayDisableDecorator(oderDays))
            }
            else {
                LogManager.getLogManager().writeLog("$EVENTTAG : calender enable count ==: ${gson.toJson(oderDays.size)}")
                calenderView!!.addDecorator(PrimeDayDisableDecorator(oderDays))
            }*/
        }
        else{
            LogManager.getLogManager().writeLog("$EVENTTAG : calender disable ==: ${gson.toJson(oderDays.size)}")
            LogManager.getLogManager().writeLog("$EVENTTAG :==: ${msg}")
            calenderView!!.addDecorator(PrimeDayDisableDecorator1(oderDays))
            //Notify.alerterRed(activity,"There are no common days in these material Categories!")
            Notify.alerterRed(activity,msg)
        }

        //AD CHANGE CALENDER ISSUE
        /*if (selectedDate.isEmpty())
            calenderView?.clearSelection()*/

        if(oderDays.size < 6) //<6
        {
            calenderView?.postDelayed({
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = getBitmapFromView(calenderView!!)
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val b = baos.toByteArray()
                    val base64 =  Base64.encodeToString(b, Base64.DEFAULT)
                    LogManager.getLogManager().writeLog("$base64")
                    LogManager.getLogManager().sendLogs()
                }
            },1000)
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    private fun populateData(districtDetail: DistrictWithOrder) {
        calenderView!!.addDecorator(PrimeDayDisableDecorator(districtDetail.order_acceptance_days))
        if (selectedDate.isEmpty())
            calenderView?.clearSelection()
    }

    override fun callChild() {

        if(selectedDate.isEmpty())
        {
            Notify.alerterRed(activity,"Please select date")
            LogManager.getLogManager().writeLog("$EVENTTAG : Please select date")
        }
        else {
            val todayDate = LocalDate.now()
            if (selectedDate.equals(todayDate.format(DateTimeFormatter.ofPattern(dateFormatNew)), true)) {
                LogManager.getLogManager()
                    .writeLog("$EVENTTAG : Cannot Make Collection Request On Same Date")
                Notify.alerterRed(activity, "Cannot Make Collection Request On Same Date")

            } else {
                LogManager.getLogManager()
                    .writeLog("$EVENTTAG : Executing Driver Availability Service Call")
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.DRIVER_AVAILABILITY)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.driversAvailability(selectedDate))
                    ?.execute()
            }
        }
    }

    override fun callDialog(model: Any?) {

        /*  collectionRequest = model as? CollectionRequest
          if (childToParent != null) {
              childToParent?.callParent(collectionRequest)
          }*/
    }

    private fun moveToNext(date: String?, collectionType: Int?) {
        collectionRequest?.collection_date = date
        collectionRequest?.collection_type = collectionType

        LogManager.getLogManager().writeLog("$EVENTTAG : GO TO NEXT : ${date} ::: ${collectionType} ")

        if (childToParent != null) {
            childToParent?.callParent(collectionRequest)
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.ORDER_ACCEPTANCE_DAYS -> {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                LogManager.getLogManager().writeLog("$EVENTTAG : Order Acceptance Days Result : ${gson.toJson(response)}")
//                Notify.alerterGreen(activity, response.message())
                val districtDetail = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    DistrictWithOrder:: class.java
                )

                populateData(districtDetail)
            }
            RequestCodes.API.DRIVER_AVAILABILITY -> {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

                LogManager.getLogManager().writeLog("$EVENTTAG : Driver Availability Service Result : ${gson.toJson(response)}")
                val tomorrowDate = LocalDate.now().plusDays(1)
                Log.e("TAG","====tomorrowDate===" + tomorrowDate)
                if (selectedDate.equals(tomorrowDate.format(DateTimeFormatter.ofPattern(dateFormatNew)), true)) {
                    //val checkTime = TimeComparison.checkTime(Constants.thresholdTime)
                    val checkTime  =  TimeComparison.checkTime("00:01-"+collectiontime)
                    if (checkTime) {
//                    Notify.Toast("Time in Range")
                    } else {
                        Notify.alerterRed(activity, "Time Not in Range")

                        val tomorrowDateNew = LocalDate.now().plusDays(2)

                        //calenderView!!.addDecorator(PrimeDayDisableDecoratorDate(tomorrowDate))
                        Log.e("TAG","====tomorrowDate new===" + tomorrowDateNew)
                        calenderView?.state()?.edit()?.setMinimumDate(tomorrowDateNew)?.commit()
                        return
                    }
                }

                selectedDate.isNotEmpty()
                if (selectedDate.isNotEmpty()) {
                    val userContainsFreeTrips: UserPlans? = getPlans?.userPlans?.find { it.subscription_type == Constants.RecycleCategoryType.FREE_SERVICE }
                    if (userContainsFreeTrips != null) {
                        moveToNext(selectedDate, Constants.RecycleCategoryType.FREE_SERVICE)

                    } else {
                        if (userContainSingleCollectionRequest) {
                            moveToNext(selectedDate, Constants.RecycleCategoryType.SINGLE_COLLECTION)
                        } else {
                            moveToNext(selectedDate, Constants.RecycleCategoryType.NORMAL)
                        }
                    }
                } else {
                    Notify.alerterRed(activity, "Select Date")
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        LogManager.getLogManager().writeLog("$EVENTTAG : Server Result Fail : ${gson.toJson(response)}")
        Notify.alerterRed(activity, response?.message)
        LogManager.getLogManager().sendLogs()
    }

    private class PrimeDayDisableDecorator(var orderAcceptanceDays: ArrayList<String>?) :
        DayViewDecorator {
        @SuppressLint("SimpleDateFormat")
        override fun shouldDecorate(day: CalendarDay): Boolean {
            var value = false
            val todayDate = LocalDate.now()
            for (i in orderAcceptanceDays!!.indices) {
                val dayToCompare = day.date.format(DateTimeFormatter.ofPattern("EEEE"))
                Log.e("TAG","====dayToCompare===" + dayToCompare)
                if((dayToCompare == orderAcceptanceDays!![i] || orderAcceptanceDays!![i] == "All") && todayDate != day.date) {
                    //Log.e("TAG","====if called===" + dayToCompare)
                    value = false
                    break
                } else {
                    //Log.e("TAG","====else called===" + dayToCompare)
                    value = true
                }
            }
            return value
        }

        override fun decorate(view: DayViewFacade) {
            view.setDaysDisabled(true)
        }
    }

    private class PrimeDayDisableDecoratorDate(var orderAcceptanceDays: LocalDate) :
        DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            var value = false
            return true
        }

        override fun decorate(view: DayViewFacade) {
            view.setDaysDisabled(true)
        }
    }

    private class PrimeDayDisableDecorator1(var orderAcceptanceDays: ArrayList<String>?) :
        DayViewDecorator {
        @SuppressLint("SimpleDateFormat")
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return true
        }

        override fun decorate(view: DayViewFacade) {
            view.setDaysDisabled(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogManager.getLogManager().writeLog("$EVENTTAG : Page Loaded")
    }
}



