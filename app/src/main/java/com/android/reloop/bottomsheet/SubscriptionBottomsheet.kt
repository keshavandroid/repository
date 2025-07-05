package com.android.reloop.bottomsheet

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.model.SavedCard
import com.android.reloop.model.SingleCard
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reloop.reloop.R
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import com.stripe.android.*
import com.stripe.android.model.*
import com.stripe.android.view.CardInputWidget
import com.stripe.android.view.CardMultilineWidget
import kotlinx.android.synthetic.main.bottomsheet_subscription.*
import retrofit2.Call
import retrofit2.Response


class SubscriptionBottomsheet : BottomSheetDialogFragment() {
  var mListener: ItemClickListener? = null
  private val header: String? = null
  private var mContext: Context? = null

  private val ARG_NAME = "item_name"
  var adapter: PaymentMethodsAdapterNew?=null

  var cardData: ArrayList<SingleCard>? = ArrayList()

  var stripe: Stripe?=null

  var isEdit : String?=""

  companion object{
      var pmID: String = ""
  }

  fun newInstance(
    listner:ItemClickListener,
    header: String?,
    itemCards: SavedCard?,

    ): SubscriptionBottomsheet {
    val fragment = SubscriptionBottomsheet()
    val args = Bundle()
    args.putString("header", header)
    if(itemCards!=null){
      if(!itemCards.cardData.isNullOrEmpty()){
        args.putSerializable(ARG_NAME, itemCards.cardData)
        args.putString("is_card", "true")
      }else{
        args.putString("is_card", "false")
      }
    }else{
      args.putString("is_card","false")
    }


    fragment.setArguments(args)
    return fragment
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.bottomsheet_subscription, container, false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    val stripeKey: String = Constants.PK_STRIPE

    val headerTV = view.findViewById<TextView>(R.id.title)
    val back = view.findViewById<Button>(R.id.back)
    val btnSubscribe = view.findViewById<Button>(R.id.btnSubscribe)
    val btnSelectCard = view.findViewById<Button>(R.id.btnSelectCard)
    val cardInputWidget = view.findViewById<CardMultilineWidget>(R.id.cardInputWidget)
    val rlListOfCards = view.findViewById<RelativeLayout>(R.id.rlListOfCards)
    val rv_paymentmethods = view.findViewById<RecyclerView>(R.id.rv_paymentmethods)
    val rlAddcard = view.findViewById<RelativeLayout>(R.id.rlAddcard)
    val imgbackArrow = view.findViewById<ImageView>(R.id.imgbackArrow)

    val txtEdit = view.findViewById<TextView>(R.id.txtEdit)
    val txtDone = view.findViewById<TextView>(R.id.txtDone)


    headerTV.text = requireArguments().getString("header")

    val isCard = requireArguments().getString("is_card")

    if(isCard.equals("true")){
      cardData = requireArguments().getSerializable(ARG_NAME) as ArrayList<SingleCard>

      cardInputWidget.visibility = View.GONE
      rlListOfCards.visibility = View.VISIBLE

      btnSelectCard.visibility = View.VISIBLE
      btnSubscribe.visibility = View.GONE

      txtEdit.visibility = View.VISIBLE
      txtDone.visibility = View.GONE

      rv_paymentmethods?.setLayoutManager(LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false))
      adapter = PaymentMethodsAdapterNew(requireActivity(),requireContext(), cardData!!)
      rv_paymentmethods.adapter = adapter

    }else{
      cardInputWidget.visibility = View.VISIBLE
      rlListOfCards.visibility = View.GONE

      btnSelectCard.visibility = View.GONE
      btnSubscribe.visibility = View.VISIBLE

      txtEdit.visibility = View.GONE
      txtDone.visibility = View.GONE

    }

    back.setOnClickListener{
      dismiss()
    }

    rlAddcard.setOnClickListener{
      cardInputWidget.visibility = View.VISIBLE
      rlListOfCards.visibility = View.GONE
      imgbackArrow.visibility = View.VISIBLE

      btnSelectCard.visibility = View.GONE
      btnSubscribe.visibility = View.VISIBLE

      txtEdit.visibility = View.GONE
      txtDone.visibility = View.GONE

    }


