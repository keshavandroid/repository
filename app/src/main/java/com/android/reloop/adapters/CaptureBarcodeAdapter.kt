package com.android.reloop.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.bottomsheet.MaterialBottomsheet
import com.android.reloop.model.BarcodeModel
import com.bumptech.glide.Glide
import com.reloop.reloop.R
import com.reloop.reloop.adapters.viewholders.ViewHolderCart
import com.reloop.reloop.interfaces.ItemClickQuantity
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*

class CaptureBarcodeAdapter(var imageList: List<BarcodeModel>, var context: Context, var listener: ClickListenerBarcode) :
    RecyclerView.Adapter<CaptureBarcodeAdapter.ViewHolderImage>() {

    interface ClickListenerBarcode {
        fun onDeleteClick(position: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderImage {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_barcode_image, viewGroup, false)
        return ViewHolderImage(view)
    }

    override fun getItemCount() = imageList.size

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolderImage, position: Int) {

//        Glide.with(context)
//            .load(imageList.get(position).imgBitmap)
//            .into(holder.img!!);

        holder.txtBarcodeResult!!.setText(imageList.get(position).barcodeResult)

        holder.txtBarcodeResult!!.setOnClickListener{
            listener.onDeleteClick(position)
        }

    }

    inner class ViewHolderImage(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        //var img: ImageView? = itemView.findViewById(R.id.image_view)

        var txtBarcodeResult: TextView? = itemView.findViewById(R.id.txtBarcodeResult)

    }
}