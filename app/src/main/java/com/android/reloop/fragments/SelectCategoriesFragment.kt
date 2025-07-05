package com.reloop.reloop.fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.model.CommanDays
import com.android.reloop.model.CommanDaysData
import com.android.reloop.model.DataSettings
import com.android.reloop.network.serializer.collectionrequest.MaximumCategoriesSelection
import com.android.reloop.network.serializer.dashboard.SettingsModel
import com.android.reloop.utils.LogManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.adapters.AdapterProductList
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.collectionrequest.CollectionRequest
import com.reloop.reloop.network.serializer.collectionrequest.GetPlans
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategoryID
import com.reloop.reloop.network.serializer.collectionrequest.RecyclingFamilies
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import okhttp3.MultipartBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


/**
 * A simple [Fragment] subclass.
 */
class SelectCategoriesFragment : BaseFragment(), ParentToChild, OnNetworkResponse {

    var childToParent: ChildToParent? = null
    var maxLimitModel = MaximumCategoriesSelection()
    lateinit var adapter: AdapterProductList
    var oderDays: ArrayList<String>? = ArrayList()
    var selectedIdList : ArrayList<Int>?= ArrayList()
    var sortedMaterialCategories: ArrayList<MaterialCategories>? = null
    private var recycling_visibility: String =""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ChildToParent) {
            childToParent = parentFragment as? ChildToParent?
        } else {
            Notify.Toast("Child To Parent Callback Not Implemented")
        }
    }

    var recyclerView: RecyclerView? = null
    var linearLayoutManager: LinearLayoutManager? = null

    companion object {
        var materialCategories: ArrayList<MaterialCategories>? = null
        var collectionRequest: CollectionRequest? = null
        var editCollectionRequest: CollectionRequests? = null
        var recyclingFamiliesList: ArrayList<RecyclingFamilies>? = null

        fun newInstance(
            materialCategories: ArrayList<MaterialCategories>?,
            collectionRequest: CollectionRequest?,
            editCollectionRequest: CollectionRequests?,
            recyclingFamiliesList: ArrayList<RecyclingFamilies>?

        ): SelectCategoriesFragment {
            this.collectionRequest = collectionRequest
            this.materialCategories = materialCategories
            this.editCollectionRequest = editCollectionRequest
            this.recyclingFamiliesList = recyclingFamiliesList

            return SelectCategoriesFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_select_categories, container, false)
        if (RecycleFragment.stepView != null) {
            RecycleFragment.stepView!!.StepNumber(Constants.recycleStep1)
        }
        RecycleFragment.parentToChild = this
        RecycleFragment.changeAddressDistrictID = 0
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        recyclerView = view?.findViewById(R.id.rV_products_list)
    }

    private fun setListeners() {

        try{
            // Sorting the ArrayList based on quantity
            materialCategories!!.sortWith(compareBy { it.recycling_family_id })
        }
        catch (e : NullPointerException)
        {
            e.printStackTrace()
        }


    }

    private fun populateData() {

        try {
            for (i in collectionRequest?.material_categories!!.indices) {
                for (j in materialCategories!!.indices) {
                    if (materialCategories?.get(j)?.id == collectionRequest?.material_categories!![i]?.id) {
                        materialCategories?.get(j)?.selected = true

                    }
                }
            }
            populateRecyclerViewData()
        }catch (e : NullPointerException)
        {
            e.printStackTrace()
        }
    }

    private fun populateRecyclerViewData() {
        if (maxLimitModel.key.isNullOrEmpty()) {
            LogManager.getLogManager().writeLog("$EVENTTAG : Executing Maximum Categories Service Call")
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.MAXIMUM_CATEGORIES)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.getMaximumCategories())
                ?.execute()
        }



        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        recycling_visibility = tinyDB.getString("recycling_visibility").toString()

        if(recycling_visibility.equals("VISIBLE")) { //VISIBLE
            //To display categories with family names

            adapter = AdapterProductList(materialCategories, requireActivity(), maxLimitModel.value, recyclingFamiliesList,
                generateCombinedList(recyclingFamiliesList!!, materialCategories!!)
            )

            linearLayoutManager = GridLayoutManager(context, 3)
            (linearLayoutManager as GridLayoutManager).spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return adapter.getSpanSize(position,3)
                }
            }
        }else{
            adapter = AdapterProductList(materialCategories, requireActivity(), maxLimitModel.value, recyclingFamiliesList,
                generateSimpleList( materialCategories!!))
            linearLayoutManager = GridLayoutManager(context, 3) //three column
        }

        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = adapter

        //EDIT ORDER DATA
        markSelectedCategories()
    }

    data class ListItem(val type: Int, val category: RecyclingFamilies?, val item: MaterialCategories?)
    private val VIEW_TYPE_CATEGORY = 1
    private val VIEW_TYPE_ITEM = 2

    fun generateCombinedList(categories: ArrayList<RecyclingFamilies>, items: ArrayList<MaterialCategories>)
            : ArrayList<ListItem> {
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
            combinedList.addAll(itemsInCategory.map { ListItem(VIEW_TYPE_ITEM, null, it) })

        }

        val nullRecyling_family_id = items.filter { it.recycling_family_id.toString() == "null" }
        combinedList.addAll(nullRecyling_family_id.map { ListItem(VIEW_TYPE_ITEM, null, it) })


        return combinedList
    }

    fun generateSimpleList( items: ArrayList<MaterialCategories>): ArrayList<ListItem> {
        val combinedList = ArrayList<ListItem>()

        for (item in items) {
            combinedList.add(ListItem(VIEW_TYPE_ITEM, null, item))
        }

        return combinedList
    }


    private fun isLastItemInCategory(position: Int): Boolean {
        if (position < materialCategories!!.size - 1) {
            val currentItem = materialCategories!![position]
            val nextItem = materialCategories!![position + 1]

            return currentItem.recycling_family_id != nextItem.recycling_family_id
        }

        return false
    }

    private fun markSelectedCategories(){

        if(editCollectionRequest!=null && !editCollectionRequest!!.request_collection.isNullOrEmpty()){
            for (i in editCollectionRequest!!.request_collection!!.indices) {
                for (j in materialCategories!!.indices) {
                    if (materialCategories?.get(j)?.id == editCollectionRequest!!.request_collection!!.get(i).materialCategoryId) {
                        materialCategories?.get(j)?.selected = true
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }



    override fun callChild() {
        var selected = false
        oderDays?.clear()
        selectedIdList?.clear()
        if (materialCategories != null && materialCategories?.size!! > 0) {
            for (i in materialCategories!!.indices) {
                if (materialCategories!![i].selected!!) {
                    /* val data =  materialCategories!![i].collection_acceptance_days
                     for(j in data!!.indices)
                     {
                         oderDays?.add(data[j])
                     }
                     Log.e("TAG","====collection acceptance days1===" + oderDays)*/
                    selected = true
                    break
                }
            }
        }

        if (selected) {
            collectionRequest?.material_categories?.clear()
            for (i in materialCategories!!.indices) {
                if (materialCategories!![i].selected!!) {
                    val obj: MaterialCategoryID = MaterialCategoryID()
                    obj.id = materialCategories?.get(i)?.id!!
                    collectionRequest?.material_categories?.add(obj)
                    val data =  materialCategories!![i].collection_acceptance_days

                    selectedIdList?.add(obj.id!!)
                    Log.e("TAG","========selected id list======" + selectedIdList)
                }
            }

            //childToParent?.callParent(collectionRequest)
            val size = selectedIdList?.size
            val part = arrayOfNulls<MultipartBody.Part>(size!!)

            for (i in 0 until selectedIdList!!.size) {
                val file_path: Int = selectedIdList?.get(i)!!
                part[i] = MultipartBody.Part.createFormData("material_category_id["+(i)+"]", file_path.toString())
            }


            LogManager.getLogManager().writeLog("$EVENTTAG : Executing Material Categories Comman Days Service Call")

            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.GET_MATERIAL_CATEGORIES_COMMAN_DAYS)
                ?.autoLoading(requireActivity())
                ?.enque(
                    Network().apis()?.getMaterialCategoriesCommanDays(part))
                ?.execute()
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        Log.e("TAG","=====base response====" + baseResponse)
        when (tag) {
            RequestCodes.API.MAXIMUM_CATEGORIES -> {
                try {
                    val gson = Gson()
                    val listType: Type = object : TypeToken<MaximumCategoriesSelection>() {}.type

                    maxLimitModel = gson.fromJson(Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>), listType)

                    LogManager.getLogManager().writeLog("$EVENTTAG : Maximum Category Result : ${gson.toJson(maxLimitModel)}")
                    adapter.setMaxLimit(maxLimitModel.value)
                }
                catch (e : ClassCastException)
                {
                    e.printStackTrace()
                    LogManager.getLogManager().writeLog("$EVENTTAG : Maximum Category Result : Issue ${e.message}")
                }
            }

            RequestCodes.API.GET_MATERIAL_CATEGORIES_COMMAN_DAYS ->
            {
                try {

                    val responsedata = Utils.getBaseResponseComman(response)
                    //Log.e("TAG", "Comman Days Result cdays ====" + responsedata)
                    Log.d("TAG", "Comman Days Result responsedata:- " + GsonBuilder().setPrettyPrinting().create().toJson(responsedata))
                    //Log.e("TAG", "Comman Days Result cdays ====" + {Gson().toJson(responsedata?.data?.dataSettings?.settings?.collection_time?.get(0)?.value)})

                    val collectiontime = responsedata?.data?.dataSettings?.settings?.collection_time?.get(0)?.value
                    Log.e("TAG", "Comman Days Result cdays time====" + collectiontime)


                    oderDays = responsedata?.data?.days
                    //Log.e("TAG", "Comman Days Result base ====" + baseResponse)
                    //Log.d("TAG", "Comman Days Result base:- " + GsonBuilder().setPrettyPrinting().create().toJson(baseResponse))
                    //Log.e("TAG", "Comman Days Result cdays days====" + responsedata?.data?.days)

                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    Log.e("TAG", "Comman Days Result ====" + {gson.toJson(baseResponse)})
                    LogManager.getLogManager().writeLog("$EVENTTAG : Comman Days Result : ${gson.toJson(baseResponse)}")

                    if(baseResponse?.data != null) {

                        val str = baseResponse.data.toString().replace("[", "")
                        val strnew = str.replace("]", "")
                        val stringArray = strnew.split(",").map { it -> it.trim() }
                        Log.e("TAG", "array main ====" + stringArray)


                       /* for (i in stringArray.indices) {
                            Log.e("TAG", "array ====" + stringArray.get(i))
                            oderDays?.add(stringArray.get(i).toString())
                        }*/


                        if (oderDays != null) {

                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("msg", baseResponse?.message)
                            tinyDB.putListString("OrderDays", oderDays!!)
                            tinyDB.putString("cTime", collectiontime)

                        } else {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("msg", baseResponse?.message)
                            tinyDB.putListString("OrderDays", oderDays!!)
                            tinyDB.putString("cTime", collectiontime)
                        }
                    }
                    else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("msg", baseResponse?.message)
                        tinyDB.putListString("OrderDays", oderDays!!)
                        tinyDB.putString("cTime", collectiontime)
                    }
                    childToParent?.callParent(collectionRequest)
                } catch (e: Exception) {
                    Log.e("Home Fragment", e.toString())
                    LogManager.getLogManager().writeLog("$EVENTTAG : Comman Days Result : Issue ${e.message}")
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        when (tag) {
            RequestCodes.API.MAXIMUM_CATEGORIES -> {
                val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()

                LogManager.getLogManager().writeLog("$EVENTTAG : Maximum Category Result : ${gson.toJson(response)}")
                Notify.alerterRed(activity, response?.message)
            }
            RequestCodes.API.GET_MATERIAL_CATEGORIES_COMMAN_DAYS -> {
                Log.e("TAG", "comman days error====" +  response?.message)
                val tinyDB: TinyDB?
                tinyDB = TinyDB(MainApplication.applicationContext())
                tinyDB.putString("msg", response?.message)
                tinyDB.putListString("OrderDays", oderDays!!)
                childToParent?.callParent(collectionRequest)
            }
        }
    }

    var EVENTTAG = "Event Categories Page"
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogManager.getLogManager().writeLog("$EVENTTAG : Page Loaded")
    }
}

