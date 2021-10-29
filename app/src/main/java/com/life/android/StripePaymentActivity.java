package com.life.android;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PaymentApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.Package;
import com.life.android.network.model.config.PaymentConfig;
import com.life.android.utils.ApiResources;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;
import com.google.android.material.textfield.TextInputEditText;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StripePaymentActivity extends AppCompatActivity {
    private static final String TAG = StripePaymentActivity.class.getSimpleName();

    private Package aPackage;
    private Toolbar mToolbar;
    private TextInputEditText mCardNoEt, mValidDateEt, mCvvNoEt, mCardNameEt;
    private Button mSubmitBt;
    private ProgressBar progressBar;
    private CardInputWidget cardInputWidget;

    private Calendar myCalendar = Calendar.getInstance();
    private int month, year;
    private String userId;
    private String secretKey, publisherKey;
    private boolean isDark;
    private DatabaseHelper databaseHelper;
    private String from = "";
    private String currency = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        setContentView(R.layout.activity_stripe_payment);
        databaseHelper = new DatabaseHelper(StripePaymentActivity.this);
        aPackage = (Package) getIntent().getSerializableExtra("package");
        from = getIntent().getStringExtra("from");

        intiView();

        userId = PreferenceUtils.getUserId(StripePaymentActivity.this);

        PaymentConfig paymentConfig = databaseHelper.getConfigurationData().getPaymentConfig();
        secretKey = paymentConfig.getStripeSecretKey();
        publisherKey = paymentConfig.getStripePublishableKey();

        if (isDark) {
            mToolbar.setBackgroundColor(getResources().getColor(R.color.black_window_light));
            mSubmitBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
            cardInputWidget.setBackground(getResources().getDrawable(R.drawable.rounded_black_transparent));
        }

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Payment For \"" + aPackage.getName() + "\"");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final DatePickerDialog.OnDateSetListener date = new
                DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateLabel();
                    }

                };

        mValidDateEt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new DatePickerDialog(StripePaymentActivity.this, date, myCalendar
                            .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                            myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                }
                return true;
            }
        });

        // getting currency symbol
        PaymentConfig config = new DatabaseHelper(this).getConfigurationData().getPaymentConfig();
        currency = config.getCurrencySymbol();
    }

    private void updateLabel() {

        year = myCalendar.get(Calendar.YEAR);
        month = myCalendar.get(Calendar.MONTH);

        String myFormat = "MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        mValidDateEt.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        mSubmitBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardInputWidget mCardInputWidget = findViewById(R.id.card_iw);
                Card card = mCardInputWidget.getCard();
                if (card != null) {
                    if (!card.validateCard()) {
                        new ToastMsg(StripePaymentActivity.this).toastIconError(getResources().getString(R.string.invalid_card));
                        return;
                    }
                } else {
                    new ToastMsg(StripePaymentActivity.this).toastIconError(getResources().getString(R.string.invalid_card));
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                mSubmitBt.setVisibility(View.GONE);
                Stripe stripe = new Stripe(StripePaymentActivity.this, publisherKey);

                stripe.createCardToken(
                        card, new ApiResultCallback<Token>() {
                            @Override
                            public void onSuccess(@NonNull Token result) {
                                createCharge(result.getId());
                            }

                            @Override
                            public void onError(@NonNull Exception e) {
                                Toast.makeText(StripePaymentActivity.this, e.getMessage(), Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                );

            }
        });

    }

    private void createCharge(final String id) {

        com.stripe.Stripe.apiKey = secretKey;

        // Toast.makeText(this, "hello", Toast.LENGTH_LONG).show();
        Double amount = (Double.valueOf(aPackage.getPrice()) * 100);
        final Map<String, Object> params = new HashMap<>();
        params.put("amount", String.valueOf(amount.intValue()));
        params.put("currency", ApiResources.CURRENCY);
        params.put("description", aPackage.getName());
        params.put("source", id);

        //new ChargeAsyncTask(params).execute();

        Thread thread = new Thread(() -> {
            try {
                Charge charge = null;
                try {
                    charge = Charge.create(params);
                    Log.d("charge", charge.toJson());

                    // save the data to the server
                    if (from.equalsIgnoreCase(Constants.WALLET)) {
                        addMoneyToWalletApiCall(charge, id);
                    } else
                        saveChargeData(charge, id);

                } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    private void intiView() {
        mToolbar = findViewById(R.id.payment_toolbar);
        mCardNoEt = findViewById(R.id.card_no_et);
        mCardNameEt = findViewById(R.id.card_name_et);
        mValidDateEt = findViewById(R.id.valid_date_et);
        mCvvNoEt = findViewById(R.id.cvv_no_et);
        mSubmitBt = findViewById(R.id.submit_bt);
        progressBar = findViewById(R.id.progress_bar);
        cardInputWidget = findViewById(R.id.card_iw);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public class ChargeAsyncTask extends AsyncTask<Void, Void, Void> {
        private Charge charge;
        private Map<String, Object> params;

        public ChargeAsyncTask(Map<String, Object> params) {
            this.params = params;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                charge = Charge.create(params);
            } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (from.equalsIgnoreCase(Constants.WALLET)) {
                addMoneyToWalletApiCall(charge, params.get("source").toString());
            } else
                saveChargeData(charge, params.get("source").toString());
        }
    }

    private void addMoneyToWalletApiCall(Charge charge, String token) {
        progressBar.setVisibility(View.VISIBLE);
        final String userId = PreferenceUtils.getUserId(this);
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("user_id", userId);
        hashMap.put("amount", String.valueOf(charge.getAmount()));
        hashMap.put("payment_info", token);
        hashMap.put("transaction_id", token);
        hashMap.put("currency", currency);
        hashMap.put("payment_method", "Stripe");
        hashMap.put("paid_with", Constants.OTHER);
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
                    new ToastMsg(StripePaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                new ToastMsg(StripePaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                t.printStackTrace();
            }

        });

    }

    public void saveChargeData(Charge charge, String token) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(from.equalsIgnoreCase(Constants.PAY_WATCH) ? "videos_id" : "plan_id", aPackage.getPlanId());
        hashMap.put("user_id", userId);
        hashMap.put("paid_amount", String.valueOf(charge.getAmount()));
        hashMap.put("payment_info", token);
        hashMap.put("payment_method", "Stripe");
        hashMap.put("paid_with", Constants.OTHER);

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
                    updateActiveStatus();
                } else {
                    new ToastMsg(StripePaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                t.printStackTrace();
                new ToastMsg(StripePaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
            }
        });
    }

    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(StripePaymentActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    progressBar.setVisibility(View.GONE);
                    new ToastMsg(StripePaymentActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                    Intent intent = getIntent();
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }


}
