package com.android.reloop.activities

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.airbnb.lottie.LottieAnimationView
import com.android.reloop.fragments.ContinueAsFragment
import com.android.reloop.utils.LogFileSyncTask
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.gson.Gson
import com.reloop.reloop.BuildConfig
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.activities.LoginActivity
import com.reloop.reloop.adapters.ViewPagerIntroAdapter
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.network.serializer.user.User
import org.jsoup.Jsoup
import java.io.IOException


class SplashActivity : BaseActivity(), View.OnClickListener {

    var lottieAnimation: LottieAnimationView? = null
    var introScreenLayout: RelativeLayout? = null
    var viewPager: ViewPager? = null
    var next: ImageButton? = null
    var dotsLayout: LinearLayout? = null
    var viewpagerAdapter: ViewPagerIntroAdapter? = null
    private lateinit var dots: Array<TextView?>
    private lateinit var layouts: IntArray
    var start: Button? = null
    var MY_REQUEST_CODE = 111
    var updateAvilable = false

    //Deep Linking
    private var uriDeepLinking: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

      /*  if(BuildConfig.DEBUG)
        {
            val gson = Gson()
            //val json: String = "{\"id\":985,\"organization_id\":108,\"first_name\":\"lisa\",\"last_name\":\"loison\",\"reference_website\":null,\"email\":\"lisa.loison@hotmail.fr\",\"user_apple_id\":null,\"facebook_id\":null,\"phone_number\":\"+971-50-4112187\",\"hh_organization_name\":\"Abu Dhabi Accueil\",\"birth_date\":\"1986-12-07\",\"gender\":\"Female\",\"player_id\":[\"7e811435-3017-494a-b1f6-1de857bcc62c\"],\"avatar\":null,\"user_type\":1,\"login_type\":1,\"trips\":4,\"free_trips\":6,\"remaining_free_trips\":3,\"reward_points\":181,\"status\":1,\"reports\":1,\"verified_at\":\"2022-04-30 06:55:05\",\"password_reset_token\":null,\"signup_token\":null,\"api_token\":\"etojm2wyqy5t3h2nU4Trs45lyoCmGB8tzpt9N2qEQnH6zfuuEP1651301683\",\"created_at\":\"2022-04-30 06:54:43\",\"updated_at\":\"2022-08-30 11:24:22\",\"addresses\":[{\"id\":4502,\"user_id\":985,\"city_id\":3,\"district_id\":192,\"location\":\"F8H9+JXP Al Marina Abu Dhabi -'\\u00c9mirats arabes unis\",\"latitude\":24.479090405074725,\"longitude\":54.32001918554306,\"type\":\"Apartment\",\"no_of_bedrooms\":1,\"building_name\":\"building A\",\"address_name\":null,\"no_of_occupants\":2,\"street\":\"marina sunset residence\",\"floor\":\"5\",\"unit_number\":\"504\",\"default\":0,\"created_at\":\"2022-04-30 06:54:43\",\"updated_at\":\"2022-04-30 06:58:29\",\"city\":{\"id\":3,\"name\":\"Abu Dhabi\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2021-01-11 17:54:06\"},\"district\":{\"id\":192,\"city_id\":3,\"name\":\"Abu Dhabi City - Main\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-17 13:56:49\",\"updated_at\":\"2021-11-20 19:12:37\"}}],\"organization\":{\"id\":108,\"org_external_id\":\"ORG536391\",\"name\":\"Accueil Abu Dhabi\",\"no_of_employees\":\"21-50\",\"no_of_branches\":\"1\",\"sector_id\":8,\"created_at\":\"2022-04-20 16:50:52\",\"updated_at\":\"2022-04-20 16:50:52\"}}"
            //val json: String = "{\"id\":1009,\"organization_id\":108,\"first_name\":\"claire\",\"last_name\":\"demoulin\",\"reference_website\":null,\"email\":\"souchaudclaire@gmail.com\",\"user_apple_id\":null,\"facebook_id\":null,\"phone_number\":\"+971-54-4498354\",\"hh_organization_name\":\"Abu Dhabi Accueil\",\"birth_date\":\"1981-11-07\",\"gender\":\"Female\",\"player_id\":[\"ab728faf-edda-408d-9da1-af728f7dcdea\"],\"avatar\":null,\"user_type\":1,\"login_type\":1,\"trips\":1,\"free_trips\":5,\"remaining_free_trips\":0,\"reward_points\":516,\"status\":1,\"reports\":1,\"verified_at\":\"2022-05-02 11:03:49\",\"password_reset_token\":null,\"signup_token\":null,\"api_token\":\"nDOy9L5ir2C3tJR6gFNAkzNzSCoQEvuRSUOcIfppMwYnaeOdnh1651489399\",\"created_at\":\"2022-05-02 11:03:19\",\"updated_at\":\"2022-09-07 06:34:56\",\"addresses\":[{\"id\":4525,\"user_id\":1009,\"city_id\":3,\"district_id\":203,\"location\":\"9FRV+JV5 Rabdan Abu Dhabi -'\\u00c9mirats arabes unis\",\"latitude\":24.392224623002726,\"longitude\":54.49343405663967,\"type\":\"Villa\",\"no_of_bedrooms\":4,\"building_name\":\"Mangrove Village\",\"address_name\":null,\"no_of_occupants\":4,\"street\":\"Al Dhannah Street\",\"floor\":null,\"unit_number\":\"villa 4092\",\"default\":0,\"created_at\":\"2022-05-02 11:03:19\",\"updated_at\":\"2022-05-02 11:07:52\",\"city\":{\"id\":3,\"name\":\"Abu Dhabi\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2021-01-11 17:54:06\"},\"district\":{\"id\":203,\"city_id\":3,\"name\":\"Abu Dhabi Gate City\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-11-20 19:13:09\",\"updated_at\":\"2021-11-20 19:13:09\"}}],\"organization\":{\"id\":108,\"org_external_id\":\"ORG536391\",\"name\":\"Accueil Abu Dhabi\",\"no_of_employees\":\"21-50\",\"no_of_branches\":\"1\",\"sector_id\":8,\"created_at\":\"2022-04-20 16:50:52\",\"updated_at\":\"2022-04-20 16:50:52\"}}"
            //val json: String = "{\"id\":570,\"organization_id\":41,\"first_name\":null,\"last_name\":null,\"reference_website\":null,\"email\":\"l'occitane.festivalcity@loccitane.me\",\"user_apple_id\":null,\"facebook_id\":null,\"phone_number\":\"+971-50-7475372\",\"hh_organization_name\":null,\"birth_date\":null,\"gender\":null,\"player_id\":[\"f974f340-5e8e-11ec-a53c-f67d8a25c07a\"],\"avatar\":null,\"user_type\":2,\"login_type\":1,\"trips\":0,\"free_trips\":3,\"remaining_free_trips\":0,\"reward_points\":106,\"status\":1,\"reports\":1,\"verified_at\":\"2022-02-27 15:48:50\",\"password_reset_token\":null,\"signup_token\":\"yP3uCeIBs4g7YKgYwIutr2clY3rUJG\",\"api_token\":\"AkslzwFZyBOIezDQBNiIJJZClGlzCgEK7a48IzKqFBUEeICPhD1641643168\",\"created_at\":\"2022-01-08 11:59:28\",\"updated_at\":\"2022-09-08 05:59:44\",\"addresses\":[{\"id\":2687,\"user_id\":570,\"city_id\":1,\"district_id\":96,\"location\":\"Dubai Festival City, Festival Centre 1 Dubai Festival City Dubai -'United Arab Emirates\",\"latitude\":25.22299699649783,\"longitude\":55.35494953393937,\"type\":null,\"no_of_bedrooms\":null,\"building_name\":\"Festival City Mall\",\"address_name\":null,\"no_of_occupants\":null,\"street\":\"Festival City Mall\",\"floor\":null,\"unit_number\":null,\"default\":1,\"created_at\":\"2022-01-08 11:59:28\",\"updated_at\":\"2022-01-08 12:01:59\",\"city\":{\"id\":1,\"name\":\"Dubai\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2020-12-08 08:24:12\"},\"district\":{\"id\":96,\"city_id\":1,\"name\":\"DFC - Dubai Festival City\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-16 16:20:25\",\"updated_at\":\"2021-01-16 16:20:25\"}}],\"organization\":{\"id\":41,\"org_external_id\":\"ORG779655\",\"name\":\"L'Occitane - Festival City\",\"no_of_employees\":\"1-20\",\"no_of_branches\":\"1\",\"sector_id\":26,\"created_at\":\"2022-01-08 11:59:28\",\"updated_at\":\"2022-01-08 12:01:59\"}}"
            //val json: String = "{\"id\":556,\"organization_id\":31,\"first_name\":null,\"last_name\":null,\"reference_website\":null,\"email\":\"itzfaisalraza@gmail.com\",\"user_apple_id\":null,\"facebook_id\":null,\"phone_number\":\"+971-55-5555555\",\"hh_organization_name\":null,\"birth_date\":null,\"gender\":null,\"player_id\":[\"0a8b580e-f25a-486f-a035-46fc05dc41de\"],\"avatar\":null,\"user_type\":2,\"login_type\":1,\"trips\":1,\"free_trips\":1,\"remaining_free_trips\":1,\"reward_points\":0,\"status\":1,\"reports\":1,\"verified_at\":\"2022-09-09 12:39:15\",\"password_reset_token\":null,\"signup_token\":\"mgXS20aTNrgxOEmwgqB3BhOanamGxy\",\"api_token\":\"Xo14UvIddIvLkfnb1B1MDXH8poxrOjxtNKy1ShqSAWnciIvFTD1641373146\",\"created_at\":\"2022-01-05 08:59:06\",\"updated_at\":\"2022-09-09 12:39:15\",\"addresses\":[{\"id\":2249,\"user_id\":556,\"city_id\":1,\"district_id\":3,\"location\":\"32 Shah Faisal Colony Burewala -'Pakistan\",\"latitude\":30.160775896583626,\"longitude\":72.69563935697079,\"type\":\"office\",\"no_of_bedrooms\":0,\"building_name\":\"tech\",\"address_name\":null,\"no_of_occupants\":2,\"street\":\"Test\",\"floor\":\"1\",\"unit_number\":\"2\",\"default\":0,\"created_at\":\"2022-01-05 08:59:06\",\"updated_at\":\"2022-09-09 12:39:15\",\"city\":{\"id\":1,\"name\":\"Dubai\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2020-12-08 08:24:12\"},\"district\":{\"id\":3,\"city_id\":1,\"name\":\"Abu Hail\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-11 17:56:01\",\"updated_at\":\"2021-11-07 16:48:24\"}}],\"organization\":{\"id\":31,\"org_external_id\":\"ORG302761\",\"name\":\"Developer\",\"no_of_employees\":\"51-200\",\"no_of_branches\":\"1\",\"sector_id\":3,\"created_at\":\"2022-01-05 08:59:06\",\"updated_at\":\"2022-09-09 12:39:15\"}}"
            //val json: String = "{\"id\":1484,\"organization_id\":null,\"first_name\":\"youssef\",\"last_name\":\"bouhaouala\",\"reference_website\":null,\"email\":\"bouhaouala_youssef@yahoo.fr\",\"user_apple_id\":null,\"facebook_id\":null,\"phone_number\":\"+971-5-22739858\",\"hh_organization_name\":null,\"birth_date\":\"1986-02-16\",\"gender\":\"Male\",\"player_id\":[\"58953368-eaab-4137-bdc4-e4613650849e\"],\"avatar\":null,\"user_type\":1,\"login_type\":1,\"trips\":3,\"free_trips\":1,\"remaining_free_trips\":0,\"reward_points\":25,\"status\":1,\"reports\":1,\"verified_at\":\"2022-08-29 11:44:05\",\"password_reset_token\":null,\"signup_token\":null,\"api_token\":\"KNjGIN6MhBXuD0EUWK75oN2Kag7otBdCPvW0JqjDzfzQsl3atU1661773430\",\"created_at\":\"2022-08-29 11:43:50\",\"updated_at\":\"2022-09-17 04:52:52\",\"addresses\":[{\"id\":5272,\"user_id\":1484,\"city_id\":1,\"district_id\":101,\"location\":\"17   Dubai -'\\u00c9mirats arabes unis\",\"latitude\":25.068283777039863,\"longitude\":55.13200834393501,\"type\":\"Apartment\",\"no_of_bedrooms\":0,\"building_name\":\"Trident Waterfront\",\"address_name\":null,\"no_of_occupants\":0,\"street\":\"al seba street\",\"floor\":\"5\",\"unit_number\":\"504\",\"default\":0,\"created_at\":\"2022-08-29 11:43:50\",\"updated_at\":\"2022-09-02 15:42:07\",\"city\":{\"id\":1,\"name\":\"Dubai\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2020-12-08 08:24:12\"},\"district\":{\"id\":101,\"city_id\":1,\"name\":\"Dubai Marina\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-16 16:23:09\",\"updated_at\":\"2021-01-16 16:23:09\"}}],\"organization\":null}"
            //val json: String = "{\"id\":27,\"organization_id\":1,\"first_name\":null,\"last_name\":null,\"reference_website\":null,\"email\":\"ecyclex@ecyclex.com\",\"user_apple_id\":null,\"facebook_id\":null,\"phone_number\":\"+971-51-1111111\",\"hh_organization_name\":null,\"birth_date\":null,\"gender\":null,\"player_id\":[\"7210360f-f0f3-4180-84c2-f87065829\",\"27bff5c0-958d-40fa-af5e-04144a94c44d\",\"7e6c426b-56eb-4f32-862a-11e9d5a29679\",\"40f4fa0d-6df7-4e50-a79a-48dbd6989d7f\",\"9daeb244-b340-4ef9-9f73-93f8fe3fbfd0\",\"987b2e8e-4214-477b-9114-ed25967f3f67\",\"75bec3e1-f599-4ab3-9d97-56f118ca7b76\"],\"avatar\":null,\"user_type\":2,\"login_type\":1,\"trips\":75,\"free_trips\":2,\"remaining_free_trips\":0,\"reward_points\":5,\"status\":1,\"reports\":1,\"verified_at\":\"2020-12-23 15:33:56\",\"password_reset_token\":\"xmorKBP2ZidB3yGaks3fu8PU9POsKp1641640376\",\"signup_token\":\"kiS7ZmxAJ9NlIUMZyXpos9cicF9wmN\",\"api_token\":\"1ZZgIChNoYXIYNIhaYcD0IhAr1b5QtyHZNdVWQhrwEyrV8MnIi1608737538\",\"created_at\":\"2020-12-23 15:32:18\",\"updated_at\":\"2022-09-22 14:17:34\",\"addresses\":[{\"id\":1206,\"user_id\":27,\"city_id\":1,\"district_id\":3,\"location\":\"Dubai Trade Centre Dubai -'United Arab Emirates\",\"latitude\":25.20574655736112,\"longitude\":55.27079991996288,\"type\":\"Shop\",\"no_of_bedrooms\":0,\"building_name\":\"a\",\"address_name\":null,\"no_of_occupants\":7,\"street\":\"a\",\"floor\":\"a\",\"unit_number\":\"a\",\"default\":1,\"created_at\":\"2021-05-17 18:27:49\",\"updated_at\":\"2022-01-04 17:27:35\",\"city\":{\"id\":1,\"name\":\"Dubai\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2020-12-08 08:24:12\"},\"district\":{\"id\":3,\"city_id\":1,\"name\":\"Abu Hail\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-11 17:56:01\",\"updated_at\":\"2021-11-07 16:48:24\"}},{\"id\":2701,\"user_id\":27,\"city_id\":1,\"district_id\":3,\"location\":\"Dubai Trade Centre Dubai -'United Arab Emirates\",\"latitude\":25.20574655736112,\"longitude\":55.27079991996288,\"type\":\"Warehouse\",\"no_of_bedrooms\":0,\"building_name\":\"b\",\"address_name\":null,\"no_of_occupants\":0,\"street\":\"b\",\"floor\":\"b\",\"unit_number\":\"b\",\"default\":0,\"created_at\":\"2022-01-15 14:03:59\",\"updated_at\":\"2022-01-15 14:03:59\",\"city\":{\"id\":1,\"name\":\"Dubai\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2020-12-08 08:24:12\"},\"district\":{\"id\":3,\"city_id\":1,\"name\":\"Abu Hail\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-11 17:56:01\",\"updated_at\":\"2021-11-07 16:48:24\"}}],\"organization\":{\"id\":1,\"org_external_id\":\"ORG384684\",\"name\":\"Ecyclex\",\"no_of_employees\":\"21-50\",\"no_of_branches\":\"2-10\",\"sector_id\":16,\"created_at\":\"2020-12-23 15:32:18\",\"updated_at\":\"2022-06-29 04:42:28\"}}"
            val json: String = "{\"id\":27,\"organization_id\":1,\"first_name\":null,\"last_name\":null,\"reference_website\":null,\"email\":\"ecyclex@ecyclex.com\",\"user_apple_id\":null,\"facebook_id\":null,\"phone_number\":\"+971-51-1111111\",\"hh_organization_name\":null,\"birth_date\":null,\"gender\":null,\"player_id\":[\"7210360f-f0f3-4180-84c2-f87065829\",\"27bff5c0-958d-40fa-af5e-04144a94c44d\",\"7e6c426b-56eb-4f32-862a-11e9d5a29679\",\"40f4fa0d-6df7-4e50-a79a-48dbd6989d7f\",\"9daeb244-b340-4ef9-9f73-93f8fe3fbfd0\",\"987b2e8e-4214-477b-9114-ed25967f3f67\",\"75bec3e1-f599-4ab3-9d97-56f118ca7b76\"],\"avatar\":null,\"user_type\":2,\"login_type\":1,\"trips\":75,\"free_trips\":2,\"remaining_free_trips\":0,\"reward_points\":5,\"status\":1,\"reports\":1,\"verified_at\":\"2020-12-23 15:33:56\",\"password_reset_token\":\"xmorKBP2ZidB3yGaks3fu8PU9POsKp1641640376\",\"signup_token\":\"kiS7ZmxAJ9NlIUMZyXpos9cicF9wmN\",\"api_token\":\"wzfEy7wzJgs5rtoqZlfF7ALVXmX4JEJ6jeHytlRbwxdvcMHnMH1637422030\",\"created_at\":\"2020-12-23 15:32:18\",\"updated_at\":\"2022-09-22 14:17:34\",\"addresses\":[{\"id\":1206,\"user_id\":27,\"city_id\":1,\"district_id\":3,\"location\":\"Dubai Trade Centre Dubai -'United Arab Emirates\",\"latitude\":25.20574655736112,\"longitude\":55.27079991996288,\"type\":\"Shop\",\"no_of_bedrooms\":0,\"building_name\":\"a\",\"address_name\":null,\"no_of_occupants\":7,\"street\":\"a\",\"floor\":\"a\",\"unit_number\":\"a\",\"default\":1,\"created_at\":\"2021-05-17 18:27:49\",\"updated_at\":\"2022-01-04 17:27:35\",\"city\":{\"id\":1,\"name\":\"Dubai\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2020-12-08 08:24:12\"},\"district\":{\"id\":3,\"city_id\":1,\"name\":\"Abu Hail\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-11 17:56:01\",\"updated_at\":\"2021-11-07 16:48:24\"}},{\"id\":2701,\"user_id\":27,\"city_id\":1,\"district_id\":3,\"location\":\"Dubai Trade Centre Dubai -'United Arab Emirates\",\"latitude\":25.20574655736112,\"longitude\":55.27079991996288,\"type\":\"Warehouse\",\"no_of_bedrooms\":0,\"building_name\":\"b\",\"address_name\":null,\"no_of_occupants\":0,\"street\":\"b\",\"floor\":\"b\",\"unit_number\":\"b\",\"default\":0,\"created_at\":\"2022-01-15 14:03:59\",\"updated_at\":\"2022-01-15 14:03:59\",\"city\":{\"id\":1,\"name\":\"Dubai\",\"status\":1,\"created_at\":\"2020-12-08 08:24:12\",\"updated_at\":\"2020-12-08 08:24:12\"},\"district\":{\"id\":3,\"city_id\":1,\"name\":\"Abu Hail\",\"status\":1,\"order_acceptance_days\":\"All\",\"created_at\":\"2021-01-11 17:56:01\",\"updated_at\":\"2021-11-07 16:48:24\"}}],\"organization\":{\"id\":1,\"org_external_id\":\"ORG384684\",\"name\":\"Ecyclex\",\"no_of_employees\":\"21-50\",\"no_of_branches\":\"2-10\",\"sector_id\":16,\"created_at\":\"2020-12-23 15:32:18\",\"updated_at\":\"2022-06-29 04:42:28\"}}"


            var user = gson.fromJson(json, User::class.java)
            user.save(user,this@SplashActivity)

        }*/

