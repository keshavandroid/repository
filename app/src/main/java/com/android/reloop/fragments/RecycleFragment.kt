package com.reloop.reloop.fragments


import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.interfaces.StepView
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DataParsing
import com.reloop.reloop.network.serializer.collectionrequest.CollectionRequest
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategoryRelation
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.settings.AboutAppData
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
class RecycleFragment : BaseFragment(), View.OnClickListener, StepView, OnNetworkResponse,
    ChildToParent {

    var EVENTTAG = "Event Recycle Page";
    private var selected: Boolean = false
    var collectionRequest: CollectionRequest? =
        CollectionRequest()

    private var imagePart: MultipartBody.Part? = null

    //SINGLE
    var part: MultipartBody.Part? = null

    //MULTIPLE
    var multipartParts: List<MultipartBody.Part>? = null



    companion object {
        var getPlans: GetPlans? = null
        var userContainSingleCollectionRequest: Boolean = false
        var editCollectionRequest: CollectionRequests? = null

        fun newInstance(
            plans: GetPlans?,
            userContainSingleCollectionRequest: Boolean,
            editCollectionRequest: CollectionRequests?
        ): RecycleFragment {
            this.getPlans = plans
            this.userContainSingleCollectionRequest = userContainSingleCollectionRequest
            this.editCollectionRequest = editCollectionRequest
            return RecycleFragment()
        }

        var parentToChild: ParentToChild? = null
        var stepView: StepView? = null
        var next: Button? = null
        var isAddedFragment = false
        var changeAddressDistrictID: Int? = 0
    }

    override fun onDetach() {
        super.onDetach()
        isAddedFragment = false
    }

    var currentStep: Int? = -1
    var imageStep1: ImageView? = null
    var textStep1: TextView? = null
    var imageStep2: ImageView? = null
    var textStep2: TextView? = null
    var imageStep3: ImageView? = null
    var textStep3: TextView? = null
    var imageStep4: ImageView? = null
    var textStep4: TextView? = null
    var textaccptMtr: TextView? = null

    var back: Button? = null
    var create: Button? = null
    private var materialCategories: ArrayList<MaterialCategories>? = ArrayList()

    var materialCategoryRelation: MaterialCategoryRelation? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_recycle, container, false)
        initViews(view)
        setListeners()
        populateData()

        return view
    }

    private fun initViews(view: View?) {

        imageStep1 = view?.findViewById(R.id.img_step1)
        imageStep2 = view?.findViewById(R.id.img_step2)
        imageStep3 = view?.findViewById(R.id.img_step3)
        imageStep4 = view?.findViewById(R.id.img_step4)

        textStep1 = view?.findViewById(R.id.text_step1)
        textStep2 = view?.findViewById(R.id.text_step2)
        textStep3 = view?.findViewById(R.id.text_step3)
        textStep4 = view?.findViewById(R.id.text_step4)

        next = view?.findViewById(R.id.next)
        back = view?.findViewById(R.id.back)
        create = view?.findViewById(R.id.create)
        textaccptMtr = view?.findViewById(R.id.btn_accpt)

    }

    private fun setListeners() {
        stepView = this
        next?.setOnClickListener(this)
        back?.setOnClickListener(this)
        create?.setOnClickListener(this)
        textaccptMtr?.setOnClickListener(this)
    }

    private fun populateData() {
        LogManager.getLogManager().writeLog("$EVENTTAG : Executing Material Categories Service Call")

        if(editCollectionRequest==null){
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.MATERIAL_CATEGORIES)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getMaterialCategories())
                ?.execute()
            isAddedFragment = true
        }else{
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.MATERIAL_CATEGORIES)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getMaterialCategories(editCollectionRequest!!.request_collection!!.get(0).request_id))
                ?.execute()
            isAddedFragment = true
        }


    }

    //------------------------------Update StepView UI-----------------------------
    override fun StepNumber(stepNumber: Int) {
        currentStep = stepNumber
        when (stepNumber) {
            Constants.recycleStep1 -> {
                BaseActivity.stepViewUpdateUI(
                    imageStep1,
                    R.drawable.icon_select_categories_en,
                    textStep1,
                    requireActivity().getColor(R.color.green_color_button)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep2,
                    R.drawable.icon_day_selection_un,
                    textStep2,
                    requireActivity().getColor(R.color.text_color_heading)
                )
            }

            Constants.recycleStep2 -> {

                BaseActivity.stepViewUpdateUI(
                    imageStep1,
                    R.drawable.icon_select_categories_un,
                    textStep1,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep2,
                    R.drawable.icon_day_selection_en,
                    textStep2,
                    requireActivity().getColor(R.color.green_color_button)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep3,
                    R.drawable.icon_confirm_selection_un,
                    textStep3,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                next?.text = getString(R.string.next)
            }
            Constants.recycleStep3 -> {

                BaseActivity.stepViewUpdateUI(
                    imageStep2,
                    R.drawable.icon_day_selection_un,
                    textStep2,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep3,
                    R.drawable.icon_confirm_selection_en,
                    textStep3,
                    requireActivity().getColor(R.color.green_color_button)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep4,
                    R.drawable.icon_request_submitted_un,
                    textStep4,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                back?.visibility = View.VISIBLE
                next?.visibility = View.VISIBLE
                create?.visibility = View.GONE

                if(editCollectionRequest!=null){ //if editCollectionRequest NOT NULL than it is Edit/UPDATE Flow
                    next?.text = getString(R.string.txt_update)
                }else{
                    next?.text = getString(R.string.proceed)
                }
            }
            Constants.recycleStep4 -> {
                BaseActivity.stepViewUpdateUI(
                    imageStep3,
                    R.drawable.icon_confirm_selection_un,
                    textStep3,
                    requireActivity().getColor(R.color.text_color_heading)
                )
                BaseActivity.stepViewUpdateUI(
                    imageStep4,
                    R.drawable.icon_request_submitted_en,
                    textStep4,
                    requireActivity().getColor(R.color.green_color_button)
                )
                back?.visibility = View.GONE
                next?.visibility = View.GONE
                create?.visibility = View.VISIBLE

            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.next -> {
                LogManager.getLogManager().writeLog("$EVENTTAG : Next Button pressed")
                selected = false
                if (SelectCategoriesFragment.materialCategories != null && SelectCategoriesFragment.materialCategories?.size!! > 0) {
                    for (i in SelectCategoriesFragment.materialCategories!!.indices) {
                        if (SelectCategoriesFragment.materialCategories!![i].selected!!) {
                            LogManager.getLogManager().writeLog("Selected Category : ${SelectCategoriesFragment.materialCategories!![i].name} ,Id : ${SelectCategoriesFragment.materialCategories!![i].id}")
                            selected = true
                        }
                    }
                }

                if (selected) {
                    if (parentToChild != null) {
                        parentToChild?.callChild()
                    }
                }
                else{
                    Notify.alerterRed(activity, "Please select at least one category")
                    LogManager.getLogManager().writeLog("$EVENTTAG : No category Selected")
                }

                /*    if (SelectCategoriesFragment.materialCategories != null && SelectCategoriesFragment.materialCategories?.size!! > 0) {
                        for (i in SelectCategoriesFragment.materialCategories!!.indices) {
                            if (SelectCategoriesFragment.materialCategories!![i].selected!!) {
                                Log.e("TAG","====for if called===")
                                if (parentToChild != null) {
                                    parentToChild?.callChild()
                                }
                                break
                            }

                    }
                }
                else{
                    Notify.alerterRed(activity, "Please select at least one category")
                }*/
            }
            R.id.back -> {
                LogManager.getLogManager().writeLog("$EVENTTAG : Back Button Pressed")
                requireActivity().onBackPressed()
            }
            R.id.create -> {
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.recycleRequestParent,
                    OrderHistoryFragment.newInstance(),
                    Constants.TAGS.RequestSubmitConfirmationFragment
                )
            }
        }
    }


    override fun callParent(model: Any?) {
        collectionRequest = model as? CollectionRequest
        when (currentStep) {
            Constants.recycleStep1 -> {
                val tinyDB: TinyDB?
                tinyDB = TinyDB(MainApplication.applicationContext())
                if (tinyDB.getBoolean(Constants.agreementCheck)) {
                    BaseActivity.replaceFragment(
                        childFragmentManager,
                        Constants.Containers.recycleFragmentContainer,
                        SelectionDayFragment.newInstance(
                            collectionRequest, getPlans,
                            userContainSingleCollectionRequest,editCollectionRequest //EDIT FLOW
                        ),
                        Constants.TAGS.SelectionDayFragment
                    )
                } else {
                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.TERM_CONDITION)
                        ?.autoLoading(requireActivity())
                        ?.enque(
                            Network().apis()?.termsAndConditions()
                        )
                        ?.execute()
                }
            }
            Constants.recycleStep2 -> {
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.recycleFragmentContainer,
                    ConfirmCollectionFragment.newInstance(materialCategories, collectionRequest,editCollectionRequest),//EDIT FLOW
                    Constants.TAGS.ConfirmCollectionFragment
                )
            }
            Constants.recycleStep3 -> {

                //SINGLE IMAGE
                /*if(collectionRequest!!.imageUri!=null){
                    val filePath: String = getRealPathFromURI(collectionRequest!!.imageUri!!)
                    val imageFile = File(filePath)
                    val requestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    part = MultipartBody.Part.createFormData("images["+0+"]", imageFile.name, requestBody)
                }*/

//MULTIPLE IMAGE
                if(!collectionRequest!!.imageUris.isNullOrEmpty()){
                    multipartParts = createMultipartParts(collectionRequest!!.imageUris)
                }


                if(editCollectionRequest==null){

                    //NORMAL COLLECTION NEW
                    if(multipartParts == null){// dont have images[0]

                        NetworkCall.make()
                            ?.setCallback(this)
                            ?.setTag(RequestCodes.API.COLLECTION_REQUEST)
                            ?.autoLoading(requireActivity())
                            ?.enque(
                                Network().apis()
                                    ?.collectionRequest(collectionRequest, collectionRequest?.map_location)
                            )
                            ?.execute()
                    }else{ //has images[0]

                        val hashMap: HashMap<String?, RequestBody?>? = HashMap()

                        hashMap?.put("map_location", RequestBody.create("text/plain".toMediaTypeOrNull(), collectionRequest?.map_location!!.toString()))

                        //selected materials
                        if(!collectionRequest!!.material_categories.isNullOrEmpty()){
                            for (i in collectionRequest!!.material_categories!!.indices){
                                hashMap?.put(
                                    "material_categories[${i}][id]", RequestBody.create(
                                        "text/plain".toMediaTypeOrNull(), collectionRequest!!.material_categories!!.get(i)!!.id.toString()
                                    )
                                )
                            }
                        }

                        hashMap?.put(
                            "collection_date", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.collection_date!!.toString()
                            )
                        )

                        hashMap?.put(
                            "collection_type", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.collection_type!!.toString()
                            )
                        )

                        hashMap?.put(
                            "first_name", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.first_name!!.toString()
                            )
                        )

                        hashMap?.put(
                            "last_name", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.last_name!!.toString()
                            )
                        )

                        hashMap?.put(
                            "location", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.location!!.toString()
                            )
                        )

                        hashMap?.put("latitude", RequestBody.create("text/plain".toMediaTypeOrNull(), collectionRequest?.latitude!!.toString()))

                        hashMap?.put("longitude", RequestBody.create("text/plain".toMediaTypeOrNull(), collectionRequest?.longitude!!.toString()))

                        hashMap?.put("phone_number", RequestBody.create("text/plain".toMediaTypeOrNull(), collectionRequest?.phone_number!!.toString()))

                        hashMap?.put("city_id", RequestBody.create("text/plain".toMediaTypeOrNull(), collectionRequest?.city_id!!.toString()))

                        hashMap?.put("district_id", RequestBody.create("text/plain".toMediaTypeOrNull(), collectionRequest?.district_id!!.toString()))

                        hashMap?.put(
                            "street", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.street!!.toString()
                            )
                        )

                        hashMap?.put(
                            "organization_name", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.organization_name!!.toString()
                            )
                        )

                        hashMap?.put(
                            "user_comments", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.user_comments!!.toString()
                            )
                        )

                        NetworkCall.make()
                            ?.setCallback(this)
                            ?.setTag(RequestCodes.API.COLLECTION_REQUEST)
                            ?.autoLoading(requireActivity())
                            ?.enque(Network().apis()?.collectionRequest(hashMap,multipartParts))
                            ?.execute()
                    }


                }else{
                    // EDIT COLLECTION ORIGINAL
                    /*NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.EDIT_COLLECTION_REQUEST)
                        ?.autoLoading(requireActivity())
                        ?.enque(
                            Network().apis()
                                ?.editCollectionRequest(editCollectionRequest!!.id,
                                    collectionRequest,
                                    collectionRequest?.map_location,
                                )
                        )
                        ?.execute()*/

                    //NORMAL COLLECTION NEW
                    if(multipartParts == null){// dont have images[0]

                        NetworkCall.make()
                            ?.setCallback(this)
                            ?.setTag(RequestCodes.API.EDIT_COLLECTION_REQUEST)
                            ?.autoLoading(requireActivity())
                            ?.enque(
                                Network().apis()
                                    ?.editCollectionRequest(
                                        editCollectionRequest!!.id,
                                        collectionRequest,
                                        collectionRequest?.map_location,
                                    )
                            )
                            ?.execute()
                    }else { //has images[0]
                        val hashMap: HashMap<String?, RequestBody?>? = HashMap()

                        hashMap?.put(
                            "map_location", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.map_location!!.toString()
                            )
                        )

                        //selected materials
                        if(!collectionRequest!!.material_categories.isNullOrEmpty()){
                            for (i in collectionRequest!!.material_categories!!.indices){
                                hashMap?.put(
                                    "material_categories[${i}][id]", RequestBody.create(
                                        "text/plain".toMediaTypeOrNull(), collectionRequest!!.material_categories!!.get(i)!!.id.toString()
                                    )
                                )
                            }
                        }

                        hashMap?.put(
                            "collection_date", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.collection_date!!.toString()
                            )
                        )

                        hashMap?.put(
                            "collection_type", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.collection_type!!.toString()
                            )
                        )

                        hashMap?.put(
                            "first_name", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.first_name!!.toString()
                            )
                        )

                        hashMap?.put(
                            "last_name", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.last_name!!.toString()
                            )
                        )

                        hashMap?.put(
                            "location", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.location!!.toString()
                            )
                        )

                        hashMap?.put(
                            "latitude", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.latitude!!.toString()
                            )
                        )

                        hashMap?.put(
                            "longitude", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.longitude!!.toString()
                            )
                        )

                        hashMap?.put(
                            "phone_number", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.phone_number!!.toString()
                            )
                        )

                        hashMap?.put(
                            "city_id", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.city_id!!.toString()
                            )
                        )

                        hashMap?.put(
                            "district_id", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.district_id!!.toString()
                            )
                        )

                        hashMap?.put(
                            "street", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.street!!.toString()
                            )
                        )

                        hashMap?.put(
                            "organization_name", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.organization_name!!.toString()
                            )
                        )

                        hashMap?.put(
                            "user_comments", RequestBody.create(
                                "text/plain".toMediaTypeOrNull(), collectionRequest?.user_comments!!.toString()
                            )
                        )


                        NetworkCall.make()
                            ?.setCallback(this)
                            ?.setTag(RequestCodes.API.EDIT_COLLECTION_REQUEST)
                            ?.autoLoading(requireActivity())
                            ?.enque(
                                Network().apis()
                                    ?.editCollectionRequest(editCollectionRequest!!.id,hashMap,multipartParts)
                            )
                            ?.execute()
                    }


                }
            }
        }
    }

    fun createMultipartParts(imageUris: ArrayList<Uri>?): List<MultipartBody.Part>? {
        return imageUris?.mapIndexed { index, uri ->
            val filePath: String = getRealPathFromURI(uri)
            val imageFile = File(filePath)
            val requestBody: RequestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("images[$index]", imageFile.name, requestBody)
        }
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            it.getString(columnIndex)
        } ?: ""
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.MATERIAL_CATEGORIES -> {

                //ORIGINAL
                /*val gson = Gson()
                val listType: Type = object : TypeToken<List<MaterialCategories?>?>() {}.type
                materialCategories = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)

                Log.d("MATE_CAT",""+GsonBuilder().setPrettyPrinting().create().toJson(
                    materialCategories
                ))

                LogManager.getLogManager().writeLog("$EVENTTAG : Material Categories Result ${gson.toJson(materialCategories)}")
                BaseActivity.replaceFragmentBackStackNull(
                    childFragmentManager,
                    Constants.Containers.recycleFragmentContainer,
                    SelectCategoriesFragment.newInstance(materialCategories, collectionRequest,editCollectionRequest))*/



                //NEW
                materialCategoryRelation = Gson().fromJson(Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>), MaterialCategoryRelation::class.java)
                materialCategories = materialCategoryRelation?.materialCategoriesList

                //Log.d("MATE_CAT",""+GsonBuilder().setPrettyPrinting().create().toJson(materialCategoryRelation?.materialCategoriesList))

                BaseActivity.replaceFragmentBackStackNull(
                    childFragmentManager,
                    Constants.Containers.recycleFragmentContainer,
                    SelectCategoriesFragment.newInstance(materialCategoryRelation?.materialCategoriesList, collectionRequest,editCollectionRequest,
                        materialCategoryRelation?.recyclingFamiliesList!!
                    ))

            }
            RequestCodes.API.COLLECTION_REQUEST -> {
//                Notify.alerterGreen(activity, baseResponse?.message)
                val dataParsing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    DataParsing::class.java
                )
                val fragment = RequestSubmitConfirmationFragment.newInstance()
                val args = Bundle()
                args.putString(Constants.DataConstants.purchaseID, dataParsing.collection_request?.get(0))
                fragment.arguments = args
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.recycleFragmentContainer,
                    fragment,
                    Constants.TAGS.RequestSubmitConfirmationFragment
                )
            }
            RequestCodes.API.EDIT_COLLECTION_REQUEST ->{
                val dataParsing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    CollectionRequests::class.java
                )
                val fragment = RequestSubmitConfirmationFragment.newInstance()
                val args = Bundle()
                args.putString(Constants.DataConstants.purchaseID, dataParsing.request_number)
                fragment.arguments = args
                BaseActivity.replaceFragment(
                    childFragmentManager,
                    Constants.Containers.recycleFragmentContainer,
                    fragment,
                    Constants.TAGS.RequestSubmitConfirmationFragment
                )
            }

            RequestCodes.API.TERM_CONDITION -> {
                if (baseResponse?.data != null) {
                    val aboutApp = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse.data as? LinkedTreeMap<*, *>),
                        AboutAppData::class.java
                    )
                    if (aboutApp != null) {
                        AlertDialogs.termConditionDialog(activity, aboutApp.body)
                    } else {
                        Notify.alerterRed(activity, "Cannot Create Collection Request for Now Try Again Later")
                    }
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        when (tag) {
            RequestCodes.API.MATERIAL_CATEGORIES -> {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

                LogManager.getLogManager().writeLog("$EVENTTAG : Material Category Result ${gson.toJson(response)}")
                Notify.alerterRed(activity, response?.message)
            }
            RequestCodes.API.COLLECTION_REQUEST -> {
                Log.d("COLLECTION_FAIL",""+response?.message)

                Notify.alerterRed(activity, response?.message)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogManager.getLogManager().writeLog("$EVENTTAG : Page Loaded")
    }
}
