package com.android.reloop.bottomsheet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.utils.MGridView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reloop.reloop.R
import com.reloop.reloop.network.serializer.DropOffDetail
import com.reloop.reloop.network.serializer.FavDropOffPoints
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.Utils


class FavouritesBottomsheet : BottomSheetDialogFragment() {
  private val ARG_NAME = "item_name"
  private var mListener: ItemClickListener? = null
  private val header: String? = null
  private var mContext: Context? = null

  fun newInstance(
    listner: FavouritesBottomsheet.ItemClickListener,
    favDropOff: ArrayList<FavDropOffPoints>,
    header: String?
  ): FavouritesBottomsheet {
    val fragment = FavouritesBottomsheet()
    val args = Bundle()
    args.putSerializable(ARG_NAME, favDropOff)
    args.putString("header", header)
    fragment.arguments = args
    return fragment
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.bottomsheet_favourite, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val favDropOffList: ArrayList<FavDropOffPoints> = requireArguments().getSerializable(ARG_NAME) as ArrayList<FavDropOffPoints>

    val headerTV = view.findViewById<TextView>(R.id.title)
    var tvNoDataFound = view.findViewById<TextView>(R.id.tvNoDataFound)
    val recyclerView = view.findViewById<RecyclerView>(R.id.list)
    val back = view.findViewById<Button>(R.id.back)
    val btnStartDropOff = view.findViewById<Button>(R.id.btnStartDropOff)

    headerTV.text = requireArguments().getString("header")
    back.setOnClickListener{
      dismiss()
    }

    recyclerView.layoutManager = LinearLayoutManager(context)

    val adapter: OperatorAdapter = OperatorAdapter(favDropOffList, mListener!!,this@FavouritesBottomsheet)


    recyclerView.adapter = adapter


    if(favDropOffList.isEmpty()){
      tvNoDataFound.visibility = View.VISIBLE
      recyclerView.visibility = View.GONE
    }else{
      tvNoDataFound.visibility = View.GONE
      recyclerView.visibility = View.VISIBLE
    }

    btnStartDropOff.setOnClickListener{

      if(!adapter.getSelectedArray().isEmpty()){
        mListener?.onStartDropOffFavourite(adapter.getSelectedArray())
        dismiss()
      }else{
        Notify.Toast("Please select the Drop-Off first")
      }
    }
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    mContext = context
    mListener = parentFragment as ItemClickListener
    if( mListener == null ) {
      throw RuntimeException("Could not get listener")
    }
  }

  override fun onDetach() {
    mListener = null
    super.onDetach()
  }

  interface ItemClickListener {
    fun onStartDropOffFavourite(selected: ArrayList<FavDropOffPoints>)
    fun onDeleteFavourite(dropOffId: Int?,pos: Int)
    fun onCheckDropOffStation(latitude: String,longitude : String, dropOffDetail: DropOffDetail)
  }

  private class ViewHolder(inflater: LayoutInflater, parent: ViewGroup?) :
    RecyclerView.ViewHolder(
      inflater.inflate(
        R.layout.row_item_favourites,
        parent,
        false
      )
    ) {
    val text: TextView
    //var logo: ImageView
    var radioAddress: RadioButton
    var imgDelete: ImageView
    var gridView: MGridView
    var txtCheckDropOffStation: TextView

    init {
      // TODO: Customize the item layout
      text = itemView.findViewById(R.id.txtLocationName)
      //logo = itemView.findViewById(R.id.iv_recharge_provider_icon)
      radioAddress = itemView.findViewById(R.id.radioAddress)
      imgDelete = itemView.findViewById(R.id.imgDelete)
      gridView = itemView.findViewById(R.id.gridLayout)
      txtCheckDropOffStation = itemView.findViewById(R.id.txtCheckDropOffStation)
    }
  }



  private class OperatorAdapter(
    var mData: ArrayList<FavDropOffPoints>?,
    var mListener: ItemClickListener,
    var ctx: FavouritesBottomsheet) :
    RecyclerView.Adapter<ViewHolder>() {

    private var lastCheckedPosition = -1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

      holder.radioAddress.setChecked(position == lastCheckedPosition);

      holder.radioAddress.setOnClickListener{
        val copyOfLastCheckedPosition = lastCheckedPosition
        lastCheckedPosition = holder.adapterPosition
        notifyItemChanged(copyOfLastCheckedPosition)
        notifyItemChanged(lastCheckedPosition)

        mData!!.get(holder.adapterPosition).isFavouriteSelected = true

      }

//      holder.text.text = mData!![position].drop_off_details!!.address
      holder.text.text = mData!![position].drop_off_details!!.description

      holder.radioAddress.text = mData!![position].drop_off_details!!.title

      holder.itemView.setOnClickListener { Log.e("clicked", "" + position) }

      holder.imgDelete.setOnClickListener{
        mListener.onDeleteFavourite(mData!![position].id,position)
        ctx.dismiss()

      }

      holder.txtCheckDropOffStation.setOnClickListener{
        mListener.onCheckDropOffStation(
          mData!!.get(position).drop_off_details!!.latitude.toString(),
          mData!!.get(position).drop_off_details!!.longitude.toString(),
          mData!!.get(position).drop_off_details!!
        )
        //OLD Change
        //ctx.dismiss()
      }

      val materialAdapter = MaterialAdapter(
        mData!![position].drop_off_details!!.materialCategories!!,
        ctx.mContext!!
      )
      holder.gridView.adapter = materialAdapter
      holder.gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
        /*Toast.makeText(
          requireContext(), materialCategories[position].id.toString() + " selected",
          Toast.LENGTH_SHORT
        ).show()*/
      }


    }

    fun getSelectedArray(): ArrayList<FavDropOffPoints> {
      val selectedArray: ArrayList<FavDropOffPoints> = ArrayList()
      for (listItem in mData!!) {
        if (listItem.isFavouriteSelected == true) {
          selectedArray.add(listItem)
        }
      }
      return selectedArray
    }

    override fun getItemCount(): Int {
      return mData!!.size
    }

    class MaterialAdapter(
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
          layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
}