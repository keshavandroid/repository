package com.android.reloop.bottomsheet

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.reloop.reloop.R
import com.reloop.reloop.network.serializer.DependencyDetail


class CityBottomsheet : BottomSheetDialogFragment(), View.OnClickListener {
  private val ARG_NAME = "item_name"
  var mListener: ItemClickListener? = null
  private val header: String? = null
  private var mContext: Context? = null
  var rgCity: RadioGroup?= null

  companion object{
      var clickedPos: String = ""
      var cityId: String = ""
  }

  fun newInstance(
    listner:ItemClickListener,
    cities: ArrayList<DependencyDetail>,
    header: String?,
    idCity: String
  ): CityBottomsheet {
    val fragment = CityBottomsheet()
    val args = Bundle()
    args.putSerializable(ARG_NAME, cities)
    args.putString("header", header)
    args.putString("idCity",idCity)
    fragment.setArguments(args)
    return fragment
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.bottomsheet_city, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val citiesData: ArrayList<DependencyDetail> = requireArguments().getSerializable(ARG_NAME) as ArrayList<DependencyDetail>
    val headerTV = view.findViewById<TextView>(R.id.title)
    headerTV.text = requireArguments().getString("header")
    val idCity = requireArguments().getString("idCity")

//    val recyclerView = view.findViewById<RecyclerView>(R.id.list)
//    recyclerView.layoutManager = LinearLayoutManager(context)
//    recyclerView.adapter = OperatorAdapter(this@CityBottomsheet,mListener,citiesData)

    rgCity = view.findViewById<RadioGroup>(R.id.rgCity)

    val back = view.findViewById<Button>(R.id.back)
    back.setOnClickListener{
      dismiss()
    }
    val btnApplyFilter = view.findViewById<Button>(R.id.btnApplyFilter)

    btnApplyFilter.setOnClickListener{
      mListener!!.onFilterSelected(clickedPos,cityId)
      dismiss()
    }

    addRadioButtons(citiesData, idCity!!)


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
    fun onFilterSelected(selected: String, cityId: String)

  }

  fun addRadioButtons(stringArrayList: ArrayList<DependencyDetail>, idCity: String) {
    rgCity!!.setOrientation(LinearLayout.VERTICAL)
    rgCity!!.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    rgCity!!.setDividerDrawable(getResources().getDrawable(android.R.drawable.divider_horizontal_textfield, requireActivity().getTheme()));

    for (i in stringArrayList!!.indices) {
      val rdbtn = RadioButton(mContext)
      rdbtn.id = View.generateViewId()
      rdbtn.setTextColor(resources.getColor(R.color.text_color_drop_off));

      rdbtn.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.drop_off_green)));


      rdbtn.text = ""+stringArrayList.get(i).name
      rdbtn.tag = stringArrayList.get(i).id

      if(!idCity.isNullOrEmpty()){
        if(idCity.equals(stringArrayList.get(i).id.toString())){
          rdbtn.isChecked = true
        }

      }

      rdbtn.setOnClickListener(this)

      val params = RadioGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
      params.setMargins(30, 30, 30, 30)

      rdbtn.setLayoutParams(params);

      rgCity!!.addView(rdbtn)
    }
  }

  private class ViewHolder internal constructor(inflater: LayoutInflater, parent: ViewGroup?) :
    RecyclerView.ViewHolder(
      inflater.inflate(
        R.layout.item_city_addresses,
        parent,
        false
      )
    ) {
    val text: TextView
    val imgRadio: ImageView

    init {
      text = itemView.findViewById(R.id.txtCity)
      imgRadio = itemView.findViewById(R.id.imgRadio)

    }
  }

  private class OperatorAdapter internal constructor(
    val context: CityBottomsheet,
    val listner: ItemClickListener?,
    val mCities: ArrayList<DependencyDetail>) :
    RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.text.text = mCities.get(position).name.toString()
      /*holder.itemView.setOnClickListener {

        holder.imgRadio.setImageResource(R.drawable.ic_radio_selected)

        Log.e("clicked", "" + mCities.get(position).name.toString())
        listner?.onFilterSelected(""+ mCities.get(position).name.toString(),mCities.get(position).id.toString())
        context.dismiss()

      }*/
    }



    override fun getItemCount(): Int {
      return mCities!!.size
    }
  }

  override fun onClick(view: View?) {
    Log.d("RADIOS", " Name " + (view as RadioButton).text + " Id is " + view.getId())
    clickedPos = ""+(view as RadioButton).text
    cityId = ""+ (view as RadioButton).tag
  }
}