package com.winstonpos.capacitor.presentation

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.webkit.WebView
import com.getcapacitor.JSObject
import com.getcapacitor.Logger
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import java.util.Objects

internal fun interface DisplayCallback {
    fun onDisplayReady(display: SecondaryDisplay?)
}

@CapacitorPlugin(name = "CapacitorPresentation")
class CapacitorPresentationPlugin : Plugin() {
    private val implementation = CapacitorPresentation()
    private var display: SecondaryDisplay? = null
    var displayManager: DisplayManager? = null
    var presentationDisplays: Array<Display?>? = null

    val SUCCESS_CALL_BACK: String = "onSuccessLoadUrl"
    val FAIL_CALL_BACK: String = "onFailLoadUrl"

    @JvmField
    val ON_MESSAGE_EVENT: String = "onMessage"

    @PluginMethod
    fun openLink(call: PluginCall) {
        val url = call.getString("url")
        val type = getResultType(call.getString("type"))

        val ret = JSObject()
        ret.put("url", url)

        openSecondDisplay { display: SecondaryDisplay? ->
            display?.init(Objects.requireNonNull<OpenType?>(type), url)
        }
        call.resolve(ret)
    }

    private fun getResultType(resultType: String?): OpenType {
        if (resultType == null) {
            return OpenType.URL
        }
        try {
            return OpenType.valueOf(resultType.uppercase())
        } catch (ex: IllegalArgumentException) {
            Logger.debug(
                getLogTag(),
                "Invalid result type \"" + resultType + "\", defaulting to base64"
            )
            return OpenType.URL
        }
    }

    @PluginMethod
    fun terminate(call: PluginCall?) {
        if (display == null) {
            return
        }

        display!!.dismiss()
    }

    @PluginMethod
    fun open(call: PluginCall) {
        val type = getResultType(call.getString("type"))
        val ret = JSObject()
        openSecondDisplay(DisplayCallback { display: SecondaryDisplay? ->
            try {
                var data: Any? = null
                when (type) {
                    OpenType.URL -> data = call.getString("url", null)
                    OpenType.HTML -> data = call.getString("html", null)
                    OpenType.VIDEO -> data = VideoOptions(
                        call.getObject("options").getString("url"),
                        (true == call.getBoolean("showControls"))
                    )
                }
                if (display != null) {
                    display.show()
                    if (type == OpenType.VIDEO) {
                        display.init(type, data as VideoOptions)
                    } else {
                        display.init(type, data as String?)
                    }
                }
                ret.put("result", data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        call.resolve(ret)
    }


    private fun openSecondDisplay(callback: DisplayCallback) {
        Handler(Looper.getMainLooper()).post(Runnable {
            displayManager =
                getActivity().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
            if (displayManager != null) {
                presentationDisplays =
                    displayManager!!.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
                if (presentationDisplays!!.size > 0) {
                    Log.d("presentationDisplays", presentationDisplays!![0].toString())
                    display = SecondaryDisplay(getContext(), presentationDisplays!![0])
                    // Callback ile sonucu döndür
                    callback.onDisplayReady(display)
                }
            }
        })
    }

    fun notifyToSuccess(view: WebView?, url: String?) {
        val response = JSObject()
        response.put("result", url)
        response.put("message", "success")
        notifyListeners(SUCCESS_CALL_BACK, response, true)
    }

    fun notifyToFail(view: WebView?, errorCode: Int) {
        val response = JSObject()
        response.put("result", errorCode)
        response.put("message", "fail")
        notifyListeners(FAIL_CALL_BACK, response, true)
    }

    fun notifyListener(tag: String?, jsObject: JSObject?) {
        notifyListeners(tag, jsObject, true)
    }

    @PluginMethod
    fun sendMessage(call: PluginCall) {
        if (display != null) {
            display!!.sendMessage(call.getObject("data"))
        }
    }

    @PluginMethod
    fun getDisplays(call: PluginCall) {
        displayManager = getActivity().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
        val response = JSObject()
        var displays = 0

        if (displayManager != null) {
            presentationDisplays =
                displayManager!!.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
            displays = presentationDisplays!!.size
        }
        response.put("displays", displays)
        call.resolve(response)
    }
}
