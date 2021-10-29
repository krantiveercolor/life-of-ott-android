package com.life.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.DetailsActivity;
import com.life.android.LoginActivity;
import com.life.android.R;
import com.life.android.helper.IConstants;
import com.life.android.models.CommonModels;
import com.life.android.utils.Constants;
import com.life.android.utils.PreferenceUtils;
import com.bumptech.glide.Glide;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.ArrayList;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

    private ArrayList<CommonModels> items = new ArrayList<>();
    private Context ctx;
    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;
    private String from;

    public MoviesAdapter(ArrayList<CommonModels> items, Context ctx, String from) {
        this.items = items;
        this.ctx = ctx;
        this.from = from;
    }

    @NonNull
    @Override
    public MoviesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_movie, parent, false);
        return new MoviesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesViewHolder holder, int position) {
        holder.bindData(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class MoviesViewHolder extends RecyclerView.ViewHolder {

        private ImageView movieImgView;
        private ShimmerTextView moviesPremiumView;

        public MoviesViewHolder(@NonNull View itemView) {
            super(itemView);
            movieImgView = itemView.findViewById(R.id.movie_img_view);
            moviesPremiumView = itemView.findViewById(R.id.movies_premium_view);

            itemView.setOnClickListener(v -> {
                if (PreferenceUtils.isMandatoryLogin(ctx)) {
                    if (PreferenceUtils.isLoggedIn(ctx)) {
                        goToDetailsActivity(items.get(getBindingAdapterPosition()));
                    } else {
                        ctx.startActivity(new Intent(ctx, LoginActivity.class));
                        ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                } else {
                    if (getBindingAdapterPosition() < items.size()) {
                        goToDetailsActivity(items.get(getBindingAdapterPosition()));
                    }
                }
            });
        }

        private void goToDetailsActivity(CommonModels obj) {
            Intent intent = new Intent(ctx, DetailsActivity.class);
            intent.putExtra("vType", obj.getVideoType());
            intent.putExtra("id", obj.getId());
            if (from.equalsIgnoreCase(Constants.EXCLUSIVE_FRAGMENT))
                intent.putExtra("opened_from", Constants.EXCLUSIVE_FRAGMENT);
            ctx.startActivity(intent);
            ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
        }

        public void bindData(CommonModels commonModels) {
            Glide.with(ctx).load(commonModels.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.poster_placeholder)
                    .into(movieImgView);

         /*   Picasso.get().load(commonModels.getImageUrl())
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.poster_placeholder).into(movieImgView);*/

            Shimmer shimmer = new Shimmer();
            shimmer.setDuration(2000);
            if (commonModels.getIsPaid().equalsIgnoreCase("1")) {
                shimmer.start(moviesPremiumView);
                moviesPremiumView.setVisibility(View.VISIBLE);
                if (commonModels.getVideo_view_type() != null) {
                    moviesPremiumView.setText(ctx.getString(R.string.pay_and_watch));
                    moviesPremiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorPayWatch));
                } else {
                    moviesPremiumView.setText(ctx.getString(R.string.premium));
                    moviesPremiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorPremium));
                }
            } else {
                moviesPremiumView.setVisibility(View.GONE);
            }

            if (from.equals(IConstants.VideoTypeMovieAPI.payAndWatch)) {
                moviesPremiumView.setText(IConstants.VideoTypeMovieAPI.payAndWatch);
            }
        }
    }

}
