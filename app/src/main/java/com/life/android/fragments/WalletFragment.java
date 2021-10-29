package com.life.android.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.life.android.AppConfig;
import com.life.android.PlainActivity;
import com.life.android.R;
import com.life.android.adapters.WalletHistoryAdapter;
import com.life.android.database.DatabaseHelper;
import com.life.android.databinding.FragmentWalletBinding;
import com.life.android.helper.IConstants;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.PaymentApi;
import com.life.android.network.model.WalletHistoryResponseModel;
import com.life.android.network.model.config.PaymentConfig;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class WalletFragment extends Fragment {
    private FragmentWalletBinding binding;
    private PlainActivity plainActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        plainActivity = ((PlainActivity) requireActivity());

        binding.walletAddCashBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.add_cash_wallet);
            startActivity(intent);
            plainActivity.overridePendingTransition(R.anim.enter, R.anim.exit);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        walletHistoryApiCall();
    }

    private void walletHistoryApiCall() {
        plainActivity.activityIndicator(true);
        final String userId = PreferenceUtils.getUserId(requireContext());
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<WalletHistoryResponseModel> call = paymentApi.walletHistory(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<WalletHistoryResponseModel>() {
            @Override
            public void onResponse(@NotNull Call<WalletHistoryResponseModel> call, @NotNull Response<WalletHistoryResponseModel> response) {
                plainActivity.activityIndicator(false);
                if (response.code() == 200 && response.body() != null) {
                    attachDataToView(response.body());
                } else {
                    new ToastMsg(requireContext()).toastIconError(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(@NotNull Call<WalletHistoryResponseModel> call, @NotNull Throwable t) {
                t.printStackTrace();
                new ToastMsg(requireContext()).toastIconError(t.getLocalizedMessage());
            }
        });
    }


    private void attachDataToView(WalletHistoryResponseModel walletHistoryResponseModel) {
        PaymentConfig config = new DatabaseHelper(requireContext()).getConfigurationData().getPaymentConfig();
        binding.walletBalanceView.setText(String.format("%s %s", config.getCurrency(), walletHistoryResponseModel.getWalletAmount()));
        WalletHistoryAdapter walletHistoryAdapter = new WalletHistoryAdapter(requireContext(), walletHistoryResponseModel.getWalletTransactions(), config.getCurrencySymbol());
        binding.walletHistoryRecyclerView.setAdapter(walletHistoryAdapter);
        PreferenceUtils.updateWalletAmount(requireActivity(), walletHistoryResponseModel.getWalletAmount());
    }
}