    imgbackArrow.setOnClickListener{
      cardInputWidget.visibility = View.GONE
      rlListOfCards.visibility = View.VISIBLE
      imgbackArrow.visibility = View.GONE

      btnSelectCard.visibility = View.VISIBLE
      btnSubscribe.visibility = View.GONE

      if(isCard.equals("true")){
        txtEdit.visibility = View.VISIBLE
        txtDone.visibility = View.GONE
      }else{
        txtEdit.visibility = View.GONE
        txtDone.visibility = View.GONE
      }


    }

    txtEdit.setOnClickListener{
      txtEdit.visibility = View.GONE
      txtDone.visibility = View.VISIBLE
      isEdit = "true"

      if(adapter!=null){
        adapter!!.checkedPosition = -1
        for (i in cardData!!.indices){
          cardData!!.get(i).isDelete = true
          cardData!!.get(i).itemClickable = false
        }

        adapter!!.setItems(cardData!!)

      }
    }

    txtDone.setOnClickListener{
      txtEdit.visibility = View.VISIBLE
      txtDone.visibility = View.GONE

      isEdit = "false"
      if(adapter!=null){

        for (i in cardData!!.indices){
          cardData!!.get(i).isDelete = false
          cardData!!.get(i).itemClickable = true

        }

        adapter!!.setItems(cardData!!)

      }
    }

    btnSelectCard.setOnClickListener{

      if(isEdit.equals("true")){
        Toast.makeText(requireContext(),"Can not pay while editing card",Toast.LENGTH_SHORT).show()
      }else{
        if(adapter!=null){
          if(adapter!!.checkedPosition == -1){
            Toast.makeText(requireContext(),"Please select payment method",Toast.LENGTH_SHORT).show()
          }else{

            var selectedCard: SingleCard? = adapter!!.getSelected()

            if (selectedCard != null) {
              mListener!!.onPaymentMethodID(selectedCard.id!!)
            }

            dismiss()

          }
        }
      }
    }

    stripe = Stripe(requireContext(), stripeKey)


    cardInputWidget.postalCodeRequired = false
    cardInputWidget.setShouldShowPostalCode(false)

