package com.life.android.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.life.android.R;
import com.life.android.adapters.LanguagesAdapter;
import com.life.android.models.home_content.LanguageModel;

import java.util.ArrayList;
import java.util.List;

public class LanguagesFragment extends Fragment {

    public LanguagesFragment() {
        // Required empty public constructor
    }

    public static LanguagesFragment newInstance() {
        LanguagesFragment fragment = new LanguagesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_languages, container, false);
    }

    private ImageView backBtn;
    private RecyclerView languagesRecyclerView;
    private List<LanguageModel> languageModelList;
    private LanguagesAdapter languagesAdapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backBtn = view.findViewById(R.id.languages_back_btn);
        languagesRecyclerView = view.findViewById(R.id.languages_recycler_view);

        backBtn.setOnClickListener(view1 ->
                requireActivity().onBackPressed()
        );
        createLanguagesRecyclerView();
    }

    private void createLanguagesRecyclerView() {
        languageModelList = new ArrayList<>();
        languageModelList.add(new LanguageModel("1", "English", "E"));
        languageModelList.add(new LanguageModel("1", "Telugu", "తె"));
        languageModelList.add(new LanguageModel("1", "Hindi", "हिं"));
        languageModelList.add(new LanguageModel("1", "Kannada", "ಕ"));
        languageModelList.add(new LanguageModel("1", "Tamil", "த"));
        languageModelList.add(new LanguageModel("1", "Malayalam", "ല"));
        languagesAdapter = new LanguagesAdapter(requireContext(), languageModelList);
        languagesRecyclerView.setAdapter(languagesAdapter);
    }
}