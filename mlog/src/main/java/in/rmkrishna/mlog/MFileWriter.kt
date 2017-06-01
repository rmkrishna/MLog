/*
 * Copyright 2017 Muthukrishnan R
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package `in`.rmkrishna.mlog

import `in`.muthu.mlog.BuildConfig
import android.text.TextUtils
import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by muthukrishnan on 30/05/17.
 */
class MFileWriter {
    private val AVAILABLE_SPACE = 5.0 * 1024.0 * 1024.0

    private var customLogFile: BufferedWriter? = null
    private var mFormatter: SimpleDateFormat? = null

    internal fun open(): Unit {
        if (!isLoggable()) {
            return
        }

        mFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

        val fileName = Util.generateLogFilePath()

        if (TextUtils.isEmpty(fileName)) {
            return
        }

        val f = File(fileName)

        if (!f.exists()) {
            f.createNewFile()
        }

        customLogFile = BufferedWriter(FileWriter(f, true))

        val sb = StringBuilder(400).apply {
            append("-------------------------\n")
            append("Logging started Time : ")
            append(Util.convertTime(System.currentTimeMillis()))
            append("\n")
            append("Process ID : ").append(android.os.Process.myPid()).append("\n")
            append("Version code : ").append(Util.getAppVersionCode()).append("\n")
            append("Version name : ").append(Util.getAppVersionName()).append("\n")
            append("SDK Version : ").append(BuildConfig.VERSION_NAME).append("\n")
            append("\n")
        }


        logToFile("INIT", sb.toString())
    }

    internal fun logToFile(tag: String, msg: String?, th: Throwable? = null): Unit {
        if (customLogFile == null) {
            return
        }

        msg?.let { msg ->
            if (Util.doesSdcardHasEnufSpace(msg.toByteArray().size.toDouble())) {
                val logMsg = getFormattedMessage(tag, msg, th)

                if (customLogFile != null) {
                    val sb = StringBuilder(1024)

                    mFormatter?.let { mFormatter ->
                        val d = mFormatter.format(Date(System.currentTimeMillis()))

                        sb.append(d).append(" ")
                    }
                    sb.append(logMsg)

                    customLogFile?.let { customLogFile ->
                        customLogFile.write(sb.toString())

                        customLogFile.flush()
                    }
                }
            }
        }
    }

    private fun isLoggable(): Boolean {
        if (Util.isSdCardPresent()
                && Util.doesSdcardHasEnufSpace(AVAILABLE_SPACE)
                && Util.hasStorageWritePermission()) {
            return true
        }

        return false
    }

    /**
     * @param tag
     * *         Used to identify the source of a log message. It usually identifies the class
     * *         where the log call occurs.
     * *
     * @param msg
     * *         The message you would like logged.
     * *
     * @param th
     * *         An exception to log.
     * *
     * *
     * @return Formatted string.
     */
    private fun getFormattedMessage(tag: String, msg: String?, th: Throwable?): String {
        var msg = msg

        if (msg == null) {
            msg = ""
        }

        val buffer = StringBuffer(tag)
        buffer.append(":").append(msg)

        buffer.append("\r\n").append(Log.getStackTraceString(th))

        return buffer.toString()
    }
}