package com.life.android.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.life.android.AppConfig;
import com.life.android.PlainActivity;
import com.life.android.adapters.MoviesAdapter;
import com.life.android.databinding.FragmentMoviesBinding;
import com.life.android.helper.PaginationScrollListener;
import com.life.android.models.CommonModels;
import com.life.android.models.home_content.Video;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.FavouriteApi;
import com.life.android.network.apis.MovieApi;
import com.life.android.utils.PreferenceUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;


public class MoviesFragment extends Fragment {
    public String movieType = null;
    private String TAG = "MoviesFragment";

    //private final String classTag = "MOVIES_FRG";

    private FragmentMoviesBinding binding;
    private PlainActivity plainActivity = null;
    private boolean isLoading = false;
    private boolean loadedAllData = false;
    private int currentPage = 1;
    private final ArrayList<CommonModels> dataList = new ArrayList<>();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMoviesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            plainActivity = ((PlainActivity) getActivity());
            if (plainActivity != null) {
                plainActivity.activityIndicator(true);
            }
            if (movieType.equals("fav") || movieType.equals("watch_later")) {
                getFavList();
            } else {
                getMovieList();
            }
            binding.swipeRefreshLayout.setOnRefreshListener(() -> {
                if (!isLoading) {
                    if (movieType.equals("watch_later") || movieType.equals("fav")) {
                        binding.swipeRefreshLayout.setEnabled(false);
                        return;
                    }
                    dataList.clear();
                    loadedAllData = false;
                    currentPage = 1;
                    getMovieList();
                }
            });
        }
    }

    private void getFavList() {
        isLoading = true;
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<List<Video>> call = api.getFavoriteList(AppConfig.API_KEY, PreferenceUtils.getUserId(getContext()), 1, movieType);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(@NotNull Call<List<Video>> call, @NotNull retrofit2.Response<List<Video>> response) {
                if (response.code() == 200 && response.body() != null) {
                    Log.d(TAG, "onResponse: " + response.body());
                    isLoading = false;


                    if (plainActivity != null && !plainActivity.isFinishing()) {
                        plainActivity.activityIndicator(false);
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.footerProgressBar.setVisibility(View.GONE);
                        if ((response.body().isEmpty() || String.valueOf(response).length() < 10) && currentPage == 1) {
                            binding.errorLayout.setVisibility(View.VISIBLE);
                        } else {
                            binding.errorLayout.setVisibility(View.GONE);
                        }
                        if (response.body().isEmpty()) {
                            loadedAllData = true;
                        } else {
                            for (int i = 0; i < response.body().size(); i++) {
                                Video video = response.body().get(i);
                                CommonModels models = new CommonModels();
                                models.setImageUrl(video.getThumbnailUrl());
                                models.setTitle(video.getTitle());
                                models.setQuality(video.getVideoQuality());
                                models.setIsPaid(video.getIsPaid());
                                models.setVideoType("movie");
                                models.setReleaseDate(video.getRelease());
                                models.setVideo_view_type(video.getVideo_view_type());
                                if (video.getIsTvseries().equals("1")) {
                                    models.setVideoType("tvseries");
                                } else {
                                    models.setVideoType("movie");
                                }
                                models.setId(video.getVideosId());
                                dataList.add(models);
                            }
                            configureRecyclerViewData(response.body().size());
                        }
                    }
                } else {
                    loadedAllData = true;
                    isLoading = false;
                    if (plainActivity != null && !plainActivity.isFinishing()) {
                        plainActivity.activityIndicator(false);
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.footerProgressBar.setVisibility(View.GONE);
                        if (currentPage == 1) {
                            binding.errorLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Video>> call, @NonNull Throwable t) {
                loadedAllData = true;
                isLoading = false;
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    if (currentPage == 1) {
                        binding.errorLayout.setVisibility(View.VISIBLE);
                    }
                }

            }
        });
    }


    private void getMovieList() {
        isLoading = true;
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);
        Call<List<Video>> call = api.getMovies(AppConfig.API_KEY, currentPage, movieType);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(@NotNull Call<List<Video>> call, @NotNull retrofit2.Response<List<Video>> response) {
                if (response.code() == 200 && response.body() != null) {
                    Log.d(TAG, "onResponse: " + response.body());
                    isLoading = false;
                    if (plainActivity != null && !plainActivity.isFinishing()) {
                        plainActivity.activityIndicator(false);
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.footerProgressBar.setVisibility(View.GONE);
                        if ((response.body().isEmpty() || String.valueOf(response).length() < 10) && currentPage == 1) {
                            binding.errorLayout.setVisibility(View.VISIBLE);
                        } else {
                            binding.errorLayout.setVisibility(View.GONE);
                        }
                        if (response.body().isEmpty()) {
                            loadedAllData = true;
                        } else {
                            for (int i = 0; i < response.body().size(); i++) {
                                Video video = response.body().get(i);
                                CommonModels models = new CommonModels();
                                models.setImageUrl(video.getThumbnailUrl());
                                models.setTitle(video.getTitle());
                                models.setQuality(video.getVideoQuality());
                                models.setIsPaid(video.getIsPaid());
                                models.setVideoType("movie");
                                models.setReleaseDate(video.getRelease());
                                models.setVideo_view_type(video.getVideo_view_type());
                                if (video.getIsTvseries().equals("1")) {
                                    models.setVideoType("tvseries");
                                } else {
                                    models.setVideoType("movie");
                                }
                                models.setId(video.getVideosId());
                                dataList.add(models);
                            }
                            configureRecyclerViewData(response.body().size());
                        }
                    }
                } else {
                    loadedAllData = true;
                    isLoading = false;
                    if (plainActivity != null && !plainActivity.isFinishing()) {
                        plainActivity.activityIndicator(false);
                        binding.swipeRefreshLayout.setRefreshing(false);
                        binding.footerProgressBar.setVisibility(View.GONE);
                        if (currentPage == 1) {
                            binding.errorLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Video>> call, @NonNull Throwable t) {
                loadedAllData = true;
                isLoading = false;
                if (plainActivity != null && !plainActivity.isFinishing()) {
                    plainActivity.activityIndicator(false);
                    binding.swipeRefreshLayout.setRefreshing(false);
                    if (currentPage == 1) {
                        binding.errorLayout.setVisibility(View.VISIBLE);
                    }
                }

            }
        });
    }

    private void configureRecyclerViewData(int additionalItemSize) {
        if (currentPage == 1) {
            if (binding.recyclerView.getAdapter() == null) {
                binding.recyclerView.setAdapter(new MoviesAdapter(dataList, getContext(), movieType));
                GridLayoutManager manager = (GridLayoutManager) binding.recyclerView.getLayoutManager();
                if (manager != null) {
                    binding.recyclerView.addOnScrollListener(new PaginationScrollListener(manager) {
                        @Override
                        public boolean isLastPage() {
                            return false;
                        }

                        @Override
                        public boolean isLoading() {
                            return false;
                        }

                        @Override
                        public void loadMoreItems() {
                            if (!loadedAllData && !isLoading) {
                                currentPage = currentPage + 1;
                                binding.footerProgressBar.setVisibility(View.VISIBLE);
                                if (movieType.equals("fav") || movieType.equals("watch_later")) {
                                    binding.footerProgressBar.setVisibility(View.GONE);
                                    if (currentPage == 1)
                                        getFavList();
                                } else {
                                    getMovieList();
                                }
                            }
                        }
                    });
                }
                binding.recyclerView.setItemViewCacheSize(100);
            } else {
                binding.recyclerView.getAdapter().notifyItemRangeChanged(0, dataList.size());
            }
        } else {
            int endPosition = this.dataList.size();
            int startPosition = endPosition - additionalItemSize;
            if (binding.recyclerView.getAdapter() != null) {
                binding.recyclerView.getAdapter().notifyItemRangeInserted(startPosition, endPosition);
            }
        }
    }
}