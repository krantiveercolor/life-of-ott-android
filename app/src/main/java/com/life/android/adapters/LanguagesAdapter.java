package com.life.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.life.android.R;
import com.life.android.models.home_content.LanguageModel;

import java.util.List;

public class LanguagesAdapter extends RecyclerView.Adapter<LanguagesAdapter.LanguagesViewHolder> {

    private Context context;
    private List<LanguageModel> languageModelList;

    public LanguagesAdapter(Context context, List<LanguageModel> languageModelList) {
        this.context = context;
        this.languageModelList = languageModelList;
    }

    @NonNull
    @Override
    public LanguagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false);
        return new LanguagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguagesViewHolder holder, int position) {
        holder.bindData(languageModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return languageModelList.size();
    }

    class LanguagesViewHolder extends RecyclerView.ViewHolder {

        private TextView langKeyView, langNameView;

        public LanguagesViewHolder(@NonNull View itemView) {
            super(itemView);
            langKeyView = itemView.findViewById(R.id.language_key_view);
            langNameView = itemView.findViewById(R.id.language_name_view);
        }

        public void bindData(LanguageModel languageModel) {
            langKeyView.setText(languageModel.getLanguageKey());
            langNameView.setText(languageModel.getLanguageName());
        }
    }
}
