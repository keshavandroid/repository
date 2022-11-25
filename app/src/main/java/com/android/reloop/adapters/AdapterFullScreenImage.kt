package com.android.reloop.adapters

import android.app.Activity
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.reloop.reloop.R
import com.reloop.reloop.utils.Utils
import java.util.*

class AdapterFullScreenImage(
    private val context: Activity,
    private val IMAGES: ArrayList<String>?
) : PagerAdapter() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        if (IMAGES.isNullOrEmpty()) {
            return 0
        }
        return IMAGES?.size!!
    }

    override fun instantiateItem(view: ViewGroup, position: Int): Any {
        val imageLayout =
            inflater.inflate(R.layout.row_zoom_image_view, view, false)!!
        val imageView = imageLayout
            .findViewById<View>(R.id.image) as ImageView
        Utils.glideImageLoaderServer(
            imageView,
            IMAGES?.get(position),
            R.drawable.icon_placeholder_generic
        )
//        imageView.setImageResource(IMAGES[position])
        view.addView(imageLayout, 0)
        return imageLayout
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean {
        return view == `object`
    }

    override fun restoreState(
        state: Parcelable?,
        loader: ClassLoader?
    ) {
    }

    override fun saveState(): Parcelable? {
        return null
    }

}