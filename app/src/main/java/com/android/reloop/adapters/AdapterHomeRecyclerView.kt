package com.reloop.reloop.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderHomeCategories
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.model.ModelHomeCategories
import com.reloop.reloop.network.serializer.dashboard.EnvironmentalStatsDescription
import com.reloop.reloop.utils.AlertDialogs
import com.reloop.reloop.utils.Utils

class AdapterHomeRecyclerView(
    var dataList: ArrayList<ModelHomeCategories>,
    var environmentalStatsDescriptions: ArrayList<EnvironmentalStatsDescription>?,
    var activity: Activity?
) :
    RecyclerView.Adapter<ViewHolderHomeCategories>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderHomeCategories {
        //OLD view
//        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_home_categories, viewGroup, false)

        //New View
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_row_store_contribution, viewGroup, false)

        return ViewHolderHomeCategories(view)
    }

    override fun getItemCount() = dataList.size

    override fun onBindViewHolder(holder: ViewHolderHomeCategories, position: Int) {
        holder.heading?.text = dataList[position].heading
        holder.unit?.text = dataList[position].unit
        holder.tv_status?.text = dataList[position].status

        dataList[position].icons?.let {
            holder.imageIcon?.setImageResource(it)
        }
        holder.points?.text = dataList.get(position).points?.let { Utils.commaConversion(it) }

        /*holder.info?.setOnClickListener {
            if (!environmentalStatsDescriptions.isNullOrEmpty()) {
                for (i in environmentalStatsDescriptions!!.indices) {
                    if (environmentalStatsDescriptions?.get(i)?.id == dataList[position].id
                    ) {
                        AlertDialogs.informationDialog(
                            activity,
                            "",
                            environmentalStatsDescriptions?.get(i)?.title,
                            environmentalStatsDescriptions?.get(i)?.description,
                            dataList[position].icons!!
                        )
                        break
                    }
                }
            }
        }*/

        holder.itemView.setOnClickListener {
//            holder.info!!.performClick()

            if (!environmentalStatsDescriptions.isNullOrEmpty()) {
                for (i in environmentalStatsDescriptions!!.indices) {
                    if (environmentalStatsDescriptions?.get(i)?.id == dataList[position].id
                    ) {
                        AlertDialogs.informationDialog(
                            activity,
                            "",
                            environmentalStatsDescriptions?.get(i)?.title,
                            environmentalStatsDescriptions?.get(i)?.description,
                            dataList[position].icons!!)
                        break
                    }
                }
            }
        }
    }
}