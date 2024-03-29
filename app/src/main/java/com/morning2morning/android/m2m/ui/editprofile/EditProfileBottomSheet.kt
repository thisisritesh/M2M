package com.morning2morning.android.m2m.ui.editprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.UserDetailsDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.UserDetails
import com.morning2morning.android.m2m.data.models.CartTransactionResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.ui.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileBottomSheet : BottomSheetDialogFragment() {

    override fun getTheme() = R.style.NoBackgroundDialogTheme


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = View.inflate(requireContext(), R.layout.edit_profile_bottom_sheet_layout, null)
        view.setBackgroundResource(R.drawable.bottom_sheet_bg)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCanceledOnTouchOutside(false)

        val userId = Pref(requireContext()).getPrefString(Constants.USER_ID)
        val emailId = Pref(requireContext()).getPrefString(Constants.USER_EMAIL_ID)
        val phoneNumber = Pref(requireContext()).getPrefString(Constants.USER_PHONE_NUMBER)
        val displayName = Pref(requireContext()).getPrefString(Constants.USER_DISPLAY_NAME)

        val nameEditText: EditText = view.findViewById(R.id.name_edit_text)
        val phoneEditText: EditText = view.findViewById(R.id.phone_number_edit_text)
        val emailEditText: EditText = view.findViewById(R.id.email_edit_text)
        val closeButton: ImageButton = view.findViewById(R.id.close_icon)
        val continueBtn: Button = view.findViewById(R.id.continue_button)

        nameEditText.setText(displayName)
        phoneEditText.setText(phoneNumber)
        emailEditText.setText(emailId)

        closeButton.setOnClickListener {
            dialog?.dismiss()
        }

        continueBtn.setOnClickListener {
            updateUserInfo(nameEditText.text.toString().trim(),emailEditText.text.toString().trim(),phoneEditText.text.toString().trim())
        }


    }

    private fun updateUserInfo(name: String, email: String, mobile: String){

        if (Utils.isUserLoggedIn(requireContext())) {
            val userId = Pref(requireContext()).getPrefString(Constants.USER_ID)
            val apiClient = NetworkHelper.getInstance(requireContext())!!.create(ApiClient::class.java)
            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", userId)
                .addFormDataPart("name", name)
                .addFormDataPart("email", email)
                .addFormDataPart("mobile", mobile)
                .addFormDataPart("address", "")
                .build()



            apiClient.updateUserProfile(requestBody)
                .enqueue(object : Callback<CartTransactionResponse> {
                    override fun onResponse(
                        call: Call<CartTransactionResponse>,
                        response: Response<CartTransactionResponse>
                    ) {
                        if (response.body()!!.code == 1) {

                            lifecycleScope.launch(Dispatchers.IO){
                                UserDetailsDB.getInstance(requireContext())
                                    .dao()
                                    .updateItem(UserDetails(userId,name,email, mobile,""))

                                Pref(requireContext()).putPref(Constants.USER_EMAIL_ID,email)
                                Pref(requireContext()).putPref(Constants.USER_PHONE_NUMBER,mobile)
                                Pref(requireContext()).putPref(Constants.USER_DISPLAY_NAME,name)

                                withContext(Dispatchers.Main){
                                    CustomToast().showToastMessage(requireActivity(),"Profile updated successfully!")
                                    dialog?.dismiss()
                                }

                            }



                        }  else {
                            CustomToast().showToastMessage(requireActivity(),"Failed to update profile!")
                        }
                    }

                    override fun onFailure(call: Call<CartTransactionResponse>, t: Throwable) {
                        CustomToast().showToastMessage(requireActivity(),"Failed to update profile!")
                    }

                })
        }


    }


}