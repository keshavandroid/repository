package com.android.reloop.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.reloop.reloop.R
import com.smarteist.autoimageslider.SliderViewAdapter
import android.widget.LinearLayout
import com.android.reloop.utils.ViewPagerDots
import com.bumptech.glide.load.DecodeFormat
import com.reloop.reloop.utils.Utils


public class SliderAdapter(
    context: Context,
  /*  sliderDataArrayList: ArrayList<CampaignImage>,*/
    sliderDataArrayList: ArrayList<String>,
    i: Int,
    layoutDots: LinearLayout
) :
    SliderViewAdapter<SliderAdapter.SliderAdapterViewHolder>() {

    // list for storing urls of images.
    //private val mSliderItems: ArrayList<CampaignImage>
    private val mSliderItemsNew: ArrayList<String>

    lateinit var lldots: LinearLayout
    lateinit var context: Context
    private var itemClickListener: ItemClickListener? = null
    var type : Int = 1
    // Constructor
    init {
        //this.mSliderItems = sliderDataArrayList
        this.mSliderItemsNew = sliderDataArrayList
        this.context = context
        this.type = i
        this.lldots = layoutDots
    }

    fun setClicklistner(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    // We are inflating the slider_layout
    // inside on Create View Holder method.
    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterViewHolder {
        var inflate : View ? = null
        //if(type == 1)
        //{
            inflate = LayoutInflater.from(parent.context).inflate(R.layout.slider_layout, null)
        //}
    /*    else{
            inflate = LayoutInflater.from(parent.context).inflate(R.layout.slider_layout_product, null)
        }*/
        return SliderAdapterViewHolder(inflate)
    }

    // Inside on bind view holder we will
    // set data to item of Slider View.
    override fun onBindViewHolder(viewHolder: SliderAdapterViewHolder, position: Int) {
        //val sliderItem: CampaignImage = mSliderItems[position]
        Log.e("TAG","====slider position===" + position)
        val sliderItem = mSliderItemsNew[position]

        ViewPagerDots.addBottomDots(position, context, lldots, mSliderItemsNew.size)

        // Glide is use to load image
        // from url in your imageview.

     /*  Glide.with(context)
            .load(sliderItem)
            .fitCenter()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)   //If your images are always same
           .format(DecodeFormat.PREFER_RGB_565)
            .skipMemoryCache(true)
            .into(viewHolder.imageViewBackground)*/

        Utils.glideImageLoaderServer(viewHolder.imageViewBackground, sliderItem,R.mipmap.ic_launcher)

      /*  val handler = Handler(Looper.getMainLooper())
        try {
            val `in` = java.net.URL(sliderItem).openStream()
            val image = BitmapFactory.decodeStream(`in`)

            // Only for making changes in UI
            handler.post {
                viewHolder.imageViewBackground.setImageBitmap(image)
            }
        }  // If the URL doesnot point to
        // image or any other kind of failure
        catch (e: Exception) {
            e.printStackTrace()
        }*/

        viewHolder.imageViewBackground.setOnClickListener {
            itemClickListener!!.itemclickSlide(position)
        }
    }

    interface ItemClickListener {
        fun itemclickSlide(position: Int)
    }

    // this method will return
    // the count of our list.
    override fun getCount(): Int {
        //return mSliderItems.size
        return mSliderItemsNew.size
    }

    class SliderAdapterViewHolder(itemView: View) : ViewHolder(itemView) {
        // Adapter class for initializing
        // the views of our slider view.

        var imageViewBackground: ImageView

        init {
            imageViewBackground = itemView.findViewById(R.id.myimage)
        }
    }
}