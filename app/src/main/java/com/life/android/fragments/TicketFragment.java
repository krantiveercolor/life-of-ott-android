package com.life.android.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.AppConfig;
import com.life.android.FragmentLoaderActivity;
import com.life.android.PlainActivity;
import com.life.android.PurchasePlanActivity;
import com.life.android.R;
import com.life.android.RazorPayActivity;
import com.life.android.SSLPayActivity;
import com.life.android.StripePaymentActivity;
import com.life.android.adapters.TicketInfoAdapter;
import com.life.android.bottomshit.PaymentBottomShitDialog;
import com.life.android.database.DatabaseHelper;
import com.life.android.helper.IConstants;
import com.life.android.models.TransactionIdModel;
import com.life.android.models.single_details.SingleDetails;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PaymentApi;
import com.life.android.network.apis.SubscriptionApi;
import com.life.android.network.model.ActiveStatus;
import com.life.android.network.model.Package;
import com.life.android.network.model.config.PaymentConfig;
import com.life.android.utils.ApiResources;
import com.life.android.utils.Constants;
import com.life.android.utils.HtmlTagHelper;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;
import com.life.android.widget.ApplyCouponBottomSheet;
import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
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
import static android.content.Context.MODE_PRIVATE;

public class TicketFragment extends Fragment implements PaymentBottomShitDialog.OnBottomShitClickListener {

    private String type = "";


    public TicketFragment() {
        // Required empty public constructor
    }

