package com.morning2morning.android.m2m.utils.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.callbacks.DialogClickListener
import com.morning2morning.android.m2m.data.callbacks.OnPermissionRationaleClicked
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.entity.CartItem
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.customviews.BoldTextView
import com.morning2morning.android.m2m.utils.customviews.RegularTextView
import java.util.*
import java.util.regex.Pattern

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
object Utils {

    fun isValidMobile(phoneNumber: String): Boolean {
        val regexStr = "^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[789]\\d{9}\$";
        val p = Pattern.compile(regexStr)
        val m = p.matcher(phoneNumber)
        return (m.find() && m.group().equals(phoneNumber))
    }


    fun isOnline(context: Context): Boolean {
        val connectivityManager: ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo: NetworkInfo = connectivityManager.getActiveNetworkInfo()!!
        return netInfo != null && netInfo.isConnected
    }

    fun isUserLoggedIn(context: Context) : Boolean {
        return Pref(context).getPrefBoolean(Constants.LOGIN_STATUS)
    }

    fun calculateTotalPrice(itemCount: Int, price: Int) : Int {
        return itemCount * price
    }

    fun showNotConnectedToInternetDialog(context: Context){
        val layoutInflater = LayoutInflater.from(context);
        val view = layoutInflater.inflate(R.layout.not_connected_to_internet_dialog, null)

        val builder = AlertDialog.Builder(context)
        val alertDialog = builder.create()
        alertDialog.setView(view)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val yesButton: Button = view.findViewById(R.id.yesButton)

        yesButton.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun showContactOptionsDialog(context: Context, dialogClickListener: DialogClickListener){
        val layoutInflater = LayoutInflater.from(context);
        val view = layoutInflater.inflate(R.layout.contact_options_dialog_layout, null)

        val builder = AlertDialog.Builder(context)
        val alertDialog = builder.create()
        alertDialog.setView(view)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val whatsappAction: ImageButton = view.findViewById(R.id.whatsapp_action)
        val phoneAction: ImageButton = view.findViewById(R.id.phone_action)

        whatsappAction.setOnClickListener {
            dialogClickListener.onPositiveButtonClicked()
            alertDialog.dismiss()
        }

        phoneAction.setOnClickListener {
            dialogClickListener.onNegativeButtonClicked()
            alertDialog.dismiss()
        }

    }

    fun showLogoutDialog(context: Context, dialogClickListener: DialogClickListener){
        val layoutInflater = LayoutInflater.from(context);
        val view = layoutInflater.inflate(R.layout.logout_dialog_item_layout, null)

        val builder = AlertDialog.Builder(context)
        val alertDialog = builder.create()
        alertDialog.setView(view)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val yesButton: Button = view.findViewById(R.id.yesButton)
        val noButton: RegularTextView = view.findViewById(R.id.noButton)

        yesButton.setOnClickListener {
            Pref(context).putPref(Constants.USER_ID,"")
            Pref(context).putPref(Constants.USER_DISPLAY_NAME,"")
            Pref(context).putPref(Constants.LOGIN_STATUS,false)
            Pref(context).putPref(Constants.USER_PHONE_NUMBER,"")
            Pref(context).putPref(Constants.USER_EMAIL_ID,"")
            dialogClickListener.onPositiveButtonClicked()
            alertDialog.dismiss()
        }

        noButton.setOnClickListener {
            dialogClickListener.onNegativeButtonClicked()
            alertDialog.dismiss()
        }
    }

    fun showPermissionRationaleBottomSheet(activity: Activity, onPermissionRationaleClicked: OnPermissionRationaleClicked, tag: String, headingText: String, bodyText: String){
        val bottomSheetDialog = BottomSheetDialog(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.permission_rationale_bottom_sheet_layout,null)
        bottomSheetDialog.setContentView(view)

        val positiveButton: Button = view.findViewById(R.id.positive_button_permission_rationale)
        val negativeButton: Button = view.findViewById(R.id.negative_buton_permission_rationale)
        val headingTextView: BoldTextView = view.findViewById(R.id.heading_text_permission_rationale)
        val bodyTextView: RegularTextView = view.findViewById(R.id.body_text_permission_rationale)

        negativeButton.visibility = View.GONE

        headingTextView.text = headingText
        bodyTextView.text = bodyText

        positiveButton.setOnClickListener {
            onPermissionRationaleClicked.onPositiveButtonClicked(tag)
            bottomSheetDialog.dismiss()
        }

        negativeButton.setOnClickListener {
            onPermissionRationaleClicked.onNegativeButtonClicked()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun showLocationRequestBottomSheet(activity: Activity, onPermissionRationaleClicked: OnPermissionRationaleClicked){
        val bottomSheetDialog = BottomSheetDialog(activity)
        val view = LayoutInflater.from(activity).inflate(R.layout.location_request_layout,null)
        bottomSheetDialog.setContentView(view)

        val positiveButton: FrameLayout = view.findViewById(R.id.positive_button_location_request)
        val negativeButton: FrameLayout = view.findViewById(R.id.negative_button_location_request)


        positiveButton.setOnClickListener {
            onPermissionRationaleClicked.onPositiveButtonClicked("")
            bottomSheetDialog.dismiss()
        }

        negativeButton.setOnClickListener {
            onPermissionRationaleClicked.onNegativeButtonClicked()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setCancelable(false)

        bottomSheetDialog.show()
    }

    fun getAddressFromCoordinates(latitude: Double, longitude: Double,context: Context) : String {
        val geocoder = Geocoder(context, Locale.getDefault())

        val stringBuilder = StringBuilder()
        try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude,longitude,1)
            if (addresses.isNotEmpty()){
                val address = addresses[0]
                for (position in 0..address.maxAddressLineIndex) {
                    stringBuilder.append(address.getAddressLine(position)).append("\n")
                    stringBuilder.append(address.locality).append("\n")
                    stringBuilder.append(address.postalCode).append("\n")
                    stringBuilder.append(address.countryName)
                }
            }
        } catch (e: Exception){
            e.printStackTrace()
        }

        return stringBuilder.toString()
    }

    fun calculateTotalPrice(list: List<CartItem>) : Int {
        var totalPrice = 0;
        for (item in list){
            totalPrice += item.totalPrice
        }
        return totalPrice
    }

    fun calculateDiscountedTotalPrice(list: List<CartItem>) : Int {
        var totalDiscountedPrice = 0;
        for (item in list){
            totalDiscountedPrice += item.totalDiscountedPrice
        }
        return totalDiscountedPrice
    }

//    fun showFailedToConnectBottomSheet(activity: Activity){
//        Utils.showInfoBottomSheet(activity, object : OnPermissionRationaleClicked{
//            override fun onPositiveButtonClicked(tag: String) {
//
//            }
//
//            override fun onNegativeButtonClicked() {
//
//            }
//
//        },"network_request","Something went wrong!","Failed to connect to the server. Please check your internet connection!","Okay")
//    }

//    fun showInfoBottomSheet(activity: Activity, onPermissionRationaleClicked: OnPermissionRationaleClicked, tag: String, headingText: String, bodyText: String, positiveButtonText: String){
//        val bottomSheetDialog = BottomSheetDialog(activity)
//        val view = LayoutInflater.from(activity).inflate(R.layout.permission_rationale_bottom_sheet_layout,null)
//        bottomSheetDialog.setContentView(view)
//
//        val positiveButton: Button = view.findViewById(R.id.positive_button_permission_rationale)
//        val negativeButton: Button = view.findViewById(R.id.negative_buton_permission_rationale)
//        val headingTextView: BoldTextView = view.findViewById(R.id.heading_text_permission_rationale)
//        val bodyTextView: NormalTextView = view.findViewById(R.id.body_text_permission_rationale)
//
//        headingTextView.text = headingText
//        bodyTextView.text = bodyText
//        positiveButton.text = positiveButtonText
////        negativeButton.text = negativeButtonText
//
//
//        positiveButton.setOnClickListener {
//            onPermissionRationaleClicked.onPositiveButtonClicked(tag)
//            bottomSheetDialog.dismiss()
//        }
//
//        negativeButton.setOnClickListener {
//            onPermissionRationaleClicked.onNegativeButtonClicked()
//            bottomSheetDialog.dismiss()
//        }
//
//        bottomSheetDialog.show()
//    }

//    fun showNotConnectedToInternetDialog(context: Context){
//        var layoutInflater = LayoutInflater.from(context);
//        var view = layoutInflater.inflate(R.layout.not_connected_to_internet_dialog, null)
//
//        var builder = AlertDialog.Builder(context)
//        var alertDialog = builder.create()
//        alertDialog.setView(view)
//        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        alertDialog.show()
//
//        val yesButton: Button = view.findViewById(R.id.yesButton)
//
//        yesButton.setOnClickListener {
//            alertDialog.dismiss()
//        }
//    }

//    fun showLogoutDialog(context: Context, onAlertDialogItemClicked: OnAlertDialogItemClicked){
//        var layoutInflater = LayoutInflater.from(context);
//        var view = layoutInflater.inflate(R.layout.logout_dialog_layout, null)
//
//        var builder = AlertDialog.Builder(context)
//        var alertDialog = builder.create()
//        alertDialog.setView(view)
//        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        alertDialog.show()
//
//        val yesButton: Button = view.findViewById(R.id.yesButton)
//        val noButton: Button = view.findViewById(R.id.noButton)
//
//        yesButton.setOnClickListener {
//            onAlertDialogItemClicked.onPositiveButtonClicked()
//            alertDialog.dismiss()
//        }
//
//        noButton.setOnClickListener {
//            onAlertDialogItemClicked.onNegativeButtonClicked()
//            alertDialog.dismiss()
//        }
//
//    }


}