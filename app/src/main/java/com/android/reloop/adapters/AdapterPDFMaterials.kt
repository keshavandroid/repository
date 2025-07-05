package com.reloop.reloop.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderPDFMaterials
import com.reloop.reloop.utils.Utils

class AdapterPDFMaterials(
    var pieChartLabels: ArrayList<String>,
    var pieChartValues: ArrayList<Double>,
    var pieChartUnits: ArrayList<Int>,
    var pieChartIcons: ArrayList<String>,
    var activity: Activity?
) :
    RecyclerView.Adapter<ViewHolderPDFMaterials>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderPDFMaterials {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_pdf_materials, viewGroup, false)

        return ViewHolderPDFMaterials(view)
    }

    override fun getItemCount() = pieChartLabels.size

    override fun onBindViewHolder(holder: ViewHolderPDFMaterials, position: Int) {
        try {
            holder.materialName?.text = pieChartLabels[position].toString()

            holder.materialValue!!.setText(""+ pieChartValues.get(position).let { Utils.commaConversion(it) }
                    + " " +
                    getUnitFromValue(pieChartUnits.get(position)))

            Utils.glideImageLoaderServer(holder.imgMaterial, pieChartIcons.get(position), R.drawable.icon_placeholder_generic)

        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun getUnitFromValue(i: Int): String? {
        var value = ""
        if(i == 1)
        {
            value = "kgs"
        }
        else if(i == 2)
        {
            value = "liter"
        }
        else if(i == 3)
        {
            value = "pieces"
        }
        return value
    }
}