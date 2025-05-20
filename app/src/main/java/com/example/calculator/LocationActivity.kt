package com.example.calculator

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.calculator.ui.theme.CalculatorTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class LocationEntry(val latitude: Double, val longitude: Double, val time: Long)

class LocationActivity : ComponentActivity() {

    private lateinit var fusedClient: FusedLocationProviderClient
    private val historyFileName = "location_history.json"
    private val externalFile by lazy { File(getExternalFilesDir(null), historyFileName) }

    private var _history = mutableStateOf<List<LocationEntry>>(emptyList())
    val history: State<List<LocationEntry>> get() = _history

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[ACCESS_FINE_LOCATION] == true
        val coarse = permissions[ACCESS_COARSE_LOCATION] == true
        if (fine || coarse) {
            fetchAndSaveLocation()
        } else {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                }
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            CalculatorTheme {
                LocationScreen(
                    historyState = history,
                    onRequestLocation = { requestLocationPermission() }
                )
            }
        }
    }

    private fun requestLocationPermission() {
        val needed = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed += ACCESS_FINE_LOCATION
        }
        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed += ACCESS_COARSE_LOCATION
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        } else {
            fetchAndSaveLocation()
        }
    }

    private fun fetchAndSaveLocation() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationActivity", "Location permission not granted")
            return
        }

        val cts = CancellationTokenSource()
        fusedClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { loc: Location? ->
                loc?.let {
                    val entry = LocationEntry(it.latitude, it.longitude, it.time)
                    val updatedHistory = loadHistory(externalFile).toMutableList().apply { add(0, entry) }
                    saveHistory(externalFile, updatedHistory)
                    _history.value = updatedHistory
                }
            }
            .addOnFailureListener { e ->
                Log.e("LocationActivity", "Failed to get location", e)
            }
    }

    private fun loadHistory(file: File): List<LocationEntry> {
        return try {
            if (!file.exists()) return emptyList()
            val text = file.readText()
            Json.decodeFromString(text)
        } catch (_: IOException) {
            emptyList()
        }
    }

    private fun saveHistory(file: File, entries: List<LocationEntry>) {
        try {
            val json = Json.encodeToString(entries)
            file.writeText(json)
            _history.value = emptyList()
        } catch (_: IOException) {
        }
    }
}

@Composable
fun LocationScreen(
    historyState: State<List<LocationEntry>>,
    onRequestLocation: () -> Unit
) {
    val history by historyState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = { onRequestLocation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Location History:")
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(history) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(entry.time))
                        Text("Lat: ${entry.latitude}")
                        Text("Lon: ${entry.longitude}")
                    }
                }
            }
        }
    }
}
