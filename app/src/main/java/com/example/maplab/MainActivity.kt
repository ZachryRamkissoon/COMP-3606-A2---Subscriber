package com.example.maplab

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback, UpdateLocation {
    private lateinit var mMap: GoogleMap
    val pointsList = mutableListOf<CustomMapPoint>()
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var publisherAdapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val mqttSubscriber = Subscriber(this, this)
        dbHelper = DatabaseHelper(this, null)
        val publisherView: RecyclerView = findViewById(R.id.rvStudents)
        val publisherList = getPublisherData()
        publisherAdapter = StudentAdapter(publisherList) { publisherData ->
            onViewMoreClicked(publisherData)
        }
        publisherView.layoutManager = LinearLayoutManager(this)
        publisherView.adapter = publisherAdapter

    }

    private fun drawPolyline() {
        val latLngPoints = pointsList.map { it.point }

        val polylineOptions = PolylineOptions()
            .addAll(latLngPoints)
            .color(Color.BLUE)
            .width(5f)
            .geodesic(true)

        mMap.addPolyline(polylineOptions)

        val bounds = LatLngBounds.builder()
        latLngPoints.forEach { bounds.include(it) }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
    }

    private fun drawPolylinesForAllStudents() {
        val studentLocationMap = dbHelper.getAllLocationsGroupedByStudent()

        for ((studentID, locations) in studentLocationMap) {
            val latLngPoints = locations.map { LatLng(it.latitude, it.longitude) }

            val polylineOptions = PolylineOptions()
                .addAll(latLngPoints)
                .color(getColorForStudent(studentID))
                .width(5f)
                .geodesic(true)

            mMap.addPolyline(polylineOptions)
        }
    }

    private fun getColorForStudent(studentID: String): Int {
        val colors = listOf(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.BLACK)
        val index = Math.abs(studentID.hashCode() % colors.size)
        return colors[index]
    }

    private fun getPublisherData(): MutableList<PStudent> {
        val locationMap = dbHelper.getAllLocationsGroupedByStudent()
        val publisherList: MutableList<PStudent> = mutableListOf()
        for((studentID, locations) in locationMap) {
            var max = Double.MIN_VALUE
            var min = Double.MAX_VALUE
            for (location in locations) {
                if (location.speed > max) {
                    max = location.speed
                } else if (location.speed < min) {
                    min = location.speed
                }
            }
            publisherList.add(PStudent(studentID, min, max))
        }
        return publisherList
    }

    private fun onViewMoreClicked(publisherData: PStudent) {
        // Handle "View More" click
        Toast.makeText(this, "View More clicked for ${publisherData.studentID}", Toast.LENGTH_SHORT).show()
        navigateToNextPage()
    }

    private fun updatePublisherList(publisherList: MutableList<PStudent>) {
        publisherAdapter.updatePublisherList(publisherList)
    }

    private fun navigateToNextPage(){
        println("DO")
        val i = Intent(this,ViewMoreActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(i)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        drawPolylinesForAllStudents()
    }

    override fun onNewLocationReceived(id: String) {
        val locations = dbHelper.getAllLocations(id)
        val data = getPublisherData()
        for (location in locations) {
            pointsList.add(CustomMapPoint(pointsList.size+1, LatLng(location.latitude, location.longitude)))
        }
        runOnUiThread {
            drawPolyline()
            updatePublisherList(data)
        }
    }
}