    public static TicketFragment newInstance(SingleDetails singleDetails) {
        TicketFragment fragment = new TicketFragment();
        Bundle args = new Bundle();
        args.putParcelable("single_details", singleDetails);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            singleDetails = getArguments().getParcelable("single_details");
            type = singleDetails.getPreBookingEnabled();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ticket, container, false);
    }

    private static final int PAYPAL_REQUEST_CODE = 100;
    private RecyclerView ticketInfoRv;
    private ImageView posterImgView;
    private TicketInfoAdapter ticketInfoAdapter;
    private String currency = "", selectedCountry = "";
    private PaymentBottomShitDialog paymentBottomShitDialog;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(ApiResources.PAYPAL_CLIENT_ID);
    private Package packageItem = new Package();
    private CheckBox termsAndConditionsCheckBox;
    private SingleDetails singleDetails;
    private TextView ticketPriceView, gstPriceView, gstPercentageView, totalPriceView, payWatchDescView;
    private ProgressDialog dialog;
    private ProgressBar progress;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("push", MODE_PRIVATE);
        selectedCountry = sharedPreferences.getString("country", "");

        castingViews(view);

        view.findViewById(R.id.have_coupon_code_view).setOnClickListener(view1 -> {
            ApplyCouponBottomSheet applyCouponBottomSheet = ApplyCouponBottomSheet.newInstance();
            applyCouponBottomSheet.show(getChildFragmentManager(), applyCouponBottomSheet.getTag());
        });

        view.findViewById(R.id.ticket_back_view).setOnClickListener(view1 ->
                requireActivity().onBackPressed());

        view.findViewById(R.id.ticket_pay_now_btn).setOnClickListener(view1 -> {
            if (singleDetails.getPriceInUsd().equals("0") || singleDetails.getPrice().equals("0")) {
                Toast.makeText(requireActivity(), "Amount should be grater than 0", Toast.LENGTH_SHORT).show();
            } else {
                if (termsAndConditionsCheckBox.isChecked()) {
                    paymentBottomShitDialog = new PaymentBottomShitDialog();
                    paymentBottomShitDialog.enableWallet = true;
                    paymentBottomShitDialog.setOnBottomShitClickListener(this);
                    paymentBottomShitDialog.show(requireActivity().getSupportFragmentManager(), paymentBottomShitDialog.getTag());
                } else {
                    new ToastMsg(requireContext()).toastIconError("Please read and accept the terms & conditions");
                }
            }

        });

        view.findViewById(R.id.terms_and_conditions_view).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.terms_conditions);
            startActivity(intent);
            requireActivity().overridePendingTransition(R.anim.enter, R.anim.exit);
        });

        PaymentConfig config = new DatabaseHelper(requireContext()).getConfigurationData().getPaymentConfig();
        currency = config.getCurrencySymbol();

        packageItem.setName("Purchasing movie " + singleDetails.getTitle());
        packageItem.setPrice(singleDetails.getFinalPrice());
        packageItem.setPlanId(singleDetails.getVideosId());
        packageItem.setUsdPrice(singleDetails.getPriceInUsd());
        packageItem.setGstAmountInUsd(singleDetails.getGstAmountInUsd());



        attachDataToView();

        dialog = new ProgressDialog(requireContext());
        dialog.setMessage("Processing payment please wait!!");
        dialog.setCancelable(false);
    }

    private void castingViews(@NonNull View view) {
        payWatchDescView = view.findViewById(R.id.pay_watch_desc_view);
        posterImgView = view.findViewById(R.id.ticket_img_view);
        ticketPriceView = view.findViewById(R.id.ticket_price_view);
        gstPriceView = view.findViewById(R.id.gst_price_view);
        gstPercentageView = view.findViewById(R.id.gst_percentage_view);
        totalPriceView = view.findViewById(R.id.total_price_view);
        termsAndConditionsCheckBox = view.findViewById(R.id.terms_and_conditions_check_box);
        progress = view.findViewById(R.id.progress);
    }

    private void attachDataToView() {
        Glide.with(requireActivity()).load(singleDetails.getPosterUrl()).into(posterImgView);
        if (selectedCountry.equals(Constants.BANGLA)) {
            ticketPriceView.setText(String.format("%s %s", currency, singleDetails.getPrice()));
            gstPriceView.setText(String.format("%s %s", currency, singleDetails.getGstAmount()));
            totalPriceView.setText(String.format("%s %s", currency, singleDetails.getFinalPrice()));
            gstPercentageView.setText(String.format("%s (%s%%)", gstPercentageView.getText(), singleDetails.getGstPercentage()));

        } else {
            ticketPriceView.setText(String.format("$%s", singleDetails.getPriceInUsd()));
            gstPriceView.setText(String.format("$%s", singleDetails.getGstAmountInUsd()));
            double price = Double.parseDouble(singleDetails.getPriceInUsd());
            double gstAmomunt = Double.parseDouble(singleDetails.getGstAmountInUsd());
            double ttl = price + gstAmomunt;
            //int total = (int) Math.floor(ttl);
            totalPriceView.setText(String.format("$%s", String.valueOf(ttl)));
            gstPercentageView.setText(String.format("%s (%s%%)", gstPercentageView.getText(), singleDetails.getGstPercentageinUsd()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            payWatchDescView.setText(Html.fromHtml(singleDetails.getPayWatchDescription(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            payWatchDescView.setText(Html.fromHtml(singleDetails.getPayWatchDescription(), null,
                    new HtmlTagHelper()));
        }
    }

    @Override
    public void onBottomShitClick(String paymentMethodName) {
        if (paymentMethodName.equals(PaymentBottomShitDialog.PAYPAL)) {
            processPaypalPayment();
        } else if (paymentMethodName.equals(PaymentBottomShitDialog.STRIP)) {
            Intent intent = new Intent(requireContext(), StripePaymentActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            intent.putExtra("from", Constants.PAY_WATCH);
            intent.putExtra("movie_id", singleDetails.getVideosId());
            intent.putExtra("preBook", singleDetails.getPreBookingEnabled());
            startActivityForResult(intent, 1001);
        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.RAZOR_PAY)) {
            paymentTransactionIdApiCall();
            /*Intent intent = new Intent(requireContext(), RazorPayActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            intent.putExtra("from", Constants.PAY_WATCH);
            intent.putExtra("movie_id", singleDetails.getVideosId());
            intent.putExtra("preBook", singleDetails.getPreBookingEnabled());
            startActivityForResult(intent, 1001);*/
        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.OFFLINE_PAY)) {
            //show an alert dialog
            showOfflinePaymentDialog();
        } else if (paymentMethodName.equalsIgnoreCase(Constants.WALLET)) {
            float planPrice = Float.parseFloat(packageItem.getPrice());
            float walletAmount = Float.parseFloat(PreferenceUtils.getWalletAmount(requireContext()));
            if (planPrice <= walletAmount) {
                sendDataToServer(new JSONObject(), Constants.WALLET);
            } else
                new ToastMsg(requireContext()).toastIconError("Your wallet amount " + currency + walletAmount + " was less than plan price");
        }
    }

    private void paymentTransactionIdApiCall() {
        progress.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("user_id", PreferenceUtils.getUserId(requireActivity()));
        hashMap.put("plan_id", "");
        hashMap.put("amount", packageItem.getPrice());
        hashMap.put("payment_status", "1");
        hashMap.put("currency", "USD");
        Call<TransactionIdModel> call = paymentApi.getTransId(AppConfig.API_KEY, hashMap);
        call.enqueue(new Callback<TransactionIdModel>() {
            @Override
            public void onResponse(Call<TransactionIdModel> call, Response<TransactionIdModel> response) {
                progress.setVisibility(View.GONE);
                if (response.code() == 200 && response.body() != null) {
                    paymentScreen(response.body());
                }
            }

            @Override
            public void onFailure(Call<TransactionIdModel> call, Throwable t) {
                progress.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });
    }

    private void paymentScreen(TransactionIdModel body) {
        Intent intent = new Intent(requireActivity(), SSLPayActivity.class);
        intent.putExtra("package", packageItem);
        intent.putExtra("currency", currency);
        intent.putExtra("from", Constants.PAY_WATCH);
        intent.putExtra("transId", body.getTransaction_id());
        startActivityForResult(intent, 1001);
    }

    private void processPaypalPayment() {

        try {
            // ---------- start paypal service ----------
            Intent intent = new Intent(requireContext(), PayPalService.class);
            intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
            requireActivity().startService(intent);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

        String[] paypalAcceptedList = getResources().getStringArray(R.array.paypal_currency_list);
        if (Arrays.asList(paypalAcceptedList).contains(ApiResources.CURRENCY)) {
            PayPalPayment payPalPayment = new PayPalPayment((new BigDecimal(String.valueOf(singleDetails.getPrice()))),
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
            double priceInDouble = Double.parseDouble(singleDetails.getPrice());
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
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
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
            }
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            new ToastMsg(requireContext()).toastIconError("Invalid");
        } else if (requestCode == 1001 && resultCode == RESULT_OK) {
            PreferenceUtils.updateWalletAmountFromServer(requireContext());
            Intent intent = requireActivity().getIntent();
            requireActivity().setResult(RESULT_OK, intent);
            requireActivity().finish();
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
            final String userId = PreferenceUtils.getUserId(requireContext());
            Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("videos_id", singleDetails.getVideosId());
            hashMap.put("user_id", userId);
            hashMap.put("paid_amount", packageItem.getPrice());
            hashMap.put("payment_info", payId);
            hashMap.put("payment_method", paidWith);
            hashMap.put("paid_with", paidWith);
            if (singleDetails.getPreBookingEnabled().equalsIgnoreCase("true")) {
                hashMap.put("type", Constants.pre_booking);
            } else {
                hashMap.put("type", Constants.PAY_WATCH);
            }
            hashMap.put("device_type", Constants.device_type);
            Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, hashMap);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        updateActiveStatus();
                    } else {
                        dialog.cancel();
                        new ToastMsg(requireContext()).toastIconError(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(@NotNull Call<ResponseBody> call, @NotNull Throwable t) {
                    dialog.cancel();
                    new ToastMsg(requireContext()).toastIconError(getString(R.string.something_went_wrong));
                    t.printStackTrace();
                    Log.e("PAYMENT", "error: " + t.getLocalizedMessage());
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(requireContext()));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(@NotNull Call<ActiveStatus> call, @NotNull Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(requireContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    dialog.cancel();
                    PreferenceUtils.updateWalletAmountFromServer(requireContext());
                    new ToastMsg(requireContext()).toastIconSuccess(getResources().getString(R.string.payment_success));
                    Intent intent = requireActivity().getIntent();
                    requireActivity().setResult(RESULT_OK, intent);
                    requireActivity().finish();
                }
            }

            @Override
            public void onFailure(@NotNull Call<ActiveStatus> call, @NotNull Throwable t) {
                t.printStackTrace();
                dialog.cancel();
            }
        });

    }

}