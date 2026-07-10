package com.winstonpos.capacitor.presentation

import android.annotation.SuppressLint
import android.app.Presentation
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.getcapacitor.JSObject
import java.io.IOException

class SecondaryDisplay(
    outerContext: Context?,
    display: Display?,
    private val onEvent: (name: String, value: JSObject) -> Unit,
) : Presentation(
    outerContext, display
) {
    private var webServer: MyWebServer? = null
    protected var url: String = ""
    protected var video: String? = ""
    private var webView: WebView? = null

    val isValid: Boolean
        get() = this.display.isValid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary_display)
        webView = findViewById<WebView?>(R.id.secondary_webview)
    }

    fun init(type: OpenType, url: String) {
        when (type) {
            OpenType.URL, OpenType.HTML -> {
                this.url = url
                this.startWebView(type, url)
            }

            else -> {}
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView(type: OpenType?, data: Any?) {
        if (webView != null) {
//      webView.addJavascriptInterface(new MessageEvents(data), "presentationCapacitor");
            val webSettings = webView!!.getSettings()
            webSettings.setJavaScriptEnabled(true)
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE)
            webSettings.setDomStorageEnabled(true)
            webSettings.setDatabaseEnabled(true)
            webSettings.setAllowContentAccess(true)
            webSettings.setAllowFileAccess(true)
            webSettings.setMediaPlaybackRequiresUserGesture(false)
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true)
            webView!!.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)

            var path = url

            if (!url.startsWith("https://") && !url.startsWith("https://") && type != OpenType.HTML) {
                path = Uri.parse("http://localhost:8080/" + url).toString()
                startWebServer(path)
            } else {
                path = Uri.parse(url).toString()
            }


            webView!!.setWebViewClient(object : WebViewClient() {
                override fun onPageFinished(view: WebView?, _url: String?) {
                    notifyToSuccess(webView, _url)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        notifyToFail(webView, error.getErrorCode())
                    }
                }
            })
            webView!!.setWebChromeClient(WebChromeClient())
            if (type == OpenType.HTML) {
                webView!!.loadDataWithBaseURL(null, path, "text/html", "UTF-8", null)
            } else {
                webView!!.loadUrl(path)
            }
        }
    }

    fun sendMessage(jsonData: JSObject) {
        webView!!.post(Runnable {
            onEvent(ON_MESSAGE_EVENT, jsonData)
            webView!!.evaluateJavascript(
                "javascript:window.receiveFromPresentationCapacitor(" + jsonData.toString() + ")",
                null
            )
        })
    }

    fun startWebServer(path: String?) {
        if (webServer == null) {
            try {
                webServer = MyWebServer(this.getContext(), 8080, path)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun notifyToSuccess(view: WebView?, url: String?) {
        val response = JSObject()
        response.put("result", url)
        response.put("message", "success")
        onEvent(ON_SUCCESS_EVENT, response)
    }

    fun notifyToFail(view: WebView?, errorCode: Int) {
        val response = JSObject()
        response.put("result", errorCode)
        response.put("message", "fail")
        onEvent(ON_FAIL_EVENT, response)
    }
}
