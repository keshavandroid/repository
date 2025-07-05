package com.android.reloop.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.android.reloop.utils.ZoomableImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.reloop.reloop.R


class FullScreenPagerAdapter(
    context: Context,
    sliderDataArrayList: ArrayList<String>,
    i: Int
) : PagerAdapter() {
    var context: Context
    var layoutInflater: LayoutInflater
    var type: Int = 1
    lateinit var lldots: LinearLayout
    private val mSliderItemsNew: ArrayList<String>

    private var itemClickListener: ItemClickListener? = null

    override fun getCount(): Int {
        return mSliderItemsNew.size
    }

    fun setClicklistner(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    interface ItemClickListener {
        fun itemclickSlide(position: Int, imageUrls: ArrayList<String>)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as RelativeLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var itemView: View? = null
        if (type == 1) {
            itemView =
                layoutInflater.inflate(R.layout.full_screen_image_layout, container, false)
        } else {
            itemView = layoutInflater.inflate(R.layout.full_screen_image_layout, container, false)
        }

        val imageView: ImageView = itemView?.findViewById(R.id.myimageFull) as ZoomableImageView
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarFull) as ProgressBar

        val sliderItem = mSliderItemsNew[position]

        // Glide is use to load image
        /*Glide.with(context)
            .load(sliderItem)
            .fitCenter()
            .dontAnimate()
            .placeholder(R.drawable.icon_placeholder_generic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)   //If your images are always same
            .format(DecodeFormat.PREFER_RGB_565)
            .skipMemoryCache(true)
            .into(imageView)*/


        Glide.with(context)
            .load(sliderItem)
            .fitCenter()
            .dontAnimate()
//            .placeholder(R.drawable.icon_placeholder_generic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .format(DecodeFormat.PREFER_RGB_565)
            .skipMemoryCache(true)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    @Nullable e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Handle exceptions differently if you want
                    progressBar.visibility = View.GONE

                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE

                    return false
                }
            }).into(imageView)

        container.addView(itemView)

        /*imageView.setOnClickListener {
            itemClickListener!!.itemclickSlide(position,mSliderItemsNew)
        }*/
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

    init {
        this.context = context
        this.mSliderItemsNew = sliderDataArrayList
        this.type = i
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}