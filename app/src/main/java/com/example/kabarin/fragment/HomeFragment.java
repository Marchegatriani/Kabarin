package com.example.kabarin.fragment;

import android.content.Intent;
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

    private DatabaseHelper dbHelper;
    private ExecutorService executorService;
    private SavedNewsManager savedNewsManager;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(getContext());
        executorService = Executors.newSingleThreadExecutor();
        savedNewsManager = new SavedNewsManager(requireContext());

        rvTrending = view.findViewById(R.id.rvTrending);
        rvLatestNews = view.findViewById(R.id.rvLatestNews);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        fabSearch = view.findViewById(R.id.fab);

        rvTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvLatestNews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLatestNews.setNestedScrollingEnabled(false);

        // FAB Search Click
        if (fabSearch != null) {
            fabSearch.setOnClickListener(v -> 
                Navigation.findNavController(v).navigate(R.id.action_nav_home_to_nav_search)
            );
        }

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

        swipeRefresh.setOnRefreshListener(this::refreshData);
        refreshData();
    }

    private void refreshData() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
        if (NetworkUtils.isNetworkConnected(requireContext())) {
            fetchNews(currentCategory, true);
            fetchNews(currentCategory, false);
        } else {
            loadOfflineData();
        }
    }

    private void fetchNews(String category, boolean isTrending) {
        RetrofitClient.getApiService()
                .getTopHeadlines("us", category, Constants.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles != null) {
                                if (isTrending) {
                                    rvTrending.setAdapter(createAdapter(articles.subList(0, Math.min(articles.size(), 5)), category, NewsAdapter.TYPE_TRENDING));
                                } else {
                                    rvLatestNews.setAdapter(createAdapter(articles, category, NewsAdapter.TYPE_LATEST));
                                    updateCache(articles);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private NewsAdapter createAdapter(List<Article> list, String cat, int type) {
        NewsAdapter adapter = new NewsAdapter(list, type, cat);
        adapter.setSavedNewsManager(savedNewsManager);
        adapter.setOnItemClickListener(this::openDetail);
        adapter.setOnSaveClickListener(article -> {
            if (savedNewsManager.isSaved(article)) {
                savedNewsManager.removeArticle(article);
                Toast.makeText(getContext(), R.string.news_removed, Toast.LENGTH_SHORT).show();
            } else {
                savedNewsManager.saveArticle(article);
                Toast.makeText(getContext(), R.string.news_saved, Toast.LENGTH_SHORT).show();
            }
        });
        return adapter;
    }

    private void updateCache(List<Article> articles) {
        executorService.execute(() -> {
            dbHelper.clearCache();
            dbHelper.saveArticles(articles);
        });
    }

    private void loadOfflineData() {
        executorService.execute(() -> {
            List<Article> cached = dbHelper.getCachedArticles();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    rvLatestNews.setAdapter(createAdapter(cached, "Offline", NewsAdapter.TYPE_LATEST));
                    rvTrending.setVisibility(View.GONE);
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
        executorService.shutdown();
    }
}
