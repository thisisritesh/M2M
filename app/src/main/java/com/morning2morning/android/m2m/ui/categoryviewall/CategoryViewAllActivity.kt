package com.morning2morning.android.m2m.ui.categoryviewall

import CategoryAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.models.CategoriesResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.CategoryRepository
import com.morning2morning.android.m2m.databinding.ActivityCategoryViewAllBinding
import com.morning2morning.android.m2m.ui.categorydetails.CategoryDetailsActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class CategoryViewAllActivity : AppCompatActivity(), OnConnectionChanged, CategoryAdapter.AdapterCallbacks {

    private lateinit var binding: ActivityCategoryViewAllBinding
    private lateinit var categoryViewModel: CategoryViewAllViewModel
    private lateinit var apiClient: ApiClient
    private lateinit var categoryAdapter: CategoryAdapter

    private var isConnectedToInternet = false

    private var categoryList: MutableList<CategoriesResponse.Result> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_category_view_all)
        binding = ActivityCategoryViewAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ConnectivityManager.init(this,this,this)

        apiClient = NetworkHelper.getInstance(this)?.create(ApiClient::class.java)!!

        val viewModelFactory = CategoryViewAllViewModelFactory(CategoryRepository(apiClient))
        categoryViewModel = ViewModelProvider(this,viewModelFactory)[CategoryViewAllViewModel::class.java]

        categoryAdapter = CategoryAdapter(categoryList, this,this,apiClient,binding.categoriesRecyclerViewFragmentCategories)
        binding.categoriesRecyclerViewFragmentCategories.adapter = categoryAdapter


        categoryViewModel.getCategoryList()

        categoryViewModel.categories.observe(this){
            when(it){
                is NetworkState.Loading -> {
                    showLoadingUi()
                }
                is NetworkState.Failed -> {
                    if (it.message == NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE){
                        showNoInternetConnectionUi()
                    } else {
                        showErrorUi()
                    }
                }
                is NetworkState.Success -> {
                    setUpCategoryRecyclerView(it.data)
                    showSuccessUi()
                }
            }
        }

        binding.backArrow.setOnClickListener {
            finish()
        }



        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(this@CategoryViewAllActivity)
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@CategoryViewAllActivity){
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


    }

    private fun setUpCategoryRecyclerView(list: List<CategoriesResponse.Result>){
        categoryAdapter.setDataList(list)
    }

    private fun showNoInternetConnectionUi(){
        binding.noInternetConnectionView.visibility = View.VISIBLE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.GONE
//        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showErrorUi(){
        binding.noInternetConnectionView.visibility = View.GONE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.GONE
//        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
    }

    private fun showLoadingUi() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.GONE
        binding.categoriesHeading.visibility = View.GONE
//        binding.logoImage.visibility = View.GONE
//        binding.shimmerSearchViewContainer.visibility = View.VISIBLE
//        binding.searchHintText.visibility = View.GONE
    }

    private fun showSuccessUi(){
        binding.shimmerViewContainer.visibility = View.GONE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.VISIBLE
        binding.categoriesHeading.visibility = View.VISIBLE
//        binding.logoImage.visibility = View.VISIBLE
//        binding.shimmerSearchViewContainer.visibility = View.GONE
//        binding.searchHintText.visibility = View.VISIBLE
        binding.noInternetConnectionView.visibility = View.GONE
    }


    override fun onItemClicked(cat_id: String,cat_title: String) {
        val intent = Intent(this, CategoryDetailsActivity::class.java)
        intent.putExtra(Constants.CATEGORY_ID,cat_id)
        intent.putExtra(Constants.CATEGORY_TITLE,cat_title)
        startActivity(intent)
        overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onRequestLoadMore() {
//        binding.progressBar.visibility = View.VISIBLE
//        catOffset++
//        getCategories()
//        binding.progressBar.visibility = View.GONE
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_in, R.anim.right_out)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected

    }
}