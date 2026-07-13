package com.winstonpos.capacitor.presentation

import android.annotation.SuppressLint
import android.app.Presentation
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import com.getcapacitor.JSObject

class SecondaryDisplay(
    outerContext: Context?,
    display: Display?,
    private val logTag: String,
    private val onEvent: (name: String, value: JSObject?) -> Unit,
) : Presentation(
    outerContext, display
) {
    private var webView: WebView? = null

    val isValid: Boolean
        get() = this.display.isValid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary_display)
    }

    override fun dismiss() {
        super.dismiss()
        onEvent(ON_CLOSE_EVENT, null)
    }

    fun load(type: OpenType, url: String) {
        val webView = this.initWebView() ?: return

        webView.post {
            val path = url.toUri().toString()
            if (type == OpenType.HTML) {
                webView.loadDataWithBaseURL(null, path, "text/html", "UTF-8", null)
            } else {
                webView.loadUrl(path)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(): WebView? {
        if (this.webView != null) {
            return this.webView
        }

        this.webView = findViewById(R.id.secondary_webview)
        val webView = this.webView ?: return null

        webView.post {
            val webSettings = webView.settings
            webSettings.javaScriptEnabled = true
            webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
            webSettings.domStorageEnabled = true
            webSettings.allowContentAccess = true
            webSettings.allowFileAccess = true
            webSettings.mediaPlaybackRequiresUserGesture = false

            webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(logTag, "Load finished")

                    val script = """
                        window.sendPresentationMessage = function(msg) { 
                            return new Promise((resolve) => { 
                                window.CapacitorPresentation.sendMessage(JSON.stringify(msg)); 
                                resolve(undefined); 
                            }); 
                        }
                    """.trimIndent()

                    view?.evaluateJavascript(script, null)

                    val response = JSObject()
                    response.put("success", true)
                    onEvent(ON_SUCCESS_EVENT, response)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError
                ) {
                    super.onReceivedError(view, request, error)
                    Log.d(logTag, "Load error: {$error}")

                    val response = JSObject()
                    response.put("success", false)
                    response.put("error", error.description)
                    onEvent(ON_FAIL_EVENT, response)
                }
            }

            val debuggingEnabled =
                (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            Log.d(logTag, "Debugging enabled: {$debuggingEnabled}")

            WebView.setWebContentsDebuggingEnabled(debuggingEnabled)
            webView.addJavascriptInterface(JSBridge(onEvent), "CapacitorPresentation")
        }

        return webView
    }

    fun sendMessage(jsonData: JSObject) {
        val webView = this.webView ?: return

        webView.post {
            webView.evaluateJavascript(
                "window.onPresentationMessage?.({ data: $jsonData })",
                null
            )
        }
    }
}
