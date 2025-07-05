package com.android.reloop.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.android.reloop.model.BarcodeModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DropOffResult
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type


class ConfirmSelectionsFragment : BaseFragment(),View.OnClickListener, OnNetworkResponse {

    var img_step4: ImageView? = null
    var text_step4: TextView?= null

    var back: Button? = null
    var btnNext:Button? = null

    var txtName:TextView?=null
    var txtMobile: TextView?=null
    var txtCategories: TextView?=null
    var txtLocation: TextView?=null

    var remember_me: CheckBox?=null

    var editComments: CustomEditText? = null

    //shredpreferences
    var dropLocation: String = ""
    var dropLat: String = ""
    var dropLong: String = ""
    var dropCityId: String = ""
    var dropDistrictId: String = ""
    var dropPointId: String = ""

    var dropOffResult = DropOffResult()


    var selectedMaterials: ArrayList<MaterialCategories> = ArrayList()

    private var imageFiles: ArrayList<File> = ArrayList()
    private var compressedImageFiles: ArrayList<File> = ArrayList()

    companion object {
        var selectedMaterialCategories: ArrayList<MaterialCategories>? = null
        var capturedImageList: ArrayList<String>? = null
        var capturedBarcodeList: ArrayList<BarcodeModel>? = null

        fun newInstance(
            materials: ArrayList<MaterialCategories>?,
            imgList: ArrayList<String>,
            barcodeList: ArrayList<BarcodeModel>
        ): ConfirmSelectionsFragment {

            this.selectedMaterialCategories = materials
            this.capturedImageList = imgList
            this.capturedBarcodeList = barcodeList

            return ConfirmSelectionsFragment()
        }
        var TAG = "DropOffPin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.fragment_confirm_selections, container, false)

        initViews(view)
        setListeners()
        setData()
        return view
    }

    private fun setData() {
        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())

        dropLocation = tinyDB.getString("DROP_LOCATION").toString()
        dropLat = tinyDB.getString("DROP_LATITUDE").toString()
        dropLong = tinyDB.getString("DROP_LONGITUDE").toString()
        dropCityId = tinyDB.getString("DROP_CITY_ID").toString()
        dropDistrictId = tinyDB.getString("DROP_DISTRICT_ID").toString()
        dropPointId = tinyDB.getString("DROP_POINT_ID").toString()

        txtName!!.text = " " + User.retrieveUser()!!.first_name +" " + User.retrieveUser()!!.last_name
        txtMobile!!.text = " " + User.retrieveUser()!!.phone_number
        txtLocation!!.text = " " + tinyDB.getString("DROP_LOCATION").toString()

        selectedMaterials!!.clear()
        if(!selectedMaterialCategories.isNullOrEmpty()){
            for (i in selectedMaterialCategories!!.indices){
                if(selectedMaterialCategories!!.get(i).selected == true){
                    selectedMaterials!!.add(selectedMaterialCategories!!.get(i))
                }
            }
        }

        for (i in selectedMaterials!!.indices){
            if(i == selectedMaterials!!.size-1){
                txtCategories!!.setText(""+txtCategories!!.text + " "+ selectedMaterials!!.get(i).name)
            }else{
                txtCategories!!.setText(""+txtCategories!!.text + " "+ selectedMaterials!!.get(i).name +",")
            }
        }

        //createDropOffRequest()

        Log.d("FINAL_VALUES","DROP_USER_NAME"+ User.retrieveUser()!!.first_name + User.retrieveUser()!!.last_name)
        Log.d("FINAL_VALUES","DROP_MOBILE"+ User.retrieveUser()!!.phone_number)


        Log.d("FINAL_VALUES","DROP_POINT_ID"+tinyDB.getString("DROP_POINT_ID").toString())
        Log.d("FINAL_VALUES","DROP_CITY_ID"+tinyDB.getString("DROP_CITY_ID").toString())
        Log.d("FINAL_VALUES","DROP_DISTRICT_ID"+tinyDB.getString("DROP_DISTRICT_ID").toString())
        Log.d("FINAL_VALUES","DROP_LOCATION"+tinyDB.getString("DROP_LOCATION").toString())
        Log.d("FINAL_VALUES","DROP_LATITUDE"+tinyDB.getString("DROP_LATITUDE").toString())
        Log.d("FINAL_VALUES","DROP_LONGITUDE"+tinyDB.getString("DROP_LONGITUDE").toString())

        //materials
        Log.d("FINAL_VALUES","DROP_MATERIALS"+GsonBuilder().setPrettyPrinting().create().toJson(
            selectedMaterialCategories))

        //images
        Log.d("FINAL_VALUES","DROP_IMAGES"+GsonBuilder().setPrettyPrinting().create().toJson(
            capturedImageList))

