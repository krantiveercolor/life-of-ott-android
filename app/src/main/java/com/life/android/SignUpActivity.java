package com.life.android;

import static com.life.android.helper.CMExtenstionKt.hideKeyboard;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.FirebaseAuthApi;
import com.life.android.network.apis.SignUpApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.User;
import com.life.android.utils.Constants;
import com.life.android.utils.CustomEditText;
import com.life.android.utils.RtlUtils;
import com.life.android.utils.ToastMsg;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.yesterselga.countrypicker.CountryPicker;
import com.yesterselga.countrypicker.Theme;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "SignUpActivity";
    private static int RC_PHONE_SIGN_IN = 1231;
    private static int RC_FACEBOOK_SIGN_IN = 1241;
    private static int RC_GOOGLE_SIGN_IN = 1251;

    private TextInputEditText etName, etEmail, etMobile;
    private CustomEditText etPass;

    private TextView countryCodeText;

    private TextView btnSignup;
    private ProgressDialog dialog;
    private View backgorundView;
    private TextView tvLogin;
    private ImageView phoneAuthButton, facebookAuthButton, googleAuthButton;
    FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "sign_up_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etPass = findViewById(R.id.password);
        etMobile = findViewById(R.id.mobile);
        countryCodeText = findViewById(R.id.countryCodeEditText);

        btnSignup = findViewById(R.id.signup);
        backgorundView = findViewById(R.id.background_view);
        progressBar = findViewById(R.id.progress_bar);
        phoneAuthButton = findViewById(R.id.phoneAuthButton);
        facebookAuthButton = findViewById(R.id.facebookAuthButton);
        googleAuthButton = findViewById(R.id.googleAuthButton);
        tvLogin = findViewById(R.id.login);

        btnSignup.setOnClickListener(v -> {
            hideKeyboard(v);
            if (etEmail.getText() == null || !isValidEmailAddress(etEmail.getText().toString())) {
                new ToastMsg(SignUpActivity.this).toastIconError("please enter valid email");
            } else if (etPass.getText() == null || etPass.getText().toString().equals("")) {
                new ToastMsg(SignUpActivity.this).toastIconError("please enter password");
            } else if (etName.getText() == null || etName.getText().toString().equals("")) {
                new ToastMsg(SignUpActivity.this).toastIconError("please enter name");
            } else {
                signUp();
            }
        });

        countryCodeText.setOnClickListener(v -> {
            hideKeyboard(v);
            openCountryCodeDialog();
        });

        //social login button
        if (AppConfig.ENABLE_FACEBOOK_LOGIN) {
            facebookAuthButton.setVisibility(View.VISIBLE);
        }
        if (AppConfig.ENABLE_GOOGLE_LOGIN) {
            googleAuthButton.setVisibility(View.VISIBLE);
        }
        if (AppConfig.ENABLE_PHONE_LOGIN) {
            phoneAuthButton.setVisibility(View.VISIBLE);
        }

        firebaseAuth = FirebaseAuth.getInstance();

        phoneAuthButton.setOnClickListener(v -> phoneSignIn());

        facebookAuthButton.setOnClickListener(v -> facebookSignIn());

        googleAuthButton.setOnClickListener(v -> googleSignIn());

        tvLogin.setOnClickListener(v -> {
            hideKeyboard(v);
            onBackPressed();
        });
    }

    public void openCountryCodeDialog() {
        CountryPicker picker = CountryPicker.newInstance("Select Country", Theme.DARK);
        picker.setListener((name, code, dialCode, flagDrawableResID) -> {
            countryCodeText.setText(String.format("%s %s", code, dialCode));
            picker.dismiss();
        });
        picker.show(getSupportFragmentManager(), "Select Country");
    }

    private void signUp() {
        dialog.show();
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SignUpApi signUpApi = retrofit.create(SignUpApi.class);
        HashMap<String, String> params = new HashMap<>();
        if (etEmail.getText() != null) {
            params.put("email", etEmail.getText().toString());
        }
        if (etPass.getText() != null) {
            params.put("password", etPass.getText().toString());
        }
        if (etName.getText() != null) {
            params.put("name", etName.getText().toString());
        }
        if (etMobile.getText() != null && !etMobile.getText().toString().isEmpty()) {
            params.put("phone", etMobile.getText().toString());
        }
        if (etMobile.getText() != null && !etMobile.getText().toString().isEmpty()) {
            params.put("phone", etMobile.getText().toString());
        }
        if (countryCodeText.getText() != null) {
            String numberOnly = countryCodeText.getText().toString().replaceAll("[^0-9]", "");
            params.put("country_code", "+" + numberOnly);
        }

        //TODO - NEED TO REMOVE IT ON MONDAY
        params.put("device_type", "ios");

        Call<User> call = signUpApi.signUp(AppConfig.API_KEY, params);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                dialog.cancel();
                if (response.body() != null) {
                    User user = response.body();
                    if (user.getStatus().equals("success")) {
                        /*if (etMobile.getText() == null || etMobile.getText().toString().isEmpty() || user.getMobile_verified().equalsIgnoreCase("1")) {
                            saveUserInfo(user);
                        } else {
                            Intent intent = new Intent(SignUpActivity.this, PlainActivity.class);
                            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.otp);
                            intent.putExtra(IConstants.IntentString.payload, user);
                            startActivity(intent);
                        }*/
                        saveUserInfo(user);
                    } else if (user.getStatus().equals("error")) {
                        new ToastMsg(SignUpActivity.this).toastIconError(user.getData());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                new ToastMsg(SignUpActivity.this).toastIconError("Something went wrong." + t.getMessage());
                t.printStackTrace();
                dialog.cancel();
            }
        });
    }

    public void saveUserInfo(User user) {
        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
        editor.putString("name", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString(Constants.USER_WALLET_AMOUNT, user.getWallet_amount());
        editor.putString("id", user.getUser_id());
        editor.putBoolean("status", true);
        editor.putBoolean(Constants.USER_LOGIN_STATUS, true);
        editor.apply();

        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
        db.deleteUserData();
        db.insertUserData(user);
        updateSubscriptionStatus(user.getUser_id());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }


    private void updateSubscriptionStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ActiveStatus activeStatus = response.body();

                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
                        db.deleteAllActiveStatusData();
                        db.insertActiveStatusData(activeStatus);
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                        finishAffinity();
                        dialog.cancel();
                    }
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(SignUpActivity.this).toastIconError(getResources().getString(R.string.something_went_wrong));
            }
        });
    }

    /*social login related task*/

    private void phoneSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                final String phoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                //already signed in
                if (!phoneNumber.isEmpty())
                    sendDataToServer();
            }

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.PhoneBuilder().build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_PHONE_SIGN_IN);
        }
    }

    private void facebookSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null) {
            if (!FirebaseAuth.getInstance().getCurrentUser().getUid().isEmpty()) {
                //already signed in
                //send data to server
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                sendFacebookDataToServer(user.getDisplayName(), String.valueOf(user.getPhotoUrl()), user.getEmail());

            }

        } else {
            progressBar.setVisibility(View.GONE);
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.FacebookBuilder()
                            //.setPermissions(Arrays.asList("email", "default"))
                            .build());
            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_FACEBOOK_SIGN_IN);
        }
    }

    private void googleSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            sendGoogleDataToServer();

        } else {
            progressBar.setVisibility(View.VISIBLE);
            // Choose authentication providers
            GoogleSignInOptions googleOptions = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build();


            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().setSignInOptions(googleOptions).build());

           /* List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());*/

            // Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_GOOGLE_SIGN_IN);
        }
    }

    private void sendDataToServer() {
        progressBar.setVisibility(View.VISIBLE);
        String phoneNo = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.getPhoneAuthStatus(AppConfig.API_KEY, uid, phoneNo);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {

                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.putString(Constants.USER_WALLET_AMOUNT, user.getWallet_amount());
                        preferences.apply();
                        updateSubscriptionStatus(user.getUser_id());
                    }

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                phoneSignIn();
            }
        });


    }

    private void sendGoogleDataToServer() {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = user.getDisplayName();
        String email = user.getEmail();
        Uri image = user.getPhotoUrl();
        String uid = user.getUid();
        String phone = "";
        if (user.getPhoneNumber() != null)
            phone = user.getPhoneNumber();

        Log.d(TAG, "onActivityResult: " + user.getEmail());
        Log.d(TAG, "onActivityResult: " + user.getDisplayName());
        Log.d(TAG, "onActivityResult: " + user.getPhoneNumber());

        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.getGoogleAuthStatus(AppConfig.API_KEY, uid, email, username, image, phone);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();

                        //save user login time, expire time
                        updateSubscriptionStatus(user.getUser_id());
                    }

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                googleSignIn();
            }
        });
    }

    private void sendFacebookDataToServer(String username, String photoUrl, String email) {
        progressBar.setVisibility(View.VISIBLE);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FirebaseAuthApi api = retrofit.create(FirebaseAuthApi.class);
        Call<User> call = api.getFacebookAuthStatus(AppConfig.API_KEY, uid, username, email, Uri.parse(photoUrl));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equals("success")) {

                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(SignUpActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        updateSubscriptionStatus(user.getUser_id());
                    }

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SignUpActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                facebookSignIn();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHONE_SIGN_IN) {

            final IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getPhoneNumber().isEmpty()) {
                    sendDataToServer();
                } else {
                    //empty
                    phoneSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }

            }
        } else if (requestCode == RC_FACEBOOK_SIGN_IN) {
            final IdpResponse response = com.firebase.ui.auth.IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getUid().isEmpty()) {
                    String username = user.getDisplayName();
                    String photoUrl = String.valueOf(user.getPhotoUrl());
                    String email = user.getEmail();

                    sendFacebookDataToServer(username, photoUrl, email);

                } else {
                    //empty
                    facebookSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        } else if (requestCode == RC_GOOGLE_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (!user.getUid().isEmpty()) {
                    sendGoogleDataToServer();

                } else {
                    //empty
                    googleSignIn();
                }
            } else {
                // sign in failed
                if (response == null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(this, "Error !!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }


}
