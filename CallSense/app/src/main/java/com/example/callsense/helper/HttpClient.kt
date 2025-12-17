package com.example.callsense.helper

import android.content.Context
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets

class HttpClient {

    fun gestureRecognizer(context: Context, data: JSONObject? = null): String? {
        var jsonObject = data
        if (jsonObject == null) {
            val jsonString = loadJsonFromAssets(context, "1-nod-dummy.txt")
            jsonObject = JSONObject(jsonString)
        }
        val apiUrl = "https://postpaludal-prayerful-temeka.ngrok-free.dev/predict"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(apiUrl)
            .post(
                jsonObject.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
            )
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("HTTP error: ${response.code}: ${response.body.string()}")
            }
            return response.body.string()
        }
    }

    fun loadJsonFromAssets(context: Context, fileName: String): String {
        var json: String
        try {
            val `is` = context.assets.open(fileName)
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            json = String(buffer, StandardCharsets.UTF_8)
        } catch (ex: IOException) {
            Log.i(TAG, "$ex")
            ex.printStackTrace()
            return ""
        }
        return json
    }

    companion object {
        private const val TAG = "CT#HttpClient"
    }
}