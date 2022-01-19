package com.krishana.androidhackathontemplates


import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.krishana.androidhackathontemplates.fragments.HomeFragment
import com.krishana.androidhackathontemplates.fragments.*
import org.json.JSONArray
import org.json.JSONException

class MainActivity : AppCompatActivity(){

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var list : ArrayList<recipeModel>
    private lateinit var adapter : recipeAdapter
    private lateinit var viewPagerImgSlider: ViewPager2
    private lateinit var sliderHandle: Handler
    private lateinit var sliderRun :Runnable


    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val bottomNavigationView : BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.selectedItemId = R.id.nav_home

        bottomNavigationView.setOnItemSelectedListener {
            val destinationActivity  = when(it.itemId){
                R.id.nav_items -> RecyclerViewActivity::class.java

                else -> MainActivity::class.java
            }
            if(it.itemId != R.id.nav_home){
                this.startActivity(Intent(this,destinationActivity))
                this.overridePendingTransition(0,0)
            }
            true
        }

        val addButton = findViewById<FloatingActionButton>(R.id.add_items)
        addButton.setOnClickListener {
            this.startActivity(Intent(this,FireBaseActivity::class.java))
//            this.overridePendingTransition(0,0)
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener{
            val destinationActivity = when(it.itemId) {
                R.id.nav_signout -> SignOutActivity::class.java
                R.id.nav_log_in -> LogInActivity::class.java
                R.id.nav_settings -> SettingsActivity::class.java
                else -> MainActivity::class.java
            }
            startActivity(Intent(this,destinationActivity))
            true
        }


        viewPagerImgSlider = findViewById(R.id.viewPagerImgSlider)
        loadrecyclerviewData()


    }


    override fun onPause() {
        super.onPause()
        sliderHandle.removeCallbacks(sliderRun)

    }


    override fun onResume() {
        super.onResume()
        sliderHandle.postDelayed(sliderRun,2000)
    }

    private fun loadrecyclerviewData() {
        list = ArrayList()
        adapter = recipeAdapter(viewPagerImgSlider,list, this)
        viewPagerImgSlider.adapter = adapter
        viewPagerImgSlider.clipToPadding = false
        viewPagerImgSlider.clipChildren = false
        viewPagerImgSlider.offscreenPageLimit = 1
        viewPagerImgSlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val comPosPageTarn = CompositePageTransformer()
        comPosPageTarn.addTransformer(MarginPageTransformer(40))
        comPosPageTarn.addTransformer { page, position ->
            val r: Float = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        viewPagerImgSlider.setPageTransformer(comPosPageTarn)
        sliderHandle = Handler()
        sliderRun = Runnable {
            viewPagerImgSlider.currentItem = viewPagerImgSlider.currentItem + 1
        }

        viewPagerImgSlider.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback(){

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    sliderHandle.removeCallbacks(sliderRun)
                    sliderHandle.postDelayed(sliderRun,2000)
                }
            })


        val stringRequest = StringRequest(
            Request.Method.GET, "https://api.spoonacular.com/recipes/findByIngredients?apiKey=841251d5281a42c28fe0076a39691cc3&ingredients="+ "carrot",
            { response ->

                try {
                    //getting data  from json object
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val o = jsonArray.getJSONObject(i)
                        val item = recipeModel(
                            o.getString("title"),
                            o.getString("image")
                        )
                        list.add(item)
                    }




                    //adapter.notifyDataSetChanged();
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        ) {
            Toast.makeText(
                this,
                "oops!! something went wrong",
                Toast.LENGTH_SHORT
            ).show()
        }
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(stringRequest)

    }

}