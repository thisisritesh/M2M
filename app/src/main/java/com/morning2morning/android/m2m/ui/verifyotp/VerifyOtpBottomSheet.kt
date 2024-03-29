package com.morning2morning.android.m2m.ui.verifyotp

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.morning2morning.android.m2m.R
import com.morning2morning.android.m2m.data.auth.AuthHelper
import com.morning2morning.android.m2m.data.dbs.Pref
import com.morning2morning.android.m2m.data.dbs.cart.db.UserDetailsDB
import com.morning2morning.android.m2m.data.dbs.cart.entity.UserDetails
import com.morning2morning.android.m2m.data.models.LoginResponse
import com.morning2morning.android.m2m.data.network.ApiClient
import com.morning2morning.android.m2m.data.network.NetworkHelper
import com.morning2morning.android.m2m.data.work.CartWorker
import com.morning2morning.android.m2m.utils.Constants
import com.morning2morning.android.m2m.utils.customviews.CustomToast
import com.morning2morning.android.m2m.utils.customviews.OtpEditText
import com.morning2morning.android.m2m.utils.customviews.RegularTextView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * @author Ritesh Gupta
 * @see <p>https://github.com/riteshmaagadh</p>
 */
@DelicateCoroutinesApi
class VerifyOtpBottomSheet : BottomSheetDialogFragment() {


    override fun getTheme() = R.style.NoBackgroundDialogTheme

    private var firebaseAuth: FirebaseAuth? = null
    private var verificationId = ""
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var otpEditText: OtpEditText
    private lateinit var enterTheOtpSentToText: RegularTextView
    private lateinit var closeIcon: ImageButton
    private lateinit var chronometerTimeRemaining: RegularTextView
    private lateinit var timeCountParent: LinearLayout
    private lateinit var resendParent: LinearLayout
    private lateinit var resendOtpText: RegularTextView
    private lateinit var continueBtn: Button
    private lateinit var waitingForOtp: LinearLayout

    private lateinit var phoneNo: String

    private lateinit var apiClient: ApiClient
    
