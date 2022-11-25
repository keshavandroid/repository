package com.reloop.reloop.network

/**
 * Created by devandro on 11/20/17.
 */
object ResponseCode {
    var SUCCESS = 200
    var INTERNAL_SERVER_ERROR = 500
    var FORBIDDEN = 403
    var UN_AUTHORIZED = 401
    var NOT_FOUND = 404
    var ERROR = 422
    var APP_NOT_UPDATED = 304
    var BAD_REQUEST = 400

    fun isBetweenSuccessRange(reqCode: Int): Boolean {
        return try {
            reqCode in 200..299
        } catch (e: Exception) {
            true
        }
    }
}