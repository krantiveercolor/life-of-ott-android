package com.life.android.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.life.android.AppConfig;
import com.life.android.PlainActivity;
import com.life.android.R;
import com.life.android.RazorPayActivity;
import com.life.android.StripePaymentActivity;
import com.life.android.adapters.AddCashAdapter;
import com.life.android.bottomshit.PaymentBottomShitDialog;
import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PaymentApi;
import com.life.android.network.model.Package;
import com.life.android.network.model.config.PaymentConfig;
import com.life.android.utils.ApiResources;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
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
import java.util.Arrays;
import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.app.Activity.RESULT_OK;

public class AddCashFragment extends Fragment implements AddCashAdapter.AddCashAdapterListener, PaymentBottomShitDialog.OnBottomShitClickListener {

    PlainActivity plainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_cash, container, false);
    }

    private RecyclerView addCashRecyclerView;
    private EditText addCashAmountEdittext;
    private final int[] cashArray = {50, 100, 500, 1000, 2000, 5000};
    private String amount = "";
    private PaymentBottomShitDialog paymentBottomShitDialog;
    private final PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(ApiResources.PAYPAL_CLIENT_ID);
    private static final int PAYPAL_REQUEST_CODE = 100;
    private final Package packageItem = new Package();
    private String currency = "";

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        plainActivity = ((PlainActivity) requireActivity());
        plainActivity.activityIndicator(false);

        addCashAmountEdittext = view.findViewById(R.id.add_cash_amount_edittext);
        addCashRecyclerView = view.findViewById(R.id.add_cash_recycler_view);

        view.findViewById(R.id.add_cash_btn).setOnClickListener(view1 -> {
            amount = addCashAmountEdittext.getText().toString();
            if (amount.isEmpty()) {
                new ToastMsg(requireContext()).toastIconError("Please enter amount");
            } else {
                packageItem.setName("Adding money to wallet");
                packageItem.setPrice(amount);
                paymentBottomShitDialog = new PaymentBottomShitDialog();
                paymentBottomShitDialog.setOnBottomShitClickListener(this);
                paymentBottomShitDialog.show(requireActivity().getSupportFragmentManager(), paymentBottomShitDialog.getTag());
            }
        });

        createAmountsRecyclerView();

        // getting currency symbol
        PaymentConfig config = new DatabaseHelper(requireContext()).getConfigurationData().getPaymentConfig();
        currency = config.getCurrencySymbol();

    }

    private void createAmountsRecyclerView() {
        AddCashAdapter addCashAdapter = new AddCashAdapter(requireContext(), cashArray, currency);
        addCashAdapter.setAddCashAdapterListener(this);
        addCashRecyclerView.setAdapter(addCashAdapter);
        onAddCashItemClick(0);
    }

    @Override
    public void onAddCashItemClick(int pos) {
        addCashAmountEdittext.setText(String.valueOf(cashArray[pos]));
        addCashAmountEdittext.setSelection(addCashAmountEdittext.getText().length());
    }

    @Override
    public void onBottomShitClick(String paymentMethodName) {
        if (paymentMethodName.equals(PaymentBottomShitDialog.PAYPAL)) {
            processPaypalPayment();
        } else if (paymentMethodName.equals(PaymentBottomShitDialog.STRIP)) {
            Intent intent = new Intent(requireContext(), StripePaymentActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            intent.putExtra("from", Constants.WALLET);
            startActivityForResult(intent, 1001);
        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.RAZOR_PAY)) {
            Intent intent = new Intent(requireContext(), RazorPayActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            intent.putExtra("from", Constants.WALLET);
            startActivityForResult(intent, 1001);
        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.OFFLINE_PAY)) {
            //show an alert dialog
            showOfflinePaymentDialog();
        }
    }

    private void processPaypalPayment() {
        String[] paypalAcceptedList = getResources().getStringArray(R.array.paypal_currency_list);
        if (Arrays.asList(paypalAcceptedList).contains(ApiResources.CURRENCY)) {
            PayPalPayment payPalPayment = new PayPalPayment((new BigDecimal(String.valueOf(amount))),
                    ApiResources.CURRENCY,
                    "Payment for Package",
                    PayPalPayment.PAYMENT_INTENT_SALE);

            Log.e("Payment", "currency: " + ApiResources.CURRENCY);
            Intent intent = new Intent(requireContext(), PaymentActivity.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        } else {
            PaymentConfig paymentConfig = new DatabaseHelper(requireContext()).getConfigurationData().getPaymentConfig();
            double exchangeRate = Double.parseDouble(paymentConfig.getExchangeRate());
            double priceInDouble = Double.parseDouble(amount);
            double priceInUSD = (double) priceInDouble / exchangeRate;
            PayPalPayment payPalPayment = new PayPalPayment((new BigDecimal(String.valueOf(priceInUSD))),
                    "USD",
                    "Payment for Package",
                    PayPalPayment.PAYMENT_INTENT_SALE);
            Intent intent = new Intent(requireContext(), PaymentActivity.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
            startActivityForResult(intent, PAYPAL_REQUEST_CODE);
        }
    }

    private void showOfflinePaymentDialog() {
        DatabaseHelper helper = new DatabaseHelper(requireContext());
        PaymentConfig paymentConfig = helper.getConfigurationData().getPaymentConfig();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(paymentConfig.getOfflinePaymentTitle())
                .setMessage(paymentConfig.getOfflinePaymentInstruction())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss()).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                new ToastMsg(requireActivity()).toastIconError("Cancel");
            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            new ToastMsg(requireContext()).toastIconError("Invalid");
        } else if (requestCode == 1001 && resultCode == RESULT_OK) {
            paymentBottomShitDialog.dismiss();
            requireActivity().onBackPressed();
        }
    }

    private void completePayment(String paymentDetails) {
        try {
            JSONObject jsonObject = new JSONObject(paymentDetails);
            addMoneyToWalletApiCall(jsonObject.getJSONObject("response"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addMoneyToWalletApiCall(JSONObject response) {

        try {

            String payId = response.getString("id");
            final String state = response.getString("state");
            final String userId = PreferenceUtils.getUserId(requireContext());
            Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("user_id", userId);
            hashMap.put("amount", packageItem.getPrice());
            hashMap.put("payment_info", payId);
            hashMap.put("transaction_id", payId);
            hashMap.put("currency", currency);
            hashMap.put("payment_method", "Paypal");
            Call<ResponseBody> call = paymentApi.addMoneyToWallet(AppConfig.API_KEY, hashMap);
            plainActivity.activityIndicator(true);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    plainActivity.activityIndicator(false);
                    if (response.code() == 200) {
                        requireActivity().onBackPressed();
                    } else {
                        new ToastMsg(requireContext()).toastIconError(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                    new ToastMsg(requireContext()).toastIconError(getString(R.string.something_went_wrong));
                    t.printStackTrace();
                    plainActivity.activityIndicator(false);
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
            plainActivity.activityIndicator(false);
        }

    }

}