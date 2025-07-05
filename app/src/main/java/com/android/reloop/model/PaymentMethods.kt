package com.android.reloop.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class PaymentMethods : Serializable {

    @SerializedName("id")
    @Expose
    private var id: Int? = null

    @SerializedName("user_id")
    @Expose
    private var userId: Int? = null

    @SerializedName("holder_name")
    @Expose
    private var holderName: Any? = null

    @SerializedName("brand")
    @Expose
    private var brand: String? = null

    @SerializedName("number")
    @Expose
    private var number: String? = null

    @SerializedName("expiry_date")
    @Expose
    private var expiryDate: String? = null

    @SerializedName("is_default")
    @Expose
    private var isDefault: Int? = null

    fun getId(): Int? {
        return id
    }

    fun setId(id: Int?) {
        this.id = id
    }

    fun getUserId(): Int? {
        return userId
    }

    fun setUserId(userId: Int?) {
        this.userId = userId
    }

    fun getHolderName(): Any? {
        return holderName
    }

    fun setHolderName(holderName: Any?) {
        this.holderName = holderName
    }

    fun getBrand(): String? {
        return brand
    }

    fun setBrand(brand: String?) {
        this.brand = brand
    }

    fun getNumber(): String? {
        return number
    }

    fun setNumber(number: String?) {
        this.number = number
    }

    fun getExpiryDate(): String? {
        return expiryDate
    }

    fun setExpiryDate(expiryDate: String?) {
        this.expiryDate = expiryDate
    }

    fun getIsDefault(): Int? {
        return isDefault
    }

    fun setIsDefault(isDefault: Int?) {
        this.isDefault = isDefault
    }

}