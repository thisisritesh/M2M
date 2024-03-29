package com.morning2morning.android.m2m.ui.commonwebview

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebSettings.PluginState
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.databinding.ActivityWebviewBinding
import com.morning2morning.android.m2m.utils.Constants


class WebviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backArrow.setOnClickListener {
            finish()
        }

        val title = intent.extras?.getString(Constants.WEBVIEW_TITLE)!!
        val url = intent.extras?.getString(Constants.WEBVIEW_URL)!!

        binding.headingText.text = title

        loadWebView(url)



    }

    private fun loadWebView(url: String){
        try {
            val webView = binding.webView
            webView.loadUrl(url)
            webView.settings.javaScriptEnabled = true
            webView.settings.javaScriptCanOpenWindowsAutomatically = true
            webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            webView.settings.lightTouchEnabled = false
            webView.isHorizontalScrollBarEnabled = false
            webView.webViewClient = object : WebViewClient() {

                override fun onPageFinished(webView: WebView?, url: String) {
                    super.onPageFinished(webView, url)

                    webView?.invalidate()

                    binding.progressBar.visibility = View.GONE

                }

                override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        finish()
    }

}