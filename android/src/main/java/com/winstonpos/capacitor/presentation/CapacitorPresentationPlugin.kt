package com.winstonpos.capacitor.presentation

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Display
import android.view.Surface
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

const val ON_SUCCESS_EVENT: String = "onSuccessLoadUrl"
const val ON_FAIL_EVENT: String = "onFailLoadUrl"
const val ON_MESSAGE_EVENT: String = "onMessage"
const val ON_CLOSE_EVENT: String = "onClose"

@CapacitorPlugin(name = "CapacitorPresentation")
class CapacitorPresentationPlugin : Plugin() {
    private var display: SecondaryDisplay? = null

    private val displayManager: DisplayManager? by lazy {
        activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
    }

    @PluginMethod
    fun open(call: PluginCall) {
        val type =
            getResultType(call.getString("type")) ?: return call.reject("Missing 'type' argument")
        val url = when (type) {
            OpenType.URL -> call.getString("url", null)
            OpenType.HTML -> call.getString("html", null)
        } ?: return call.reject("Missing argument")

        openSecondDisplay { display ->
            val ret = JSObject()

            if (display == null) {
                ret.put("success", false)
                ret.put("error", "No displays")
            } else {
                try {
                    display.show()
                    display.init(type, url)

                    ret.put("success", true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ret.put("success", false)
                    ret.put("error", e.localizedMessage)
                }
            }

            call.resolve(ret)
        }
    }

    @PluginMethod
    fun terminate(call: PluginCall) {
        display?.dismiss()
        call.resolve()
    }

    @PluginMethod
    fun sendMessage(call: PluginCall) {
        display?.sendMessage(call.getObject("data"))
        call.resolve()
    }

    @PluginMethod
    fun getDisplays(call: PluginCall) {
        val displays = JSArray()

        if (displayManager != null) {
            val presentationDisplays =
                displayManager!!.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)

            for (display in presentationDisplays) {
                val displayObject = JSObject()
                displayObject.put("displayId", display.displayId)
                displayObject.put("name", display.name)
                displayObject.put("isDefaultDisplay", display.displayId == Display.DEFAULT_DISPLAY)

                run {
                    val metrics = DisplayMetrics()
                    display.getMetrics(metrics)
                    displayObject.put("width", metrics.widthPixels)
                    displayObject.put("height", metrics.heightPixels)
                }

                run {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val cutout = display.cutout
                        val jsObject = JSObject()

                        if (cutout == null) {
                            jsObject.put("safeInsetTop", 0)
                            jsObject.put("safeInsetLeft", 0)
                            jsObject.put("safeInsetBottom", 0)
                            jsObject.put("safeInsetRight", 0)
                        } else {
                            jsObject.put("safeInsetTop", cutout.safeInsetTop)
                            jsObject.put("safeInsetLeft", cutout.safeInsetLeft)
                            jsObject.put("safeInsetBottom", cutout.safeInsetBottom)
                            jsObject.put("safeInsetRight", cutout.safeInsetRight)
                        }

                        displayObject.put("cutout", jsObject)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val deviceProductInfo = display.deviceProductInfo

                    if (deviceProductInfo != null) {
                        val jsObject = JSObject()
                        jsObject.put("productUId", deviceProductInfo.productId)
                        jsObject.put("manufacturerPnpId", deviceProductInfo.manufacturerPnpId)
                        jsObject.put("name", deviceProductInfo.name)
                        jsObject.put("modelYear", deviceProductInfo.modelYear)
                        jsObject.put("manufactureYear", deviceProductInfo.manufactureYear)
                        jsObject.put("manufactureWeek", deviceProductInfo.manufactureWeek)
                        displayObject.put("productInfo", jsObject)
                    }
                }

                displayObject.put(
                    "rotation", when (display.rotation) {
                        Surface.ROTATION_0 -> {
                            0
                        }

                        Surface.ROTATION_90 -> {
                            90
                        }

                        Surface.ROTATION_180 -> {
                            180
                        }

                        Surface.ROTATION_270 -> {
                            270
                        }

                        else -> {
                            0
                        }
                    }
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    displayObject.put("isHdr", display.isHdr)
                }

                displays.put(displayObject)
            }
        }

        val response = JSObject()
        response.put("displays", displays)
        call.resolve(response)
    }

    private fun getResultType(resultType: String?): OpenType? {
        if (resultType == null) {
            return null
        }

        return try {
            OpenType.valueOf(resultType.uppercase())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun openSecondDisplay(callback: (display: SecondaryDisplay?) -> Unit) {
        if (this.display != null && this.display!!.isValid) {
            return callback(this.display)
        }

        if (this.display != null) {
            this.display!!.dismiss()
            this.display = null
        }

        Handler(Looper.getMainLooper()).post {
            if (displayManager == null) {
                callback(null)
            } else {
                val presentationDisplay =
                    displayManager!!.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
                        .first()

                if (presentationDisplay == null) {
                    callback(null)
                } else {
                    display = SecondaryDisplay(context, presentationDisplay) { name, data ->
                        notifyListeners(name, data, true)
                    }

                    callback(display)
                }
            }
        }
    }
}
