package com.reloop.reloop.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.android.reloop.network.serializer.FacebookLoginModel
import com.android.reloop.searchablespinner.SearchableSpinner
import com.android.reloop.utils.PhoneTextFormatter
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
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
import com.reloop.reloop.R
import com.reloop.reloop.adapters.AdapterSpinnerCustom
import com.reloop.reloop.adapters.AdapterSpinnerSimple
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.auth.SignUpHouseholdAuth
import com.reloop.reloop.auth.SignUpOrganizationAuth
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.fragments.HomeFragment
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.*
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import kotlinx.android.synthetic.main.activity_signup.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class SignUpActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {

    companion object {
        var TAG = "SignUpActivity"
    }

    private lateinit var whatsAppNumber: String
    private var isOrganization: Boolean = false
    var createSignup: Button? = null
    var household: Button? = null
    var organizationBtn: Button? = null
    var noOfBranches: Spinner? = null
    var noOfEmployees: Spinner? = null
    var organization: CustomEditText? = null
    var sector: SearchableSpinner? = null
    var organizationName: CustomEditText? = null
    var login: TextView? = null
    private var contactus: TextView? = null
    var location: CustomEditText? = null
    var userType: Int? = null
    var signUp: SignUp? = SignUp()
    var districtAdapter: AdapterSpinnerSimple? = null
    var cityAdapter: AdapterSpinnerCustom? = null
    private lateinit var auth: FirebaseAuth
    var googleSignIn: ImageView? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var facebookLogin: LoginButton
    private var facebookCallbackManager: CallbackManager? = null
    var fb_icon: ImageView? = null
    private lateinit var progressDialog: Dialog
    private var dependenciesListing: Dependencies? = null
    val districtFilterList: ArrayList<DependencyDetail>? = ArrayList()
    private var oneSignalPlayerId = ""

    var districtFilterNames: ArrayList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        MainApplication.hideActionBar(supportActionBar)

        isOrganization = intent.getBooleanExtra("isOrganization", false)

        //--------------------------------Google Sign In Auth-------------------
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
        //--------------------------------FaceBook Sign In Auth-------------------
        facebookCallbackManager = CallbackManager.Factory.create()
//        LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"));
        initViews()
        setClickListeners()
        populateData()
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DEPENDENCIES)
            ?.autoLoading(this)
            ?.enque(Network().apis()?.dependencies())
            ?.execute()

        if (isOrganization) {
            updateUI(Constants.UserType.organization)
        } else {
            updateUI(Constants.UserType.household)
        }
    }


    private fun initViews() {
        createSignup = findViewById(R.id.signup)
        contactus = findViewById(R.id.contactus)
        household = findViewById(R.id.household)
        organizationBtn = findViewById(R.id.organization_btn)
        noOfBranches = findViewById(R.id.no_of_branches)
        noOfEmployees = findViewById(R.id.no_of_employees)
        organization = findViewById(R.id.organization)
        sector = findViewById(R.id.sector)
        login = findViewById(R.id.login)
        organizationName = findViewById(R.id.organization_name)
        location = findViewById(R.id.location)
        googleSignIn = findViewById(R.id.google_sign_in_button)
        fb_icon = findViewById(R.id.fb_icon)
        facebookLogin = findViewById(R.id.facebook_login)
        contactus?.setOnClickListener(this)
    }

    private fun setClickListeners() {
        createSignup?.setOnClickListener(this)
        household?.setOnClickListener(this)
        organizationBtn?.setOnClickListener(this)
        login?.setOnClickListener(this)
        location?.setOnClickListener(this)
        contactus?.setOnClickListener(this)

        googleSignIn?.setOnClickListener(this)
        fb_icon?.setOnClickListener(this)
//        setFacebookLoginClickListener()
    }

    private fun setFacebookLoginClickListener() {
//        facebookLogin.setReadPermissions(listOf("public_profile", "email"));

        LoginManager.getInstance().logInWithReadPermissions(
            this@SignUpActivity,
            facebookCallbackManager!!,
            Arrays.asList("public_profile","email"))//NEW AD

        LoginManager.getInstance()
            .registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val mGraphRequest = GraphRequest.newMeRequest(
                        loginResult?.accessToken
                    ) { me, response ->
                        if (response!!.error != null) {
                            Notify.alerterRed(
                                this@SignUpActivity,
                                "Unable To Get User Facebook Data"
                            )
                        } else {
                            val email = me?.optString("email")
                            val firstName = me?.optString("first_name")
                            val lastName = me?.optString("last_name")

                            /*  if (email.isNullOrEmpty()) {
                                  Notify.alerterRed(
                                      this@SignUpActivity,
                                      "Unable To Get Email, Cannot Allow For Login"
                                  )
                                  if (LoginManager.getInstance() != null)
                                      LoginManager.getInstance().logOut()
                              } else {*/
                            /*socialLogin(
                                email,
                                Constants.LoginTypes.FACEBOOK,
                                firstName,
                                lastName
                            )*/

                            val model = Gson().fromJson(Utils.jsonConverterObject(me as JSONObject),
                                FacebookLoginModel::class.java)
                            socialLogin(Constants.LoginTypes.FACEBOOK, model)
                        // }
                        }
                    }
                    val parameters = Bundle()
                    parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location")
                    mGraphRequest.parameters = parameters
                    mGraphRequest.executeAsync()
                }

                override fun onCancel() {
                // App code
//                 Notify.Toast("Login Manager Facebook Login Cancel")
                }

                override fun onError(exception: FacebookException) { // App code
                    Notify.Toast("Login Manager Facebook Login Failed")
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun populateData() {
        try {
            val oneSignalUserID = OneSignal.getDeviceState()!!.userId
            oneSignalPlayerId = oneSignalUserID ?: ""
            /*OneSignal.idsAvailable { userId, registrationId ->
                oneSignalPlayerId = userId
            }*/
            household?.performClick()
            email?.markRequired()
            password?.markRequired()
            confirm_password?.markRequired()
            phone_number?.markRequired()
            organizationName?.markRequired()
            organization?.markOptional()
            sector?.setTitle("")
            sector?.setPositiveButton("")
            district?.setTitle("")
            district?.setPositiveButton("")
            //----------------ProgressDialog-----------------
            progressDialog = Dialog(this)
            progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            progressDialog.setCancelable(true)
            progressDialog.setContentView(R.layout.progress_dialog_layout)
            //--------Set No Of Employees Spinner----------------
            val noOfEmployeesAdapter = AdapterSpinnerSimple(
                R.layout.spinner_item_textview_drawable,
                Constants.getEmployeesNumberSelectList(),
                getDrawable(R.drawable.icon_employees_un)!!,
                true
            )
            noOfEmployees?.adapter = noOfEmployeesAdapter
            //--------Set No Of Branches Spinner----------------
            val noOfBranchesAdapter = AdapterSpinnerSimple(
                R.layout.spinner_item_textview_drawable,
                Constants.getBranchesNumberListSelectList(),
                getDrawable(R.drawable.icon_branches_office_un)!!,
                true
            )
            noOfBranches?.adapter = noOfBranchesAdapter

            llPhone.setOnClickListener {
                phone_number.performClick()
            }

            ivcall.setOnClickListener {
                phone_number.performClick()
            }

            tvprefix.setOnClickListener {
                phone_number.performClick()
            }

            phone_number.addTextChangedListener(PhoneTextFormatter(phone_number, "#-########"))


            //-----------------Phone Number Changes--------------
            phone_number.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    /*if (phone_number.text!!.length <= 6)
                        phone_number?.setText("+971-5")

                    phone_number?.setSelection(phone_number.text!!.length)*/

                    tvprefix.visibility = View.VISIBLE
                    phone_number.setHint("")
                    ivcall.isFocusable = true
                    ivcall.setBackgroundResource(R.drawable.icon_phone_en)
                    llPhone.setBackgroundResource(R.drawable.edittextbackgroundselected)
                    phone_number.setPadding(1,0,10,0)

                    val inputMethodManager: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    if (inputMethodManager != null) {
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                    }
                }
            }

            phone_number.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int,
                    count: Int, after: Int
                ) {
                }

                @SuppressLint("SetTextI18n")
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val after = s.toString()
                    tvprefix.visibility = View.VISIBLE
                    phone_number.setHint("")
                    ivcall.isFocusable = true
                    ivcall.setBackgroundResource(R.drawable.icon_phone_en)
                    llPhone.setBackgroundResource(R.drawable.edittextbackgroundselected)
                    phone_number.setPadding(1,0,10,0)

                    /*if (s.length == 1 && count > before) {
                        phone_number?.setText(phone_number.text.toString() + "-")
                        phone_number?.setSelection(phone_number.text!!.length)
                    }*/

                    /*if (after.length <= 6 && count < before) {
                        phone_number?.setText("+971-5")
                        phone_number?.setSelection(phone_number.text!!.length)

                    } else if (s.length == 7 && count > before) {
                        phone_number?.setText(phone_number.text.toString() + "-")
                        phone_number?.setSelection(phone_number.text!!.length)
                    }*/
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isNumeric(str: String) = str.all { it in '0'..'9' }

    private fun populateSpinnerData(
        citiesList: ArrayList<DependencyDetail>?,
        districtList: ArrayList<DependencyDetail>?, sectorList: ArrayList<DependencyDetail>?
    ) {
        //-------------------District Spinner---------------------
        districtFilterNames.clear()
        for (i in districtFilterList!!.indices) {
            districtFilterNames.add(districtFilterList[i].name.toString())
        }
        districtAdapter = AdapterSpinnerSimple(
            R.layout.spinner_item_textview_drawable,
            districtFilterNames,
            getDrawable(R.drawable.icon_address_location_un)!!, true
        )
        district?.adapter = districtAdapter
        //-------------------City Spinner---------------------

        cityAdapter = AdapterSpinnerCustom(
            R.layout.spinner_item_textview_drawable,
            citiesList,
            getDrawable(R.drawable.icon_address_location_un)!!, true
        )
        city?.adapter = cityAdapter
        city?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    districtFilterList.clear()
                    districtList?.get(0)?.let { districtFilterList.add(it) }
                    districtFilterNames.clear()
                    for (i in districtFilterList.indices) {
                        districtFilterNames.add(districtFilterList[i].name.toString())
                    }
                    districtAdapter?.notifyDataSetChanged(districtFilterNames)
                } else {
                    if (districtList != null) {
                        districtFilterList.clear()
                        districtList[0].let { districtFilterList.add(it) }
                        for (i in districtList.indices) {
                            if (districtList[i].city_id == citiesList?.get(position)?.id) {
                                districtFilterList.add(districtList[i])
                            }
                        }
                    }
                    districtFilterNames.clear()
                    for (i in districtFilterList.indices) {
                        districtFilterNames.add(districtFilterList[i].name.toString())
                    }
                    districtAdapter?.notifyDataSetChanged(districtFilterNames)
                }
            }
        }

        //-------------------Sector Spinner---------------------

        if (!sectorList.isNullOrEmpty()) {
            val names: ArrayList<String> = ArrayList()
            names.clear()
            for (i in sectorList.indices) {
                names.add(sectorList[i].name.toString())
            }
            val sectorAdapter = AdapterSpinnerSimple(R.layout.spinner_item_textview_drawable, names, getDrawable(R.drawable.icon_sector_un)!!, true)
            sector?.adapter = sectorAdapter
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.signup -> {
                signupUser()
            }
            R.id.household -> {
                updateUI(Constants.UserType.household)
            }
            R.id.organization_btn -> {
                updateUI(Constants.UserType.organization)

            }
            R.id.login -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivityForResult(intent, Constants.resultCode)
                val newIntent = Intent()
                setResult(Constants.resultCode, newIntent)
            }
            R.id.location -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivityForResult(intent, Constants.mapsCode)
            }

            R.id.google_sign_in_button -> {
                signInGoogle()
            }
            R.id.fb_icon -> {
//                facebookLogin.performClick()
                setFacebookLoginClickListener()
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

    private fun signupUser() {
        if (userType == Constants.UserType.household) {
            val authSuccessful: Boolean = SignUpHouseholdAuth.authenticate(
                email?.text.toString(),
                password?.text.toString(),
                confirm_password?.text.toString(),
                tvprefix.text.toString()+phone_number?.text.toString(),
                location?.text.toString(),
                city.selectedItemPosition,
                district.selectedItemPosition,
                this
            )
            if (authSuccessful) {
                createSignup?.isClickable = false

                //ALL OLD params
                /*signUp?.email = email?.text.toString()
                signUp?.password = password?.text.toString()
                signUp?.password_confirmation = confirm_password?.text.toString()
                signUp?.phone_number = tvprefix.text.toString()+phone_number?.text.toString()
                signUp?.location = location?.text.toString()
                signUp?.user_type = userType
                signUp?.no_of_employees = null
                signUp?.no_of_branches = null
                if (organization?.text.toString().isEmpty()) {
                    signUp?.hh_organization_name = null
                } else {
                    signUp?.hh_organization_name = organization?.text.toString()
                }
                signUp?.city_id = dependenciesListing?.cities?.get(city.selectedItemPosition)?.id
                signUp?.district_id = districtFilterList?.get(district.selectedItemPosition)?.id
                signUp?.sector_id = null
                signUp?.organization_id = null*/

                signUp?.email = email?.text.toString()
                signUp?.password = password?.text.toString()
                signUp?.password_confirmation = confirm_password?.text.toString()
                signUp?.phone_number = tvprefix.text.toString()+phone_number?.text.toString()
//                signUp?.location = location?.text.toString()
                signUp?.user_type = userType
//                signUp?.no_of_employees = null
//                signUp?.no_of_branches = null
                if (organization?.text.toString().isEmpty()) {
//                    signUp?.hh_organization_name = null
                } else {
//                    signUp?.hh_organization_name = organization?.text.toString()
                }
//                signUp?.city_id = dependenciesListing?.cities?.get(city.selectedItemPosition)?.id
//                signUp?.district_id = districtFilterList?.get(district.selectedItemPosition)?.id
//                signUp?.sector_id = null
//                signUp?.organization_id = null

                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.SIGN_UP)
                    ?.autoLoading(this)
                    ?.enque(Network().apis()?.register(signUp))?.execute()
            }
        } else if (userType == Constants.UserType.organization) {
            val authSuccessful: Boolean = SignUpOrganizationAuth.authenticate(
                email?.text.toString(),
                organizationName?.text.toString(),
                password?.text.toString(),
                confirm_password?.text.toString(),
                tvprefix.text.toString()+phone_number?.text.toString(),
                location?.text.toString(),
                noOfEmployees?.selectedItemPosition!!,
                noOfBranches?.selectedItemPosition!!,
                city.selectedItemPosition,
                district.selectedItemPosition,
                sector?.selectedItemPosition,
                this
            )
            if (authSuccessful) {
                createSignup?.isClickable = false

                //ALL OLD Params
                /*signUp?.email = email?.text.toString()
                signUp?.organization_name = organizationName?.text.toString()
                signUp?.password = password?.text.toString()
                signUp?.password_confirmation = confirm_password?.text.toString()
                signUp?.phone_number = tvprefix.text.toString()+phone_number?.text.toString()
                signUp?.location = location?.text.toString()
                signUp?.user_type = userType
                signUp?.no_of_employees = Constants.getEmployeesNumberSelectList()[noOfEmployees?.selectedItemPosition!!]
                signUp?.no_of_branches = Constants.getBranchesNumberListSelectList()[noOfBranches?.selectedItemPosition!!]
                signUp?.organization_id = null
                signUp?.city_id = dependenciesListing?.cities?.get(city.selectedItemPosition)?.id
                signUp?.district_id = districtFilterList?.get(district.selectedItemPosition)?.id
                signUp?.sector_id = dependenciesListing?.sectors?.get(sector?.selectedItemPosition!!)?.id*/

                signUp?.email = email?.text.toString()
                signUp?.organization_name = organizationName?.text.toString()
                signUp?.password = password?.text.toString()
                signUp?.password_confirmation = confirm_password?.text.toString()
                signUp?.phone_number = tvprefix.text.toString() + phone_number?.text.toString()
//                signUp?.location = location?.text.toString()
                signUp?.user_type = userType
//                signUp?.no_of_employees = Constants.getEmployeesNumberSelectList()[noOfEmployees?.selectedItemPosition!!]
//                signUp?.no_of_branches = Constants.getBranchesNumberListSelectList()[noOfBranches?.selectedItemPosition!!]
//                signUp?.organization_id = null
//                signUp?.city_id = dependenciesListing?.cities?.get(city.selectedItemPosition)?.id
//                signUp?.district_id = districtFilterList?.get(district.selectedItemPosition)?.id
//                signUp?.sector_id = dependenciesListing?.sectors?.get(sector?.selectedItemPosition!!)?.id

                NetworkCall.make()
                    ?.setCallback(this)
                    ?.setTag(RequestCodes.API.SIGN_UP)
                    ?.autoLoading(this)
                    ?.enque(Network().apis()?.register(signUp))
                    ?.execute()
            }
        }
    }


    private fun updateUI(value: Int) {
        when (value) {
            Constants.UserType.organization -> {
                //AKSHAY17
                /*noOfEmployees?.visibility = View.VISIBLE
                noOfBranches?.visibility = View.VISIBLE
                sector?.visibility = View.VISIBLE
                organization?.visibility = View.GONE
                */

                organizationName?.visibility = View.VISIBLE
                organizationBtn?.background = getDrawable(R.drawable.button_shape_green)
                organizationBtn?.setTextColor(getColor(R.color.white))
                household?.background = getDrawable(R.drawable.signup_button_style)
                household?.setTextColor(getColor(R.color.color_signup_text))
                userType = Constants.UserType.organization
                location?.hint = "Select Main Location"
                dependenciesListing?.cities?.get(0)?.name = "Select Main City"
                dependenciesListing?.districts?.get(0)?.name = "Select Main District"
                cityAdapter?.notifyDataSetChanged()
                districtAdapter?.notifyDataSetChanged()
                social_login_layout?.visibility = View.GONE
                createSignup?.text = getString(R.string.submit)
                location?.markRequired()

            }
            Constants.UserType.household -> {
                //AKSHAY17
/*                noOfEmployees?.visibility = View.GONE
                noOfBranches?.visibility = View.GONE
                sector?.visibility = View.GONE
                organization?.visibility = View.VISIBLE
                */

                organizationName?.visibility = View.GONE
                household?.background = getDrawable(R.drawable.button_shape_green)
                household?.setTextColor(getColor(R.color.white))
                organizationBtn?.background = getDrawable(R.drawable.signup_button_style)
                organizationBtn?.setTextColor(getColor(R.color.color_signup_text))
                userType = Constants.UserType.household
                location?.hint = "Select Location"
                dependenciesListing?.cities?.get(0)?.name = "Select City"
                dependenciesListing?.districts?.get(0)?.name = "Select District"
                cityAdapter?.notifyDataSetChanged()
                districtAdapter?.notifyDataSetChanged()
                social_login_layout?.visibility = View.VISIBLE
                createSignup?.text = getString(R.string.create)
                location?.markRequired()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Constants.resultCode) {
            finish()
        } else if (resultCode == Constants.mapsCode) {
            val address: String? = data?.getStringExtra(Constants.DataConstants.location)
            location?.setText(address)
            signUp?.latitude = data?.getDoubleExtra(Constants.DataConstants.latitude, 0.0)
            signUp?.longitude = data?.getDoubleExtra(Constants.DataConstants.longitude, 0.0)
        } else if (requestCode == RequestCodes.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: Exception) {
                Log.e(TAG, "Google sign in failed", e)
                progressDialogHide()
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.SIGN_UP -> {

                val baseResponse = Utils.getBaseResponse(response)
                Notify.alerterGreen(this, baseResponse?.message)
                val sharedPreferences: SharedPreferences = this.getSharedPreferences(
                    "continueASsharedPref",
                    Context.MODE_PRIVATE
                )

                //old
                /*if (isOrganization) {
                    val sharedValue =
                        sharedPreferences.getString(
                            "organization_signup_text_value",
                            "defaultValue"
                        )
                    showInfoPopup(this, "Organization", sharedValue)
                }*/

                if(userType == Constants.UserType.organization){
                    val sharedValue =
                        sharedPreferences.getString(
                            "organization_signup_text_value",
                            "defaultValue"
                        )
                    showInfoPopup(this, "Organization", sharedValue)
                }
                val handler = Handler()
                handler.postDelayed(
                    {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }, 2000
                )

            }
            RequestCodes.API.DEPENDENCIES -> {
                dependenciesListing = Dependencies()
                var dependencyDetail = DependencyDetail()
                dependencyDetail.name = "Select City"
                dependencyDetail.id = 0
                dependenciesListing?.cities?.add(dependencyDetail)
                dependencyDetail = DependencyDetail()
                dependencyDetail.name = "Select District"
                dependencyDetail.id = 0
                dependenciesListing?.districts?.add(dependencyDetail)
                dependencyDetail = DependencyDetail()
                dependencyDetail.name = "Select Sector"
                dependencyDetail.id = 0
                dependenciesListing?.sectors?.add(dependencyDetail)
                val baseResponse = Utils.getBaseResponse(response)
                val apiDependenciesListing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                    Dependencies::class.java
                )
                if (apiDependenciesListing?.sectors != null) {
                    dependenciesListing?.sectors?.addAll(apiDependenciesListing.sectors!!)
                }
                if (apiDependenciesListing?.cities != null) {
                    dependenciesListing?.cities?.addAll(apiDependenciesListing.cities!!)
                }
                if (apiDependenciesListing?.districts != null) {
                    dependenciesListing?.districts?.addAll(apiDependenciesListing.districts!!)
                }
                if (apiDependenciesListing?.organizations != null) {
                    dependenciesListing?.organizations?.addAll(apiDependenciesListing.organizations!!)
                }
                if (apiDependenciesListing?.whatsapp_Number != null) {
                    whatsAppNumber = apiDependenciesListing.whatsapp_Number.get(0).value
                }

                populateSpinnerData(
                    dependenciesListing?.cities,
                    dependenciesListing?.districts,
                    dependenciesListing?.sectors
                )
            }
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
                userModel.save(userModel, this,true)
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
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        createSignup?.isClickable = true
        when (tag) {
            RequestCodes.API.SIGN_UP -> {
                Notify.alerterRed(
                    this,
                    "${response?.message}"
                )
            }

            RequestCodes.API.DEPENDENCIES -> {
                Notify.alerterRed(
                    this,
                    "${response?.message}"
                )
            }
            RequestCodes.API.LOGIN -> {
                Notify.alerterRed(
                    this,
                    "${response?.message}"
                )
            }
        }
    }

    fun showInfoPopup(
        activity: Activity?,
        titleString: String?,
        messageString: String?
    ) {
        val dialog = Dialog(activity!!)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setContentView(R.layout.show_info_popup_dialog)
        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(false)
        val tvTitleInfo = dialog?.findViewById(R.id.tv_title_info) as TextView
        val tvMessageInfo = dialog?.findViewById(R.id.tv_message_info) as TextView
        val cancel = dialog?.findViewById(R.id.close) as Button
        cancel.text = "Close"
        tvTitleInfo.text = titleString
        tvMessageInfo.text = messageString
        cancel.setOnClickListener {
            dialog.dismiss()
            val handler = Handler()
            handler.postDelayed(
                {
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("isOrganization", isOrganization)
                    startActivity(intent)
                    val intent1 = Intent()
                    setResult(Constants.resultCode, intent1)
                    finish()
                }, 1000
            )
        }
        dialog.show()
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RequestCodes.RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        progressDialogShow()
        Log.e(LoginActivity.TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.e(LoginActivity.TAG, "signInWithCredential:success")
                if (auth.currentUser != null) {
                    socialLogin(
                        auth.currentUser?.email,
                        Constants.LoginTypes.GOOGLE,
                        acct.givenName,
                        acct.familyName)

                    progressDialogHide()
                }
            } else {
                Log.e(LoginActivity.TAG, "signInWithCredential:failure", task.exception)
                Notify.alerterRed(this, "Google Authentication Failed")
                progressDialogHide()
            }
        }
    }

    private fun socialLogin(type: Int, model: FacebookLoginModel) {

        NetworkCall.make()
            ?.setCallback(this@SignUpActivity)
            ?.setTag(RequestCodes.API.LOGIN)
            ?.autoLoading(this@SignUpActivity)
            ?.enque(Network().apis()?.login(Login(model.email, "", type, oneSignalPlayerId, model.first_name, model.last_name, model.id)))
            ?.execute()
    }


    private fun socialLogin(email: String?, type: Int, firstName: String?, lastName: String?) {
        NetworkCall.make()
            ?.setCallback(this@SignUpActivity)
            ?.setTag(RequestCodes.API.LOGIN)
            ?.autoLoading(this@SignUpActivity)
            ?.enque(Network().apis()?.login(Login(email!!, "", type, oneSignalPlayerId, firstName, lastName)))
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
