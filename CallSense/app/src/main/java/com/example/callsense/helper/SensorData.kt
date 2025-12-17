package com.example.callsense.helper

import android.content.Context
import android.os.Environment
import android.util.Log
import io.esense.esenselib.ESenseConfig
import io.esense.esenselib.ESenseEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.io.IOException


object SensorData {
    private val timestamps = mutableListOf<Long>()
    private val axList = mutableListOf<Float>()
    private val ayList = mutableListOf<Float>()
    private val azList = mutableListOf<Float>()
    private val gxList = mutableListOf<Float>()
    private val gyList = mutableListOf<Float>()
    private val gzList = mutableListOf<Float>()
    private var gx: Float? = null
    private var gy: Float? = null
    private var gz: Float? = null
    private var ax: Float? = null
    private var ay: Float? = null
    private var az: Float? = null
    var record = false
    private const val TAG = "CT#SensorData"

    fun setData(event: ESenseEvent) {
        if (record) {
            val scaledAcceleration = event.convertAccToG(ESenseConfig())
            val scaledGyro = event.convertGyroToDegPerSecond(ESenseConfig())
            ax = scaledAcceleration[0].toFloat()
            ay = scaledAcceleration[1].toFloat()
            az = scaledAcceleration[2].toFloat()
            gx = scaledGyro[0].toFloat()
            gy = scaledGyro[1].toFloat()
            gz = scaledGyro[2].toFloat()
//            ax = event.accel[0].toFloat()
//            ay = event.accel[1].toFloat()
//            az = event.accel[2].toFloat()
//            gx = event.gyro[0].toFloat()
//            gy = event.gyro[1].toFloat()
//            gz = event.gyro[2].toFloat()
            val timestamp = System.currentTimeMillis()
            Log.i(TAG, "accel: ax: $ax, ay: $ay, az: $az ---- gyro: gx: $gx, gy: $gy, gz: $gz, time: $timestamp")
            combineData(timestamp)
        }

    }

    fun startRecord() {
        Log.i(TAG, "startRecord()")
        ax = null
        ay = null
        az = null
        gx = null
        gy = null
        gz = null
        axList.clear()
        ayList.clear()
        azList.clear()
        gxList.clear()
        gyList.clear()
        gzList.clear()
        timestamps.clear()
        record = true
    }

    fun stopAndSendData(context: Context) : String? {
        Log.i(TAG, "stopAndSend()")
        record = false
        val jsonObject = JSONObject().apply {
            put("timestamp", JSONArray(timestamps))
            put("ax", JSONArray(axList))
            put("ay", JSONArray(ayList))
            put("az", JSONArray(azList))
            put("gx", JSONArray(gxList))
            put("gy", JSONArray(gyList))
            put("gz", JSONArray(gzList))
        }
        var downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.i(TAG, "downloadDir: $downloadsDir")
        val jsonFile = File(downloadsDir, "lip_${System.currentTimeMillis()}.json")

        try {
            FileWriter(jsonFile).use { writer ->
                writer.write(jsonObject.toString())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        clearData()
        val httpClient = HttpClient()
        val gesture  = httpClient.gestureRecognizer(context, jsonObject)
        return gesture

    }

    private fun clearData() {
        ax = null
        ay = null
        az = null
        gx = null
        gy = null
        gz = null
        axList.clear()
        ayList.clear()
        azList.clear()
        gxList.clear()
        gyList.clear()
        gzList.clear()
        timestamps.clear()
    }

    private fun combineData(time: Long) {
        if (ax != null && ay != null && az != null && gx != null && gy != null && gz != null) {
            timestamps.add(time)
            axList.add(ax ?: 0f)
            ayList.add(ay ?: 0f)
            azList.add(az ?: 0f)
            gxList.add(gx ?: 0f)
            gyList.add(gy ?: 0f)
            gzList.add(gz ?: 0f)
            ax = null
            ay = null
            az = null
            gx = null
            gy = null
            gz = null
        }
    }
}