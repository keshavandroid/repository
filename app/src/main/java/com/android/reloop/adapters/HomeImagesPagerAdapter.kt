package com.android.reloop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.reloop.reloop.R

class HomeImagesPagerAdapter(context: Context, sliderDataArrayList: ArrayList<String>, i: Int) : PagerAdapter() {
    var context: Context
    var layoutInflater: LayoutInflater
    var type : Int = 1
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
        fun itemclickSlide(position: Int)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as CardView
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var itemView : View ? = null
        val sliderItem = mSliderItemsNew[position]

        if(type == 1)
        {
            itemView = layoutInflater.inflate(R.layout.new_slider_layout, container, false)
        }
        else{
            itemView = layoutInflater.inflate(R.layout.slider_layout_product, container, false)
        }
        val imageView: ImageView = itemView?.findViewById(R.id.myimage) as ImageView


        // Glide is use to load image
        // from url in your imageview.
        Glide.with(context)
            .load(sliderItem)
            .fitCenter()
            .dontAnimate()
            .placeholder(R.drawable.icon_placeholder_generic)
            .diskCacheStrategy(DiskCacheStrategy.ALL)   //If your images are always same
            .format(DecodeFormat.PREFER_RGB_565)
            .skipMemoryCache(true)
            .into(imageView)

    container.addView(itemView)

        imageView.setOnClickListener {
            itemClickListener!!.itemclickSlide(position)
        }
    return itemView
}

override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
    container.removeView(`object` as CardView)
}

init {
    this.context = context
    this.mSliderItemsNew = sliderDataArrayList
    this.type = i
    layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
}
}