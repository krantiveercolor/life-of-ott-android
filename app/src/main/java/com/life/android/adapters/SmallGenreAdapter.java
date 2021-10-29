package com.life.android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;
import com.life.android.models.single_details.Genre;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SmallGenreAdapter extends RecyclerView.Adapter<SmallGenreAdapter.SmallGenreViewHolder> {

    private final List<Genre> genreList;

    public SmallGenreAdapter(List<Genre> genreList) {
        this.genreList = genreList;
    }

    @NotNull
    @Override
    public SmallGenreViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_small_genre, parent, false);
        return new SmallGenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull SmallGenreViewHolder holder, int position) {
        holder.bindData(genreList.get(position));
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    public static class SmallGenreViewHolder extends RecyclerView.ViewHolder {

        private final TextView textView;

        public SmallGenreViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.small_genre_txt_view);
        }

        public void bindData(Genre genre) {
            textView.setText(genre.getName());
        }
    }
}
