package com.reloop.reloop.utils

import android.content.SharedPreferences
import com.android.reloop.fragments.PaymentMethodsFragment
import com.android.reloop.utils.Configuration.baseUrl
import com.android.reloop.utils.Configuration.pkStripe
import com.android.reloop.utils.Configuration.skStripe
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.fragments.CollectionBinsFragment


object Constants {
    var PRIVATE_MODE = 0
    val PREF_NAME = "reloop"
    var BASE_URL = baseUrl[0]

    var PK_STRIPE = pkStripe[0]
    var SK_STRIPE = skStripe[0]



    var BasePathForImage = baseUrl[1]
    var BasePathForDeepLink = baseUrl[2]
    var token: String? = "Token"
    var resultCode = 101
    var mapsCode = 102
    var accessLocation = 201
    var recycleStep1 = 1
    var recycleStep2 = 2
    var recycleStep3 = 3
    var recycleStep4 = 4
    var SaveCartList = "Save Cart List"
    var agreementCheck = "Agreement Check"

    var monthlySubscription = 0
    var addedService = 1
    var recyclingBoxes = 2
    var environmentalProducts = 3

    var subscriptionCycleOne = 1
    var subscriptionCycleTwo = 2

    var userPartOfOrganization = 1
    var userNotPartOfOrganization = 0
    var deleteOrganizationId = 3
    var serviceType = 1
    var productType = 2
    var all = 3
    var currencySign = "AED"

    var orgname = ""

    var thresholdTime = "00:01-21:00"

    object RecyclerViewSpan {
        var singleRow = 1
        var twoColumns = 2

    }



    object Containers {
        //-------Parent Container-----------
        var homeFragmentContainer = R.id.container_home
        var shopFragmentContainer = R.id.container_shop_fragment
        var settingsFragmentContainer = R.id.settings_fragment_container
        var containerSubscriptionCycleParent = R.id.container_subscription_cycle_parent
        var homeActivityContainer = R.id.home_activity_container
        var recycleRequestParent = R.id.recycle_request_parent
        var campaignListContainer = R.id.container_campainList
        var reportsFragmentContainer = R.id.reports_fragment_container


        var paymentMethodListContainer = R.id.container_paymentMethodList


        //drop-off screens
        var dropOffPinContainer = R.id.container_drop_off_pin
        var materialSelectFragment = R.id.container_material_select
        var verifyDropOffContainer= R.id.container_verify_drop_off
        var confirmSelectionsContainer = R.id.container_confirm_selections
        var requestSuccessContainer = R.id.container_req_success



        //---------------Child Container-----------
        var recycleFragmentContainer = R.id.container_recycle
        var containerSubscriptionCycle = R.id.container_subscription_cycle
        var containerMonthlySubscriptionFragment = R.id.container_monthly_subscription_fragment
        var containerOrderHistory = R.id.container_order_history_fragment
        var containerRewardHistory = R.id.container_reward_history_fragment

        var containerNewBilling = R.id.container_new_billing


        var containerBillingFragment = R.id.container_billing_fragment
        var containerEditProfile = R.id.container_edit_profile
        var containerForgotPassword = R.id.container_forgot_password
        var confirmCollection = R.id.confirm_collection
        var ViewReceiptContainer = R.id.view_receipt_container
        var containerCampainDetail = R.id.container_campain
        var containerNewsDetail = R.id.ContainerNews
        var productDetailFragmentContainer = R.id.container_productDetail
        var containercartInformation = R.id.containerCartInformation
        var subscrptionFragmentContainer = R.id.container_subscriptions
        var collectionBinsFragment = R.id.container_collection_bin
    }

    object DataConstants {
        var trackOrder = "Track Order"
        var shopFragmentHeading = "shopFragmentData"
        var shopFragmentIcon = "shopFragmentIcon"
        var position = "position"
        var subscriptionCycle = "subscriptionCycle"
        var location = "location"
        var user = "user"
        var userStatus = "userStatus"
        var purchaseID = "purchaseID"
        var longitude = "longitude"
        var latitude = "latitude"
        var rewardPoints = "rewardPoints"
        var isGuest = "isGuest"

        object Api {
            var productID = "productID"
            var productType = "productType"
            var planID = "planID"
            var planPrice = "planPrice"
            var serviceType = "serviceType"

            //new added
            var planImage = "planImage"
            var planTripValue = "planTripValue"
            var planDescription = "planDescription"
            var planIcon = "planIcon"


        }

        var bundle = "bundle"
        var removeSaveButton = "removeSaveButton"
    }

    object UserType {
        var household = 1
        var organization = 2
    }

    object TAGS {
        var AboutReloopFragment = "About Reloop Fragment"
        var AddAddressFragment = "Add Address Fragment"
        var AddressInformationFragment = "Address Information Fragment"
        var BillingFragment = "Billing Fragment"
        var BillingInformationFragment = "Billing Information Fragment"
        var NewBillingInformationFragment = "New Billing Information Fragment"

        var ChangePasswordFragment = "Change Password Fragment"
        var ConfirmationSubscriptionFragment = "Confirm Subscription Fragment"
        var ConfirmCollectionFragment = "Confirm Collection Fragment"
        var ContactUsFragment = "Contact Us Fragment"
        var EditProfileFragment = "Edit Profile Fragment"

        var OrderHistoryFragment = "Order History Fragment"
        var ContinueAsFragmentTag = "ContinueAsFragmentTag"
        var RecycleFragment = "Recycle Fragment"
        var RequestSubmitConfirmationFragment = "Request Submit Confirmation Fragment"

