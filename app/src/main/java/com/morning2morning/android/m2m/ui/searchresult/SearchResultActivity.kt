package com.morning2morning.android.m2m.ui.searchresult

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.SearchResultAdapter
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.models.SearchResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.SearchResultRepository
import com.morning2morning.android.m2m.databinding.ActivitySearchResultBinding
import com.morning2morning.android.m2m.ui.cart.CartActivity
import com.morning2morning.android.m2m.ui.productdetails.ProductDetailsActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class SearchResultActivity : AppCompatActivity(), SearchResultAdapter.AdapterCallback,
    OnConnectionChanged {

    private lateinit var binding: ActivitySearchResultBinding
    private lateinit var apiClient: ApiClient
    private lateinit var viewModel: SearchResultViewModel

    private lateinit var inputQuery: String

    private lateinit var searchAdapter: SearchResultAdapter

    private var searchResultList: MutableList<SearchResponse.Product.ProductX> = mutableListOf()

    private var offset = 0

    private var isConnectedToInternet = true

    private lateinit var recyclerView: RecyclerView
    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var noInternetConnectedView: LinearLayout
    private lateinit var noSearchedResultFoundView: LinearLayout
    private lateinit var errorView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_search_result)
        binding = ActivitySearchResultBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ConnectivityManager.init(this,this,this)

        recyclerView = binding.recyclerViewSearchResults
        shimmerFrameLayout = binding.shimmerViewContainer
        noInternetConnectedView = binding.noInternetConnectionView
        noSearchedResultFoundView = binding.noSearchResultsFoundView
        errorView = binding.errorView

        searchAdapter = SearchResultAdapter(searchResultList,this, recyclerView, this)
        recyclerView.adapter = searchAdapter

        inputQuery = intent.getStringExtra(Constants.SEARCH_TAG)!!

        apiClient = NetworkHelper.getInstance(this)?.create(ApiClient::class.java)!!

        val viewModelFactory = SearchResultViewModelFactory(SearchResultRepository(apiClient,inputQuery))
        viewModel = ViewModelProvider(this,viewModelFactory)[SearchResultViewModel::class.java]

        getSearchResult()

        binding.backArrow.setOnClickListener {
            finish()
        }

        viewModel.searchResultData.observe(this){
            when(it){
                is NetworkState.Loading -> {
                    showLoadingUi()
                }
                is NetworkState.Failed -> {
                    when (it.message) {
                        NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE -> {
                            showNoInternetConnectionUi()
                        } else -> {
                        showErrorUi()
                    }
                    }
                }
                is NetworkState.Success -> {
                    showSuccessUi()
                    if (it.data.product.isNotEmpty()){
                        setUpRecyclerView(it.data.product)
                        binding.resultDescriptionTextView.text = "Showing results of $inputQuery (${it.data.count})"
                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(this@SearchResultActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@SearchResultActivity){
                    if (it.isEmpty()) {
                        binding.cartDetailsCard.visibility = View.GONE
                    } else {
                        binding.cartDetailsCard.visibility = View.VISIBLE
                        binding.itemCountTextView.text = "${it.size} items"
                        binding.discountedPriceSumTextView.text = "\u20B9" +" " + Utils.calculateDiscountedTotalPrice(it)
                        binding.originalPriceSumTextView.text = "\u20B9" +" " + Utils.calculateTotalPrice(it)
                    }
                }
            }

        }


        binding.proceedText.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }


    }

    private fun showSuccessUi() {
        recyclerView.visibility = View.VISIBLE
        shimmerFrameLayout.visibility = View.GONE
        noInternetConnectedView.visibility = View.GONE
        noSearchedResultFoundView.visibility = View.GONE
        errorView.visibility = View.GONE
    }

    private fun showErrorUi() {
        recyclerView.visibility = View.GONE
        shimmerFrameLayout.visibility = View.GONE
        noInternetConnectedView.visibility = View.GONE
        noSearchedResultFoundView.visibility = View.GONE
        binding.noSearchResultsFoundView.visibility = View.VISIBLE
        errorView.visibility = View.GONE
        CustomToast().showToastMessage(this,"No search result found with $inputQuery")
    }

    private fun showNoInternetConnectionUi() {
        recyclerView.visibility = View.GONE
        shimmerFrameLayout.visibility = View.GONE
        noInternetConnectedView.visibility = View.VISIBLE
        noSearchedResultFoundView.visibility = View.GONE
        errorView.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private fun setUpRecyclerView(list: List<SearchResponse.Product.ProductX>) {
        searchAdapter.setData(list)
    }

    private fun showLoadingUi(){
        recyclerView.visibility = View.GONE
        shimmerFrameLayout.visibility = View.VISIBLE
        noInternetConnectedView.visibility = View.GONE
        noSearchedResultFoundView.visibility = View.GONE
        errorView.visibility = View.GONE
    }

    override fun onRequestLoadMore() {
        binding.progressBar.visibility = View.VISIBLE
        offset++
        getSearchResult()
        binding.progressBar.visibility = View.GONE
    }

    private fun getSearchResult() {
        viewModel.getSearchResultList(offset)
    }

    override fun onSearchResultItemClicked(productId: String) {
//        val action = SearchResultFragmentDirections.actionSearchResultFragmentToProductDetailsFragment(productId)
//        navController.navigate(action)
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtra(Constants.PRODUCT_ID,productId)
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