package com.life.android;

import static com.life.android.helper.CMExtenstionKt.hideKeyboard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.LoginApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.User;
import com.life.android.utils.Constants;
import com.life.android.utils.ToastMsg;
import com.google.android.material.textfield.TextInputEditText;
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

public class OtpActivityNew extends AppCompatActivity {

    private TextView textPhoneNumber;
    private TextInputEditText otpInputField;
    private User user;
    private ProgressDialog dialog;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_otp_new);

        if (getIntent().hasExtra(Constants.USER_MODEL)) {
            user = getIntent().getParcelableExtra(Constants.USER_MODEL);
        }

        mAuth = FirebaseAuth.getInstance();

        otpInputField = findViewById(R.id.otpInputField);
        textPhoneNumber = findViewById(R.id.textPhoneNumber);

        String mobileNumber = user.getCountry_code() + "" + user.getPhone();
        textPhoneNumber.setText(mobileNumber);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                dialog.cancel();
                e.printStackTrace();
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    new ToastMsg(OtpActivityNew.this).toastIconError("Invalid credentials");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    new ToastMsg(OtpActivityNew.this).toastIconError("The SMS quota for the project has been exceeded");
                } else {
                    new ToastMsg(OtpActivityNew.this).toastIconError(e.getLocalizedMessage());
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                dialog.cancel();
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        initializeProgress();


        findViewById(R.id.resend_otp).setOnClickListener(v -> {
            hideKeyboard(v);
            resendVerificationCode(textPhoneNumber.getText().toString(), mResendToken);
        });

        findViewById(R.id.otp_verify_btn).setOnClickListener(v -> {
            hideKeyboard(v);
            if (otpInputField.getText() == null || otpInputField.getText().toString().length() < 6) {
                new ToastMsg(this).toastIconError("Please enter 6 digit otp");
            } else {
                otpVerification(otpInputField.getText().toString());
            }
        });
        processedForMobileNumberAuthentication(textPhoneNumber.getText().toString());
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        dialog.setMessage("Resending OTP");
        dialog.show();
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .setForceResendingToken(token)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void processedForMobileNumberAuthentication(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(30L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void otpVerification(String otp) {
        dialog.setMessage("Verifying OTP");
        dialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            FirebaseUser fcmUser = task.getResult().getUser();
                            if (fcmUser != null) {
                                verifyOtpFirebase(user.getUser_id(), fcmUser.getUid());
                            }
                        } else {
                            dialog.cancel();
                            new ToastMsg(OtpActivityNew.this).toastIconError("User details not found, Please try again!");
                        }
                    } else {
                        dialog.cancel();
                        if (task.getException() != null) {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                new ToastMsg(OtpActivityNew.this).toastIconError("The verification code entered was invalid");
                            } else {
                                new ToastMsg(OtpActivityNew.this).toastIconError(task.getException().getLocalizedMessage());
                            }
                        }
                    }
                });
    }


    private void initializeProgress() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP");
        dialog.setCancelable(false);
        dialog.show();
    }

    private void verifyOtpFirebase(String userId, String firebase_auth_token) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);

        Call<User> call = api.verifyOtpFirebase(userId, firebase_auth_token, AppConfig.API_KEY);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                dialog.cancel();
                if (response.code() == 200 && response.body() != null) {
                    user = response.body();
                    saveUserInfo();
                } else {
                    new ToastMsg(OtpActivityNew.this).toastIconError(getResources().getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                dialog.cancel();
                t.printStackTrace();
                new ToastMsg(OtpActivityNew.this).toastIconError(getResources().getString(R.string.something_went_wrong));
            }
        });
    }


    public void saveUserInfo() {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
        editor.putString("name", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString(Constants.USER_WALLET_AMOUNT, user.getWallet_amount());
        editor.putString("id", user.getUser_id());
        editor.putBoolean("status", true);
        editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
        editor.apply();

        DatabaseHelper db = new DatabaseHelper(OtpActivityNew.this);
        db.deleteUserData();
        db.insertUserData(user);
        updateSubscriptionStatus(user.getUser_id());
    }

    private void updateSubscriptionStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(@NonNull Call<ActiveStatus> call, @NonNull Response<ActiveStatus> response) {
                dialog.cancel();
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ActiveStatus activeStatus = response.body();

                        DatabaseHelper db = new DatabaseHelper(OtpActivityNew.this);
                        db.deleteAllActiveStatusData();
                        db.insertActiveStatusData(activeStatus);

                        Intent intent = new Intent(OtpActivityNew.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                        overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActiveStatus> call, @NonNull Throwable t) {
                dialog.cancel();
                t.printStackTrace();
                new ToastMsg(OtpActivityNew.this).toastIconError(getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }
}