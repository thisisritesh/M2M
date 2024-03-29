package com.morning2morning.android.m2m

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.databinding.ActivityMainBinding
import com.morning2morning.android.m2m.ui.cart.CartFragment
import com.morning2morning.android.m2m.ui.categories.CategoryFragment
import com.morning2morning.android.m2m.ui.home.HomeFragment
import com.morning2morning.android.m2m.ui.offers.OffersFragment
import com.morning2morning.android.m2m.ui.search.SearchActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navView: BottomNavigationView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        navView = binding.navView

        switchScreen(HomeFragment())
        navView.selectedItemId = R.id.navigation_home

        navView.setOnItemSelectedListener {
            when (it.itemId){
                R.id.navigation_home -> {
                    switchScreen(HomeFragment())
                    true
                }
                R.id.navigation_search -> {
                    startActivity(Intent(this,SearchActivity::class.java))
                    overridePendingTransition(R.anim.right_in, R.anim.left_out)
                    false
                }
                R.id.navigation_offers -> {
                    switchScreen(OffersFragment())
                    true
                }
                R.id.navigation_category -> {
                    switchScreen(CategoryFragment())
                    true
                }
                R.id.navigation_cart -> {
                    switchScreen(CartFragment())
                    true
                }
                else -> {false}
            }
        }

        navView.setOnItemReselectedListener {

        }

        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(this@MainActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@MainActivity){
                    navView.getOrCreateBadge(R.id.navigation_cart).number = it.size
                }
            }

        }


    }


    private fun switchScreen(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.nav_host_fragment_activity_main,fragment)
                commit()
            }
    }


}