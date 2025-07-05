package com.reloop.reloop.fragments


import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.network.serializer.dashboard.Dashboard
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.internal.LinkedTreeMap
import com.reloop.reloop.R
import com.reloop.reloop.activities.BaseActivity
import com.reloop.reloop.adapters.AdapterPDFMaterials
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.model.ModelHomeCategories
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.reports.PieChart
import com.reloop.reloop.network.serializer.reports.Reports
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import kotlinx.android.synthetic.main.fragment_term_condition.view.back
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * A simple [Fragment] subclass.
 */
class PdfDownloadFragment : BaseFragment(), OnNetworkResponse, View.OnClickListener {

    companion object {
        private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 123

        var reportData: Reports? = null
        var envDataList: ArrayList<ModelHomeCategories> = ArrayList()
        var pieChart: PieChart?=null
        var orgLogo: String?=null
        var selectedFilterName: String?=null
        var timePeriod: String?=null
        var totalKgs: String?=null

        fun newInstance(reports: Reports?,dataList: ArrayList<ModelHomeCategories>,pieChart: PieChart,orgLogo: String?,
                        selectedFilter: String, selectedTime: String,totalKgsData: String): PdfDownloadFragment {
            this.reportData = reports
            this.envDataList = dataList
            this.pieChart = pieChart
            this.orgLogo = orgLogo
            this.selectedFilterName = selectedFilter
            this.timePeriod = selectedTime
            this.totalKgs = totalKgsData

            return PdfDownloadFragment()
        }
    }

    var view_: View? = null

    var scrollViewPDF: ScrollView?=null
    var btnDownloadPDF: Button?=null
    var imgOrganization: ImageView?=null

    var txtDatePDF: TextView?=null

    //Environmental STATS
    var txtCo2Emissions: TextView ?= null
    var txtWater: TextView ?= null
    var txtElectricity: TextView ?= null
    var txtFarmingLand: TextView ?= null
    var txtLandfillSpace: TextView ?= null
    var txtTrees: TextView ?= null
    var txtOil: TextView ?= null
    var txtComposs: TextView ?= null

    //Waste Recycled STATIC CATEGORY
    /*var txtPiePlastic: TextView?=null
    var txtPieElectronics: TextView?=null
    var txtPieMetals: TextView?=null
    var txtPieGlass: TextView?=null
    var txtPieClothes: TextView?=null
    var txtPiePaper: TextView?=null
    var txtPieFood: TextView?=null
    var txtPieOil: TextView?=null*/

    //Total Waste recycled
    var txtTotalWasteRecycled : TextView?=null

    var linearLayoutFirst: LinearLayout?=null
    var linearLayoutSecond: LinearLayout?=null

    //Certificate Name
    var txtNamePDF: TextView?=null

    //Recyclerview (Waste Recycled DYNAMIC CATEGORY)
    var rvPDFMaterials: RecyclerView?=null
    private lateinit var linearLayoutManager: GridLayoutManager
    val maxSpanCount = 6
    lateinit var adapterPDFMaterial: AdapterPDFMaterials
    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        view_ = inflater.inflate(R.layout.fragment_pdf_download, container, false)

        scrollViewPDF = view_?.findViewById(R.id.scrollViewPDF)
        btnDownloadPDF = view_?.findViewById(R.id.btnDownloadPDF)
        txtDatePDF = view_?.findViewById(R.id.txtDatePDF)
        imgOrganization = view_?.findViewById(R.id.imgOrganization)

        txtCo2Emissions = view_?.findViewById(R.id.txtCo2Emissions)
        txtWater = view_?.findViewById(R.id.txtWater)
        txtElectricity = view_?.findViewById(R.id.txtElectricity)
        txtFarmingLand = view_?.findViewById(R.id.txtFarmingLand)
        txtLandfillSpace = view_?.findViewById(R.id.txtLandfillSpace)
        txtTrees = view_?.findViewById(R.id.txtTrees)
        txtOil = view_?.findViewById(R.id.txtOil)
        txtComposs = view_?.findViewById(R.id.txtComposs)

