package com.example.callsense.service

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import com.example.callsense.helper.HttpClient
import com.example.callsense.helper.SensorData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CTInCallService: InCallService() {
    enum class Gestures {
        NOD,
        SHAKE,
        TILT,
        LIP_PULLER
    }
    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)
        Log.i(TAG, "new call added")
        answerCall(call)
    }

    private fun answerCall(call: Call?) {
        CoroutineScope(Dispatchers.IO).launch {
            SensorData.startRecord()
            delay(3000)
            val gesture = SensorData.stopAndSendData(this@CTInCallService)
            Log.i(TAG, "gesture: $gesture")
            if (gesture?.contains("nod") == true) {
                call?.reject(false, null)
            } else if (gesture?.contains("shake") == true ) {
                call?.answer(0)
            } else {
                call?.reject(true, "I am busy with wireless project, will call later")
            }
        }

    }

    companion object {
        private const val TAG = "CT#CTInCallService"
    }
}