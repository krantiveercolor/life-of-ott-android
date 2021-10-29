package com.life.android.fragments;

import static com.life.android.helper.CMExtenstionKt.setSnackBar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.life.android.AppConfig;
import com.life.android.MainActivity;
import com.life.android.R;
import com.life.android.adapters.GenreHomeAdapter;
import com.life.android.adapters.SliderAdapter;
import com.life.android.database.DatabaseHelper;
import com.life.android.models.CommonModels;
import com.life.android.models.GenreModel;
import com.life.android.models.home_content.FeaturesGenreAndMovie;
import com.life.android.models.home_content.HomeContent;
import com.life.android.models.home_content.Slider;
import com.life.android.models.home_content.Video;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.HomeContentApi;
import com.bumptech.glide.Glide;
import com.github.islamkhsh.CardSliderViewPager;
import com.google.android.material.appbar.AppBarLayout;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class HomeFragment extends Fragment {
    private final String classTag = "HOME_FRG";

    CardSliderViewPager cViewPager;

    private RecyclerView recyclerViewGenre;

    private final List<GenreModel> listGenre = new ArrayList<>();

    private View sliderLayout;

    private MainActivity activity;

    private ImageView menuIv, searchIv;
    private HomeContent homeContent = null;
    private AppBarLayout appBarLayout;
    private DotsIndicator dotsIndicator;
    private ImageView indicatorImageView;
    private NestedScrollView contentView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        contentView = view.findViewById(R.id.contentView);
        indicatorImageView = view.findViewById(R.id.indicatorImageView);
        Glide.with(this).load(R.drawable.logo_anim).into(indicatorImageView);

        sliderLayout = view.findViewById(R.id.slider_layout);
        cViewPager = view.findViewById(R.id.c_viewPager);
        menuIv = view.findViewById(R.id.bt_menu);
        searchIv = view.findViewById(R.id.search_iv);
        appBarLayout = view.findViewById(R.id.appBarLayout);
        dotsIndicator = view.findViewById(R.id.dots_indicator);

        //----genre's recycler view--------------------
        recyclerViewGenre = view.findViewById(R.id.recyclerView_by_genre);
        recyclerViewGenre.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewGenre.setNestedScrollingEnabled(false);

        contentView = view.findViewById(R.id.contentView);
        contentView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> animateToolBar(scrollY < 100));

        menuIv.setOnClickListener(v -> activity.openDrawer());

        searchIv.setOnClickListener(v -> activity.goToSearchActivity());

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::getHomeContentDataFromServer);

        activityIndicator(true);
        getHomeContentDataFromServer();
    }


    private void populateViews() {
        Slider slider = homeContent.getSlider();
        if (slider.getSliderType().equalsIgnoreCase("disable")) {
            sliderLayout.setVisibility(View.GONE);
        }

        if (getContext() != null) {
            SliderAdapter sliderAdapter = new SliderAdapter(slider.getSlideArrayList(), requireContext());
            cViewPager.setAdapter(sliderAdapter);
            dotsIndicator.setViewPager(cViewPager);
        }

        //get data by genre
        for (int i = 0; i < homeContent.getFeaturesGenreAndMovie().size(); i++) {
            FeaturesGenreAndMovie genreAndMovie = homeContent.getFeaturesGenreAndMovie().get(i);
            GenreModel models = new GenreModel();
            models.setName(genreAndMovie.getName());
            models.setViewType(genreAndMovie.getViewType());
            models.setId(genreAndMovie.getGenreId());
            List<CommonModels> listGenreMovie = new ArrayList<>();
            for (int j = 0; j < genreAndMovie.getVideos().size(); j++) {
                Video video = genreAndMovie.getVideos().get(j);
                CommonModels commonModels = new CommonModels();
                commonModels.setId(video.getVideosId());
                commonModels.setTitle(video.getTitle());
                commonModels.setIsPaid(video.getIsPaid());
                commonModels.setVideo_view_type(video.getVideo_view_type());
                if (video.getIsTvseries() != null && video.getIsTvseries().equals("0")) {
                    commonModels.setVideoType("movie");
                } else if (video.continue_watch_minutes != null) {
                    commonModels.setVideoType(video.continue_watch_minutes.video_type);
                    commonModels.continue_watch_minutes = video.continue_watch_minutes;
                } else {
                    commonModels.setVideoType("tvseries");
                }
                commonModels.setReleaseDate(video.getRelease());
                commonModels.setQuality(video.getVideoQuality());
                commonModels.setImageUrl(video.getThumbnailUrl());
                commonModels.setPosterUrl(video.getPosterUrl());
                listGenreMovie.add(commonModels);
            }
            models.setList(listGenreMovie);
            listGenre.add(models);
        }
        GenreHomeAdapter genreHomeAdapter = new GenreHomeAdapter(getContext(), listGenre);
        recyclerViewGenre.setAdapter(genreHomeAdapter);
        recyclerViewGenre.setItemViewCacheSize(100);
    }


    private void getHomeContentDataFromServer() {
        listGenre.clear();
        String userId = new DatabaseHelper(requireContext()).getUserData().getUser_id();
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        HomeContentApi api = retrofit.create(HomeContentApi.class);

        Call<HomeContent> call = api.getHomeContent(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<HomeContent>() {
            @Override
            public void onResponse(@NotNull Call<HomeContent> call, @NotNull retrofit2.Response<HomeContent> response) {
                swipeRefreshLayout.setRefreshing(false);
                activityIndicator(false);
                if (response.code() == 200 && response.body() != null) {

                    homeContent = response.body();
                    homeContent.setHomeContentId(1);
                    populateViews();
                } else {
                    setSnackBar(contentView, "Something went wrong, Please try again after some time!", 2);
                }
            }

            @Override
            public void onFailure(@NonNull Call<HomeContent> call, @NonNull Throwable t) {
                setSnackBar(contentView, t.getLocalizedMessage(), 2);
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recyclerViewGenre.getAdapter() != null) {
            ((GenreHomeAdapter) recyclerViewGenre.getAdapter()).onDestroy();
        }
    }


    private boolean isSearchBarHide = false;
    private boolean isFirstTimeBarHide = false;

    private void animateToolBar(boolean hide) {
        if (isFirstTimeBarHide) {
            if (isSearchBarHide && hide || !isSearchBarHide && !hide) {
                return;
            }
        }
        isFirstTimeBarHide = true;
        isSearchBarHide = hide;
        if (appBarLayout.getVisibility() == View.GONE) {
            appBarLayout.setVisibility(View.VISIBLE);
        }
        int moveY = hide ? -(2 * appBarLayout.getHeight()) : 0;
        appBarLayout.animate().translationY(moveY).setStartDelay(100).setDuration(300).start();
    }

    public void activityIndicator(boolean show) {
        if (show) {
            indicatorImageView.setVisibility(View.VISIBLE);
            contentView.animate().alpha(0.2f);
        } else {
            indicatorImageView.setVisibility(View.GONE);
            contentView.animate().alpha(1f);
        }
    }
}
