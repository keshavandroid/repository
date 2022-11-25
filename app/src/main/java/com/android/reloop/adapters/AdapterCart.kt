package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderCart
import com.reloop.reloop.interfaces.ItemClickQuantity
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils

class AdapterCart(var dataList: ArrayList<Category?>?, var quantityClick: ItemClickQuantity) :
    RecyclerView.Adapter<ViewHolderCart>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderCart {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_cart_information, viewGroup, false)
        return ViewHolderCart(view, quantityClick)
    }

    override fun getItemCount() = dataList!!.size

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolderCart, position: Int) {
        holder.heading?.text = dataList?.get(position)?.name
        /*  dataList?.get(position)?.icon?.let {
              holder.imageIcon?.setImageResource(it)
          }*/
        holder.quantity?.text = "${dataList?.get(position)?.quantity}"
        holder.price?.text = "${Constants.currencySign} ${Utils.commaConversion(dataList?.get(position)?.price)}"
        holder.tv_description?.text = dataList?.get(position)?.description
        holder.tv_description?.movementMethod = ScrollingMovementMethod()
        holder.tv_description?.setOnTouchListener { v, event ->
            holder.tv_description!!.parent.requestDisallowInterceptTouchEvent(true)
            v?.onTouchEvent(event) ?: true
        }
        Utils.glideImageLoaderServer(
            holder.imageIcon,
            dataList?.get(position)?.avatarToShow,R.mipmap.ic_launcher
        )
    }
}