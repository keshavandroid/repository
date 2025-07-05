package com.android.reloop.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.bottomsheet.CityBottomsheet
import com.android.reloop.bottomsheet.MaterialBottomsheet
import com.android.reloop.bottomsheet.SelectedFilterBottomsheet
import com.android.reloop.utils.LogManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterMaterialListNew
import com.reloop.reloop.adapters.AdapterProductList
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.fragments.RecycleFragment
import com.reloop.reloop.fragments.SelectCategoriesFragment
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.DependencyDetail
import com.reloop.reloop.network.serializer.FavDropOffPoints
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategoryID
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategoryRelation
import com.reloop.reloop.network.serializer.collectionrequest.RecyclingFamilies
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


class MaterialSelectFragment : BaseFragment(),View.OnClickListener, OnNetworkResponse{

    var back: Button? = null
    var btnNext:Button? = null

    var img_step2: ImageView? = null
    var text_step2: TextView?= null

    var recyclerView: RecyclerView? = null
    lateinit var adapter: AdapterMaterialListNew
    var linearLayoutManager: LinearLayoutManager? = null
    private var selected: Boolean = false

    var tinyDB: TinyDB?=null

    var materials: ArrayList<MaterialCategories>? = ArrayList()

    private var recycling_visibility: String =""
    var materialCategoryRelation: MaterialCategoryRelation? = null
    var recyclingFamiliesList: ArrayList<RecyclingFamilies>? = null
    data class ListItem(val type: Int, val category: RecyclingFamilies?, val item: MaterialCategories?)
    private val VIEW_TYPE_CATEGORY = 1
    private val VIEW_TYPE_ITEM = 2

    companion object {
        var materialCategories: ArrayList<MaterialCategories>? = null

        fun newInstance(bundle: ArrayList<MaterialCategories>?): MaterialSelectFragment {
            this.materialCategories = bundle
            return MaterialSelectFragment()
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

        val view: View? = inflater.inflate(R.layout.fragment_select_materials, container, false)

        tinyDB = TinyDB(MainApplication.applicationContext())

        initViews(view)
        setListeners()

        getMaterialCategory()

        return view
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        btnNext?.setOnClickListener(this)

        img_step2?.setImageResource(R.drawable.ic_step_material_clicked)
        text_step2?.setTextColor(requireActivity().getColor(R.color.green_color_button))
    }

    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        btnNext = view?.findViewById(R.id.btnNext)

        img_step2 = view?.findViewById(R.id.img_step2)
        text_step2 = view?.findViewById(R.id.text_step2)

        recyclerView = view?.findViewById(R.id.rv_sel_materials)


    }

    private fun getMaterialCategory(){
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.MATERIAL_CATEGORIES)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getMaterialCategories())
            ?.execute()
    }

    private fun populateRecyclerViewData() {

        Log.d("DDDDD","111")
        this.materials = materialCategories

        if(!materials.isNullOrEmpty()){
            for(i in materials!!.indices){
                materials!!.get(i).selected = false
            }
        }

        //OLD
//        adapter = AdapterMaterialList(materials, requireActivity())
//        linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)

        //NEW
        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        recycling_visibility = tinyDB.getString("recycling_visibility").toString()

        if(recycling_visibility.equals("VISIBLE")) { //VISIBLE
            //To display categories with family names

            adapter = AdapterMaterialListNew(
                materials, requireActivity(),
                generateCombinedList(recyclingFamiliesList!!, materials!!)
            )

            linearLayoutManager = GridLayoutManager(context, 3)
            (linearLayoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return adapter.getSpanSize(position,3)
                }
            }
        }else{
            adapter = AdapterMaterialListNew(materials, requireActivity(), generateSimpleList(materials!!))
            linearLayoutManager = GridLayoutManager(context, 3) //three column
        }

        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = adapter
    }

    fun generateCombinedList(categories: ArrayList<RecyclingFamilies>, items: ArrayList<MaterialCategories>) : ArrayList<ListItem> {
        val combinedList = ArrayList<ListItem>()

        for (category in categories) {
//            val categoryId = category.id ?: 4 // Use a placeholder ID for null category

            //OLD
//            combinedList.add(ListItem(VIEW_TYPE_CATEGORY, category, null))

            //NEW CHANGE
            val isIDFound = items.any { it.recycling_family_id == category.id  || it.recycling_family_id.toString() == "null"}

            if (isIDFound){
                combinedList.add(ListItem(VIEW_TYPE_CATEGORY, category, null))
            }

            val itemsInCategory = items.filter { it.recycling_family_id == category.id }
            combinedList.addAll(itemsInCategory.map {
                ListItem(
                    VIEW_TYPE_ITEM,
                    null,
                    it
                )
            })
        }

        val nullRecyling_family_id = items.filter { it.recycling_family_id.toString() == "null" }
        combinedList.addAll(nullRecyling_family_id.map {
            ListItem(VIEW_TYPE_ITEM, null, it)
        })


        return combinedList
    }

    fun generateSimpleList( items: ArrayList<MaterialCategories>): ArrayList<ListItem> {
        val combinedList = ArrayList<ListItem>()

        for (item in items) {
            combinedList.add(ListItem(VIEW_TYPE_ITEM, null, item))
        }

        return combinedList
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
            R.id.btnNext -> {

                selected = false
                if(!materials.isNullOrEmpty()){
                    for(i in materials!!.indices){
                        if(materials!!.get(i).selected == true){
                            selected = true
                        }
                    }
                }

                /*Check if allowed LOCATION,PHOTO,BARCODE*/
                val req_location = tinyDB!!.getString("DROP_REQUIRE_LOCATION").toString()
                val req_photo = tinyDB!!.getString("DROP_REQUIRE_PHOTO").toString()
                val req_barcode = tinyDB!!.getString("DROP_REQUIRE_BARCODE").toString()

                Log.d("CONDITIONS_",""+req_location + req_photo + req_barcode)

                if(req_location.equals("0") && req_photo.equals("0") && req_barcode.equals("0")){ // 000 is real condition
                    if(selected){
                        BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.materialSelectFragment,
                            ConfirmSelectionsFragment.newInstance(materials, ArrayList(),ArrayList()),
                            Constants.TAGS.ConfirmSelectionsFragment)
                    }else{
                        Notify.alerterRed(activity, "Please select at least one category")
                    }
                }else{
                    if(selected){
                        BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.materialSelectFragment,
                            VerifyDropOffFragment.newInstance(materials),
                            Constants.TAGS.VerifyDropOffFragment)
                    }else{
                        Notify.alerterRed(activity, "Please select at least one category")
                    }
                }
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.MATERIAL_CATEGORIES -> {
                materialCategoryRelation = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    MaterialCategoryRelation::class.java)

                if(!materialCategoryRelation?.recyclingFamiliesList.isNullOrEmpty()){
                    recyclingFamiliesList = materialCategoryRelation?.recyclingFamiliesList
                }

                populateRecyclerViewData()
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        when (tag) {
            RequestCodes.API.MATERIAL_CATEGORIES -> {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                Notify.alerterRed(activity, response?.message)
            }
        }
    }
}