        var SelectionDayFragment = "Selection Day Fragment"

        var ProductPurchasingFragment = "Product Purchasing Fragment"
        var ShopDetailFragment = "Shop Detail Fragment"
        var SubscriptionFragment = "Subscription Fragment"
        var TermConditionFragment = "Term Condition Fragment"
        var ViewReceiptFragment = "View Receipt Fragment"
        var ServicePurchasingFragment = "Service Purchasing Fragment"

        var SettingsFragment = "Settings Fragment"
        var HomeFragment = "Home Fragment"
        var MapsFragment = "Maps Fragment"
        var RewardsFragment = "Rewards Fragment"
        var ShopFragment = "Shop Fragment"
        var ReportsFragment = "Reports Fragment"

        var SelectCategoriesFragment = "Select Categories Fragment"
        var CartInformationFragment = "Cart Information Fragment"

        var RewardsHistoryFragment = "Rewards History Fragment"
        var CampainDetailFragment = "Campain Detail Fragment"
        var ProductDetailFragment = "Product Detail Fragment"
        var NewsDetailsFragment = "News Detail Fragment"
        var CampaignsListFragment = "Campaigns List Fragment"

        var PaymentMethodsFragment = "Payment Method List Fragment"

        var NewPaymentMethodsFragment = "New Payment Method List Fragment"

        var PdfDownloadFragment = "Pdf Download Fragment"


        //drop off screens
        var DropOffPinFragment = "Drop Off Pin Fragment"
        var MaterialSelectFragment = "Material Select Fragment"
        var VerifyDropOffFragment = "Verify Drop Off Fragment"
        var ConfirmSelectionsFragment = "Confirm Selections Fragment"
        var RequestSuccessFragment = "Request Success Fragment"
        var CollectionBinsFragment = "Collection Bins Fragment"


    }

    object RecycleCategoryType {
        var SAME_DAY = 1;
        var NEXT_DAY = 2;
        var SINGLE_COLLECTION = 3;
        var BULKY_ITEM = 4;
        var NORMAL = 5;
        var FREE_SERVICE = 6

    }


    object RememberMe {
        const val PREFS_NAME = "RememberMe"
        const val PREFS_USER_EMAIL = "prefsUserEmail"
        const val PREFS_PASS = "prefsPassword"
        val sharedPref: SharedPreferences? =
            MainApplication.applicationContext().getSharedPreferences(
                PREFS_NAME,
                PRIVATE_MODE
            )
        val prefsEditor: SharedPreferences.Editor? = sharedPref?.edit()

        fun save(email: String?, password: String?) {
            prefsEditor?.putString(PREFS_USER_EMAIL, email)
                ?.putString(PREFS_PASS, password)
                ?.apply()
        }

        fun getEmail(): String? {
            val email: String? = sharedPref?.getString(PREFS_USER_EMAIL, "")
            return email
        }

        fun getPassword(): String? {
            val password: String? = sharedPref?.getString(PREFS_PASS, "")
            return password
        }

        fun clear() {
            prefsEditor?.clear()
            prefsEditor?.apply()
        }
        fun getLogFileName(): String {
            var fileName: String = sharedPref!!.getString("log_file_name", "") ?:""
            if (fileName.isEmpty()) {
                fileName = "log_manager_" + System.currentTimeMillis() + ".txt"
                setLogFileName(fileName)
            }
            return fileName
        }

        fun setLogFileName(fileName: String?) {
            val editor: SharedPreferences.Editor = sharedPref!!.edit()
            editor.putString("log_file_name", fileName)
            editor.apply()
            editor.commit()
        }
    }

    object DiscountType {
        var FIXED = 1;
        var PERCENTAGE = 2;
    }

    object LoginTypes {
        const val APP_LOGIN = 1
        const val FACEBOOK = 2
        const val GOOGLE = 3
    }

    object ReportsFilter {
        const val Daily = 1
        const val Week = 2
        const val Month = 3
        const val Year = 4
    }


    fun getEmployeesNumberSelectList(): ArrayList<String> {
        val employeesList: ArrayList<String> = ArrayList()
        employeesList.clear()
        employeesList.add(MainApplication.applicationContext().getString(R.string.no_of_employees))
        employeesList.add("1-20")
        employeesList.add("21-50")
        employeesList.add("51-200")
        employeesList.add("201-500")
        employeesList.add("501+")
        return employeesList
    }

    fun getBranchesNumberListSelectList(): ArrayList<String> {
        val branchesList: ArrayList<String> = ArrayList()
        branchesList.clear()
        branchesList.add(MainApplication.applicationContext().getString(R.string.no_of_branches))
        branchesList.add("1")
        branchesList.add("2-10")
        branchesList.add("10+")
        return branchesList
    }

    fun getContactUsLabels(): ArrayList<String> {
        val employeesList: ArrayList<String> = ArrayList()
        employeesList.clear()
        employeesList.add("Feedback")
        employeesList.add("Complaint")
        employeesList.add("New Ideas")
        employeesList.add("Refund")
        employeesList.add("Return")
        employeesList.add("Others")
        return employeesList
    }

    object FilterOption {
        const val HOUSEHOLD = 1
        const val ORGANIZATION = 2
        const val ALL = 3
        const val ADDRESS = 4
        const val CONNECTED_ORGS = 5
        const val TOTAL_BRANCHES = 6


    }

}