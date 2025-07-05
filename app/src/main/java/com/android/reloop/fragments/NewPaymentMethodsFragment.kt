package com.android.reloop.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.adapters.NewPaymentMethodsAdapter
import com.android.reloop.model.SavedCard
import com.android.reloop.model.SingleCard
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.gson.Gson
import com.reloop.reloop.R
import com.reloop.reloop.auth.BillingInformationAuth
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.stripe.android.ApiResultCallback
import com.stripe.android.CardUtils
import com.stripe.android.Stripe
import com.stripe.android.model.Card
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.view.CardMultilineWidget
import kotlinx.android.synthetic.main.bottomsheet_subscription.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response


class NewPaymentMethodsFragment : BaseFragment(),View.OnClickListener, AlertDialogCallback,NewPaymentMethodsAdapter.ItemClickListener,OnNetworkResponse {

    var back: Button? = null
    var relPaymentMethodBack: Button? = null

    var stripe: Stripe?=null
    var rv_pm: RecyclerView? = null
    var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var dialog: Dialog
    var savedCardList: SavedCard?=null
    var selectedCardId : String = ""
    var defaultPMId: String?=null

    var iv_card: ImageView?= null
    var tvcardNo: TextView? = null

    var txtNoPM: TextView?= null
    var relPaymentMethod: RelativeLayout?=null
    var relDefaultCard: RelativeLayout?=null
    var relOtherCards: RelativeLayout?=null
    var rladdPaymentMethod : RelativeLayout?= null

    var btnAddNewPM: Button?=null
    val pmListNew: ArrayList<SingleCard> = ArrayList()

    var llAddNewCard: FrameLayout?=null


    var cardNumber: CustomEditText? = null
    var cardExpiry: CustomEditText? = null
    var cardCVV: CustomEditText? = null
    var cardHolder: CustomEditText? = null

//    var addButton: Button? = null

    var _type: String = "LISTING"

    companion object {

        fun newInstance(): NewPaymentMethodsFragment {
            return NewPaymentMethodsFragment()
        }

        var TAG = "NewPaymentMethodsFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.new_fragment_payment_methods, container, false)

        /*PaymentConfiguration.init(
            requireContext(),
            "pk_test_51O4xn4CtnaegeBeWpzicQd1zgAXn8JtL4AlLK96hp1VLsv2yB5hTxnLCyYQh7dH2fHi6oEFH6swjW7o11jeG1dfB00o5Onfynr"
        )*/


        val stripeKey: String = Constants.PK_STRIPE
        stripe = Stripe(requireContext(), stripeKey)

        initViews(view)
        setListeners()
        //getStripePaymentMethodList()

        checkIfDefaultPaymentMethod()
        return view
    }


    private fun initViews(view: View?) {
        back = view?.findViewById(R.id.back)
        relPaymentMethodBack = view?.findViewById(R.id.relPaymentMethodBack)
        rv_pm = view?.findViewById(R.id.rv_paymentmethods)

        //Default Card detail
        tvcardNo = view?.findViewById(R.id.tv_card_no)
        iv_card = view?.findViewById(R.id.iv_card)

        txtNoPM = view?.findViewById(R.id.txtNoPM)
        relPaymentMethod = view?.findViewById(R.id.relPaymentMethod)
        llAddNewCard = view?.findViewById(R.id.llAddNewCard)
        relDefaultCard = view?.findViewById(R.id.relDefaultCard)
        relOtherCards = view?.findViewById(R.id.relOtherCards)

        rladdPaymentMethod = view?.findViewById(R.id.rlAddPaymentMethod)
//        addButton = view?.findViewById(R.id.addButton)
        btnAddNewPM = view?.findViewById(R.id.btnAddCard)


        cardNumber = view?.findViewById(R.id.card_number)
        cardExpiry = view?.findViewById(R.id.card_expiry)
        cardCVV = view?.findViewById(R.id.card_cvv)
        cardHolder = view?.findViewById(R.id.card_holder)
    }