        /*txtPiePlastic = view_?.findViewById(R.id.txtPiePlastic)
        txtPieElectronics= view_?.findViewById(R.id.txtPieElectronics)
        txtPieMetals= view_?.findViewById(R.id.txtPieMetals)
        txtPieGlass= view_?.findViewById(R.id.txtPieGlass)
        txtPieClothes= view_?.findViewById(R.id.txtPieClothes)
        txtPiePaper= view_?.findViewById(R.id.txtPiePaper)
        txtPieFood= view_?.findViewById(R.id.txtPieFood)
        txtPieOil= view_?.findViewById(R.id.txtPieOil)*/

        txtTotalWasteRecycled = view_?.findViewById(R.id.txtTotalWasteRecycled)
        linearLayoutFirst = view_!!.findViewById<LinearLayout>(R.id.llFirstRow)
        linearLayoutSecond = view_!!.findViewById<LinearLayout>(R.id.llSecondRow)

        txtNamePDF = view_?.findViewById(R.id.txtNamePDF)

        rvPDFMaterials = view_?.findViewById(R.id.rvPDFMaterials)

        populateData()
//        populateRecyclerViewData()

        if(pieChart!!.pieChartLabels.size<=6){
            populateLinearLayoutData()
        }else{
            populateLinearLayoutData2()
        }

        dialog = Dialog(requireActivity())

        setListeners()

        getDashboard()

