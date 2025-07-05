package com.android.reloop.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.adapters.PaymentMethodsAdapter
import com.android.reloop.model.PaymentMethods
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.reloop.reloop.R
import com.reloop.reloop.auth.BillingInformationAuth
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.shop.AddCard
import com.reloop.reloop.network.serializer.shop.BuyPlan
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import retrofit2.Call
import retrofit2.Response
import java.lang.reflect.Type

class PaymentMethodsFragment : BaseFragment(),View.OnClickListener,PaymentMethodsAdapter.ItemClickListener,
    AlertDialogCallback,
    OnNetworkResponse {

    var rv_pm: RecyclerView? = null
    var tvNopm: TextView? = null
    var back: Button? = null
    var update: Button? = null
    var pmList: ArrayList<PaymentMethods>? = ArrayList()
    var pmListNew: ArrayList<PaymentMethods>? = ArrayList()
    var linearLayoutManager: LinearLayoutManager? = null
    var ivcard: ImageView? = null
    var tvcardNo: TextView? = null
    var selectedCardId : String = ""
    var lllist: LinearLayout? = null
    var iv_card: ImageView?= null
    var otherSavedCards: TextView?= null
    var rlAddPaymentMethod: RelativeLayout?= null
    var frmCard: FrameLayout?= null
    var llbutton: LinearLayout?= null
    var relPaymentMethodBack: Button?= null
    var btnAddCard: Button?= null

    var cardNumber: CustomEditText? = null
    var cardExpiry: CustomEditText? = null
    var cardCVV: CustomEditText? = null
    var cardHolder: CustomEditText? = null
    var addCard: AddCard? = AddCard()
    var isCardsAvailable = false

    companion object {

        fun newInstance(): PaymentMethodsFragment {
            return PaymentMethodsFragment()
        }

        var TAG = "PaymentMethodsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.fragment_payment_methods, container, false)

        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun populateData() {

        cardExpiry?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(p0: CharSequence?, start: Int, removed: Int, added: Int) {
                if (start == 1 && start + added == 2 && p0?.contains('/') == false) {
                    cardExpiry?.setText("$p0/")
                } else if (start == 3 && start - removed == 2 && p0?.contains('/') == true) {
                    cardExpiry?.setText(p0.toString().replace("/", ""))
                }
                cardExpiry?.setSelection(cardExpiry?.text!!.length)
            }
        })

        pmListNew?.clear()

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.PAYMENT_METHOD_LISTING)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.paymentMethodListing())
            ?.execute()
    }

    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        update = view?.findViewById(R.id.update)

        tvcardNo = view?.findViewById(R.id.tv_card_no)
        rv_pm = view?.findViewById(R.id.rv_paymentmethods)
        tvNopm = view?.findViewById(R.id.tvNoPaymentMethods)
        lllist = view?.findViewById(R.id.lllist)
        iv_card = view?.findViewById(R.id.iv_card)
        otherSavedCards = view?.findViewById(R.id.otherSavedCards);

        rlAddPaymentMethod = view?.findViewById(R.id.rlAddPaymentMethod)
        frmCard = view?.findViewById(R.id.frmCard)
        llbutton = view?.findViewById(R.id.llbutton)
        relPaymentMethodBack = view?.findViewById(R.id.relPaymentMethodBack)
        btnAddCard = view?.findViewById(R.id.btnAddCard)

        cardNumber = view?.findViewById(R.id.card_number)
        cardExpiry = view?.findViewById(R.id.card_expiry)
        cardCVV = view?.findViewById(R.id.card_cvv)
        cardHolder = view?.findViewById(R.id.card_holder)


    }

    private fun setListeners() {

        back?.setOnClickListener(this)
        update?.setOnClickListener(this)
        rlAddPaymentMethod?.setOnClickListener(this)
        btnAddCard?.setOnClickListener(this)
        relPaymentMethodBack?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.back->{
                activity?.onBackPressed()
            }
            R.id.update->{

                //OLD AD
                /*if(selectedCardId.isEmpty()) {
                    Notify.alerterRed(activity, "Please select payment method")
                }
                else{
                    //call api to update
                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.UPDATE_PAYMENT_METHOD)
                        ?.autoLoading(requireActivity())
                        ?.enque(Network().apis()?.updatePaymentMethod(selectedCardId))
                        ?.execute()
                }*/
            }
            R.id.rlAddPaymentMethod->{
                rlAddPaymentMethod?.visibility = View.GONE
                frmCard?.visibility = View.VISIBLE
                lllist?.visibility = View.GONE
                llbutton?.visibility = View.GONE
            }
            R.id.relPaymentMethodBack->{
                rlAddPaymentMethod?.visibility = View.VISIBLE
                frmCard?.visibility = View.GONE
                llbutton?.visibility = View.VISIBLE

                if(isCardsAvailable){
                    lllist?.visibility = View.VISIBLE
                }else{
                    lllist?.visibility = View.GONE
                }
            }
            R.id.btnAddCard->{
                val auhSuccessful = BillingInformationAuth.authenticate(cardNumber?.text.toString(), cardExpiry?.text.toString(), cardCVV?.text.toString(), requireActivity())

                Log.d("ADD_CARD","111")

                if (auhSuccessful) {

                    val delimiter1 = "/"
                    val dateExpiry = cardExpiry?.text.toString().split(delimiter1)
                    addCard?.card_number = cardNumber?.text.toString()
                    addCard?.cvv = cardCVV?.text.toString()
                    addCard?.exp_month = dateExpiry[0]
                    addCard?.exp_year = dateExpiry[1]
                    addCard?.card_holder = cardHolder?.text.toString()

                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.ADD_NEW_CARD)
                        ?.autoLoading(requireActivity())
                        ?.enque(Network().apis()?.addNewCard(addCard))
                        ?.execute()
                }
            }
        }
    }

    override fun itemclick(position: Int, paymentMethods: PaymentMethods) {
        //Log.e(TAG,"===item click id===" + pmList?.get(position)?.getId().toString())
        //selectedCardId = pmList?.get(position)?.getId().toString()

        selectedCardId = paymentMethods.getId().toString()


        //NEW AD
        if(selectedCardId.isEmpty()) {
            Notify.alerterRed(activity, "Please select payment method")
        }
        else{
            //call api to update
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.UPDATE_PAYMENT_METHOD)
                ?.autoLoading(requireActivity())
                ?.enque(Network().apis()?.updatePaymentMethod(selectedCardId))
                ?.execute()
        }

    }

    override fun deletePaymentMethod(position: Int,paymentMethods: PaymentMethods) {
        Log.e(TAG,"===delete item click id===" + pmList?.get(position)?.getId().toString())
        //selectedCardId = pmList?.get(position)?.getId().toString()  //original
        selectedCardId = paymentMethods.getId().toString() //New AD
        AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.delete_payment_method_text_msg))
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.PAYMENT_METHOD_LISTING -> {
                val baseResponse = Utils.getBaseResponse(response)
                Log.e(TAG,"===response===" + baseResponse)
                val gson = Gson()
                val listType: Type = object : TypeToken<List<PaymentMethods?>?>() {}.type
                pmList = gson.fromJson(Utils.jsonConverterArray(baseResponse?.data as? ArrayList<*>), listType)

                Log.e(TAG,"===payment method list===" + pmList?.size)

                Log.e(TAG,"CARD_RES" + GsonBuilder().setPrettyPrinting().create().toJson(pmList))


                if(pmList?.size!! > 0) {
                    isCardsAvailable = true
                    lllist?.visibility = View.VISIBLE
                    tvNopm?.visibility = View.GONE
//                    update?.visibility = View.VISIBLE
                    llbutton?.visibility = View.VISIBLE

                    if(pmList?.size==1){
//                        update?.visibility = View.GONE
                    }else{
//                        update?.visibility = View.VISIBLE
                    }

                    for (i in pmList!!.indices) {

                        if (pmList!![i].getIsDefault() == 1) {
                            Log.e(TAG,"===default card id===" + pmList!![i].getId())

                            tvcardNo?.setText("**** **** **** " + pmList!![i].getNumber())
                            pmListNew?.remove(pmList!![i])

                            if(pmList!![i].getBrand().equals("visa", ignoreCase = true)){
                                iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_visa_card))
                            }else if(pmList!![i].getBrand().equals("MasterCard", ignoreCase = true)){
                                iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_master_card))
                            }else{
                                iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_card_grey))
                            }
                        }
                        else{
                            pmListNew?.add(pmList!![i])
                        }
                    }
                    Log.e(TAG,"===payment method list new ===" + pmListNew?.size)
                    populateRecyclerViewData(pmListNew!!)

                } else{
                    isCardsAvailable = false
                    //Notify.alerterRed(activity, "No record found!")
                    lllist?.visibility = View.GONE
                    tvNopm?.visibility = View.VISIBLE
                    //update?.visibility = View.GONE

                    llbutton?.visibility = View.VISIBLE
                    back?.visibility = View.VISIBLE
                    //update?.visibility = View.GONE

                }
            }

            RequestCodes.API.UPDATE_PAYMENT_METHOD ->
            {
                val baseResponse = Utils.getBaseResponse(response)
                val message = baseResponse?.message
                Log.e("TAG", "===message===" + message)
                Notify.alerterGreen(activity, message)

                populateData()
            }
            RequestCodes.API.DELETE_PAYMENT_METHOD ->
            {
                val baseResponse = Utils.getBaseResponse(response)
                val message = baseResponse?.message
                Notify.alerterGreen(activity, message)
                populateData()
            }
            RequestCodes.API.ADD_NEW_CARD -> {
                frmCard?.visibility = View.GONE
                rlAddPaymentMethod?.visibility = View.VISIBLE
                llbutton?.visibility = View.VISIBLE

                pmListNew?.clear()
                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.PAYMENT_METHOD_LISTING)
                    ?.autoLoading(requireActivity())
                    ?.enque(Network().apis()?.paymentMethodListing())
                    ?.execute()
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
        requireActivity().onBackPressed()
    }

    private fun populateRecyclerViewData(pmList: ArrayList<PaymentMethods>) {

        if(pmList.size>0){
            otherSavedCards?.visibility = View.VISIBLE
        }else{
            otherSavedCards?.visibility = View.GONE
        }

        linearLayoutManager = LinearLayoutManager(context)
        rv_pm?.layoutManager = linearLayoutManager
        val adapter = PaymentMethodsAdapter(requireContext(), pmList)
        rv_pm?.adapter = adapter
        adapter.setClicklistner(this)
    }

    override fun callDialog(model: Any?) {
        //delete payment method and go back
        Log.e(TAG,"===deleted id===" + selectedCardId)
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DELETE_PAYMENT_METHOD)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.deletePaymentMethod(selectedCardId))
            ?.execute()
    }
}