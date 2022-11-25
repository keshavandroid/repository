package com.android.reloop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.model.JoinedCampaign
import com.android.reloop.model.ModelCampainNews
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.reloop.reloop.R
import com.reloop.reloop.interfaces.RecyclerViewItemClick
import java.text.SimpleDateFormat

class CampainListAdapter() : RecyclerView.Adapter<CampainListAdapter.ViewHolder>() {

    var list: ArrayList<JoinedCampaign> = ArrayList()
    lateinit var context: Context
    var itemClickListener: ItemClickListener? = null

    fun setClicklistner(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    constructor(context: Context, lists: ArrayList<JoinedCampaign>) : this() {

        this.context=context
        this.list = lists
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View

        view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_campain_list, parent, false)

        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = list.get(position)

        Glide.with(context)
            .load(data.getCampaign()?.image)
            .fitCenter()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.ivImg)

        holder.tvname.setText(data.getCampaign()?.title)
        holder.tvdesc.setText(data.getCampaign()?.description)

        val parser =  SimpleDateFormat("yyyy-MM-dd")
        val formatter = SimpleDateFormat("dd-MM-yyyy")

        //to show join date
        if(data.createdAt != null)
        {
            holder.lljoindate.visibility = View.VISIBLE
            val formattedDate = formatter.format(parser.parse(data.createdAt.toString()))
            holder.tvjoindate.setText(formattedDate.toString())
        }
        else{
            holder.lljoindate.visibility = View.GONE
        }

        //to show exit date or exit button
        if(data.exitDate == null)
        {
            holder.llexitdate.visibility = View.GONE
            holder.tvexit.visibility= View.VISIBLE
        }
        else{
            holder.llexitdate.visibility = View.VISIBLE
            val formattedDate1 = formatter.format(parser.parse(data.exitDate.toString()))
            holder.tvexitdate.setText(formattedDate1.toString())
            holder.tvexit.visibility= View.GONE
        }

        holder.itemView.setOnClickListener {
            itemClickListener!!.itemclick(position)
        }

        holder.tvexit.setOnClickListener {
            itemClickListener!!.exitCampain(position)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    interface ItemClickListener {
       // fun itemclick(bean: ModelCampainNews,position: Int,type : String)
       fun itemclick(position: Int)
       fun exitCampain(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var ivImg: ImageView
        internal var tvname: TextView
        internal var tvdesc: TextView
        internal var tvjoindate: TextView
        internal var tvexit: TextView
        internal var lljoindate: LinearLayout
        internal var llexitdate: LinearLayout
        internal var tvexitdate: TextView

        init {
            ivImg = view.findViewById(R.id.ivImg)
            tvname = view.findViewById(R.id.name)
            tvdesc = view.findViewById(R.id.desc)
            tvjoindate = view.findViewById(R.id.tvjoindate)
            tvexit = view.findViewById(R.id.exit)
            tvexitdate = view.findViewById(R.id.tvexitdate)
            lljoindate = view.findViewById(R.id.lljoindate)
            llexitdate = view.findViewById(R.id.llexitDate)
        }
    }
}