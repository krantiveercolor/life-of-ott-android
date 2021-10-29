package com.life.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.ItemMovieActivity;
import com.life.android.R;
import com.life.android.models.CommonModels;
import com.bumptech.glide.Glide;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> {
    private Context context;
    private List<CommonModels> commonModels;
    private String type;
    private String layout;
    private int c;


    public GenreAdapter(Context context, List<CommonModels> commonModels, String type, String layout) {
        this.context = context;
        this.commonModels = commonModels;
        this.type = type;
        this.layout = layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (layout.equals("home")) {
            v = LayoutInflater.from(context).inflate(R.layout.layout_genre_item, parent,
                    false);
        } else {
            v = LayoutInflater.from(context).inflate(R.layout.layout_genre_item_2, parent,
                    false);
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final CommonModels commonModel = commonModels.get(position);
        if (commonModel != null) {
            holder.cardView.requestFocus();
            holder.nameTv.setText(commonModel.getTitle());

            Glide.with(context).load(commonModel.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.poster_placeholder)
                    .into(holder.icon);


            holder.cardView.setBackgroundResource(getColor());

            holder.nameTv.setTypeface(ResourcesCompat.getFont(context, R.font.roboto));


            holder.itemLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context, ItemMovieActivity.class);
                intent.putExtra("id", commonModel.getId());
                intent.putExtra("title", commonModel.getTitle());
                intent.putExtra("type", type);
                context.startActivity(intent);
                ((Activity) context).overridePendingTransition(R.anim.enter, R.anim.exit);
            });

        }
    }

    @Override
    public int getItemCount() {
        return commonModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv;
        ImageView icon;
        CardView cardView;
        LinearLayout itemLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.genre_name_tv);
            icon = itemView.findViewById(R.id.icon);
            cardView = itemView.findViewById(R.id.card_view);
            itemLayout = itemView.findViewById(R.id.item_layout);

        }
    }

    private int getColor() {

        int[] colorList2 = {R.drawable.gradient_1, R.drawable.gradient_2, R.drawable.gradient_3, R.drawable.gradient_4, R.drawable.gradient_5, R.drawable.gradient_6};

        if (c >= 6) {
            c = 0;
        }

        int color = colorList2[c];
        c++;

        return color;

    }

}

