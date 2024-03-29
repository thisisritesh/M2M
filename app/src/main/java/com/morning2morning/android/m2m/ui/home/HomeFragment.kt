package com.morning2morning.android.m2m.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.*
import com.morning2morning.android.m2m.data.callbacks.OnPermissionRationaleClicked
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.models.HomeBannerResponse
import com.morning2morning.android.m2m.data.models.HomeCategoryResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.network.NetworkState
import com.morning2morning.android.m2m.data.repositories.HomeRepository
import com.morning2morning.android.m2m.databinding.FragmentHomeBinding
import com.morning2morning.android.m2m.ui.cart.CartActivity
import com.morning2morning.android.m2m.ui.categorydetails.CategoryDetailsActivity
import com.morning2morning.android.m2m.ui.categoryviewall.CategoryViewAllActivity
import com.morning2morning.android.m2m.ui.location.LocationBottomSheetFragment
import com.morning2morning.android.m2m.ui.login.LoginBottomSheet
import com.morning2morning.android.m2m.ui.map.MapsActivity
import com.morning2morning.android.m2m.ui.productdetails.ProductDetailsActivity
import com.morning2morning.android.m2m.ui.search.SearchActivity
import com.morning2morning.android.m2m.ui.sidemenu.SideMenuActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.customviews.CustomToast
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
class HomeFragment : Fragment(), HomeCategoryAdapter.AdapterCallbacks, NestedCategoryAdapter.AdapterCallbacks, OnConnectionChanged {

    private var isScrolling: Boolean = false

    private lateinit var binding: FragmentHomeBinding

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var apiClient: ApiClient

    var homeSliderHandler = Handler()
    var offerSliderHandler = Handler()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationStr: String

//    private lateinit var navController: NavController

    private var isConnectedToInternet = false

    private var catOffset = 0

    private var categoryList: MutableList<HomeCategoryResponse.Categories.Category> = mutableListOf()

    private lateinit var nestedHomeAdapter: NestedCategoryAdapter

//    var isNestedRecyclerViewScrolling = false
//    var currentItems = 0
//    var scrollOutItems = 0
//    var totalItems = 0

    private lateinit var homeBannerViewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        ConnectivityManager.init(requireContext(),viewLifecycleOwner,this)

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
                        Handler(Looper.myLooper()!!)
                            .postDelayed(Runnable {
                                if (isConnectedToInternet){
                                    binding.cartDetailsCard.visibility = View.VISIBLE
                                } else {
                                    binding.cartDetailsCard.visibility = View.GONE
                                }
                                binding.itemCountTextView.text = "${it.size} items"
                                binding.discountedPriceSumTextView.text = "\u20B9" +" " + Utils.calculateDiscountedTotalPrice(it)
                                binding.originalPriceSumTextView.text = "\u20B9" +" " + Utils.calculateTotalPrice(it)
                            },100)
                    }
                }
            }

        }

    }

