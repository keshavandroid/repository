package com.reloop.reloop.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.android.reloop.model.CommanBase
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.AccessToken
import com.facebook.Profile
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.LoginActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.PickerCallback
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DependencyDetail
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.File
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object Utils {

    var dateFormat = "dd/MM/yyyy"
    var dateFormatDisplay = "MMM dd, yyyy"

    fun isCause(
        expected: Class<out Throwable?>,
        exc: Throwable?
    ): Boolean {
        return expected.isInstance(exc) || exc != null && isCause(expected, exc.cause)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun isLocationEnabled(context: Context): Boolean {
        var locationMode = 0
        val locationProviders: String
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            locationMode = try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.LOCATION_MODE
                )
            } catch (e: Settings.SettingNotFoundException) {
                e.printStackTrace()
                return false
            }
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
            !TextUtils.isEmpty(locationProviders)
        }
    }


    fun jsonConverterObject(data: LinkedTreeMap<*, *>?): String {

        //old
      /*  val jsonObj = JSONObject(data)
        val json = jsonObj.toString()
        return json*/

        //new code
        if(data != null) {
            val jsonObj = JSONObject(data)
            val json = jsonObj.toString()
            return json
        }
        return ""
    }

    fun jsonConverterObject(data: JSONObject): String {
        val jsonObj = data
        val json = jsonObj.toString()
        return json
    }

    fun jsonConverterArray(
        data: ArrayList<*>?
    ): String {
        val jsonArray = JSONArray(data)
        val json = jsonArray.toString()
        return json
    }

    fun urlSlashChange(url: String?): String? {

        var newUrl = url
        if (newUrl != null && !newUrl.isEmpty()) {
            if (newUrl.contains("\\")) {
                newUrl = newUrl.replace("\\\\".toRegex(), "/")
            }
        }
        return newUrl
    }

    fun glideImageLoaderServer(
        imageView: ImageView?,
        url: String?, placeHolder: Int?
    ) {
        var newUrl: String? = ""
        newUrl = if (url != null) {
            urlSlashChange(url)
        } else {
            ""
        }
        if (imageView != null) {
            try {
                Glide.with(MainApplication.applicationContext()).applyDefaultRequestOptions(
                    RequestOptions()
                        .error(placeHolder ?: R.drawable.icon_placeholder_generic)
                        .override(300, 300)
                        .placeholder(placeHolder ?: R.drawable.icon_placeholder_generic)
                )
                    .load(newUrl)
                    .into(imageView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
    }

    fun glideImageLoaderUriLocal(
        context: Context,
        imageView: ImageView,
        uri: Uri
    ) {
        try{
        if (uri.path != null) {
            Glide.with(context)
                .load(File(uri.path!!))
                .fitCenter()
                .placeholder(R.drawable.icon_placeholder_generic)
                .error(R.drawable.icon_placeholder_generic)
                .into(imageView)
        }
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    fun multiPartImageFile(uri: Uri?): MultipartBody.Part? {

        var filePart: MultipartBody.Part? = null
        if (uri != null) {
            val file = File(uri.path!!)
            try {
                filePart = MultipartBody.Part.createFormData(
                    "profile_pic",
                    file.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), file)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return filePart
    }

    fun imageRequestBody(uri: Uri?): RequestBody? {
        var requestBody: RequestBody? = null
        if (uri != null) {
            val file = File(uri.path!!)
            try {
                val requestBodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)

                requestBodyBuilder.addFormDataPart(
                    "profile_pic", file.name,
                    RequestBody.create(
                        "multipart/form-data".toMediaTypeOrNull(),
                        file
                    )
                )
                requestBody = requestBodyBuilder.build()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return requestBody
    }

    object CartList {
        fun saveArrayList(arrayList: ArrayList<Category?>?) {
            val prefs: SharedPreferences =
                MainApplication.applicationContext().getSharedPreferences(
                    Constants.PREF_NAME,
                    Constants.PRIVATE_MODE
                )
                    ?: error("err")
            val gson = Gson()
            val editor = prefs.edit()
            val json = gson.toJson(arrayList)
            editor.putString(Constants.SaveCartList, json)
            editor.apply()

        }

        fun fetchArrayList(): ArrayList<Category?> {
            val prefs: SharedPreferences =
                MainApplication.applicationContext().getSharedPreferences(
                    Constants.PREF_NAME,
                    Constants.PRIVATE_MODE
                ) ?: error("err")
            val gson = Gson()
            val yourArrayList: ArrayList<Category?>
            val json = prefs.getString(Constants.SaveCartList, "")

            yourArrayList = when {
                json.isNullOrEmpty() -> ArrayList()
                else -> gson.fromJson(json, object : TypeToken<List<Category?>>() {}.type)
            }
            return yourArrayList
        }
    }

    fun saveLastLocationLatLng(lat: Double, lng: Double) {
        val tinyDB = TinyDB(MainApplication.applicationContext())
        tinyDB.putDouble("lat", lat)
        tinyDB.putDouble("lng", lng)
    }

    fun getLat(): Double {
        return TinyDB(MainApplication.applicationContext()).getDouble("lat", 25.2048)
    }

    fun getLng(): Double {
        return TinyDB(MainApplication.applicationContext()).getDouble("lng", 55.2708)
    }

    //===============NEW for performance improvement==============
    // Optimized jsonConverterObject function
    fun jsonConverterObjectReloop(data: LinkedTreeMap<*, *>?): String {
        return data?.let {
            JSONObject(it).toString()
        } ?: ""
    }

    // Optimized getBaseResponse function
    fun getBaseResponseReloop(response: Response<Any?>): BaseResponse? {
        val responseBody = response.body() as? LinkedTreeMap<*, *>
        return responseBody?.let {
            val json = jsonConverterObject(it)
            Gson().fromJson(json, BaseResponse::class.java)
        }
    }
    //===============================================================




    fun getBaseResponse(response: Response<Any?>): BaseResponse? {
        return Gson().fromJson(
            jsonConverterObject(response.body() as? LinkedTreeMap<*, *>),
            BaseResponse::class.java
        )
    }

    fun getBaseResponseComman(response: Response<Any?>): CommanBase? {
        return Gson().fromJson(
            jsonConverterObject(response.body() as? LinkedTreeMap<*, *>),
            CommanBase::class.java
        )
    }

    fun commaConversion(number: Int?): String {
        return try {
            val myFormatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))
            val output: String = myFormatter.format(number)
            output
        } catch (e: Exception) {
            "Conversion Exception"
        }

    }

    fun commaConversion(number: Double?): String {
        return try {
            val myFormatter = DecimalFormat("###,##0.00", DecimalFormatSymbols(Locale.US))
            val output: String = myFormatter.format(number)
            output
        } catch (e: Exception) {
            ""
        }

    }

    fun logOut(activity: Activity?) {
        User.clearUser()
        val prefs = activity?.getSharedPreferences(Constants.PREF_NAME, AppCompatActivity.MODE_PRIVATE)
        prefs?.edit()?.putString("orgname", "")?.apply()
        prefs?.edit()?.putString("address_arr", "")?.apply()


        activity?.finish()
        try {
            TinyDB(activity).clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (LoginManager.getInstance() != null) {
            LoginManager.getInstance().logOut()
            AccessToken.setCurrentAccessToken(null)
            Profile.setCurrentProfile((null))
        }
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseAuth.getInstance().signOut()
            lateinit var googleSignInClient: GoogleSignInClient
            //--------------------------------Google Sign In Auth-------------------
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(MainApplication.applicationContext().getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(activity!!, gso)
            googleSignInClient.revokeAccess()
        }

        val handler = Handler()
        handler.postDelayed(
            {
                val intent = Intent(activity, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                activity?.startActivity(intent)
            }, 800
        )

    }

    fun setUnderLineText(tv: TextView?, textToUnderLine: String) {
        val tvt = tv?.text.toString()
        var ofe = tvt.indexOf(textToUnderLine, 0)
        val underlineSpan = UnderlineSpan()
        val wordToSpan = SpannableString(tv?.text)
        var ofs = 0
        while (ofs < tvt.length && ofe != -1) {
            ofe = tvt.indexOf(textToUnderLine, ofs)
            if (ofe == -1) break else {
                wordToSpan.setSpan(
                    underlineSpan,
                    ofe,
                    ofe + textToUnderLine.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                tv?.setText(wordToSpan, TextView.BufferType.SPANNABLE)
            }
            ofs = ofe + 1
        }
    }

    @SuppressLint("SetTextI18n")
    fun markRequired(textview: TextView) {
        textview.text = "${textview.text}*"
    }


    fun setSpinnerDistrictBasedOnId(
        districtId: Int?,
        districtFilterList: ArrayList<DependencyDetail>?,
        spinner: Spinner?
    ) {
        for (i in districtFilterList!!.indices) {
            if (districtFilterList[i].id == districtId) {
                spinner?.setSelection(i)
                break
            }
        }
    }

    fun getStringBasedOnID(
        districtId: Int?,
        districtFilterList: ArrayList<DependencyDetail>?
    ): String {
        for (i in districtFilterList!!.indices) {
            if (districtFilterList[i].id == districtId) {
                return districtFilterList[i].name.toString()
            }
        }
        return ""
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormattedServerDate(serverDate: String?): String? {
        return try {
            val serverFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val date: Date = serverFormat.parse(serverDate)
            val newFormat: DateFormat = SimpleDateFormat(dateFormat);
            val finalString = newFormat.format(date)

            finalString



        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormattedDisplayDate(serverDate: String?): String? {
        return try {
            val serverFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
            val date: Date = serverFormat.parse(serverDate)
            val newFormat: DateFormat = SimpleDateFormat(dateFormatDisplay);
            val finalString = newFormat.format(date)
            finalString
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getSingleDateServer(serverDate: String?): String? {
        return try {
            val serverFormat: DateFormat = SimpleDateFormat(dateFormatDisplay)
            val date: Date = serverFormat.parse(serverDate)
            val newFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd");
            val finalString = newFormat.format(date)
            finalString
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getFormattedDisplayDateCollection(serverDate: String?): String? {
        return try {
            val serverFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date: Date = serverFormat.parse(serverDate)
            val newFormat: DateFormat = SimpleDateFormat(dateFormatDisplay);
            val finalString = newFormat.format(date)
            finalString
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getDayFromDate(serverDate: String?): String? {
        return try {
            val serverFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date: Date = serverFormat.parse(serverDate)
            val newFormat: DateFormat = SimpleDateFormat("EEEE");
            val finalString = newFormat.format(date)
            finalString
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun showDatePickerWithLimit(
        activity: Activity?,
        callback: PickerCallback?,
        maxLimit: Boolean,
        minLimit: Boolean
    ) {
        val yy: Int
        val mm: Int
        val dd: Int
        val calendar = Calendar.getInstance()
        yy = calendar[Calendar.YEAR]
        mm = calendar[Calendar.MONTH]
        dd = calendar[Calendar.DATE]
        val dialog =
            DatePickerDialog(activity!!, { view, year, monthOfYear, dayOfMonth ->
                val monthOfYearNew = monthOfYear + 1
                var d = dayOfMonth.toString() + ""
                var m = monthOfYearNew.toString() + ""
                if (monthOfYearNew + "".length == 1) {
                    m = "0$monthOfYearNew"
                }
                if (dayOfMonth + "".length == 1) {
                    d = "0$dayOfMonth"
                }
                if (callback != null) {
                    @SuppressLint("SimpleDateFormat") val format =
                        SimpleDateFormat(dateFormatDisplay)
                    calendar[year, monthOfYearNew - 1] = dayOfMonth
                    val strDate = format.format(calendar.time)
                    callback.onSelected(strDate) // getMonth(monthOfYear)
                }
            }, yy, mm, dd)
        dialog.setOnCancelListener { dialog1 ->
            dialog1.dismiss()
        }
        if (maxLimit) {
            dialog.datePicker.maxDate = System.currentTimeMillis() - 1000
        } else if (minLimit) {
            calendar.add(Calendar.DAY_OF_WEEK, 0)
            dialog.datePicker.minDate = calendar.timeInMillis
        }
        dialog.show()
    }

    fun clearAllFragmentStack(fm: FragmentManager?): Boolean {
        if (fm != null) {
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                return true
            }
            val fragList = fm.fragments
            if (fragList.size > 0) {
                for (frag in fragList) {
                    if (frag == null) {
                        continue
                    }
                    if (frag.isVisible) {
                        if (clearAllFragmentStack(frag.childFragmentManager)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun colorMyText(inputText: String, startIndex: Int, endIndex: Int, textColor: Int): Spannable {
        val outPutColoredText: Spannable = SpannableString(inputText)
        try {
            outPutColoredText.setSpan(
                ForegroundColorSpan(textColor), startIndex, endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return outPutColoredText
    }

}