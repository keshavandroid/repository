package com.android.reloop.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Transformations.map
import com.android.reloop.CustomGridView.TwoWayAdapterView
import com.android.reloop.CustomGridView.TwoWayGridView
import com.android.reloop.bottomsheet.*
import com.android.reloop.model.DropOffPointsModel
import com.android.reloop.model.MapData
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.reloop.reloop.BuildConfig
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.*
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


class DropOffPinFragment : BaseFragment(),View.OnClickListener, OnNetworkResponse,GoogleMap.OnMapClickListener,
    OnMapReadyCallback, GoogleMap.OnCameraChangeListener, CityBottomsheet.ItemClickListener, MaterialBottomsheet.ItemClickListener,
    SelectedFilterBottomsheet.ItemClickListener,SelectedFilterFromFavouriteBottomsheet.ItemClickListener,FavouritesBottomsheet.ItemClickListener {

    var img_step1: ImageView? = null
    var text_step1: TextView?= null

    var back: Button? = null
    private var mapFragment: SupportMapFragment? = null
    var dropOffList: ArrayList<DropOffPoints>? = ArrayList()
    var favDropOffList: ArrayList<FavDropOffPoints>? = ArrayList()


    private var mMap: GoogleMap? = null


    var llSelectType: LinearLayout?=null
    var llFilter: LinearLayout?=null
    var txtClearFilter: TextView?=null

    var txtFilter: TextView?=null
    var txtDownArrow: TextView?=null
    var txtFavourite: TextView?=null
    var btnSelectStation: Button?=null

    var btnMainFilterApply: Button?=null

    var txtCity: TextView? = null
    var txtMaterial: TextView? = null
    var txtSelectedCities: TextView? = null
    //var txtSelectedMaterials: TextView? = null

    var isFromDelete: Boolean = false

    var visible_on_map: Int = 0


    //var grdSelectedMaterial: GridView? = null//OLD
    var grdSelectedMaterial: TwoWayGridView? = null // NEW

    var tinyDB: TinyDB?=null
    //address and materials
    private var dependenciesListing: Dependencies? = Dependencies()

    private var mSelectedMarker: Marker? = null

    //Filter
    var idCity: String = ""
    var selectedMaterial: ArrayList<MaterialCategories>? = null
    var filteredMaterial: ArrayList<MaterialCategories>? = null

    var selectedMarkerId: String = ""

    //select station from marker click
    var dropOffPoint: DropOffPoints?= null

    //select station from favorite list
    var dropOffDetail: DropOffDetail?= null

    //current location
    lateinit var mLocationRequest: LocationRequest
    var mLastLocation: Location? = null
    internal var mCurrLocationMarker: Marker? = null
    internal var mFusedLocationClient: FusedLocationProviderClient? = null

    //route
    var apiKey: String = ""
    var polyline: Polyline? = null


    var size: Int = 0
    var part = arrayOfNulls<MultipartBody.Part>(size)

    companion object {
        val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private var dropOffIdDeepLink: String = ""

        fun newInstance(dropOffId: String): DropOffPinFragment {
            dropOffIdDeepLink = dropOffId
            return DropOffPinFragment()
        }
        var TAG = "DropOffPin"
    }

    internal var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude())
                mLastLocation = location

                //store current location in shared preference
                //un comment this to use
                if(mLastLocation!=null){
                    tinyDB!!.putString("DROP_CURRENT_LAT", mLastLocation!!.latitude.toString())
                    tinyDB!!.putString("DROP_CURRENT_LONG", mLastLocation!!.longitude.toString())
                }

                if (mCurrLocationMarker != null) {
                    mCurrLocationMarker?.remove()
                }


                /*val latLng = LatLng(location.latitude, location.longitude) // current location
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title("Current Location")
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                mCurrLocationMarker = mMap!!.addMarker(markerOptions)*/

                //mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11.0F))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tinyDB = TinyDB(MainApplication.applicationContext())

        if (!Places.isInitialized()) Places.initialize(
            MainApplication.applicationContext(),
            requireActivity().getString(R.string.google_maps_key)
        )

        // Fetching API_KEY which we wrapped
        val ai: ApplicationInfo = requireActivity().packageManager
            .getApplicationInfo(requireActivity().packageName, PackageManager.GET_META_DATA)
        val value = ai.metaData["com.google.android.geo.API_KEY"]
        apiKey = value.toString()

    }

    override fun onPause() {
        super.onPause()
        //stop location updates when Activity is no longer active
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.fragment_drop_off_pin, container, false)

        initViews(view)
        setListeners()
        populateData()

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return view
    }

    override fun onResume() {
        super.onResume()

        locationEnabled()

    }

    private fun locationEnabled() {
        val lm = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var gps_enabled = false
        var network_enabled = false
        try {
            gps_enabled = lm!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            network_enabled = lm!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder(requireContext())
                .setTitle("Enable GPS")
                .setMessage("ReLoop requires GPS to get accurate position of drop-off location. Kindly turn on GPS in order to proceed.")
                .setCancelable(false)
                .setPositiveButton(
                    "Settings"
                ) { paramDialogInterface, paramInt -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                //.setNegativeButton("Cancel", null)
                .show()
        }
        else{

            if(mMap!=null && mFusedLocationClient!=null){
                //current location update
                mLocationRequest = LocationRequest()
                mLocationRequest.interval = 120000
                mLocationRequest.fastestInterval = 120000
                mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Location Permission already granted
                        mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback,
                            Looper.myLooper()!!)
                        mMap!!.isMyLocationEnabled = true
                    } else {
                        //Request Location Permission
                        checkLocationPermission()
                    }
                } else {
                    mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper()!!)
                    mMap!!.isMyLocationEnabled = true
                }
            }
        }
    }

    private fun setListeners() {

        back?.setOnClickListener(this)
        txtCity?.setOnClickListener(this)
        txtMaterial?.setOnClickListener(this)
        txtFilter?.setOnClickListener(this)
        txtFavourite?.setOnClickListener(this)
        btnSelectStation?.setOnClickListener(this)
        txtDownArrow?.setOnClickListener(this)
        txtClearFilter?.setOnClickListener(this)
        txtSelectedCities?.setOnClickListener(this)
        btnMainFilterApply?.setOnClickListener(this)


        img_step1?.setImageResource(R.drawable.ic_step_select_station_clicked)
        text_step1?.setTextColor(requireActivity().getColor(R.color.green_color_button))

    }

    private fun initViews(view: View?) {
        img_step1 = view?.findViewById(R.id.img_step1)
        text_step1 = view?.findViewById(R.id.text_step1)

        back = view?.findViewById(R.id.back)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        txtMaterial = view?.findViewById(R.id.txtMaterial)
        txtCity = view?.findViewById(R.id.txtCity)

        llSelectType = view?.findViewById(R.id.llSelectType)
        llFilter = view?.findViewById(R.id.llFilter)

        txtDownArrow = view?.findViewById(R.id.txtDownArrow)
        txtFilter = view?.findViewById(R.id.txtFilter)
        txtFavourite = view?.findViewById(R.id.txtFavourite)
        txtSelectedCities = view?.findViewById(R.id.txtSelectedCities)
        //txtSelectedMaterials = view?.findViewById(R.id.txtSelectedMaterials)
        btnSelectStation = view?.findViewById(R.id.btnSelectStation)
        txtClearFilter = view?.findViewById(R.id.txtClearFilter)

        grdSelectedMaterial = view?.findViewById(R.id.grdSelectedMaterial)

        btnMainFilterApply = view?.findViewById(R.id.btnMainFilterApply)

    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.back->{
                activity?.onBackPressed()
            }
            R.id.txtCity -> {
                if(dependenciesListing?.cities !=null){
                    showCityBottomSheet(dependenciesListing?.cities!!)
                }

            }
            R.id.txtMaterial -> {
                if(dependenciesListing!=null && dependenciesListing!!.materialCategories!=null){
                    if(!dependenciesListing!!.materialCategories!!.isEmpty())
                        showMaterialBottomSheet(dependenciesListing!!.materialCategories!!)
                }

            }
            R.id.btnSelectStation -> {
                showSelectedFilterBottomSheet("")
            }

            R.id.txtDownArrow -> {
                llFilter?.visibility = View.GONE
                llSelectType?.visibility = View.VISIBLE

                //FILTER BUTTONS
                btnMainFilterApply!!.visibility = View.GONE
                btnSelectStation!!.visibility = View.VISIBLE
            }

            R.id.txtFilter -> {
                llFilter?.visibility = View.VISIBLE
                llSelectType?.visibility = View.GONE

                //FILTER BUTTONS
                btnMainFilterApply!!.visibility = View.VISIBLE
                btnSelectStation!!.visibility = View.GONE

            }
            R.id.txtFavourite -> {
                showFavouriteBottomSheet()
            }
            R.id.txtClearFilter -> {

                clearDataOfFilter()

                //change visibility
                txtSelectedCities!!.visibility = View.GONE
                grdSelectedMaterial!!.visibility = View.GONE
                txtClearFilter!!.visibility = View.GONE

                llFilter?.visibility = View.GONE
                llSelectType?.visibility = View.VISIBLE

                populateData()
            }
            R.id.txtSelectedCities -> {
                idCity = ""
                txtSelectedCities!!.text = ""
                txtSelectedCities!!.visibility = View.GONE

            }
            R.id.btnMainFilterApply -> {
                populateDataWithFilter()
            }
        }
    }

    fun clearDataOfFilter(){
        //clear data
        idCity = ""
        if(selectedMaterial!=null){
            selectedMaterial!!.clear()
        }

        if(filteredMaterial!=null){
            for (mItem in filteredMaterial!!) {
                mItem.isCheckedCategory = false
            }
        }

        selectedMarkerId = ""

        //update empty view
        txtSelectedCities!!.text = ""
    }

    fun showFavouriteBottomSheet() {

        /*var favourites: ArrayList<String> = ArrayList()
        favourites.add("Loc1")
        favourites.add("Loc2")
        favourites.add("Loc3")*/

        getFavourite(false)

    }

    fun showCityBottomSheet(cities: ArrayList<DependencyDetail>) {

        /*var cities: ArrayList<String> = ArrayList()
        cities.add("Abu Dhabi")
        cities.add("Dubai")
        cities.add("Sharjah")*/

        val bottomSheetFragment: CityBottomsheet = CityBottomsheet().newInstance(this,cities,"City",idCity)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)

    }

    fun showMaterialBottomSheet(materialCategories: ArrayList<MaterialCategories>) {

        if(filteredMaterial!=null){ // to open already selected materials

            //OLD
//            val bottomSheetFragment: MaterialBottomsheet = MaterialBottomsheet().newInstance(this, filteredMaterial!!,"Material")
//            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)

            //NEW

            if(materialVisibleFilter(filteredMaterial!!).isEmpty()){
                Notify.alerterRed(activity, "No Materials found.")

            }else{
                val bottomSheetFragment: MaterialBottomsheet = MaterialBottomsheet().newInstance(this, materialVisibleFilter(
                    filteredMaterial!!
                ),"Material")
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }



        }else{ // to open fresh material without selected materials

            //OLD
//            val bottomSheetFragment: MaterialBottomsheet = MaterialBottomsheet().newInstance(this,materialCategories,"Material")
//            bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)


            //NEW
            if(materialVisibleFilter(materialCategories).isEmpty()){
                Notify.alerterRed(activity, "No Materials found.")

            }else{
                val bottomSheetFragment: MaterialBottomsheet = MaterialBottomsheet().newInstance(this,materialVisibleFilter(materialCategories),"Material")
                bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
            }
        }
    }

    fun materialVisibleFilter(materialCategories: ArrayList<MaterialCategories>): ArrayList<MaterialCategories> {
        val selectedArray: ArrayList<MaterialCategories> = ArrayList()
        for (listItem in materialCategories) {
            if (listItem.visible_in_dropoff == 1) { //original 1
              selectedArray.add(listItem)
            }

        }
        return selectedArray
    }

    fun showSelectedFilterBottomSheet(dropOffIdDeepLinking: String) {

        if(!dropOffIdDeepLinking.isEmpty()){
            selectedMarkerId = dropOffIdDeepLinking
            dropOffIdDeepLink = ""
        }

        if(!selectedMarkerId.equals("")){
            //new
            val markerID: String = selectedMarkerId

            for (i in dropOffList!!.indices){
                if(markerID.toInt() == dropOffList!!.get(i).id){

                    Log.d("MarkerClick","COMPARE" + markerID)

                    dropOffPoint = dropOffList!!.get(i)
                }
            }

            if(dropOffPoint!=null){

                if(!dropOffIdDeepLinking.isEmpty()){ //User came from deep linking
                    openMaterialCategoryDirectlyFromDeepLinking(dropOffPoint!!.materialCategories!!)
                }else{ //normal redirection
                    val bottomSheetFragment: SelectedFilterBottomsheet = SelectedFilterBottomsheet().newInstance(this,
                        dropOffPoint!!, "")
                    bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                }
            }
        }else{
            Notify.Toast("Please select Drop-Off point from the map to continue")
        }
    }

    fun openMaterialCategoryDirectlyFromDeepLinking(value: ArrayList<MaterialCategories>){
        tinyDB!!.putString("DROP_POINT_ID", dropOffPoint!!.id.toString())
        tinyDB!!.putString("DROP_CITY_ID", dropOffPoint!!.city_id.toString())
        tinyDB!!.putString("DROP_DISTRICT_ID", dropOffPoint!!.district_id.toString())
        tinyDB!!.putString("DROP_LOCATION", dropOffPoint!!.address.toString())
        tinyDB!!.putString("DROP_LATITUDE", dropOffPoint!!.latitude.toString())
        tinyDB!!.putString("DROP_LONGITUDE", dropOffPoint!!.longitude.toString())

        tinyDB!!.putString("DROP_REQUIRE_LOCATION", dropOffPoint!!.require_location.toString())
        tinyDB!!.putString("DROP_REQUIRE_PHOTO", dropOffPoint!!.require_photo.toString())
        tinyDB!!.putString("DROP_REQUIRE_BARCODE", dropOffPoint!!.require_barcode.toString())


        //static value of current location, use listner code for original values
//        tinyDB!!.putString("DROP_CURRENT_LAT", dropOffPoint!!.latitude.toString())
//        tinyDB!!.putString("DROP_CURRENT_LONG", dropOffPoint!!.longitude.toString())


        BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.dropOffPinContainer,
            MaterialSelectFragment.newInstance(value),
            Constants.TAGS.MaterialSelectFragment)
    }

    fun showSelectedFilter(dropOffDetail: DropOffDetail) {

        val bottomSheetFragment: SelectedFilterFromFavouriteBottomsheet = SelectedFilterFromFavouriteBottomsheet().newInstance(this,
            dropOffDetail!!,
            "",true)
        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
    }


    private fun populateData() {

//        val size = 0
//        val part = arrayOfNulls<MultipartBody.Part>(size)

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DROP_OFF_PINS)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getDropOffPins())
            ?.execute()

    }

    private fun addToFavourite(dropOffId: Int) {

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.ADD_TO_FAV_DROP_OFF)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.addToFavDropOff(dropOffId))
            ?.execute()
    }

    private fun getFavourite(value: Boolean) {

        isFromDelete = value

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.GET_FAV_DROP_OFF)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getFavDropOff())
            ?.execute()
    }

    private fun deleteFavourite(dropOffId: Int?) {

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DELETE_FAV_DROP_OFF)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.deleteFavDropOff(dropOffId))
            ?.execute()
    }

    private fun populateDataWithFilter() {

        if(selectedMaterial.isNullOrEmpty() && idCity==""){
            populateData()
        }else{
            if(selectedMaterial!=null){
                if(!selectedMaterial!!.isEmpty()){
                    size = selectedMaterial?.size!!
                    part = arrayOfNulls<MultipartBody.Part>(size)
                    for (i in selectedMaterial!!.indices){
                        part[i] = MultipartBody.Part.createFormData("material_category_id["+i+"]",
                            selectedMaterial!!.get(i).id.toString())
                    }
                }
            }

            val hashMap: HashMap<String?, RequestBody?>? = HashMap()
            hashMap?.put("city_id", RequestBody.create("text/plain".toMediaTypeOrNull(), idCity))

            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.DROP_OFF_PINS)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getDropOffPins(hashMap,part))
                ?.execute()
        }
    }

    private fun getCitiesMaterials(){
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.CITIES_MATERIALS)
            //?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.citiesAndMaterials())
            ?.execute()
    }




    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.DROP_OFF_PINS -> {

                val baseResponse = Utils.getBaseResponse(response)

                val data = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), DropOffPointsModel::class.java)
                Log.e("TAG","===response===" + data)

             /*   val gson = Gson()
                val listType: Type = object : TypeToken<List<DropOffPoints?>?>() {}.type
                dropOffList = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)*/

                dropOffList = data.dropOffPoints

                Log.d("dropOffList",""+GsonBuilder().setPrettyPrinting().create().toJson(dropOffList))

                Log.e(TAG,"===dropOff list===" + dropOffList?.size)

                if(dropOffList?.size!! > 0) {

                    showLocationPin(dropOffList!!)

                    llSelectType!!.visibility = View.VISIBLE

                   /* Handler(Looper.getMainLooper()).postDelayed({
                        if(!llFilter!!.isVisible){
                            llSelectType!!.visibility = View.VISIBLE
                        }
                    }, 3000)*/

                } else{

                    clearMap()

                    Notify.alerterRed(activity, "No Location Found !")
                }

                //get cities and materials
                //getCitiesMaterials()  // instead of calling api get data directly from current api

                dependenciesListing = data.dropOffDependencies
                //check if DeepLink is available
                if(!dropOffIdDeepLink.isEmpty()){
                    showSelectedFilterBottomSheet(dropOffIdDeepLink)
                }
            }
            RequestCodes.API.CITIES_MATERIALS -> {
                val baseResponse = Utils.getBaseResponse(response)

                try {
                    dependenciesListing = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dependencies::class.java
                    )
                } catch (e: Exception) {
                    Log.e("DropOff Error", e.toString())
                }

                //check if DeepLink is available
                if(!dropOffIdDeepLink.isEmpty()){
                    showSelectedFilterBottomSheet(dropOffIdDeepLink)
                }
            }
            RequestCodes.API.ADD_TO_FAV_DROP_OFF -> {
                val baseResponse = Utils.getBaseResponse(response)

                try {
                    Notify.alerterGreen(requireActivity(),""+ baseResponse!!.message)
                }catch (e: Exception){
                    e.printStackTrace()
                }

            }
            RequestCodes.API.GET_FAV_DROP_OFF -> {
                val baseResponse = Utils.getBaseResponse(response)

                val gson = Gson()
                val listType: Type = object : TypeToken<List<FavDropOffPoints?>?>() {}.type
                favDropOffList = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)

                if(favDropOffList!=null){

                    if(favDropOffList!!.size>0){
                        val bottomSheetFragment: FavouritesBottomsheet = FavouritesBottomsheet().newInstance(this,
                            favDropOffList!!,"Favorite Drop-off Points")
                        bottomSheetFragment.show(childFragmentManager, bottomSheetFragment.tag)
                    }else{
                        Notify.alerterRed(requireActivity(),"No drop-off added in your favorite list")
                    }

                }

                //to refresh the main list after remove favorite
                if(isFromDelete){
                    Handler(Looper.getMainLooper()).postDelayed({
                        populateData()
                    }, 3000)
                }



            }
            RequestCodes.API.DELETE_FAV_DROP_OFF -> {
                val baseResponse = Utils.getBaseResponse(response)
                Notify.alerterGreen(requireActivity(),""+ baseResponse!!.message)

                getFavourite(true)



            }


            /*RequestCodes.API.MATERIAL_CATEGORIES -> {
                val baseResponse = Utils.getBaseResponse(response)

                val gson = Gson()
                val listType: Type = object : TypeToken<List<MaterialCategories?>?>() {}.type
                materialCategories = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)


            }*/


        }
    }

    fun clearMap(){
        if(mMap!=null){
            mMap!!.clear()
            mSelectedMarker = null
        }
    }

    private fun showLocationPin(dropOffList: ArrayList<DropOffPoints>) {

        clearMap()

        for (i in dropOffList.indices){

            //NEW ADDED
            visible_on_map = dropOffList.get(i).visible_on_map!!



            if(visible_on_map == 1){
                //NEW HERE
                if(dropOffList.get(i).latitude != null && dropOffList.get(i).longitude !=null &&
                    dropOffList.get(i).latitude.toString() != "" && dropOffList.get(i).longitude.toString() != ""){
                    val latLong = LatLng(dropOffList.get(i).latitude!!,dropOffList.get(i).longitude!!)

                    //ORIGINAL
                    mMap?.addMarker(
                        MarkerOptions().position(latLong)
                            //.title(""+dropOffList.get(i).address) //for title of marker
                            .icon(BitmapFromVector(requireActivity(), R.drawable.icon_marker))
                    )!!.tag = dropOffList.get(i).id
                }
            }
        }
        val latLong = LatLng(dropOffList.get(0).latitude!!,dropOffList.get(0).longitude!!)
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 8f))

    }

    private fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        requireActivity().onBackPressed()
    }


    override fun onMapReady(map: GoogleMap) {
        mMap = map;

        map.setOnMarkerClickListener(object : OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                try {
                    Log.d("MarkerClick","" + marker.id)
                    Log.d("MarkerClick","" + marker.title)
                    Log.d("MarkerClick","CLICK" + marker.tag)

                    //FILTER BUTTONS
                    btnMainFilterApply!!.visibility = View.GONE
                    btnSelectStation!!.visibility = View.VISIBLE

                    //original
                    //selectedMarkerId = marker.id

                    //new
                    selectedMarkerId = marker.tag.toString()



                    if (null != mSelectedMarker) {
                        mSelectedMarker!!.setIcon(BitmapFromVector(requireActivity(), R.drawable.icon_marker));
                    }
                    mSelectedMarker = marker;
                    mSelectedMarker!!.setIcon(BitmapFromVector(requireActivity(), R.drawable.icon_marker_green));


                    showSelectedFilterBottomSheet("")

                }catch (e: Exception){
                    e.printStackTrace()
                }

                return false;
            }

        })

        //current location update
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 120000 // two minute interval
        mLocationRequest.fastestInterval = 120000
        mLocationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper()!!
                )
                mMap!!.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper()!!
            )
            mMap!!.isMyLocationEnabled = true
        }

    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(requireContext())
                    .setTitle("Location Permission Needed")
                    .setCancelable(false)
                    .setMessage("ReLoop requires permission to access location for verifying the drop-off location. Kindly allow permission to location in order to proceed.")
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )

                        try {
                            activity?.onBackPressed()
                            //Redirect to setting new added
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" +
                                        BuildConfig.APPLICATION_ID))
                            requireContext().startActivity(intent)
                        }catch (e: Exception){
                            e.printStackTrace()
                        }

                    }
                    .create()
                    .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    /*fun showPermissionRequiredDialog() {
        val alertDialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
        alertDialog.setTitle("title")
        alertDialog.setMessage("your message to user")
        alertDialog.setPositiveButton("Ok", DialogInterface.OnClickListener { dialog, which ->
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" +
                    BuildConfig.APPLICATION_ID))
            requireContext().startActivity(intent)
        })
        alertDialog.show()

    }*/


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {

                        mFusedLocationClient?.requestLocationUpdates(
                            mLocationRequest,
                            mLocationCallback,
                            Looper.myLooper()!!
                        )
                        mMap!!.setMyLocationEnabled(true)
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(requireContext(), "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }


    override fun onMapClick(p0: LatLng) {
        if(mSelectedMarker != null) {
            mSelectedMarker!!.setIcon(BitmapFromVector(requireActivity(), R.drawable.icon_marker_green));
        }
        mSelectedMarker = null;
    }

    override fun onCameraChange(p0: CameraPosition) {
    }



    //from marker click Start-Drop-Off
    override fun onSelectStation(value: ArrayList<MaterialCategories>) {

        //store data in local for final api call [[ MARKER_CLICK ]]

        tinyDB!!.putString("DROP_POINT_ID", dropOffPoint!!.id.toString())
        tinyDB!!.putString("DROP_CITY_ID", dropOffPoint!!.city_id.toString())
        tinyDB!!.putString("DROP_DISTRICT_ID", dropOffPoint!!.district_id.toString())
        tinyDB!!.putString("DROP_LOCATION", dropOffPoint!!.address.toString())
        tinyDB!!.putString("DROP_LATITUDE", dropOffPoint!!.latitude.toString())
        tinyDB!!.putString("DROP_LONGITUDE", dropOffPoint!!.longitude.toString())

        tinyDB!!.putString("DROP_REQUIRE_LOCATION", dropOffPoint!!.require_location.toString())
        tinyDB!!.putString("DROP_REQUIRE_PHOTO", dropOffPoint!!.require_photo.toString())
        tinyDB!!.putString("DROP_REQUIRE_BARCODE", dropOffPoint!!.require_barcode.toString())


        //static value of current location, use listner code for original values
//        tinyDB!!.putString("DROP_CURRENT_LAT", dropOffPoint!!.latitude.toString())
//        tinyDB!!.putString("DROP_CURRENT_LONG", dropOffPoint!!.longitude.toString())


        BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.dropOffPinContainer,
            MaterialSelectFragment.newInstance(value),
            Constants.TAGS.MaterialSelectFragment)
    }

    //From favorite screen start-drop-off
    override fun onSelectStationFromFavorite(value: ArrayList<MaterialCategories>) {
        //store data in local for final api call [[ MARKER_CLICK ]]

        tinyDB!!.putString("DROP_POINT_ID", dropOffDetail!!.id.toString())
        tinyDB!!.putString("DROP_CITY_ID", dropOffDetail!!.city_id.toString())
        tinyDB!!.putString("DROP_DISTRICT_ID", dropOffDetail!!.district_id.toString())
        tinyDB!!.putString("DROP_LOCATION", dropOffDetail!!.address.toString())
        tinyDB!!.putString("DROP_LATITUDE", dropOffDetail!!.latitude.toString())
        tinyDB!!.putString("DROP_LONGITUDE", dropOffDetail!!.longitude.toString())

        tinyDB!!.putString("DROP_REQUIRE_LOCATION", dropOffDetail!!.require_location.toString())
        tinyDB!!.putString("DROP_REQUIRE_PHOTO", dropOffDetail!!.require_photo.toString())
        tinyDB!!.putString("DROP_REQUIRE_BARCODE", dropOffDetail!!.require_barcode.toString())


        //static value of current location, use listner code for original values
//        tinyDB!!.putString("DROP_CURRENT_LAT", dropOffDetail!!.latitude.toString())
//        tinyDB!!.putString("DROP_CURRENT_LONG", dropOffDetail!!.longitude.toString())


        BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.dropOffPinContainer,
            MaterialSelectFragment.newInstance(value),
            Constants.TAGS.MaterialSelectFragment)
    }

    override fun onAddToFavourite(dropOffId: Int) {
        addToFavourite(dropOffId)
    }

    override fun onNavigateToLocation(latitude: String, longitude: String) {
        //drawRoute(latitude,longitude)

        checkLocationPermission()

        navigateToLocation(latitude,longitude)
    }

    private fun navigateToLocation(latitude: String, longitude: String){
        if(mLastLocation!=null){
            try {

                //Original location
                val uri = Uri.parse("http://maps.google.com/maps?saddr="+
                        mLastLocation!!.latitude +","+ mLastLocation!!.longitude+" &daddr="+
                        latitude +","+ longitude +" &dirflg=d")

                //static location
                /*val uri = Uri.parse("http://maps.google.com/maps?saddr=24.9319811,55.4539556 &daddr="+
                        latitude +","+ longitude +" &dirflg=d")*/

                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }catch (e: Exception){
                Toast.makeText(requireContext(),"Google Map Not Found",Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun drawRoute(latitude: String, longitude: String) {

        try {

            if(mLastLocation!=null){

                if(polyline!= null){
                    polyline!!.remove()
                }



                //current location
                val originLocation = LatLng(mLastLocation!!.latitude, mLastLocation!!.longitude)

                //static location
//                val originLocation = LatLng(24.9319811, 55.4539556)

                val destinationLocation = LatLng(latitude.toDouble(), longitude.toDouble())

                val pathUrl = getDirectionURL(originLocation, destinationLocation, apiKey)
                Log.d("PATHURL",""+pathUrl)
                GetDirection(pathUrl).execute()
                mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))

            }



        }catch (e: Exception){
            e.printStackTrace()
            Log.d("DRAW_ROUTE","Error in draw route")
        }


    }


    override fun onStartDropOffFavourite(selected: ArrayList<FavDropOffPoints>) {

        //store data in local for final api call [[ FAVORITE_CLICK ]]

        tinyDB!!.putString("DROP_POINT_ID", selected.get(0).drop_off_point_id.toString())
        tinyDB!!.putString("DROP_CITY_ID", selected.get(0).drop_off_details!!.city_id.toString())
        tinyDB!!.putString("DROP_DISTRICT_ID", selected.get(0).drop_off_details!!.district_id.toString())
        tinyDB!!.putString("DROP_LOCATION", selected.get(0).drop_off_details!!.address.toString())
        tinyDB!!.putString("DROP_LATITUDE", selected.get(0).drop_off_details!!.latitude.toString())
        tinyDB!!.putString("DROP_LONGITUDE", selected.get(0).drop_off_details!!.longitude.toString())

        tinyDB!!.putString("DROP_REQUIRE_LOCATION", selected.get(0).drop_off_details!!.require_location.toString())
        tinyDB!!.putString("DROP_REQUIRE_PHOTO", selected.get(0).drop_off_details!!.require_photo.toString())
        tinyDB!!.putString("DROP_REQUIRE_BARCODE", selected.get(0).drop_off_details!!.require_barcode.toString())


        //static value of current location, use listner code for original values
//        tinyDB!!.putString("DROP_CURRENT_LAT", selected.get(0).drop_off_details!!.latitude.toString())
//        tinyDB!!.putString("DROP_CURRENT_LONG", selected.get(0).drop_off_details!!.longitude.toString())



        BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.dropOffPinContainer,
            MaterialSelectFragment.newInstance(selected.get(0).drop_off_details!!.materialCategories),
            Constants.TAGS.MaterialSelectFragment)
    }

    override fun onDeleteFavourite(dropOffId: Int?,pos: Int) {
        deleteFavourite(dropOffId)
    }

    override fun onCheckDropOffStation(latitude: String, longitude: String,dropDetail: DropOffDetail) {
        //drawRoute(latitude,longitude)

        dropOffDetail = dropDetail
        showSelectedFilter(dropOffDetail!!)
    }

    //CITY FILTER
    override fun onFilterSelected(selected: String, cityId: String) {

        if(!selected.equals("") && !cityId.equals("")){
            idCity = cityId
            txtSelectedCities!!.visibility = View.VISIBLE
            //Toast.makeText(requireContext(),""+selected,Toast.LENGTH_SHORT).show()
            txtSelectedCities!!.text = ""+selected

            populateDataWithFilter()

            //FILTER BUTTONS
            btnMainFilterApply!!.visibility = View.VISIBLE
            btnSelectStation!!.visibility = View.GONE
            txtClearFilter!!.visibility =View.VISIBLE

        }

    }

    //MATERIAL FILTER
    override fun onFilterMaterial(materialData: ArrayList<MaterialCategories>) {
        Log.d("MATERIAL", ""+GsonBuilder().setPrettyPrinting().create().toJson(materialData))


        if(!materialData.isEmpty()){

            filteredMaterial = materialData

            //get only selected materials to call api
            val selectedArray: ArrayList<MaterialCategories> = ArrayList()
            for (listItem in materialData) {
                if (listItem.isCheckedCategory == true) {
                    selectedArray.add(listItem)
                }
            }

            if(!selectedArray.isEmpty()){
                selectedMaterial = selectedArray

                grdSelectedMaterial!!.visibility =View.VISIBLE

                val materialAdapter = SelectedMaterialAdapter(selectedMaterial!!, requireContext())
                grdSelectedMaterial!!.adapter = materialAdapter

                //NEW
                grdSelectedMaterial!!.setOnItemClickListener(object : TwoWayAdapterView.OnItemClickListener {
                    override fun onItemClick(
                        parent: TwoWayAdapterView<*>?,
                        v: View?,
                        position: Int,
                        id: Long
                    ) {
                        if(filteredMaterial!=null){
                            for (mItem in filteredMaterial!!) {
                                if(selectedMaterial!!.get(position).name == mItem.name){
                                    mItem.isCheckedCategory = false
                                }
                            }
                        }



                        selectedMaterial!!.removeAt(position)
                        materialAdapter.notifyDataSetChanged()

                        if(selectedMaterial!!.isEmpty()){
                            grdSelectedMaterial!!.visibility = View.GONE
                        }
                    }
                })

                //OLD
                /*grdSelectedMaterial!!.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    *//*Toast.makeText(
                      requireContext(), selectedMaterial!![position].id.toString() + " selected",
                      Toast.LENGTH_SHORT
                    ).show()*//*




                    //to update the already apply filter when bottomsheet open next time
                    if(filteredMaterial!=null){
                        for (mItem in filteredMaterial!!) {
                            if(selectedMaterial!!.get(position).name == mItem.name){
                                mItem.isCheckedCategory = false
                            }
                        }
                    }

                    selectedMaterial!!.removeAt(position)
                    materialAdapter.notifyDataSetChanged()



                }*/
                populateDataWithFilter()

                //FILTER BUTTONS
                btnMainFilterApply!!.visibility = View.VISIBLE
                btnSelectStation!!.visibility = View.GONE
                txtClearFilter!!.visibility =View.VISIBLE

            }

        }
    }
    

    internal class SelectedMaterialAdapter(
        private val materialList: List<MaterialCategories>,
        private val context: Context
    ) :
        BaseAdapter() {
        private var layoutInflater: LayoutInflater? = null
        private lateinit var txtCatName: TextView
        private lateinit var imgClose: ImageView

        override fun getCount(): Int {
            return materialList.size
        }

        override fun getItem(position: Int): Any? {
            return null
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            var convertView = convertView

            if (layoutInflater == null) {
                layoutInflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            }

            if (convertView == null) {
                convertView = layoutInflater!!.inflate(R.layout.item_materials, null)
            }

            imgClose = convertView!!.findViewById(R.id.imgClose)
            txtCatName = convertView.findViewById(R.id.txtCatName)

            txtCatName.setText(materialList.get(position).name)

            return convertView
        }
    }


    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data, MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.RED)
                lineoption.geodesic(true)
            }

            polyline = mMap!!.addPolyline(lineoption)
        }
    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }


}