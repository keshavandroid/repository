package com.reloop.reloop.adapters.viewholders

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.reloop.reloop.R
import com.reloop.reloop.interfaces.RecyclerViewItemClick


class ViewHolderPreviousSubscriptions(
    itemView: View,
    val recyclerViewItemClick: RecyclerViewItemClick
) :
    RecyclerView.ViewHolder(itemView) {
    var subscriptionTitle: TextView? = itemView.findViewById(R.id.subscription_title)
    var subscriptionStatus: TextView? = itemView.findViewById(R.id.subscription_status)
    var subscriptionAmount: TextView? = itemView.findViewById(R.id.subscription_amount)
    var subscriptionRemainingDays: TextView? =
        itemView.findViewById(R.id.subscription_remaining_days)
    var subscriptionDate: TextView? = itemView.findViewById(R.id.subscription_date)
    var subscriptionIcon: ImageView? = itemView.findViewById(R.id.subscription_icon)
    var cardView: CardView? = itemView.findViewById(R.id.cardView)
    var subscription_id: TextView? = itemView.findViewById(R.id.subscription_id)
    var subscriptionDateActive: TextView? = itemView.findViewById(R.id.subscription_date_active)
    var unSubscribe: TextView? = itemView.findViewById(R.id.unsubscribe)
    var expiry_date: TextView? = itemView.findViewById(R.id.expiry_date)
    var txtYearlyRenew: TextView? = itemView.findViewById(R.id.txtYearlyRenew)

//    var params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    init {
        unSubscribe?.setOnClickListener {
            recyclerViewItemClick.itemPosition(this.adapterPosition)
        }
    }
/*
    fun Layout_hide() {
        params.height = 0
        itemView.setLayoutParams(params);
    }*/
}