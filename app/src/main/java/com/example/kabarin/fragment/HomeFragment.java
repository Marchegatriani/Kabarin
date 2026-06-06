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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.kabarin.R;
import com.example.kabarin.activity.NewsDetailActivity;
import com.example.kabarin.activity.SearchActivity;
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
    private String currentCategory = "general";

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTrending        = view.findViewById(R.id.rvTrending);
        rvLatestNews      = view.findViewById(R.id.rvLatestNews);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        swipeRefresh      = view.findViewById(R.id.swipeRefresh);
        fabSearch         = view.findViewById(R.id.fab);

        rvTrending.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
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

        refreshData();

        swipeRefresh.setColorSchemeResources(R.color.selector_chip_bg);
        swipeRefresh.setOnRefreshListener(this::refreshData);

        // Membuka SearchActivity saat FAB diklik
        fabSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
    }

    private void refreshData() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
        fetchNews(currentCategory, true);   
        fetchNews(currentCategory, false);  
    }

    private void fetchNews(final String categoryToFetch, boolean isTrending) {
        RetrofitClient.getApiService()
                .getTopHeadlines("us", categoryToFetch, Constants.API_KEY)
                .enqueue(new Callback<NewsResponse>() {

                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (!isAdded() || !isVisible()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles == null) return;

                            if (isTrending) {
                                List<Article> trendingList = articles.size() > 5 ? articles.subList(0, 5) : articles;
                                NewsAdapter trendingAdapter = new NewsAdapter(trendingList, NewsAdapter.TYPE_TRENDING, categoryToFetch);
                                trendingAdapter.setOnItemClickListener(article -> openDetail(article));
                                rvTrending.setAdapter(trendingAdapter);
                            } else {
                                NewsAdapter latestAdapter = new NewsAdapter(articles, NewsAdapter.TYPE_LATEST, categoryToFetch);
                                latestAdapter.setOnItemClickListener(article -> openDetail(article));
                                rvLatestNews.setAdapter(latestAdapter);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), "Gagal memuat berita", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openDetail(Article article) {
        Intent intent = new Intent(getContext(), NewsDetailActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
    }
}
