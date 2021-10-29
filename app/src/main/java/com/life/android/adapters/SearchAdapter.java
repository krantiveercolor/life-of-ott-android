package com.life.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.life.android.R;
import com.life.android.network.model.CommonModel;
import com.life.android.utils.ItemAnimation;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    List<CommonModel> commonModels;
    Context context;

    private int lastPosition = -1;
    private boolean on_attach = true;
    private int animation_type = 2;

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public SearchAdapter(List<CommonModel> commonModels, Context context) {
        this.commonModels = commonModels;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.card_home_view, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CommonModel commonModel = commonModels.get(position);

        if (commonModel != null) {

            holder.name.setText(commonModel.getTitle());
            holder.qualityTv.setText(commonModel.getVideoQuality());
            holder.releaseDateTv.setText(commonModel.getRelease());

            Glide.with(context).load(commonModel.getThumbnailUrl())
                    .apply(new RequestOptions().override(250, 450))
                    .apply(new RequestOptions().transform(new RoundedCorners(20)))
                    .placeholder(R.drawable.poster_placeholder)
                    .into(holder.image);


            Shimmer shimmer = new Shimmer();
            shimmer.setDuration(2000);
            if (commonModel.getIsPaid() != null && commonModel.getIsPaid().equalsIgnoreCase("1")) {
                shimmer.start(holder.premiumView);
                holder.premiumView.setVisibility(View.VISIBLE);
            } else {
                holder.premiumView.setVisibility(View.GONE);
            }

        }

        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return commonModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public ImageView image;
        public TextView name, qualityTv, releaseDateTv;
        public MaterialRippleLayout lyt_parent;
        public ShimmerTextView premiumView;

        public ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            qualityTv = v.findViewById(R.id.quality_tv);
            releaseDateTv = v.findViewById(R.id.release_date_tv);
            premiumView = v.findViewById(R.id.card_home_premium_view);


            lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(commonModels.get(getAdapterPosition()));
                    }

                }
            });

        }
    }

    public interface OnItemClickListener {

        void onItemClick(CommonModel commonModel);

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
