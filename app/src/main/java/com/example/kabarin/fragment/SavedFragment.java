package com.example.kabarin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class SavedFragment extends Fragment {

    private RecyclerView rvSavedArticles;
    private View cardEmptyState;
    private Button btnExploreNews;
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

        // Inisialisasi View
        rvSavedArticles = view.findViewById(R.id.rvSavedArticles);
        cardEmptyState  = view.findViewById(R.id.cardEmptyState);
        btnExploreNews  = view.findViewById(R.id.btnExploreNews);
        
        savedNewsManager = new SavedNewsManager(requireContext());

        rvSavedArticles.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tombol Explore: Kembali ke tab Home
        btnExploreNews.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            }
        });

        refreshSavedList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshSavedList();
    }

    private void refreshSavedList() {
        List<Article> savedArticles = savedNewsManager.getSavedArticles();
        
        if (savedArticles == null || savedArticles.isEmpty()) {
            // Tampilkan Empty State, sembunyikan List
            rvSavedArticles.setVisibility(View.GONE);
            cardEmptyState.setVisibility(View.VISIBLE);
        } else {
            // Tampilkan List, sembunyikan Empty State
            rvSavedArticles.setVisibility(View.VISIBLE);
            cardEmptyState.setVisibility(View.GONE);

            adapter = new SavedNewsAdapter(savedArticles);
            adapter.setOnItemClickListener(article -> {
                Intent intent = new Intent(getContext(), NewsDetailActivity.class);
                intent.putExtra("article", article);
                startActivity(intent);
            });
            
            adapter.setOnRemoveClickListener(article -> {
                savedNewsManager.removeArticle(article);
                refreshSavedList(); // Refresh tampilan setelah dihapus
            });
            
            rvSavedArticles.setAdapter(adapter);
        }
    }
}
