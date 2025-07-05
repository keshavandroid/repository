package com.android.reloop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.model.PaymentMethods
import com.reloop.reloop.R


class PaymentMethodsAdapter() : RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder>() {

    var list: ArrayList<PaymentMethods> = ArrayList()
    lateinit var context: Context
    var itemClickListener: ItemClickListener? = null
    private var checkedPosition = -1

    fun setClicklistner(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    constructor(context: Context, lists: ArrayList<PaymentMethods>) : this() {

        this.context=context
        this.list = lists
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View

        view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_method, parent, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = list.get(position)

        holder.tvcardno.setText("**** **** **** " + data.getNumber())

        /* if(data.getIsDefault() == 1)
        {
            holder.radiobutton.isChecked = true
            holder.ivdelete.visibility = View.GONE
        }
        else{
            holder.radiobutton.isChecked = false
            holder.ivdelete.visibility = View.VISIBLE
        }*/

        if (checkedPosition == -1) {
            holder.radiobutton.isChecked = false
        } else {
            if (checkedPosition == holder.getAdapterPosition()) {
                holder.radiobutton.isChecked = true
            } else {
                holder.radiobutton.isChecked = false
            }
        }

        if(data.getBrand().equals("visa", ignoreCase = true)){
            holder.ivcard.setImageDrawable(context.resources.getDrawable(R.drawable.ic_visa_card))
        }else if(data.getBrand().equals("MasterCard",ignoreCase = true)){
            holder.ivcard.setImageDrawable(context.resources.getDrawable(R.drawable.ic_master_card))
        }else{
            holder.ivcard.setImageDrawable(context.resources.getDrawable(R.drawable.ic_card_grey))
        }

        holder.radiobutton.setOnClickListener {
            holder.radiobutton.isChecked = true
            if (checkedPosition != holder.getAdapterPosition()) {
                notifyItemChanged(checkedPosition)
                checkedPosition = holder.getAdapterPosition()
            }
            itemClickListener!!.itemclick(position,list.get(position))
        }

        holder.rlCardName.setOnClickListener{
            holder.radiobutton.isChecked = true
            if (checkedPosition != holder.getAdapterPosition()) {
                notifyItemChanged(checkedPosition)
                checkedPosition = holder.getAdapterPosition()
            }
//          itemClickListener!!.itemclick(position,list.get(position)) // OLD CODE onClick of setDefault
        }

        /*  holder.itemView.setOnClickListener {
            itemClickListener!!.itemclick(position)
        }*/

        holder.txtSetDefault.setOnClickListener{
            itemClickListener!!.itemclick(position,list.get(position)) // NEW CODE onClick of setDefault
        }

        holder.ivdelete.setOnClickListener {
            itemClickListener!!.deletePaymentMethod(position,list.get(position))
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    interface ItemClickListener {
       // fun itemclick(bean: ModelCampainNews,position: Int,type : String)
       fun itemclick(position: Int,paymentMethod: PaymentMethods)
       fun deletePaymentMethod(position: Int,paymentMethod: PaymentMethods)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var ivcard: ImageView
        internal var tvcardno: TextView
        internal var radiobutton: RadioButton
        internal var ivdelete: ImageView
        internal var rlCardName: RelativeLayout
        internal var txtSetDefault: TextView

        init {
            ivcard = view.findViewById(R.id.iv_card)
            tvcardno = view.findViewById(R.id.tv_card_no)
            radiobutton = view.findViewById(R.id.radio_button)
            ivdelete = view.findViewById(R.id.iv_delete)
            rlCardName = view.findViewById(R.id.rlCardName)
            txtSetDefault = view.findViewById(R.id.txtSetDefault)

        }
    }
}