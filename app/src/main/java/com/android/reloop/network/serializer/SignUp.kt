package com.reloop.reloop.network.serializer

import com.reloop.reloop.utils.Constants

class SignUp {
    var email: String? = null
    var password: String? = null
    var password_confirmation: String? = null
    var phone_number: String? = null
//    var location: String? = null
    var user_type: Int? = 0
    var hh_organization_name: String? = ""
    var no_of_employees: String? = ""
    var no_of_branches: String? = ""
    var organization_name: String? = null
//    var organization_id: Int? = 0
//    var city_id: Int? = 0
//    var district_id: Int? = 0
//    var sector_id: Int? = 0
    var latitude: Double? = 0.0
    var longitude: Double? = 0.0
    var login_type:Int=Constants.LoginTypes.APP_LOGIN
}