package com.reloop.reloop.network.serializer

class Login {
    var email: String
    var password: String
    var login_type: Int
    var player_id: String
    var first_name: String?
    var last_name: String?
    lateinit var facebook_id: String

    constructor(
        email: String,
        password: String,
        login_type: Int,
        player_id: String,
        first_name: String?,
        last_name: String?
    ) {
        this.email = email
        this.password = password
        this.login_type = login_type
        this.player_id = player_id
        this.first_name = first_name
        this.last_name = last_name
    }

    constructor(
        email: String,
        password: String,
        login_type: Int,
        player_id: String,
        first_name: String?,
        last_name: String?,
        facebook_id: String
    ) {
        this.email = email
        this.password = password
        this.login_type = login_type
        this.player_id = player_id
        this.first_name = first_name
        this.last_name = last_name
        this.facebook_id = facebook_id
    }

}