        return view_
    }


    private fun getDashboard() {
        NetworkCall.make()
            ?.setCallback(this)
            ?.setTag(RequestCodes.API.DASHBOARD)
            //?.autoLoading(requireActivity())
            ?.enque(Network().apis()?.dashboard())
            ?.execute()
    }

    private fun setListeners() {
        view_?.back?.setOnClickListener(this)

        btnDownloadPDF!!.setOnClickListener {
            /*if (checkPermission()) {
                generatePDF()
            } else {
                requestPermission()
            }*/
            generatePDF()

        }
    }

    private fun populateLinearLayoutData(){

        val inflater = LayoutInflater.from(requireContext())

        for (item in pieChart!!.pieChartLabels.indices) {
            val itemView = inflater.inflate(R.layout.item_pdf_materials, null)

            val imgMaterial: ImageView? = itemView.findViewById(R.id.imgMaterial)
            val materialValue: TextView? = itemView.findViewById(R.id.materialValue)
            val materialName: TextView?= itemView.findViewById(R.id.materialName)

            try {
                materialName?.text = pieChart!!.pieChartLabels[item].toString()

                materialValue!!.setText(""+ pieChart!!.pieChartValues.get(item).let { Utils.commaConversion(it) }
                        + " " +
                        getUnitFromValue(pieChart!!.pieChartUnits.get(item)))

                Utils.glideImageLoaderServer(
                    imgMaterial,
                    pieChart!!.pieChartIcons.get(item),
                    R.drawable.icon_placeholder_generic
                )

            }catch (e: Exception){
                e.printStackTrace()
            }


            linearLayoutFirst!!.addView(itemView)
        }
    }

    private fun populateLinearLayoutData2(){

        val pieChartLabelsFirst = pieChart!!.pieChartLabels.subList(0, 6)
        val pieChartValuesFirst = pieChart!!.pieChartValues.subList(0, 6)
        val pieChartUnitsFirst = pieChart!!.pieChartUnits.subList(0, 6)
        val pieChartIconsFirst = pieChart!!.pieChartIcons.subList(0, 6)

        val pieChartLabels = pieChart!!.pieChartLabels.subList(6, pieChart!!.pieChartLabels.size)
        val pieChartValues = pieChart!!.pieChartValues.subList(6, pieChart!!.pieChartValues.size)
        val pieChartUnits = pieChart!!.pieChartUnits.subList(6, pieChart!!.pieChartUnits.size)
        val pieChartIcons = pieChart!!.pieChartIcons.subList(6, pieChart!!.pieChartIcons.size)

        val inflater = LayoutInflater.from(requireContext())

        //FIRST LINEAR LAYOUT
        for (item in pieChartLabelsFirst.indices) {
            val itemView = inflater.inflate(R.layout.item_pdf_materials, null)

            val imgMaterial: ImageView? = itemView.findViewById(R.id.imgMaterial)
            val materialValue: TextView? = itemView.findViewById(R.id.materialValue)
            val materialName: TextView?= itemView.findViewById(R.id.materialName)

            try {
                materialName?.text = pieChartLabelsFirst[item].toString()

                materialValue!!.setText(""+ pieChartValuesFirst.get(item).let { Utils.commaConversion(it) } + " " +
                        getUnitFromValue(pieChartUnitsFirst.get(item)))

                Utils.glideImageLoaderServer(imgMaterial, pieChartIconsFirst.get(item), R.drawable.icon_placeholder_generic)

            }catch (e: Exception){
                e.printStackTrace()
            }
            linearLayoutFirst!!.addView(itemView)
        }

        //SECOND LINEAR LAYOUT
        for (item in pieChartLabels.indices) {
            val itemView = inflater.inflate(R.layout.item_pdf_materials, null)

            val imgMaterial: ImageView? = itemView.findViewById(R.id.imgMaterial)
            val materialValue: TextView? = itemView.findViewById(R.id.materialValue)
            val materialName: TextView?= itemView.findViewById(R.id.materialName)

            try {
                materialName?.text = pieChartLabels[item].toString()

                materialValue!!.setText(""+ pieChartValues.get(item).let { Utils.commaConversion(it) }
                        + " " +
                        getUnitFromValue(pieChartUnits.get(item)))

                Utils.glideImageLoaderServer(
                    imgMaterial,
                    pieChartIcons.get(item),
                    R.drawable.icon_placeholder_generic
                )

            }catch (e: Exception){
                e.printStackTrace()
            }
            linearLayoutSecond!!.addView(itemView)
        }
    }

    private fun populateRecyclerViewData(){
        if(pieChart != null){
            adapterPDFMaterial = AdapterPDFMaterials(
                pieChart!!.pieChartLabels,
                pieChart!!.pieChartValues,
                pieChart!!.pieChartUnits,
                pieChart!!.pieChartIcons,
                activity)

            //ORIGINAL
            linearLayoutManager = GridLayoutManager(context, 6)

            linearLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val spanSize = 1
                    val totalItemCount: Int = rvPDFMaterials!!.adapter!!.itemCount
                    val spanCount: Int = linearLayoutManager.spanCount
                    val rows = Math.ceil(totalItemCount.toDouble() / spanCount).toInt()
                    val currentRow = position / spanCount
                    val itemsInCurrentRow =
                        if (currentRow == rows - 1) totalItemCount % spanCount else spanCount

                    Log.d("Items in Row", "Items in current row: $itemsInCurrentRow")

                    if(itemsInCurrentRow==1){
                        return 6
                    }else if (itemsInCurrentRow==2){
                        return 3
                    }else if (itemsInCurrentRow==3){
                        return 2
                    }else if (itemsInCurrentRow==4){
                        itemResizeRecyclerview()
                        return 1
                    }else if (itemsInCurrentRow==5){
                        itemResizeRecyclerview()
                        return 1
                    }

                    return spanSize;
                }
            }

            rvPDFMaterials?.layoutManager = linearLayoutManager
            rvPDFMaterials!!.adapter = adapterPDFMaterial
        }
    }

    private fun itemResizeRecyclerview(){

        val totalItemCount: Int = linearLayoutManager.getItemCount()
        val fullRows: Int = totalItemCount / 6
        val itemsInLastRow: Int = totalItemCount % 6
        if (itemsInLastRow == 5 || itemsInLastRow == 4) {
            linearLayoutManager.setSpanCount(itemsInLastRow)
            linearLayoutManager.requestLayout() // Update layout
        } else {
            linearLayoutManager.setSpanCount(6)
            linearLayoutManager.requestLayout() // Update layout
        }
    }


    private fun populateData() {
        try {
            if(reportData!=null){

                if(timePeriod.isNullOrEmpty()){
                    txtDatePDF!!.setText(reportData!!.header.heading)
                }else{
                    txtDatePDF!!.setText(timePeriod.toString())
                }
            }

            if (MainApplication.userType() == Constants.UserType.household) {
                imgOrganization!!.visibility = View.GONE
            }else{
                if(!orgLogo.isNullOrEmpty()){
                    imgOrganization!!.visibility = View.VISIBLE
                    Utils.glideImageLoaderServer(
                        imgOrganization,
                        orgLogo,
                        R.drawable.icon_placeholder_generic)
                }
            }

            if (!envDataList.isEmpty()){

                Log.d("ENV_STATS",""+GsonBuilder().setPrettyPrinting().create().toJson(envDataList))

                for (i in envDataList.indices) {
                    val item = envDataList.get(i)

                    if(item.headingReport!!.contains("Emission",ignoreCase = true)){
                        txtCo2Emissions!!.setText(""+
                                item.points?.let { Utils.commaConversion(it) } +" "+
                                item.unit)
                    }else if (item.headingReport!!.contains("Trees",ignoreCase = true)){
                            txtTrees!!.setText(""+
                                    item.points?.let { Utils.commaConversion(it) } +" "+
                                    item.unit)
                    }else if (item.headingReport!!.contains("Oil",ignoreCase = true)){
                        txtOil!!.setText(""+
                                item.points?.let { Utils.commaConversion(it) } +" "+
                                item.unit)
                    }else if (item.headingReport!!.contains("Electricity",ignoreCase = true)){
                        txtElectricity!!.setText(""+
                                item.points?.let { Utils.commaConversion(it) } +" "+
                                item.unit)
                    }else if (item.headingReport!!.contains("Landfill",ignoreCase = true)){
                        txtLandfillSpace!!.setText(""+
                                item.points?.let { Utils.commaConversion(it) } +" "+
                                item.unit)
                    }else if (item.headingReport!!.contains("Water",ignoreCase = true)){
                        txtWater!!.setText(""+
                                item.points?.let { Utils.commaConversion(it) } +" "+
                                item.unit)
                    }else if (item.headingReport!!.contains("Compost",ignoreCase = true)){
                        txtComposs!!.setText(""+
                                item.points?.let { Utils.commaConversion(it) } +" "+
                                item.unit)
                    }else if (item.headingReport!!.contains("Farming",ignoreCase = true)){
                        txtFarmingLand!!.setText(""+
                                item.points?.let { Utils.commaConversion(it) } +" "+
                                item.unit)
                    }



                }
            }

            //Waste Recycled STATIC CATEGORY
            /*if(pieChart != null){

                for (j in pieChart!!.pieChartLabels.indices) {
                    val itemLabel = pieChart!!.pieChartLabels.get(j)
                    if(itemLabel.contains("Plastics",ignoreCase = true)){
                        txtPiePlastic!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))
                    }else if(itemLabel.contains("Electronics",ignoreCase = true)){
                        txtPieElectronics!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))
                    }else if(itemLabel.contains("Metals",ignoreCase = true)){
                        txtPieMetals!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))
                    }else if(itemLabel.contains("Glass",ignoreCase = true)){
                        txtPieGlass!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))

                    }else if(itemLabel.contains("Paper",ignoreCase = true)){
                        txtPiePaper!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))

                    }else if(itemLabel.contains("Clothes",ignoreCase = true)){
                        txtPieClothes!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))

                    }else if(itemLabel.contains("food",ignoreCase = true)){
                        txtPieFood!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))

                    }else if(itemLabel.contains("oil",ignoreCase = true)){
                        txtPieOil!!.setText(""+pieChart!!.pieChartValues.get(j).let { Utils.commaConversion(it) }
                                + " " +
                                getUnitFromValue(
                                    pieChart!!.pieChartUnits.get(j)))
                    }


                }

            }*/

            //BAR CHART VALUES TOTAL
            /*var totalPie = 0f
            for (index in reportData!!.barChartValues.indices){
                totalPie += reportData!!.barChartValues.get(index).yAxisValue
            }*/

            txtTotalWasteRecycled!!.setText(""+totalKgs +" Kgs")

            Log.e("TAG","===response===" + User.retrieveUser()?.first_name)
            Log.e("TAG","===response===" + User.retrieveUser()?.last_name)


           /* //SHOW NAME IN PDF
            if (MainApplication.userType() == Constants.UserType.household) {
                if(!User.retrieveUser()?.first_name.isNullOrEmpty()){
                    txtNamePDF!!.setText(User.retrieveUser()?.first_name.toString() + " " +
                            User.retrieveUser()?.last_name.toString())
                }
            }else{
                if(!User.retrieveUser()?.organization?.name.isNullOrEmpty()){
                    if(!selectedFilterName.isNullOrEmpty()){
                        txtNamePDF!!.setText(User.retrieveUser()?.organization?.name.toString() +" : "+
                                selectedFilterName)
                    }else{
                        txtNamePDF!!.setText(User.retrieveUser()?.organization?.name.toString())
                    }
                }

            }*/

        }catch (e: Exception){
            e.printStackTrace()
        }




    }

    private fun checkPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            WRITE_EXTERNAL_STORAGE_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                generatePDF()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission denied, cannot generate PDF",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generatePDF() {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(
            scrollViewPDF!!.width,
            scrollViewPDF!!.height,
            1
        ).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        canvas.drawPaint(paint.apply { color = Color.WHITE })

        scrollViewPDF!!.draw(canvas)

        document.finishPage(page)

        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "Recycling_Certificate_" + System.currentTimeMillis() +".pdf"

            )
            document.writeTo(FileOutputStream(file))
            Toast.makeText(
                requireContext(),
                "PDF saved to ${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Failed to save PDF",
                Toast.LENGTH_SHORT
            ).show()
        }

        document.close()
    }

    private fun dismissProgressDialog() {
        dialog.dismiss()
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {
        val baseResponse = Utils.getBaseResponse(response)
        when (tag) {

            RequestCodes.API.DASHBOARD ->
            {
                try {
                    dismissProgressDialog()

                    val dashboard = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse?.data as? LinkedTreeMap<*, *>),
                        Dashboard::class.java)
                    Log.e("TAG","===response2===" + dashboard)

                    val userModel = User.retrieveUser()

                    userModel?.first_name = dashboard.userProfile?.first_name
                    userModel?.last_name = dashboard.userProfile?.last_name
                    userModel?.organization?.name =  dashboard.userProfile?.hh_organization_name

                    Log.e("TAG","===response===" + userModel?.first_name)
                    Log.e("TAG","===response===" + userModel?.last_name)

                    userModel?.save(userModel, context,false)

                    //SHOW NAME IN PDF
                    if (MainApplication.userType() == Constants.UserType.household) {
                        if(!dashboard.userProfile?.first_name.isNullOrEmpty()){
                            txtNamePDF!!.setText(dashboard.userProfile?.first_name.toString() + " " +
                                    dashboard.userProfile?.last_name.toString())
                        }
                    }else{
                        if(!dashboard.userProfile?.organization?.name.isNullOrEmpty()){
                            if(!selectedFilterName.isNullOrEmpty()){
                                txtNamePDF!!.setText(dashboard.userProfile?.organization?.name.toString() +" : "+
                                        selectedFilterName)
                            }else{
                                txtNamePDF!!.setText(dashboard.userProfile?.organization?.name.toString())
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Home Fragment", e.toString())
                }
            }
            /*RequestCodes.API.TERM_CONDITION -> {
                val baseResponse = Utils.getBaseResponse(response)

                if (baseResponse?.data != null) {
                    val aboutApp = Gson().fromJson(
                        Utils.jsonConverterObject(baseResponse.data as? LinkedTreeMap<*, *>),
                        AboutAppData::class.java
                    )
                    if (aboutApp != null)
                        view_?.term_condition_text?.text = aboutApp.body
                    view_?.term_condition_text?.movementMethod = ScrollingMovementMethod()

                }
            }*/
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        Notify.alerterRed(activity, response?.message)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> {
                activity?.onBackPressed()
            }
        }
    }

    private fun getUnitFromValue(i: Int): String? {
        var value = ""
        if(i == 1)
        {
            value = "kgs"
        }
        else if(i == 2)
        {
            value = "liter"
        }
        else if(i == 3)
        {
            value = "pieces"
        }
        return value
    }

}
