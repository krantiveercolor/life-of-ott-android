package com.life.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.life.android.adapters.CommonGridAdapter;
import com.life.android.models.CommonModels;
import com.life.android.models.home_content.Video;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.MovieApi;
import com.life.android.utils.NetworkInst;
import com.life.android.utils.RtlUtils;
import com.life.android.utils.SpacingItemDecoration;
import com.life.android.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ItemMovieActivity extends AppCompatActivity {
    public static final String INTENT_TYPE_STAR = "star";
    public static final String INTENT_TYPE_GENRE = "genre";
    public static final String INTENT_TYPE_COUNTRY = "star";

    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;
    private CommonGridAdapter mAdapter;
    private List<CommonModels> list = new ArrayList<>();

    private boolean isLoading = false;
    private ProgressBar progressBar;
    private int pageCount = 1;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String id = "", type = "";
    private CoordinatorLayout coordinatorLayout;
    private TextView tvNoItem;
    private RelativeLayout adView;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (!isDark) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.black_window_light));
        }

        setSupportActionBar(toolbar);

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "movie_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adView = findViewById(R.id.adView);
        coordinatorLayout = findViewById(R.id.coordinator_lyt);
        progressBar = findViewById(R.id.item_progress_bar);
        shimmerFrameLayout = findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        tvNoItem = findViewById(R.id.tv_noitem);

        //----movie's recycler view-----------------
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration
                (new SpacingItemDecoration(3, Tools.dpToPx(this, 8), true));
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        mAdapter = new CommonGridAdapter(this, list);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    pageCount = pageCount + 1;
                    isLoading = true;
                    progressBar.setVisibility(View.VISIBLE);
                    if (id == null) {
                        getMovie(pageCount);
                    } else if (type.equals("country")) {
                        getMovieByCountryId(id, pageCount);
                    } else if (type.equalsIgnoreCase(INTENT_TYPE_STAR)) {
                        getMovieByStarId(id, pageCount);
                    } else {
                        getMovieByGenreId(id, pageCount);
                    }
                }
            }
        });

        id = getIntent().getStringExtra("id");
        type = getIntent().getStringExtra("type");

        if (new NetworkInst(this).isNetworkAvailable()) {
            initData();
        } else {
            tvNoItem.setText(getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }


        swipeRefreshLayout.setOnRefreshListener(() -> {

            coordinatorLayout.setVisibility(View.GONE);

            pageCount = 1;

            list.clear();
            recyclerView.removeAllViews();
            mAdapter.notifyDataSetChanged();

            if (new NetworkInst(ItemMovieActivity.this).isNetworkAvailable()) {
                initData();
            } else {
                tvNoItem.setText(getString(R.string.no_internet));
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                coordinatorLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    private void getMovieByStarId(String id, int pageCount) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);
        Call<List<Video>> call = api.getMovieByStarId(AppConfig.API_KEY, id, pageCount);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, Response<List<Video>> response) {
                if (response.code() == 200) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    } else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }

                    for (int i = 0; i < response.body().size(); i++) {
                        Video video = response.body().get(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setQuality(video.getVideoQuality());
                        models.setReleaseDate(video.getRelease());
                        models.setIsPaid(video.getIsPaid());
                        if (video.getIsTvseries().equals("1")) {
                            models.setVideoType("tvseries");
                        } else {
                            models.setVideoType("movie");
                        }


                        models.setId(video.getVideosId());
                        list.add(models);
                    }

                    mAdapter.notifyDataSetChanged();
                } else {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount == 1) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void initData() {
        if (id == null) {
            getMovie(pageCount);
        } else if (type.equals("country")) {
            getMovieByCountryId(id, pageCount);
        } else if (type.equalsIgnoreCase(INTENT_TYPE_STAR)) {
            getMovieByStarId(id, pageCount);
        } else {
            getMovieByGenreId(id, pageCount);
        }

    }

    private void getMovieByGenreId(String id, int pageNum) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);
        Call<List<Video>> call = api.getMovieByGenreId(AppConfig.API_KEY, id, pageNum);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, retrofit2.Response<List<Video>> response) {
                if (response.code() == 200) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    } else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }

                    for (int i = 0; i < response.body().size(); i++) {
                        Video video = response.body().get(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setQuality(video.getVideoQuality());
                        models.setReleaseDate(video.getRelease());
                        models.setVideo_view_type(video.getVideo_view_type());
                        models.setIsPaid(video.getIsPaid());
                        if (video.getIsTvseries().equals("1")) {
                            models.setVideoType("tvseries");
                        } else {
                            models.setVideoType("movie");
                        }

                        models.setId(video.getVideosId());
                        list.add(models);
                    }

                    mAdapter.notifyDataSetChanged();
                } else {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount == 1) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getMovieByCountryId(String id, int pageNum) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);
        Call<List<Video>> call = api.getMovieByCountryId(AppConfig.API_KEY, id, pageNum);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, retrofit2.Response<List<Video>> response) {
                if (response.code() == 200) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    } else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }

                    for (int i = 0; i < response.body().size(); i++) {
                        Video video = response.body().get(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setQuality(video.getVideoQuality());
                        models.setReleaseDate(video.getRelease());
                        models.setIsPaid(video.getIsPaid());
                        if (video.getIsTvseries().equals("1")) {
                            models.setVideoType("tvseries");
                        } else {
                            models.setVideoType("movie");
                        }


                        models.setId(video.getVideosId());
                        list.add(models);
                    }

                    mAdapter.notifyDataSetChanged();
                } else {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount == 1) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    private void getMovie(int pageNum) {
        Retrofit retrofit = RetrofitClient.getAuthRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);

        Call<List<Video>> call = api.getMovies(AppConfig.API_KEY, pageNum, null);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, retrofit2.Response<List<Video>> response) {
                if (response.code() == 200) {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    } else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }

                    for (int i = 0; i < response.body().size(); i++) {
                        Video video = response.body().get(i);
                        CommonModels models = new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setQuality(video.getVideoQuality());
                        models.setReleaseDate(video.getRelease());
                        models.setIsPaid(video.getIsPaid());
                        if (video.getIsTvseries().equals("1")) {
                            models.setVideoType("tvseries");
                        } else {
                            models.setVideoType("movie");
                        }


                        models.setId(video.getVideosId());
                        list.add(models);
                    }

                    mAdapter.notifyDataSetChanged();
                } else {
                    isLoading = false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount == 1) {
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount == 1) {
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pop_enter, R.anim.pop_exit);
    }

}
