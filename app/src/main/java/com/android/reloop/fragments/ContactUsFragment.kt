package com.reloop.reloop.fragments


/*import com.google.android.recaptcha.Recaptcha
import com.google.android.recaptcha.Recaptcha.getTasksClient
import com.google.android.recaptcha.RecaptchaAction
import com.google.android.recaptcha.RecaptchaTasksClient*/

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.android.reloop.utils.CaptchaImageView
import com.android.volley.RequestQueue
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.adapters.AdapterSpinnerSimple
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.enums.OrderHistoryEnum
import com.reloop.reloop.interfaces.AlertDialogCallback
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


/**
 * A simple [Fragment] subclass.
 */
class ContactUsFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse ,
    AlertDialogCallback {

    companion object {
        var orderIdToSend = ""
        var orderIdToShow = ""
        var status = "-1"
        var _type = ""
        fun newInstance(orderId: String, orderIdToShow: String, status: String,_type: String): ContactUsFragment {
            orderIdToSend = orderId
            this.orderIdToShow = orderIdToShow
            this.status = status
            this._type = _type
            return ContactUsFragment()
        }
    }

    private var imagePart: MultipartBody.Part? = null
    var email: CustomEditText? = null
    var subject: CustomEditText? = null
    var description: CustomEditText? = null

    var img_contact_us: ImageView? = null
    var imageUri: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val requestCodeCameraPermission = 1001
    private var optionsDialog: AlertDialog? = null

    var photoFile: File? = null
    var mCurrentPhotoPath: String? = null





    var submit: Button? = null
    var back: Button? = null
    var labelSpinner: Spinner? = null
    var orderIDTV: TextView? = null
    var whatsApp: ConstraintLayout? = null
    var instagram: ConstraintLayout? = null
    var deletact: TextView? = null

    //CAPTCHA
    var refreshButton: ImageView? = null
    var captchaInput: EditText? = null
    var submitButton: Button? = null
    var captchaImageView: CaptchaImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View? = inflater.inflate(R.layout.fragment_contact_us, container, false)
        initViews(view)
        setListeners()
        populateData()


        return view
    }


    @SuppressLint("SetTextI18n")
    private fun initViews(view: View?) {
        submit = view?.findViewById(R.id.submit)
        email = view?.findViewById(R.id.email)
        subject = view?.findViewById(R.id.subject)
        description = view?.findViewById(R.id.description)
        back = view?.findViewById(R.id.back)
        labelSpinner = view?.findViewById(R.id.labels)
        orderIDTV = view?.findViewById(R.id.orderId)
        email?.setText(User.retrieveUser()?.email)
        whatsApp = view?.findViewById(R.id.whatsApp)
        instagram = view?.findViewById(R.id.instagram)
        deletact = view?.findViewById(R.id.deleteAct)

        refreshButton= view?.findViewById(R.id.regen);
        captchaInput= view?.findViewById(R.id.captchaInput);
        submitButton= view?.findViewById(R.id.submitButton);
        captchaImageView = view?.findViewById(R.id.image);
        captchaImageView?.setCaptchaType(CaptchaImageView.CaptchaGenerator.BOTH);

        img_contact_us = view?.findViewById(R.id.img_contact_us)

        //--------Set No Of Employees Spinner----------------
        val noOfEmployeesAdapter = AdapterSpinnerSimple(
            R.layout.spinner_item_textview_drawable,
            Constants.getContactUsLabels(),
            null,
            false
        )
        labelSpinner?.adapter = noOfEmployeesAdapter
        labelSpinner!!.setSelection(0)
        if (!orderIdToShow.isNullOrEmpty()) {
//            orderIDTV?.text = "Order Id : $orderIdToShow" //original

            //new
            if(_type.equals("drop-off")){
                orderIDTV?.text = "Drop-Off Id : $orderIdToShow"
            }else{
                orderIDTV?.text = "Order Id : $orderIdToShow"
            }

        } else {
            orderIDTV?.visibility = View.GONE
        }
        if (HomeFragment.settings.whatsapp_Number.isNullOrEmpty()) {
            NetworkCall.make()
                ?.setCallback(this)
                ?.setTag(RequestCodes.API.DASHBOARD)
                ?.enque(
                    Network().apis()?.dashboard()
                )
                ?.execute()
        }
    }

    private fun setListeners() {
        submit?.setOnClickListener(this)
        back?.setOnClickListener(this)
        whatsApp?.setOnClickListener(this)
        instagram?.setOnClickListener(this)
        deletact?.setOnClickListener(this)
        img_contact_us?.setOnClickListener(this)

        refreshButton?.setOnClickListener(this)
        submitButton?.setOnClickListener(this)

    }

    private fun populateData() {
        email?.setText(User.retrieveUser()?.email)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.submit -> {
                when {
                    email?.text.toString().isEmpty() -> {
                        Notify.alerterRed(activity, "Enter Email")
                    }
                    /* subject?.text.toString().isEmpty() -> {
                         Notify.alerterRed(activity, "Enter Subject")
                     }*/
                    description?.text.toString().isEmpty() -> {
                        Notify.alerterRed(activity, "Enter Description")
                    }
                    else -> {
                        if (status == "-1" || status == "0") {
                            //DO NOTHING
                        } else {
                            if (status != OrderHistoryEnum.COMPLETED && (labelSpinner!!.selectedItemPosition == 3 || labelSpinner!!.selectedItemPosition == 4)
                            ) {
                                Notify.alerterRed(activity, "Your Order is not completed")
                                return
                            } else {
                                //DO NOTHING
                            }
                        }

                        if (orderIdToSend.isNullOrEmpty()) {

                            val postEmail = email?.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                            val postSubject = Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition].toRequestBody("text/plain".toMediaTypeOrNull())
                            val postMessage = description?.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                            if(imageUri!=null){
                                val filePath: String = getRealPathFromURI(imageUri!!)
                                val imageFile = File(filePath)
                                val requestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
                            }

                            NetworkCall.make()
                                ?.setCallback(this)
                                ?.setTag(RequestCodes.API.CONTACT_US)
                                ?.autoLoading(requireActivity())
                                ?.enque(

                                    if(imagePart==null){
                                        Network().apis()?.contactUsNoImage(
                                        email?.text.toString(),
                                        Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition],
                                        description?.text.toString()
                                    )
                                    }else{
                                        Network().apis()?.contactUs(
                                            postEmail,
                                            postSubject,
                                            postMessage,
                                            imagePart!!
                                        )
                                    }
                                )
                                ?.execute()
                        } else {
                            if (status == "0") {

                                val postEmail = email?.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val postSubject = Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition].toRequestBody("text/plain".toMediaTypeOrNull())
                                val postMessage = description?.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val postCollectionID = orderIdToSend.toRequestBody("text/plain".toMediaTypeOrNull())

                                if(imageUri!=null){
                                    val filePath: String = getRealPathFromURI(imageUri!!)
                                    val imageFile = File(filePath)
                                    val requestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
                                }

                                if(imagePart==null){
                                    NetworkCall.make()
                                    ?.setCallback(this)
                                    ?.setTag(RequestCodes.API.CONTACT_US)
                                    ?.autoLoading(requireActivity())
                                    ?.enque(Network().apis()?.contactUsCollectionNoImage(email?.text.toString(),
                                            Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition],
                                            description?.text.toString(), orderIdToSend))?.execute()
                                }else{
                                    NetworkCall.make()
                                        ?.setCallback(this)
                                        ?.setTag(RequestCodes.API.CONTACT_US)
                                        ?.autoLoading(requireActivity())
                                        ?.enque(Network().apis()?.contactUsCollection(postEmail, postSubject, postMessage, postCollectionID, imagePart!!))
                                        ?.execute()
                                }
                            } else{

                                val postEmail = email?.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val postSubject = Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition].toRequestBody("text/plain".toMediaTypeOrNull())
                                val postMessage = description?.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                                val postCollectionID = orderIdToSend.toRequestBody("text/plain".toMediaTypeOrNull())

                                if(imageUri!=null){
                                    val filePath: String = getRealPathFromURI(imageUri!!)
                                    val imageFile = File(filePath)
                                    val requestBody = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                                    imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
                                }

                                if(imagePart==null){
                                    NetworkCall.make()
                                   ?.setCallback(this)
                                   ?.setTag(RequestCodes.API.CONTACT_US)
                                   ?.autoLoading(requireActivity())
                                   ?.enque(
                                       Network().apis()?.contactUsNoImage(
                                           email?.text.toString(),
                                           Constants.getContactUsLabels()[labelSpinner!!.selectedItemPosition],
                                           description?.text.toString(), orderIdToSend
                                       )
                                   )
                                   ?.execute()
                                }else{
                                    NetworkCall.make()
                                        ?.setCallback(this)
                                        ?.setTag(RequestCodes.API.CONTACT_US)
                                        ?.autoLoading(requireActivity())
                                        ?.enque(
                                            Network().apis()?.contactUs(
                                                postEmail,
                                                postSubject,
                                                postMessage,
                                                postCollectionID,
                                                imagePart!!
                                            )
                                        )
                                        ?.execute()
                                }
                            }
                        }
                    }
                }
            }
            R.id.back -> {
                activity?.onBackPressed()
            }
            R.id.whatsApp -> {
                if (!HomeFragment.settings.whatsapp_Number.isNullOrEmpty())
                    sendMessageToWhatsAppContact(HomeFragment.settings.whatsapp_Number[0].value)
                else
                    Notify.alerterRed(requireActivity(), "Whatsapp Number Not available")
            }
            R.id.instagram -> {
                if (!HomeFragment.settings.instagram_Link.isNullOrEmpty())
//                    openInstagram("https://www.instagram.com/pakistanicinemaa/")
                    openInstagram(HomeFragment.settings.instagram_Link[0].value)
                else
                    Notify.alerterRed(requireActivity(), "Instagram Account Not available")
            }
            R.id.deleteAct -> {
                AlertDialogs.alertDialog(activity, this, activity?.getString(R.string.delete_account_text_msg))
            }

            R.id.regen -> {
                captchaImageView?.regenerate();

            }
            R.id.submitButton -> {

                try {
                    if (captchaImageView?.getCaptchaCode() != null) {
                        if (captchaInput?.getText().toString().equals(captchaImageView?.getCaptchaCode())
                        ) {
                            Toast.makeText(requireContext(), "Matched", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Not Matching", Toast.LENGTH_SHORT)
                                .show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Code not found", Toast.LENGTH_SHORT)
                            .show();
                    }
                }
                catch (e : NullPointerException)
                {
                    e.printStackTrace()
                }
            }

            R.id.img_contact_us -> {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED ) {
                        askForCameraPermission()
                    } else {
                        showOptionsDialog()
                    }
                }else{
                    if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ) {
                        askForCameraPermission()
                    } else {
                        showOptionsDialog()
                    }
                }

            }

        }
    }

    private fun verifyCaptcha(){
        /*recaptchaTasksClient?.let {
            it.executeTask(RecaptchaAction.custom("contact_form"))
                .addOnSuccessListener(OnSuccessListener { token ->
                    // Handle success ...
                    // Proceed with form submission using the token
                    submitForm(token)
                })
                .addOnFailureListener(OnFailureListener { e ->
                    // Handle communication errors ...
                    // Log or display the error to the user
                    e.printStackTrace() // or show an error message
                })
        } ?: run {
            // Handle case where recaptchaTasksClient is not initialized
            // Show error to the user
        }*/
    }

    private fun submitForm(token: String) {
        // Use the token to submit the form
        // Implement your form submission logic here
        // Example:
        // val formData = collectFormData()
        // sendFormDataToServer(formData, token)

        Toast.makeText(requireContext(),""+token,Toast.LENGTH_SHORT).show()
    }


    private fun getRealPathFromURI(uri: Uri): String {
        val cursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            it.getString(columnIndex)
        } ?: ""
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE), requestCodeCameraPermission);

    }

    private fun showOptionsDialog() {
        // You can show a dialog or use any other UI element to let the user choose between camera and gallery
        // For simplicity, I'll directly call the camera intent here

        val options = arrayOf("Capture from Camera", "Pick from Gallery")

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
        builder.setItems(options) { _, which ->
            when (which) {
                0 ->{
                    dispatchTakePictureIntent()
                    optionsDialog?.dismiss()
                }
                1 ->{
                    dispatchPickImageIntent()
                    optionsDialog?.dismiss()
                }
            }
        }

        optionsDialog = builder.create()
        optionsDialog?.show()

    }

    private fun dispatchTakePictureIntent() {

        //OLD
        /*val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }*/

        //NEW
        captureImage()

    }

    private fun dispatchPickImageIntent() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageIntent.type = "image/*"
        startActivityForResult(pickImageIntent, REQUEST_IMAGE_PICK)
    }

    private fun captureImage() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                0
            )
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                // Create the File where the photo should go
                try {
                    photoFile = createImageFile()
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        val photoURI = FileProvider.getUriForFile(
                            requireActivity(),
                            requireActivity().packageName + ".fileprovider",
                            photoFile!!
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                    }
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    displayMessage(requireContext(), ex.message.toString())
                }

            } else {
                displayMessage(requireContext(), "Null")
            }
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    /*val extras: Bundle? = data?.extras
                    val imageBitmap = extras?.get("data") as? Bitmap
                    img_contact_us!!.setImageBitmap(imageBitmap)*/

                    //NEW
