package com.morning2morning.android.m2m.ui.location

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.ui.map.MapsActivity
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.connection.ConnectivityManager
import com.morning2morning.android.m2m.utils.connection.OnConnectionChanged
import com.morning2morning.android.m2m.utils.customviews.RegularTextView

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class LocationBottomSheetFragment : BottomSheetDialogFragment(), OnConnectionChanged {


    override fun getTheme(): Int = R.style.NoBackgroundDialogTheme


    private lateinit var closeIcon: ImageButton
    private lateinit var selectedLocationText: RegularTextView
//    private lateinit var selectYourCurrentAddress: LinearLayout


    private var isConnectedToInternet = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = View.inflate(requireContext(), R.layout.location_bottom_sheet_layout, null)
        view.setBackgroundResource(R.drawable.bottom_sheet_bg)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeIcon = view.findViewById(R.id.close_icon)
        selectedLocationText = view.findViewById(R.id.selected_location_text_view)
//        selectYourCurrentAddress = view.findViewById(R.id.select_your_current_address)
        val addNewAddress: RegularTextView = view.findViewById(R.id.add_new_location)

        ConnectivityManager.init(requireContext(),viewLifecycleOwner,this)

        dialog?.setCanceledOnTouchOutside(false)


        closeIcon.setOnClickListener {
            dialog?.dismiss()
        }

        addNewAddress.setOnClickListener {
            startActivity(Intent(requireContext(), MapsActivity::class.java))
            dialog?.dismiss()
        }

    }

    override fun onResume() {
        super.onResume()

        selectedLocationText.text = Pref(requireContext()).getPrefString(Constants.USER_DELIVERY_ADDRESS)

    }


    override fun onInternetConnectionChanged(isConnected: Boolean) {
        isConnectedToInternet = isConnected
    }

}