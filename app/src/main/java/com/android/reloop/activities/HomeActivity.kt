package com.reloop.reloop.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.reloop.utils.LogFileSyncTask
import com.android.reloop.utils.LogManager
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarItemView
import com.reloop.reloop.R
import com.reloop.reloop.adapters.ViewPagerAdapter
import com.reloop.reloop.customviews.CustomViewPager
import com.reloop.reloop.fragments.*
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.serializer.shop.Category
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.Utils


class HomeActivity : BaseActivity(), View.OnClickListener, AlertDialogCallback {
    private var actionBar: ActionBar? = null
    var fragments: ArrayList<BaseFragment> = ArrayList()
    private var viewPagerAdapter: ViewPagerAdapter? = null
    var cart: ImageButton? = null
    var viewPager: CustomViewPager? = null

    var TAG: String = "Home Activity"

    companion object {
        lateinit var bottomNav: BottomNavigationView

        //-----------------Update Cart List-------------------
        var countTextView: TextView? = null
        var cartList: ArrayList<Category?>? = ArrayList()
        fun refreshCart() {
            Utils.CartList.saveArrayList(cartList)
            if (cartList?.isEmpty() == true) {
                countTextView?.visibility = View.GONE
            } else {
                countTextView?.visibility = View.VISIBLE
                countTextView?.text = "${cartList?.size}"
            }
        }

        //--------------------Clear Fragment Stack------------------
        private var clearAllFragments = false

        fun clearAllFragments(value: Boolean) {
            clearAllFragments = value
        }

        fun getClearAllFragments(): Boolean {
            return clearAllFragments
        }

        var settingClicked = false
        var openTermCondition = false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val newIntent = Intent()
        setResult(Constants.resultCode, newIntent)
        actionBarSetting()
        initViews()
        setListeners()
        populateData()
        bottomNavigationListeners()
        cartList = Utils.CartList.fetchArrayList()
        refreshCart()
        bottomNav.selectedItemId = R.id.navigation_home
        if (User.retrieveUser()?.login_type!! == Constants.LoginTypes.FACEBOOK
            || User.retrieveUser()?.login_type!! == Constants.LoginTypes.GOOGLE
        ) {
            if (User.retrieveUser()?.first_name.isNullOrEmpty()
                || User.retrieveUser()?.last_name.isNullOrEmpty()
                || User.retrieveUser()?.addresses?.size == 0
            ) {
                bottomNav.selectedItemId = R.id.navigation_settings
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            LogFileSyncTask.getInstance(this@HomeActivity).executeTask()
        }, 7000)



    }

    @SuppressLint("InflateParams")
    private fun actionBarSetting() {
        actionBar = supportActionBar
        actionBar?.setDisplayShowHomeEnabled(false)
        actionBar?.setDisplayShowCustomEnabled(true)
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.elevation = 0f
        val customView: View? = layoutInflater.inflate(R.layout.custom_actionbar_layout, null)
        actionBar?.customView = customView
        val parent = customView?.parent as Toolbar?
        parent?.setPadding(0, 0, 0, 0)
        parent?.setContentInsetsAbsolute(0, 0)
        countTextView = customView?.findViewById(R.id.count)
        cart = customView?.findViewById(R.id.cart)
        cartList?.clear()
        if (cartList?.size == 0) {
            countTextView?.visibility = View.GONE
        }
    }

    private fun initViews() {
        bottomNav = findViewById(R.id.nav_view)
        viewPager = findViewById(R.id.viewPager)
    }


    private fun setListeners() {
        cart?.setOnClickListener(this)
    }

