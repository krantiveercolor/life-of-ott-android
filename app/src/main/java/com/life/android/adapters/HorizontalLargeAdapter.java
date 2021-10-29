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
import com.life.android.utils.ItemAnimation;
import com.life.android.utils.PreferenceUtils;
import com.balysv.materialripple.MaterialRippleLayout;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HorizontalLargeAdapter extends RecyclerView.Adapter<HorizontalLargeAdapter.ViewHolder> {

    private List<CommonModels> items;
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public HorizontalLargeAdapter(Context context, List<CommonModels> items) {
        this.items = items;
        ctx = context;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_horizontal_large, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final CommonModels obj = items.get(position % items.size());
        Picasso.get().load(obj.getPosterUrl())
                .resize(450, 250)
                .placeholder(R.drawable.poster_placeholder_land)
                .into(holder.horizontal_large_image);
        if (obj.getIsPaid().equalsIgnoreCase("1")) {
            Shimmer shimmer = new Shimmer();
            shimmer.setDuration(2000);
            shimmer.start(holder.horizontalLargePremiumView);
            holder.horizontalLargePremiumView.setVisibility(View.VISIBLE);
            if (obj.getVideo_view_type() != null) {
                if (obj.getVideo_view_type().equals("Pay and Watch")) {
                    holder.horizontalLargePremiumView.setText(ctx.getString(R.string.pay_and_watch));
                    holder.horizontalLargePremiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorPayWatch));
                } else {
                    holder.horizontalLargePremiumView.setText(ctx.getText(R.string.premium));
                    holder.horizontalLargePremiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorPremium));
                }
            }
        } else {
             holder.horizontalLargePremiumView.setVisibility(View.GONE);
            holder.horizontalLargePremiumView.setText(ctx.getText(R.string.free));
            holder.horizontalLargePremiumView.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.colorFree));
        }

        holder.horizontal_large_layout.setOnClickListener(v -> new Thread(() -> {
            if (PreferenceUtils.isMandatoryLogin(ctx)) {
                if (PreferenceUtils.isLoggedIn(ctx)) {
                    Intent intent = new Intent(ctx, DetailsActivity.class);
                    intent.putExtra("vType", obj.getVideoType());
                    intent.putExtra("id", obj.getId());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ctx.startActivity(intent);
                } else {
                    ctx.startActivity(new Intent(ctx, LoginActivity.class));
                }
            } else {
                Intent intent = new Intent(ctx, DetailsActivity.class);
                intent.putExtra("vType", obj.getVideoType());
                intent.putExtra("id", obj.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                ctx.startActivity(intent);
            }
            ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
        }).start());

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView horizontal_large_image;
        public MaterialRippleLayout horizontal_large_layout;
        public ShimmerTextView horizontalLargePremiumView;


        public ViewHolder(View v) {
            super(v);
            horizontal_large_image = v.findViewById(R.id.horizontal_large_image);
            horizontal_large_layout = v.findViewById(R.id.horizontal_large_layout);
            horizontalLargePremiumView = v.findViewById(R.id.horizontal_large_premium_view);
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
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }

}
