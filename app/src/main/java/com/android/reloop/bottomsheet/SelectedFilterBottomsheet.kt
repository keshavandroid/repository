package com.android.reloop.bottomsheet

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reloop.reloop.R
import com.reloop.reloop.network.serializer.DropOffPoints
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.Utils


class SelectedFilterBottomsheet : BottomSheetDialogFragment() {
  private val ARG_NAME = "item_name"
  var mListener: ItemClickListener? = null
  private val header: String? = null
  private var mContext: Context? = null
  var rgCity: RadioGroup?= null

  companion object{
      var clickedPos: String = ""
  }

  fun newInstance(
    listner:ItemClickListener,
    dropOffPoint: DropOffPoints,
    header: String?
  ): SelectedFilterBottomsheet {

    val fragment = SelectedFilterBottomsheet()

    val args = Bundle()
    args.putInt("dropOffId", dropOffPoint.id!!)
    args.putString("address", dropOffPoint.address)
    args.putString("photo", dropOffPoint.photo)
    args.putString("title",dropOffPoint.title)
    args.putSerializable(ARG_NAME, dropOffPoint.materialCategories)
    args.putString("lat",dropOffPoint.latitude.toString())
    args.putString("long",dropOffPoint.longitude.toString())
    args.putBoolean("isFavorite", dropOffPoint.is_favourite)
    args.putString("header", header)
    args.putString("description", dropOffPoint.description)

    fragment.setArguments(args)
    return fragment
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.bottomsheet_selected_filter, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//    val headerTV = view.findViewById<TextView>(R.id.title)
//    headerTV.text = requireArguments().getString("header")

    val back = view.findViewById<Button>(R.id.back)
    val imgProfile = view.findViewById<ImageView>(R.id.imgProfile)
    val titleLoc = view.findViewById<TextView>(R.id.titleLoc)
    val infoTxt = view.findViewById<TextView>(R.id.infoTxt)
    val gridView = view.findViewById<GridView>(R.id.gridLayout)
    val txtAddFav = view.findViewById<TextView>(R.id.txtAddFav)
    val closeImg = view.findViewById<ImageView>(R.id.closeImg)
    val txtNavigateToLoc = view.findViewById<TextView>(R.id.txtNavigateToLoc)

    var dropOffID = requireArguments().getInt("dropOffId")
    var address = requireArguments().getString("address")
    var photo = requireArguments().getString("photo")
    var title = requireArguments().getString("title")
    val materialCategories: ArrayList<MaterialCategories> = requireArguments().getSerializable(ARG_NAME) as ArrayList<MaterialCategories>
    var latitude = requireArguments().getString("lat")
    var longitude = requireArguments().getString("long")
    var isFavorited = requireArguments().getBoolean("isFavorite")
    var description = requireArguments().getString("description")


    titleLoc.setText(title)
    infoTxt.setText(description)
    Utils.glideImageLoaderServer(
      imgProfile,
      photo,
      R.mipmap.ic_launcher
    )

    if(isFavorited){
      txtAddFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star, 0, 0, 0);
    }else{
      txtAddFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fav_green, 0, 0, 0);
    }

    val materialAdapter = MaterialAdapter(materialCategories, requireContext())
    gridView.adapter = materialAdapter
    gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
      /*Toast.makeText(
        requireContext(), materialCategories[position].id.toString() + " selected",
        Toast.LENGTH_SHORT
      ).show()*/
    }

    back.setOnClickListener{
      dismiss()
    }
    closeImg.setOnClickListener{
      dismiss()
    }

    val btnStartDropOff = view.findViewById<Button>(R.id.btnStartDropOff)

    btnStartDropOff.setOnClickListener{
      mListener?.onSelectStation(materialCategories)
      dismiss()
    }

    txtAddFav.setOnClickListener {
      if(isFavorited){
        Notify.alerterGreen(requireActivity(),"Drop-off already added in your favorite list")
      }else{
        mListener!!.onAddToFavourite(dropOffID)
      }

    }

    txtNavigateToLoc.setOnClickListener{

      if(latitude.isNullOrEmpty() || longitude.isNullOrEmpty()){
          Notify.Toast("Drop-off Location is empty")
      }else{
        mListener!!.onNavigateToLocation(latitude, longitude)
        dismiss()
      }
    }
  }



  override fun onAttach(context: Context) {
    super.onAttach(context)
    mContext = context
    mListener = parentFragment as ItemClickListener?
    if( mListener == null ) {
      throw RuntimeException("Could not get listener");
    }
  }

  override fun onDetach() {
    mListener = null
    super.onDetach()
  }

  override fun onCancel(dialog: DialogInterface) {
    super.onCancel(dialog)

  }


  interface ItemClickListener {
    fun onSelectStation(value: ArrayList<MaterialCategories>)
    fun onAddToFavourite(dropOffId: Int)

    fun onNavigateToLocation(latitude: String,longitude : String)
  }

  internal class MaterialAdapter(
    private val materialList: List<MaterialCategories>,
    private val context: Context
  ) :
    BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var txtCatName: TextView
    private lateinit var imgMaterialCat: ImageView

    override fun getCount(): Int {
      return materialList.size
    }

    override fun getItem(position: Int): Any? {
      return null
    }

    override fun getItemId(position: Int): Long {
      return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
      var convertView = convertView

      if (layoutInflater == null) {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
      }

      if (convertView == null) {
        convertView = layoutInflater!!.inflate(R.layout.item_material_categories, null)
      }

      imgMaterialCat = convertView!!.findViewById(R.id.imgMaterialCat)
      txtCatName = convertView.findViewById(R.id.txtCatName)

      Utils.glideImageLoaderServer(imgMaterialCat, materialList.get(position).avatar, R.drawable.icon_placeholder_generic)

      if(materialList.get(position).name!!.contains("Food")){
        txtCatName.text = materialList.get(position).name!!.replace(" ","")
      }else{
        txtCatName.text = materialList.get(position).name
      }
      return convertView
    }
  }
}