//                    imageUri = data?.data
//                    img_contact_us?.setImageURI(imageUri)


                    //ORIGINAL
//                    imageUri = getImageUriFromCamera(data)

//                    val myBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
//                    img_contact_us!!.setImageBitmap(myBitmap)
//                    imageUri = getImageUri(requireActivity(),myBitmap)

                    processCapturedImage(requireContext(),photoFile!!.absolutePath)


                }
                REQUEST_IMAGE_PICK -> {
                    // Handle image picked from gallery
                    // You may want to use an image loading library like Picasso or Glide here
                    // Example: Glide.with(this).load(data?.data).into(imageView)

                    imageUri = data?.data
                    Glide.with(requireActivity()).load(data?.data).into(img_contact_us!!)

                }
            }
        }
    }


    fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imagePath: String): Bitmap {
        val exif = try {
            ExifInterface(imagePath)
        } catch (e: IOException) {
            e.printStackTrace()
            return bitmap
        }

        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotationInDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val matrix = Matrix()
        if (rotationInDegrees != 0) {
            matrix.postRotate(rotationInDegrees.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        return bitmap
    }

    // Usage example
    fun processCapturedImage(context: Context, imagePath: String) {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(imagePath, options)

        val rotatedBitmap = rotateImageIfRequired(context, bitmap, imagePath)

        img_contact_us!!.setImageBitmap(rotatedBitmap)
        imageUri = getImageUri(requireActivity(),rotatedBitmap)

        // Now you can use the rotatedBitmap as needed.
        // For example, display it in an ImageView.
        // imageView.setImageBitmap(rotatedBitmap)
    }

    private fun getImageUriFromCamera(data: Intent?): Uri? {
        val extras: Bundle? = data?.extras
        val imageBitmap = extras?.get("data") as? Bitmap
        return imageBitmap?.let { bitmap ->
            val uri = getImageUri(requireActivity(), bitmap)
            img_contact_us?.setImageBitmap(bitmap)
            uri
        }


    }

    private fun getImageUri(inContext: Activity, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


    override fun callDialog(model: Any?) {
        //call api to delete account

        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DELETE_ACCOUNT)
            ?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.deleteaccount())
            ?.execute()
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        when (tag) {
            RequestCodes.API.CONTACT_US -> {
                val baseResponse = Utils.getBaseResponse(response)
                Notify.alerterGreen(activity, baseResponse?.message)
                Handler().postDelayed({
                    activity?.onBackPressed()
                }, 1000)


            }
            RequestCodes.API.DASHBOARD -> {
                val baseResponse = Utils.getBaseResponse(response)
                try {
                    val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dashboard::class.java
                    )
                    HomeFragment.settings = dashboard.settings
                } catch (e: Exception) {
                    Log.e("TAG", e.toString())
                }
            }
            RequestCodes.API.DELETE_ACCOUNT -> {
                val baseResponse = Utils.getBaseResponse(response)
                Notify.alerterGreen(activity, baseResponse?.message)
                Handler().postDelayed({
                    Utils.logOut(activity)
                }, 1000)
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    private fun sendMessageToWhatsAppContact(number: String) {
        val packageManager = requireActivity().packageManager
        val i = Intent(Intent.ACTION_VIEW)
        try {
            if (appInstalledOrNot("com.whatsapp")) {
                val url = "https://api.whatsapp.com/send?phone=$number"
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                if (i.resolveActivity(packageManager) != null) {
                    requireActivity().startActivity(i)
                }
            } else {
                Notify.alerterRed(activity, "App Not installed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun appInstalledOrNot(uri: String): Boolean {
        val pm = requireActivity().packageManager
        val app_installed: Boolean = try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return app_installed
    }

    fun openInstagram(url: String) {
        val uri = Uri.parse(url)
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/xxx")
                )
            )
        }
    }

}
