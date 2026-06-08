package com.example.kabarin.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kabarin.R;
import com.example.kabarin.activity.NewsDetailActivity;
import com.example.kabarin.adapter.NewsAdapter;
import com.example.kabarin.api.RetrofitClient;
import com.example.kabarin.local.DatabaseHelper;
import com.example.kabarin.model.Article;
import com.example.kabarin.model.NewsResponse;
import com.example.kabarin.utils.Constants;
import com.example.kabarin.utils.NetworkUtils;
import com.example.kabarin.utils.SavedNewsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvTrending, rvLatestNews;
    private ChipGroup chipGroupCategories;
    private SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabSearch;
    private String currentCategory = "general";

    // Tambahan untuk Offline Mode
    private DatabaseHelper dbHelper;
    private ExecutorService executorService;
    private SavedNewsManager savedNewsManager;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());
        executorService = Executors.newSingleThreadExecutor();
        savedNewsManager = new SavedNewsManager(requireContext());

        rvTrending        = view.findViewById(R.id.rvTrending);
        rvLatestNews      = view.findViewById(R.id.rvLatestNews);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        swipeRefresh      = view.findViewById(R.id.swipeRefresh);
        fabSearch         = view.findViewById(R.id.fab);

        rvTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLatestNews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLatestNews.setNestedScrollingEnabled(false);

        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    String categoryStr = selectedChip.getText().toString().toLowerCase();
                    currentCategory = categoryStr.equals("all") ? "general" : categoryStr;
                    refreshData();
                }
            }
        });

        swipeRefresh.setColorSchemeResources(R.color.selector_chip_bg);
        swipeRefresh.setOnRefreshListener(this::refreshData);

        fabSearch.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_home_to_nav_search);
        });

        // Load data awal
        refreshData();
    }

    private void refreshData() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);

        if (NetworkUtils.isNetworkConnected(requireContext())) {
            // MODE ONLINE
            rvTrending.setVisibility(View.VISIBLE);
            fetchNews(currentCategory, true);   
            fetchNews(currentCategory, false);  
        } else {
            // MODE OFFLINE
            loadOfflineData();
        }
    }

    private void fetchNews(final String categoryToFetch, boolean isTrending) {
        RetrofitClient.getApiService()
                .getTopHeadlines("us", categoryToFetch, Constants.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles != null) {
                                if (isTrending) {
                                    List<Article> trending = articles.size() > 5 ? articles.subList(0, 5) : articles;
                                    rvTrending.setAdapter(createAdapter(trending, categoryToFetch, NewsAdapter.TYPE_TRENDING));
                                } else {
                                    rvLatestNews.setAdapter(createAdapter(articles, categoryToFetch, NewsAdapter.TYPE_LATEST));
                                    updateCache(articles);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private NewsAdapter createAdapter(List<Article> list, String cat, int type) {
        NewsAdapter adapter = new NewsAdapter(list, type, cat);
        adapter.setOnItemClickListener(this::openDetail);
        return adapter;
    }

    private void updateCache(List<Article> articles) {
        if (articles == null || articles.isEmpty()) return;
        executorService.execute(() -> {
            dbHelper.clearCache();
            dbHelper.saveArticles(articles);
        });
    }

    private void loadOfflineData() {
        executorService.execute(() -> {
            List<Article> cached = dbHelper.getCachedArticles();
            List<Article> saved = savedNewsManager.getSavedArticles();
            
            List<Article> displayList = new ArrayList<>(cached);
            if (displayList.isEmpty() && !saved.isEmpty()) {
                displayList.addAll(saved);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    if (!displayList.isEmpty()) {
                        rvLatestNews.setVisibility(View.VISIBLE);
                        rvLatestNews.setAdapter(createAdapter(displayList, "Offline", NewsAdapter.TYPE_LATEST));
                        rvTrending.setVisibility(View.GONE); 
                    } else {
                        Toast.makeText(getContext(), "Belum ada berita tersimpan", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void openDetail(Article article) {
        Intent intent = new Intent(getContext(), NewsDetailActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) executorService.shutdown();
    }
}
