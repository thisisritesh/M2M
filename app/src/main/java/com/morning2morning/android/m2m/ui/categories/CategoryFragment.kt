package com.morning2morning.android.m2m.ui.categories

import CategoryAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.models.CategoriesResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.CategoryRepository
import com.morning2morning.android.m2m.databinding.FragmentCategoryBinding
import com.morning2morning.android.m2m.ui.categorydetails.CategoryDetailsActivity
import com.morning2morning.android.m2m.ui.login.LoginBottomSheet
import com.morning2morning.android.m2m.ui.search.SearchActivity
import com.morning2morning.android.m2m.ui.sidemenu.SideMenuActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.DelicateCoroutinesApi

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class CategoryFragment : Fragment(), CategoryAdapter.AdapterCallbacks, OnConnectionChanged {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var apiClient: ApiClient
//    private lateinit var navController: NavController
    private var catOffset = 0
    private lateinit var categoryAdapter: CategoryAdapter

    private var isConnectedToInternet = false

    private var categoryList: MutableList<CategoriesResponse.Result> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        navController = Navigation.findNavController(view)

        ConnectivityManager.init(requireContext(),viewLifecycleOwner,this)

        apiClient = NetworkHelper.getInstance(requireContext())?.create(ApiClient::class.java)!!

        val viewModelFactory = CategoryViewModelFactory(CategoryRepository(apiClient))
        categoryViewModel = ViewModelProvider(this,viewModelFactory)[CategoryViewModel::class.java]

        categoryAdapter = CategoryAdapter(categoryList, this,requireContext(),apiClient,binding.categoriesRecyclerViewFragmentCategories)
        binding.categoriesRecyclerViewFragmentCategories.adapter = categoryAdapter


        categoryViewModel.getCategoryList()

        categoryViewModel.categories.observe(viewLifecycleOwner){
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

        binding.searchCard.setOnClickListener {
            startActivity(Intent(requireContext(),SearchActivity::class.java))
            activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
//            navController.navigate(R.id.action_navigation_category_to_navigation_search)
        }

        binding.searchButton.setOnClickListener {
            startActivity(Intent(requireContext(),SearchActivity::class.java))
            activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }

        binding.userProfileButton.setOnClickListener {
            if (isConnectedToInternet){
                if (Pref(requireContext()).getPrefBoolean(Constants.LOGIN_STATUS)){
                    startActivity(Intent(requireContext(), SideMenuActivity::class.java))
                    activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
                } else {
                    LoginBottomSheet().show(activity?.supportFragmentManager!!,"diej")
                }
            } else {
                Utils.showNotConnectedToInternetDialog(requireContext())
            }
        }

    }

    private fun setUpCategoryRecyclerView(list: List<CategoriesResponse.Result>){
        categoryAdapter.setDataList(list)
    }

    private fun showNoInternetConnectionUi(){
        binding.noInternetConnectionView.visibility = View.VISIBLE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.GONE
        binding.searchCard.visibility = View.GONE
        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showErrorUi(){
        binding.noInternetConnectionView.visibility = View.GONE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.GONE
        binding.searchCard.visibility = View.GONE
        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
    }

    private fun showLoadingUi() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerSearchViewContainer.visibility = View.VISIBLE
        binding.searchCard.visibility = View.VISIBLE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.GONE
        binding.categoriesHeading.visibility = View.GONE
        binding.logoImage.visibility = View.GONE
        binding.searchHintText.visibility = View.GONE
        binding.noInternetConnectionView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showSuccessUi(){
        binding.shimmerViewContainer.visibility = View.GONE
        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.searchCard.visibility = View.VISIBLE
        binding.categoriesRecyclerViewFragmentCategories.visibility = View.VISIBLE
        binding.categoriesHeading.visibility = View.VISIBLE
        binding.logoImage.visibility = View.VISIBLE
        binding.searchHintText.visibility = View.VISIBLE
        binding.noInternetConnectionView.visibility = View.GONE
        binding.errorView.visibility = View.GONE

        Glide.with(requireContext())
            .load(R.mipmap.high_resolution_horizon_m2m_logo)
            .into(binding.logoImage)
    }


    override fun onItemClicked(cat_id: String,cat_title: String) {
        val intent = Intent(requireContext(), CategoryDetailsActivity::class.java)
        intent.putExtra(Constants.CATEGORY_ID,cat_id)
        intent.putExtra(Constants.CATEGORY_TITLE,cat_title)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onRequestLoadMore() {
//        binding.progressBar.visibility = View.VISIBLE
//        catOffset++
//        getCategories()
//        binding.progressBar.visibility = View.GONE
    }


    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected

    }

}