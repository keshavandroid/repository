package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R

class ViewHolderPDFMaterials(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    var imgMaterial: ImageView? = itemView.findViewById(R.id.imgMaterial)
    var materialValue: TextView? = itemView.findViewById(R.id.materialValue)
    var materialName: TextView?= itemView.findViewById(R.id.materialName)


}