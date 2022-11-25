package com.reloop.reloop.network

import com.android.reloop.network.serializer.reports.ReportsParams
import com.reloop.reloop.network.serializer.Login
import com.reloop.reloop.network.serializer.SignUp
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface ApiServices {
    @POST("login")
    fun login(@Body body: Login?): Call<Any>?

    @POST("register")
    fun register(@Body body: SignUp?): Call<Any>?

    @GET("dependencies")
    fun dependencies(): Call<Any>?

    @GET("settings")
    fun continueAsSettings(): Call<Any>?

    @GET("categories")
    fun categories(): Call<Any>?

    @POST("forgot-password")
    fun forgot(@Query("email") email: String?): Call<Any>?

    @GET("category/products")
    fun products(@Query("category_id") id: Int?, @Query("category_type") type: Int?): Call<Any>?

    @POST("buy-plan")
    fun buyPlan(@QueryMap map: HashMap<String, Any?>): Call<Any>?

    @POST("buy-product")
    fun buyProduct(@Body body: Any?): Call<Any>?

    @POST("coupon-verification")
    fun couponVerification(@Body body: Any?): Call<Any>?

    @POST("change-password")
    fun changePassword(@Body body: Any?): Call<Any>?

    @GET("user-profile")
    fun getUserProfile(): Call<Any>?


    @POST("update-address")
    fun updateAddress(@Body body: Any?): Call<Any>?

    @Multipart
    @POST("update-user-profile")
    fun updateUserProfile(
        @Part file: MultipartBody.Part?,
        @PartMap body: HashMap<String?, RequestBody?>?
    ): Call<Any>?

    @GET("material-categories")
    fun getMaterialCategories(): Call<Any>?

    @GET("get-plan")
    fun getPlan(): Call<Any>?

    @POST("collection-request")
    fun collectionRequest(@Body body: Any?, @Query("map_location") mapLocation: String?): Call<Any>?

    @GET("privacy-policy")
    fun privacyPolicy(): Call<Any>?

    @GET("terms-and-conditions")
    fun termsAndConditions(): Call<Any>?

    @GET("about-us")
    fun aboutUs(): Call<Any>?

    @GET("orders-listing")
    fun ordersListing(): Call<Any>?

    @GET("user-subscriptions")
    fun userSubscriptions(): Call<Any>?

    @GET("billing-listing")
    fun billingListing(): Call<Any>?

    @POST("contact-us")
    fun contactUs(
        @Query("email") email: String?, @Query("subject") subject: String?,
        @Query("message") message: String?
    ): Call<Any>?

    @POST("contact-us")
    fun contactUs(
        @Query("email") email: String?, @Query("subject") subject: String?,
        @Query("message") message: String?, @Query("order_id") order_id: String?
    ): Call<Any>?

    @POST("contact-us")
    fun contactUsCollection(
        @Query("email") email: String?, @Query("subject") subject: String?,
        @Query("message") message: String?, @Query("collection_id") order_id: String?
    ): Call<Any>?


    @POST("delete-address")
    fun deleteAddress(@Query("address_id") id: Int?): Call<Any>?

    @GET("donation-categories")
    fun donationCategories(): Call<Any>?

    @POST("donation-products")
    fun donationProducts(@Query("category_id") id: Int?): Call<Any>?

    @POST("donations")
    fun donations(@Query("product_id") id: Int?): Call<Any>?

    @POST("redeem-points")
    fun redeemPoints(@Query("redeem_points") id: Int?): Call<Any>?

    @GET("dashboard")
    fun dashboard(): Call<Any>?

    @POST("reset-password")
    fun resetPassword(
        @Query("reset_token") reset_token: String?,
        @Query("new_password") new_password: String?,
        @Query("new_password_confirmation") new_password_confirmation: String?
    ): Call<Any>?

    /*@POST("get-barChart-data")
    fun getBarChartData(
        @Query("filter") filter: Any?, @Query("date") date: Any?
        , @Query("filter_option") filter_option: Any?, @Query("address_id") address_id: Any?
    ): Call<Any>?*/

    @POST("get-barChart-data")
    fun getBarChartData(@Body body: ReportsParams?): Call<Any>?


    @POST("organization-verification")
    fun organizationVerification(@Query("org_external_id") org_external_id: String?): Call<Any>?

    @POST("order-acceptance-days")
    fun orderAcceptanceDays(@Query("district_id") district_id: Int?): Call<Any>?

    @GET("rewards-history")
    fun rewardsHistory(): Call<Any>?

    @POST("logout")
    fun logout(@Query("player_id") player_id: String?): Call<Any>?

    @POST("cancel-order")
    fun cancel_order(
        @Query("order_id") order_id: Any?,
        @Query("order_type") order_type: Any?
    ): Call<Any>?

    @POST("drivers-availability")
    fun driversAvailability(@Query("collection_date") collection_date: String?): Call<Any>?

    @POST("cancel-subscription")
    fun unSubscribe(@Query("user_subscriptionId") userSubscriptionId: String?): Call<Any>?

    @GET("maximum-categories")
    fun getMaximumCategories(): Call<Any>?

    @GET("remove-organization")
    fun removeOrganization(): Call<Any>?

    @GET("delivery-fee")
    fun getDeliveryFee(): Call<Any>?

    @GET("campaigns")
    fun getCampaigns(): Call<Any>?

    @GET("campaign/{id}")
    fun getCampaignDetails(@Path("id") id: String): Call<Any>?

    @GET("news/{news_id}")
    fun getNewsDetails(@Path("news_id") id: String): Call<Any>?

    @GET("subscriber-discount")
    fun getSubscriberOrder(): Call<Any>?

    @Multipart
    @POST("get-common-days")
    fun getMaterialCategoriesCommanDays(@Part part: Array<MultipartBody.Part?>): Call<Any>?


    @GET("user-campaigns")
    fun joinedCampaignListing(): Call<Any>?

    @GET("exit-campaign/{id}")
    fun exitCampaign(@Path("id") id: String): Call<Any>?

    @GET("join-campaign/{id}")
    fun joinCampaign(@Path("id") id: String): Call<Any>?
    // https://staging.reloopapp.com/api/barcodes
    //https://staging.reloopapp.com/api/product-barcode/%7Bbarcode%7D

    @GET("product-barcode/{barcode}")
    fun fetchScannedBarCodeDetails(@Path("barcode") barcode: String): Call<Any>?

    @POST("app-activity-logs")
    fun pushLogFile(
        @Body body: RequestBody
    ): Call<Any>?

    @GET("delete-account")
    fun deleteaccount(): Call<Any>?

   /* @GET("https://maps.googleapis.com/maps/api/geocode/json")
    fun findAddress(@Path("latlng") ll: String,@Path("sensor") sensor: Boolean,@Path("key") key: String): Call<Any>?*/

    @GET
    fun findAddress(@Url url: String?): Call<Any>?
}