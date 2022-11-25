package com.reloop.reloop.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderShopRV
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.interfaces.ShopFragmentItemClick
import com.reloop.reloop.model.ModelShopCategories


class AdapterShopFragment(
    var dataList: ArrayList<ModelShopCategories>,
    var click: ShopFragmentItemClick
) :
    RecyclerView.Adapter<ViewHolderShopRV>() {
    /*  var HEADER=0
      var NORMAL_ITEM=1*/
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderShopRV {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_shop_category, viewGroup, false)
        return ViewHolderShopRV(view)
    }

    override fun getItemCount() = dataList.size
    override fun onBindViewHolder(holder: ViewHolderShopRV, position: Int) {

//        if (dataList[position].type == 1 || dataList[position].type == 2) {
//        if (dataList[position].type == 1 && !holder.products_heading?.text?.equals(
//                MainApplication.applicationContext().getString(R.string.services)
//            )!!
//        ) {
//            holder.products_heading?.visibility = View.VISIBLE
//            holder.products_heading?.text =
//                MainApplication.applicationContext().getString(R.string.services)
//        } else if (dataList[position].type == 1 && holder.products_heading?.text?.equals(
//                MainApplication.applicationContext().getString(R.string.services)
//            )!!
//        ) {
//            holder.products_heading?.visibility = View.GONE
//
//        } else if (dataList[position].type == 2 && !holder.products_heading?.text?.equals(
//                MainApplication.applicationContext().getString(R.string.products)
//            )!!
//        ) {
//            holder.products_heading?.text =
//                MainApplication.applicationContext().getString(R.string.products)
//        } else {
//            holder.products_heading?.visibility = View.GONE
//        }
//    else {
//            holder.products_heading?.visibility = View.GONE
//        }
//        if (position == 2 || position == 0) {
//            holder.products_heading?.visibility = View.VISIBLE
//            if (position == 0) {
//                holder.products_heading?.text =
//                    MainApplication.applicationContext().getString(R.string.services)
//            } else if (position == 2) {
//                holder.products_heading?.text =
//                    MainApplication.applicationContext().getString(R.string.products)
//            }
//        } else {
//            holder.products_heading?.visibility = View.GONE
//        }
        holder.heading?.text = dataList.get(position).heading
        dataList[position].icons?.let {
            holder.imageIcon?.setImageResource(it)
        }
        holder.cardView?.setOnClickListener {
            click.itemPosition(position, dataList[holder.adapterPosition].type)
        }

    }
    /*   override fun getItemViewType(position: Int): Int {
           return if (position == 0) {
               HEADER
           } else {
               NORMAL_ITEM
           }
       }*/
}