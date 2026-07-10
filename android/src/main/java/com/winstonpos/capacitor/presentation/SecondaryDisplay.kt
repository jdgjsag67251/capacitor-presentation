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
import android.widget.MediaController
import android.widget.VideoView
import com.getcapacitor.JSObject
import java.io.IOException
import java.util.Objects

class SecondaryDisplay(private val outerContext: Context?, display: Display?) : Presentation(
    outerContext, display
) {
    var capPlugin: CapacitorPresentationPlugin = CapacitorPresentationPlugin()
    private var webServer: MyWebServer? = null
    protected var url: String = ""
    protected var video: String? = ""
    private var webView: WebView? = null
    private var videoView: VideoView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secondary_display)
        webView = findViewById<WebView?>(R.id.secondary_webview)
        videoView = findViewById<VideoView>(R.id.videoView)
    }

    fun init(type: OpenType?, data: VideoOptions) {
        if (Objects.requireNonNull<OpenType?>(type) == OpenType.VIDEO) {
            video = data.videoUrl
            startVideo(data.showControls)
        }
    }

    fun init(type: OpenType, data: String?) {
        when (type) {
            OpenType.URL, OpenType.HTML -> {
                url = data as String
                this.startWebView(type, data)
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
                    capPlugin.notifyToSuccess(webView, _url)
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        capPlugin.notifyToFail(webView, error.getErrorCode())
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
            capPlugin.notifyListener(capPlugin.ON_MESSAGE_EVENT, jsonData)
            webView!!.evaluateJavascript(
                "javascript:window.receiveFromPresentationCapacitor(" + jsonData.toString() + ")",
                null
            )
        })
    }

    private fun startVideo(showControls: Boolean) {
        webView!!.setVisibility(View.GONE)
        videoView!!.setVisibility(View.VISIBLE)

        val videoUrl = video
        val uri = Uri.parse(videoUrl)
        videoView!!.setVideoURI(uri)

        if (showControls) {
            val mediaController = MediaController(this.getContext())
            mediaController.setAnchorView(videoView)
            videoView!!.setMediaController(mediaController)
        }

        videoView!!.start()
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
}
