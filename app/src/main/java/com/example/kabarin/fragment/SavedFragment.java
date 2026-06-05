package com.example.kabarin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kabarin.R;
import com.example.kabarin.activity.NewsDetailActivity;
import com.example.kabarin.adapter.SavedNewsAdapter;
import com.example.kabarin.model.Article;
import com.example.kabarin.utils.SavedNewsManager;

import java.util.List;

public class SavedFragment extends Fragment {

    private RecyclerView rvSavedArticles;
    private LinearLayout layoutEmpty;
    private SavedNewsAdapter adapter;
    private SavedNewsManager savedNewsManager;

    public SavedFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSavedArticles = view.findViewById(R.id.rvSavedArticles);
        // Assuming you might add a layoutEmpty in the XML if needed
        // For now, let's just handle the list
        
        savedNewsManager = new SavedNewsManager(requireContext());
        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSavedList();
    }

    private void setupRecyclerView() {
        rvSavedArticles.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshSavedList();
    }

    private void refreshSavedList() {
        List<Article> savedArticles = savedNewsManager.getSavedArticles();
        
        adapter = new SavedNewsAdapter(savedArticles);
        adapter.setOnItemClickListener(article -> {
            Intent intent = new Intent(getContext(), NewsDetailActivity.class);
            intent.putExtra("article", article);
            startActivity(intent);
        });
        
        adapter.setOnRemoveClickListener(article -> {
            savedNewsManager.removeArticle(article);
            refreshSavedList();
        });
        
        rvSavedArticles.setAdapter(adapter);
    }
}
