package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.interfaces.ItemClickQuantity

class ViewHolderCart(itemView: View, quantityClick: ItemClickQuantity) :
    RecyclerView.ViewHolder(itemView) {
    var heading: TextView? = itemView.findViewById(R.id.tv_heading_category)
    var imageIcon: ImageView? = itemView.findViewById(R.id.imageView_category_icon)
    var tv_description: TextView? = itemView.findViewById(R.id.tv_description)
    var price: TextView? = itemView.findViewById(R.id.price)
    var plus: TextView? = itemView.findViewById(R.id.plus)
    var minus: TextView? = itemView.findViewById(R.id.minus)
    var quantity: TextView? = itemView.findViewById(R.id.quantity)

    init {
        plus?.setOnClickListener {
            quantityClick.itemPosition(adapterPosition, +1)
        }
        minus?.setOnClickListener {
            quantityClick.itemPosition(adapterPosition, -1)
        }
    }
}