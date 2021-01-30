package eu.monniot.subpleaseapp.clients.deluge

enum class DownloadStatus {
    Downloading, Paused, Done
}

data class DownloadItem(val fileName: String, val status: DownloadStatus, val progress: Double)
