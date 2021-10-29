package com.life.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.DetailsActivity;
import com.life.android.LoginActivity;
import com.life.android.R;
import com.life.android.models.CommonModels;
import com.life.android.utils.PreferenceUtils;
import com.bumptech.glide.Glide;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.ArrayList;

public class WebSeriesAdapter extends RecyclerView.Adapter<WebSeriesAdapter.WebSeriesViewHolder> {
    private Context context;
    private ArrayList<CommonModels> commonModelsList = new ArrayList<>();
    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    public WebSeriesAdapter(Context context, ArrayList<CommonModels> commonModelsList) {
        this.context = context;
        this.commonModelsList = commonModelsList;
    }

    @NonNull
    @Override
    public WebSeriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_web_series, parent, false);
        return new WebSeriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WebSeriesViewHolder holder, int position) {
        holder.bindData(commonModelsList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return commonModelsList.size();
    }

    public class WebSeriesViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ShimmerTextView webSeriesPremiumView;

        public WebSeriesViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.web_series_img_view);
            webSeriesPremiumView = itemView.findViewById(R.id.webSeries_premium_view);

            itemView.setOnClickListener(v -> {
                if (PreferenceUtils.isMandatoryLogin(context)) {
                    if (PreferenceUtils.isLoggedIn(context)) {
                        goToDetailsActivity(commonModelsList.get(getAbsoluteAdapterPosition()));
                    } else {
                        context.startActivity(new Intent(context, LoginActivity.class));
                        ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
                    }
                } else {
                    goToDetailsActivity(commonModelsList.get(getAbsoluteAdapterPosition()));
                }
            });
        }

        private void goToDetailsActivity(CommonModels obj) {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra("vType", obj.getVideoType());
            intent.putExtra("id", obj.getId());
            context.startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
        }

        public void bindData(CommonModels commonModels, int position) {
            Glide.with(context).load(commonModels.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.poster_placeholder_land)
                    .into(imageView);

            Shimmer shimmer = new Shimmer();
            shimmer.setDuration(2000);
            if (commonModels.getIsPaid().equalsIgnoreCase("1")) {
                shimmer.start(webSeriesPremiumView);
                webSeriesPremiumView.setVisibility(View.VISIBLE);
            } else {
                webSeriesPremiumView.setVisibility(View.GONE);
            }
        }
    }

}
