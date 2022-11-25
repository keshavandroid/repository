package com.reloop.reloop.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import com.android.reloop.activities.ContinueAsActivity
import com.android.reloop.network.serializer.FacebookLoginModel
import com.android.reloop.utils.Configuration
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.onesignal.OneSignal
import com.reloop.reloop.BuildConfig
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.auth.LoginAuth
import com.reloop.reloop.fragments.HomeFragment
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.network.serializer.DependencyDetail
import com.reloop.reloop.network.serializer.Login
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {

    companion object {
        var TAG = "Login Activity"
    }
    private var dependenciesListing: Dependencies? = null
    private lateinit var whatsAppNumber: String
    private var isOrganization: Boolean = false
    private var email: EditText? = null
    private var password: EditText? = null
    private var login: Button? = null
    private var signup: TextView? = null
    private var contactus: TextView? = null
    private var tvSignInAsHousehold: TextView? = null
    private var forgot_password: TextView? = null
    var rememberMe: CheckBox? = null
    private lateinit var auth: FirebaseAuth
    var googleSignIn: ImageView? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var facebookLogin: LoginButton
    private var facebookCallbackManager: CallbackManager? = null
    var fb_icon: ImageView? = null
    private lateinit var progressDialog: Dialog
    private var oneSignalPlayerId = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        MainApplication.hideActionBar(supportActionBar)
        printHashKey(this);

        isOrganization = intent.getBooleanExtra("isOrganization", false)

        //--------------------------------Google Sign In Auth-------------------
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
        //--------------------------------FaceBo ok Sign In Auth-------------------
        facebookCallbackManager = CallbackManager.Factory.create()
        initialization()
        setOnclickListeners()
        populateValues()

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DEPENDENCIES)
            ?.autoLoading(this)
            ?.enque(Network().apis()?.dependencies())
            ?.execute()

        if (isOrganization) {
            fb_icon?.visibility = View.GONE
            googleSignIn?.visibility = View.GONE
            tvSignInAsHousehold?.visibility = View.GONE
        } else {
            fb_icon?.visibility = View.VISIBLE
            googleSignIn?.visibility = View.VISIBLE
        }
    }

    private fun initialization() {
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login)
        signup = findViewById(R.id.signup)
        contactus = findViewById(R.id.contactus)
        forgot_password = findViewById(R.id.forgot_password)
        rememberMe = findViewById(R.id.remember_me)
        googleSignIn = findViewById(R.id.google_sign_in_button)
        fb_icon = findViewById(R.id.fb_icon)
        facebookLogin = findViewById(R.id.facebook_login)
        tvSignInAsHousehold = findViewById(R.id.tvSignInAsHousehold)
    }

    private fun setOnclickListeners() {
        login?.setOnClickListener(this)
        signup?.setOnClickListener(this)
        forgot_password?.setOnClickListener(this)
        contactus?.setOnClickListener(this)

        googleSignIn?.setOnClickListener(this)
        fb_icon?.setOnClickListener(this)

        //if(BuildConfig.DEBUG) {
            setFacebookLoginClickListener()
       // }
    }

    private fun setFacebookLoginClickListener() {
//        val EMAIL = "email"
        facebookLogin.setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
       // facebookLogin.setReadPermissions(listOf("public_profile", "email", "user_friends"));
        facebookLogin.setReadPermissions(listOf("public_profile", "email"));
        LoginManager.getInstance()
            .registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    val mGraphRequest = GraphRequest.newMeRequest(
                        loginResult?.accessToken
                    ) { me, response ->
                        if (response.error != null) {
                            Notify.alerterRed(this@LoginActivity, "Unable To Get User Facebook Data")
                        } else {
                            val email = me.optString("email")
                            val firstName = me.optString("first_name")
                            val lastName = me.optString("last_name")

//                            if (email.isNullOrEmpty()) {
//                                Notify.alerterRed(
//                                    this@LoginActivity,
//                                    "Unable To Get Email, Cannot Allow For Login"
//                                )
//                                if (LoginManager.getInstance() != null)
//                                    LoginManager.getInstance().logOut()
//                            } else {

                            val model = Gson().fromJson(Utils.jsonConverterObject(me as JSONObject), FacebookLoginModel::class.java)
                            socialLogin(Constants.LoginTypes.FACEBOOK, model)
//                        }
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields","id, first_name, last_name, email,gender, birthday, location")
                    mGraphRequest.parameters = parameters
                    mGraphRequest.executeAsync()
                }

                override fun onCancel() { // App code
//                    Notify.Toast("Login Manager Facebook Login Cancel")
                    Log.e(TAG,"==on cancel==")
                }

                override fun onError(exception: FacebookException) { // App code
                    Notify.Toast(exception.localizedMessage)
                }
            })
    }

    private fun populateValues() {
        try {
        val oneSignalUserID = OneSignal.getDeviceState()!!.userId
        oneSignalPlayerId = oneSignalUserID ?: ""

        /*OneSignal.addSubscriptionObserver {
             oneSignalPlayerId = it.from.userId
             Log.d("TAGggg", "populateValues: $oneSignalPlayerId")
         }*/
        /*OneSignal.idsAvailable { userId, registrationId ->
            //            Log.e("Login Activity", "User:$userId")
            oneSignalPlayerId = userId
            *//*if (registrationId != null) {
                Log.e(
                    "Login Activity",
                    "registrationId:$registrationId"
                )
            }*//*
        }*/
        //----------------ProgressDialog-----------------
        progressDialog = Dialog(this)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        progressDialog.setCancelable(true)
        progressDialog.setContentView(R.layout.progress_dialog_layout)
        //-----------For Testing Purpose-----------------
        val rememberMeData = Constants.RememberMe

        if (rememberMeData.getEmail().isNullOrEmpty()) {
            if (Configuration.isDevelopment) {
                email?.setText("reloop@testing.com")
                password?.setText("12345678")
            }
        } else {
            email?.setText(rememberMeData.getEmail())
            password?.setText(rememberMeData.getPassword())
            rememberMe?.isChecked = true
        }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.login -> {
                val authSuccessful: Boolean = LoginAuth.authenticate(
                    email?.text.toString(), password?.text.toString(), this
                )
                if (authSuccessful) {
                    login?.isClickable = false
                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.LOGIN)
                        ?.autoLoading(this)
                        ?.enque(
                            Network().apis()?.login(
                                Login(
                                    email?.text.toString(),
                                    password?.text.toString(),
                                    Constants.LoginTypes.APP_LOGIN,
                                    oneSignalPlayerId, "", ""
                                )
                            )
                        )
                        ?.execute()
                }
            }
            R.id.signup -> {
//                val intent = Intent(this, SignUpActivity::class.java)
//                startActivityForResult(intent, Constants.resultCode)
//                val newIntent = Intent()
//                setResult(Constants.resultCode, newIntent)
                showSignupActivity(isOrganization)
            }
            R.id.forgot_password -> {
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)
            }
            R.id.google_sign_in_button -> {
                signInGoogle()
            }
            R.id.fb_icon -> {
                facebookLogin.performClick()
            }
            R.id.contactus -> {

                if (!whatsAppNumber.isEmpty())
                    sendMessageToWhatsAppContact(whatsAppNumber)
                else
                    Notify.alerterRed(this, "Whatsapp Number Not available")
                 }
            }
        }

    private fun sendMessageToWhatsAppContact(number: String) {
        val packageManager = packageManager
        val i = Intent(Intent.ACTION_VIEW)
        try {
            if (appInstalledOrNot("com.whatsapp")) {
                val url = "https://api.whatsapp.com/send?phone=$number"
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i)
                }
            } else {
                Notify.alerterRed(this, "App Not installed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = packageManager
        val app_installed: Boolean = try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }
    
    private fun showSignupActivity(isOrganization: Boolean) {
        val intent = Intent(this, SignUpActivity::class.java)
        intent.putExtra("isOrganization", isOrganization)
        startActivity(intent)
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.LOGIN -> {

                val baseResponse = Utils.getBaseResponse(response)

                Notify.alerterGreen(
                    this,
                    baseResponse?.message
                )
                // your json value here
                val userModel = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    User::class.java
                )
                if (userModel.user_type == 0 || userModel.user_type == null) {
                    userModel?.user_type = Constants.UserType.household
                }
                userModel.save(userModel, this)
                if (rememberMe?.isChecked!!) {
                    val rememberMe = Constants.RememberMe
                    rememberMe.save(email?.text.toString(), password?.text.toString())
                } else {
                    val rememberMe = Constants.RememberMe
                    rememberMe.clear()
                }

                val handler = Handler()
                handler.postDelayed(
                    {
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        val newIntent = Intent()
                        setResult(Constants.resultCode, newIntent)
                        finish()
                    }, 1000
                )
            }
            RequestCodes.API.DEPENDENCIES -> {
                dependenciesListing = Dependencies()

                val baseResponse = Utils.getBaseResponse(response)
                val apiDependenciesListing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    Dependencies::class.java
                )
                if (apiDependenciesListing?.whatsapp_Number != null) {
                    whatsAppNumber = apiDependenciesListing.whatsapp_Number.get(0).value
                }
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        login?.isClickable = true
        when (tag) {
            RequestCodes.API.LOGIN -> {
                Notify.alerterRed(
                    this,
                    "${response?.message}"
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Constants.resultCode) {
            finish()
        } else if (requestCode == RequestCodes.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: Exception) {
                Log.e(SignUpActivity.TAG, "Google sign in failed", e)
                progressDialogHide()
            }
        }
    }

    fun printHashKey(pContext: Context) {
        val sha1 = "E7:FC:23:09:69:A6:4D:46:02:80:5F:F9:A1:2E:F1:F3:7C:3B:1B:64"
        val arr: List<String> = sha1.split(":")
        val byteArr = ByteArray(arr.size)

        for (i in arr.indices) {
            byteArr[i] = Integer.decode("0x" + arr[i]).toByte()
        }

        Log.e("hash!!!!!! : ", Base64.encodeToString(byteArr, Base64.NO_WRAP))

        try {
            @SuppressLint("PackageManagerGetSignatures") val info =
                pContext.packageManager.getPackageInfo(pContext.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.e(TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "printHashKey() Failed ", e)
        } catch (e: Exception) {
            Log.e(TAG, "printHashKey() Failed ", e)
        }
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RequestCodes.RC_SIGN_IN)

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        progressDialogShow()
        Log.e(TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
//                Log.e(TAG, "signInWithCredential:success")
                if (auth.currentUser != null) {
//                    val firstName: String = payload.get("given_name").toString()
//                    val lastName: String = payload.get("family_name").toString()
                    socialLogin(
                        auth.currentUser?.email,
                        Constants.LoginTypes.GOOGLE,
                        acct.givenName,
                        acct.familyName
                    )
                    progressDialogHide()

                }
            } else {
                Log.e(TAG, "signInWithCredential:failure", task.exception)
                Notify.alerterRed(this, "Google Authentication Failed")
                progressDialogHide()

            }
        }
    }

    private fun socialLogin(email: String?, type: Int, firstName: String?, lastName: String?) {

        NetworkCall.make()
            ?.setCallback(this@LoginActivity)
            ?.setTag(RequestCodes.API.LOGIN)
            ?.autoLoading(this@LoginActivity)
            ?.enque(
                Network().apis()
                    ?.login(Login(email!!, "", type, oneSignalPlayerId, firstName, lastName))
            )
            ?.execute()

    }

    private fun socialLogin(type: Int, model: FacebookLoginModel) {

        NetworkCall.make()
            ?.setCallback(this@LoginActivity)
            ?.setTag(RequestCodes.API.LOGIN)
            ?.autoLoading(this@LoginActivity)
            ?.enque(
                Network().apis()?.login(
                    Login(
                        model.email,
                        "",
                        type,
                        oneSignalPlayerId,
                        model.first_name,
                        model.last_name,
                        model.id
                    )
                )
            )
        ?.execute()
    }


    private fun progressDialogShow() {
        progressDialog.show()
    }

    private fun progressDialogHide() {
        if (progressDialog.isShowing) {
            progressDialog.hide()
        }
    }
}
