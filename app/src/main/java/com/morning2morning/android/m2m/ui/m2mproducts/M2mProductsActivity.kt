package com.morning2morning.android.m2m.ui.m2mproducts

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.M2mProductsAdapter
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.models.M2mProductsResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.M2mProductsRepository
import com.morning2morning.android.m2m.databinding.ActivityM2mProductsBinding
import com.morning2morning.android.m2m.ui.cart.CartActivity
import com.morning2morning.android.m2m.ui.productdetails.ProductDetailsActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
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
class M2mProductsActivity : AppCompatActivity(), M2mProductsAdapter.AdapterCallbacks,
    OnConnectionChanged {

    private lateinit var binding: ActivityM2mProductsBinding
    private lateinit var viewModel: M2mProductsViewModel
    private lateinit var apiClient: ApiClient

    private lateinit var adapter: M2mProductsAdapter

    private var isConnectedToInternet = true

    private var productsList: MutableList<M2mProductsResponse.Product.ProductX> = mutableListOf()

    private var offset = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_m2m_products)
        binding = ActivityM2mProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ConnectivityManager.init(this,this,this)

        apiClient = NetworkHelper.getInstance(this)?.create(ApiClient::class.java)!!

        val viewModelFactory = M2mProductsViewModelFactory(M2mProductsRepository(apiClient))
        viewModel = ViewModelProvider(this,viewModelFactory)[M2mProductsViewModel::class.java]

        adapter = M2mProductsAdapter(productsList,this,this,binding.recyclerViewCategoryDetails)
        binding.recyclerViewCategoryDetails.adapter = adapter

        setUpData()

        binding.backArrow.setOnClickListener {
            finish()
        }

        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(this@M2mProductsActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@M2mProductsActivity){
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

            binding.proceedText.setOnClickListener {
                startActivity(Intent(this@M2mProductsActivity, CartActivity::class.java))
                overridePendingTransition(R.anim.right_in, R.anim.left_out)
            }

        }


    }


    private fun setUpData() {
        getM2mProductsData()

        viewModel.m2mProducts.observe(this){
            when(it){
                is NetworkState.Loading -> {
                    if (offset == 0){
                        showLoadingUi()
                    } else {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
                is NetworkState.Failed -> {
                    if (it.message == NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE){
                        showNoInternetConnectionUi()
                    } else {
                        showErrorUi()
                    }
                }
                is NetworkState.Success -> {
                    showSuccessUi()
                    setUpRecyclerView(it.data)
                }
            }
        }




    }



    private fun setUpRecyclerView(productList: List<M2mProductsResponse.Product.ProductX>) {
        productsList.addAll(productList)
        adapter.notifyDataSetChanged()
    }

    private fun showLoadingUi() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.recyclerViewCategoryDetails.visibility = View.GONE
    }

    private fun showSuccessUi() {
        binding.shimmerViewContainer.visibility = View.GONE
        binding.recyclerViewCategoryDetails.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun getM2mProductsData() {
        if (isConnectedToInternet){
            viewModel.getM2mProductsList(offset)
        } else {
            Utils.showNotConnectedToInternetDialog(this)
        }

    }

    private fun showNoInternetConnectionUi(){
        binding.noInternetConnectionView.visibility = View.VISIBLE
        binding.recyclerViewCategoryDetails.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showErrorUi(){
        binding.noInternetConnectionView.visibility = View.GONE
        binding.recyclerViewCategoryDetails.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
    }

    override fun onItemClicked(productId: String) {
//        val action = M2mProductsFragmentDirections.actionM2mProductsFragmentToProductDetailsFragment(productId)
//        navController.navigate(action)
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtra(Constants.PRODUCT_ID,productId)
        startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onRequestLoadMore() {
        binding.progressBar.visibility = View.VISIBLE
        offset++
        getM2mProductsData()
        binding.progressBar.visibility = View.GONE
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