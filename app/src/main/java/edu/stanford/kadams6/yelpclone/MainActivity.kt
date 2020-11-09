package edu.stanford.kadams6.yelpclone

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = "2FQWE5RVIjQkDUVuQN6euPZsInVDoDXDdxr4atU-ZlVghAo1lxW_atMfZ0Or4tbMAk9cSG96wNZNYANrEjhXrVuTHMysI7DluRoGkjy9Cc4YeW4qTGD27Fqo_bmoX3Yx"
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!isOnline()) {
            // Tell user to connect to the internet
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("Connect to the internet and restart app.")
            builder.setCancelable(true)

            val alert: AlertDialog = builder.create()
            alert.show()
        }
        else {

            Log.i(TAG, "checked if online")

            val restaurants = mutableListOf<YelpRestaurant>()
            val adapter = RestaurantsAdapter(this, restaurants)
            rvRestaurants.adapter = adapter
            rvRestaurants.layoutManager = LinearLayoutManager(this)

            // Create Retrofit instance
            val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()

            // Create instance of endpoint
            val yelpService = retrofit.create(YelpService::class.java)

            // Asynchronous network request
            yelpService.searchRestaurants("Bearer $API_KEY", "Avocado Toast", "New York")
                .enqueue(object : Callback<YelpSearchResult> {
                    override fun onResponse(
                        call: Call<YelpSearchResult>,
                        response: Response<YelpSearchResult>
                    ) {
                        Log.i(TAG, "onResponse $response")
                        val body = response.body()
                        if (body == null) {
                            Log.w(
                                TAG,
                                "Did not receive valid response body from Yelp API... exiting"
                            )
                            return
                        }
                        Log.i(TAG, "restaurants ${body.restaurants}")
                        restaurants.addAll(body.restaurants)
                        adapter.notifyDataSetChanged()
                    }

                    override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                        Log.i(TAG, "onFailure $t")
                    }

                })
        }
    }

    // This code is adapted from a Java example at codepath.com:
    // https://guides.codepath.com/android/sending-and-managing-network-requests#checking-for-network-connectivity
    fun isOnline(): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val exitValue = ipProcess.waitFor()
            return exitValue == 0
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return false
    }
}