    btnSubscribe.setOnClickListener{

      onAddCardButtonClicked(cardInputWidget)

    }

  }

  private fun onAddCardButtonClicked(cardInputWidget: CardMultilineWidget) {
    val paymentMethodCreateParams = cardInputWidget.paymentMethodCreateParams
    if (paymentMethodCreateParams != null){
      addStripeCard(paymentMethodCreateParams)
    }

  }


  private fun addStripeCard(paymentMethodCreateParams: PaymentMethodCreateParams) {
    stripe!!.createPaymentMethod(
      paymentMethodCreateParams, null, null,
      object : ApiResultCallback<PaymentMethod> {
        override fun onSuccess(result: PaymentMethod) {

          if(result.id != null){
            if(!result.id.toString().isEmpty()){
              Log.i("[SUCCESS] Payment Method: ", result.toString())
              Log.i("[SUCCESS] Stripe Token ID: ", result.id.toString())
              // add a call to own server to save the details

              pmID = result.id.toString()
              mListener!!.onPaymentMethodID(pmID)


            }else{
              Toast.makeText(requireContext(),"Please check your card details",Toast.LENGTH_SHORT).show()
            }
          }else{
            Toast.makeText(requireContext(),"Please check your card details",Toast.LENGTH_SHORT).show()
          }
          dismiss()
        }

        override fun onError(e: java.lang.Exception) {
          // add a Toast to show the exception

          Toast.makeText(requireContext(),"Please check your card details",Toast.LENGTH_SHORT).show()

        }

      })

  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    mContext = context
    mListener = parentFragment as ItemClickListener?
    if( mListener == null ) {
      throw RuntimeException("Could not get listener");
    }
  }

  override fun onDetach() {
    mListener = null
    super.onDetach()
  }

  override fun onCancel(dialog: DialogInterface) {
    super.onCancel(dialog)

  }

  interface ItemClickListener {
    fun onPaymentMethodID(pmId: String)

  }

  class PaymentMethodsAdapterNew() : RecyclerView.Adapter<PaymentMethodsAdapterNew.ViewHolder>(),
    AlertDialogCallback, OnNetworkResponse {

    var list: ArrayList<SingleCard> = ArrayList()
    lateinit var context: Context
    lateinit var activity: Activity
    var itemClickListener: ItemClickListener? = null

    var checkedPosition = -1
    var deleteClickPos = -1



    fun setClicklistner(itemClickListener: ItemClickListener) {
      this.itemClickListener = itemClickListener
    }

    constructor(activity: Activity,context: Context, lists: ArrayList<SingleCard>) : this() {
      this.activity = activity
      this.context=context
      this.list = lists
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

      val view: View

      view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_payment_method_new, parent, false)

      return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

      val data = list.get(position)

      try {
        holder.txtLastFour.setText("...." + data.itemCard!!.last4)

        if(data.itemCard!!.brand.equals("visa", ignoreCase = true)){
          holder.ivcard.setImageDrawable(context.resources.getDrawable(R.drawable.ic_visa_card))
        }else if(data.itemCard!!.brand.equals("MasterCard",ignoreCase = true)){
          holder.ivcard.setImageDrawable(context.resources.getDrawable(R.drawable.ic_master_card))
        }else{
          holder.ivcard.setImageDrawable(context.resources.getDrawable(R.drawable.ic_card_grey))
        }

        if (checkedPosition == -1) {
          holder.card_details.setBackgroundResource(R.color.zxing_transparent);
        } else {
          if (checkedPosition == holder.getAdapterPosition()) {
            holder.card_details.setBackgroundResource(R.drawable.bg_green_border_1dp);
          } else {
            holder.card_details.setBackgroundResource(R.color.zxing_transparent);
          }
        }

        holder.cardView.setOnClickListener{
          if(list.get(position).itemClickable){
            holder.card_details.setBackgroundResource(R.drawable.bg_green_border_1dp);
            if (checkedPosition != holder.getAdapterPosition()) {
              notifyItemChanged(checkedPosition)
              checkedPosition = holder.getAdapterPosition()
            }
          }
        }

        if(data.isDelete){
          holder.cardViewDelete.visibility = View.VISIBLE
        }else{
          holder.cardViewDelete.visibility = View.GONE
        }

        holder.cardViewDelete.setOnClickListener{
            deleteClickPos = position
            AlertDialogs.alertDialogFragment(context, this, "Are you sure you want to delete this card?")
        }

      }catch (e: Exception){
        e.printStackTrace()
      }

    }

    fun getSelected(): SingleCard? {
      return if (checkedPosition != -1) {
        list.get(checkedPosition)
      } else null
    }

    fun setItems(lists: ArrayList<SingleCard>) {
      deleteClickPos = -1
      this.list = lists
      notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
      return list.size
    }

    interface ItemClickListener {
      fun itemclick(position: Int,paymentMethod: SingleCard)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

      internal var ivcard: ImageView
      internal var txtLastFour: TextView
      internal var cardView: CardView
      internal var card_details: RelativeLayout
      internal var cardViewDelete: CardView

      init {
        ivcard = view.findViewById(R.id.iv_card)
        txtLastFour = view.findViewById(R.id.txtLastFour)
        cardView = view.findViewById(R.id.cardView)
        card_details = view.findViewById(R.id.card_details)

        cardViewDelete = view.findViewById(R.id.cardViewDelete)

      }
    }

    override fun callDialog(model: Any?) {
      if (model as Int == 1) {
        if(deleteClickPos != -1){
          NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DELETE_CARD)
            ?.autoLoading(activity)
            ?.enque(Network().apis()?.deleteCard(""+list.get(deleteClickPos).id!!))
            ?.execute()
        }

      }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
      when (tag) {
        RequestCodes.API.DELETE_CARD -> {
          list.removeAt(deleteClickPos)
          setItems(list)
        }
      }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
      //Notify.alerterRed(activity, response?.message)
      Toast.makeText(context,""+response?.message,Toast.LENGTH_SHORT).show()
    }
  }
}