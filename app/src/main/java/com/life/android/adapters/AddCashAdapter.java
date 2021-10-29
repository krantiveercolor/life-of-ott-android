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

public class AddCashAdapter extends RecyclerView.Adapter<AddCashAdapter.AddCashViewHolder> {
    private int[] cashArray;
    private Context context;
    private String currency;

    public AddCashAdapter(Context context, int[] cashArray, String currency) {
        this.context = context;
        this.cashArray = cashArray;
        this.currency = currency;
    }

    private int selectedPos = 0;

    public int getSelectedPos() {
        return selectedPos;
    }

    public interface AddCashAdapterListener {
        void onAddCashItemClick(int pos);
    }

    private AddCashAdapterListener addCashAdapterListener;

    public void setAddCashAdapterListener(AddCashAdapterListener addCashAdapterListener) {
        this.addCashAdapterListener = addCashAdapterListener;
    }

    @NonNull
    @Override
    public AddCashViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_add_cash, parent, false);
        return new AddCashViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddCashViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        return cashArray.length;
    }

    public class AddCashViewHolder extends RecyclerView.ViewHolder {

        private final TextView itemAddCashTextView;
        private final LinearLayout rootLayout;

        public AddCashViewHolder(@NonNull View itemView) {
            super(itemView);
            itemAddCashTextView = itemView.findViewById(R.id.item_add_cash_text_view);
            rootLayout = itemView.findViewById(R.id.item_add_cash_root_layout);
            itemView.setOnClickListener(view -> {
                selectedPos = getAbsoluteAdapterPosition();
                notifyDataSetChanged();
                addCashAdapterListener.onAddCashItemClick(selectedPos);
            });
        }

        public void bindData(int position) {
            itemAddCashTextView.setText(String.format("%s %d", currency, cashArray[position]));
            if (position == selectedPos) {
                itemAddCashTextView.setTextColor(ContextCompat.getColor(context, R.color.colourBlue));
                rootLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.curved_gold_bg));
            } else {
                itemAddCashTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
                rootLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.curved_gold_stroke_bg));
            }
        }
    }
}
