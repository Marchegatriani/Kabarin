package com.example.kabarin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kabarin.R;
import com.example.kabarin.adapter.NewsAdapter;
import com.example.kabarin.api.RetrofitClient;
import com.example.kabarin.model.Article;
import com.example.kabarin.model.NewsResponse;
import com.example.kabarin.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvTrending, rvLatestNews;
    private ChipGroup chipGroupCategories;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabSearch;
    private NewsAdapter latestNewsAdapter, trendingNewsAdapter;
    private String currentCategory = "general";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inisialisasi View
        rvTrending = view.findViewById(R.id.rvTrending);
        rvLatestNews = view.findViewById(R.id.rvLatestNews);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        fabSearch = view.findViewById(R.id.fab);

        // 2. Setup RecyclerView
        rvTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLatestNews.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Load Berita Awal
        refreshData();

        // 4. Swipe Refresh Listener
        swipeRefresh.setOnRefreshListener(() -> {
            refreshData();
        });

        // 5. Category Listener
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = view.findViewById(checkedIds.get(0));
                String category = selectedChip.getText().toString().toLowerCase();
                currentCategory = category.equals("all") ? "general" : category;
                refreshData();
            }
        });

        fabSearch.setOnClickListener(v -> Toast.makeText(getContext(), "Search Feature Coming Soon", Toast.LENGTH_SHORT).show());
    }

    private void refreshData() {
        swipeRefresh.setRefreshing(true);
        fetchNews(currentCategory, true);  // Trending
        fetchNews(currentCategory, false); // Latest
    }

    private void fetchNews(String category, boolean isTrending) {
        RetrofitClient.getApiService().getTopHeadlines("us", category, Constants.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (isAdded() && isVisible()) {
                            swipeRefresh.setRefreshing(false);
                            if (response.isSuccessful() && response.body() != null) {
                                List<Article> articles = response.body().getArticles();
                                if (isTrending) {
                                    trendingNewsAdapter = new NewsAdapter(articles);
                                    rvTrending.setAdapter(trendingNewsAdapter);
                                } else {
                                    latestNewsAdapter = new NewsAdapter(articles);
                                    rvLatestNews.setAdapter(latestNewsAdapter);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        if (isAdded()) {
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
