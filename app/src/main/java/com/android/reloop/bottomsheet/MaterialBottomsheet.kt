package com.android.reloop.bottomsheet

import android.R.array
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.GsonBuilder
import com.reloop.reloop.R
import com.reloop.reloop.network.serializer.collectionrequest.MaterialCategories
import com.reloop.reloop.utils.Utils


class MaterialBottomsheet : BottomSheetDialogFragment(){
  private val ARG_NAME = "item_name"
  private var mListener: ItemClickListener? = null
  private val header: String? = null
  private var mContext: Context? = null

//  lateinit var materialCategoriesData: ArrayList<MaterialCategories>
//  var adapter: OperatorAdapter? = null


  fun newInstance(
    listner: ItemClickListener,
    materialCategories: ArrayList<MaterialCategories>,
    header: String?
  ): MaterialBottomsheet {
    val fragment = MaterialBottomsheet()
    val args = Bundle()
    args.putSerializable(ARG_NAME, materialCategories)
    args.putString("header", header)
    fragment.setArguments(args)
    return fragment
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.bottomsheet_material, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val headerTV = view.findViewById<TextView>(R.id.title)
    headerTV.text = requireArguments().getString("header")

    val materialCategoriesData: ArrayList<MaterialCategories> = requireArguments().getSerializable(ARG_NAME) as ArrayList<MaterialCategories>

    Log.d("MATERIAL", ""+ GsonBuilder().setPrettyPrinting().create().toJson(materialCategoriesData))

    val back = view.findViewById<Button>(R.id.back)
    back.setOnClickListener{
      dismiss()
    }

    val recyclerView = view.findViewById<RecyclerView>(R.id.list)
    recyclerView.layoutManager = LinearLayoutManager(context)
    val adapter: OperatorAdapter = OperatorAdapter(materialCategoriesData!!)
    recyclerView.adapter = adapter

    val btnApplyFilter = view.findViewById<Button>(R.id.btnApplyFilter)
    btnApplyFilter.setOnClickListener{
      mListener!!.onFilterMaterial(adapter.getSelectedArray())
      dismiss()
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


  interface ItemClickListener {
    fun onFilterMaterial(materialData: ArrayList<MaterialCategories>)
  }

  private class ViewHolder internal constructor(inflater: LayoutInflater, parent: ViewGroup?) :
    RecyclerView.ViewHolder(
      inflater.inflate(
        R.layout.item_material_addresses,
        parent,
        false
      )
    ) {
    val text: TextView
    var imgMaterial: ImageView
    var chkBox: CheckBox

    init {
      // TODO: Customize the item layout
      text = itemView.findViewById(R.id.txtCity)
      imgMaterial = itemView.findViewById(R.id.imgMaterial)
      chkBox = itemView.findViewById(R.id.chkBox)
    }
  }

  private class OperatorAdapter(var mData:ArrayList<MaterialCategories>) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      var itemData = mData.get(position)
      holder.text.text = mData.get(position).name

      Log.d("visible_in_dropoff", "" + mData.get(position).visible_in_dropoff)

      Utils.glideImageLoaderServer(holder.imgMaterial, mData.get(position).avatar, R.drawable.icon_placeholder_generic)

      holder.itemView.setOnClickListener {
        Log.e("clicked", "" + position)
      }

      if(mData.get(position).isCheckedCategory == true){
        holder.chkBox.isChecked = true
      }else{
        holder.chkBox.isChecked = false
      }

      holder.chkBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(chkButton: CompoundButton?, isChecked: Boolean) {
          Log.d("chkBox"," Pos : "+holder.adapterPosition + " isChecked : " + isChecked)
          mData.get(holder.adapterPosition).isCheckedCategory = isChecked
        }
      })
    }

    fun getArrayList(): ArrayList<MaterialCategories> {
      return mData
    }

    fun getSelectedArray(): ArrayList<MaterialCategories> {
      val selectedArray: ArrayList<MaterialCategories> = ArrayList()
      for (listItem in mData) {

        //old condition
        /*if (listItem.isCheckedCategory == true) {
          selectedArray.add(listItem)
        }*/

        //new condition
        selectedArray.add(listItem)

      }
      return selectedArray
    }

    override fun getItemCount(): Int {
      return mData.size
    }

  }
}