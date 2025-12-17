package com.example.callsense.activity

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.ComponentCaller
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
//import com.example.callsense.helper.HttpClient
import com.example.callsense.helper.SensorData
import com.example.callsense.service.CallTrackService
import com.example.callsense.ui.theme.CallSenseTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestUserPermission()

        enableEdgeToEdge()
        setContent {
            CallSenseTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                    )
                }
            }
        }
    }

    val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.i(TAG, "permission granted")
        }
    }

    fun requestUserPermission() {
        requestPermission.launch(Manifest.permission.MODIFY_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun HomeScreen() {
        val context = LocalContext.current
        Box(
            modifier = Modifier.Companion.fillMaxSize(),
            contentAlignment = Alignment.Companion.Center
        ) {
            Column(modifier = Modifier.Companion.padding(vertical = 15.dp)) {
                Button(
                    onClick = {
                        requestDefaultDialerRole()
                        context.startService(Intent(context, CallTrackService::class.java))
                    },
                    contentPadding = PaddingValues(horizontal = 15.dp),
                    modifier = Modifier.Companion
                        .padding(30.dp)
                        .height(50.dp)
                        .width(300.dp)
                ) {
                    Row(
                        modifier = Modifier.Companion.padding(horizontal = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "start tracking"
                        )
                        Text("Start Tracking")

                    }
                }

                Button(
                    onClick = {
                        context.stopService(Intent(context, CallTrackService::class.java))
                        context.startActivity(Intent(context, CTDialerActivity::class.java))
                    },
                    contentPadding = PaddingValues(horizontal = 15.dp),
                    modifier = Modifier.Companion
                        .padding(30.dp)
                        .height(50.dp)
                        .width(300.dp)
                ) {
                    Row(
                        modifier = Modifier.Companion.padding(horizontal = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "start tracking"
                        )
                        Text("Stop Tracking")

                    }
                }
                Button(
                    onClick = {
                        SensorData.startRecord()
                    },
                    contentPadding = PaddingValues(horizontal = 15.dp),
                    modifier = Modifier.Companion
                        .padding(30.dp)
                        .height(50.dp)
                        .width(300.dp)
                ) {
                    Row(
                        modifier = Modifier.Companion.padding(horizontal = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "start record"
                        )
                        Text("Start Record")
                    }
                }

                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            val gesture = SensorData.stopAndSendData(this@MainActivity)
                            Log.i(TAG, "gesture: $gesture")
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(this@MainActivity, "gesture: $gesture", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 15.dp),
                    modifier = Modifier.Companion
                        .padding(30.dp)
                        .height(50.dp)
                        .width(300.dp)
                ) {
                    Row(
                        modifier = Modifier.Companion.padding(horizontal = 15.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "stop record"
                        )
                        Text("Stop Record")

                    }
                }
            }
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf<String?>(
                ACCESS_FINE_LOCATION,
            ), 200
        )
    }

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Check if the user accepted
        Log.i(TAG, "onResult: $result")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestDefaultDialerRole() {
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)) {
            if (roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
                Toast.makeText(this, "Already Default Dialer", Toast.LENGTH_SHORT).show()
            } else {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                roleRequestLauncher.launch(intent)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        Log.i(TAG, "requestCode: $requestCode, caller: $caller")
        if (requestCode == REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                Log.i(TAG, "Your app is now the call screening app")
            } else {
                Log.i(TAG, "failed")
            }
        }
        super.onActivityResult(requestCode, resultCode, data, caller)
    }

    companion object {
        const val REQUEST_ID = 83
        const val TAG = "CT#MainActivity"
    }
}