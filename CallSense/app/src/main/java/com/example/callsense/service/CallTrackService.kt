package com.example.callsense.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.callsense.helper.HttpClient
import com.example.callsense.helper.SensorData
import io.esense.esenselib.ESenseConnectionListener
import io.esense.esenselib.ESenseEvent
import io.esense.esenselib.ESenseManager
import io.esense.esenselib.ESenseSensorListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class CallTrackService : Service() {
    private lateinit var manager : ESenseManager
    private var connectionListener  = object : ESenseConnectionListener {
        override fun onDeviceFound(manager: ESenseManager?) {
            Log.i(TAG, "device found")
        }

        override fun onDeviceNotFound(manager: ESenseManager?) {
            Log.i(TAG, "device not found")
        }

        override fun onConnected(manager: ESenseManager?) {
            Log.i(TAG, "device connected")
            manager?.registerSensorListener(senseListener, 100)
        }

        override fun onDisconnected(manager: ESenseManager?) {
            Log.i(TAG, "device disconnected")
        }
    }

    private var senseListener = ESenseSensorListener {event ->
        SensorData.setData(event)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("CallTrackService", "Service started")
        val notification = getNotification()
        manager = ESenseManager("eSense-0591", this.applicationContext, connectionListener)
        CoroutineScope(Dispatchers.IO).launch {
            manager.connect(5000)
        }

        ServiceCompat.startForeground(this, 100, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)

        // If killed, restart service if needed
        return START_STICKY
    }

    private fun getNotification(): Notification {
        val channel = NotificationChannel(
            "CHANNEL_ID",              // Your channel ID
            "Call Tracking",           // User-visible name
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = this.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        // Build the notification
        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Call Tracker Running")
            .setContentText("Tracking your calls...")
            .setSmallIcon(android.R.drawable.alert_dark_frame)
            .build()
        return notification
    }

    override fun onDestroy() {
        Log.i("CallTrackService", "onDestroy")
        super.onDestroy()
    }

    companion object {
        const val TAG = "CT#CallTrackService"
    }

}