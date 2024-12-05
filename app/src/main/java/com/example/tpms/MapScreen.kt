package com.example.tpms

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun MapScreen(tempRef: DatabaseReference) {
    // Mutable state to hold latitude and longitude
    val latitudeState = remember { mutableStateOf(13.0827) } // Default: Chennai latitude
    val longitudeState = remember { mutableStateOf(80.2707) } // Default: Chennai longitude

    // Fetch data from Firebase
    DisposableEffect(tempRef) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorData = snapshot.getValue(SensorData::class.java)
                if (sensorData != null) {
                    // Log the values before updating state
                    Log.d("MapScreen", "Fetched Latitude: ${sensorData.latitude}, Longitude: ${sensorData.longitude}")

                    // Update the states with new data from Firebase
                    sensorData.latitude?.let {
                        latitudeState.value = it.toDouble()
                    }
                    sensorData.longitude?.let {
                        longitudeState.value = it.toDouble()
                    }

                    // Log updated state values
                    Log.d("MapScreen", "Updated LatitudeState: ${latitudeState.value}, LongitudeState: ${longitudeState.value}")
                } else {
                    Log.e("MapScreen", "SensorData is null")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MapScreen", "Error fetching data from Firebase", error.toException())
            }
        }

        // Attach the listener to the Firebase reference
        tempRef.addValueEventListener(listener)

        // Remove the listener when the composable is disposed
        onDispose {
            tempRef.removeEventListener(listener)
        }
    }

    // Render the map with the updated latitude and longitude
    OpenStreetMapScreen(
        latitude = latitudeState.value,
        longitude = longitudeState.value,
        context = LocalContext.current
    )
}

@Composable
fun OpenStreetMapScreen(latitude: Double, longitude: Double, context: Context) {
    // Initialize osmdroid
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

    // Create and display the MapView
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                // Set tile source, you can change to other OSM tile providers if needed
                setTileSource(TileSourceFactory.MAPNIK)

                // Enable zoom controls
                setMultiTouchControls(true)

                // Set initial map zoom and position
                controller.setZoom(13.0) // Adjust zoom level if needed
                controller.setCenter(GeoPoint(latitude, longitude))

                // Optional: Add a marker at the specified location
                val marker = org.osmdroid.views.overlay.Marker(this)
                marker.position = GeoPoint(latitude, longitude)
                marker.title = "Selected Location"
                overlays.add(marker)

                // Refresh the map to reflect changes
                invalidate()
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            // Update the map view with the new coordinates
            mapView.controller.setCenter(GeoPoint(latitude, longitude))

            // Update marker position
            mapView.overlays.filterIsInstance<org.osmdroid.views.overlay.Marker>().forEach { marker ->
                marker.position = GeoPoint(latitude, longitude)
            }

            // Refresh the map to reflect changes
            mapView.invalidate()
        }
    )
}


