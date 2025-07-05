package com.reloop.reloop.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.adapters.ImagesAdapter
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.activities.MapsActivity
import com.reloop.reloop.adapters.viewholders.AdapterSpinnerAddress
import com.reloop.reloop.adapters.viewholders.AdapterSpinnerAddressTitle
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.interfaces.ChildToParent
import com.reloop.reloop.interfaces.ParentToChild
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.network.serializer.collectionrequest.CollectionRequest
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.network.serializer.orderhistory.CollectionRequests
import com.reloop.reloop.network.serializer.orderhistory.DistrictWithOrder
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class ConfirmCollectionFragment : BaseFragment(), ParentToChild, OnNetworkResponse,
    View.OnClickListener, ImagesAdapter.OnRemoveImageListener {

    private val fragmentScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onStart() {
        super.onStart()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
    }



    var childToParent: ChildToParent? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ChildToParent) {
            childToParent = parentFragment as? ChildToParent?
        } else {
            Notify.Toast("Child To Parent Callback Not Implemented")
        }
    }

    companion object {
        var selectedDateAvailable = true
        var collectionRequest: CollectionRequest? = CollectionRequest()
        var materialCategories: ArrayList<MaterialCategories>? = ArrayList()
        var editCollectionRequest : CollectionRequests? = null

        fun newInstance(
            materialCategories: ArrayList<MaterialCategories>?,
            collectionRequest: CollectionRequest?,
            editCollectionRequest: CollectionRequests?

        ): ConfirmCollectionFragment {
            this.collectionRequest = collectionRequest
            this.materialCategories = materialCategories
            this.editCollectionRequest = editCollectionRequest

            return ConfirmCollectionFragment()
        }
    }

    private var dependenciesListing: Dependencies? = null

    var spinnerAddresses: Spinner? = null
    var spinnerAddressesTitle: Spinner? = null

    var term_condition_check: CheckBox? = null
    var name: TextView? = null
    var category: TextView? = null
    var location: CustomEditText? = null
    var schedule_date: TextView? = null
    var phoneNumber: TextView? = null
    var nameHeading: TextView? = null
    var term_condition: TextView? = null
    val user = User.retrieveUser()
    var address_header: TextView? = null
    var txtAddTitle: TextView?= null

    var imgClose: ImageView?=null
    var img_contact_us: ImageView? = null
    var relUploadImage: RelativeLayout? = null
    private var optionsDialog: AlertDialog? = null
    private val requestCodeCameraPermission = 1001
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    var imageUri: Uri? = null
    private var photo_take_visibility: String =""

    var photoFile: File? = null
    var mCurrentPhotoPath: String? = null

    //    var question1: TextView? = null
