package com.reloop.reloop.network

import com.android.reloop.network.serializer.reports.PieChartParams
import com.android.reloop.network.serializer.reports.PieChartParamsSpecific
import com.android.reloop.network.serializer.reports.PieChartParamsSpecificNew
import com.android.reloop.network.serializer.reports.ReportsParams
import com.android.reloop.network.serializer.reports.ReportsParamsNew
import com.reloop.reloop.network.serializer.Login
import com.reloop.reloop.network.serializer.SignUp
import com.reloop.reloop.network.serializer.shop.AddCard
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface ApiServices {
    @POST("login")
    fun login(@Body body: Login?): Call<Any>?

    @POST("register")
    fun register(@Body body: SignUp?): Call<Any>?

    @GET("dependencies")
    fun dependencies(): Call<Any>?

    @GET("user-profile-dependencies")
    fun userProfiledependencies(): Call<Any>?

    @GET("drop-off-dependencies")
    fun citiesAndMaterials(): Call<Any>?


    @Multipart
    @POST("drop-off-request")
    fun dropOffRequest(
        @Part imageMap: Array<MultipartBody.Part?>,
        @PartMap partMap: HashMap<String?, RequestBody?>?
    ): Call<Any>?


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

    @GET("collection-request/{request_id}")
    fun editOrderDetail(@Path("request_id") request_id: Int?): Call<Any>?

    @POST("buy-product")
    fun buyProduct(@Body body: Any?): Call<Any>?

    @POST("buy-new-product")
    fun buyProductNew(@Body body: Any): Call<Any>?

    @POST("buy-new-product")
    fun buyProductNew(@Body body: Map<String, String>): Call<Any>?

    @FormUrlEncoded
    @POST("buy-new-product")
    fun buyProductNew(@FieldMap hashMap: HashMap<String?, String?>): Call<Any>?

    @POST("buy-new-service")
    fun buyService(@Body body: Any?): Call<Any>?


    @POST("monthly-service-payment-intend")
    fun getMonthlyServicePaymentIntent(
        @Query("subscription_id") subscription_id: Int?,
        @Query("coupon_id") coupon_id: Int?): Call<Any>?

    @POST("add-payment-method")
    fun addNewCard(@Body body: AddCard?): Call<Any>?

    @POST("add-payment-method")
    fun addNewPM(@Query("payment_method") payment_method: String?): Call<Any>?

    @POST("coupon-verification")
    fun couponVerification(@Body body: Any?): Call<Any>?

    @POST("change-password")
    fun changePassword(@Body body: Any?): Call<Any>?

    @GET("user-profile")
    fun getUserProfile(): Call<Any>?


    @POST("update-address")
    fun updateAddress(@Body body: Any?): Call<Any>?

    @POST("update-payment-intend")
    fun updatepaymentIntend(@Body body: Any?): Call<Any>?

    @Multipart
    @POST("update-user-profile")
    fun updateUserProfile(
        @Part file: MultipartBody.Part?,
        @PartMap body: HashMap<String?, RequestBody?>?
    ): Call<Any>?



    //FOR NEW COLLECTION REQUEST
    @GET("material-categories")
    fun getMaterialCategories(): Call<Any>?

    //FOR EDIT COLLECTION REQUEST
    @GET("material-categories/{request_id}")
    fun getMaterialCategories(@Path("request_id") request_id: Int?): Call<Any>?


    @GET("get-plan")
    fun getPlan(): Call<Any>?

    @GET("get-plan-status")
    fun getPlanStatus(): Call<Any>?


    //original without image NORMAL
    @POST("collection-request")
    fun collectionRequest(@Body body: Any?, @Query("map_location") mapLocation: String?): Call<Any>?

    //new SINGLE IMAGE
    @Multipart
    @POST("collection-request")
    fun collectionRequest(
        @PartMap partMap: HashMap<String?, RequestBody?>?,
        @Part image: MultipartBody.Part?
    ): Call<Any>?

    //new MULTIPLE IMAGE
    @Multipart
    @POST("collection-request")
    fun collectionRequest(
        @PartMap partMap: HashMap<String?, RequestBody?>?,
        @Part images: List<MultipartBody.Part>?
    ): Call<Any>?



    //original without image EDIT
    @POST("collection-request/{request_id}")
    fun editCollectionRequest(@Path("request_id") request_id: Int? , @Body body: Any?, @Query("map_location") mapLocation: String?): Call<Any>?

    //new with image EDIT SINGLE IMAGE
    @Multipart
    @POST("collection-request/{request_id}")
    fun editCollectionRequest(
        @Path("request_id") request_id: Int?,
        @PartMap partMap: HashMap<String?, RequestBody?>?,
        @Part image: MultipartBody.Part?
    ): Call<Any>?

    //new with image EDIT MULTIPLE IMAGES
    @Multipart
    @POST("collection-request/{request_id}")
    fun editCollectionRequest(
        @Path("request_id") request_id: Int?,
        @PartMap partMap: HashMap<String?, RequestBody?>?,
        @Part images: List<MultipartBody.Part>?
    ): Call<Any>?




    @GET("privacy-policy")
    fun privacyPolicy(): Call<Any>?

    @GET("terms-and-conditions")
    fun termsAndConditions(): Call<Any>?

    @GET("about-us")
    fun aboutUs(): Call<Any>?

    @GET("orders-listing")
    fun ordersListing(): Call<Any>?

    //WITH FILTER
    @GET("orders-listing")
    fun ordersListingFilter(@Query("filter_option") filter_option: Int?,@Query("specific_org_id") specific_org_id: Int?): Call<Any>?

    //WITH FILTER
    @GET("orders-listing")
    fun ordersListingFilterAddress(@Query("filter_option") filter_option: Int?,@Query("address_id") address_id: Int?): Call<Any>?


    @GET("request-collection-details/{request_id}")
    fun collectionBinDetails(@Path("request_id") request_id: String?): Call<Any>?


    @GET("user-subscriptions")
    fun userSubscriptions(): Call<Any>?

    @GET("billing-listing")
    fun billingListing(): Call<Any>?


    /*New image parameter added for all contact-us apis*/

    @Multipart
    @POST("contact-us")
    fun contactUs(
        @Part("email") email: RequestBody?, @Part("subject") subject: RequestBody?,
        @Part("message") message: RequestBody?, @Part image: MultipartBody.Part?
    ): Call<Any>?

    @Multipart
    @POST("contact-us")
    fun contactUs(
        @Part("email") email: RequestBody?, @Part("subject") subject: RequestBody?,
        @Part("message") message: RequestBody?, @Part("order_id") order_id: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<Any>?

    @Multipart
    @POST("contact-us")
    fun contactUsCollection(
        @Part("email") email: RequestBody?, @Part("subject") subject: RequestBody?,
        @Part("message") message: RequestBody?, @Part("collection_id") order_id: RequestBody?,
        @Part image: MultipartBody.Part?
    ): Call<Any>?



    @POST("contact-us")
    fun contactUsNoImage(
        @Query("email") email: String?, @Query("subject") subject: String?,
        @Query("message") message: String?
    ): Call<Any>?

    @POST("contact-us")
    fun contactUsNoImage(
        @Query("email") email: String?, @Query("subject") subject: String?,
        @Query("message") message: String?, @Query("order_id") order_id: String?
    ): Call<Any>?

    @POST("contact-us")
    fun contactUsCollectionNoImage(
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

    @POST("get-barChart-data")
    fun getBarChartDataNew(@Body body: ReportsParamsNew?): Call<Any>?

    //OLD
    /*@POST("get-pieChart-data")
    fun getPieChartData(@Body body: PieChartParams?): Call<Any>?*/

    //NEW
    @POST("get-pieChart-data")
    fun getPieChartData(@Body body: PieChartParams?): Call<Any>?

    @POST("get-pieChart-data")
    fun getPieChartDataSpecific(@Body body: PieChartParamsSpecific?): Call<Any>?

    @POST("get-pieChart-data")
    fun getPieChartDataSpecificNew(@Body body: PieChartParamsSpecificNew?): Call<Any>?

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

    //old api
    /*@POST("cancel-subscription")
    fun unSubscribe(@Query("user_subscriptionId") userSubscriptionId: String?): Call<Any>?*/

    //new api
    @GET("cancel-subscription/{id}")
    fun unSubscribe(@Path("id") id: String?): Call<Any>?

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


    /*@FormUrlEncoded
    @POST("drop-off-points")
    fun getDropOffPins(@FieldMap body: HashMap<String?, String?>): Call<Any>?*/

    @Multipart
    @POST("drop-off-points")
    fun getDropOffPins(@PartMap partMap: HashMap<String?, RequestBody?>?,
                       @Part part: Array<MultipartBody.Part?>): Call<Any>?


    @POST("drop-off-points")
    fun getDropOffPins(): Call<Any>?

    @GET("payment-methods")
    fun paymentMethodListing(): Call<Any>?

    @GET("user-cards")
    fun getSavedCards(): Call<Any>?

    @DELETE("delete-card/{payment_method_id}")
    fun deleteCard(@Path("payment_method_id") pmID: String?): Call<Any>?


    @POST("favourite-drop-off/{drop-off-id}")
    fun addToFavDropOff(@Path("drop-off-id") dropOffId: Int?): Call<Any>?

    @DELETE("favourite-drop-off/{drop-off-id}")
    fun deleteFavDropOff(@Path("drop-off-id") dropOffId: Int?): Call<Any>?

    @GET("favourite-drop-offs")
    fun getFavDropOff(): Call<Any>?

    @GET("exit-campaign/{id}")
    fun exitCampaign(@Path("id") id: String): Call<Any>?

    @GET("join-campaign/{id}")
    fun joinCampaign(@Path("id") id: String): Call<Any>?

    @DELETE("delete-payment-method/{id}")
    fun deletePaymentMethod(@Path("id") id: String): Call<Any>?


    @POST("update-payment-method")
    fun updatePaymentMethod(@Query("card_id") cardId: String?): Call<Any>?

    @POST("update-payment-method")
    fun updatePaymentMethodStripe(@Query("payment_method") pmID: String?): Call<Any>?

    @DELETE("delete-card/{id}")
    fun deletePM(@Path("id") id: String): Call<Any>?

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

    //GOOGLE PAY SECRET KEY
    @POST("payment-intend")
    fun getPaymentIntentGooglePay(@Query("amount") amount: String?): Call<Any>?

    //STRIPE PAY PAYMENT INTENT
    @POST("card-payment-intend")
    fun getPaymentIntentStripePay(@Query("amount") amount: String?): Call<Any>?

    @GET("cart")
    fun getCart(): Call<Any>?

}