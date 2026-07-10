package com.winstonpos.capacitor.presentation

class VideoOptions internal constructor(videoUrl: String?, showControls: Boolean) {
    var showControls: Boolean = false
    var videoUrl: String? = ""

    init {
        this.showControls = showControls
        this.videoUrl = videoUrl
    }
}
