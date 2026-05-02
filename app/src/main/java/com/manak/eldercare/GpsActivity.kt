package com.manak.eldercare

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import kotlin.math.sqrt

class GpsActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var tvZoneStatus: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCoords: TextView
    private lateinit var tvMoving: TextView
    private lateinit var tvAlertDistance: TextView
    private lateinit var cardOutsideAlert: CardView

    // Home location (Hyderabad)
    private val homeLocation = GeoPoint(17.3850, 78.4867)
    private val safeRadiusKm = 5.0

    // Simulated waypoints
    private val waypoints = listOf(
        GeoPoint(17.3850, 78.4867),
        GeoPoint(17.3900, 78.4920),
        GeoPoint(17.3950, 78.4970),
        GeoPoint(17.4000, 78.5020), // outside
        GeoPoint(17.4050, 78.5070), // further outside
        GeoPoint(17.4000, 78.5020),
        GeoPoint(17.3950, 78.4970),
        GeoPoint(17.3900, 78.4920),
        GeoPoint(17.3850, 78.4867), // back home
    )

    private var waypointIndex = 0
    private var elderMarker: Marker? = null
    private var homeMarker: Marker? = null
    private val trailPoints = mutableListOf<GeoPoint>()
    private var trailLine: Polyline? = null
    private val handler = Handler(Looper.getMainLooper())

    private val moveRunnable = object : Runnable {
        override fun run() {
            moveElder()
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OSMDroid config
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_gps)

        // Bind views
        mapView         = findViewById(R.id.mapView)
        tvZoneStatus    = findViewById(R.id.tvZoneStatus)
        tvDistance      = findViewById(R.id.tvDistance)
        tvStatus        = findViewById(R.id.tvStatus)
        tvCoords        = findViewById(R.id.tvCoords)
        tvMoving        = findViewById(R.id.tvMoving)
        tvAlertDistance = findViewById(R.id.tvAlertDistance)
        cardOutsideAlert = findViewById(R.id.cardOutsideAlert)

        // Back button
        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup map
        setupMap()

        // Start movement simulation
        handler.post(moveRunnable)
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(14.0)
        mapView.controller.setCenter(homeLocation)

        // Compass
        val compass = CompassOverlay(this, mapView)
        compass.enableCompass()
        mapView.overlays.add(compass)

        // Home marker
        homeMarker = Marker(mapView).apply {
            position = homeLocation
            title = "🏠 Home"
            snippet = "Safe zone center"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(homeMarker)

        // Safe zone circle
        val circle = org.osmdroid.views.overlay.Polygon().apply {
            val circlePoints = org.osmdroid.views.overlay.Polygon.pointsAsCircle(homeLocation, safeRadiusKm * 1000)
            points = circlePoints
            fillColor = 0x154ADE80.toInt()
            strokeColor = 0x804ADE80.toInt()
            strokeWidth = 2f
        }
        mapView.overlays.add(circle)

        // Elder marker
        elderMarker = Marker(mapView).apply {
            position = waypoints[0]
            title = "👵 Amma"
            snippet = "Current location"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(elderMarker)

        // Trail line
        trailLine = Polyline().apply {
            color = 0xFF4ADE80.toInt()
            width = 5f
        }
        mapView.overlays.add(trailLine)

        trailPoints.add(waypoints[0])
        mapView.invalidate()
    }

    private fun moveElder() {
        waypointIndex = (waypointIndex + 1) % waypoints.size
        val newPos = waypoints[waypointIndex]

        // Update marker
        elderMarker?.position = newPos

        // Update trail
        trailPoints.add(newPos)
        if (trailPoints.size > 20) trailPoints.removeAt(0)
        trailLine?.setPoints(trailPoints)

        // Calculate distance from home
        val distKm = distanceKm(homeLocation, newPos)

        // Update UI
        tvDistance.text = "%.2f km".format(distKm)
        tvCoords.text = "📍 %.4f°N, %.4f°E".format(newPos.latitude, newPos.longitude)

        val outside = distKm > safeRadiusKm
        if (outside) {
            tvZoneStatus.text = "● OUTSIDE SAFE ZONE"
            tvZoneStatus.setTextColor(0xFFFF5252.toInt())
            tvStatus.text = "Outside ⚠️"
            tvStatus.setTextColor(0xFFFF5252.toInt())
            cardOutsideAlert.visibility = View.VISIBLE
            tvAlertDistance.text = "%.1f km from home".format(distKm)
            trailLine?.color = 0xFFFF5252.toInt()
        } else {
            tvZoneStatus.text = "● WITHIN SAFE ZONE"
            tvZoneStatus.setTextColor(0xFF4ADE80.toInt())
            tvStatus.text = "Safe ✓"
            tvStatus.setTextColor(0xFF4ADE80.toInt())
            cardOutsideAlert.visibility = View.GONE
            trailLine?.color = 0xFF4ADE80.toInt()
        }

        // Pan map to follow elder
        mapView.controller.animateTo(newPos)
        mapView.invalidate()
    }

    private fun distanceKm(a: GeoPoint, b: GeoPoint): Double {
        val latDiff = (a.latitude - b.latitude) * 111.0
        val lngDiff = (a.longitude - b.longitude) * 111.0 *
                Math.cos(Math.toRadians(a.latitude))
        return sqrt(latDiff * latDiff + lngDiff * lngDiff)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        handler.removeCallbacks(moveRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(moveRunnable)
    }
}