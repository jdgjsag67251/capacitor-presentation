package com.winstonpos.capacitor.presentation

enum class OpenType(type: String) {
    URL("url"),
    VIDEO("video"),
    HTML("html");

    val type: String?

    init {
        this.type = type
    }
}
