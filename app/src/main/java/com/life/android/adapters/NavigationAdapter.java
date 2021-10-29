package com.life.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;
import com.life.android.models.NavigationModel;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.List;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.OriginalViewHolder> {

    private final List<NavigationModel> items;
    private final Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private final String menuStyle;

    public interface OnItemClickListener {
        void onItemClick(View view, NavigationModel obj, int position, OriginalViewHolder holder);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }


    public NavigationAdapter(Context context, List<NavigationModel> items, String menuStyle) {
        this.items = items;
        mContext = context;
        this.menuStyle = menuStyle;
    }


    @NonNull
    @Override
    public NavigationAdapter.OriginalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NavigationAdapter.OriginalViewHolder vh;
        View v;
        if (menuStyle.equals("grid")) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_nav_view, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_nav_view_2, parent, false);
        }
        vh = new NavigationAdapter.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull NavigationAdapter.OriginalViewHolder holder, int position) {

        NavigationModel obj = items.get(position);

        if (position == 0) {
            holder.name.setTextColor(mContext.getResources().getColor(R.color.white));
        } else {
            holder.name.setTextColor(mContext.getResources().getColor(R.color.default_text));
        }
        holder.name.setText(obj.getTitle());
        holder.image.setImageResource(getImageId(mContext, obj.getImg()));

        if (menuStyle.equals("grid")) {
            holder.cardView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, items.get(position), position, holder);
                    onValidateItemAfterClick(position);
                }
            });
        } else {
            holder.rowLayout.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, items.get(position), position, holder);
                    onValidateItemAfterClick(position);
                }
            });
        }

        if (position == 3) {
            holder.newView.setVisibility(View.VISIBLE);
            YoYo.with(Techniques.Tada)
                    .duration(1000).repeat(-1).playOn(holder.newView);
        } else holder.newView.setVisibility(View.GONE);

    }

    private void onValidateItemAfterClick(int pos) {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).is_selected = false;
        }
        items.get(pos).is_selected = true;
        notifyItemRangeChanged(0, items.size());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public TextView name, newView;
        public CardView cardView;
        public LinearLayout rowLayout;

        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            name = v.findViewById(R.id.name);
            newView = v.findViewById(R.id.new_view);

            if (menuStyle.equals("grid")) {
                cardView = v.findViewById(R.id.card_view_home);
            } else {
                rowLayout = v.findViewById(R.id.rowLayout);
            }
        }

    }

    public static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }
}
