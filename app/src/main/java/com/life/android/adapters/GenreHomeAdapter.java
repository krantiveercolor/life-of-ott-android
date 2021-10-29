package com.life.android.adapters;

import static com.life.android.helper.CMExtenstionKt.setSnackBar;
import static java.lang.Math.abs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.viewpager2.widget.ViewPager2;

import com.life.android.AppConfig;
import com.life.android.ItemMovieActivity;
import com.life.android.R;
import com.life.android.database.DatabaseHelper;
import com.life.android.models.GenreModel;
import com.life.android.network.RetrofitClient;
import com.life.android.network.apis.ContinueWatchApi;
import com.life.android.network.model.APIResponse;
import com.life.android.utils.Constants;
import com.life.android.utils.ExtensionHelperKt;
import com.life.android.utils.HorizontalMarginItemDecoration;
import com.life.android.utils.ItemAnimation;
import com.life.android.utils.LinearLayoutManagerWithSmoothScroller;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GenreHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<GenreModel> items;
    private final Context ctx;
    private int lastPosition = -1;
    private boolean on_attach = true;
    private final Timer timer = new Timer();


    public GenreHomeAdapter(Context context, List<GenreModel> items) {
        this.items = items;
        ctx = context;
    }

    public interface GenreHomeAdapterListener {
        void onClearClick();
    }

    private GenreHomeAdapterListener genreHomeAdapterListener;

    public void setGenreHomeAdapterListener(GenreHomeAdapterListener genreHomeAdapterListener) {
        this.genreHomeAdapterListener = genreHomeAdapterListener;
    }


    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == Constants.GenreSizeTypes.VERTICAL_LARGE) {
            return new VerticalLargeViewHolder(getView(parent, R.layout.item_genre_vertical_large));
        } else if (viewType == Constants.GenreSizeTypes.VERTICAL_SMALL) {
            return new VerticalSmallViewHolder(getView(parent, R.layout.item_genre_vertical_small));
        } else if (viewType == Constants.GenreSizeTypes.HORIZONTAL_SMALL) {
            return new HorizontalSmallViewHolder(getView(parent, R.layout.item_genre_vertical_small));
        } else if (viewType == Constants.GenreSizeTypes.HORIZONTAL_LARGE) {
            return new HorizontalLargeViewHolder(getView(parent, R.layout.item_genre_vertical_small));
        } else if (viewType == Constants.GenreSizeTypes.CIRCLE_SMALL) {
            return new CircleSmallViewHolder(getView(parent, R.layout.item_genre_vertical_small));
        } else {
            return new VerticalSmallViewHolder(getView(parent, R.layout.item_genre_vertical_small));
        }
    }

    private View getView(ViewGroup parent, int resource) {
        return LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int position) {
        final GenreModel obj = items.get(position);
        if (holder instanceof VerticalLargeViewHolder) {
            ((VerticalLargeViewHolder) holder).name.setText(obj.getName());
            HomePageZoomAdapter adapter = new HomePageZoomAdapter(ctx, obj.getList());
            ((VerticalLargeViewHolder) holder).viewPager2.setAdapter(adapter);
            ((VerticalLargeViewHolder) holder).viewPager2.setCurrentItem(1);
            ((VerticalLargeViewHolder) holder).viewPager2.setOffscreenPageLimit(1);
            int nextItemVisiblePx = (int) ctx.getResources().getDimension(R.dimen.viewpager_next_item_visible);
            int currentItemHorizontalMarginPx = (int) ctx.getResources().getDimension(R.dimen.viewpager_current_item_horizontal_margin);
            int pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx;
            ViewPager2.PageTransformer pageTransformer = (page, position1) -> {
                page.setTranslationX(-pageTranslationX * position1);
                page.setScaleY(1 - (0.07f * abs(position1)));
            };
            ((VerticalLargeViewHolder) holder).viewPager2.setPageTransformer(pageTransformer);
            HorizontalMarginItemDecoration itemDecoration = new HorizontalMarginItemDecoration(ctx, R.dimen.viewpager_current_item_horizontal_margin);
            ((VerticalLargeViewHolder) holder).viewPager2.addItemDecoration(itemDecoration);
            RecyclerView recyclerView = (RecyclerView) ((VerticalLargeViewHolder) holder).viewPager2.getChildAt(0);
            recyclerView.setNestedScrollingEnabled(false);
            ExtensionHelperKt.enforceSingleScrollDirection(recyclerView);
            ((VerticalLargeViewHolder) holder).btnMore.setOnClickListener(v -> openDetailsScreen(obj));

        } else if (holder instanceof VerticalSmallViewHolder) {
            ((VerticalSmallViewHolder) holder).name.setText(obj.getName());
            HomePageAdapter adapter = new HomePageAdapter(ctx, obj.getList(), "");
            ((VerticalSmallViewHolder) holder).recyclerView.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL,
                    false));
            ((VerticalSmallViewHolder) holder).recyclerView.setHasFixedSize(false);
            ((VerticalSmallViewHolder) holder).recyclerView.setAdapter(adapter);
            ((VerticalSmallViewHolder) holder).recyclerView.setNestedScrollingEnabled(true);
            ((VerticalSmallViewHolder) holder).btnMore.setOnClickListener(v -> openDetailsScreen(obj));
        } else if (holder instanceof HorizontalSmallViewHolder) {
            ((HorizontalSmallViewHolder) holder).name.setText(obj.getName());
            HomePageAdapter adapter = new HomePageAdapter(ctx, obj.getList(), "horizontal_small");
            ((HorizontalSmallViewHolder) holder).recyclerView.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL,
                    false));
            ((HorizontalSmallViewHolder) holder).recyclerView.setHasFixedSize(false);
            ((HorizontalSmallViewHolder) holder).recyclerView.setAdapter(adapter);
            ((HorizontalSmallViewHolder) holder).recyclerView.setNestedScrollingEnabled(false);
            if (obj.getId().equalsIgnoreCase("0")) {
                ((HorizontalSmallViewHolder) holder).btnMore.setText(ctx.getString(R.string.clear));
                ((HorizontalSmallViewHolder) holder).btnMore.setOnClickListener(v -> clearContinueWatch(position, v));
            } else {
                ((HorizontalSmallViewHolder) holder).btnMore.setText(ctx.getString(R.string.see_more));
                ((HorizontalSmallViewHolder) holder).btnMore.setOnClickListener(v -> openDetailsScreen(obj));
            }

        } else if (holder instanceof HorizontalLargeViewHolder) {
            ((HorizontalLargeViewHolder) holder).name.setText(obj.getName());
            HorizontalLargeAdapter adapter = new HorizontalLargeAdapter(ctx, obj.getList());
            LinearLayoutManagerWithSmoothScroller linearLayoutManager = new LinearLayoutManagerWithSmoothScroller(ctx, LinearLayoutManager.HORIZONTAL,
                    false);
            ((HorizontalLargeViewHolder) holder).recyclerView.setLayoutManager(linearLayoutManager);
            ((HorizontalLargeViewHolder) holder).recyclerView.setHasFixedSize(false);
            ((HorizontalLargeViewHolder) holder).recyclerView.setAdapter(adapter);
            SnapHelper snapHelper = new PagerSnapHelper();
            ((HorizontalLargeViewHolder) holder).recyclerView.setOnFlingListener(null);
            snapHelper.attachToRecyclerView(((HorizontalLargeViewHolder) holder).recyclerView);
            ((HorizontalLargeViewHolder) holder).recyclerView.setNestedScrollingEnabled(false);
            ((HorizontalLargeViewHolder) holder).btnMore.setOnClickListener(v -> openDetailsScreen(obj));
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    int pos = linearLayoutManager.findFirstVisibleItemPosition();
                    ((HorizontalLargeViewHolder) holder).recyclerView.smoothScrollToPosition(++pos);
                }
            }, 5000, 5000);

        }

        setAnimation(holder.itemView, position);
    }

    private void openDetailsScreen(GenreModel obj) {
        new Thread(() -> {
            Intent intent = new Intent(ctx, ItemMovieActivity.class);
            intent.putExtra("id", obj.getId());
            intent.putExtra("title", obj.getName());
            intent.putExtra("type", "genre");
            ctx.startActivity(intent);
            ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
        }).start();
    }

    private void clearContinueWatch(int position, View view) {

        String userId = new DatabaseHelper(ctx).getUserData().getUser_id();

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ContinueWatchApi api = retrofit.create(ContinueWatchApi.class);

        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", userId);

        Call<APIResponse<Object>> call = api.clearContinueWatch(AppConfig.API_KEY, params);

        call.enqueue(new Callback<APIResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<APIResponse<Object>> call, @NonNull Response<APIResponse<Object>> response) {
                try {
                    if (response.isSuccessful() && response.code() == 200 && response.body() != null) {
                        if (response.body().status.equals("valid")) {
                            setSnackBar(view, response.body().message, 1);
                            items.remove(position);
                            notifyItemRemoved(position);
                        } else {
                            setSnackBar(view, response.body().message, 2);
                        }
                    } else {
                        setSnackBar(view, "Something went wrong, Please try again!", 2);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<APIResponse<Object>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class VerticalLargeViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        ViewPager2 viewPager2;
        Button btnMore;

        public VerticalLargeViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            viewPager2 = v.findViewById(R.id.recyclerView);
            btnMore = v.findViewById(R.id.btn_more);
        }
    }

    public static class VerticalSmallViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        RecyclerView recyclerView;
        Button btnMore;

        public VerticalSmallViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            recyclerView = v.findViewById(R.id.recyclerView);
            btnMore = v.findViewById(R.id.btn_more);
        }
    }

    public static class HorizontalSmallViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        RecyclerView recyclerView;
        Button btnMore;

        public HorizontalSmallViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            recyclerView = v.findViewById(R.id.recyclerView);
            btnMore = v.findViewById(R.id.btn_more);
        }
    }

    public static class HorizontalLargeViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        RecyclerView recyclerView;
        Button btnMore;

        public HorizontalLargeViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            recyclerView = v.findViewById(R.id.recyclerView);
            btnMore = v.findViewById(R.id.btn_more);
        }
    }

    public static class CircleSmallViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        RecyclerView recyclerView;
        Button btnMore;

        public CircleSmallViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            recyclerView = v.findViewById(R.id.recyclerView);
            btnMore = v.findViewById(R.id.btn_more);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }

        });

        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            int animation_type = 2;
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }

    public void onDestroy() {
        timer.cancel();
    }
}
