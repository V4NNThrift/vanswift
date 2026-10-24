package com.vanswift.backup.engine

import android.content.ContentValues
import android.content.Context
import android.provider.CallLog
import android.provider.Telephony
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TelephonyEngine(private val context: Context) {

    fun backupMessages(outputFile: File): Result<Int> = runCatching {
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE, Telephony.Sms.READ),
            null, null, null
        ) ?: return@runCatching 0

        val array = JSONArray()
        cursor.use {
            val iAddr = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val iBody = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val iDate = it.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val iType = it.getColumnIndexOrThrow(Telephony.Sms.TYPE)
            val iRead = it.getColumnIndexOrThrow(Telephony.Sms.READ)

            while (it.moveToNext()) {
                val obj = JSONObject().apply {
                    put("address", it.getString(iAddr))
                    put("body", it.getString(iBody))
                    put("date", it.getLong(iDate))
                    put("type", it.getInt(iType))
                    put("read", it.getInt(iRead))
                }
                array.put(obj)
            }
        }
        outputFile.writeText(array.toString(2))
        array.length()
    }

    fun restoreMessages(inputFile: File): Result<Int> = runCatching {
        val json = JSONArray(inputFile.readText())
        var restored = 0
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val cv = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, obj.optString("address"))
                put(Telephony.Sms.BODY, obj.optString("body"))
                put(Telephony.Sms.DATE, obj.optLong("date"))
                put(Telephony.Sms.TYPE, obj.optInt("type"))
                put(Telephony.Sms.READ, obj.optInt("read"))
            }
            context.contentResolver.insert(Telephony.Sms.CONTENT_URI, cv)
            restored++
        }
        restored
    }

    fun backupCallLogs(outputFile: File): Result<Int> = runCatching {
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION, CallLog.Calls.TYPE),
            null, null, null
        ) ?: return@runCatching 0

        val array = JSONArray()
        cursor.use {
            val iNum = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val iDate = it.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val iDur = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            val iType = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)

            while (it.moveToNext()) {
                val obj = JSONObject().apply {
                    put("number", it.getString(iNum))
                    put("date", it.getLong(iDate))
                    put("duration", it.getLong(iDur))
                    put("type", it.getInt(iType))
                }
                array.put(obj)
            }
        }
        outputFile.writeText(array.toString(2))
        array.length()
    }

    fun restoreCallLogs(inputFile: File): Result<Int> = runCatching {
        val json = JSONArray(inputFile.readText())
        var restored = 0
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val cv = ContentValues().apply {
                put(CallLog.Calls.NUMBER, obj.optString("number"))
                put(CallLog.Calls.DATE, obj.optLong("date"))
                put(CallLog.Calls.DURATION, obj.optLong("duration"))
                put(CallLog.Calls.TYPE, obj.optInt("type"))
            }
            context.contentResolver.insert(CallLog.Calls.CONTENT_URI, cv)
            restored++
        }
        restored
    }
}
