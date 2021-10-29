package com.life.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.DetailsActivity;
import com.life.android.LoginActivity;
import com.life.android.R;
import com.life.android.models.CommonModels;
import com.life.android.utils.PreferenceUtils;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HomePageZoomAdapter extends RecyclerView.Adapter<HomePageZoomAdapter.OriginalViewHolder> {

    private List<CommonModels> items;
    private Context ctx;

    public HomePageZoomAdapter(Context context, List<CommonModels> items) {
        this.items = items;
        ctx = context;
    }

    @Override
    public HomePageZoomAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HomePageZoomAdapter.OriginalViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_home_zoom_view, parent, false);
        vh = new HomePageZoomAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final HomePageZoomAdapter.OriginalViewHolder holder, final int position) {

        final CommonModels obj = items.get(position % items.size());
        Picasso.get().load(obj.getImageUrl())
                .resize(350, 550)
                .placeholder(R.drawable.poster_placeholder).into(holder.image);
        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(2000);
        if (obj.getIsPaid().equalsIgnoreCase("1")) {
            shimmer.start(holder.premiumView);
            holder.premiumView.setVisibility(View.VISIBLE);
            if (obj.getVideo_view_type() != null) {
                if (obj.getVideo_view_type().equalsIgnoreCase("Pay and Watch")) {
                    holder.premiumView.setText(ctx.getString(R.string.pay_and_watch));
                    holder.premiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorPayWatch));
                } else {
                    holder.premiumView.setText(ctx.getString(R.string.premium));
                    holder.premiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorPremium));
                }
            }
        } else {
            shimmer.start(holder.premiumView);
            holder.premiumView.setVisibility(View.GONE);
            holder.premiumView.setText(ctx.getText(R.string.free));
            holder.premiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorFree));

        }
        holder.itemView.setOnClickListener(v -> {
            if (PreferenceUtils.isMandatoryLogin(ctx)) {
                if (PreferenceUtils.isLoggedIn(ctx)) {
                    Intent intent = new Intent(ctx, DetailsActivity.class);
                    intent.putExtra("vType", obj.getVideoType());
                    intent.putExtra("id", obj.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ctx.startActivity(intent);
                    ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
                } else {
                    ctx.startActivity(new Intent(ctx, LoginActivity.class));
                    ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            } else {
                Intent intent = new Intent(ctx, DetailsActivity.class);
                intent.putExtra("vType", obj.getVideoType());
                intent.putExtra("id", obj.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ctx.startActivity(intent);
                ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
            }
        });

    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;

        private ShimmerTextView premiumView;

        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            premiumView = v.findViewById(R.id.vertical_large_premium_view);
        }
    }

}
