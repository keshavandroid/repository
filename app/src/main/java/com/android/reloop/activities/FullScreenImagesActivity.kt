package com.android.reloop.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.android.reloop.adapters.FullScreenPagerAdapter
import com.android.reloop.customviews.InfiniteCirclePageIndicator
import com.android.reloop.customviews.InfinitePagerAdapter
import com.android.reloop.customviews.InfiniteViewPager
import com.bumptech.glide.Glide
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication

class FullScreenImagesActivity : AppCompatActivity() {

    // Collection images viewpager
    private var viewpagerFullScreen: InfiniteViewPager? = null
    private var adapterCollection: InfinitePagerAdapter? = null
    private var dotsCollection: InfiniteCirclePageIndicator? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApplication.hideActionBar(supportActionBar)

        setContentView(R.layout.activity_full_screen_image)

        viewpagerFullScreen = findViewById(R.id.viewpagerFullScreen)
        dotsCollection = findViewById(R.id.dotsCollection)

        val imgClose: ImageView = findViewById(R.id.imgClose)
        imgClose.setOnClickListener { finish() }

        val intent = intent
        val imageUrls = intent.getStringArrayListExtra("IMAGE_URLS")
        val position = intent.getIntExtra("POSITION", 0)

        setInfiniteViewpagerCollection(imageUrls, position)

    }

    private fun setInfiniteViewpagerCollection(list: ArrayList<String>?, position: Int) {
        Log.e("TAG", "==== CollectionImages SIZE===" + (list?.size ?: "null"))

        viewpagerFullScreen?.offscreenPageLimit = 3

        val myCustomPagerAdapter = FullScreenPagerAdapter(this, list!!, 1)

        adapterCollection = InfinitePagerAdapter(myCustomPagerAdapter)
        adapterCollection?.setOneItemMode()

        viewpagerFullScreen?.apply {
            adapter = adapterCollection
            setCurrentItem(position, false)
        }

        dotsCollection?.apply {
            setSnap(false)
            setViewPager(viewpagerFullScreen)

            // Hide dots when there is one image
            visibility = if (list.size == 1) View.GONE else View.VISIBLE
        }
    }

}