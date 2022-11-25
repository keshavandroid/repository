package com.reloop.reloop.fragments


import android.os.Bundle
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
import com.google.gson.reflect.TypeToken
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
import com.reloop.reloop.network.serializer.settings.AboutAppData
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.*
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type


/**
 * A simple [Fragment] subclass.
 */
class RecycleFragment : BaseFragment(), View.OnClickListener, StepView, OnNetworkResponse,
    ChildToParent {

    var EVENTTAG = "Event Recycle Page";
    private var selected: Boolean = false
    var collectionRequest: CollectionRequest? =
        CollectionRequest()

    companion object {
        var getPlans: GetPlans? = null
        var userContainSingleCollectionRequest: Boolean = false
        fun newInstance(
            plans: GetPlans?,
            userContainSingleCollectionRequest: Boolean
        ): RecycleFragment {
            this.getPlans = plans
            this.userContainSingleCollectionRequest = userContainSingleCollectionRequest
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

    var back: Button? = null
    var create: Button? = null
    private var materialCategories: ArrayList<MaterialCategories>? = ArrayList()

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

    }

    private fun setListeners() {
        stepView = this
        next?.setOnClickListener(this)
        back?.setOnClickListener(this)
        create?.setOnClickListener(this)
    }

    private fun populateData() {
        LogManager.getLogManager().writeLog("$EVENTTAG : Executing Material Categories Service Call")
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.MATERIAL_CATEGORIES)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.getMaterialCategories())
            ?.execute()
        isAddedFragment = true
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
                next?.text = getString(R.string.proceed)
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
                            userContainSingleCollectionRequest
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
                    ConfirmCollectionFragment.newInstance(materialCategories, collectionRequest),
                    Constants.TAGS.ConfirmCollectionFragment
                )
            }
            Constants.recycleStep3 -> {

                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.COLLECTION_REQUEST)
                    ?.autoLoading(requireActivity())
                    ?.enque(
                        Network().apis()
                            ?.collectionRequest(collectionRequest, collectionRequest?.map_location)
                    )
                    ?.execute()
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.MATERIAL_CATEGORIES -> {
                val gson = Gson()
                val listType: Type = object : TypeToken<List<MaterialCategories?>?>() {}.type
                materialCategories = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)
                LogManager.getLogManager().writeLog("$EVENTTAG : Material Categories Result ${gson.toJson(materialCategories)}")
                BaseActivity.replaceFragmentBackStackNull(
                    childFragmentManager,
                    Constants.Containers.recycleFragmentContainer,
                    SelectCategoriesFragment.newInstance(materialCategories, collectionRequest))

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
                Notify.alerterRed(activity, response?.message)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogManager.getLogManager().writeLog("$EVENTTAG : Page Loaded")
    }
}
