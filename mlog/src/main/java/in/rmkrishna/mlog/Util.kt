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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Environment
import android.os.StatFs
import android.support.v4.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by muthukrishnan on 30/05/17.
 */


internal class Util {

    companion object {

        internal fun generateLogFilePath(): String {

            val folderName = getLoggingFolderOfThisApp()

            val f = File(folderName)

            // create the folder
            if (!f.isDirectory) {

                if (!f.mkdirs()) {
                    return "" // return empty string if fail.
                }
            }

            val timestamp = System.currentTimeMillis()

            // file name
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US)
            val fileName = folderName + File.separator + sdf.format(Date(timestamp)) + ".log"

            return fileName
        }

        internal fun getLoggingFolderOfThisApp(): String {

            val sb = StringBuilder(130).apply {
                append(Environment.getExternalStorageDirectory().getPath())
                append(File.separator)
                append(MLog.folderName)
                append(File.separator)
            }

            return sb.toString()
        }

        /**
         * This method will return the long time into Normal Date time with format
         * "2013 03 20 20:08:45".

         * @param time
         * *
         * *
         * @return
         */
        internal fun convertTime(time: Long): String {
            val date = Date(time)

            val format = SimpleDateFormat("yyyy MM dd HH:mm:ss", Locale.US)

            return format.format(date).toString()
        }

        /**
         * Get current application version name from package manager.

         * @return
         */
        internal fun getAppVersionName(): String? {
            val context = MLog.context

            context?.let { context ->
                var info = context.getPackageManager().getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)

                info?.let { info ->
                    return info.versionName
                }
            }

            return null
        }

        /**
         * Get current application version code from package manager.

         * @return
         */
        internal fun getAppVersionCode(): Int {
            val context = MLog.context

            context?.let { context ->
                var info = context.getPackageManager().getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)

                info?.let { info ->
                    return info.versionCode
                }
            }

            return -1
        }

        /**
         * @param requiredSize
         * *
         * *
         * @return
         */
        internal fun doesSdcardHasEnufSpace(requiredSize: Double): Boolean {
            val path = Environment.getExternalStorageDirectory() ?: return false

            val filePath = path.path ?: return false

            var stat: StatFs? = StatFs(filePath)

            stat?.let {
                val availSize = it.availableBlocks.toDouble() * it.blockSize.toDouble()

                if (requiredSize < availSize) {
                    return true
                }
            }

            return false
        }

        internal fun isSdCardPresent() = (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState())


        internal fun hasStorageWritePermission(): Boolean {
            MLog.context?.let {
                return ContextCompat.checkSelfPermission(it,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }

            return false
        }
    }
}