        //barcodes
        Log.d("FINAL_VALUES","DROP_BARCODES"+GsonBuilder().setPrettyPrinting().create().toJson(
            capturedBarcodeList))

    }

    fun createDropOffRequest(){
        val hashMap: HashMap<String?, RequestBody?>? = HashMap()

        hashMap?.put("first_name", RequestBody.create("text/plain".toMediaTypeOrNull(), User.retrieveUser()!!.first_name.toString()))

        hashMap?.put("last_name", RequestBody.create("text/plain".toMediaTypeOrNull(), User.retrieveUser()!!.last_name.toString()))

        hashMap?.put("phone_number", RequestBody.create("text/plain".toMediaTypeOrNull(), User.retrieveUser()!!.phone_number.toString()))

        hashMap?.put("location", RequestBody.create("text/plain".toMediaTypeOrNull(), dropLocation))

        hashMap?.put("latitude", RequestBody.create("text/plain".toMediaTypeOrNull(), dropLat))

        hashMap?.put("longitude", RequestBody.create("text/plain".toMediaTypeOrNull(), dropLong))

        hashMap?.put("city_id", RequestBody.create("text/plain".toMediaTypeOrNull(), dropCityId))

        hashMap?.put("district_id", RequestBody.create("text/plain".toMediaTypeOrNull(), dropDistrictId))

        hashMap?.put("organization_name", RequestBody.create("text/plain".toMediaTypeOrNull(), ""))

        hashMap?.put("user_comments", RequestBody.create("text/plain".toMediaTypeOrNull(), editComments!!.text.toString()))

        hashMap?.put("map_location", RequestBody.create("text/plain".toMediaTypeOrNull(), dropLocation))

        hashMap?.put("drop_off_point_id", RequestBody.create("text/plain".toMediaTypeOrNull(), dropPointId))

        //selected materials
        if(!selectedMaterials.isNullOrEmpty()){
            for (i in selectedMaterials!!.indices){
                hashMap?.put(
                    "material_categories[${i}][id]", RequestBody.create(
                        "text/plain".toMediaTypeOrNull(), selectedMaterials!!.get(i).id.toString())
                )
            }
        }

        //barcode
        if(!capturedBarcodeList.isNullOrEmpty()){
            for (i in capturedBarcodeList!!.indices){
                hashMap?.put("barcodes[${i}]", RequestBody.create("text/plain".toMediaTypeOrNull(), capturedBarcodeList!!.get(i).barcodeResult))
            }
        }

        val imageMap: HashMap<String?, MultipartBody.Part?> = HashMap()


        //images
        if(!capturedImageList.isNullOrEmpty()){

            val size = capturedImageList?.size
            val part = arrayOfNulls<MultipartBody.Part>(size!!)

            for (i in 0 until capturedImageList!!.size) {

                var fileUri: Uri
                fileUri = Uri.fromFile(File(capturedImageList!!.get(i)))
                val file = File(fileUri.path!!)

                part[i] = MultipartBody.Part.createFormData(
                    "images[${i}]",
                    file.name,
                    RequestBody.create("image/*".toMediaTypeOrNull(), file)
                )
            }

            NetworkCall.make()
                ?.setCallback(this)
                ?.autoLoading(requireActivity())
                ?.setTag(RequestCodes.API.DROP_OFF_REQUEST)
                ?.enque(Network().apis()?.dropOffRequest(part, hashMap))
                ?.execute()

        }else{

            NetworkCall.make()
                ?.setCallback(this)
                ?.autoLoading(requireActivity())
                ?.setTag(RequestCodes.API.DROP_OFF_REQUEST)
                ?.enque(Network().apis()?.dropOffRequest(arrayOfNulls<MultipartBody.Part>(0), hashMap))
                ?.execute()
        }
    }


    private fun createImageFile(imagePaths: ArrayList<String>):  ArrayList<File>{
        imageFiles = ArrayList()

        for (i in 0 until imagePaths.size) {
            try {
                imageFiles.add(File(imagePaths[i]))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return imageFiles
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        btnNext?.setOnClickListener(this)

        img_step4?.setImageResource(R.drawable.ic_step_confirm_selections_clicked)
        text_step4?.setTextColor(requireActivity().getColor(R.color.green_color_button))

    }

    private fun initViews(view: View?) {

        img_step4 = view?.findViewById(R.id.img_step4)
        text_step4 = view?.findViewById(R.id.text_step4)

        remember_me = view?.findViewById(R.id.remember_me)

        back = view?.findViewById(R.id.back)
        btnNext = view?.findViewById(R.id.btnNext)
        editComments = view?.findViewById(R.id.editComments)

        txtName= view?.findViewById(R.id.txtName)
        txtMobile = view?.findViewById(R.id.txtMobile)
        txtCategories= view?.findViewById(R.id.txtCategories)
        txtLocation= view?.findViewById(R.id.txtLocation)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
            R.id.btnNext -> {

                if(remember_me!!.isChecked){
                    createDropOffRequest()
                }else{
                    Notify.alerterRed(activity, "Please confirm drop-off details")
                }
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.DROP_OFF_REQUEST -> {
                try {

                    val gson = Gson()
                    val listType: Type = object : TypeToken<DropOffResult>() {}.type
                    dropOffResult = gson.fromJson(Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>), listType)

                    Log.d("FINAL_RESULT", " DATA :> "+GsonBuilder().setPrettyPrinting().create().toJson(baseResponse.data))

                    Log.d("FINAL_RESULT", ""+GsonBuilder().setPrettyPrinting().create().toJson(dropOffResult.collection_request))

                    val message = baseResponse.message
                    Notify.alerterGreen(activity, message)

                    if(baseResponse.status == true){

                        if(!dropOffResult!!.collection_request!!.isEmpty()){
                            BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.confirmSelectionsContainer,
                                RequestSucceededFragment.newInstance(dropOffResult!!.collection_request!!.get(0)),
                                Constants.TAGS.RequestSuccessFragment)
                        }else{
                            Notify.alerterRed(activity, "Drop Off ID not found")
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }


}