package com.life.android.fragments;

import static android.content.Context.MODE_PRIVATE;

import static com.life.android.helper.CMExtenstionKt.hideKeyboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.life.android.AppConfig;
import com.life.android.MainActivity;
import com.life.android.PlainActivity;
import com.life.android.R;
import com.life.android.database.DatabaseHelper;
import com.life.android.databinding.FragmentOtpBinding;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.LoginApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.User;
import com.life.android.utils.Constants;
import com.life.android.utils.ToastMsg;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class OtpFragment extends Fragment {

    public User loggedInUser = null;

    private FragmentOtpBinding binding;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private PlainActivity plainActivity = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        plainActivity = ((PlainActivity) requireActivity());

        String mobileNumber = loggedInUser.getCountry_code() + "" + loggedInUser.getPhone();
        binding.textPhoneNumber.setText(mobileNumber);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                e.printStackTrace();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    new ToastMsg(requireContext()).toastIconError("Invalid credentials");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    new ToastMsg(requireContext()).toastIconError("The SMS quota for the project has been exceeded");
                } else {
                    new ToastMsg(requireContext()).toastIconError(e.getLocalizedMessage());
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                }
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };


        binding.resendOtp.setOnClickListener(v -> {
            hideKeyboard(v);
            resendVerificationCode(binding.textPhoneNumber.getText().toString(), mResendToken);
        });

        binding.otpVerifyBtn.setOnClickListener(v -> {
            hideKeyboard(v);
            if (binding.otpInputField.getText() == null || binding.otpInputField.getText().toString().length() < 6) {
                new ToastMsg(requireContext()).toastIconError("Please enter 6 digit otp");
            } else {
                otpVerification(binding.otpInputField.getText().toString());
            }
        });
        plainActivity.activityIndicator(true);
        processedForMobileNumberAuthentication(binding.textPhoneNumber.getText().toString());
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        if (plainActivity != null && !plainActivity.isFinishing()) {
            plainActivity.activityIndicator(true);
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(requireActivity())
                            .setCallbacks(mCallbacks)
                            .setForceResendingToken(token)
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }

    }

    private void processedForMobileNumberAuthentication(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(30L, TimeUnit.SECONDS)
                        .setActivity(requireActivity())
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void otpVerification(String otp) {
        if (plainActivity != null && !plainActivity.isFinishing()) {
            plainActivity.activityIndicator(true);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (plainActivity != null && !plainActivity.isFinishing()) {
                        if (task.isSuccessful()) {
                            if (task.getResult() != null) {
                                FirebaseUser fcmUser = task.getResult().getUser();
                                if (fcmUser != null) {
                                    verifyOtpFirebase(loggedInUser.getUser_id(), fcmUser.getUid());
                                }
                            } else {
                                plainActivity.activityIndicator(false);
                                new ToastMsg(requireContext()).toastIconError("User details not found, Please try again!");
                            }
                        } else {
                            plainActivity.activityIndicator(false);
                            if (task.getException() != null) {
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    new ToastMsg(requireContext()).toastIconError("The verification code entered was invalid");
                                } else {
                                    new ToastMsg(requireContext()).toastIconError(task.getException().getLocalizedMessage());
                                }
                            }
                        }
                    }
                });
    }

    private void verifyOtpFirebase(String userId, String firebase_auth_token) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);

        Call<User> call = api.verifyOtpFirebase(userId, firebase_auth_token, AppConfig.API_KEY);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    if (response.code() == 200 && response.body() != null) {
                        loggedInUser = response.body();
                        saveUserInfo();
                    } else {
                        new ToastMsg(requireContext()).toastIconError(getResources().getString(R.string.something_went_wrong));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    t.printStackTrace();
                    new ToastMsg(requireContext()).toastIconError(getResources().getString(R.string.something_went_wrong));
                }
            }
        });
    }


    public void saveUserInfo() {
        if (plainActivity != null && !plainActivity.isFinishing()) {
            SharedPreferences.Editor editor = plainActivity.getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
            editor.putString("name", loggedInUser.getName());
            editor.putString("email", loggedInUser.getEmail());
            editor.putString(Constants.USER_WALLET_AMOUNT, loggedInUser.getWallet_amount());
            editor.putString("id", loggedInUser.getUser_id());
            editor.putBoolean("status", true);
            editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
            editor.apply();

            DatabaseHelper db = new DatabaseHelper(plainActivity);
            db.deleteUserData();
            db.insertUserData(loggedInUser);
            updateSubscriptionStatus(loggedInUser.getUser_id());
        }
    }

    private void updateSubscriptionStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(@NonNull Call<ActiveStatus> call, @NonNull Response<ActiveStatus> response) {
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            ActiveStatus activeStatus = response.body();

                            DatabaseHelper db = new DatabaseHelper(plainActivity);
                            db.deleteAllActiveStatusData();
                            db.insertActiveStatusData(activeStatus);

                            Intent intent = new Intent(requireContext(), MainActivity.class);
                            startActivity(intent);
                            plainActivity.finishAffinity();
                            plainActivity.overridePendingTransition(R.anim.enter, R.anim.exit);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActiveStatus> call, @NonNull Throwable t) {
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    t.printStackTrace();
                    new ToastMsg(requireContext()).toastIconError(getResources().getString(R.string.something_went_wrong));
                }
            }
        });
    }
}