        MainApplication.hideActionBar(supportActionBar)
        initViews()
        setListeners()
        setData()
        Log.e("App Update ", "On Create")
//        updateApp()
        val update = Handler(Looper.getMainLooper())
        var currentVersion = ""
        try {
            currentVersion = applicationContext.packageManager.getPackageInfo(packageName, 0).versionName
            Log.e("Current Version", "::$currentVersion")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

//      GetVersionCode(this,currentVersion).execute()
        update.postDelayed(
            {
                if (!updateAvilable) {
                    val userModel = User.retrieveUser()
                    if (userModel != null && userModel.api_token?.isNotEmpty()!!) {
                        lottieAnimation?.visibility = View.VISIBLE
                        introScreenLayout?.visibility = View.GONE

                        //Check if deep-linking is available
                        uriDeepLinking = intent.data

                        // checking if the uri is null or not.
                        if (uriDeepLinking != null) {
                            // if the uri is not null then we are getting the path segments and storing it in list.
                            val parameters = uriDeepLinking!!.pathSegments

                            // after that we are extracting string
                            // from that parameters.
                            val dropOffID = parameters[parameters.size - 1]
                            val mainParam = parameters[parameters.size - 2]

//                            Log.d("DeepLink",""+mainParam)
//                            Log.d("DeepLink",""+dropOffID)

                            showHomeScreen("deeplink",dropOffID)

                        }else{
                            showHomeScreen("normal","")
                        }

                        /*update.postDelayed(
                            {
                                callLogs()
                            }, 7000)*/

                    } else {
                        lottieAnimation?.visibility = View.GONE
                        introScreenLayout?.visibility = View.VISIBLE
                    }
                }
            }, 1000
        )
        //updateApp()
    }

