package com.life.android.adapters;

import static com.life.android.utils.Constants.POSITION;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.DetailsActivity;
import com.life.android.LoginActivity;
import com.life.android.R;
import com.life.android.models.CommonModels;
import com.life.android.utils.ItemAnimation;
import com.life.android.utils.PreferenceUtils;
import com.balysv.materialripple.MaterialRippleLayout;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomePageAdapter extends RecyclerView.Adapter<HomePageAdapter.HomePageViewHolder> {

    private final List<CommonModels> items;
    private final Context mContext;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private String from = "";

    public HomePageAdapter(Context context, List<CommonModels> items, String from) {
        this.items = items;
        mContext = context;
        this.from = from;
    }

    @NotNull
    @Override
    public HomePageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HomePageViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_home_view, parent, false);
        vh = new HomePageViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final HomePageViewHolder holder, final int position) {

        final CommonModels obj = items.get(position);
        holder.name.setText(obj.getTitle());
        holder.qualityTv.setText(obj.getQuality());
        holder.releaseDateTv.setText(obj.getReleaseDate());
        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(2000);
        if (from.equalsIgnoreCase("horizontal_small")) {
            holder.horizontal_small_layout.setVisibility(View.VISIBLE);
            holder.lyt_parent.setVisibility(View.GONE);
            Picasso.get().load(obj.getPosterUrl())
                    .resize(400, 200)
                    .placeholder(R.drawable.poster_placeholder_land).into(holder.horizontal_small_image);
            holder.horizontal_small_layout.setOnClickListener(v -> openDetailsScreen(obj));
            if (obj.getIsPaid().equalsIgnoreCase("1")) {
                shimmer.start(holder.horizontalSmallPremiumView);
                if (obj.getVideo_view_type() != null) {
                    if (obj.getVideo_view_type().equals("Pay and Watch")) {
                        holder.horizontalSmallPremiumView.setText(mContext.getString(R.string.pay_and_watch));
                        holder.horizontalSmallPremiumView.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.colorPayWatch));
                    } else {
                        holder.horizontalSmallPremiumView.setText(mContext.getText(R.string.premium));
                        holder.horizontalSmallPremiumView.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.colorPremium));
                    }
                }
                holder.horizontalSmallPremiumView.setVisibility(View.VISIBLE);

            } else {
                holder.horizontalSmallPremiumView.setVisibility(View.GONE);
            }
        } else {
            holder.horizontal_small_layout.setVisibility(View.GONE);
            holder.lyt_parent.setVisibility(View.VISIBLE);
            Picasso.get().load(obj.getImageUrl())
                    .resize(250, 450)
                    .placeholder(R.drawable.poster_placeholder).into(holder.image);
            if (obj.getIsPaid().equalsIgnoreCase("1")) {
                if (obj.getVideo_view_type() != null) {
                    if (obj.getVideo_view_type().equals("Pay and Watch")) {
                        holder.cardHomePremiumView.setText(mContext.getString(R.string.pay_and_watch));
                        holder.cardHomePremiumView.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.colorPayWatch));
                    } else {
                        holder.cardHomePremiumView.setText(mContext.getText(R.string.premium));
                        holder.cardHomePremiumView.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.colorPremium));
                    }
                }
                holder.cardHomePremiumView.setVisibility(View.VISIBLE);
                shimmer.start(holder.cardHomePremiumView);
            } else {
                holder.cardHomePremiumView.setVisibility(View.GONE);
            }
        }

        if (obj.continue_watch_minutes != null) {
            holder.cardHomePremiumView.setVisibility(View.GONE);
            holder.horizontalSmallPremiumView.setVisibility(View.GONE);
        }

        holder.lyt_parent.setOnClickListener(v -> openDetailsScreen(obj));

        setAnimation(holder.itemView, position);
    }

    private void openDetailsScreen(CommonModels obj) {
        if (obj.continue_watch_minutes != null) {
            openDetailScreenWithContinueWatching(obj);
        } else if (PreferenceUtils.isMandatoryLogin(mContext)) {
            if (PreferenceUtils.isLoggedIn(mContext)) {
                Intent intent = new Intent(mContext, DetailsActivity.class);
                intent.putExtra("vType", obj.getVideoType());
                intent.putExtra("id", obj.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            } else {
                mContext.startActivity(new Intent(mContext, LoginActivity.class));
            }
        } else {
            Intent intent = new Intent(mContext, DetailsActivity.class);
            intent.putExtra("vType", obj.getVideoType());
            intent.putExtra("id", obj.getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
        }
        ((Activity) mContext).overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    private void openDetailScreenWithContinueWatching(CommonModels model) {
        Intent intent = new Intent(mContext, DetailsActivity.class);
        intent.putExtra("vType", model.getVideoType());
        intent.putExtra("id", model.getId());
        intent.putExtra(POSITION, model.continue_watch_minutes.last_watched_at);
        mContext.startActivity(intent);
        ((FragmentActivity) mContext).overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class HomePageViewHolder extends RecyclerView.ViewHolder {

        public ImageView image, horizontal_small_image;
        public TextView name, qualityTv, releaseDateTv;
        public MaterialRippleLayout lyt_parent, horizontal_small_layout;
        private ShimmerTextView horizontalSmallPremiumView, cardHomePremiumView;

        public HomePageViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            horizontal_small_image = v.findViewById(R.id.horizontal_image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            horizontal_small_layout = v.findViewById(R.id.horizontal_small_layout);
            qualityTv = v.findViewById(R.id.quality_tv);
            releaseDateTv = v.findViewById(R.id.release_date_tv);
            cardHomePremiumView = v.findViewById(R.id.card_home_premium_view);
            horizontalSmallPremiumView = v.findViewById(R.id.horizontal_small_premium_view);
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

}
