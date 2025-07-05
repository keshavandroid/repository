package com.android.reloop.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.reloop.reloop.R

class ImagesAdapter(
    private val context: Context,
    private val imageUris: List<Uri>,
    private val onRemoveImageListener: OnRemoveImageListener
) :
    RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = imageUris[position]
        //            holder.imageView.setImageURI(uri);
        Glide.with(context)
            .load(uri) // Load the full image URI
            .thumbnail(0.1f) // Load a 10% sized thumbnail first
            .into(holder.imageView)
        holder.btnRemove.setOnClickListener { v: View? ->
            onRemoveImageListener.onRemoveImage(
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var btnRemove: ImageView

        init {
            imageView = itemView.findViewById(R.id.image_view)
            btnRemove = itemView.findViewById(R.id.btn_remove)
        }
    }

    interface OnRemoveImageListener {
        fun onRemoveImage(position: Int)
    }
}