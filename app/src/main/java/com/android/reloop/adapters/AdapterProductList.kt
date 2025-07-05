package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderShopRV
import com.reloop.reloop.fragments.SelectCategoriesFragment
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.collectionrequest.RecyclingFamilies
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.Utils
import java.io.Serializable


class AdapterProductList(
    var dataList: ArrayList<MaterialCategories>?,
    var activity: Activity,
    var maxLimitValue: Int,
    var recyclingFamiliesList: ArrayList<RecyclingFamilies>?,
    private val items: ArrayList<SelectCategoriesFragment.ListItem>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var shownItemsMap = HashMap<String, Boolean>()
    private val VIEW_TYPE_CATEGORY = 1
    private val VIEW_TYPE_ITEM = 2


    fun setMaxLimit(maxLimit: Int) {
        this.maxLimitValue = maxLimit
    }


    override fun getItemViewType(position: Int): Int {
        return when {
            items[position].category != null -> VIEW_TYPE_CATEGORY
            items[position].item != null -> VIEW_TYPE_ITEM
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    fun getSpanSize(position: Int, spanCount: Int): Int {
        return when (getItemViewType(position)) {
            VIEW_TYPE_CATEGORY -> spanCount // Category items span the entire row
            VIEW_TYPE_ITEM -> 1 // Regular items take up one span
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        //OLD view
//        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_products_list, viewGroup, false)

        //NEW view WORKING WITH THREE COLUMNS
        /*val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_product_list_three_column, viewGroup, false)
        return ViewHolderShopRV(view)*/

        return when (viewType) {
            VIEW_TYPE_CATEGORY -> HeaderViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.row_material_title, viewGroup, false))
            VIEW_TYPE_ITEM -> ViewHolderShopRV(LayoutInflater.from(viewGroup.context).inflate(R.layout.row_product_list_three_column, viewGroup, false))
            else -> throw IllegalArgumentException("Invalid view type")

        }
    }


    /*override fun getItemCount(): Int {
        return dataList!!.size
    }*/

    override fun getItemCount(): Int {
        return items.size
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        /*Log.d("MATE_CAT",""+GsonBuilder().setPrettyPrinting().create().toJson(dataList!!.get(position)))

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
            AlertDialogs.informationDialog(
                activity,
                dataList?.get(position)?.avatar,
                dataList?.get(position)?.name,
                dataList?.get(position)?.description,
                R.drawable.icon_placeholder_generic
            )
        }


        holder.itemView.setOnClickListener {
            shownItemsMap.clear()

            var count = 0
            for (i in dataList!!.indices) {
                if (dataList!![i].selected!!) {
                    count += 1
                }
            }

            if (dataList!![position].selected!!) {
                dataList!![position].selected = false

                notifyDataSetChanged() //OLD

            } else {
                if (count < maxLimitValue) {
                    dataList!![position].selected = true

                    notifyDataSetChanged() //OLD

                } else {
                    Notify.alerterRed(activity, "Sorry you are not allowed to select more than $maxLimitValue categories")
                }
            }


        }*/

        val listItem = items[position]
        when (holder.itemViewType) {
            VIEW_TYPE_CATEGORY -> {
                val categoryViewHolder = holder as HeaderViewHolder
                categoryViewHolder.materialTitle!!.text = listItem.category!!.name
            }
            VIEW_TYPE_ITEM -> {
                val holderCategory = holder as ViewHolderShopRV

                holderCategory.heading?.text = listItem.item!!.name
                Utils.glideImageLoaderServer(
                    holderCategory.imageIcon,
                    listItem.item!!.avatar,
                    R.drawable.icon_placeholder_generic
                )
                if (listItem.item!!.selected!!) {
                    holderCategory.layout?.background = activity.getDrawable(R.drawable.layout_shape_selected)
                } else {
                    holderCategory.layout?.background = null
                }
                holderCategory.info?.setOnClickListener {
                    AlertDialogs.informationDialog(
                        activity,
                        listItem.item!!.avatar,
                        listItem.item!!.name,
                        listItem.item!!.description,
                        R.drawable.icon_placeholder_generic
                    )
                }
                holderCategory.itemView.setOnClickListener {
                    var count = 0
                    for (i in dataList!!.indices) {
                        if (dataList!![i].selected!!) {
                            count += 1
                        }
                    }

                    if (listItem.item!!.selected!!) {
                        listItem.item!!.selected = false
                        notifyDataSetChanged()
                    } else {
                        if (count < maxLimitValue) {
                            listItem.item!!.selected = true
                            notifyDataSetChanged()
                        } else {
                            Notify.alerterRed(activity, "Sorry you are not allowed to select more than $maxLimitValue categories")
                        }
                    }
                }
            }
        }
    }


    private class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var materialTitle: TextView? = null

        init {
            materialTitle = view.findViewById(R.id.materialTitle)

        }
    }
}