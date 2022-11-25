package com.reloop.reloop.network

import android.util.Log
import com.reloop.reloop.network.serializer.user.User
import com.android.reloop.utils.Configuration
import com.reloop.reloop.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class Network() {
    private var networkClient: Retrofit? = null
    private var services: ApiServices? = null

    init {
        val gson = GsonBuilder().create()
        val httpClient = OkHttpClient.Builder()
        httpClient.retryOnConnectionFailure(true)
        httpClient.connectTimeout(2, TimeUnit.MINUTES)
        httpClient.readTimeout(2, TimeUnit.MINUTES)
        httpClient.writeTimeout(2, TimeUnit.MINUTES)
        httpClient.addInterceptor { chain: Interceptor.Chain ->
            if (User.retrieveUser() != null && User.retrieveUser()?.api_token?.isNotEmpty()!!) {
                val original = chain.request()
                val request = original.newBuilder()
                    .header("Authorization", "Bearer ${User.retrieveUser()?.api_token}")
                    //.header("Authorization", "Bearer KNjGIN6MhBXuD0EUWK75oN2Kag7otBdCPvW0JqjDzfzQsl3atU1661773430")

                    .method(original.method, original.body).build()
                return@addInterceptor chain.proceed(request)
            } else {
                val request = chain.request().newBuilder().build()
                return@addInterceptor chain.proceed(request)
            }
        }
        httpClient.addInterceptor(getLoggingIntercept())
        networkClient = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build())
            .build()
        services = networkClient!!.create(ApiServices::class.java)
    }

    private fun getLoggingIntercept(): Interceptor {
        val httpLoggingInterceptor =
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
//                    if (!Configuration.isProduction)
                    longLog(message)
                }
            })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    fun apis(): ApiServices? {
        return getApiServices()
    }

    fun getApiServices(): ApiServices? {
        return services
    }

    fun getNetworkClient(): Retrofit? {

        return networkClient
    }

    fun longLog(str: String) {
        if (str.length > 4000) {
            Log.e("NetWork Call", str.substring(0, 4000))
//            longLog(str.substring(4000))
        } else Log.e("NetWork Call", str)
    }
}