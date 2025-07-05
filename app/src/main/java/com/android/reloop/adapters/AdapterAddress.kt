package com.reloop.reloop.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import com.reloop.reloop.interfaces.DeleteItem
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import com.reloop.reloop.network.serializer.Addresses
import com.reloop.reloop.network.serializer.Dependencies
import com.reloop.reloop.utils.Constants
import com.reloop.reloop.utils.Utils


open class AdapterAddress(
    var dataList: ArrayList<Addresses>?,
    var itemClick: RecyclerViewItemClick,
    var deleteItem: DeleteItem,
    var dependenciesListing: Dependencies?
) :
    RecyclerView.Adapter<AdapterAddress.ViewHolder>() {
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.row_add_address, viewGroup, false)
        return ViewHolder(view, deleteItem)
    }

    override fun getItemCount() = dataList!!.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position_: Int) {
//        list?.add("Villa No. 7851, 04 Bedrooms, 06 Occupants,  Street 14-A, Floor 02, Dummy District, New York.")
        val position = holder.adapterPosition
        try {
            if(MainApplication.userType() == Constants.UserType.household){
                //default address 1
                if(dataList?.get(position)?.default ==1){ //default address
                    holder.addressStar?.visibility = View.VISIBLE
                    holder.delete?.visibility = View.GONE
                    holder.defaultLable?.visibility = View.GONE //ORIGINAL VISIBLE
                    holder.addressLabel?.text = MainApplication.applicationContext().getString(R.string.address) +" "+ (position+1)
                }else{
                    holder.addressStar?.visibility = View.GONE
                    holder.defaultLable?.visibility = View.GONE
                    holder.delete?.visibility = View.VISIBLE
                    holder.addressLabel?.text = MainApplication.applicationContext().getString(R.string.address) + " "+ (position+1)
                }
            }else{
                if(dataList?.get(position)?.default ==1){ //default address
                    holder.addressStar?.visibility = View.VISIBLE
                    holder.delete?.visibility = View.GONE
                    holder.defaultLable?.visibility = View.VISIBLE
                    holder.addressLabel?.text = dataList!!.get(position).title
                }else{
                    holder.addressStar?.visibility = View.GONE
                    holder.defaultLable?.visibility = View.GONE
                    holder.delete?.visibility = View.VISIBLE
                    holder.addressLabel?.text = dataList!!.get(position).title
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }


        if (dataList?.get(position)?.street.isNullOrEmpty()) {
            holder.address?.text = "Update Your Address"
        }
        else {
            var street = ""
            var buildingName = ""
            var unitNumberValue = ""
            if (!dataList?.get(position)?.building_name.isNullOrEmpty()) {
                buildingName = "${dataList?.get(position)?.building_name}"
            }
            if (!dataList?.get(position)?.street.isNullOrEmpty()) {
                street = "${dataList?.get(position)?.street}"
            }
            if (!dataList?.get(position)?.unit_number.isNullOrEmpty()) {
                unitNumberValue = "${dataList?.get(position)?.unit_number}"
            }
            holder.address?.text = "$unitNumberValue, $buildingName, $street, ${
                    Utils.getStringBasedOnID(dataList?.get(position)?.district_id, dependenciesListing?.districts)
                }, ${
                    Utils.getStringBasedOnID(dataList?.get(position)?.city_id, dependenciesListing?.cities)
                }"
        }
        holder.itemView.setOnClickListener {
            itemClick.itemPosition(position)
        }
        holder.delete?.setOnClickListener {
            dataList?.get(position)?.let { _ -> deleteItem.deleteItem(position) }
        }
        if (MainApplication.userType() == Constants.UserType.household) {
            holder.delete?.visibility = View.GONE
        }
    }

    class ViewHolder(itemView: View, deleteItem: DeleteItem) : RecyclerView.ViewHolder(itemView) {
        var address: TextView? = itemView.findViewById(R.id.address)
        var delete: ImageButton? = itemView.findViewById(R.id.delete)
        var addressLabel: TextView? = itemView.findViewById(R.id.addressLabel)
        var defaultLable: TextView? = itemView.findViewById(R.id.defaultLable)
        var addressStar: TextView? = itemView.findViewById(R.id.addressStar)
    }

    open fun notify(dataList: ArrayList<Addresses>?, dependenciesListing: Dependencies?) {
        this.dataList = dataList
        this.dependenciesListing = dependenciesListing
        notifyDataSetChanged()
    }
}