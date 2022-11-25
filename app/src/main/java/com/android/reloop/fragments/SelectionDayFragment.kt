package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.content.Context
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
import com.reloop.reloop.activities.WebViewActivity
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
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class SelectionDayFragment : BaseFragment(), ParentToChild, AlertDialogCallback, OnNetworkResponse {

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
        var collectionRequest: CollectionRequest? =
            CollectionRequest()
        var getPlans: GetPlans? = null
        var userContainSingleCollectionRequest: Boolean = false
        fun newInstance(
            collectionRequest: CollectionRequest?,
            plans: GetPlans?,
            userContainSingleCollectionRequest: Boolean
        ): SelectionDayFragment {
            this.getPlans = plans
            this.collectionRequest = collectionRequest
            this.userContainSingleCollectionRequest = userContainSingleCollectionRequest
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
        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        oderDays = tinyDB.getListString("OrderDays")
        Log.e("TAG","====list in days view===" + oderDays)

        msg = tinyDB.getString("msg").toString()
        Log.e("TAG","===messages===" + msg)
        populateDataNew(oderDays!!)

        //check date after 9pm
        val checkTime = TimeComparison.checkTime(Constants.thresholdTime)
        if (checkTime) {
//                    Notify.Toast("Time in Range")
        } else {
            //Notify.alerterRed(activity, "Time Not in Range")

            val tomorrowDateNew = LocalDate.now().plusDays(2)

            //calenderView!!.addDecorator(PrimeDayDisableDecoratorDate(tomorrowDate))
            Log.e("TAG","====tomorrowDate new===" + tomorrowDateNew)
            calenderView?.state()?.edit()?.setMinimumDate(tomorrowDateNew)?.commit()
        }

        return view
    }

    private fun initViews(view: View?) {
        calenderView = view?.findViewById(R.id.calendarView)

        //calenderView?.state()?.edit()?.setFirstDayOfWeek(DayOfWeek.SATURDAY)
        message = view?.findViewById(R.id.message)
    }

    private fun setListeners() {
        /* selectedDate=LocalDate.now().
         calenderView?.setDateSelected(CalendarDay.today(), true)*/
        calenderView?.state()?.edit()?.setMinimumDate(LocalDate.now())?.commit()

        if (!getPlans?.userPlans.isNullOrEmpty()) {
            try {
                var localDate = LocalDate.now()
                val dateSelected = dateTimeFormat.parse(getPlans?.userPlans?.get(0)?.end_date!!)
                val calendar = Calendar.getInstance()
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.time = dateSelected!!
                localDate = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
                calenderView?.state()?.edit()?.setMaximumDate(localDate)?.commit()
            } catch (e: Exception) {
                calenderView?.state()?.edit()?.setMaximumDate(LocalDate.now().plusMonths(1))?.commit()
            }
        } else {
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
    }

    private fun populateDataNew(oderDays: ArrayList<String>) {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        LogManager.getLogManager().writeLog("$EVENTTAG : Available Days : ${gson.toJson(oderDays)}")

        if(oderDays.size > 0)
        {
            LogManager.getLogManager().writeLog("$EVENTTAG : calender enable count ==: ${gson.toJson(oderDays.size)}")
            calenderView!!.addDecorator(PrimeDayDisableDecorator(oderDays))
        }
        else{
            LogManager.getLogManager().writeLog("$EVENTTAG : calender disable ==: ${gson.toJson(oderDays.size)}")
            LogManager.getLogManager().writeLog("$EVENTTAG :==: ${msg}")
            calenderView!!.addDecorator(PrimeDayDisableDecorator1(oderDays))
            //Notify.alerterRed(activity,"There are no common days in these material Categories!")
            Notify.alerterRed(activity,msg)
        }

        Log.e("TAG","=====selected date when loading data====" + selectedDate)
       /* if (selectedDate.isEmpty())
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
                   // LogManager.getLogManager().sendLogs()
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
            if (selectedDate.equals(
                    todayDate.format(DateTimeFormatter.ofPattern(dateFormatNew)),
                    true
                )
            ) {
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
                    val checkTime = TimeComparison.checkTime(Constants.thresholdTime)
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
            val TAG = "SELECTION DATE DECORATOR CLASS"
            val todayDate = LocalDate.now()
            for (i in orderAcceptanceDays!!.indices) {
                val dayToCompare = day.date.format(DateTimeFormatter.ofPattern("EEEE"))
                if ((dayToCompare == orderAcceptanceDays!![i] || orderAcceptanceDays!![i] == "All") && todayDate != day.date) {
                    //Log.e("TAG","====if called===" + dayToCompare)
                    LogManager.getLogManager().writeLog("$TAG : day enabled : ${dayToCompare}")
                    value = false
                    break
                } else {
                    LogManager.getLogManager().writeLog("$TAG : day disabled : ${dayToCompare}")
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



