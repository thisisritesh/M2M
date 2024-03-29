package com.morning2morning.android.m2m.ui.offers

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.HomeBannerAdapter
import com.morning2morning.android.m2m.data.adapters.NestedCategoryAdapter
import com.morning2morning.android.m2m.data.adapters.ViewPagerIndicatorAdapter
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.models.HomeBannerResponse
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.OffersRepository
import com.morning2morning.android.m2m.databinding.FragmentOffersBinding
import com.morning2morning.android.m2m.ui.cart.CartActivity
import com.morning2morning.android.m2m.ui.categorydetails.CategoryDetailsActivity
import com.morning2morning.android.m2m.ui.login.LoginBottomSheet
import com.morning2morning.android.m2m.ui.productdetails.ProductDetailsActivity
import com.morning2morning.android.m2m.ui.search.SearchActivity
import com.morning2morning.android.m2m.ui.sidemenu.SideMenuActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.customviews.HorizontalMarginItemDecoration
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
class OffersFragment : Fragment(), NestedCategoryAdapter.AdapterCallbacks, OnConnectionChanged {

    private lateinit var binding: FragmentOffersBinding

    private val homeSliderHandler = Handler()
    private lateinit var indicatorAdapter: ViewPagerIndicatorAdapter

    private lateinit var homeBannerViewPager: ViewPager2

    private lateinit var viewModel: OffersViewModel
    private lateinit var apiClient: ApiClient


    private var catOffset = 0

    private var categoryList: MutableList<HomeCategoryResponse.Categories.Category> = mutableListOf()

    private lateinit var nestedCategoryAdapter: NestedCategoryAdapter

    private var isConnectedToInternet = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOffersBinding.inflate(inflater, container, false)
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        navController = Navigation.findNavController(view)
        apiClient = NetworkHelper.getInstance(requireContext())?.create(ApiClient::class.java)!!

        val recyclerView = binding.nestedRecyclerView
        nestedCategoryAdapter = NestedCategoryAdapter(categoryList,requireContext(),this,recyclerView)
        recyclerView.adapter = nestedCategoryAdapter

        ConnectivityManager.init(requireContext(),viewLifecycleOwner,this)

        binding.searchCard.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
            activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
//            navController.navigate(R.id.action_navigation_offers_to_navigation_search)
        }

        binding.searchButton.setOnClickListener {
            startActivity(Intent(requireContext(),SearchActivity::class.java))
            activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
//            navController.navigate(R.id.action_navigation_offers_to_navigation_search)
        }


        binding.proceedText.setOnClickListener {
            startActivity(Intent(requireContext(), CartActivity::class.java))
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

        homeBannerViewPager = binding.homeBannerViewPager

        homeBannerViewPager.clipToPadding = true
        homeBannerViewPager.clipChildren = false
        homeBannerViewPager.offscreenPageLimit = 1


        // View Pager -- start
        val nextItemVisiblePx = resources.getDimension(R.dimen.next_item_visible_px)
        val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.current_item_horizontal_margin_px)

        val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
        Log.d("ceinceipnc", "setUpHomeBanner: $pageTranslationX")

        val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
            page.translationX = - pageTranslationX * position
