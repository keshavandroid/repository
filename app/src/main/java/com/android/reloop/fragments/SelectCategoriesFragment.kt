package com.reloop.reloop.fragments


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.network.serializer.collectionrequest.MaximumCategoriesSelection
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
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategoryID
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import okhttp3.MultipartBody
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
        fun newInstance(
            materialCategories: ArrayList<MaterialCategories>?,
            collectionRequest: CollectionRequest?
        ): SelectCategoriesFragment {
            this.collectionRequest = collectionRequest
            this.materialCategories = materialCategories
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
        adapter = AdapterProductList(materialCategories, requireActivity(), maxLimitModel.value)
        // Set CustomAdapter as the adapter for RecyclerView.
        linearLayoutManager = GridLayoutManager(context, Constants.RecyclerViewSpan.twoColumns)
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.adapter = adapter
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

                    maxLimitModel = gson.fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                        listType
                    )

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
                    val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                    Log.e("TAG", "Comman Days Result ====" + {gson.toJson(baseResponse)})
                    LogManager.getLogManager().writeLog("$EVENTTAG : Comman Days Result : ${gson.toJson(baseResponse)}")

                    if(baseResponse?.data != null) {

                        val str = baseResponse.data.toString().replace("[", "")
                        val strnew = str.replace("]", "")
                        val stringArray = strnew.split(",").map { it -> it.trim() }
                        Log.e("TAG", "array====" + stringArray)

                        for (i in stringArray.indices) {
                            Log.e("TAG", "array====" + stringArray.get(i))
                            oderDays?.add(stringArray.get(i).toString())
                        }

                        if (oderDays != null) {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("msg", baseResponse?.message)
                            tinyDB.putListString("OrderDays", oderDays!!)

                        } else {
                            val tinyDB: TinyDB?
                            tinyDB = TinyDB(MainApplication.applicationContext())
                            tinyDB.putString("msg", baseResponse?.message)
                            tinyDB.putListString("OrderDays", oderDays!!)
                        }
                    }
                    else{
                        val tinyDB: TinyDB?
                        tinyDB = TinyDB(MainApplication.applicationContext())
                        tinyDB.putString("msg", baseResponse?.message)
                        tinyDB.putListString("OrderDays", oderDays!!)
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

