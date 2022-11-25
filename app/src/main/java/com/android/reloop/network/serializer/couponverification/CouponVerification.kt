import com.reloop.reloop.network.serializer.couponverification.CouponDetails
import com.reloop.reloop.network.serializer.couponverification.ValidForCategory
import com.google.gson.annotations.SerializedName


class CouponVerification {
    @SerializedName("couponDetails")
    var couponDetails: CouponDetails? = CouponDetails()

    @SerializedName("validForCategory")
    var validForCategory: ValidForCategory? = ValidForCategory()
}