    private fun callLogs() {
        LogFileSyncTask.getInstance(this@SplashActivity).executeTask()
    }

    private fun updateApp() {
        try {
            val versionName: String = this.getPackageManager()
                .getPackageInfo(this.getPackageName(), 0).versionName
            Log.e(
                "App Update ",
                "Version Name=$versionName"
            )
          /*  val versionCode: Int = this.packageManager
                .getPackageInfo(this.getPackageName(), 0).versionCode*/
            val versionCode: Int = this.packageManager
                .getPackageInfo(this.getPackageName(), 0).versionCode
            Log.e(
                "App Update ",
                "Version Code=$versionCode"
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

// Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

// Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            Log.e("App Update ", "Update Type" + appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
            Log.e("App Update ", "Update Available" + appUpdateInfo.updateAvailability())

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                // instead, pass in AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                updateAvilable = true
                Log.e("App Update ", "Available")
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                        AppUpdateType.IMMEDIATE,
                        // The current activity making the update request.
                        this,
                        // Include a request code to later monitor this update request.
                        MY_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    Log.e("App Update Exception ", "" + e.printStackTrace())
                }

            } else {
                Log.e("App Update ", "Not Available")
            }
        }
    }

    private fun setData() {
        layouts = intArrayOf(
            R.layout.intro_1,
            R.layout.intro_2,
            R.layout.intro_3
        )
        addBottomDots(0)
        viewpagerAdapter = ViewPagerIntroAdapter(layouts)
        viewPager?.adapter = viewpagerAdapter
        viewPager?.addOnPageChangeListener(viewPagerPageChangeListener)
    }

    private fun setListeners() {
        next?.setOnClickListener(this)
        start?.setOnClickListener(this)

    }

    private fun initViews() {
        lottieAnimation = findViewById(R.id.animationView)
        introScreenLayout = findViewById(R.id.intro_screens)
        viewPager = findViewById(R.id.intro)
        next = findViewById(R.id.next)
        dotsLayout = findViewById(R.id.layoutDots)
        start = findViewById(R.id.start)
    }

    override fun onClick(v: View?) {
        when (v) {
            next -> {
                val current = getItem(1)
                if (current < layouts.size) { // move to next screen
                    viewPager!!.currentItem = current
                }
                if (current == 2) {
                    next?.visibility = View.GONE
                    start?.visibility = View.VISIBLE
                }
            }
            start -> {
//                showLoginScreen()
//                val continueAsFragment =  ContinueAsFragment()
//                val transaction = supportFragmentManager.beginTransaction()
//                transaction.add(R.id.container_continue_as, continueAsFragment)
//                transaction.commit()
                showContinueAsScreen()
            }
        }
    }

    private fun addBottomDots(currentPage: Int) {
        /*  dataList?.get(position)?.icon?.let {
      holder.imageIcon?.setImageResource(it)
  }*/
        dots = arrayOfNulls(layouts.size)

        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        dotsLayout?.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226;")
            dots[i]?.textSize = 35f
            dots[i]?.setTextColor(colorsInactive[currentPage])
            dotsLayout?.addView(dots[i])
        }
        if (dots.isNotEmpty()) {
            dots[currentPage]?.setTextColor(colorsActive[currentPage])
        }
    }

    private fun getItem(i: Int): Int {
        return viewPager?.currentItem?.plus(i)!!
    }

    //  viewpager change listener
    private var viewPagerPageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            addBottomDots(position)
            if (position == 0 || position == 1) {
                next?.visibility = View.VISIBLE
                start?.visibility = View.INVISIBLE
            } else if (position == 2) {
                next?.visibility = View.GONE
                start?.visibility = View.VISIBLE
            }

            /* // changing the next button text 'NEXT' / 'GOT IT'
                   if (position == layouts.length - 1) { // last page. make button text to GOT IT
                       btnNext.setText(getString(R.string.start))
                       btnSkip.setVisibility(View.GONE)
                   } else { // still pages are left
                       btnNext.setText(getString(R.string.next))
                       btnSkip.setVisibility(View.VISIBLE)
                   }*/
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    private fun showContinueAsScreen() {
        val intent = Intent(this, ContinueAsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showHomeScreen(isDeepLink: String,dropOffId: String) {

        if(isDeepLink.equals("deeplink")){
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("openDropOff",true)
            intent.putExtra("dropOffId",dropOffId)
            startActivity(intent)
            finish()
        }else{
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("openDropOff",false)
            intent.putExtra("dropOffId",dropOffId)
            startActivity(intent)
            finish()
        }


        //LogFileSyncTask.getInstance(this@SplashActivity).executeTask()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
//                Toast.makeText(this, "App Update Canceled", Toast.LENGTH_SHORT).show()
                // If the update is cancelled or fails,
                // you can request to start the update again.
            } else {
//                Toast.makeText(this, "App Update Started", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateApp()
    }

    class GetVersionCode(val context: Activity, val currentVersion: String) :
        AsyncTask<Void?, String?, String?>() {
        protected override fun doInBackground(vararg p0: Void?): String? {
            var newVersion: String? = null
            try {
                val document = Jsoup.connect(
                    "https://play.google.com/store/apps/details?id=" + context.getPackageName()
                        .toString() + "&hl=en"
                )
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                if (document != null) {
                    val element = document.getElementsContainingOwnText("Current Version")
                    for (ele in element) {
                        if (ele.siblingElements() != null) {
                            val sibElemets = ele.siblingElements()
                            for (sibElemet in sibElemets) {
                                newVersion = sibElemet.text()
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return newVersion
        }

        override fun onPostExecute(onlineVersion: String?) {
            super.onPostExecute(onlineVersion)
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                if (onlineVersion == currentVersion) {
                } else {
                    val alertDialog = AlertDialog.Builder(context).create()
                    alertDialog.setTitle("Update")
                    alertDialog.setIcon(R.mipmap.ic_launcher)
                    alertDialog.setMessage("New Update is available")
                    alertDialog.setButton(
                        AlertDialog.BUTTON_POSITIVE, "Update"
                    ) { dialog, which ->
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())))
                        } catch (anfe: ActivityNotFoundException) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())
                                )
                            )
                        }
                    }
                    alertDialog.setButton(
                        AlertDialog.BUTTON_NEGATIVE, "Cancel"
                    ) { dialog, which -> dialog.dismiss() }
                    alertDialog.show()
                }
            }
            Log.d("update", "Current version " + currentVersion.toString() + "playstore version " + onlineVersion)
        }
    }
}