package com.android.reloop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.reloop.reloop.R

class InfiniteSliderAdapter internal constructor(
    context: Context,
    sliderItems: ArrayList<String>,
    i: Int,
    viewPager2: ViewPager2
) :
    RecyclerView.Adapter<InfiniteSliderAdapter.SliderViewHolder>() {
    var context: Context
    private val sliderItems: ArrayList<String>
    private val viewPager2: ViewPager2
    var type : Int = 1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderViewHolder {
        return SliderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.slider_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        holder.setImage(sliderItems[position])
        if (position == sliderItems.size - 2) {
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int {
        return sliderItems.size
    }

    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView
        fun setImage(sliderItems: String) {


            Glide.with(context)
                .load(sliderItems)
//            .thumbnail(0.05f)
//            .transition(DrawableTransitionOptions.withCrossFade()) // for transition
                .fitCenter()
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)   //If your images are always same
                .format(DecodeFormat.PREFER_RGB_565)
                .skipMemoryCache(true)
                .into(imageView)
        }

        init {
            imageView = itemView.findViewById(R.id.myimage)
        }
    }

    private val runnable = Runnable {
        sliderItems.addAll(sliderItems)
        notifyDataSetChanged()
    }

    init {
        this.context = context
        this.sliderItems = sliderItems
        this.type = i
        this.viewPager2 = viewPager2
    }
}

