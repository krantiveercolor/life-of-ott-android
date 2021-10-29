package com.life.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;

import java.util.List;

public class SeasonsAdapter extends RecyclerView.Adapter<SeasonsAdapter.SeasonsViewHolder> {
    private Context context;
    private List<String> seasonsList;
    private int selectedPos = 0;

    public SeasonsAdapter(Context context, List<String> seasonsList) {
        this.context = context;
        this.seasonsList = seasonsList;
    }

    public interface SeasonsAdapterListener {
        void onSeasonItemClick(int position);
    }

    private SeasonsAdapterListener seasonsAdapterListener;

    public void setSeasonsAdapterListener(SeasonsAdapterListener seasonsAdapterListener) {
        this.seasonsAdapterListener = seasonsAdapterListener;
    }

    @NonNull
    @Override
    public SeasonsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_season, parent, false);
        return new SeasonsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonsViewHolder holder, int position) {
        holder.bindData(seasonsList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return seasonsList.size();
    }

    public class SeasonsViewHolder extends RecyclerView.ViewHolder {

        private final TextView itemAddCashTextView;
        private final LinearLayout rootLayout;

        public SeasonsViewHolder(@NonNull View itemView) {
            super(itemView);
            itemAddCashTextView = itemView.findViewById(R.id.item_add_cash_text_view);
            rootLayout = itemView.findViewById(R.id.item_add_cash_root_layout);

            itemView.setOnClickListener(view -> {
                selectedPos = getAbsoluteAdapterPosition();
                notifyDataSetChanged();
                if (seasonsAdapterListener != null)
                    seasonsAdapterListener.onSeasonItemClick(selectedPos);
            });
        }

        public void bindData(String seasonName, int position) {
            itemAddCashTextView.setText(seasonName);
            if (position == selectedPos) {
                itemAddCashTextView.setTextColor(ContextCompat.getColor(context, R.color.colorGold));
                rootLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.curved_gold_stroke_bg));
            } else {
                itemAddCashTextView.setTextColor(ContextCompat.getColor(context, R.color.grey_40));
                rootLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.curved_gold_stroke_grey_bg));
            }
        }
    }
}