//    var question2: TextView? = null
//    var groupQuestion1: RadioGroup? = null
//    var groupQuestion2: RadioGroup? = null
//    var question1Id: Int? = 0
//    var question2Id: Int? = 0
//    var question1Answer = "Yes"
//    var question2Answer = "Yes"
    var defaultAddress: Addresses? = null
    var scrollView: ScrollView? = null
    var positionAdress = 0

    var positionAdressTitle = 0


    var comments: CustomEditText? = null

    //Multiple IMAGES
    private var imageUris: ArrayList<Uri> = ArrayList()
    private var recyclerViewImages: RecyclerView? = null
    private var imagesAdapter: ImagesAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View? = inflater.inflate(R.layout.fragment_confirm_collection, container, false)
        if (RecycleFragment.stepView != null) {
            RecycleFragment.stepView!!.StepNumber(Constants.recycleStep3)
        }
        RecycleFragment.parentToChild = this
        initViews(view)
        setListeners()
        populateData(view)
        return view
    }

    private fun initViews(view: View?) {


        //Multiple Images Upload
        recyclerViewImages = view?.findViewById(R.id.recycler_view_images)
        imagesAdapter = ImagesAdapter(requireContext(), imageUris, this)
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewImages?.layoutManager = layoutManager
        recyclerViewImages?.adapter = imagesAdapter

        txtAddTitle = view?.findViewById(R.id.txtAddTitle)


        name = view?.findViewById(R.id.name)
        category = view?.findViewById(R.id.category)
        location = view?.findViewById(R.id.location)
        schedule_date = view?.findViewById(R.id.schedule_date)
        phoneNumber = view?.findViewById(R.id.phone_number)
        nameHeading = view?.findViewById(R.id.name_heading)
        spinnerAddresses = view?.findViewById(R.id.address_organization)
        spinnerAddresses!!.isClickable = false
        spinnerAddresses!!.isEnabled = false

        spinnerAddressesTitle= view?.findViewById(R.id.address_title_organization)



        scrollView = view?.findViewById(R.id.scrollView)
        comments = view?.findViewById(R.id.comments)
        term_condition_check = view?.findViewById(R.id.term_condition_check)
        term_condition = view?.findViewById(R.id.term_condition)
        address_header = view?.findViewById(R.id.address_header)

        img_contact_us = view?.findViewById(R.id.img_contact_us)
        relUploadImage = view?.findViewById(R.id.relUploadImage)
        imgClose = view?.findViewById(R.id.imgClose)

        if (MainApplication.userType() == Constants.UserType.household) {
            txtAddTitle?.visibility = View.GONE
            spinnerAddressesTitle?.visibility = View.GONE
        }else{
            txtAddTitle?.visibility = View.VISIBLE
            spinnerAddressesTitle?.visibility = View.VISIBLE
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        comments?.setOnTouchListener(OnTouchListener { v, event ->
            if (comments?.hasFocus()!!) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_SCROLL -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        return@OnTouchListener true
                    }
                }
            }
            false
        })
        location?.setOnClickListener(this)
        term_condition?.setOnClickListener(this)
        img_contact_us?.setOnClickListener(this)
