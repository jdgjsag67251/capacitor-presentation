package com.winstonpos.capacitor.presentation

import android.webkit.JavascriptInterface
import com.getcapacitor.JSObject
import org.json.JSONException

class JSBridge(private val onEvent: (name: String, value: JSObject?) -> Unit) {
    @JavascriptInterface
    fun sendMessage(jsonString: String) {
        try {
            val parsed = JSObject(jsonString)
            onEvent(ON_MESSAGE_EVENT, parsed)
        } catch (_: JSONException) {
            // Don't send anything
        }
    }
}
