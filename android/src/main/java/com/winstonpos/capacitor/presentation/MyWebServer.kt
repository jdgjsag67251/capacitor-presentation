package com.winstonpos.capacitor.presentation

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.InputStream

class MyWebServer(private val context: Context, port: Int, protected var html: String?) :
    NanoHTTPD(port) {
    init {
        start(SOCKET_READ_TIMEOUT, false)
        println("Web server started on port: " + port)
    }

    override fun serve(session: IHTTPSession): Response {
        var uri = session.getUri()
        if (!uri.contains(".") || uri == "/") {
            uri = "/index.html"
        }

        return getAssetFileResponse(uri)
    }


    private fun getAssetFileResponse(uri: String): Response {
        val assetManager = context.getAssets()
        val inputStream: InputStream?

        try {
            inputStream = assetManager.open("public" + uri)
            val mimeType = getMimeType(uri)


            return newChunkedResponse(Response.Status.OK, mimeType, inputStream)
        } catch (e: IOException) {
            e.printStackTrace()

            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                "text/plain",
                "404 - File Not Found"
            )
        }
    }

    private fun getMimeType(uri: String): String {
        if (uri.endsWith(".js")) {
            return "application/javascript"
        } else if (uri.endsWith(".html")) {
            return "text/html"
        } else if (uri.endsWith(".css")) {
            return "text/css"
        } else if (uri.endsWith(".png")) {
            return "image/png"
        } else if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
            return "image/jpeg"
        } else if (uri.endsWith(".gif")) {
            return "image/gif"
        } else if (uri.endsWith(".svg")) {
            return "image/svg+xml"
        } else if (uri.endsWith(".json")) {
            return "application/json"
        }

        return "text/plain"
    }
}
