package com.morning2morning.android.m2m.ui.categorydetails

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.CategoryDetailsAdapter
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.models.ProductsListByCatIdResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.CategoryDetailsRepository
import com.morning2morning.android.m2m.databinding.ActivityCategoryDetailsBinding
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
class CategoryDetailsActivity : AppCompatActivity(), CategoryDetailsAdapter.AdapterCallbacks,
    OnConnectionChanged {

    private lateinit var binding: ActivityCategoryDetailsBinding
    private lateinit var viewModel: CategoryDetailsViewModel
    private lateinit var apiClient: ApiClient

    private var offset = 0

    private lateinit var adapter: CategoryDetailsAdapter

    private var productsList: MutableList<ProductsListByCatIdResponse.Product.ProductX> = mutableListOf()

    private var isConnectedToInternet = false

    private lateinit var catId: String
    private lateinit var catTitle: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_category_details)
        binding = ActivityCategoryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ConnectivityManager.init(this,this,this)

        if (intent != null){
            catId = intent.getStringExtra(Constants.CATEGORY_ID)!!
            catTitle = intent.getStringExtra(Constants.CATEGORY_TITLE)!!
        }



        binding.headingTextView.text = catTitle

//        navController = Navigation.findNavController(view)

        apiClient = NetworkHelper.getInstance(this)?.create(ApiClient::class.java)!!

        val viewModelFactory = CategoryDetailsViewModelFactory(CategoryDetailsRepository(apiClient,catId))
        viewModel = ViewModelProvider(this,viewModelFactory)[CategoryDetailsViewModel::class.java]

        binding.backArrow.setOnClickListener {
            finish()
        }

        //overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_left)
        // finish -- overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_right)

        adapter = CategoryDetailsAdapter(productsList, this,binding.recyclerViewCategoryDetails,this)
        binding.recyclerViewCategoryDetails.adapter = adapter

        viewModel.getCategoriesData(offset)

        viewModel.categoryDetailsData.observe(this){
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
                    setUpRecyclerView(it.data)
                    showSuccessUi()
                }
            }
        }


        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(this@CategoryDetailsActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@CategoryDetailsActivity){
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

    private fun setUpRecyclerView(list: List<ProductsListByCatIdResponse.Product.ProductX>){
        productsList.addAll(list)
        adapter.notifyDataSetChanged()
    }

    private fun showLoadingUi() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.recyclerViewCategoryDetails.visibility = View.GONE
        binding.headingTextView.visibility = View.GONE
    }

    private fun showSuccessUi(){
        binding.shimmerViewContainer.visibility = View.GONE
        binding.recyclerViewCategoryDetails.visibility = View.VISIBLE
        binding.headingTextView.visibility = View.VISIBLE
        binding.headingTextView.visibility = View.VISIBLE
    }



    override fun onItemClicked(productId: String) {
//        val action = CategoryDetailsFragmentDirections.actionCategoryDetailsFragmentToProductDetailsFragment(productId)
//        navController.navigate(action)
        val intent = Intent(this, ProductDetailsActivity::class.java)
        intent.putExtra(Constants.PRODUCT_ID,productId)
        startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onRequestLoadMore() {
        binding.progressBar.visibility = View.VISIBLE
        offset++
        viewModel.getCategoriesData(offset)
        binding.progressBar.visibility = View.GONE
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
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

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        finish()
    }


}