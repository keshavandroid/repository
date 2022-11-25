package com.android.reloop.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.reloop.model.ModelCampainNews
import com.android.reloop.network.serializer.Campain.CampaignDetails.NewsDetails
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.reloop.reloop.R
import java.text.SimpleDateFormat

class CampainNewsAdapter() : RecyclerView.Adapter<CampainNewsAdapter.ViewHolder>() {

    var list: ArrayList<NewsDetails?> = ArrayList()
    lateinit var context: Context

    private var itemClickListener: ItemClickListener? = null

    fun setClicklistner(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    constructor(context: Context, foodlist: ArrayList<NewsDetails?>) : this() {

        this.context=context
        this.list = foodlist
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View

        view = LayoutInflater.from(parent.context).inflate(R.layout.item_row_campain_news, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = list.get(position)

        Glide.with(context)
            .load(data?.getImage())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .fitCenter()
            .dontAnimate()
            .into(holder.ivImg)

        holder.tvTitle.setText(data?.getTitle())

        if(data?.getShowdate() == 1 && data.getDate() != null)
        {
            val parser =  SimpleDateFormat("yyyy-MM-dd")
            val formatter = SimpleDateFormat("dd-MM-yyyy")
            val formattedDate = formatter.format(parser.parse(data.getDate().toString()))
            holder.tvDate.setText(formattedDate)
        }

        holder.itemView.setOnClickListener {
            itemClickListener!!.itemclick(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface ItemClickListener {
       // fun itemclick(bean: ModelCampainNews,position: Int,type : String)
       fun itemclick(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        internal var ivImg: ImageView
        internal var tvTitle: TextView
        internal var tvDate: TextView

        init {

            ivImg = view.findViewById(R.id.ivImg)
            tvTitle = view.findViewById(R.id.tvTitle)
            tvDate = view.findViewById(R.id.tvDate)
        }
    }
}