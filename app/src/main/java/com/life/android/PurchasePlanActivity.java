package com.life.android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.adapters.PackageAdapter;
import com.life.android.bottomshit.PaymentBottomShitDialog;
import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PackageApi;
import com.life.android.network.apis.PaymentApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.ActiveSubscription;
import com.life.android.network.model.AllPackage;
import com.life.android.network.model.Package;
import com.life.android.network.model.config.PaymentConfig;
import com.life.android.utils.ApiResources;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.RtlUtils;
import com.life.android.utils.ToastMsg;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PurchasePlanActivity extends AppCompatActivity implements PackageAdapter.OnItemClickListener, PaymentBottomShitDialog.OnBottomShitClickListener {

    private static final String TAG = PurchasePlanActivity.class.getSimpleName();
    private static final int PAYPAL_REQUEST_CODE = 100;
    private TextView noTv;
    private ProgressBar progressBar;
    private ImageView closeIv;
    private RecyclerView packageRv;
    private List<Package> packages = new ArrayList<>();
    private List<ImageView> imageViews = new ArrayList<>();
    private String currency = "";
    private String exchangeRate;
    private boolean isDark;

    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(ApiResources.PAYPAL_CLIENT_ID);
    private Package packageItem;
    private PaymentBottomShitDialog paymentBottomShitDialog;
    private ProgressDialog dialog;
    private ActiveSubscription activeSubscription;
    private TextView noteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);
        activeSubscription = getIntent().getParcelableExtra("active_sub");
        /*if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }*/

        setContentView(R.layout.activity_purchase_plan);

        initView();

        // ---------- start paypal service ----------
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);

        // getting currency symbol
        PaymentConfig config = new DatabaseHelper(PurchasePlanActivity.this).getConfigurationData().getPaymentConfig();
        currency = config.getCurrencySymbol();
        exchangeRate = config.getExchangeRate();
        packageRv.setHasFixedSize(true);
        packageRv.setLayoutManager(new LinearLayoutManager(this));

        getPurchasePlanInfo();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Processing payment, please wait!!");
        dialog.setCancelable(false);
    }

    private void getPurchasePlanInfo() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        final PackageApi packageApi = retrofit.create(PackageApi.class);
        Call<AllPackage> call = packageApi.getAllPackage(AppConfig.API_KEY);
        call.enqueue(new Callback<AllPackage>() {
            @Override
            public void onResponse(Call<AllPackage> call, Response<AllPackage> response) {
                AllPackage allPackage = response.body();
                packages = allPackage.getPackage();
                if (allPackage.getPackage().size() > 0) {
                    noTv.setVisibility(View.GONE);
                    PackageAdapter adapter = new PackageAdapter(PurchasePlanActivity.this, allPackage.getPackage(), currency);
                    adapter.setItemClickListener(PurchasePlanActivity.this);
                    packageRv.setAdapter(adapter);
                } else {
                    noTv.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<AllPackage> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetails = confirmation.toJSONObject().toString(4);
                        completePayment(paymentDetails);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    new ToastMsg(this).toastIconError("Cancel");
                }
            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            new ToastMsg(this).toastIconError("Invalid");
        } else if (requestCode == 1001 && resultCode == RESULT_OK) {
            PreferenceUtils.updateWalletAmountFromServer(this);
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void completePayment(String paymentDetails) {
        try {
            JSONObject jsonObject = new JSONObject(paymentDetails);
            sendDataToServer(jsonObject.getJSONObject("response"), "Paypal");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendDataToServer(JSONObject response, String paidWith) {
        dialog.show();
        try {
            String payId = paidWith.equalsIgnoreCase(Constants.WALLET) ? "" : response.getString("id");
            final String userId = PreferenceUtils.getUserId(PurchasePlanActivity.this);
            Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("plan_id", packageItem.getPlanId());
            hashMap.put("user_id", userId);
            hashMap.put("paid_amount", packageItem.getPrice());
            hashMap.put("payment_info", payId);
            hashMap.put("payment_method", paidWith);
            hashMap.put("paid_with", paidWith);
            hashMap.put("type", Constants.SUBSCRIPTION);
            hashMap.put("device_type", Constants.device_type);
            Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, hashMap);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        updateActiveStatus(userId);
                    } else {
                        new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                    dialog.cancel();
                    new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    t.printStackTrace();
                    Log.e("PAYMENT", "error: " + t.getLocalizedMessage());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateActiveStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activiStatus = response.body();
                    saveActiveStatus(activiStatus);
                } else {
                    dialog.cancel();
                    new ToastMsg(PurchasePlanActivity.this).toastIconError("Payment info not save to the own server. something went wrong.");
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                dialog.cancel();
                new ToastMsg(PurchasePlanActivity.this).toastIconError(t.getMessage());
                t.printStackTrace();
            }
        });

    }

    private void saveActiveStatus(ActiveStatus activeStatus) {
        DatabaseHelper db = new DatabaseHelper(PurchasePlanActivity.this);
        db.deleteAllActiveStatusData();
        db.insertActiveStatusData(activeStatus);
        dialog.hide();
        PreferenceUtils.updateWalletAmountFromServer(this);
        new ToastMsg(PurchasePlanActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
        Intent intent = getIntent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        super.onDestroy();
    }

    private void processPaypalPayment(Package packageItem) {
        String[] paypalAcceptedList = getResources().getStringArray(R.array.paypal_currency_list);
        if (Arrays.asList(paypalAcceptedList).contains(ApiResources.CURRENCY)) {
            PayPalPayment payPalPayment = new PayPalPayment((new BigDecimal(String.valueOf(packageItem.getPrice()))),
                    ApiResources.CURRENCY,
                    "Payment for Package",
                    PayPalPayment.PAYMENT_INTENT_SALE);

            Log.e("Payment", "currency: " + ApiResources.CURRENCY);
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        } else {
            PaymentConfig paymentConfig = new DatabaseHelper(PurchasePlanActivity.this).getConfigurationData().getPaymentConfig();
            double exchangeRate = Double.parseDouble(paymentConfig.getExchangeRate());
            double price = Double.parseDouble(packageItem.getPrice());
            double priceInUSD = (double) price / exchangeRate;
            PayPalPayment payPalPayment = new PayPalPayment((new BigDecimal(String.valueOf(priceInUSD))),
                    "USD",
                    "Payment for Package",
                    PayPalPayment.PAYMENT_INTENT_SALE);
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        }
    }

    private void initView() {
        noTv = findViewById(R.id.no_tv);
        progressBar = findViewById(R.id.progress_bar);
        packageRv = findViewById(R.id.pacakge_rv);
        closeIv = findViewById(R.id.close_iv);
        noteView = findViewById(R.id.package_note_view);
        if (activeSubscription != null) {
            noteView.setVisibility(View.VISIBLE);
            noteView.setText(String.format("Note : Your new package will be continued after the current package expiry date i.e.,%s", activeSubscription.getExpireDate()));
        }
    }

    @Override
    public void onItemClick(Package pac) {
        packageItem = pac;
        paymentBottomShitDialog = new PaymentBottomShitDialog();
        paymentBottomShitDialog.setOnBottomShitClickListener(this);
        paymentBottomShitDialog.enableWallet = true;
        paymentBottomShitDialog.show(getSupportFragmentManager(), "PaymentBottomShitDialog");
    }

    @Override
    public void onBottomShitClick(String paymentMethodName) {
        if (paymentMethodName.equals(PaymentBottomShitDialog.PAYPAL)) {
            processPaypalPayment(packageItem);
        } else if (paymentMethodName.equals(PaymentBottomShitDialog.STRIP)) {
            Intent intent = new Intent(PurchasePlanActivity.this, StripePaymentActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            intent.putExtra("from", Constants.SUBSCRIPTION);
            startActivityForResult(intent, 1001);
        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.RAZOR_PAY)) {
            Intent intent = new Intent(PurchasePlanActivity.this, SSLPayActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            intent.putExtra("from", Constants.SUBSCRIPTION);
            startActivityForResult(intent, 1001);
            /*Intent intent = new Intent(PurchasePlanActivity.this, RazorPayActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            intent.putExtra("from", Constants.SUBSCRIPTION);
            startActivityForResult(intent, 1001);*/
        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.OFFLINE_PAY)) {
            //show an alert dialog
            showOfflinePaymentDialog();
        } else if (paymentMethodName.equalsIgnoreCase(Constants.WALLET)) {
            float planPrice = Float.parseFloat(packageItem.getPrice());
            float walletAmount = Float.parseFloat(PreferenceUtils.getWalletAmount(this));
            if (planPrice <= walletAmount) {
                sendDataToServer(new JSONObject(), Constants.WALLET);
            } else
                new ToastMsg(this).toastIconError("Your wallet amount " + currency + walletAmount + " was less than plan price");
        }
    }

    private void showOfflinePaymentDialog() {
        DatabaseHelper helper = new DatabaseHelper(this);
        PaymentConfig paymentConfig = helper.getConfigurationData().getPaymentConfig();
        new MaterialAlertDialogBuilder(this)
                .setTitle(paymentConfig.getOfflinePaymentTitle())
                .setMessage(paymentConfig.getOfflinePaymentInstruction())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }
}

