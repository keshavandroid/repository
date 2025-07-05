package com.reloop.reloop.fragments


import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import java.io.IOException
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class MapsFragment : BaseFragment(), OnMapReadyCallback,View.OnClickListener,GoogleMap.OnCameraChangeListener {

    companion object {
        fun newInstance(): MapsFragment {
            return MapsFragment()
        }
    }
    private var mapFragment: SupportMapFragment? = null
    var address: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Places.isInitialized()) Places.initialize(
            MainApplication.applicationContext(),
            requireActivity().getString(R.string.google_maps_key)
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_maps, container, false)
        val view: View? = inflater.inflate(R.layout.fragment_maps, container, false)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        address = view?.findViewById(R.id.address)

    }

    private fun setListeners() {

    }

    private fun populateData() {

    }

    /*override fun onMapReady(googleMap: GoogleMap?) {

    }*/

    override fun onClick(view: View?) {

    }

    @SuppressLint("SetTextI18n")
    private fun getAddress(lat: Double?, lng: Double?) {
        val geoCoder = Geocoder(requireActivity(), Locale.getDefault())
        try {
            val addresses = lat?.let { lng?.let { it1 -> geoCoder.getFromLocation(it, it1, 1) } }
            try {
                val obj = addresses?.get(0)
                /*when {
                    obj.subLocality != null -> {
                        selectedPlaceName.setText(obj.subLocality)
                    }
                    obj.locality != null -> {
                        selectedPlaceName.setText(obj.locality)
                    }
                    obj.countryName != null -> {
                        selectedPlaceName.setText(obj.countryName)
                    }
                    obj.featureName != null -> {
                        selectedPlaceName.setText(obj.featureName)
                    }
                    //                addresses=FeatureName+" "+SubLocality+" "+Locality+" -'"+CountryName;
                }*/
                var featureName = obj?.featureName
                var subLocality = obj?.subLocality
                var locality = obj?.locality
                var countryName = obj?.countryName
                if (featureName == null) featureName = " "
                if (subLocality == null) subLocality = " "
                if (locality == null) locality = " "
                if (countryName == null) countryName = " "
                address?.text = "$featureName $subLocality $locality -'$countryName"
            } catch (exception: IndexOutOfBoundsException) {
                Log.v("IGA", "Array index out of bound")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            //Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    /*override fun onCameraChange(cameraPosition: CameraPosition?) {
        val latLng:LatLng?=cameraPosition?.target
            getAddress(latLng?.latitude,latLng?.longitude)
    }
*/
    override fun onMapReady(p0: GoogleMap) {

    }

    override fun onCameraChange(p0: CameraPosition) {
        val latLng = p0.target
        getAddress(latLng.latitude, latLng.longitude)
    }

}
