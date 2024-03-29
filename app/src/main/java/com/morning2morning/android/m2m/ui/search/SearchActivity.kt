package com.morning2morning.android.m2m.ui.search

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.RecentSearchesAdapter
import com.morning2morning.android.m2m.data.adapters.TrendingSearchAdapter
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.entity.RecentSearch
import com.morning2morning.android.m2m.databinding.ActivitySearchBinding
import com.morning2morning.android.m2m.ui.login.LoginBottomSheet
import com.morning2morning.android.m2m.ui.searchresult.SearchResultActivity
import com.morning2morning.android.m2m.ui.sidemenu.SideMenuActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class SearchActivity : AppCompatActivity(), OnConnectionChanged {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchEditText: EditText
    private lateinit var mAdapter: RecentSearchesAdapter
    private var recentSearchList: List<RecentSearch> = listOf()

    private var isConnectedToInternet = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_search)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        searchEditText = binding.searchViewEditText
//        searchEditText.requestFocus()

        ConnectivityManager.init(this,this,this)

        binding.searchIcon.setOnClickListener {
            val searchTag = searchEditText.text.toString().lowercase().trim() + ""
            if (searchTag.isNotEmpty()){
                goToSearchResultActivity(searchTag)
            } else {
                CustomToast().showToastMessage(this,"Please enter a keyword!")
            }

        }

        binding.backArrow.setOnClickListener {
            finish()
        }

        binding.userProfileButton.setOnClickListener {
            if (isConnectedToInternet){
                if (Pref(this).getPrefBoolean(Constants.LOGIN_STATUS)){
                    startActivity(Intent(this, SideMenuActivity::class.java))
                    overridePendingTransition(R.anim.right_in, R.anim.left_out)
                } else {
                    LoginBottomSheet().show(supportFragmentManager,"diej")
                }
            } else {
                Utils.showNotConnectedToInternetDialog(this)
            }
        }

        searchEditText.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus){
                val searchTag = searchEditText.text.toString().lowercase().trim() + ""
//                addRecentSearch(searchTag)
                goToSearchResultActivity(searchTag)
            }
        }

        val list = listOf(
            "OnePlus U1S 126 cm (50 inch) Ultra HD (4K) LED Smart Android TV (50UC1A00)",
            "Amazon Brand - Solimo Revolving Plastic Spice Rack set (16 pieces,Silver)",
            "LG 7.5 kg with Roller Jet Pulsator + Soak Semi Automatic Top Load Purple  (P7525SPAZ)",
            "Bharat Lifestyle Tulip Fabric 3 Seater Sofa  (Finish Color - Black Grey, DIY(Do-It-Yourself))",
            "Good Knight Gold Flash Liquid Vapouriser with Machine 45 ml"
        )

        binding.popularSuggestionsRecyclerView.adapter = TrendingSearchAdapter(list)



        // Recent search -- starts
//        mAdapter = RecentSearchesAdapter(recentSearchList)
//        binding.recentSearchesRecyclerView.adapter = mAdapter
//
//
//        lifecycleScope.launch(Dispatchers.IO){
//            val list = RecentSearchDB.getInstance(requireContext())
//                .dao()
//                .getAllRecentSearches()
//
//            mAdapter.setData(list)
//        }

    }

    private fun goToSearchResultActivity(searchTag: String) {
        val intent = Intent(this, SearchResultActivity::class.java)
        intent.putExtra(Constants.SEARCH_TAG,searchTag)
        startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        finish()
    }

}