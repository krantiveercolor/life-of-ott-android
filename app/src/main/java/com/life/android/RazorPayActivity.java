package com.life.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PaymentApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.APIResponse;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.CreatePaymentModel;
import com.life.android.network.model.Package;
import com.life.android.network.model.User;
import com.life.android.network.model.config.PaymentConfig;
import com.life.android.utils.ApiResources;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCCustomerInfoInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCProductInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCShipmentInfoInitializer;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RazorPayActivity extends AppCompatActivity implements PaymentResultWithDataListener {
    private static final String TAG = "RazorPayActivity";
    private Package aPackage;
    private DatabaseHelper databaseHelper;
    private ProgressBar progressBar;
    private String from = "";
    private String currency = "";
    private String razor_pay_order_id = null;
    private String local_transaction_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_razor_pay);
        progressBar = findViewById(R.id.progress_bar);
        aPackage = (Package) getIntent().getSerializableExtra("package");
        from = getIntent().getStringExtra("from");
        databaseHelper = new DatabaseHelper(this);

        createPaymentRequest();
        PaymentConfig config = new DatabaseHelper(this).getConfigurationData().getPaymentConfig();
        currency = config.getCurrencySymbol();
    }

    public void startPayment() {
        PaymentConfig config = databaseHelper.getConfigurationData().getPaymentConfig();
        User user = databaseHelper.getUserData();

        final Activity activity = this;
        Checkout checkout = new Checkout();
        checkout.setKeyID(config.getRazorpayKeyId());
        checkout.setImage(R.drawable.life_icon);

        JSONObject options = new JSONObject();
        try {
            options.put("name", getString(R.string.app_name));
            options.put("description", aPackage.getName());
            options.put("currency", "INR");
            if (razor_pay_order_id != null) {
                options.put("order_id", razor_pay_order_id);
            }
            options.put("amount", currencyConvert(config.getCurrency(), aPackage.getPrice(), ApiResources.RAZORPAY_EXCHANGE_RATE));
            JSONObject prefill = new JSONObject();
            prefill.put("email", user.getEmail());
            options.put("prefill", prefill);
            checkout.open(activity, options);
        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        if (from.equalsIgnoreCase(Constants.WALLET)) {
            addMoneyToWalletApiCall(s, paymentData);
        } else {
            saveChargeData(s, paymentData);
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }


    private void createPaymentRequest() {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        HashMap<String, String> hashMap = new HashMap<>();
        if (aPackage.getPlanId() != null) {
            hashMap.put(from.equalsIgnoreCase(Constants.PAY_WATCH) ? "videos_id" : "plan_id", aPackage.getPlanId());
        }
        hashMap.put("user_id", databaseHelper.getUserData().getUser_id());
        hashMap.put("paid_amount", aPackage.getPrice());
        hashMap.put("payment_method", "RazorPay");

        if (getIntent().hasExtra("preBook") && getIntent().getStringExtra("preBook").equals("true")) {
            hashMap.put("type", Constants.pre_booking);
        } else {
            hashMap.put("type", from);
        }
        hashMap.put("device_type", Constants.device_type);

        Call<APIResponse<CreatePaymentModel>> call = paymentApi.createPaymentRequest(AppConfig.API_KEY, hashMap);
        call.enqueue(new Callback<APIResponse<CreatePaymentModel>>() {
            @Override
            public void onResponse(@NotNull Call<APIResponse<CreatePaymentModel>> call, @NotNull Response<APIResponse<CreatePaymentModel>> response) {
                if (response.code() == 200 && response.body() != null && response.body().status.equalsIgnoreCase("success")) {
                    razor_pay_order_id = response.body().data.razor_pay_order_id;
                    local_transaction_id = response.body().data.local_transaction_id;
                    startPayment();
                } else {
                    new ToastMsg(RazorPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    finish();
                    overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<APIResponse<CreatePaymentModel>> call, @NotNull Throwable t) {
                t.printStackTrace();
                new ToastMsg(RazorPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void addMoneyToWalletApiCall(String token, PaymentData paymentData) {
        progressBar.setVisibility(View.VISIBLE);
        final String userId = PreferenceUtils.getUserId(this);
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("user_id", userId);
        hashMap.put("amount", aPackage.getPrice());
        hashMap.put("payment_info", paymentData.getData().toString());
        hashMap.put("transaction_id", token);
        hashMap.put("currency", currency);
        hashMap.put("payment_method", "RazorPay");
        hashMap.put("paid_with", Constants.OTHER);
        hashMap.put("device_type", Constants.device_type);
        if (razor_pay_order_id != null) {
            hashMap.put("razor_pay_order_id", razor_pay_order_id);
        }
        if (local_transaction_id != null) {
            hashMap.put("local_transaction_id", local_transaction_id);
        }
        Call<ResponseBody> call = paymentApi.addMoneyToWallet(AppConfig.API_KEY, hashMap);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.code() == 200) {
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    new ToastMsg(RazorPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                new ToastMsg(RazorPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                t.printStackTrace();
            }
        });
    }


    public void saveChargeData(String token, PaymentData paymentData) {
        progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        HashMap<String, String> hashMap = new HashMap<>();

        if (aPackage.getPlanId() != null) {
            hashMap.put(from.equalsIgnoreCase(Constants.PAY_WATCH) ? "videos_id" : "plan_id", aPackage.getPlanId());
        }
        hashMap.put("user_id", databaseHelper.getUserData().getUser_id());
        hashMap.put("paid_amount", aPackage.getPrice());
        hashMap.put("payment_info", paymentData.getData().toString());
        hashMap.put("transaction_id", token);
        hashMap.put("payment_method", "RazorPay");
        hashMap.put("paid_with", Constants.OTHER);

        if (razor_pay_order_id != null) {
            hashMap.put("razor_pay_order_id", razor_pay_order_id);
        }
        if (local_transaction_id != null) {
            hashMap.put("local_transaction_id", local_transaction_id);
        }

        if (getIntent().hasExtra("preBook") && getIntent().getStringExtra("preBook").equals("true")) {
            hashMap.put("type", Constants.pre_booking);
        } else {
            hashMap.put("type", from);
        }
        hashMap.put("device_type", Constants.device_type);

        Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, hashMap);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        updateActiveStatus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    new ToastMsg(RazorPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    finish();
                    progressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                t.printStackTrace();
                new ToastMsg(RazorPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(RazorPayActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(@NotNull Call<ActiveStatus> call, @NotNull Response<ActiveStatus> response) {
                if (response.code() == 200 && response.body() != null) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    new ToastMsg(RazorPayActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NotNull Call<ActiveStatus> call, @NotNull Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
                new ToastMsg(RazorPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
            }
        });

    }

    private String currencyConvert(String currency, String value, String exchangeRate) {
        double temp;
        if (currency.equalsIgnoreCase("INR")) {
            temp = Double.parseDouble(value);
        } else {
            temp = Double.parseDouble(value) * Double.parseDouble(exchangeRate);
        }
        return String.valueOf(temp * 100);
    }


}
