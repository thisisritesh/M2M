package com.morning2morning.android.m2m.ui.sidemenu

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.callbacks.DialogClickListener
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.CartDB
import com.morning2morning.android.m2m.data.dbs.cart.db.UserDetailsDB
import com.morning2morning.android.m2m.data.network.NetworkConstants
import com.morning2morning.android.m2m.databinding.ActivitySideMenuBinding
import com.morning2morning.android.m2m.ui.categoryviewall.CategoryViewAllActivity
import com.morning2morning.android.m2m.ui.commonwebview.WebviewActivity
import com.morning2morning.android.m2m.ui.editprofile.EditProfileBottomSheet
import com.morning2morning.android.m2m.ui.m2mproducts.M2mProductsActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class SideMenuActivity : AppCompatActivity(), OnConnectionChanged {

    private lateinit var binding: ActivitySideMenuBinding

    private var isConnectedToInternet = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySideMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ConnectivityManager.init(this,this,this)

        binding.editProfileTextView.setOnClickListener {
            EditProfileBottomSheet().show(supportFragmentManager,"diej")
        }

        binding.shopByCategoryRoot.setOnClickListener {
            val intent = Intent(this, CategoryViewAllActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }

        binding.m2mProductsParent.setOnClickListener {
            val intent = Intent(this, M2mProductsActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }


        binding.contactUsParent.setOnClickListener {
            Utils.showContactOptionsDialog(this, object : DialogClickListener{
                override fun onPositiveButtonClicked() {
                    openWhatsApp("917317017083","")
                }

                override fun onNegativeButtonClicked() {
                    openDialer()
                }
            })
        }


        binding.termsAndConditionsParent.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra(Constants.WEBVIEW_TITLE, "Terms & Conditions")
            intent.putExtra(Constants.WEBVIEW_URL, NetworkConstants.TERMS_AND_CONDITIONS_URL)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }

        binding.aboutUsParent.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra(Constants.WEBVIEW_TITLE, "About Us")
            intent.putExtra(Constants.WEBVIEW_URL, NetworkConstants.ABOUT_US_URL)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }


        binding.pricingParent.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra(Constants.WEBVIEW_TITLE, "Pricing")
            intent.putExtra(Constants.WEBVIEW_URL, NetworkConstants.PRICING_URL)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }

        binding.privacyPolicyParent.setOnClickListener {
            val intent = Intent(this, WebviewActivity::class.java)
            intent.putExtra(Constants.WEBVIEW_TITLE, "Privacy Policy")
            intent.putExtra(Constants.WEBVIEW_URL, NetworkConstants.PRIVACY_POLICY_URL)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }



        binding.backArrow.setOnClickListener {
            finish()
        }

        binding.logOutRoot.setOnClickListener {
            Utils.showLogoutDialog(this, object : DialogClickListener {
                override fun onPositiveButtonClicked() {
                    lifecycleScope.launch(Dispatchers.IO){
                        CartDB.getInstance(this@SideMenuActivity)
                            .clearAllTables()

                        Pref(this@SideMenuActivity).putPref(Constants.USER_DISPLAY_NAME,"")
                        Pref(this@SideMenuActivity).putPref(Constants.USER_EMAIL_ID,"")
                        Pref(this@SideMenuActivity).putPref(Constants.USER_PHONE_NUMBER,"")
                        Pref(this@SideMenuActivity).putPref(Constants.USER_ID,"")
                        Pref(this@SideMenuActivity).putPref(Constants.USER_DELIVERY_ADDRESS,"")
                        Pref(this@SideMenuActivity).putPref(Constants.LOGIN_STATUS,false)


                        finish()
                    }
                }

                override fun onNegativeButtonClicked() {

                }

            })
        }

        lifecycleScope.launch(Dispatchers.IO){
            val cartLiveData = UserDetailsDB.getInstance(this@SideMenuActivity)
                .dao()
                .getAllRecentSearchesLiveData()

            withContext(Dispatchers.Main){
                cartLiveData.observe(this@SideMenuActivity){
                    if (it[0].email.isNotEmpty()){
                        binding.emailTextView.visibility = View.VISIBLE
                        binding.emailTextView.text = it[0].email
                    } else {
                        binding.emailTextView.visibility = View.GONE
                    }

                    if (it[0].phone.isNotEmpty()){
                        binding.phoneNumberTextView.visibility = View.VISIBLE
                        binding.phoneNumberTextView.text = "+91 ${it[0].phone}"
                    } else {
                        binding.phoneNumberTextView.visibility = View.GONE
                    }

                    if (it[0].name.isNotEmpty()){
                        binding.displayNameTextView.text = "Hi, ${it[0].name}!"
                    } else {
                        binding.displayNameTextView.text = "Hi, there!"
                    }
                }
            }

        }
        
    }



    private fun openWhatsApp(number: String, message: String) {
        try {
            val packageManager: PackageManager = packageManager
            val i = Intent(Intent.ACTION_VIEW)
            val url =
                "https://api.whatsapp.com/send?phone=$number&text=" + URLEncoder.encode(
                    message,
                    "UTF-8"
                )
            i.setPackage("com.whatsapp")
            i.data = Uri.parse(url)
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            } else {
                CustomToast().showToastMessage(this, "WhatsApp not installed!")
            }
        } catch (e: Exception) {
            CustomToast().showToastMessage(this, "WhatsApp not installed!")
        }
    }

    private fun openDialer(){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:7317017083")
        startActivity(intent)
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