package com.life.android;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PackageApi;
import com.life.android.network.apis.PaymentApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.Package;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization;
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType;
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz;
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SSLPayActivity extends AppCompatActivity implements SSLCTransactionResponseListener {
    private Package aPackage;
    private String amount = "";
    private String transId = "", selectedCountry = "", from = "";
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ssl_layout);

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        selectedCountry = sharedPreferences.getString("country", "");

        progress = findViewById(R.id.progress);
        aPackage = (Package) getIntent().getSerializableExtra("package");
        amount = aPackage.getPrice();
        transId = getIntent().getStringExtra("transId");
        from = getIntent().getStringExtra("from");


        if (selectedCountry.equals("")) {
            openSelectCountryDialog();
        } else {
            paymentStart();
        }
    }

    private void openSelectCountryDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_country);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Button acceptBt = dialog.findViewById(R.id.bt_accept);
        CardView cardOther = dialog.findViewById(R.id.cardOther);
        CardView cardbangladesh = dialog.findViewById(R.id.cardbangladesh);
        TextView tvCountry = dialog.findViewById(R.id.tvCountry);
        TextView tvBangla = dialog.findViewById(R.id.tvBangla);


        cardbangladesh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCountry = Constants.BANGLA;
                tvBangla.setTextColor(ContextCompat.getColor(SSLPayActivity.this, R.color.light_green_400));
                tvCountry.setTextColor(ContextCompat.getColor(SSLPayActivity.this, R.color.white));
            }
        });

        cardOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedCountry = Constants.OTHER_COUNTRY;
                tvCountry.setTextColor(ContextCompat.getColor(SSLPayActivity.this, R.color.light_green_400));
                tvBangla.setTextColor(ContextCompat.getColor(SSLPayActivity.this, R.color.white));
            }
        });

        acceptBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                editor.putString("country", selectedCountry);
                editor.apply();
                dialog.dismiss();
                paymentStart();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void paymentStart() {
        double amountDouble = Double.parseDouble(amount);
        if (selectedCountry.equals(Constants.BANGLA)) {
            final SSLCommerzInitialization sslCommerzInitialization = new SSLCommerzInitialization("dhupc617f9467b3ff4", "dhupc617f9467b3ff4@ssl", amountDouble, SSLCCurrencyType.BDT, transId, "purchasing", SSLCSdkType.TESTBOX);
            IntegrateSSLCommerz
                    .getInstance(SSLPayActivity.this)
                    .addSSLCommerzInitialization(sslCommerzInitialization)
                    .buildApiCall(SSLPayActivity.this);
        } else {
            double ttl = 0;
            double price = Double.parseDouble(aPackage.getUsdPrice());
            if (aPackage.getGstAmountInUsd() != null) {
                double gstAmomunt = Double.parseDouble(aPackage.getGstAmountInUsd());
                ttl = price + gstAmomunt;
            } else {
                ttl = price;
            }

            final SSLCommerzInitialization sslCommerzInitialization = new SSLCommerzInitialization("dhupc617f9467b3ff4", "dhupc617f9467b3ff4@ssl", ttl, SSLCCurrencyType.USD, transId, "purchasing", SSLCSdkType.TESTBOX);
            IntegrateSSLCommerz
                    .getInstance(SSLPayActivity.this)
                    .addSSLCommerzInitialization(sslCommerzInitialization)
                    .buildApiCall(SSLPayActivity.this);
        }
    }


    @Override
    public void transactionSuccess(SSLCTransactionInfoModel sslcTransactionInfoModel) {
        paySuccessApiCall(sslcTransactionInfoModel);
    }

    private void paySuccessApiCall(SSLCTransactionInfoModel model) {
        Gson gson = new Gson();
        String json = gson.toJson(model);
        progress.setVisibility(View.VISIBLE);
        HashMap<String, String> map = new HashMap<>();
        map.put("transaction_id", transId);
        map.put("ssl_commerz_payment_id", model.getTranId());
        if (from.equals(Constants.PAY_WATCH)) {
            map.put("video_id", aPackage.getPlanId());
        }
        map.put("transaction_details", json);
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.paySuccess(AppConfig.API_KEY, map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200 && response.body() != null) {
                    updateActiveStatus();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(SSLPayActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(@NotNull Call<ActiveStatus> call, @NotNull Response<ActiveStatus> response) {
                if (response.code() == 200 && response.body() != null) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    new ToastMsg(SSLPayActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ActiveStatus> call, @NotNull Throwable t) {
                t.printStackTrace();
                new ToastMsg(SSLPayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
            }
        });
    }

    @Override
    public void transactionFail(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void merchantValidationError(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}