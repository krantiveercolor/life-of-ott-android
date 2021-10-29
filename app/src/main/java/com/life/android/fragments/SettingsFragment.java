package com.life.android.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.life.android.AppConfig;
import com.life.android.BuildConfig;
import com.life.android.PlainActivity;
import com.life.android.R;
import com.life.android.databinding.FragmentSettingsBinding;
import com.life.android.helper.IConstants;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.UserDataApi;
import com.life.android.network.model.User;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.Tools;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Pushpendra Kumar on 23/08/21 @ 1:10 pm
 * Organization - Team Leader @ Colour Moon Technologies PVT LTD INDIA
 * Contact - pushpendra@thecolourmoon.com ► +91-9719325299
 */

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private PlainActivity plainActivity = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        plainActivity = ((PlainActivity) requireActivity());

        if (PreferenceUtils.isLoggedIn(requireActivity())) {
            getProfileDetails();
        } else {
            plainActivity.activityIndicator(false);
        }

        String applicationDetails = "Version details : " + BuildConfig.VERSION_NAME + "[" + BuildConfig.VERSION_CODE + "]";
        binding.versionText.setText(applicationDetails);

        SharedPreferences preferences = requireActivity().getSharedPreferences("push", MODE_PRIVATE);
        binding.notifySwitch.setChecked(preferences.getBoolean("status", true));

        binding.notifySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("status", isChecked);
            editor.apply();
        });

        binding.changePasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.change_password);
            startActivity(intent);
            plainActivity.overridePendingTransition(R.anim.enter, R.anim.exit);
        });

        binding.helpSupportText.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.support);
            startActivity(intent);
            plainActivity.overridePendingTransition(R.anim.enter, R.anim.exit);
        });

        binding.shareText.setOnClickListener(v -> {
            Tools.share(plainActivity, "");
        });

        binding.contactUsText.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.contact_us);
            startActivity(intent);
            plainActivity.overridePendingTransition(R.anim.enter, R.anim.exit);
        });

        binding.termsConditionText.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.terms_conditions);
            startActivity(intent);
            plainActivity.overridePendingTransition(R.anim.enter, R.anim.exit);
        });

        binding.privacyPolicyText.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlainActivity.class);
            intent.putExtra(IConstants.IntentString.type, IConstants.Fragments.privacy_policy);
            startActivity(intent);
            plainActivity.overridePendingTransition(R.anim.enter, R.anim.exit);
        });
    }

    private void getProfileDetails() {
        plainActivity.activityIndicator(true);
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        UserDataApi api = retrofit.create(UserDataApi.class);
        Call<User> call = api.getUserData(AppConfig.API_KEY, PreferenceUtils.getUserId(requireActivity()));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NotNull Call<User> call, @NotNull Response<User> response) {
                if (!plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    if (response.code() == 200 && response.body() != null) {
                        User user = response.body();
                        if (user.isPassword_available()) {
                            binding.changePasswordText.setVisibility(View.VISIBLE);
                        } else {
                            binding.changePasswordText.setVisibility(View.GONE);
                        }
                    } else {
                        binding.changePasswordText.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<User> call, @NotNull Throwable t) {
                if (!plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    binding.changePasswordText.setVisibility(View.GONE);
                }
            }
        });
    }
}