//            page.scaleY = 1 - (0.25f * abs(position))
// page.alpha = 0.25f + (1 - abs(position))
        }
        homeBannerViewPager.setPageTransformer(pageTransformer)


        val itemDecoration = HorizontalMarginItemDecoration(
            requireContext(),
            R.dimen.next_item_visible_px
        )
        homeBannerViewPager.addItemDecoration(itemDecoration)

        // View pager -- end

        val viewModelFactory = OffersViewModelFactory(OffersRepository(apiClient))
        viewModel = ViewModelProvider(this,viewModelFactory)[OffersViewModel::class.java]

        setUpData()


        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = CartDB.getInstance(requireContext())
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(requireActivity()){
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

    private fun showNoInternetConnectionUi(){
        binding.noInternetConnectionView.visibility = View.VISIBLE
        binding.nestedRecyclerView.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun showErrorUi(){
        binding.noInternetConnectionView.visibility = View.GONE
        binding.nestedRecyclerView.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
    }

    private fun setUpData(){
        getHomeBannerData()

        viewModel.homeBannerData.observe(viewLifecycleOwner){
            when(it){
                is NetworkState.Loading -> {
//                    showLoadingUi()
                }
                is NetworkState.Failed -> {
//                    showErrorUi()
                }
                is NetworkState.Success -> {
                    setUpHomeBanner(it.data as MutableList<HomeBannerResponse.Result>)
                }
            }
        }


        getCategories()

        viewModel.categoryData.observe(viewLifecycleOwner){
            when(it){
                is NetworkState.Loading -> {
                    showLoadingUi()
                }
                is NetworkState.Failed -> {
                    if (it.message.equals(NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE)){
                        showNoInternetConnectionUi()
                    } else {
                        showErrorUi()
                    }
                }
                is NetworkState.Success -> {
                    setUpNestedRecyclerView(it.data)
                    showSuccessUi()
                }
            }
        }
    }

    private fun getHomeBannerData() {
        if (isConnectedToInternet){
            viewModel.getHomeBannerList()
        }
    }

//    private fun setUpNestedRecyclerView(list: List<HomeCategoryResponse.Categories.Category>, ){
//        val recyclerView = binding.nestedRecyclerView
//        recyclerView.adapter = NestedHomeAdapter(list,apiClient,viewLifecycleOwner,requireContext(),this,recyclerView)
//    }

    private fun setUpNestedRecyclerView(list: List<HomeCategoryResponse.Categories.Category> ){
        categoryList.addAll(list)
        nestedCategoryAdapter.notifyDataSetChanged()
    }

    private fun setUpHomeBanner(dataList: MutableList<HomeBannerResponse.Result>){
        val indicatorRecyclerView = binding.indicatorRecyclerView


        val adapter = HomeBannerAdapter(
            dataList,homeBannerViewPager, object : HomeBannerAdapter.AdapterCallback{
                override fun onItemClicked(productId: String) {
                    goToProductDetailActivity(productId)
                }
            }
        )

        homeBannerViewPager.adapter = adapter



        indicatorAdapter =  ViewPagerIndicatorAdapter(dataList.size)

        indicatorRecyclerView.adapter = indicatorAdapter

        homeBannerViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                homeSliderHandler.removeCallbacks(homeSliderRunnable)
                homeSliderHandler.postDelayed(homeSliderRunnable,3500)

                indicatorAdapter.selectItem(position)

            }

        })
    }


    private fun showLoadingUi(){
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.nestedRecyclerView.visibility = View.GONE
        binding.indicatorRecyclerView.visibility = View.GONE
        binding.homeBannerViewPager.visibility = View.GONE
        binding.logoImage.visibility = View.GONE
        binding.shimmerSearchViewContainer.visibility = View.VISIBLE
        binding.searchHintText.visibility = View.GONE
    }

    private val homeSliderRunnable = Runnable {
        homeBannerViewPager.currentItem = homeBannerViewPager.currentItem + 1
    }

    private fun showSuccessUi(){
        binding.shimmerViewContainer.visibility = View.GONE
        binding.nestedRecyclerView.visibility = View.VISIBLE
        binding.indicatorRecyclerView.visibility = View.VISIBLE
        binding.homeBannerViewPager.visibility = View.VISIBLE
        binding.logoImage.visibility = View.VISIBLE
        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.searchHintText.visibility = View.VISIBLE
        binding.noInternetConnectionView.visibility = View.GONE

        Glide.with(requireContext())
            .load(R.mipmap.high_resolution_horizon_m2m_logo)
            .into(binding.logoImage)
    }

    override fun onViewAllClicked(cat_id: String, cat_title: String) {
        if (isConnectedToInternet){
//            val action = OffersFragmentDirections.actionNavigationOffersToCategoryDetailsFragment(cat_id,cat_title)
//            navController.navigate(action)
            goToCategoryDetailsActivity(cat_id,cat_title)
        } else {
            Utils.showNotConnectedToInternetDialog(requireContext())
        }
    }

    private fun goToCategoryDetailsActivity(cat_id: String, cat_title: String){
        val intent = Intent(requireContext(), CategoryDetailsActivity::class.java)
        intent.putExtra(Constants.CATEGORY_ID,cat_id)
        intent.putExtra(Constants.CATEGORY_TITLE,cat_title)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    private fun getCategories(){
        if (isConnectedToInternet){
            viewModel.getCategoryList(catOffset)
        }
    }

//    override fun onRequestLoadMore() {
//        binding.progressBar.visibility = View.VISIBLE
//        catOffset++
//        getCategories()
//        binding.progressBar.visibility = View.GONE
//    }

    override fun onNestedRecyclerViewItemClicked(productId: String) {
        if (isConnectedToInternet){
//            goToProductDetailFragment(productId)
            goToProductDetailActivity(productId)
        } else {
            Utils.showNotConnectedToInternetDialog(requireContext())
        }
    }

    override fun onLoadMore() {

    }

//    private fun goToProductDetailFragment(productId: String){
//        val action = OffersFragmentDirections.actionNavigationOffersToProductDetailsFragment(productId)
//        navController.navigate(action)
//    }

    private fun goToProductDetailActivity(productId: String){
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        intent.putExtra(Constants.PRODUCT_ID,productId)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
    }

}