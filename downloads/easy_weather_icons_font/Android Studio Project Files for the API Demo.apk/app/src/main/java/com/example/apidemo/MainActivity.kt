package com.example.apidemo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.AlignmentSpan
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    // create values
    // api key (put your openweathermap key here)
    private var apiKey: String? = "73cbebdd0322acd49bda6ede059b2b18"

    // use this code for user city input (also use the api call for city with myExecutor in apiWeatherCall() further below)
    private var cityUserInput: String? = "London"
    private var city: EditText? = null

    // use this code for latitude and longitude (also use the api call for latitude and longitude with myExecutor in apiWeatherCall() further below)
    // this is just a fixed location London example, add extra code to find your location and you can use your actual current GPS position here
    //    private var latitude = 51.507351
    //    private var longitude = -0.127758


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiWeatherCall() // calls the default city "London" to populate app prior to any user input

        city = findViewById(R.id.inputEditTextForCity)
        city?.setText(cityUserInput.toString())


        // soft keyboard listener
        // on pressing the enter key.
        city?.setOnKeyListener { _, keyCode, event ->
            // If the event is a key-down event on the "enter" button
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                // Perform action on key press
                 cityUserInput = city?.text.toString()
                if (TextUtils.isEmpty(cityUserInput)) {
                    Toast.makeText(this,"Oops, Try entering a different location?", Toast.LENGTH_SHORT).show()
                }

                apiWeatherCall() // gets the weather from an api call to openweathermap

                true
            } else {
                false
            }
        }

        // button
        val refreshButton = findViewById<View>(R.id.button) as Button
        refreshButton.setOnClickListener {

            cityUserInput = city?.text.toString()
            if (TextUtils.isEmpty(cityUserInput)) {
                Toast.makeText(this,"Oops, Try entering a different location?", Toast.LENGTH_SHORT).show()
            }

            // close soft keyboard if open when button pressed
            val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = this.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(this)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)

        apiWeatherCall() // gets the weather from an api call to openweathermap

        Toast.makeText(this, "Refresh Button Pressed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun apiWeatherCall() {

        myExecutor.execute {
            // use this if using city user input
            val result = HttpRequest.executeGet("https://api.openweathermap.org/data/2.5/weather?q=$cityUserInput&units=metric&appid=$apiKey").toString() // userCity

            // use this if using latitude and longitude
            // val result = HttpRequest.executeGet("https://api.openweathermap.org/data/2.5/weather?lat=$latitude&lon=$longitude&units=metric&appid=$apiKey").toString()

            myHandler.post {

                try {
                    // create JSON objects from the API response
                    val jsonObj = JSONObject(result)
                    val main = jsonObj.getJSONObject("main")
                    val sys = jsonObj.getJSONObject("sys")
                    val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                    // create variables using strings and data from the JSON object
                    var location = jsonObj.getString("name") // + ", " + sys.getString("country");
                    val temperature = String.format("%.0f", main.getDouble("temp")) + "°C"
                    var description = weather.getString("description") // String icon = weather.getString("main");
                    val sunrise = sys.getLong("sunrise")
                    val sunset = sys.getLong("sunset")
                    val updatedAt = jsonObj.getLong("dt")
                    val updatedAtFormatted = "Updated:   " + SimpleDateFormat("HH:mm   dd/MM/yy", Locale.ENGLISH).format(updatedAt * 1000)

                    // use sunrise, sunset and updatedAt variables above to create a tag for night time:
                    val nightTag: Boolean = updatedAt !in sunrise..<sunset

                    if ("Globe".equals(location, ignoreCase = true)) {
                        location = "Not Available"
                        description = "Not Available"
                    }
                    if ("Greater London".equals(location, ignoreCase = true)) {
                        location = "London" // here just to shorten this response
                    }


                    // convert openweathermap descriptions to easy_weather_icons_font
                    // (feel free to use this font conversion list in your own app when using easy_weather_icons_font.)
                    var icon = "\uE900" // creates icon string and sets default icon to: (N/A)

                    if (description == "") { icon = "\uE900" } // sets empty icon to: (N/A)
                    if (description == null) { icon = "\uE900" } // sets null icon to: (N/A) keep this just in case input is ever redefined as null
                    if (description == "clear sky") { icon = "\uE96D" } // CLEARday
                    if (description == "clear sky" && nightTag) { icon = "\uE96E" } // CLEARnight
                    if (description == "few clouds") { icon = "\uE967" } // PARTLY_CLOUDYday (few clouds: 11-25%)
                    if (description == "few clouds" && nightTag) { icon = "\uE968" } // PARTLY_CLOUDYnight (few clouds: 11-25%)
                    if (description == "scattered clouds") { icon = "\uE9D3" } // CLOUD2day (scattered clouds: 25-50%)
                    if (description == "scattered clouds" && nightTag) { icon = "\uE9D4" } // CLOUD2night (scattered clouds: 25-50%)
                    if (description == "broken clouds") { icon = "\uE9DF" } // CLOUDY2day (broken clouds: 51-84%)
                    if (description == "broken clouds" && nightTag) { icon = "\uE9E0" } // CLOUDY2night (broken clouds: 51-84%)
                    if (description == "overcast clouds") { icon = "\uE961" } // MOSTLY_CLOUDYday (overcast clouds: 85-100%)
                    if (description == "overcast clouds" && nightTag) { icon = "\uE962" } // MOSTLY_CLOUDYnight (overcast clouds: 85-100%)
                    if (description == "shower rain") { icon = "\uE928" } // SHOWERSday
                    if (description == "shower rain" && nightTag) { icon = "\uE929" } // SHOWERSnight
                    if (description == "rain") { icon = "\uE92E" } // RAINday
                    if (description == "rain" && nightTag) { icon = "\uE932" } // RAINnight
                    if (description == "thunderstorm") { icon = "\uE910" } // THUNDERSTORMSday
                    if (description == "thunderstorm" && nightTag) { icon = "\uE911" } // THUNDERSTORMSnight
                    if (description == "snow") { icon = "\uE940" } // SNOWday
                    if (description == "snow" && nightTag) { icon = "\uE941" } // SNOWnight
                    if (description == "mist") { icon = "\uEA42" } // mistday
                    if (description == "mist" && nightTag) { icon = "\uEA43" } // mistnight
                    if (description == "thunderstorm with light rain") {icon = "\uEA60" } // thunderstorm_with_light_rainday
                    if (description == "thunderstorm with light rain" && nightTag) { icon = "\uEA61" } // thunderstorm_with_light_rainnight
                    if (description == "thunderstorm with rain") { icon = "\uEA63" } // thunderstorm_with_rainday
                    if (description == "thunderstorm with rain" && nightTag) { icon = "\uEA64" } // thunderstorm_with_rainnight
                    if (description == "thunderstorm with heavy rain") { icon = "\uEA66" } // thunderstorm_with_heavy_rainday
                    if (description == "thunderstorm with heavy rain" && nightTag) { icon = "\uEA67" } // thunderstorm_with_heavy_rainnight
                    if (description == "light thunderstorm") { icon = "\uE910" } // THUNDERSTORMSday
                    if (description == "light thunderstorm" && nightTag) { icon = "\uE911" } // THUNDERSTORMSnight
                    if (description == "heavy thunderstorm") { icon = "\uE90D" } // SEVERE_THUNDERSTORMSday
                    if (description == "heavy thunderstorm" && nightTag) { icon = "\uE90E" } // SEVERE_THUNDERSTORMSnight
                    if (description == "ragged thunderstorm") { icon = "\uEA69" } // ragged_thunderstormday
                    if (description == "ragged thunderstorm" && nightTag) { icon = "\uEA6A" } // ragged_thunderstormnight
                    if (description == "thunderstorm with light drizzle") { icon = "\uE982" } // THUNDERSHOWERSday
                    if (description == "thunderstorm with light drizzle" && nightTag) { icon = "\uE983" } // THUNDERSHOWERSnight
                    if (description == "thunderstorm with drizzle") { icon = "\uEA6C" } // thunderstorm_with_drizzleday
                    if (description == "thunderstorm with drizzle" && nightTag) { icon = "\uEA6D" } // thunderstorm_with_drizzlenight
                    if (description == "thunderstorm with heavy drizzle") { icon = "\uE982" } // THUNDERSHOWERSday
                    if (description == "thunderstorm with heavy drizzle" && nightTag) { icon = "\uE983" } // THUNDERSHOWERSnight
                    if (description == "light intensity drizzle") { icon = "\uE922" } // DRIZZLEday
                    if (description == "light intensity drizzle" && nightTag) { icon = "\uE923" } // DRIZZLEnight
                    if (description == "drizzle") { icon = "\uE922" } // DRIZZLEday
                    if (description == "drizzle" && nightTag) { icon = "\uE923" } // DRIZZLEnight
                    if (description == "heavy intensity drizzle") { icon = "\uE922" } // DRIZZLEday
                    if (description == "heavy intensity drizzle" && nightTag) { icon = "\uE923" } // DRIZZLEnight
                    if (description == "light intensity drizzle rain") { icon = "\uE922" } // DRIZZLEday
                    if (description == "light intensity drizzle rain" && nightTag) { icon = "\uEA70" } // DRIZZLEnight
                    if (description == "drizzle rain") { icon = "\uEA6F" } // drizzle_rainday
                    if (description == "drizzle rain" && nightTag) { icon = "\uEA01" } // drizzle_rainnight
                    if (description == "heavy intensity drizzle rain") { icon = "\uEA00" } // RAINYday
                    if (description == "heavy intensity drizzle rain" && nightTag) { icon = "\uEA01" } // RAINYnight
                    if (description == "shower rain and drizzle") { icon = "\uE928" } // SHOWERSday
                    if (description == "shower rain and drizzle" && nightTag) { icon = "\uE929" } // SHOWERSnight
                    if (description == "heavy shower rain and drizzle") { icon = "\uE931" } // HEAVY_SHOWERSday
                    if (description == "heavy shower rain and drizzle" && nightTag) { icon = "\uE932" } // HEAVY_SHOWERSnight
                    if (description == "shower drizzle") { icon = "\uE928" } // SHOWERSday
                    if (description == "shower drizzle" && nightTag) { icon = "\uE929" } // SHOWERSnight
                    if (description == "light rain") { icon = "\uEA00" } // RAINYday
                    if (description == "light rain" && nightTag) { icon = "\uEA01" } // RAINYnight
                    if (description == "moderate rain") { icon = "\uE92E" } // RAINday
                    if (description == "moderate rain" && nightTag) { icon = "\uE932" } // RAINnight
                    if (description == "heavy intensity rain") { icon = "\uE92E" } // RAINday
                    if (description == "heavy intensity rain" && nightTag) { icon = "\uE932" } // RAINnight
                    if (description == "very heavy rain") { icon = "\uEA03" } // RAINY2day
                    if (description == "very heavy rain" && nightTag) { icon = "\uEA04" } // RAINY2night
                    if (description == "extreme rain") { icon = "\uEA03" } // RAINY2day
                    if (description == "extreme rain" && nightTag) { icon = "\uEA04" } // RAINY2night
                    if (description == "freezing rain") { icon = "\uE925" } // FREEZING_RAINday
                    if (description == "freezing rain" && nightTag) { icon = "\uE926" } // FREEZING_RAINnight
                    if (description == "light intensity shower rain") { icon = "\uE928" } // SHOWERSday
                    if (description == "light intensity shower rain" && nightTag) { icon = "\uE929" } // SHOWERSnight
                    if (description == "heavy intensity shower rain") { icon = "\uE931" } // HEAVY_SHOWERSday
                    if (description == "heavy intensity shower rain" && nightTag) { icon = "\uE932" } // HEAVY_SHOWERSnight
                    if (description == "ragged shower rain") { icon = "\uE928"  } // SHOWERSday
                    if (description == "ragged shower rain" && nightTag) { icon = "\uE929" } // SHOWERSnight
                    if (description == "light snow") { icon = "\uE937" } // LIGHT_SNOW_SHOWERSday
                    if (description == "light snow" && nightTag) { icon = "\uE938" } // LIGHT_SNOW_SHOWERSnight
                    if (description == "heavy snow") { icon = "\uE97C" } // HEAVY_SNOWday
                    if (description == "heavy snow" && nightTag) { icon = "\uE97D" } // HEAVY_SNOWnight
                    if (description == "sleet") { icon = "\uE946" } // SLEETday
                    if (description == "sleet" && nightTag) { icon = "\uE947" } // SLEETnight
                    if (description == "light shower sleet") { icon = "\uE919" } // MIXED_RAIN_SLEETday
                    if (description == "light shower sleet" && nightTag) { icon = "\uE91A" } // MIXED_RAIN_SLEETnight
                    if (description == "shower sleet") { icon = "\uE919" } // MIXED_RAIN_SLEETday
                    if (description == "shower sleet" && nightTag) { icon = "\uE91A" } // MIXED_RAIN_SLEETnight
                    if (description == "light rain and snow") { icon = "\uE916" } // MIXED_RAIN_SNOWday
                    if (description == "light rain and snow" && nightTag) { icon = "\uE917" } // MIXED_RAIN_SNOWnight
                    if (description == "rain and snow") { icon = "\uE916" } // MIXED_RAIN_SNOWday
                    if (description == "rain and snow" && nightTag) { icon = "\uE917" } // MIXED_RAIN_SNOWnight
                    if (description == "light shower snow") { icon = "\uE97F" } // SCATTERED_SNOW_SHOWERSday
                    if (description == "light shower snow" && nightTag) { icon = "\uE980" } // SCATTERED_SNOW_SHOWERSnight
                    if (description == "shower snow") { icon = "\uE988" } // SNOW_SHOWERSday
                    if (description == "shower snow" && nightTag) { icon = "\uE989" } // SNOW_SHOWERSnight
                    if (description == "heavy shower snow") { icon = "\uE97C" } // HEAVY_SNOWday
                    if (description == "heavy shower snow" && nightTag) { icon = "\uE97D" } // HEAVY_SNOWnight
                    if (description == "smoke") { icon = "\uEA45" } // smokeday
                    if (description == "smoke" && nightTag) { icon = "\uEA46" } // smokenight
                    if (description == "haze") { icon = "\uE952" } // HAZEday
                    if (description == "haze" && nightTag) { icon = "\uE953" } // HAZEnight
                    if (description == "sand/ dust whirls") { icon = "\uEA4B" } // sand/_dust_whirlsday
                    if (description == "sand/ dust whirls" && nightTag) { icon = "\uEA4C" } // sand/_dust_whirlsnight
                    if (description == "fog") { icon = "\uE94F" } // FOGday
                    if (description == "fog" && nightTag) { icon = "\uE950" } // FOGnight
                    if (description == "sand") { icon = "\uEA51" } // sandday
                    if (description == "sand" && nightTag) { icon = "\uEA52" } // sandnight
                    if (description == "dust") { icon = "\uEA51" } // DUSTday
                    if (description == "dust" && nightTag) { icon = "\uE94A" } // DUSTnight
                    if (description == "volcanic ash") { icon = "\uEA57" } // volcanic_ashday
                    if (description == "volcanic ash" && nightTag) { icon = "\uEA58" } // volcanic_ashnight
                    if (description == "squalls") { icon = "\uEA5A" } // squallsday
                    if (description == "squalls" && nightTag) { icon = "\uEA5B" } // squallsnight
                    if (description == "tornado") { icon = "\uE904" } // TORNADOday
                    if (description == "tornado" && nightTag) { icon = "\uE905" } // TORNADOnight


                    // set layout id's
                    val iconText: TextView = findViewById(R.id.displayWeatherIconText)
                    val descriptionText: TextView = findViewById(R.id.displayDescriptionText)
                    val temperatureText: TextView = findViewById(R.id.displayTemperatureText)
                    val editLocationText: EditText = findViewById(R.id.inputEditTextForCity)
                    val enterLocation: Editable = SpannableStringBuilder(location) // Pass a string here
                    val updatedAtText: TextView = findViewById(R.id.displayUpdatedAtText)

                    // show values
                    iconText.text = icon
                    descriptionText.text = description
                    temperatureText.text = temperature
                    editLocationText.text = enterLocation
                    updatedAtText.text = updatedAtFormatted

                } catch (e: JSONException) {
                    val text = "Oops, unable to get data from Openweathermap!"
                    val centeredText: Spannable = SpannableString(text)
                    centeredText.setSpan(
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        0, text.length - 1,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )

                    Toast.makeText(this@MainActivity, centeredText, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}