package com.life.android.fragments;

import static com.life.android.helper.CMExtenstionKt.hideKeyboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.life.android.AppConfig;
import com.life.android.PlainActivity;
import com.life.android.R;
import com.life.android.database.DatabaseHelper;
import com.life.android.databinding.FragmentContactUsBinding;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.ContactUsApi;
import com.life.android.network.model.StatusModel;
import com.life.android.network.model.User;
import com.life.android.utils.ToastMsg;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ContactUsFragment extends Fragment {

    private FragmentContactUsBinding binding;
    private PlainActivity plainActivity;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactUsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        plainActivity = ((PlainActivity) requireActivity());
        plainActivity.activityIndicator(false);


        AppCompatButton btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            hideKeyboard(v);
            submitContactUsForm();
        });


        DatabaseHelper db = new DatabaseHelper(requireContext());
        if (db.getUserData() != null) {
            User user = db.getUserData();
            binding.etName.setText(user.getName());
            binding.etEmail.setText(user.getEmail());
            binding.etPhone.setText(user.getPhone());
        }
    }

    private void submitContactUsForm() {
        if (binding.etName.getText() == null || binding.etName.getText().toString().isEmpty()) {
            binding.tvError.setText(getString(R.string.enter_name));
            binding.tvError.setVisibility(View.VISIBLE);
        } else if (binding.etEmail.getText() == null || binding.etEmail.getText().toString().isEmpty()) {
            binding.tvError.setText(getString(R.string.enter_email));
            binding.tvError.setVisibility(View.VISIBLE);
        } else if (binding.etPhone.getText() == null || binding.etPhone.getText().toString().isEmpty()) {
            binding.tvError.setText(getString(R.string.enter_phone));
            binding.tvError.setVisibility(View.VISIBLE);
        } else if (binding.etMessage.getText() == null || binding.etMessage.getText().toString().isEmpty()) {
            binding.tvError.setText(getString(R.string.enter_message));
            binding.tvError.setVisibility(View.VISIBLE);
        } else {
            binding.tvError.setVisibility(View.GONE);
            plainActivity.activityIndicator(true);
            HashMap<String, String> map = new HashMap<>();
            map.put("name", binding.etName.getText().toString());
            map.put("email", binding.etEmail.getText().toString());
            map.put("mobile_number", binding.etPhone.getText().toString());
            map.put("message", binding.etMessage.getText().toString());
            map.put("from_source", "Android");


            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            ContactUsApi api = retrofit.create(ContactUsApi.class);

            Call<StatusModel> call = api.submitContactUsForm(AppConfig.API_KEY, map);

            call.enqueue(new Callback<StatusModel>() {
                @Override
                public void onResponse(@NotNull Call<StatusModel> call, @NotNull Response<StatusModel> response) {
                    if (!plainActivity.isFinishing()) {
                        plainActivity.activityIndicator(false);
                        if (response.code() == 200 && response.body() != null) {
                            StatusModel model = response.body();
                            if (model.status.equals("valid")) {
                                binding.etMessage.setText("");
                                new ToastMsg(requireContext()).toastIconSuccess(model.message);
                            } else {
                                new ToastMsg(requireContext()).toastIconError(model.message);
                            }
                        } else {
                            binding.tvError.setVisibility(View.VISIBLE);
                            binding.tvError.setText(getString(R.string.no_data_found));
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<StatusModel> call, @NotNull Throwable t) {
                    if (!plainActivity.isFinishing()) {
                        plainActivity.activityIndicator(false);
                        binding.tvError.setVisibility(View.VISIBLE);
                        binding.tvError.setText(t.getLocalizedMessage());
                    }
                }
            });
        }
    }
}