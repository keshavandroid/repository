package com.reloop.reloop.network.serializer

import android.util.Log
import com.reloop.reloop.network.ResponseCode
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import java.io.Serializable

class BaseResponse() : Serializable {

    @SerializedName("message")
    var message: String? = null
    @SerializedName("code")
    var code: Int? = null
    @SerializedName("status")
    var status: Boolean? = null
    @SerializedName("data")
    var data: Any? = null
    @SerializedName("token")
    var token: Any? = null

    constructor(message: String?, code: Int?) : this() {
        this.message = message
        this.code = code
    }

    constructor(message: String?, code: Int?, status: Boolean?) : this() {
        this.message = message
        this.code = code
        this.status = status
    }

    constructor(message: String?) : this() {
        this.message = message
    }

    companion object {
        fun isSuccess(response: Response<*>): Boolean {
            var check = false
            try { //            return ResponseCode.isBetweenSuccessRange(Integer.parseInt(((BaseResponse) response.body()).getCode()));
                if (response.code() == ResponseCode.SUCCESS) {
                    check = true
                }
            } catch (e: Exception) {
                check = false
                Log.e("Base Response check=$check", "Exception$e")
            }
            return check
        }

        fun isUnAuthorized(response: Response<*>): Boolean {
            return try {
                (response.code() == ResponseCode.UN_AUTHORIZED || response.code() == ResponseCode.NOT_FOUND ||
                        response.code() == ResponseCode.INTERNAL_SERVER_ERROR ||
                        response.code() == ResponseCode.BAD_REQUEST)
            } catch (e: java.lang.Exception) {
                false
            }
        }

        fun isForbiddenResponse(response: Response<*>): Boolean {
            return try {
                (response.code() == ResponseCode.FORBIDDEN)
            } catch (e: java.lang.Exception) {
                false
            }
        }
    }
}