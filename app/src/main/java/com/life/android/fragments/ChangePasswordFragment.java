package com.life.android.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.life.android.LoginActivity;
import com.life.android.R;
import com.life.android.database.DatabaseHelper;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.ProfileApi;
import com.life.android.network.model.ResponseStatus;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
import com.life.android.utils.ToastMsg;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

import static android.content.Context.MODE_PRIVATE;
import static com.life.android.utils.PreferenceUtils.TAG;

public class ChangePasswordFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    private EditText newPassEditText, oldPassEditText;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newPassEditText = view.findViewById(R.id.password);
        oldPassEditText = view.findViewById(R.id.currentPassword);

        view.findViewById(R.id.submit_button).setOnClickListener(view1 -> {
            String newPass = newPassEditText.getText().toString();
            String oldPass = oldPassEditText.getText().toString();
            if (oldPass.isEmpty()) {
                oldPassEditText.setError("Please enter your current password");
                newPassEditText.setError(null);
            } else if (newPass.isEmpty()) {
                newPassEditText.setError("Please enter new password");
                oldPassEditText.setError(null);
            } else
                changePasswordApiCall(oldPass, newPass);
        });

    }

    private void changePasswordApiCall(String oldPass, String newPass) {
        Map<String, String> data = new HashMap<>();
        data.put("current_password", oldPass);
        data.put("password", newPass);
        data.put("id", PreferenceUtils.getUserId(requireContext()));

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ProfileApi api = retrofit.create(ProfileApi.class);
        Call<ResponseStatus> call = api.changePassword(data);
        call.enqueue(new Callback<ResponseStatus>() {
            @Override
            public void onResponse(@NonNull Call<ResponseStatus> call, @NonNull retrofit2.Response<ResponseStatus> response) {
                if (response.code() == 200 && response.body() != null) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        new ToastMsg(requireContext()).toastIconSuccess(response.body().getData());
                        logout();
                    } else {
                        new ToastMsg(requireContext()).toastIconError(response.body().getData());
                    }
                } else {
                    new ToastMsg(requireContext()).toastIconError(getString(R.string.something_went_wrong));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseStatus> call, @NonNull Throwable t) {
                new ToastMsg(requireContext()).toastIconError(getString(R.string.something_went_wrong));
                Log.e(TAG, t.getLocalizedMessage());
            }
        });
    }

    private void logout() {
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        databaseHelper.deleteAllDownloadData();
        databaseHelper.deleteUserData();
        databaseHelper.deleteAllActiveStatusData();

        SharedPreferences.Editor sp = requireActivity().getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
        sp.putBoolean(Constants.USER_LOGIN_STATUS, false);
        sp.apply();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


}