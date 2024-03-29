package com.morning2morning.android.m2m.ui.cart

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.adapters.CartAdapter
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.databinding.FragmentCartBinding
import com.morning2morning.android.m2m.ui.checkout.CheckoutActivity
import com.morning2morning.android.m2m.ui.login.LoginBottomSheet
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
class CartFragment : Fragment(), CartAdapter.AdapterCallbacks, OnConnectionChanged {

    private lateinit var binding: FragmentCartBinding
//    private lateinit var navController: NavController
    private var list: List<CartItem> = listOf()
    private lateinit var cartAdapter: CartAdapter

    private var isConnectedToInternet = false

    private lateinit var viewModel: CartViewModel

    companion object{
        private const val TAG = "CartFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ConnectivityManager.init(requireContext(),viewLifecycleOwner,this)



        Handler(Looper.myLooper()!!)
            .postDelayed(Runnable {
                if (isConnectedToInternet){
                    showSuccessUi()
                    fetchCartDetailsFromLocalServer()
                } else {
                    showNoInternetConnectionUi()
                }
            }, 100)

        /*val viewModelFactory = CartViewModelFactory(CartRepository(requireContext()))
        viewModel = ViewModelProvider(this,viewModelFactory)[CartViewModel::class.java]

        viewModel.cartData.observe(viewLifecycleOwner){
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
                    Handler(Looper.myLooper()!!)
                        .postDelayed(Runnable {
                            if (isConnectedToInternet){
                                showSuccessUi()
                                setUpRecyclerView(it.data)
                            } else {
                                showNoInternetConnectionUi()
                            }
                        },100)
                }
            }
        }
*/

//        navController = Navigation.findNavController(view)
        binding.originalPriceSumTextView.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG


        binding.proceedText.setOnClickListener {
            if (Utils.isUserLoggedIn(requireContext())){
                startActivity(Intent(requireContext(), CheckoutActivity::class.java))
                activity?.overridePendingTransition(R.anim.right_in, R.anim.left_out)
            } else {
                LoginBottomSheet().show(activity?.supportFragmentManager!!,"diej")
            }
        }


        lifecycleScope.launch(Dispatchers.IO){

            val liveData = CartDB.getInstance(requireContext())
                .dao()
                .getAllCartItemsLiveData()

//            Log.d("frnece", "onViewCreated: ${list.toString()}" )

            withContext(Dispatchers.Main){

//                val adapter = CartAdapter(list,requireActivity(),requireContext(), this@CartFragment)
//                binding.recyclerView.adapter = adapter

                liveData.observe(viewLifecycleOwner){
//                    adapter.setData(it)

                    if (it.isEmpty()){
                        binding.emptyView.visibility = View.VISIBLE
                        binding.cartDetailsCard.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.cartDetailsCard.visibility = View.VISIBLE
                        binding.itemCountTextView.text = "${it.size} items"
                        binding.discountedPriceSumTextView.text = "\u20B9" +" " + Utils.calculateDiscountedTotalPrice(it)
                        binding.originalPriceSumTextView.text = "\u20B9" +" " + Utils.calculateTotalPrice(it)
                    }
                }

            }
        }


    }

    private fun showNoInternetConnectionUi() {
        binding.noConnectionView.visibility = View.VISIBLE
        binding.shimmerViewContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.cartDetailsCard.visibility = View.GONE
    }

    private fun showSuccessUi() {
        binding.shimmerViewContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun fetchCartDetailsFromLocalServer() {

        lifecycleScope.launch(Dispatchers.IO){
            list = CartDB.getInstance(requireContext())
                .dao()
                .getAllCartItems()

            val liveData = CartDB.getInstance(requireContext())
                .dao()
                .getAllCartItemsLiveData()

            withContext(Dispatchers.Main){

                val adapter = CartAdapter(list,requireActivity(),this@CartFragment)
                binding.recyclerView.adapter = adapter

                liveData.observe(viewLifecycleOwner){
                    adapter.setData(it)

                    if (it.isEmpty()){
                        binding.emptyView.visibility = View.VISIBLE
                        binding.cartDetailsCard.visibility = View.GONE
                    } else {
                        binding.emptyView.visibility = View.GONE
                        binding.cartDetailsCard.visibility = View.VISIBLE
                        binding.itemCountTextView.text = "${it.size} items"
                        binding.discountedPriceSumTextView.text = "\u20B9" +" " + Utils.calculateDiscountedTotalPrice(it)
                        binding.originalPriceSumTextView.text = "\u20B9" +" " + Utils.calculateTotalPrice(it)
                    }

                }

            }
        }



    }




    override fun onItemClicked(productId: String) {
//        val action = CartFragmentDirections.actionNavigationCartToProductDetailsFragment(productId)
//        navController.navigate(action)
        val intent = Intent(requireContext(), ProductDetailsActivity::class.java)
        intent.putExtra(Constants.PRODUCT_ID,productId)
        requireActivity().overridePendingTransition(R.anim.right_in,R.anim.left_out)
        startActivity(intent)
    }

    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
    }

}