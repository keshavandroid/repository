package com.reloop.reloop.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.reloop.reloop.R
import com.reloop.reloop.activities.HomeActivity
import com.reloop.reloop.network.serializer.user.User
import com.reloop.reloop.utils.Constants

/**
 * A simple [Fragment] subclass.
 */
class ConfirmationSubscriptionFragment : BaseFragment() {

    companion object {
        fun newInstance(): ConfirmationSubscriptionFragment {
            return ConfirmationSubscriptionFragment()
        }
    }

    var subscriptionCycle: Int? = -1
    var rewardPoint: Int? = 0
    var image: ImageView? = null
    var message: TextView? = null
    var idMessage: TextView? = null
    var idNumber: TextView? = null

    var purchaseID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (ProductPurchasingFragment.stepView != null) {
            ProductPurchasingFragment.stepView!!.StepNumber(Constants.recycleStep4)
        }
        HomeActivity.clearAllFragments(true)

        subscriptionCycle = arguments?.getInt(Constants.DataConstants.subscriptionCycle)

        purchaseID = arguments?.getString(Constants.DataConstants.purchaseID)

        rewardPoint = arguments?.getInt(Constants.DataConstants.rewardPoints)

        val view: View? = inflater.inflate(R.layout.fragment_confirmation_subscription, container, false)

        initViews(view)

        setListeners()

        populateData()

        return view
    }

    private fun initViews(view: View?) {
        image = view?.findViewById(R.id.imageConfirmation)
        message = view?.findViewById(R.id.message)
        idMessage = view?.findViewById(R.id.idMessage)
        idNumber = view?.findViewById(R.id.idNumber)
    }

    private fun setListeners() {

    }

    private fun populateData() {
        if (subscriptionCycle == Constants.subscriptionCycleOne) {
            image?.setImageResource(R.drawable.img_request_submitted)
            message?.text = activity?.getString(R.string.order_request_submitted)
            idMessage?.text = activity?.getString(R.string.order_submitted)
            if (rewardPoint!=null && rewardPoint!=0)
            {
                val user= User.retrieveUser()
                user?.reward_points = user?.reward_points?.minus(rewardPoint!!)
                user?.save(user,activity,false)
            }
        } else if (subscriptionCycle == Constants.subscriptionCycleTwo) {
            image?.setImageResource(R.drawable.icon_confirm_subscription_cycle_two)
            message?.text = activity?.getString(R.string.subscription_activated)
            idMessage?.text = activity?.getString(R.string.subscription_id_message)
        }
        idNumber?.text = purchaseID
    }
}
