package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderShopRV
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.Utils

class AdapterMaterialList(
    var dataList: ArrayList<MaterialCategories>?,
    var activity: Activity) :
    RecyclerView.Adapter<ViewHolderShopRV>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderShopRV {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_products_list, viewGroup, false)
        return ViewHolderShopRV(view)
    }

    override fun getItemCount() = dataList!!.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolderShopRV, position: Int) {
        holder.heading?.text = dataList?.get(position)?.name
        Utils.glideImageLoaderServer(
            holder.imageIcon,
            dataList?.get(position)?.avatar,
            R.drawable.icon_placeholder_generic
        )
        if (dataList?.get(position)?.selected!!) {
            holder.layout?.background = activity.getDrawable(R.drawable.layout_shape_selected)
        } else {
            holder.layout?.background = null
        }
        holder.info?.setOnClickListener {
            AlertDialogs.informationDialog(activity, dataList?.get(position)?.avatar, dataList?.get(position)?.name,
                dataList?.get(position)?.description, R.drawable.icon_placeholder_generic)
        }
        holder.itemView.setOnClickListener {

            if (dataList!![position].selected!!) {
                dataList!![position].selected = false
                notifyDataSetChanged()
            } else {
                dataList!![position].selected = true
                notifyDataSetChanged()
            }
        }
    }
}