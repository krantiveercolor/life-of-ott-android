package com.life.android.adapters;

import static com.life.android.utils.Constants.CATEGORY_TYPE;
import static com.life.android.utils.Constants.CONTENT_ID;
import static com.life.android.utils.Constants.CONTENT_TITLE;
import static com.life.android.utils.Constants.IMAGE_URL;
import static com.life.android.utils.Constants.POSITION;
import static com.life.android.utils.Constants.SERVER_TYPE;
import static com.life.android.utils.Constants.STREAM_URL;

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
import com.life.android.R;
import com.life.android.database.continueWatching.ContinueWatchingModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ContinueWatchingAdapter extends RecyclerView.Adapter<ContinueWatchingAdapter.ContinueWatchingViewHolder> {

    private final Context context;
    private final List<ContinueWatchingModel> list;

    public ContinueWatchingAdapter(Context context, List<ContinueWatchingModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ContinueWatchingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_continue_watching, parent, false);
        return new ContinueWatchingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContinueWatchingViewHolder holder, int position) {
        final ContinueWatchingModel model = list.get(position);
        if (model != null) {
            Glide.with(context).load(model.getImgUrl())
                    .apply(new RequestOptions().override(250, 250).transform(new RoundedCorners(15)))
                    .placeholder(R.drawable.poster_placeholder_square).into(holder.posterIV);
            holder.textView.setText(model.getName());
            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra(CONTENT_ID, model.getContentId());
                intent.putExtra(CONTENT_TITLE, model.getName());
                intent.putExtra(IMAGE_URL, model.getImgUrl());
                intent.putExtra(STREAM_URL, model.getStreamUrl());
                intent.putExtra(SERVER_TYPE, model.getvType());
                intent.putExtra(CATEGORY_TYPE, model.getType());
                intent.putExtra(POSITION, model.getPosition());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ContinueWatchingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView posterIV;
        private final TextView textView;

        public ContinueWatchingViewHolder(@NonNull View itemView) {
            super(itemView);
            posterIV = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.movieNameView);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
