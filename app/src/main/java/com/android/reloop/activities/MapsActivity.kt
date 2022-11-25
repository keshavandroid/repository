package com.reloop.reloop.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.reloop.fragments.CampaignsListFragment
import com.android.reloop.model.JoinedCampaign
import com.android.reloop.model.LocationAddress
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.android.reloop.utils.LogManager
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.BuildConfig
import com.reloop.reloop.R
import com.reloop.reloop.network.ApiServices
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.activity_maps.*
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.IOException
import java.lang.reflect.Type
import java.util.*


class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnCameraChangeListener,
    View.OnClickListener, GoogleMap.OnMapClickListener , OnNetworkResponse {

    companion object {
        var TAG = "Maps Activity"
    }

    private lateinit var mMap: GoogleMap
    var address: TextView? = null
    var userLocation: FloatingActionButton? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var autocompleteFragment: AutocompleteSupportFragment? = null
    var done: Button? = null
    var cancel: Button? = null
    var back: ImageButton? = null

    private var actionBar: ActionBar? = null
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var bundle: Bundle? = Bundle()
    var lat: Double? = 0.0
    var lng: Double? = 0.0
    var removeSaveButton: Int? = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val policy = ThreadPolicy.Builder().permitAll().build()

        StrictMode.setThreadPolicy(policy)

        if (intent.getBundleExtra(Constants.DataConstants.bundle) != null) {
            bundle = intent?.getBundleExtra(Constants.DataConstants.bundle)
            lat = bundle?.getDouble(Constants.DataConstants.latitude, 0.0)
            lng = bundle?.getDouble(Constants.DataConstants.longitude, 0.0)
            removeSaveButton = bundle?.getInt(Constants.DataConstants.removeSaveButton, 0)
        }
        setActionbar()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.google_maps_key_project))

            //Places.initialize(applicationContext, BuildConfig.GOOGLE_API_KEY);

        }
        initViews()
        setListeners()
        setPlaceSearching()

    }

    @SuppressLint("InflateParams")
    private fun setActionbar() {
        actionBar = supportActionBar
        actionBar?.setDisplayShowHomeEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.elevation = 0f
        val customView: View = layoutInflater.inflate(R.layout.custom_actionbar_layout_maps, null)
        actionBar?.customView = customView
        val parent = customView.parent as Toolbar?
        parent?.setPadding(0, 0, 0, 0)
        parent?.setContentInsetsAbsolute(0, 0)
    }

    private fun initViews() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        address = findViewById(R.id.address_title)
        userLocation = findViewById(R.id.user_location)

        autocompleteFragment = supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        done = findViewById(R.id.done)
        cancel = findViewById(R.id.cancel)
        back = findViewById(R.id.back)
    }

    private fun setListeners() {
        userLocation?.setOnClickListener(this)
        done?.setOnClickListener(this)
        cancel?.setOnClickListener(this)
        back?.setOnClickListener(this)
    }

    //------------------------------On Map Ready Functionality-----------------------
    override fun onMapReady(googleMap: GoogleMap) {
        LogManager.getLogManager().writeLog("Maps Activity : onMapReady called")
        mMap = googleMap
        mMap.setOnCameraChangeListener(this)
        mMap.setOnMapClickListener(this)
        var currentLocation = LatLng(25.2048, 55.2708)
        if (hasLocationPermission()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 8f))
            if (lat != null && lat != 0.0 && lng != null && lng != 0.0) {
                LogManager.getLogManager().writeLog("Maps Activity : onMapReady got location : $lat-$lng")
                currentLocation = LatLng(lat!!, lng!!)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            }

        } else {
            LogManager.getLogManager().writeLog("Maps Activity : onMapReady : asking permission")
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
                Constants.accessLocation
            )
        }
    }

    /* override fun onCameraChange(p0: CameraPosition?) {
         val latLng: LatLng? = p0?.target
         getAddress(latLng?.latitude, latLng?.longitude)
     }*/

    private fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.user_location -> {
                if (hasLocationPermission()) {
                    if (Utils.isLocationEnabled(this)) {
                        gotoCurrentLocation()
                    } else {
                        AlertDialog.Builder(this, R.style.AlertDialogTheme)
                            .setTitle(R.string.gps_network_not_enabled) // GPS not found
                            .setMessage(R.string.open_location_settings) // Want to enable?
                            .setPositiveButton(R.string.yes) { _: DialogInterface?, i: Int ->
                                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                LogManager.getLogManager().writeLog("Maps Activity : permission alert : YES")
                            }
                            .setNegativeButton(R.string.no, null)
                            .show()
                    }
                } else {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
                        Constants.accessLocation
                    )
                }
            }
            R.id.done -> {
                val intent = Intent()
                intent.putExtra(Constants.DataConstants.location, address?.text?.toString())
                intent.putExtra(Constants.DataConstants.longitude, longitude)
                intent.putExtra(Constants.DataConstants.latitude, latitude)
                Utils.saveLastLocationLatLng(latitude!!, longitude!!)
                setResult(Constants.mapsCode, intent)
                finish()
            }
            R.id.cancel -> {
                finish()
            }
            R.id.back -> {
                finish()
            }
        }
    }

    private fun setPlaceSearching() {

        autocompleteFragment?.setPlaceFields(
            listOf(
                Place.Field.NAME,
                Place.Field.ID,
                Place.Field.LAT_LNG
            )
        )
//        autocompleteFragment?.setTypeFilter(TypeFilter.ADDRESS)

        autocompleteFragment?.setOnPlaceSelectedListener(object :
            PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                LogManager.getLogManager().writeLog("Maps Activity : Place selected : $place")
                Handler().postDelayed({
                    val latLng = place.latLng
                    if (latLng != null)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }, 500)
            }

            override fun onError(status: Status) {
                Log.e(TAG, "An error occurred: $status")
            }
        })
    }

    //---------------------------Permission Result Functionality---------------------
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.accessLocation) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogManager.getLogManager().writeLog("Maps Activity : Permission Granted")
                gotoCurrentLocation()
            } else {
                LogManager.getLogManager().writeLog("Maps Activity : Permission Alert")
                AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setMessage(R.string.app_may_not_work_msg)
                    .setPositiveButton(R.string.go_to_setting) { _: DialogInterface?, i: Int ->
                        startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))
                        LogManager.getLogManager().writeLog("Maps Activity : permission alert : YES")
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }
    }

    private fun gotoCurrentLocation() {
        LogManager.getLogManager().writeLog("Maps Activity : Get Current Location called")
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                    Utils.saveLastLocationLatLng(location.latitude, location.longitude)

                } else {
                    val currentLocation = LatLng(Utils.getLat(), Utils.getLng())
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                }
            }
    }


    //---------------------------------Set Text With Location Functionality----
    @SuppressLint("SetTextI18n")
    private fun getAddress(lat: Double?, lng: Double?) {
        LogManager.getLogManager().writeLog("Maps Activity : Get Address : $lat $lng")
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            latitude = lat
            longitude = lng
            val addresses = lat?.let { lng?.let { it1 -> geoCoder.getFromLocation(it, it1, 1) } }
            try {
                val obj = addresses?.get(0)
                when {
                    obj?.subLocality != null -> {
//                        selectedPlaceName?.text = obj.subLocality
                    }
                    obj?.locality != null -> {
//                        selectedPlaceName?.text = obj.locality
                    }
                    obj?.countryName != null -> {
//                        selectedPlaceName?.text = obj.countryName
                    }
                    obj?.featureName != null -> {
//                        selectedPlaceName?.text = obj.featureName
                    }
                    //                addresses=FeatureName+" "+SubLocality+" "+Locality+" -'"+CountryName;
                }
                var featureName = obj?.featureName
                var subLocality = obj?.subLocality
                var locality = obj?.locality
                var countryName = obj?.countryName
                if (featureName == null) featureName = " "
                if (subLocality == null) subLocality = " "
                if (locality == null) locality = " "
                if (countryName == null) countryName = " "
                done?.visibility = View.VISIBLE
                cancel?.visibility = View.VISIBLE
                if (removeSaveButton == 1) {
                    done?.visibility = View.GONE
                }
                address?.text = "$featureName $subLocality $locality -'$countryName"
                LogManager.getLogManager().writeLog("Maps Activity : Get Address : $featureName $subLocality $locality -'$countryName")
            } catch (exception: IndexOutOfBoundsException) {
                Log.v(TAG, "Array index out of bound")
            }
            catch (exception: NetworkOnMainThreadException) {
                Log.v(TAG, "NetworkOnMainThreadException")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /* override fun onMapClick(latLng: LatLng?) {
         if (latLng != null)
             mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
     }*/

    override fun onCameraChange(p0: CameraPosition) {
        Log.e("TAG","====onCameraChange===" + p0)
        LogManager.getLogManager().writeLog("Maps Activity : onCameraChange : $p0")
        val latLng = p0.target
        //getAddress(latLng.latitude, latLng.longitude)
        getAddressNew(latLng.latitude,latLng.longitude)
        //findAddress(latLng.latitude,latLng.longitude)
    }

    private fun findAddress(latitude: Double, longitude: Double) {
        val ll = latitude.toString() +","+ longitude.toString()
        Log.e("TAG","====LATLNG===" + ll)
        val mapkey = "AIzaSyBi8YohE9_P2E9rse6AuuHWWalDFEDTVT8"

        val url = "https://maps.googleapis.com/maps/api/geocode/json?key="+mapkey +"&latlng=" + ll +"&sensor=true"

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.FIND_ADDRESS)
            ?.autoLoading(this)
            ?.enque(Network().getApiServices()?.findAddress(url))
            ?.executeLocation()
    }

    private fun getAddressNew(latitude: Double, longitude: Double) {
        LogManager.getLogManager().writeLog("Maps Activity : Get Address : $latitude $longitude")
        val geoCoder = Geocoder(
            this,
            Locale.getDefault()
        )
        var result: String = null.toString()
        try {
            val addressList = geoCoder.getFromLocation(
                latitude, longitude, 1
            )

            if ((addressList != null && addressList.size > 0)) {
                val addresses = addressList.get(0)
                val sb = StringBuilder()
                for (i in 0 until addresses.maxAddressLineIndex) {
                    sb.append(addresses.getAddressLine(i)).append("\n")
                }

                var featureName = ""
                var subLocality =""
                var locality =""
                var countryName = ""

                if(addresses.featureName != null && addresses.featureName.length > 0)
                {
                    featureName = addresses.featureName + ", "
                }
                if(addresses.subLocality != null && addresses.subLocality.length > 0)
                {
                    subLocality = addresses.subLocality + ", "
                }
                if(addresses.locality != null && addresses.locality.length > 0)
                {
                    locality = addresses.locality + ", "
                }
                if(addresses.countryName != null && addresses.countryName.length > 0)
                {
                    countryName = addresses.countryName + ", "
                }

                result = "$featureName $subLocality $locality -'$countryName"
                Log.e("TAG","====address===" + result)
                done?.visibility = View.VISIBLE
                cancel?.visibility = View.VISIBLE
                if (removeSaveButton == 1) {
                    done?.visibility = View.GONE
                }
                address?.text = "$featureName $subLocality $locality -'$countryName"
                LogManager.getLogManager().writeLog("Maps Activity : Get Address : $featureName $subLocality $locality -'$countryName")
            }
            else{
                Log.e("TAG", "Unable to find location")
                LogManager.getLogManager().writeLog("Maps Activity : Unable to find location from latlng")
                findAddress(latitude,longitude)
            }
        } catch (e: IOException) {
            Log.e("TAG", "Unable connect to GeoCoder", e)
            LogManager.getLogManager().writeLog("Maps Activity : Unable connect to GeoCoder")

        }
    }

    override fun onMapClick(p0: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(p0, 15f))
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.FIND_ADDRESS -> {

                Log.e(TAG, "===response===" + response.body())

                val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(response.body() as? LinkedTreeMap<*, *>),
                        LocationAddress::class.java)

                if (dashboard.getResults()!!.isEmpty()) {
                    Log.e("TAG", "====unable to find location===")
                    LogManager.getLogManager().writeLog("Maps Activity : unable to find location from api : permission asked")
                    //askPermission()

                } else {
                    val addresses = dashboard.getResults()!!.get(0)!!.formattedAddress.toString()
                    Log.e("TAG", "====address===" + address)
                    done?.visibility = View.VISIBLE
                    cancel?.visibility = View.VISIBLE
                    if (removeSaveButton == 1) {
                        done?.visibility = View.GONE
                    }
                    address?.text = addresses
                    LogManager.getLogManager().writeLog("Maps Activity : Get Address From API: $addresses")
                }
            }
        }
    }

    private fun askPermission() {

        if (hasLocationPermission()) {
            if (Utils.isLocationEnabled(this)) {
                gotoCurrentLocation()
            } else {
                AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setTitle(R.string.gps_network_not_enabled) // GPS not found
                    .setMessage(R.string.open_location_settings) // Want to enable?
                    .setPositiveButton(R.string.yes) { _: DialogInterface?, i: Int ->
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        LogManager.getLogManager().writeLog("Maps Activity : permission alert : YES")
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),
                Constants.accessLocation
            )
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Log.e("TAG","====onFailure===" +  response?.message)
    }
}
