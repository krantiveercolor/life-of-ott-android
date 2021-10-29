package com.life.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;

public class TicketInfoAdapter extends RecyclerView.Adapter<TicketInfoAdapter.TicketInfoViewHolder> {
    private Context context;
    private String[] ticketInfoArray;

    public TicketInfoAdapter(Context context) {
        this.context = context;
        this.ticketInfoArray = context.getResources().getStringArray(R.array.ticket_info_array);
    }

    @NonNull
    @Override
    public TicketInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_info, parent, false);
        return new TicketInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketInfoViewHolder holder, int position) {
        holder.bindData(ticketInfoArray[position]);
    }

    @Override
    public int getItemCount() {
        return ticketInfoArray.length;
    }

    public class TicketInfoViewHolder extends RecyclerView.ViewHolder {

        private TextView infoTextView;

        public TicketInfoViewHolder(@NonNull View itemView) {
            super(itemView);
            infoTextView = itemView.findViewById(R.id.info_text_view);
        }

        public void bindData(String info) {
            infoTextView.setText(info);
        }
    }
}
