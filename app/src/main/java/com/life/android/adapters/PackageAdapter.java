package com.life.android.adapters;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;
import com.life.android.network.model.Package;
import com.life.android.utils.Constants;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private Context context;
    private List<Package> packageList;
    private int c;
    private OnItemClickListener itemClickListener;
    private String currency, selectedCountry = "";

    public PackageAdapter(Context context, List<Package> packageList, String currency) {
        this.context = context;
        this.packageList = packageList;
        this.currency = currency;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.layout_package_item_2, parent,
                false);

        SharedPreferences sharedPreferences = context.getSharedPreferences("push", MODE_PRIVATE);
        selectedCountry = sharedPreferences.getString("country", "");
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Package pac = packageList.get(position);
        if (pac != null) {
            if (selectedCountry.equals(Constants.BANGLA)) {
                holder.packagePrice.setText(currency + pac.getPrice());
            } else {
                holder.packagePrice.setText("$" + pac.getUsdPrice());
            }

            holder.packageName.setText(pac.getName());
            holder.packageValidity.setText("Valid for " + pac.getDay() + " days");
            Shimmer shimmer = new Shimmer();
            shimmer.setDuration(2000);
            shimmer.start(holder.packagePrice);
        }

    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView packageName, packageValidity;
        ShimmerTextView packagePrice;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            packagePrice = itemView.findViewById(R.id.package_price);
            packageName = itemView.findViewById(R.id.package_name);
            packageValidity = itemView.findViewById(R.id.package_validity);
            itemView.setOnClickListener(view -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(packageList.get(getAdapterPosition()));
                }

            });


        }
    }

    private int getColor() {

        int colorList[] = {R.color.red_400, R.color.blue_400, R.color.indigo_400, R.color.orange_400, R.color.light_green_400, R.color.blue_grey_400};
        //int colorList2[] = {R.drawable.gradient_1 ,R.drawable.gradient_2,R.drawable.gradient_3,R.drawable.gradient_4,R.drawable.gradient_5,R.drawable.gradient_6};

        if (c >= 6) {
            c = 0;
        }

        int color = colorList[c];
        c++;

        return color;

    }

    public interface OnItemClickListener {
        void onItemClick(Package pac);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