    companion object{
        private const val TAG = "VerifyOtpBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = View.inflate(requireContext(), R.layout.verify_otp_bottom_sheet_layout, null)
        view.setBackgroundResource(R.drawable.bottom_sheet_bg)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setCanceledOnTouchOutside(false)

        enterTheOtpSentToText = view.findViewById(R.id.enter_the_otp_sent_to_message)
        otpEditText = view.findViewById(R.id.otp_edit_text)
        closeIcon = view.findViewById(R.id.close_icon)
        chronometerTimeRemaining = view.findViewById(R.id.chronometer_otp_remaining_time)
        resendParent = view.findViewById(R.id.resend_otp_parent)
        timeCountParent = view.findViewById(R.id.time_count_parent)
        resendOtpText = view.findViewById(R.id.resend_otp_text)
        continueBtn = view.findViewById(R.id.continue_button_verify)
        waitingForOtp = view.findViewById(R.id.waiting_for_otp)
        apiClient = NetworkHelper.getInstance(requireContext())?.create(ApiClient::class.java)!!


        phoneNo = Pref(requireContext()).getPrefString(Constants.OTP_MOBILE_NUMBER)

        enterTheOtpSentToText.text = "Enter the OTP sent to $phoneNo"

        firebaseAuth = FirebaseAuth.getInstance()

        AuthHelper.sendVerificationCode(
            phoneNo,
            callbacks,
            firebaseAuth!!,
            requireActivity()
        )

        closeIcon.setOnClickListener {
            dialog?.dismiss()
        }

        continueBtn.setOnClickListener {
            val code: String = otpEditText.otpValue!!
            verifyPhoneNumberWithCode(verificationId, code)
        }

        resendOtpText.setOnClickListener {
            AuthHelper.resendVerificationCode(
                phoneNo,
                resendingToken!!,
                callbacks,
                firebaseAuth!!,
                requireActivity()
            )
        }

    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential)
    }


    private fun signInWithCredential(credential: PhoneAuthCredential) {
        firebaseAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    login(phoneNo.substring(3))
                } else {
                    Pref(requireContext()).putPref(Constants.LOGIN_STATUS,false)
                    CustomToast().showToastMessage(activity!!, "Authentication failed. Try Again!")
                }
            }
    }


    private fun login(mobile: String){

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("mobile", mobile)
            .build()

        lifecycleScope.launch(Dispatchers.IO){
            val loginCall = apiClient.loginWithPhoneNumber(requestBody)

            loginCall.enqueue(object : Callback<LoginResponse>{
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    val res = response.body()!!
                    if (res.code == 1){
                        lifecycleScope.launch(Dispatchers.IO){
                            withContext(Dispatchers.Main){
                                Pref(requireContext()).putPref(Constants.LOGIN_STATUS,true)
                                Pref(requireContext()).putPref(Constants.USER_ID, res.result[0].id)
                                Pref(requireContext()).putPref(Constants.USER_EMAIL_ID,res.result[0].email)
                                Pref(requireContext()).putPref(Constants.USER_PHONE_NUMBER,res.result[0].mobile)
                                Pref(requireContext()).putPref(Constants.USER_DISPLAY_NAME,res.result[0].name)
                                Pref(requireContext()).putPref(Constants.IS_NEED_CART_TO_BE_SYNC,true)
                            }

                            UserDetailsDB.getInstance(requireContext())
                                .dao()
                                .insertItem(UserDetails(res.result[0].id,res.result[0].name,res.result[0].email,res.result[0].mobile,""))


//                            if (Pref(requireContext()).getPrefBoolean(Constants.IS_NEED_CART_TO_BE_SYNC))
                            startCartWork()

                            CustomToast().showToastMessage(activity!!, "Login success!")
                            dialog?.dismiss()
//                            fetchUserCartListAndSaveToLocalDb(res.result[0].id)

                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e(TAG, "onFailure: ", t)
                    Pref(requireContext()).putPref(Constants.USER_ID, "")
                    Pref(requireContext()).putPref(Constants.USER_EMAIL_ID,"")
                    Pref(requireContext()).putPref(Constants.USER_PHONE_NUMBER,"")
                    Pref(requireContext()).putPref(Constants.USER_DISPLAY_NAME,"")
                }

            })

        }
    }

    private fun startCartWork(){
        val constraints = Constraints.Builder().setRequiredNetworkType(
            NetworkType.CONNECTED).build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CartWorker::class.java)
            .setConstraints(constraints)
            .build()
        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueue(oneTimeWorkRequest)
    }

    private fun startCountdown() {
        resendParent.visibility = View.GONE
        timeCountParent.visibility = View.VISIBLE
//        binding.otpWillExpireInText.setVisibility(View.VISIBLE)
        object : CountDownTimer(60000, 1000) {
            override fun onTick(l: Long) {
                chronometerTimeRemaining.text = (l / 1000).toString() + " seconds"
            }

            override fun onFinish() {
                resendParent.visibility = View.VISIBLE
                timeCountParent.visibility = View.GONE
//                binding.otpWillExpireInText.setVisibility(View.GONE)
                continueBtn.isEnabled = false
//                binding.verifyOtpButton.setTextColor(resources.getColor(R.color.secondary_text_color))
            }
        }.start()
    }

    private val callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                val code = phoneAuthCredential.smsCode
                if (code != null) {
                    otpEditText.setText(code)
                    verifyPhoneNumberWithCode(verificationId, code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    CustomToast().showToastMessage(requireActivity(), "Invalid request!")
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    CustomToast().showToastMessage(
                        requireActivity(),
                        "The SMS quota for the project has been exceeded!"
                    )
                } else if (e is FirebaseNetworkException) {
                    CustomToast().showToastMessage(requireActivity(), "Weak network connection!")
                } else {
                    CustomToast().showToastMessage(requireActivity(), "Verification failed!")
                }
                Log.e("hfbvriuu", "onVerificationFailed: ", e)
            }

            override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)
                verificationId = s
                resendingToken = forceResendingToken
//                binding.progressBarGetOtpButton.setVisibility(View.GONE)
//                binding.getOtpButton.setEnabled(true)
//                binding.getOtpButton.setText("Get OTP")
//                motionContainer.setTransition(R.id.start, R.id.end)
//                motionContainer.transitionToEnd()
//                binding.verifyOtpButton.setEnabled(true)
//                binding.verifyOtpButton.setTextColor(resources.getColor(R.color.white))
                waitingForOtp.visibility = View.GONE
                continueBtn.isEnabled = true
                startCountdown()
            }
        }


}