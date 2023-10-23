package group.nhom14.textospeech.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.core.content.ContextCompat
import java.io.File

object DownloadUtil {
    fun downloadAudioFile(url: String, context: Context, callback: (Boolean, String) -> Unit) {
        try {
            val fileName = System.currentTimeMillis().toString() + ".wav"
            val folder = File(context.getExternalFilesDir(null),"downloaded_sounds")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val filePath = "${folder.absolutePath}/$fileName"
            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(fileName)
                .setDestinationUri(Uri.fromFile(File(filePath)))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

            val downloadId = downloadManager.enqueue(request)
            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
                        val cursor = downloadManager.query(query)
                        if (cursor.moveToFirst()) {
                            callback(true, filePath)
                        } else {
                            callback(false, "")
                        }
                        cursor.close()
                        context?.unregisterReceiver(this)
                    }
                }
            }
            ContextCompat.registerReceiver(
                context,
                broadcastReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
//            context.registerReceiver(
//                broadcastReceiver,
//                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//            )
        } catch (e: Exception) {
            callback(false, "")
        }
    }



    private fun getFileNameFromUrl(url: String): String {
        val uri = Uri.parse(url)
        return uri.lastPathSegment ?: "audio.mp3"
    }
}
