package com.example.dragontracker

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.URL

object AppUpdater {
    private const val REPO = "microsoftofficials661-create/DragonTigerTracker"

    suspend fun checkAndInstallUpdate(context: Context, currentVersionCode: Int): String = withContext(Dispatchers.IO) {
        try {
            val apiUrl = "https://api.github.com/repos/$REPO/releases/latest"
            val json = JSONObject(URL(apiUrl).readText())
            val tagName = json.getString("tag_name").removePrefix("v")
            val remoteCode = tagName.toIntOrNull() ?: return@withContext "Invalid tag: $tagName"

            if (remoteCode <= currentVersionCode) {
                return@withContext "Up to date (v$currentVersionCode)"
            }

            val assets = json.getJSONArray("assets")
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                if (asset.getString("name").endsWith(".apk")) {
                    apkUrl = asset.getString("browser_download_url")
                    break
                }
            }
            if (apkUrl == null) return@withContext "No APK in release"

            val apkFile = File(context.externalCacheDir, "update.apk")
            URL(apkUrl).openStream().use { input ->
                apkFile.outputStream().use { output -> input.copyTo(output) }
            }

            withContext(Dispatchers.Main) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            "Installing v$remoteCode..."
        } catch (e: Exception) {
            "Update check failed: ${e.message}"
        }
    }
}
