package com.life.android.fragments;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import com.life.android.AppConfig;
import com.life.android.PlainActivity;
import com.life.android.R;
import com.life.android.databinding.FragmentCmsBinding;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.CmsApi;
import com.life.android.network.model.CmsModel;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CMSFragment extends Fragment {

    public String from;
    private FragmentCmsBinding binding;
    private PlainActivity plainActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCmsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        plainActivity = ((PlainActivity) requireActivity());
        binding.cmsText.setMovementMethod(new LinkMovementMethod());
        getCMSContent();
    }

    private void getCMSContent() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        CmsApi api = retrofit.create(CmsApi.class);

        Call<CmsModel> call = api.privacyPolicy(AppConfig.API_KEY);

        if (from.equals(getString(R.string.terms_conditions))) {
            call = api.termsAndConditions(AppConfig.API_KEY);
        } else if (from.equals(getString(R.string.pay_watch_terms_conditions))) {
            call = api.payAndWatchTermsAndConditions(AppConfig.API_KEY);
        } else if (from.equals(getString(R.string.support))) {
            call = api.helpAndSupport(AppConfig.API_KEY);
        }
        plainActivity.activityIndicator(true);
        call.enqueue(new Callback<CmsModel>() {
            @Override
            public void onResponse(@NotNull Call<CmsModel> call, @NotNull Response<CmsModel> response) {
                if (!plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    if (response.code() == 200) {
                        if (response.body() != null) {
                            binding.cmsText.setText(HtmlCompat.fromHtml(response.body().content, HtmlCompat.FROM_HTML_MODE_COMPACT));
                        } else {
                            binding.cmsText.setText(R.string.no_data_found);
                        }
                    } else {
                        binding.cmsText.setText(R.string.no_data_found);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<CmsModel> call, @NotNull Throwable t) {
                if (!plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    binding.cmsText.setText(R.string.no_data_found);
                }
            }
        });
    }

}