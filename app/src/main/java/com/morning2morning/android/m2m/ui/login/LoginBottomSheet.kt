package com.morning2morning.android.m2m.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.ui.verifyotp.VerifyOtpBottomSheet
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.ui.Utils

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
class LoginBottomSheet : BottomSheetDialogFragment() {

    override fun getTheme(): Int = R.style.NoBackgroundDialogTheme


    private lateinit var closeIcon: ImageButton
    private lateinit var phoneNumberEditText: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var continueButton: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = View.inflate(requireContext(), R.layout.login_bottom_sheet_layout, null)
        view.setBackgroundResource(R.drawable.bottom_sheet_bg)
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeIcon = view.findViewById(R.id.close_icon)
        phoneNumberEditText = view.findViewById(R.id.phone_number_edit_text)
        checkBox = view.findViewById(R.id.agree_to_terms_and_conditions_checkbox)
        continueButton = view.findViewById(R.id.continue_button)

        phoneNumberEditText.addTextChangedListener(textWatcher)




        dialog?.setCanceledOnTouchOutside(false)


        continueButton.setOnClickListener {
            val phoneNumber: String = phoneNumberEditText.text.toString().trim()
            if (Utils.isValidMobile(phoneNumber)) {
                Pref(requireContext()).putPref(Constants.OTP_MOBILE_NUMBER, "+91$phoneNumber")


                dialog?.dismiss()

                VerifyOtpBottomSheet().show(requireFragmentManager(),"eojd")

            } else {
                phoneNumberEditText.error = "Enter a valid phone number!"
            }


        }

        closeIcon.setOnClickListener {
            dialog?.dismiss()
        }


    }



    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            val phoneNumber: String = phoneNumberEditText.text.toString().trim()
            continueButton.isEnabled = !TextUtils.isEmpty(phoneNumber)
        }

        override fun afterTextChanged(editable: Editable) {}
    }




}