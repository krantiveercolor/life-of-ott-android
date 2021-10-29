package com.life.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.life.android.AppConfig;
import com.life.android.PlainActivity;
import com.life.android.adapters.GenreAdapter;
import com.life.android.databinding.FragmentGenreBinding;
import com.life.android.models.CommonModels;
import com.life.android.models.home_content.AllGenre;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.GenreApi;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class GenreFragment extends Fragment {

    private FragmentGenreBinding binding;
    private PlainActivity plainActivity = null;
    private final ArrayList<CommonModels> dataList = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGenreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            plainActivity = ((PlainActivity) getActivity());
            if (plainActivity != null) {
                plainActivity.activityIndicator(true);
            }
            getDataList();

            binding.swipeRefreshLayout.setOnRefreshListener(() -> {
                dataList.clear();
                getDataList();
            });
        }
    }


    private void getDataList() {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        GenreApi api = retrofit.create(GenreApi.class);
        Call<List<AllGenre>> call = api.getGenre(AppConfig.API_KEY);
        call.enqueue(new Callback<List<AllGenre>>() {
            @Override
            public void onResponse(@NotNull Call<List<AllGenre>> call, @NotNull retrofit2.Response<List<AllGenre>> response) {
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    if (response.code() == 200 && response.body() != null && !response.body().isEmpty()) {
                        for (int i = 0; i < response.body().size(); i++) {
                            AllGenre genre = response.body().get(i);
                            CommonModels models = new CommonModels();
                            models.setId(genre.getGenreId());
                            models.setTitle(genre.getName());
                            models.setImageUrl(genre.getImageUrl());
                            dataList.add(models);
                        }
                        configureRecyclerViewData();
                    } else {
                        binding.errorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AllGenre>> call, @NonNull Throwable t) {

                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    binding.errorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void configureRecyclerViewData() {
        GenreAdapter mAdapter = new GenreAdapter(requireContext(), dataList, "genre", "");
        binding.recyclerView.setAdapter(mAdapter);
    }
}