    private fun populateData() {
        bottomNav.itemIconTintList = null
        if (fragments.size == 0) {
            fragments.add(SettingsFragment.newInstance())
            fragments.add(ShopFragment.newInstance())
            fragments.add(HomeFragment.newInstance())
            fragments.add(ReportsFragment.newInstance())
            fragments.add(RewardsFragment.newInstance())

        }
        viewPager?.offscreenPageLimit = 5
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, fragments)
        viewPager?.adapter = viewPagerAdapter
    }

    private fun bottomNavigationListeners() {
        //------------------Bottom Navigation On Click---------------------
        //------------------Bottom Navigation Item Selected---------------------
        val mOnNavigationItemSelectedListener =
            BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.navigation_settings -> {
                        if (viewPager?.currentItem != 0) {
                            viewPager?.currentItem = 0
                        }
                    }
                    R.id.navigation_shop -> {
                        if (viewPager?.currentItem != 1) {
                            viewPager?.currentItem = 1
                        }
                    }
                    R.id.navigation_home -> {
                        if (viewPager?.currentItem != 2) {
                            viewPager?.currentItem = 2
                        }
                    }
                    R.id.navigation_reports -> {
                        if (viewPager?.currentItem != 3) {
                            viewPager?.currentItem = 3
                        }
                    }
                    R.id.navigation_rewards -> {
                        if (viewPager?.currentItem != 4) {
                            viewPager?.currentItem = 4
                        }
                    }
                }
                true
            }
        bottomNav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        //------------------Bottom Navigation Item ReSelected---------------------
        val mOnNavigationItemReSelectedListener =
            BottomNavigationView.OnNavigationItemReselectedListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.navigation_settings -> {
                        clearAllFragmentStack(supportFragmentManager)
                    }
                    R.id.navigation_shop -> {
                        clearAllFragmentStack(supportFragmentManager)
                    }
                    R.id.navigation_home -> {
                        clearAllFragmentStack(supportFragmentManager)
                    }
                    R.id.navigation_reports -> {
                        clearAllFragmentStack(supportFragmentManager)
                    }
                    R.id.navigation_rewards -> {
                        clearAllFragmentStack(supportFragmentManager)
                    }
                }
            }
        bottomNav.setOnNavigationItemReselectedListener(mOnNavigationItemReSelectedListener)
        setBottomNavIconSizes()
    }

    @SuppressLint("RestrictedApi")
    private fun setBottomNavIconSizes() {
        try {
            val menuView = bottomNav.getChildAt(0)as BottomNavigationMenuView
            for (i in 0 until menuView.childCount) {
                if (i == 2) {
                    val displayMetrics: DisplayMetrics = resources.displayMetrics
                    (menuView.getChildAt(i) as NavigationBarItemView).setIconSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, displayMetrics).toInt())
                }
            }
            /*val iconView =
                menuView.getChildAt(2)
            iconView.scaleY = 1.3f
            iconView.scaleX = 1f*/

            /*val menuView = bottomNav.getChildAt(0) as BottomNavigationMenuView
            val iconView =
                menuView.getChildAt(2)
                    .findViewById(com.google.android.material.R.id.icon) as ImageView
            iconView.scaleY = 1.5f
            iconView.scaleX = 1.5f*/

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.cart -> {
                if (BaseFragment.isCartCycleOpened()) {
                    if (bottomNav.selectedItemId != R.id.navigation_shop) {
                        bottomNav.selectedItemId = R.id.navigation_shop
                        return
                    }
                    Notify.alerterRed(this, "Shopping Cart Already Opened")
                } else {
                    if (cartList?.size == 0) {
                        Notify.hyperlinkAlert(this, "Add Items To Cart First", "Go to Reloop Store", this, 1)
                    } else {
                        clearAllFragmentStack(supportFragmentManager)
                        bottomNav.selectedItemId = R.id.navigation_shop

                        replaceFragment(
                            supportFragmentManager,
                            Constants.Containers.shopFragmentContainer,
                            ProductPurchasingFragment.newInstance(),
                            Constants.TAGS.ProductPurchasingFragment
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        /*      if (cartCalledOnMain) {
                  super.onBackPressed()
                  cartCalledOnMain = false
              } else {*/

        if (getClearAllFragments()) {
            clearAllFragmentStack(supportFragmentManager)
            clearAllFragments(false)
        } else {
            val fm = supportFragmentManager
            if (openTermCondition) {
                fm.popBackStackImmediate()
                openTermCondition = false
            } else {
                if (onBackPressed(fm)) {
                    return
                }
                super.onBackPressed();
            }
        }
//    }
    }

    private fun onBackPressed(fm: FragmentManager?): Boolean {
        Log.e(TAG,"==onbackpress fm called===" + fm?.fragments?.size)
        if (fm != null) {
            val fragList = fm.fragments
            if (fragList.size > 0) {
                for (frag in fragList) {
                    if (frag == null) {
                        continue
                    }
                    if (frag.isVisible) {
                        if (onBackPressed(frag.childFragmentManager)) {
                            return true
                        }
                    }
                }
            }

            if (fm.backStackEntryCount > 0) {

                fm.popBackStack()
                return true
            }

        }
        return false
    }

    private fun clearAllFragmentStack(fm: FragmentManager?): Boolean {
        if (fm != null) {
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                return true
            }
            val fragList = fm.fragments
            if (fragList.size > 0) {
                for (frag in fragList) {
                    if (frag == null) {
                        continue
                    }
                    if (frag.isVisible) {
                        if (clearAllFragmentStack(frag.childFragmentManager)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    override fun callDialog(model: Any?) {
        if (model as Int == 1) {
            clearAllFragmentStack(supportFragmentManager)
            bottomNav.selectedItemId = R.id.navigation_shop
        }
    }

    /*  private fun findFragment(fm: FragmentManager, tag: String) {
          val fragList = fm.fragments
          if (fragList.size > 0) {
              for (fragment in fragList) {
                  Log.e(TAG, "No Return =$tag")
                  if (fragment == fm.findFragmentByTag(tag)) {
                      Log.e(TAG, "Found Fragment =$tag")
                  } else {
                      if (fragment.childFragmentManager.backStackEntryCount > 0) {
                          findFragment(
                              fragment.childFragmentManager,
                              tag
                          )
                      }
                  }
              }
          }
      }*/

    override fun onDestroy() {
        /*if(LogManager.isInitialized())
        {
            LogManager.getLogManager().sendLogs(true)
        }*/
        super.onDestroy()
    }


}
