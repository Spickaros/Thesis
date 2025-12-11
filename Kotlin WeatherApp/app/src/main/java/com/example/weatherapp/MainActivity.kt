package com.example.weatherapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {

    private val apiKey = "4564573f304c4eea0a7aa5da9fed9074"
    private lateinit var weatherService: WeatherService
    private lateinit var map: MapView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        setContentView(R.layout.activity_main)

        val cityEditText = findViewById<EditText>(R.id.editTextCity)
        val searchButton = findViewById<Button>(R.id.buttonSearch)
        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()


        weatherService = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        searchButton.setOnClickListener {
            val city = cityEditText.text.toString()
            if (city.isNotBlank()) {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val weatherData = weatherService.getWeather(city, apiKey)
                        withContext(Dispatchers.Main) {
                            updateUI(weatherData)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(weatherData: WeatherData) {
        val cityText = findViewById<TextView>(R.id.textViewCity)
        val tempText = findViewById<TextView>(R.id.textViewTemperature)
        val iconImage = findViewById<ImageView>(R.id.imageViewWeatherIcon)
        val infoBox = findViewById<LinearLayout>(R.id.infoLayout)
        infoBox.visibility = LinearLayout.VISIBLE


        cityText.text = weatherData.name
        tempText.text = "${weatherData.main.temp.toInt()}°C"

        val lat = weatherData.coord.lat
        val lon = weatherData.coord.lon
        val geoPoint = GeoPoint(lat, lon)

        map.controller.setZoom(10.0)
        map.controller.setCenter(geoPoint)

        val iconUrl = "https://openweathermap.org/img/w/${weatherData.weather[0].icon}.png"

        Glide.with(this)
            .asBitmap()
            .load(iconUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val marker = Marker(map)
                    marker.position = geoPoint
                    marker.icon = BitmapDrawable(resources, resource)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = weatherData.name

                    map.overlays.clear()
                    map.overlays.add(marker)
                    map.invalidate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        // Also show icon in UI
        Glide.with(this).load(iconUrl).into(iconImage)
    }
}

// Data models
data class WeatherData(
    val name: String,
    val coord: Coord,
    val main: Main,
    val weather: List<Weather>
)

data class Coord(val lat: Double, val lon: Double)
data class Main(val temp: Double)
data class Weather(val icon: String)

// Retrofit API
interface WeatherService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherData
}