    private fun sendPMtoBackend(pmID: String){
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.ADD_PAYMENT_METHOD)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.addNewPM(pmID))
            ?.execute()
    }

    fun getStripePaymentMethodList(){

        if (!User.retrieveUser()!!.stripe_id.isNullOrEmpty()){
            showProgressDialog()

            var baseURL = ""
            baseURL = "https://api.stripe.com/v1/customers/"+ User.retrieveUser()!!.stripe_id+"/payment_methods";


            AndroidNetworking.get(baseURL).setPriority(Priority.MEDIUM)
                .addHeaders("Authorization", "Bearer " + Constants.SK_STRIPE)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        dismissProgressDialog()

                        try {
                            Log.d("==>>", "onResponse: $response")

                            if(response.has("data")){
                                savedCardList = Gson().fromJson(response.toString(), SavedCard::class.java)

//                                Log.d(PaymentMethodsFragment.TAG,"PM_LIST" + GsonBuilder().setPrettyPrinting().create().toJson(savedCardList))

                                if(!savedCardList!!.cardData.isNullOrEmpty()){

                                    txtNoPM!!.visibility = View.GONE
                                    relOtherCards!!.visibility = View.VISIBLE

                                    populateRecyclerViewData(savedCardList!!.cardData!!)

                                    setDefaultCardDetails(savedCardList!!.cardData!!)
                                }else{
                                    txtNoPM!!.visibility = View.VISIBLE
                                    relOtherCards!!.visibility = View.GONE
                                }
                            }

                        } catch (e: java.lang.Exception) {
                            Log.d("==>>", "onResponse: " + e.message)
                        }
                    }

                    override fun onError(error: ANError) {
                        dismissProgressDialog()
                        Log.d("==>>", "onError: " + error.message)
                    }
                })
        }

    }

    fun setDefaultCardDetails(cardData: ArrayList<SingleCard>) {

        if (defaultPMId != null && !defaultPMId.equals("null")) {
            for (i in 0 until cardData.size){
                if(defaultPMId.equals(cardData.get(i).id)){
                    tvcardNo?.setText("**** **** **** " + cardData.get(i).itemCard!!.last4)

                    if(cardData.get(i).itemCard!!.brand.equals("visa", ignoreCase = true)){
                        iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_visa_card))
                    }else if(cardData.get(i).itemCard!!.brand.equals("MasterCard", ignoreCase = true)){
                        iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_master_card))
                    }else{
                        iv_card?.setImageDrawable(resources.getDrawable(R.drawable.ic_card_grey))
                    }
                }
            }
            relDefaultCard!!.visibility = View.VISIBLE

        }else{
            relDefaultCard!!.visibility = View.GONE
        }
    }

    fun checkIfDefaultPaymentMethod(){

        if (!User.retrieveUser()!!.stripe_id.isNullOrEmpty()){
            showProgressDialog()

            var baseURL = ""
            baseURL = "https://api.stripe.com/v1/customers/"+ User.retrieveUser()!!.stripe_id


            AndroidNetworking.get(baseURL)
                .setPriority(Priority.MEDIUM)
                .addHeaders("Authorization", "Bearer " + Constants.SK_STRIPE)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        dismissProgressDialog()
                        rladdPaymentMethod!!.visibility = View. VISIBLE

                        try {
                            Log.d("DEFAULT_PM", "onResponse: $response")

                            if(response.has("invoice_settings")){

                                if(response.getJSONObject("invoice_settings").has("default_payment_method")){
                                    defaultPMId = response.getJSONObject("invoice_settings").getString("default_payment_method")

                                    Log.d("DEFAULT_PM", "defaultPMId " + defaultPMId)


                                    if(defaultPMId == null || defaultPMId.equals("null")){
                                        Log.d("TEST_LOG","444")

                                        relPaymentMethod!!.visibility = View.GONE
//                                        relDefaultCard!!.visibility = View.GONE
                                    }else{
                                        Log.d("TEST_LOG","333")

                                        relPaymentMethod!!.visibility = View.VISIBLE
//                                        relDefaultCard!!.visibility = View.VISIBLE

                                    }

                                    getStripePaymentMethodList()

                                }

                            }


                        } catch (e: java.lang.Exception) {
                            Log.d("DEFAULT_PM", "onResponse: " + e.message)

                            relPaymentMethod!!.visibility = View.GONE
                            txtNoPM!!.visibility = View.VISIBLE
                        }
                    }

                    override fun onError(error: ANError) {
                        dismissProgressDialog()
                        relPaymentMethod!!.visibility = View.GONE
                        txtNoPM!!.visibility = View.VISIBLE
                        Log.d("DEFAULT_PM", "onError: " + error.message)
                    }
                })
        }

    }

    private fun showProgressDialog() {
        dialog = Dialog(requireContext())
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.progress_dialog_layout)
            dialog.show()
        }
    }

    private fun dismissProgressDialog() {
        dialog.dismiss()
    }

    private fun populateRecyclerViewData(cardData: ArrayList<SingleCard>) {
        pmListNew.clear()
        if (defaultPMId != null && !defaultPMId.equals("null")) {
            for (i in cardData.indices) {
                if (!defaultPMId.equals(cardData.get(i).id)) {
                    pmListNew.add(cardData.get(i))
                }
            }
        }else{
            for (i in cardData.indices) {
                pmListNew.add(cardData.get(i))
            }
        }

        if(pmListNew.isEmpty()){
            relOtherCards!!.visibility = View.GONE
        }else{
            relOtherCards!!.visibility = View.VISIBLE
        }

        relPaymentMethod!!.visibility = View.VISIBLE

        txtNoPM!!.visibility = View.GONE

        linearLayoutManager = LinearLayoutManager(context)
        rv_pm?.layoutManager = linearLayoutManager
        val adapter = NewPaymentMethodsAdapter(requireContext(), pmListNew)
        rv_pm?.adapter = adapter

//        adapter.updateListForDefaultPM(defaultPMId!!)

        adapter.setClicklistner(this)
    }

    override fun itemclick(position: Int, paymentMethod: SingleCard) {

        selectedCardId = paymentMethod.id.toString()

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
                ?.enque(Network().apis()?.updatePaymentMethodStripe(selectedCardId))
                ?.execute()
        }
    }

    override fun deletePaymentMethod(position: Int, paymentMethod: SingleCard) {
        selectedCardId = paymentMethod.id.toString()
        AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.delete_payment_method_text_msg))
    }


    private fun setListeners() {

        back?.setOnClickListener(this)
        relPaymentMethodBack?.setOnClickListener(this)

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

        rladdPaymentMethod?.setOnClickListener {

            _type = "NEW"

            rladdPaymentMethod!!.visibility = View.GONE
            llAddNewCard!!.visibility = View.VISIBLE
            relPaymentMethod!!.visibility = View.GONE
            back!!.visibility = View. GONE

            /*if(cardInputWidget!!.visibility == View.VISIBLE){
                cardInputWidget!!.visibility = View.GONE
            }else{
                cardInputWidget!!.visibility = View.VISIBLE
            }*/

        }

        btnAddNewPM?.setOnClickListener{
            ////////
            //ORIGINAL
//            onAddCardButtonClicked()

            val auhSuccessful = BillingInformationAuth.authenticate(cardNumber?.text.toString(), cardExpiry?.text.toString()
                , cardCVV?.text.toString(), requireActivity())

            Log.d("ADD_CARD","111")

            if (auhSuccessful) {

                val delimiter1 = "/"
                val dateExpiry = cardExpiry?.text.toString().split(delimiter1)
               /* addCard?.card_number = cardNumber?.text.toString()
                addCard?.cvv = cardCVV?.text.toString()
                addCard?.exp_month = dateExpiry[0]
                addCard?.exp_year = dateExpiry[1]
                addCard?.card_holder = cardHolder?.text.toString()*/

                val cNumber = cardNumber?.text.toString()
                val cExpMonth = dateExpiry[0]
                val cExpYear = dateExpiry[1]
                val cCvv = cardCVV?.text.toString()
                val cHolderName = cardHolder?.text.toString()

                val card = PaymentMethodCreateParams.Card.Builder()
                    .setNumber(cNumber)
                    .setExpiryMonth(cExpMonth.toIntOrNull())
                    .setExpiryYear(cExpYear.toIntOrNull())
                    .setCvc(cCvv)
                    .build()

                onAddCardButtonClicked(card)

            }
        }
    }

    private fun onAddCardButtonClicked(card : PaymentMethodCreateParams.Card) {

        //OLD
        /*val paymentMethodCreateParams = cardInputWidget?.paymentMethodCreateParams
        if (paymentMethodCreateParams != null)
            addStripeCard(paymentMethodCreateParams)*/


        val params = PaymentMethodCreateParams.create(card)
        addStripeCard(params)

    }

    private fun addStripeCard(paymentMethodCreateParams: PaymentMethodCreateParams) {
        stripe!!.createPaymentMethod(
            paymentMethodCreateParams, null, null,
            object : ApiResultCallback<PaymentMethod> {
                override fun onSuccess(result: PaymentMethod) {
                    Log.i("[SUCCESS] Payment Method: ", result.toString())
                    Log.i("[SUCCESS] Stripe Token ID: ", result.id.toString())

                    val pmID = result.id.toString()
                    if(pmID.isNotEmpty()){
                        sendPMtoBackend(pmID)
                    }


                    // add a call to own server to save the details
                }

                override fun onError(e: java.lang.Exception) {
                    // add a Toast to show the exception
                    Notify.alerterRed(activity, e.message)
//                    Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.back->{
                activity?.onBackPressed()
            }
            R.id.relPaymentMethodBack -> {
                _type = "LISTING"

                rladdPaymentMethod!!.visibility = View.VISIBLE
                relPaymentMethod!!.visibility = View.VISIBLE
                back!!.visibility = View. VISIBLE
                llAddNewCard!!.visibility = View.GONE
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.UPDATE_PAYMENT_METHOD -> {

                val message = baseResponse?.message
                Log.e("TAG", "===message===" + message)
                Notify.alerterGreen(activity, message)

                checkIfDefaultPaymentMethod()
            }
            RequestCodes.API.DELETE_PAYMENT_METHOD -> {
                val message = baseResponse?.message
                Log.e("TAG", "===message===" + message)
                Notify.alerterGreen(activity, message)

                checkIfDefaultPaymentMethod()
            }
            RequestCodes.API.ADD_PAYMENT_METHOD -> {
                val message = baseResponse?.message
                Log.e("TAG", "===message===" + message)
                Notify.alerterGreen(activity, message)

                cardNumber!!.setText("")
                cardExpiry!!.setText("")
                cardCVV!!.setText("")
                cardHolder!!.setText("")


                rladdPaymentMethod!!.visibility = View.VISIBLE
                relPaymentMethod!!.visibility = View.VISIBLE
                llAddNewCard!!.visibility = View.GONE

                checkIfDefaultPaymentMethod()
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {

    }

    override fun callDialog(model: Any?) {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DELETE_PAYMENT_METHOD)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.deletePM(selectedCardId))
            ?.execute()
    }


}