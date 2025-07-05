package com.android.reloop.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.location.Location
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.adapters.CaptureBarcodeAdapter
import com.android.reloop.adapters.CaptureImageAdapter
import com.android.reloop.model.BarcodeModel
import com.android.reloop.utils.Configuration
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.fragments.BaseFragment
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.tinydb.TinyDB
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import kotlinx.android.synthetic.main.fragment_continue_as.*
import kotlinx.android.synthetic.main.fragment_product_detail.*
import kotlinx.android.synthetic.main.fragment_product_detail.rlbarcodep
import kotlinx.android.synthetic.main.fragment_verify_drop_off.*
import org.json.JSONArray
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class VerifyDropOffFragment : BaseFragment(),View.OnClickListener, BarcodeCallback,
    CaptureBarcodeAdapter.ClickListenerBarcode,CaptureImageAdapter.ClickListenerImage {

    private lateinit var dialog: Dialog

    var img_step3: ImageView? = null
    var text_step3: TextView?= null

    var back: Button? = null
    var btnNext:Button? = null

    var locationAdd: TextView?=null
    var btnVerify: TextView?=null

    var rlLocationLayout: RelativeLayout?=null
    var rlPhotoLayout: RelativeLayout?=null
    var rlBarcodeLayout: RelativeLayout?=null

    //image
    var txtImageSelection: TextView ?= null
    val REQUEST_CODE = 200
    var rcv_capture_images: RecyclerView?=null

    var imageList: ArrayList<String> = ArrayList()
    var imageDataAdapter: CaptureImageAdapter? = null



    //barcode
    var txtBarcode : TextView?= null
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private var code: String = ""
    var rcv_barcode: RecyclerView ?= null
    var ivClose: ImageView?=null

    var barcodeAdapter: CaptureBarcodeAdapter? = null
    var barcodeList: ArrayList<BarcodeModel> = ArrayList()

    var tinyDB: TinyDB?=null
    var isLocationVerified : Boolean = false

    //radius
    private var radius_value: String = ""

    companion object {
        var selectedMaterialCategories: ArrayList<MaterialCategories>? = null

        fun newInstance(bundle: ArrayList<MaterialCategories>?): VerifyDropOffFragment {
            this.selectedMaterialCategories = bundle

            return VerifyDropOffFragment()
        }
        var TAG = "DropOffPin"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view: View? = inflater.inflate(R.layout.fragment_verify_drop_off, container, false)

        tinyDB = TinyDB(MainApplication.applicationContext())

        initViews(view)
        setListeners()
        settingsAPIforRadius()

        /*Check if allowed LOCATION,PHOTO,BARCODE*/
        val req_location = tinyDB!!.getString("DROP_REQUIRE_LOCATION").toString()
        val req_photo = tinyDB!!.getString("DROP_REQUIRE_PHOTO").toString()
        val req_barcode = tinyDB!!.getString("DROP_REQUIRE_BARCODE").toString()

        Log.d("CONDITIONS_","VERIFY : "+req_location + req_photo + req_barcode)


        if(req_location.equals("0")){
            rlLocationLayout!!.visibility = View.GONE
        }
        if(req_photo.equals("0")){
            rlPhotoLayout!!.visibility = View.GONE
        }
        if(req_barcode.equals("0")){
            rlBarcodeLayout!!.visibility = View.GONE
        }

        rcv_capture_images!!.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )

        imageDataAdapter = CaptureImageAdapter(imageList,requireContext(),this)
        rcv_capture_images!!.adapter = imageDataAdapter


        rcv_barcode!!.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )

        barcodeAdapter = CaptureBarcodeAdapter(barcodeList,requireContext(),this)
        rcv_barcode!!.adapter = barcodeAdapter


        return view
    }

    private fun setListeners() {
        back?.setOnClickListener(this)
        btnNext?.setOnClickListener(this)
        txtBarcode?.setOnClickListener(this)
        locationAdd?.setOnClickListener(this)
        txtImageSelection?.setOnClickListener(this)
        ivClose?.setOnClickListener(this)
        btnVerify?.setOnClickListener(this)

        img_step3?.setImageResource(R.drawable.ic_step_verify_clicked)
        text_step3?.setTextColor(requireActivity().getColor(R.color.green_color_button))


    }

    private fun initViews(view: View?) {
        img_step3 = view?.findViewById(R.id.img_step3)
        text_step3 = view?.findViewById(R.id.text_step3)

        rlLocationLayout= view?.findViewById(R.id.rlLocationLayout)
        rlPhotoLayout= view?.findViewById(R.id.rlPhotoLayout)
        rlBarcodeLayout= view?.findViewById(R.id.rlBarcodeLayout)

        back = view?.findViewById(R.id.back)
        btnVerify = view?.findViewById(R.id.btnVerify)
        btnNext = view?.findViewById(R.id.btnNext)
        rcv_capture_images = view?.findViewById(R.id.rcv_capture_images)
        rcv_barcode = view?.findViewById(R.id.rcv_barcode)

        locationAdd = view?.findViewById(R.id.locationAdd)
        txtBarcode = view?.findViewById(R.id.txtBarcode)
        txtImageSelection = view?.findViewById(R.id.txtImageSelection)

        ivClose = view?.findViewById(R.id.ivClose)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.back -> {

                //new added
                if(rlbarcodep.visibility == View.VISIBLE){
                    barcodeScannerDropOff.pause()
                    rlbarcodep.visibility = View.GONE
                }else{
                    activity?.onBackPressed()
                }

                //original
//                activity?.onBackPressed()

            }
            R.id.btnVerify -> {
//NEW here
                val dropLocationName = tinyDB!!.getString("DROP_LOCATION").toString()


                val drop_lat = tinyDB!!.getString("DROP_LATITUDE").toString()
                val drop_long = tinyDB!!.getString("DROP_LONGITUDE").toString()

                val current_lat = tinyDB!!.getString("DROP_CURRENT_LAT").toString()
                val current_long = tinyDB!!.getString("DROP_CURRENT_LONG").toString()

                try {
                    val dist = FloatArray(1)

                    Location.distanceBetween(
                        drop_lat.toDouble(),
                        drop_long.toDouble(),
                        current_lat.toDouble(),
                        current_long.toDouble(),
                        dist
                    )

                    Log.d("RADIUS_VAL",""+radius_value)

                    if(!radius_value.equals("")){
                        if (dist[0] / radius_value.toInt() > 1) { // 50 meter radius... 1000 meter is 1 km
                            //user is outside of radius
                            Notify.alerterRed(activity, "You are outside "+radius_value+" meter radius, get closer to the drop-off point and try again")
                            isLocationVerified = false
                            locationAdd!!.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittextbackgroundselected_rect_red));
                        }else{
                            //user is inside radius
                            isLocationVerified = true
                            Notify.alerterGreen(activity, "Location verified successfully!")
                            locationAdd!!.setText(dropLocationName)
                            locationAdd!!.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittextbackgroundselected_rect));
                        }
                    }else{
                        Notify.alerterRed(activity, "Radius not found")
                    }


                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            R.id.btnNext -> {

                val req_location = tinyDB!!.getString("DROP_REQUIRE_LOCATION").toString()
                val req_photo = tinyDB!!.getString("DROP_REQUIRE_PHOTO").toString()
                val req_barcode = tinyDB!!.getString("DROP_REQUIRE_BARCODE").toString()


                if(req_location.equals("1") && !isLocationVerified){
                    Notify.alerterRed(activity, "Please verify Location")
                }else if (req_photo.equals("1") && imageList.isEmpty()){
                    Notify.alerterRed(activity, "Please add material photo")
                }else if(req_barcode.equals("1") && barcodeList.isEmpty()){
                    Notify.alerterRed(activity, "Please scan barcode")
                }else{
                    BaseActivity.replaceFragment(childFragmentManager, Constants.Containers.verifyDropOffContainer,
                        ConfirmSelectionsFragment.newInstance(selectedMaterialCategories,imageList,barcodeList),
                        Constants.TAGS.ConfirmSelectionsFragment)
                }
            }
            R.id.locationAdd -> {
//OLD here
                /*val dropLocationName = tinyDB!!.getString("DROP_LOCATION").toString()


                val drop_lat = tinyDB!!.getString("DROP_LATITUDE").toString()
                val drop_long = tinyDB!!.getString("DROP_LONGITUDE").toString()

                val current_lat = tinyDB!!.getString("DROP_CURRENT_LAT").toString()
                val current_long = tinyDB!!.getString("DROP_CURRENT_LONG").toString()

                try {
                    val dist = FloatArray(1)

                    Location.distanceBetween(
                        drop_lat.toDouble(),
                        drop_long.toDouble(),
                        current_lat.toDouble(),
                        current_long.toDouble(),
                        dist
                    )

                    if (dist[0] / 50 > 1) { // 50 meter radius... 1000 meter is 1 km
                        //user is outside of radius
                        Notify.alerterRed(activity, "You are outside 50 meter radius, get closer to the drop-off point and try again")
                        isLocationVerified = false
                        locationAdd!!.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittextbackgroundselected_rect_red));
                    }else{
                        //user is inside radius
                        isLocationVerified = true
                        Notify.alerterGreen(activity, "Location verified successfully!")
                        locationAdd!!.setText(dropLocationName)
                        locationAdd!!.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittextbackgroundselected_rect));
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }*/



            }
            R.id.txtBarcode -> {
                if (ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForCameraPermission()
                } else {
                    //original
                    rlbarcodep?.visibility = View.VISIBLE
                    setupControls()
                }
            }

            R.id.ivClose -> {

                barcodeScannerDropOff.pause()
                rlbarcodep?.visibility = View.GONE

            }

            R.id.txtImageSelection -> {

                //OLD
                /*if (ContextCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
                    askForCameraPermission()
                } else {
                    takePhotos()
                }*/


                //NEW

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                        askForCameraPermission();
                    } else {
                        pickImage(REQUEST_CODE)

                    }
                }else{
                    if (ContextCompat.checkSelfPermission(requireContext(),
                            android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ) {
                        askForCameraPermission()
                    } else {
                        takePhotos()
                    }
                }
            }
        }

    }

    private fun showProgressDialog() {
        dialog = Dialog(requireContext())
        if (dialog.window != null) {
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.progress_dialog_layout)
            dialog.show()
        }
    }

    private fun dismissProgressDialog() {
        dialog.dismiss()
    }

    private fun settingsAPIforRadius() {
        showProgressDialog()
        var baseURL = ""
        baseURL = if (Configuration.isProduction) {
            "https://api.reloopapp.com/api/settings";
        } else if (Configuration.isStagingNew) {
//            "https://staging.reloopapp.com/api/settings";
            "https://dev.reloopapp.com/api/settings";
        } else {
            "http://reloop.teamtechverx.com/api/settings";
        }

        AndroidNetworking.get(baseURL)
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    dismissProgressDialog()
                    try {
                        Log.d("==>>", "onResponse: $response")
                        val data: JSONObject = response.getJSONObject("data")

                        val dropoff_location_radius: JSONArray = data.getJSONArray("dropoff_location_radius")

                        radius_value = dropoff_location_radius.getJSONObject(0).getString("value")

                    } catch (e: java.lang.Exception) {
                        dismissProgressDialog()
                        Log.d("==>>", "onResponse: " + e.message)
                    }
                }

                override fun onError(error: ANError) {
                    dismissProgressDialog()
                    Log.d("==>>", "onError: " + error.message)
                }
            })
    }

    private fun takePhotos() {
        if (hasStoragePermission()) {
            pickImage(REQUEST_CODE)
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.write_external_storage),
                RequestCodes.RC_STORAGE_PERM,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun pickImage(code: Int) {
        val pictureIntent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )

        var photoFile: File? = null
        try {
            photoFile = createImageFile()
        } catch (ex: IOException) {
        }
        if (photoFile != null) {
            Log.e("CAMERA", "photoFile=====$photoFile")
            val photoURI = FileProvider.getUriForFile(
                requireActivity(),
                requireActivity().packageName + ".fileprovider",
                photoFile
            )
            pictureIntent.putExtra(
                MediaStore.EXTRA_OUTPUT,
                photoURI
            )
            startActivityForResult(
                pictureIntent,
                code
            )
        }

    }

    var imageFilePath: String? = null

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) //original
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        imageFilePath = image.absolutePath
        return image
    }

    private fun hasStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        Log.e("CAMERA", "imageFilePath=====$imageFilePath")
                        val gpxfile = File(imageFilePath)

                        val size = gpxfile.length().toInt()
                        val bytes1 = ByteArray(size)
                        val buf = BufferedInputStream(FileInputStream(gpxfile))
                        buf.read(bytes1, 0, bytes1.size)
                        buf.close()




