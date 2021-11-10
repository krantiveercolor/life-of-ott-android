package com.life.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.Package;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;
import com.sslwireless.sslcommerzlibrary.model.initializer.SSLCommerzInitialization;
import com.sslwireless.sslcommerzlibrary.model.response.SSLCTransactionInfoModel;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCCurrencyType;
import com.sslwireless.sslcommerzlibrary.model.util.SSLCSdkType;
import com.sslwireless.sslcommerzlibrary.view.singleton.IntegrateSSLCommerz;
import com.sslwireless.sslcommerzlibrary.viewmodel.listener.SSLCTransactionResponseListener;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SSLPayActivity extends AppCompatActivity implements SSLCTransactionResponseListener {
    private Package aPackage;
    private String amount = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ssl_layout);

        aPackage = (Package) getIntent().getSerializableExtra("package");
        amount = aPackage.getPrice();
        double amountDouble = Double.parseDouble(amount);


        final SSLCommerzInitialization sslCommerzInitialization = new SSLCommerzInitialization("dhupc617f9467b3ff4", "dhupc617f9467b3ff4@ssl", amountDouble, SSLCCurrencyType.BDT, "123456789098765", "yourProductType", SSLCSdkType.TESTBOX);
        /*final SSLCCustomerInfoInitializer customerInfoInitializer = new SSLCCustomerInfoInitializer("customer name", "customer email",
                "address", "dhaka", "1214", "Bangladesh", "phoneNumber");
        final SSLCProductInitializer productInitializer = new SSLCProductInitializer("food", "food",
                new SSLCProductInitializer.ProductProfile.TravelVertical("Travel", "10",
                        "A", "12", "Dhk-S"));
        final SSLCShipmentInfoInitializer shipmentInfoInitializer = new SSLCShipmentInfoInitializer("Courier",
                2, new SSLCShipmentInfoInitializer.ShipmentDetails("AA", "Address 1",
                "Dhaka", "1000", "BD"));*/
        IntegrateSSLCommerz
                .getInstance(SSLPayActivity.this)
                .addSSLCommerzInitialization(sslCommerzInitialization)
                .buildApiCall(SSLPayActivity.this);

    }


    @Override
    public void transactionSuccess(SSLCTransactionInfoModel sslcTransactionInfoModel) {
        String trans = sslcTransactionInfoModel.getTranId();
        sslcTransactionInfoModel.getAPIConnect();
        Toast.makeText(this, trans, Toast.LENGTH_SHORT).show();
        updateActiveStatus();

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
    }

    @Override
    public void merchantValidationError(String s) {

    }
}