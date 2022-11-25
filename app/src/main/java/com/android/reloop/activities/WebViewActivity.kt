package com.reloop.reloop.activities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.reloop.model.PaymentResponse
import com.android.reloop.utils.Configuration
import com.google.gson.Gson
import com.reloop.reloop.R
import com.reloop.reloop.app.MainApplication
import kotlinx.android.synthetic.main.activity_web_view.*
import org.apache.http.util.EncodingUtils


class WebViewActivity : AppCompatActivity() {

    companion object {
        var URL: String = "URL"
        var REFERENCE_ID: String = "REFERENCE_ID"
        var MESSAGE: String = "MESSAGE"
    }

    var progressDialog: Dialog? = null

    var webUrl: String = "";

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        MainApplication.hideActionBar(supportActionBar)
        var baseURL = ""
        baseURL = if (Configuration.isProduction) {
            "https://api.reloopapp.com/create-token";
        } else if (Configuration.isStagingNew) {
            "https://staging.reloopapp.com/create-token";
        } else {
            "http://reloop.teamtechverx.com/create-token";
        }
        if (intent.extras != null && intent.hasExtra(URL))
            webUrl = intent.getStringExtra(URL).toString()
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = MyWebViewClient(this, webUrl, this)
        val post = EncodingUtils.getBytes(webUrl, "BASE64")

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().builtInZoomControls = true;
        webView.setWebViewClient(webView.webViewClient);
        webView.postUrl(baseURL, post)
        webView.addJavascriptInterface(MyWebViewClient.MyJavaScriptInterface(this), "HtmlViewer");

    }

    override fun onDestroy() {
        super.onDestroy()
        stopLoading()
    }

    class MyWebViewClient internal constructor(
        private val activity: Activity,
        private val webURL: String,
        private val mainActivity: WebViewActivity
    ) : WebViewClient() {
        var referenceId: String = "";

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url: String = request?.url.toString();
            view?.loadUrl(url)
            return true
        }

        override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
            webView.loadUrl(url)
            return true
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            mainActivity.stopLoading()
            Toast.makeText(activity, "Something went wrong!", Toast.LENGTH_SHORT).show()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            if (webURL != url) {
                view?.loadUrl(
                    "javascript:window.HtmlViewer.showHTML" +
                            "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"
                )
            }
        }


        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            //  code here

            val uri: Uri = Uri.parse(url)
            if (url?.contains("merchant_extra")!!) {
                referenceId = uri.getQueryParameter("merchant_extra").toString();
            }
            if (url?.contains("https://sbcheckout.payfort.com")) {
                mainActivity.showLoading()
            } else {
                mainActivity.stopLoading()
            }
            if (url?.contains("response_code")!!) {
                val responseMessage =
                    uri.getQueryParameter("response_message"); //url?.substring(url?.indexOf("response_message") + 17 , url?.indexOf("&merchant_reference="))
                val responseCode = uri.getQueryParameter("response_code");
                if (responseMessage != "Success") {
                    WebViewActivity@ this.activity.setResult(
                        Activity.RESULT_CANCELED,
                        Intent().putExtra(MESSAGE, responseMessage))
                    WebViewActivity@ this.activity.finish()
                }
            }
            if (view?.getUrl().equals(webURL)) {
                //same url
            } else {
                if (!referenceId.isEmpty()) {
                    WebViewActivity@ this.activity.setResult(
                        Activity.RESULT_OK,
                        Intent().putExtra(REFERENCE_ID, referenceId)
                    )
                    WebViewActivity@ this.activity.finish()
                }
            }
            super.doUpdateVisitedHistory(view, url, isReload)
        }


        internal class MyJavaScriptInterface(ctx: Context) {
            private val ctx: Context

            @JavascriptInterface
            fun showHTML(html: String?) {
                if (html?.contains("response_code")!!) {
                    val json: String = html.substring(html.indexOf("{"), html.indexOf("}") + 1)
                    val gson = Gson()
                    val response: PaymentResponse =
                        gson.fromJson<PaymentResponse>(json, PaymentResponse::class.java)
                    var referenceId: String = "";
                    referenceId = response.getMerchentExtra().toString();
                    if (response != null && response?.getResponseMessage() != "Success") {
                        (ctx as Activity).setResult(
                            Activity.RESULT_CANCELED,
                            Intent().putExtra(MESSAGE, response.getResponseMessage()))
                        ctx.finish()
                    } else
                        if (response?.getResponseCode() == "14000") {
                            if (!referenceId.isEmpty()) {
                                (ctx as Activity).setResult(
                                    Activity.RESULT_OK,
                                    Intent().putExtra(REFERENCE_ID, referenceId)
                                )
                                ctx.finish()
                            }
                        }
                }
            }

            init {
                this.ctx = ctx
            }
        }

    }

    private fun showLoading() {
        progressDialog = Dialog(this)
        if (progressDialog?.window != null) {
            progressDialog?.setCancelable(false)
            progressDialog?.setContentView(R.layout.payment_progress_dialog_layout)
            progressDialog?.show();
        }
    }

    private fun stopLoading() {
        if (progressDialog?.window != null) {
            progressDialog?.dismiss();
        }
    }
}