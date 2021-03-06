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
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KProperty


/**
 * Created by muthukrishnan on 26/05/17.
 */
class MLog {



    companion object {

        private val MAX_LOG_LENGTH = 4000
        private val JSON_INDENT = 4

        /**
         * Maximum Tag length for a log
         */
        private val MAX_TAG_LENGTH = 23

        internal var context: Context? = null
        private var isLogEnable: Boolean = false
        internal var isFileLogEnable: Boolean by Delegate()
        internal var folderName: String? = "MLog"

        class Delegate() {
            var fileLoggable: Boolean = false

            operator fun getValue(thisRef: Any?, prop: KProperty<*>): Boolean {
                return fileLoggable
            }

            operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: Boolean) {
                fileLoggable = value

                if (fileLoggable) {
                    MFileLog.init()
                }
            }
        }

        public fun init(context: Context, isLogEnable: Boolean, isFileLogEnable: Boolean = false, folder: String? = "MLog") {
            Companion.context = context
            Companion.isLogEnable = isLogEnable

            Companion.isFileLogEnable = isFileLogEnable

            folderName = folder
        }

        public fun setFileLoggable(isFileLogEnable: Boolean): Unit {
            Companion.isFileLogEnable = isFileLogEnable
        }

        public fun d(tag: String?, message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.DEBUG, tag, message, throwable)
        }

        public fun d(message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.DEBUG, null, message, throwable)
        }

        public fun v(tag: String?, message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.VERBOSE, tag, message, throwable)
        }

        public fun v(message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.VERBOSE, null, message, throwable)
        }

        public fun i(tag: String?, message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.INFO, tag, message, throwable)
        }

        public fun i(message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.INFO, null, message, throwable)
        }

        public fun w(tag: String?, message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.WARN, tag, message, throwable)
        }

        public fun w(message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.WARN, null, message, throwable)
        }

        public fun e(tag: String?, message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.ERROR, tag, message, throwable)
        }

        public fun e(message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.ERROR, null, message, throwable)
        }

        public fun a(tag: String?, message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.ASSERT, tag, message, throwable)
        }

        public fun a(message: Any, throwable: Throwable? = null): Unit {
            loggable(Log.ASSERT, null, message, throwable)
        }

        private fun getMessage(any: Any): String {
            any?.let {
                when (any) {
                    is String -> {
                        return any
                    }
                    is JSONObject -> {
                        var jsonObj = any

                        return jsonObj.toString(JSON_INDENT)
                    }
                    is JSONArray -> {
                        var jsonArray = any

                        return jsonArray.toString(JSON_INDENT)
                    }
                    else -> {
                        return any.toString()
                    }
                }
            }
        }

        private fun loggable(priority: Int, tagValue: String?, any: Any, throwable: Throwable? = null): Unit {

            var tag: String? = tagValue;

            if (tag == null) {
                val stackTrace = Throwable().stackTrace

                val minSize: Int = 2

                if (stackTrace.size > minSize) {
                    var className = stackTrace[minSize + 1].className;
                    var methodName = stackTrace[minSize + 1].methodName;

                    className = className?.substring(className?.lastIndexOf('.') + 1) + "-" + methodName

                    tag = className;
                }
            }

            var message: String? = getMessage(any)

            if (message == null || tag == null) {
                return
            }

            tag = formatTag(tag)

            message?.let {
                if (it.length > MAX_LOG_LENGTH) {
                    logAsChunk(priority, tag, it, throwable)

                    return
                }
            }

            if (isLogEnable) {
                when (priority) {
                    Log.VERBOSE -> if (throwable == null) Log.v(tag, message) else Log.v(tag, message, throwable)
                    Log.DEBUG -> if (throwable == null) Log.d(tag, message) else Log.d(tag, message, throwable)
                    Log.ERROR -> if (throwable == null) Log.e(tag, message) else Log.e(tag, message, throwable)
                    Log.INFO -> if (throwable == null) Log.i(tag, message) else Log.i(tag, message, throwable)
                    Log.WARN -> if (throwable == null) Log.w(tag, message) else Log.w(tag, message, throwable)
                    Log.ASSERT -> if (throwable == null) Log.wtf(tag, message) else Log.wtf(tag, message, throwable)
                }
            }

            if (!isFileLogEnable || context == null) {
                return
            }

            context?.let {
                if (ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    return
                }
            }

            // Write in file here
            if (isFileLogEnable) {

                MFileLog.log(tag, message, throwable)
            }
        }

        private fun formatTag(tag: String) = if (tag.length > MAX_TAG_LENGTH) tag.substring(0, MAX_TAG_LENGTH) else tag

        private fun logAsChunk(priority: Int, tagValue: String?, messageValue: String, throwable: Throwable? = null): Unit {
            if (messageValue.length == 0) {
                return
            }

            var i: Int = 0
            val length: Int = messageValue.length

            while (i < length) {
                var newline = messageValue?.indexOf('\n', i)

                newline = if (newline != -1) newline else length

                do {
                    val end: Int = Math.min(newline, i + MAX_LOG_LENGTH)

                    val part: String = messageValue.substring(i, end)

                    loggable(priority, tagValue, part, throwable)
                    i = end
                } while (i < newline)

                i++
            }
        }
    }
}