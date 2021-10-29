package com.life.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;
import com.life.android.models.WalletHistoryModel;

import java.util.List;

public class WalletHistoryAdapter extends RecyclerView.Adapter<WalletHistoryAdapter.WalletHistoryViewHolder> {

    private Context context;
    private List<WalletHistoryModel> walletHistoryModelList;
    private String currency;

    public WalletHistoryAdapter(Context context, List<WalletHistoryModel> walletHistoryModelList, String currency) {
        this.context = context;
        this.walletHistoryModelList = walletHistoryModelList;
        this.currency = currency;
    }

    @NonNull
    @Override
    public WalletHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wallet_history, parent, false);
        return new WalletHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalletHistoryViewHolder holder, int position) {
        holder.bindData(walletHistoryModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return walletHistoryModelList.size();
    }

    public class WalletHistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView historyOrderDescView;
        private final TextView historyDateView;
        private final TextView historyCashView;
        private final ImageView arrowView;

        public WalletHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            historyOrderDescView = itemView.findViewById(R.id.history_desc_view);
            historyDateView = itemView.findViewById(R.id.history_order_date_view);
            historyCashView = itemView.findViewById(R.id.history_cash_view);
            arrowView = itemView.findViewById(R.id.history_img_view);
        }

        public void bindData(WalletHistoryModel walletHistoryModel) {
            historyOrderDescView.setText(String.format("#%s %s", walletHistoryModel.id, walletHistoryModel.remark));
            historyDateView.setText(walletHistoryModel.payment_timestamp);
            if (walletHistoryModel.transaction_type.equalsIgnoreCase("credit")) {
                historyCashView.setTextColor(ContextCompat.getColor(context, R.color.green_500));
                arrowView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.up_arrow));
                if (walletHistoryModel.payment_status.equalsIgnoreCase("Pending")) {
                    arrowView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_forward_10_white));
                    historyCashView.setTextColor(ContextCompat.getColor(context, R.color.red_600));
                    String messageText = walletHistoryModel.payment_timestamp + "[" + walletHistoryModel.payment_status + "]";
                    historyDateView.setText(messageText);
                }
            } else {
                arrowView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.down_arrow));
                historyCashView.setTextColor(ContextCompat.getColor(context, R.color.red_600));
            }
            historyCashView.setText(String.format("%s%s", currency, walletHistoryModel.amount));
        }
    }
}