//    private fun setUpHomeBanner(dataList: MutableList<HomeBannerResponse.Result>) {
//        val homeBannerAdapter = HomeBannerAdapter(
//            dataList,
//            binding.homeBannerViewPager,
//            object : HomeBannerAdapter.AdapterCallback {
//                override fun onItemClicked(productId: String) {
//                    // Handle click here
//                }
//            })
//        binding.homeBannerViewPager.adapter = homeBannerAdapter
//        val indicatorAdapter = ViewPagerIndicatorAdapter(dataList.size)
//        binding.indicatorRecyclerView.adapter = indicatorAdapter
//        binding.homeBannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                homeSliderHandler.removeCallbacks(homeSliderRunnable)
//                homeSliderHandler.postDelayed(homeSliderRunnable, 3500)
//                indicatorAdapter.selectItem(position)
//            }
//        })
//
//
//
//    }





    private fun setUpData() {

        Handler(Looper.myLooper()!!)
            .postDelayed(Runnable {
                getFineLocation()
            },1000)

        initViewPager()

        homeViewModel.getHomeBannerList()

        homeViewModel.homeBannerData.observe(viewLifecycleOwner){
            when(it){
                is NetworkState.Loading -> {
//                    showLoadingUi()
                }
                is NetworkState.Failed -> {
//                    if (it.message.equals(NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE)){
//                        showNoInternetConnectionUi()
//                    } else {
//                        showErrorUi()
//                    }
                }
                is NetworkState.Success -> {
//                    showSuccessUi()
                    setUpHomeBanner(it.data as MutableList<HomeBannerResponse.Result>)
                }
            }
        }




        getCategories()

        homeViewModel.categoryData.observe(viewLifecycleOwner){
            when(it){
                is NetworkState.Loading -> {
//                    binding.progressBar.visibility = View.VISIBLE
                    showLoadingUi()
//                    Log.d("bubnk", "setUpData: ")
                }
                is NetworkState.Failed -> {
//                    Log.d("bubnk", "setUpData: ")
                    if (it.message.equals(NetworkConstants.NO_INTERNET_CONNECTION_MESSAGE)){
                        showNoInternetConnectionUi()
                    } else {
                        showErrorUi()
                    }
                }
                is NetworkState.Success -> {
                    Log.d("cerncirec", "setUpData: " + it.data.toString())
                    showSuccessUi()
                    setUpNestedRecyclerView(it.data)
                }
            }
        }



        homeViewModel.getCategoryGridList()

        homeViewModel.categoryGridData.observe(viewLifecycleOwner){
            when(it){
                is NetworkState.Loading -> {

                }
                is NetworkState.Failed -> {

                }
                is NetworkState.Success -> {
                    setUpCategoryItems(it.data)
                }
            }
        }


        setUpOffersBanner()


    }

    private fun setUpOffersBanner() {
        val list: MutableList<Int> = ArrayList()
        list.add(R.mipmap.offer_banner)
        list.add(R.mipmap.offer_banner)
        list.add(R.mipmap.offer_banner)
        val adapter = OfferSliderAdapter(
            list,
            binding.offersViewPager,
            object : OfferSliderAdapter.AdapterCallback {
                override fun onItemClicked(productId: String) {
                    // handle click here
                }
            })
        binding.offersViewPager.adapter = adapter
        binding.offersViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                offerSliderHandler.removeCallbacks(offersSliderRunnable)
                offerSliderHandler.postDelayed(offersSliderRunnable, 3500)
            }
        })
    }


    private fun setUpHomeBanner(dataList: MutableList<HomeBannerResponse.Result>) {
        val homeBannerAdapter = HomeBannerAdapter(
            dataList,
            homeBannerViewPager,
            object : HomeBannerAdapter.AdapterCallback {
                override fun onItemClicked(productId: String) {
                    goToProductDetailActivity(productId)
//                    goToProductDetailFragment(productId)
                    // Handle click here
                }
            })
        homeBannerViewPager.adapter = homeBannerAdapter
        val indicatorAdapter = ViewPagerIndicatorAdapter(dataList.size)
        binding.indicatorRecyclerView.adapter = indicatorAdapter
        homeBannerViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                homeSliderHandler.removeCallbacks(homeSliderRunnable)
                homeSliderHandler.postDelayed(homeSliderRunnable, 3500)
                indicatorAdapter.selectItem(position)
            }
        })



    }

    private val homeSliderRunnable = Runnable {
        homeBannerViewPager.currentItem = homeBannerViewPager.currentItem + 1
    }

    private fun getCategories(){
        homeViewModel.getCategoryList(catOffset)
        catOffset += 1
    }

    val offersSliderRunnable = Runnable {
        binding.offersViewPager.currentItem = binding.offersViewPager.currentItem + 1
    }

    private fun initViewPager(){
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
    }

    private fun init(view: View){
//        navController = Navigation.findNavController(view)

        apiClient = NetworkHelper.getInstance(requireContext())?.create(ApiClient::class.java)!!

        val viewModelFactory = HomeViewModelFactory(HomeRepository(apiClient))
        homeViewModel = ViewModelProvider(this,viewModelFactory)[HomeViewModel::class.java]



        binding.searchCard.setOnClickListener {
            startActivity(Intent(requireContext(), SearchActivity::class.java))
            activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
//            navController.navigate(R.id.action_navigation_home_to_navigation_search)
        }

        binding.searchButton.setOnClickListener {
            if (isConnectedToInternet){
                startActivity(Intent(requireContext(),SearchActivity::class.java))
                activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
//                navController.navigate(R.id.action_navigation_home_to_navigation_search)
            }
        }

        binding.viewAllCategoryText.setOnClickListener {
            if (isConnectedToInternet){
                val intent = Intent(requireContext(),CategoryViewAllActivity::class.java)
//                intent.putExtra(Constants.PRODUCT_ID,productId)
                requireActivity().overridePendingTransition(R.anim.right_in,R.anim.left_out)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
//                navController.navigate(R.id.action_navigation_home_to_categoryViewAllFragment)
            }
        }

        // user_profile_button
        binding.userProfileButton.setOnClickListener {
            if (isConnectedToInternet){
                if (Pref(requireContext()).getPrefBoolean(Constants.LOGIN_STATUS)){
                    startActivity(Intent(requireContext(),SideMenuActivity::class.java))
                    activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
                } else {
                    LoginBottomSheet().show(activity?.supportFragmentManager!!,"diej")
                }
            } else {
                Utils.showNotConnectedToInternetDialog(requireContext())
            }
        }


//        location_root_layout
        binding.locationRootLayout.setOnClickListener {
            if (isConnectedToInternet){
                LocationBottomSheetFragment().show(activity?.supportFragmentManager!!,"ceoec")
            } else {
                Utils.showNotConnectedToInternetDialog(requireContext())
            }
        }


        val recyclerView = binding.nestedRecyclerView

        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        nestedHomeAdapter = NestedCategoryAdapter(categoryList,requireContext(),this,recyclerView)
        recyclerView.adapter = nestedHomeAdapter


        binding.proceedText.setOnClickListener {
            startActivity(Intent(requireContext(),CartActivity::class.java))
            activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }


    }


    private fun setUpForLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()
        locationRequest.interval = 500
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun setUpNestedRecyclerView(list: List<HomeCategoryResponse.Categories.Category> ){
        categoryList.addAll(list)
        nestedHomeAdapter.notifyDataSetChanged()
        binding.nestedRecyclerView.visibility = View.VISIBLE
//        binding.progressBar.visibility = View.GONE
    }

    private fun setUpCategoryItems(list: List<HomeCategoryResponse.Categories.Category>) {
        val recyclerView = binding.categoryRecyclerView
        recyclerView.adapter = HomeCategoryAdapter(list,this)
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }



    override fun onStart() {
        super.onStart()
        if (isConnectedToInternet && this::fusedLocationClient.isInitialized){
            startLocationUpdates()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isConnectedToInternet && this::fusedLocationClient.isInitialized){
            stopLocationUpdates()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            locationStr = Utils.getAddressFromCoordinates(locationResult.lastLocation.latitude,locationResult.lastLocation.longitude,requireContext())

            Pref(requireContext()).putPref(Constants.USER_DELIVERY_ADDRESS, locationStr)

            latitude = locationResult.lastLocation.latitude
            longitude = locationResult.lastLocation.longitude
        }
    }


    private fun showNoInternetConnectionUi(){
        binding.locationRootLayout.visibility = View.GONE
        binding.logoImage.visibility = View.GONE
        binding.punchLineText.visibility = View.GONE
        binding.searchHintText.visibility = View.GONE
        binding.noInternetConnectionView.visibility = View.VISIBLE
        binding.nestedRecyclerView.visibility = View.GONE
        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.uiRootLayout.visibility = View.GONE
        binding.searchCard.visibility = View.GONE
        binding.locationRootLayout.visibility = View.GONE
        binding.cartDetailsCard.visibility = View.GONE
    }

    private fun showLoadingUi(){
        binding.locationRootLayout.visibility = View.VISIBLE
        binding.shimmerSearchViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.locationDetailTextView.text = "Locating..."
        binding.logoImage.visibility = View.GONE
        binding.punchLineText.visibility = View.GONE
        binding.searchHintText.visibility = View.GONE
        binding.noInternetConnectionView.visibility = View.GONE
        binding.nestedRecyclerView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.uiRootLayout.visibility = View.GONE
    }

    private fun showErrorUi(){
        binding.locationRootLayout.visibility = View.VISIBLE
        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.locationDetailTextView.text = "Locating..."
        binding.logoImage.visibility = View.GONE
        binding.punchLineText.visibility = View.GONE
        binding.searchHintText.visibility = View.GONE
        binding.noInternetConnectionView.visibility = View.GONE
        binding.nestedRecyclerView.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.uiRootLayout.visibility = View.GONE

    }

    private fun showSuccessUi(){
        binding.locationRootLayout.visibility = View.VISIBLE
        binding.shimmerSearchViewContainer.visibility = View.GONE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.logoImage.visibility = View.VISIBLE
        binding.punchLineText.visibility = View.VISIBLE
        binding.searchHintText.visibility = View.VISIBLE
        binding.nestedRecyclerView.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE
        binding.noInternetConnectionView.visibility = View.GONE
        binding.uiRootLayout.visibility = View.VISIBLE

        if (this::locationStr.isInitialized){
            binding.locationDetailTextView.text = locationStr
            Pref(requireContext()).putPref(Constants.USER_DELIVERY_ADDRESS,locationStr)
        } else {
            binding.locationDetailTextView.text = "Locating..."
        }

        Glide.with(requireContext())
            .load(R.mipmap.high_resolution_horizon_m2m_logo)
            .into(binding.logoImage)

    }


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Do if the permission is granted
            getFineLocation()
        }
        else {
            // Do otherwise
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)){
                Utils.showPermissionRationaleBottomSheet(requireActivity(), object :
                    OnPermissionRationaleClicked {
                    override fun onPositiveButtonClicked(tag: String) {

                    }

                    override fun onNegativeButtonClicked() {

                    }
                }, "hfeuhfu","Permission Denied!","We need this location permission to set your current location as your delivery address. Kindly grant the permission!")
            }
        }
    }


    private fun getFineLocation() {
        if (isConnectedToInternet){

            setUpForLocation()

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (this::fusedLocationClient.isInitialized){
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location : Location? ->
                            // Got last known location. In some rare situations this can be null.
                            if (location != null){
                                locationStr = Utils.getAddressFromCoordinates(location.latitude,
                                    location.longitude,requireContext())

                                val userDeliveryAddress = Pref(requireContext()).getPrefString(Constants.USER_DELIVERY_ADDRESS)
                                if (userDeliveryAddress.isEmpty()) {
                                    binding.locationDetailTextView.text = locationStr
                                } else {
                                    binding.locationDetailTextView.text = userDeliveryAddress
                                }

                                Pref(requireContext()).putPref(Constants.USER_DELIVERY_ADDRESS, locationStr)
//                    CustomToast().showToastMessage(requireActivity(),"Location = $locationStr")
                                latitude = location.latitude
                                longitude = location.longitude
                            } else {
                                Pref(requireContext()).getPrefString(Constants.USER_DELIVERY_ADDRESS)
                            }
                        }
                        .addOnFailureListener {
                            CustomToast().showToastMessage(requireActivity(),"Failed to get location")
                        }
                }
            } else {
                Utils.showLocationRequestBottomSheet(requireActivity(), object : OnPermissionRationaleClicked{
                    override fun onPositiveButtonClicked(tag: String) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }

                    override fun onNegativeButtonClicked() {
                        startActivity(Intent(requireContext(), MapsActivity::class.java))
                    }
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val userDeliveryAddress = Pref(requireContext()).getPrefString(Constants.USER_DELIVERY_ADDRESS)
        if (userDeliveryAddress.isEmpty()){
            if (this::locationStr.isInitialized){
                binding.locationDetailTextView.text = locationStr
            }
        } else {
            binding.locationDetailTextView.text = userDeliveryAddress
        }
    }

    override fun onItemClicked(cat_id: String, cat_title: String) {
        goToCategoryDetailsActivity(cat_id,cat_title)
    }

    override fun onViewAllClicked(cat_id: String, cat_title: String) {
        goToCategoryDetailsActivity(cat_id,cat_title)
    }

    override fun onNestedRecyclerViewItemClicked(productId: String) {
        goToProductDetailActivity(productId)
    }

    override fun onLoadMore() {
//        Log.d("cneinece", "onLoadMore: ")
//        getCategories()
    }

//    private fun goToCategoryDetailsFragment(cat_id: String, cat_title: String){
//        val action = HomeFragmentDirections.actionNavigationHomeToCategoryDetailsFragment(cat_id,cat_title)
//        navController.navigate(action)
//    }

    private fun goToCategoryDetailsActivity(cat_id: String, cat_title: String){
        val intent = Intent(requireContext(),CategoryDetailsActivity::class.java)
        intent.putExtra(Constants.CATEGORY_ID,cat_id)
        intent.putExtra(Constants.CATEGORY_TITLE,cat_title)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
    }


//    private fun goToProductDetailFragment(productId: String){
//        val action = HomeFragmentDirections.actionNavigationHomeToProductDetailsFragment(productId)
//        navController.navigate(action)
//    }

    private fun goToProductDetailActivity(productId: String){
        val intent = Intent(requireContext(),ProductDetailsActivity::class.java)
        intent.putExtra(Constants.PRODUCT_ID,productId)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
    }


}