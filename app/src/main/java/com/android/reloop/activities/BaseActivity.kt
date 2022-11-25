package com.reloop.reloop.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.reloop.reloop.app.MainApplication


@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApplication.setCurrentActivity(this)
    }

    companion object {
        fun replaceFragment(
            fragmentManager: FragmentManager,
            container: Int,
            fragment: Fragment,
            TAG: String
        ) {
            try {
                /* val manager =
                     (MainApplication.getCurrentActivity() as FragmentActivity).supportFragmentManager
                 manager.beginTransaction()
                     .replace(container, fragment, TAG).addToBackStack(TAG).commit()*/
                fragmentManager.beginTransaction()
                    .replace(container, fragment, TAG)
                    .addToBackStack(TAG)
                    .commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun stepViewUpdateUI(imageView: ImageView?, img: Int, textView: TextView?, color: Int) {
            imageView?.setImageResource(img)
            textView?.setTextColor(color)
        }

        fun replaceFragmentBackStackNull(
            fragmentManager: FragmentManager,
            container: Int,
            fragment: Fragment
        ) {
            try {
                val transaction = fragmentManager.beginTransaction()
                transaction.replace(container, fragment)
                transaction.commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
