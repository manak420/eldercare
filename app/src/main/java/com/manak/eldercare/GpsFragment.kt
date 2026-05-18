package com.manak.eldercare

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.compass.CompassOverlay
import kotlin.math.sqrt

class GpsFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var tvZoneStatus: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvCoords: TextView
    private lateinit var tvAlertDistance: TextView
    private lateinit var cardOutsideAlert: CardView

    private val homeLocation = GeoPoint(28.374112, 77.325222)
    private val safeRadiusKm = 5.0

    private var elderMarker: Marker? = null
    private val trailPoints = mutableListOf<GeoPoint>()
    private var trailLine: Polyline? = null
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance().userAgentValue = requireContext().packageName
        return inflater.inflate(R.layout.fragment_gps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView          = view.findViewById(R.id.mapView)
        tvZoneStatus     = view.findViewById(R.id.tvZoneStatus)
        tvDistance       = view.findViewById(R.id.tvDistance)
        tvStatus         = view.findViewById(R.id.tvStatus)
        tvCoords         = view.findViewById(R.id.tvCoords)
        tvAlertDistance  = view.findViewById(R.id.tvAlertDistance)
        cardOutsideAlert = view.findViewById(R.id.cardOutsideAlert)

        database = FirebaseDatabase.getInstance("https://eldercare-84c09-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("vitals")
        setupMap()
        listenForLocation()
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.zoomController.setVisibility(
            org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS
        )
        mapView.controller.setZoom(14.0)
        mapView.controller.setCenter(homeLocation)

        // Compass
        val compass = CompassOverlay(requireContext(), mapView)
        compass.enableCompass()
        mapView.overlays.add(compass)

        // Home marker
        val homeMarker = Marker(mapView).apply {
            position = homeLocation
            title = "🏠 Home"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(homeMarker)

        // Safe zone circle
        val circle = org.osmdroid.views.overlay.Polygon().apply {
            points = org.osmdroid.views.overlay.Polygon
                .pointsAsCircle(homeLocation, safeRadiusKm * 1000)
            fillColor   = 0x154ADE80.toInt()
            strokeColor = 0x804ADE80.toInt()
            strokeWidth = 2f
        }
        mapView.overlays.add(circle)

        // Elder marker
        elderMarker = Marker(mapView).apply {
            position = homeLocation
            title = "👵 Amma"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(elderMarker)

        // Trail
        trailLine = Polyline().apply {
            color = 0xFF4ADE80.toInt()
            width = 5f
        }
        mapView.overlays.add(trailLine)
        trailPoints.add(homeLocation)
        mapView.invalidate()
    }

    private fun listenForLocation() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("lat").getValue(Double::class.java) ?: return
                val lng = snapshot.child("lng").getValue(Double::class.java) ?: return

                val newPos = GeoPoint(lat, lng)

                // Update marker
                elderMarker?.position = newPos

                // Update trail
                trailPoints.add(newPos)
                if (trailPoints.size > 30) trailPoints.removeAt(0)
                trailLine?.setPoints(trailPoints)

                // Calculate distance
                val distKm = distanceKm(homeLocation, newPos)
                tvDistance.text = "%.2f km".format(distKm)
                tvCoords.text = "📍 %.4f°N, %.4f°E".format(lat, lng)

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

                mapView.controller.animateTo(newPos)
                mapView.invalidate()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
    }
}