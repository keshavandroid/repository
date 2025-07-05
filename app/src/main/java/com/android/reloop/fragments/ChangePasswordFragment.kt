package com.reloop.reloop.fragments


import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.reloop.reloop.R
import com.reloop.reloop.auth.ChangePasswordAuth
import com.reloop.reloop.customviews.CustomEditText
import com.reloop.reloop.network.Network
import com.reloop.reloop.network.NetworkCall
import com.reloop.reloop.network.OnNetworkResponse
import com.reloop.reloop.network.serializer.BaseResponse
import com.reloop.reloop.network.serializer.settings.ChangePassword
import com.reloop.reloop.utils.Notify
import com.reloop.reloop.utils.RequestCodes
import com.reloop.reloop.utils.Utils
import retrofit2.Call
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 */
class ChangePasswordFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse {
    companion object {

        fun newInstance(): ChangePasswordFragment {
            return ChangePasswordFragment()
        }

    }

    var submit: Button? = null
    var oldPassword: CustomEditText? = null
    var newPassword: CustomEditText? = null
    var confirmNewPassword: CustomEditText? = null
    var back:Button?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View? = inflater.inflate(R.layout.fragment_change_password, container, false)
        initViews(view)
        setListeners()
        populateData()
        return view
    }

    private fun initViews(view: View?) {
        submit = view?.findViewById(R.id.submit)
        oldPassword = view?.findViewById(R.id.old_password)
        newPassword = view?.findViewById(R.id.new_password)
        confirmNewPassword = view?.findViewById(R.id.confirm_new_password)
        back=view?.findViewById(R.id.back)

    }

    private fun setListeners() {
        submit?.setOnClickListener(this)
        back?.setOnClickListener(this)
    }

    private fun populateData() {

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.submit -> {
                val authSuccessful: Boolean = ChangePasswordAuth.authenticate(
                    oldPassword?.text.toString(),
                    newPassword?.text.toString(),
                    confirmNewPassword?.text.toString(),
                    requireActivity()
                )
                if (authSuccessful) {
                    val changePassword: ChangePassword = ChangePassword()
                    changePassword?.old_password = oldPassword?.text.toString()
                    changePassword?.new_password = newPassword?.text.toString()
                    changePassword?.new_password_confirmation = confirmNewPassword?.text.toString()

                    submit?.isClickable = false
                    NetworkCall.make()
                        ?.setCallback(this)
                        ?.setTag(RequestCodes.API.CHANGE_PASSWORD)
                        ?.autoLoading(requireActivity())
                        ?.enque(Network().apis()?.changePassword(changePassword))
                        ?.execute()
                }
            }
            R.id.back->{
                activity?.onBackPressed()
            }
        }
    }

    override fun onSuccess(call: Call<Any?>?, response: Response<Any?>, tag: Any?) {

        when (tag) {
            RequestCodes.API.CHANGE_PASSWORD -> {
                val baseResponse = Utils.getBaseResponse(response)

                Notify.alerterGreen(
                    requireActivity(),
                    baseResponse?.message)
                val handler = Handler()
                handler.postDelayed(
                    {
                        activity?.onBackPressed()
                    }
                    , 1000
                )
            }
        }
    }

    override fun onFailure(call: Call<Any?>?, response: BaseResponse?, tag: Any?) {
        submit?.isClickable = true
        Notify.alerterRed(activity, response?.message)
    }
}
