package com.life.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.DetailsActivity;
import com.life.android.LoginActivity;
import com.life.android.R;
import com.life.android.models.CommonModels;
import com.life.android.utils.ItemAnimation;
import com.life.android.utils.PreferenceUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private List<CommonModels> items = new ArrayList<>();
    private Context ctx;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public FavoritesAdapter(List<CommonModels> items, Context ctx) {
        this.items = items;
        this.ctx = ctx;
    }

    public interface FavoritesAdapterClickListener {
        void onRemoveClick(int pos);
    }

    private FavoritesAdapterClickListener favoritesAdapterClickListener;

    public void setFavoritesAdapterClickListener(FavoritesAdapterClickListener favoritesAdapterClickListener) {
        this.favoritesAdapterClickListener = favoritesAdapterClickListener;
    }

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_favorite, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        holder.bindData(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class FavoritesViewHolder extends RecyclerView.ViewHolder {

        private ImageView favorite_img_view, remove_favorite_view;
        private TextView favorite_title_view, favorite_desc_view;

        public FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            favorite_img_view = itemView.findViewById(R.id.favorite_img_view);
            remove_favorite_view = itemView.findViewById(R.id.remove_favorite_view);
            favorite_title_view = itemView.findViewById(R.id.favorite_title_view);
            favorite_desc_view = itemView.findViewById(R.id.favorite_desc_view);

            remove_favorite_view.setOnClickListener(view -> {
                if (favoritesAdapterClickListener != null)
                    favoritesAdapterClickListener.onRemoveClick(getAbsoluteAdapterPosition());
            });

            favorite_img_view.setOnClickListener(v -> {
                if (PreferenceUtils.isMandatoryLogin(ctx)) {
                    if (PreferenceUtils.isLoggedIn(ctx)) {
                        goToDetailsActivity(items.get(getAbsoluteAdapterPosition()));
                    } else {
                        ctx.startActivity(new Intent(ctx, LoginActivity.class));
                    }
                } else {
                    goToDetailsActivity(items.get(getAbsoluteAdapterPosition()));
                }
            });

            favorite_title_view.setOnClickListener(v -> favorite_img_view.performClick());
            favorite_desc_view.setOnClickListener(v -> favorite_img_view.performClick());
        }

        private void goToDetailsActivity(CommonModels obj) {
            Intent intent = new Intent(ctx, DetailsActivity.class);
            intent.putExtra("vType", obj.getVideoType());
            intent.putExtra("id", obj.getId());
            ctx.startActivity(intent);
            ((Activity) ctx).overridePendingTransition(R.anim.enter, R.anim.exit);
        }

        public void bindData(CommonModels commonModels) {
            Picasso.get().load(commonModels.getImageUrl())
                    .resize(300,250)
                    .placeholder(R.drawable.poster_placeholder).into(favorite_img_view);
            favorite_title_view.setText(commonModels.getTitle());
            favorite_desc_view.setText(commonModels.getBrief());
            setAnimation(itemView, getBindingAdapterPosition());
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
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
