package org.fcitx.fcitx5.android.input

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SecLogger {
    private const val TAG = "SecIME"
    private const val LOG_FILE = "secime_debug.log"
    private const val MAX_SIZE = 2 * 1024 * 1024L // 2MB rotate

    private var writer: FileWriter? = null
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        try {
            val dir = context.getExternalFilesDir(null) ?: context.filesDir
            val logFile = File(dir, LOG_FILE)
            if (logFile.length() > MAX_SIZE) {
                logFile.writeText("")
            }
            writer = FileWriter(logFile, true)
            d("SecLogger", "=== log initialized, path=${logFile.absolutePath} ===")
        } catch (e: Exception) {
            Log.e(TAG, "SecLogger init failed", e)
        }
    }

    fun d(tag: String, msg: String) {
        Log.d(TAG, "[$tag] $msg")
        write("D", tag, msg)
    }

    fun e(tag: String, msg: String, thr: Throwable? = null) {
        Log.e(TAG, "[$tag] $msg", thr)
        write("E", tag, msg + (thr?.let { " | ${it.message}" } ?: ""))
    }

    private fun write(level: String, tag: String, msg: String) {
        try {
            val w = writer ?: return
            val ts = dateFormat.format(Date())
            w.write("$ts $level/$tag: $msg\n")
            w.flush()
        } catch (_: Exception) {
        }
    }
}