//                        val file = File(imageFilePath)
//                        val file_size: Int = java.lang.String.valueOf(file.length() / 1024).toInt()
                        //Log.e("FILE_CHECK", "OLD SIZE" + file_size)

                        var compressImgFile: File? = null
                        compressImgFile = compressImage(File(imageFilePath))

                        //val newFile: Int = java.lang.String.valueOf(compressImgFile!!.length() / 1024).toInt()
                        //Log.e("FILE_CHECK", "NEW SIZE" + newFile)

                        rcv_capture_images!!.visibility = View.VISIBLE
                        imageList.add(compressImgFile!!.absolutePath)
                        imageDataAdapter!!.notifyDataSetChanged()

                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                }
            }
        }





    }


    //compress image
    fun compressImage(file: File): File? {
        return try {

            // BitmapFactory options to downsize the image
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            o.inSampleSize = 6
            // factor of downsizing the image
            var inputStream = FileInputStream(file)
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o)
            inputStream.close()

            // The new size we want to scale to
            val REQUIRED_SIZE = 75

            // Find the correct scale value. It should be the power of 2.
            var scale = 1
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                o.outHeight / scale / 2 >= REQUIRED_SIZE
            ) {
                scale *= 2
            }
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            inputStream = FileInputStream(file)
            val selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2)
            inputStream.close()

            // here i override the original image file
            file.createNewFile()
            val outputStream = FileOutputStream(file)
            selectedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            file
        } catch (e: java.lang.Exception) {
            null
        }
    }


    //barcode scanner

    private fun askForCameraPermission() {
        /*ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )*/


        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), requestCodeCameraPermission);

    }


    private fun showBarcodeScanner() {
        barcodeScannerDropOff!!.decodeContinuous(this)
        val settings: CameraSettings = barcodeScannerDropOff!!.getBarcodeView().getCameraSettings()

        /*if (barcodeScannerDropOff!!.getBarcodeView().isPreviewActive()) {
            Log.d("BARCODE_ZXING","111")
            barcodeScannerDropOff!!.pause()
        }else{
            Log.d("BARCODE_ZXING","222")
            barcodeScannerDropOff!!.resume()
        }*/

        barcodeScannerDropOff!!.resume()



        //swap the id of the camera to be used
        settings.requestedCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        barcodeScannerDropOff!!.getBarcodeView().setCameraSettings(settings)
    }

    private fun setupControls() {

        /*if (BuildConfig.DEBUG) {
            showBarcodeScanner()
            return
        }*/

        showBarcodeScanner()

        /*barcodeDetector = BarcodeDetector.Builder(context).build()
        var barcodeFactory = BarcodeTrackerFactory
        barcodeDetector.setProcessor(MultiProcessor.Builder<Barcode>(barcodeFactory).build())

        cameraSource = CameraSource.Builder(context, barcodeDetector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .build()


        cameraSurfaceView?.getHolder()?.addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {

                    Log.d("SCANNER_BAR", "created")

                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    Log.d("SCANNER_BAR", "created exception")
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int,
            ) {
                try {
                    Log.d("SCANNER_BAR", "surface changed")

                    cameraSource.start(holder)
                } catch (e: IOException) {
                    Log.d("SCANNER_BAR", "surface change exception ")

                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                Log.d("SCANNER_BAR", "surface destroyed")

                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(requireContext(), "Scanner has been closed", Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue

                    //Don't forget to add this line printing value or finishing activity must run on main thread
                    requireActivity().runOnUiThread {
                        cameraSource.stop()
                        Log.e("TAG", "value- $scannedValue")

                    }
                } else {
                    Log.e("TAG", "value- else")
                }
            }
        })*/
    }

    private fun playSound() {
        val toneGen1 = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen1.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    override fun barcodeResult(result: BarcodeResult?) {
        Log.v("Barcode result:", result!!.text) // Prints scan results

        Log.v("Barcode result:", result.barcodeFormat.toString()) // Prints the scan format (qrcode, pdf417 etc.)



        if (result!= null && !code.equals(result.text)) {

            //show data in recyclerview
            try {
                var barcodeModel = BarcodeModel()
                barcodeModel.imgBitmap = result.bitmap
                barcodeModel.barcodeResult = result.text
                rcv_barcode!!.visibility = View.VISIBLE
                barcodeList.add(barcodeModel)
                barcodeAdapter!!.notifyDataSetChanged()

            }catch (e: Exception){
                e.printStackTrace()
            }


            playSound()

            try {
                Notify.alerterGreen(activity, result!!.text)
            }catch (e: Exception){
                e.printStackTrace()
            }

            Log.d(TAG, "barcodeResult: called " + result.text)
            code = result.text


            rlbarcodep?.visibility = View.GONE
            barcodeScannerDropOff!!.pause()

        }
    }

    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
    }

    override fun onDeleteClick(position: Int) {
        barcodeList.removeAt(position)
        barcodeAdapter!!.notifyDataSetChanged()
    }

    override fun onDeleteClickImage(position: Int) {
        imageList.removeAt(position)
        imageDataAdapter!!.notifyDataSetChanged()
    }


}