//        imgClose?.setOnClickListener(this)


    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun populateData(view: View?) {
        Utils.setUnderLineText(term_condition, term_condition?.text.toString())
        if (!user?.addresses.isNullOrEmpty()) {
            defaultAddress = user?.addresses?.find { it.default == 1 }
            if (defaultAddress == null) {
                defaultAddress = user?.addresses?.find { it.default == 0 }
            }
        }
        phoneNumber?.text = user?.phone_number
        name?.text = "${user?.first_name} ${user?.last_name}"
        //-------------------Addresses  Spinner---------------------
        val hideArrow = user?.user_type != Constants.UserType.organization

        //Main Address =======================================
        val address = AdapterSpinnerAddress(
            R.layout.spinner_item_textview_drawable,
            user?.addresses,
            activity?.getDrawable(R.drawable.icon_address_location_un)!!, true
        )

        spinnerAddresses?.adapter = address
        spinnerAddresses?.visibility = View.VISIBLE
        address_header?.visibility = View.VISIBLE

        if (user?.user_type == Constants.UserType.organization) {
            nameHeading?.text = "Organization Name"
            name?.text = "${user.organization?.name}"
            if (RecycleFragment.changeAddressDistrictID == 0) {
                if (!user.addresses.isNullOrEmpty()) {
                    for (i in user.addresses?.indices!!) {
                        if (user.addresses!![i].default == 1) {
                            spinnerAddresses?.setSelection(i)
                            break
                        }
                    }
                }
            } else {
                if (!user.addresses.isNullOrEmpty()) {
                    for (i in user.addresses?.indices!!) {
                        if (user.addresses!![i].district_id == RecycleFragment.changeAddressDistrictID) {
                            spinnerAddresses?.setSelection(i)
                            break
                        }
                    }
                }
            }
            val callback: OnNetworkResponse = this
            spinnerAddresses?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    positionAdress = position
                    location?.setText(user.addresses?.get(position)?.location)
//                    location?.text = user.addresses?.get(position)?.location
                    NetworkCall.make()
                        ?.setCallback(callback)
                        ?.setTag(RequestCodes.API.ORDER_ACCEPTANCE_DAYS)
                        ?.autoLoading(activity!!)
                        ?.enque(Network().apis()?.orderAcceptanceDays(user.addresses?.get(position)?.district_id))
                        ?.execute()
                }

            }
        } else {
//            spinnerAddresses?.background?.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            spinnerAddresses?.isClickable = false
            spinnerAddresses?.isEnabled = false
            location?.setText(defaultAddress?.location)
        }

        //Address Title =======================================
        val addressTitle = AdapterSpinnerAddressTitle(
            R.layout.spinner_item_textview_drawable,
            user?.addresses,
            activity?.getDrawable(R.drawable.icon_address_location_un)!!, hideArrow
        )
        spinnerAddressesTitle?.adapter = addressTitle
        if (user?.user_type == Constants.UserType.organization) {
            nameHeading?.text = "Organization Name"
            name?.text = "${user.organization?.name}"
            if (RecycleFragment.changeAddressDistrictID == 0) {
                if (!user.addresses.isNullOrEmpty()) {
                    for (i in user.addresses?.indices!!) {
                        if (user.addresses!![i].default == 1) {
                            spinnerAddressesTitle?.setSelection(i)
                            spinnerAddresses?.setSelection(i)
                            break
                        }
                    }
                }
            } else {
                if (!user.addresses.isNullOrEmpty()) {
                    for (i in user.addresses?.indices!!) {
                        if (user.addresses!![i].district_id == RecycleFragment.changeAddressDistrictID) {
                            spinnerAddressesTitle?.setSelection(i)
                            spinnerAddresses?.setSelection(i)
                            break
                        }
                    }
                }
            }

            spinnerAddressesTitle?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    positionAdressTitle = position
                    spinnerAddresses?.setSelection(position)
                }
            }
        } else {
            spinnerAddressesTitle?.isClickable = false
            spinnerAddressesTitle?.isEnabled = false
            location?.setText(defaultAddress?.location)
        }

        val categoryName = StringBuilder()
        val categoryDescription = StringBuilder()
        Log.e("TAG","===categories===" +collectionRequest?.material_categories?.size!! )
        Log.e("TAG","===mcategories===" +materialCategories?.size!! )
        var prefix = ""
        for (i in 0 until collectionRequest?.material_categories?.size!!) {
            for (j in materialCategories!!.indices) {
                if (materialCategories?.get(j)?.id == collectionRequest?.material_categories!![i]?.id) {
                    Log.e("TAG","===name===" +materialCategories?.get(j)?.name )

                    categoryName.append(prefix)
                    categoryDescription.append(prefix)
                    categoryName.append(SelectCategoriesFragment.materialCategories?.get(j)?.name)
                    categoryDescription.append(SelectCategoriesFragment.materialCategories?.get(j)?.description)
                    prefix = ","
                    break
                }
            }
        }
        category?.text = categoryName.toString()
        schedule_date?.text =
            Utils.getFormattedDisplayDateCollection(collectionRequest?.collection_date)


        //EDIT COLLECTION SET DATA

        if(editCollectionRequest!=null){
            if(!editCollectionRequest!!.user_comments.isNullOrEmpty()){
                comments!!.setText(editCollectionRequest!!.user_comments)
            }

            if(!editCollectionRequest!!.request_images.isNullOrEmpty()){

                //SINGLE IMAGE
                /*if(!editCollectionRequest!!.request_images!!.get(0).image.isNullOrEmpty()){
                    imgClose!!.visibility = View.VISIBLE
                    Utils.glideImageLoaderServer(
                        img_contact_us,
                        editCollectionRequest!!.request_images!!.get(0).image, R.drawable.icon_placeholder_generic
                    )
                }*/

                //MULTIPLE IMAGES
                /*
                val tempArray: ArrayList<String> = ArrayList()
                for (i in 0 until editCollectionRequest!!.request_images!!.size){
                    if(!editCollectionRequest!!.request_images!!.get(i).image.isNullOrEmpty()){
                        tempArray.add(editCollectionRequest!!.request_images!!.get(i).image.toString())
                    }
                }
                imageUris.addAll(convertUrlsToUris(tempArray))
                imagesAdapter!!.notifyDataSetChanged()*/

            }
        }

        val tinyDB: TinyDB?
        tinyDB = TinyDB(MainApplication.applicationContext())
        photo_take_visibility = tinyDB.getString("photo_take_visibility").toString()

        if(photo_take_visibility.equals("VISIBLE")) { //VISIBLE
            relUploadImage!!.visibility = View.VISIBLE

        }else{
            relUploadImage!!.visibility = View.GONE
        }
    }

    fun convertUrlsToUris(imageUrls: List<String>?): ArrayList<Uri> {
        val uriList = ArrayList<Uri>()
        imageUrls?.forEach { url ->
            try {
                val uri = Uri.parse(url)
                uriList.add(uri)
            } catch (e: Exception) {
                // Handle potential parsing errors
                e.printStackTrace()
            }
        }
        return uriList
    }

    override fun callChild() {
        if (selectedDateAvailable) {
            if (term_condition_check?.isChecked!!) {
                if (user?.user_type == Constants.UserType.organization) {
                    collectionRequest?.city_id = user.addresses?.get(positionAdress)?.city_id
                    collectionRequest?.district_id = user.addresses?.get(positionAdress)?.district_id
                    collectionRequest?.latitude = user.addresses?.get(positionAdress)?.latitude
                    collectionRequest?.longitude = user.addresses?.get(positionAdress)?.longitude
                    collectionRequest?.map_location = location?.text.toString()
                    collectionRequest?.location =
                        user.addresses?.get(positionAdress)?.unit_number + ", " + user.addresses?.get(positionAdress)?.building_name + ", " +
                                user.addresses?.get(positionAdress)?.street + ", " + user.addresses?.get(positionAdress)?.district?.name + ", " + user.addresses?.get(positionAdress)?.city?.name
                } else {
                    collectionRequest?.city_id = defaultAddress?.city_id
                    collectionRequest?.district_id = defaultAddress?.district_id
                    collectionRequest?.latitude = defaultAddress?.latitude
                    collectionRequest?.longitude = defaultAddress?.longitude
                    collectionRequest?.map_location = location?.text.toString()
                    collectionRequest?.location = defaultAddress?.unit_number + ", " + defaultAddress?.building_name + ", " +
                         defaultAddress?.street + ", " + defaultAddress?.district?.name + ", " + defaultAddress?.city?.name
                }
                collectionRequest?.street = user?.addresses?.get(positionAdress)?.street
                collectionRequest?.first_name = user?.first_name
                collectionRequest?.last_name = user?.last_name
                collectionRequest?.organization_name = user?.organization?.name
                collectionRequest?.phone_number = user?.phone_number
                collectionRequest?.user_comments = comments?.text.toString()

                if(!user?.addresses.isNullOrEmpty()){
                    collectionRequest?.address_title = user!!.addresses?.get(positionAdressTitle)?.title.toString()
                }else{
                    collectionRequest?.address_title = ""
                }


                //new added for image upload feature in the collection request ONE IMAGE
                /*if(imageUri!=null){
                    collectionRequest!!.imageUri = imageUri
                }*/

                //MULTIPLE IMAGE UPLOAD
                if(imageUris.isNotEmpty()){
                    collectionRequest!!.imageUris = imageUris
                }

                if (childToParent != null) {
                    childToParent?.callParent(collectionRequest)
                }
            } else {
                Notify.alerterRed(activity, "Please Agree To Term and Conditions")
            }
        } else {
            Notify.alerterRed(activity, "Please Go Back And Select Date Again")
        }
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

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {
            RequestCodes.API.DEPENDENCIES -> {
                val baseResponseNew = Gson().fromJson(
                    Utils.jsonConverterObject(response.body() as LinkedTreeMap<*, *>),
                    BaseResponse::class.java
                )
                dependenciesListing = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponseNew.data as LinkedTreeMap<*, *>),
                    Dependencies::class.java
                )
            }
            RequestCodes.API.ORDER_ACCEPTANCE_DAYS -> {
                val districtDetail = Gson().fromJson(
                    Utils.jsonConverterObject(baseResponse?.data as LinkedTreeMap<*, *>),
                    DistrictWithOrder::class.java
                )
                val collectionDay = Utils.getDayFromDate(collectionRequest?.collection_date)
                if (!districtDetail?.order_acceptance_days.isNullOrEmpty()) {
                    for (i in districtDetail?.order_acceptance_days!!.indices) {
                        if (collectionDay?.equals(districtDetail.order_acceptance_days[i], true)!!
                            || districtDetail.order_acceptance_days[i].equals("All", true))
                        {
                            selectedDateAvailable = true
                            break
                        } else {
                            selectedDateAvailable = false
                        }
                    }
                }
                if (!selectedDateAvailable) {
                    Notify.alerterRed(activity, "Please Go Back And Select Date Again")
                }
                RecycleFragment.changeAddressDistrictID = user?.addresses?.get(positionAdress)?.district_id
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    //CAMERA

                    //MULTI CHANGE
//                    imgClose!!.visibility = View.VISIBLE

                    processCapturedImage(requireContext(),photoFile!!.absolutePath)

                }
                REQUEST_IMAGE_PICK -> {
                    // GALLERY

                    //MULTI CHANGE
//                    imgClose!!.visibility = View.VISIBLE

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


    fun processCapturedImage(context: Context, imagePath: String) {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(imagePath, options)

        val rotatedBitmap = rotateImageIfRequired(context, bitmap, imagePath)

        //MULTI CHANGE
//        img_contact_us!!.setImageBitmap(rotatedBitmap)

//        imageUri = getImageUri(requireActivity(),rotatedBitmap)

        fragmentScope.launch {
            imageUri = getImageUri(requireActivity(), rotatedBitmap)
            if (imageUri != null) {
                // Handle the Uri
                Log.d("ImageUri", "Image URI: $imageUri")
                //NEW FOR MULTIPLE IMAGE UPLOAD MULTI
                imageUris.add(imageUri!!)
                imagesAdapter!!.notifyDataSetChanged()
            } else {
                Log.e("ImageUri", "Failed to get image URI")
            }
        }




        //NEW FOR MULTIPLE IMAGE UPLOAD MULTI



    }


    //OLD
    /*private fun getImageUri(inContext: Activity, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }*/



    /*suspend fun getImageUri(inContext: Activity, inImage: Bitmap): Uri? {

        val uniqueFileName = "image_${System.currentTimeMillis()}.jpg"

        val imageFile = File(inContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), uniqueFileName)

        try {
            FileOutputStream(imageFile).use { out ->
                inImage.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        // Print original file size
        val originalFileSize = imageFile.length()
        Log.d("ImageCompression", "Original file size: $originalFileSize bytes")

        return withContext(Dispatchers.IO) {
            try {
                val compressedImageFile = Compressor.compress(inContext, imageFile) {
                    resolution(1280, 720)
                    quality(50)
                    format(Bitmap.CompressFormat.JPEG)
                }

                // Print compressed file size
                val compressedFileSize = compressedImageFile.length()
                Log.d("ImageCompression", "Compressed file size: $compressedFileSize bytes")

                // Convert compressed image file to Uri
                val path = MediaStore.Images.Media.insertImage(
                    inContext.contentResolver,
                    BitmapFactory.decodeFile(compressedImageFile.absolutePath),
                    "image_${System.currentTimeMillis()}",
                    null
                )

                Uri.parse(path)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }*/

    suspend fun getImageUri(inContext: Activity, inImage: Bitmap): Uri? {

        val uniqueFileName = "image_${System.currentTimeMillis()}.jpg"
        val imageFile = File(inContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), uniqueFileName)

        try {
            FileOutputStream(imageFile).use { out ->
                inImage.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        // Print original file size
        val originalFileSize = imageFile.length()
        Log.d("ImageCompression", "Original file size: $originalFileSize bytes")

        return withContext(Dispatchers.IO) {
            try {
                val finalImageFile = if (originalFileSize > 500 * 1024) {
                    Log.d("ImageCompression", "File size is greater than 500KB, compressing...")

                    Compressor.compress(inContext, imageFile) {
                        resolution(1280, 720)
                        quality(50)
                        format(Bitmap.CompressFormat.JPEG)
                    }
                } else {
                    Log.d("ImageCompression", "File size is less than or equal to 500KB, using original file")
                    imageFile
                }

                // Print final file size
                val finalFileSize = finalImageFile.length()
                Log.d("ImageCompression", "Final file size: $finalFileSize bytes")

                // Convert final image file to Uri
                val path = MediaStore.Images.Media.insertImage(
                    inContext.contentResolver,
                    BitmapFactory.decodeFile(finalImageFile.absolutePath),
                    "image_${System.currentTimeMillis()}",
                    null
                )

                Uri.parse(path)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.location -> {
                val intent = Intent(activity, MapsActivity::class.java)
                val bundle = Bundle()
                if (user?.user_type == Constants.UserType.organization) {
                    bundle.putDouble(Constants.DataConstants.latitude, user.addresses?.get(positionAdress)?.latitude!!)
                    bundle.putDouble(Constants.DataConstants.longitude, user.addresses?.get(positionAdress)?.longitude!!)
                } else {
                    bundle.putDouble(Constants.DataConstants.latitude, defaultAddress?.latitude!!)
                    bundle.putDouble(Constants.DataConstants.longitude, defaultAddress?.longitude!!)
                }
                bundle.putInt(Constants.DataConstants.removeSaveButton, 1)
                intent.putExtra(Constants.DataConstants.bundle, bundle)
                startActivity(intent)
            }
            R.id.term_condition -> {
                BaseActivity.replaceFragment(
                    activity?.supportFragmentManager!!,
                    Constants.Containers.recycleRequestParent,
                    TermConditionFragment.newInstance(),
                    Constants.TAGS.TermConditionFragment)
                HomeActivity.openTermCondition = true
            }

            //MULTI CHANGE
            /*R.id.imgClose -> {
                imgClose!!.visibility = View.GONE
                img_contact_us!!.setImageDrawable(resources.getDrawable(R.drawable.upload_image))
                imageUri = null
            }*/

            R.id.img_contact_us -> {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED ) {
                        askForCameraPermission();
                    } else {

                        //FOR CAMERA AND GALLERY
                        //showOptionsDialog()

                        //FOR CAMERA ONLY
                        dispatchTakePictureIntent()


                    }
                }else{
                    if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ) {
                        askForCameraPermission()
                    } else {

                        //FOR CAMERA AND GALLERY
                        //showOptionsDialog()

                        //FOR CAMERA ONLY
                        dispatchTakePictureIntent()
                    }
                }
            }
        }
    }



    /*private fun removeImage(position: Int) {
        imageUris.removeAt(position)
        imagesAdapter!!.notifyItemRemoved(position)
    }*/

    override fun onRemoveImage(position: Int) {
        imageUris.removeAt(position)
        imagesAdapter!!.notifyDataSetChanged()
    }


}
