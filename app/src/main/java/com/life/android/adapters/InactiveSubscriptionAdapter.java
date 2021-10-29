package com.life.android.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;
import com.life.android.network.model.ActiveSubscription;

import java.util.List;

public class InactiveSubscriptionAdapter extends RecyclerView.Adapter<InactiveSubscriptionAdapter.ViewHolder> {

    private List<ActiveSubscription> inactiveSubscriptions;
    private Context context;

    public InactiveSubscriptionAdapter(List<ActiveSubscription> inactiveSubscriptions, Context context) {
        this.inactiveSubscriptions = inactiveSubscriptions;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.subscription_layout, parent,
                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ActiveSubscription inactiveSubscription = inactiveSubscriptions.get(position);
        if (inactiveSubscription != null) {
            holder.serialNoTv.setText(String.valueOf(position + 1));
            holder.planTv.setText(inactiveSubscription.getPlanTitle());
            holder.purchaseDateTv.setText(inactiveSubscription.getPaymentTimestamp());
            holder.fromTv.setText(inactiveSubscription.getStartDate());
            holder.toTv.setText(inactiveSubscription.getExpireDate());
            if (inactiveSubscription.getStatus().equalsIgnoreCase("2")) {
                holder.statusTv.setText(context.getString(R.string.active));
            } else
                holder.statusTv.setText(context.getString(R.string.cancelled));
        }
    }

    @Override
    public int getItemCount() {
        return inactiveSubscriptions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView serialNoTv, planTv, purchaseDateTv, fromTv, toTv, actionTv, statusTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            serialNoTv = itemView.findViewById(R.id.serial_no_tv);
            planTv = itemView.findViewById(R.id.plan_tv);
            purchaseDateTv = itemView.findViewById(R.id.purchase_date_tv);
            fromTv = itemView.findViewById(R.id.from_tv);
            toTv = itemView.findViewById(R.id.to_tv);
            statusTv = itemView.findViewById(R.id.status_tv);

        }
    }
}

