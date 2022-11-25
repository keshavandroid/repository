package com.android.reloop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.model.ModelCampainNews
import com.android.reloop.model.ModelProductRecyclable
import com.reloop.reloop.R

class ProductRecyclableAdapter() : RecyclerView.Adapter<ProductRecyclableAdapter.ViewHolder>() {

    var list: ArrayList<ModelProductRecyclable> = ArrayList()
    lateinit var context: Context

    private var itemClickListener: ItemClickListener? = null

    fun setClicklistner(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    constructor(context: Context, foodlist: ArrayList<ModelProductRecyclable>) : this() {

        this.context=context
        this.list = foodlist
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View

        view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_product_recycled, parent, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        //val data = list.get(position)

    }

    override fun getItemCount(): Int {
        return list.size
//        return 5
    }

    interface ItemClickListener {
        fun itemclick(bean: ModelProductRecyclable,position: Int,type : String)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var tvname: TextView
        internal var llColor : LinearLayout

        init {

            tvname = view.findViewById(R.id.tvName)
            llColor = view.findViewById(R.id.llColor)
        }
    }
}