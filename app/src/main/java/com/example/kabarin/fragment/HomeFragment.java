package com.example.kabarin.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.kabarin.R;
import com.example.kabarin.activity.NewsDetailActivity;
import com.example.kabarin.activity.SearchActivity;
import com.example.kabarin.adapter.NewsAdapter;
import com.example.kabarin.api.RetrofitClient;
import com.example.kabarin.local.DatabaseHelper;
import com.example.kabarin.model.Article;
import com.example.kabarin.model.NewsResponse;
import com.example.kabarin.utils.Constants;
import com.example.kabarin.utils.NetworkUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;

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
    private TextView tvHomeUserName, tvNetworkStatus;
    private ShapeableImageView ivHomeProfile;
    private String currentCategory = "general";
    private SharedPreferences prefs;

    // Tambahan untuk Offline Mode
    private DatabaseHelper dbHelper;
    private ExecutorService executorService;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(getContext());
        executorService = Executors.newSingleThreadExecutor();

        rvTrending        = view.findViewById(R.id.rvTrending);
        rvLatestNews      = view.findViewById(R.id.rvLatestNews);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        swipeRefresh      = view.findViewById(R.id.swipeRefresh);
        fabSearch         = view.findViewById(R.id.fab);
        tvHomeUserName    = view.findViewById(R.id.tvHomeUserName);
        ivHomeProfile     = view.findViewById(R.id.ivHomeProfile);
        tvNetworkStatus   = view.findViewById(R.id.tvNetworkStatus);

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

        loadUserProfile();
        refreshData();

        swipeRefresh.setColorSchemeResources(R.color.selector_chip_bg);
        swipeRefresh.setOnRefreshListener(this::refreshData);

        fabSearch.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        String name = prefs.getString("userName", "Pengguna");
        String photoUri = prefs.getString("profileUri", null);
        tvHomeUserName.setText(name.isEmpty() ? "Pengguna" : name);
        if (photoUri != null) {
            Glide.with(this).load(Uri.parse(photoUri)).placeholder(R.drawable.ic_profile).into(ivHomeProfile);
        }
    }

    private void refreshData() {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(true);

        if (NetworkUtils.isNetworkConnected(requireContext())) {
            // MODE ONLINE
            tvNetworkStatus.setText("Berita terbaru");
            tvNetworkStatus.setBackgroundColor(getResources().getColor(R.color.selector_chip_bg, null));
            tvNetworkStatus.setTextColor(android.graphics.Color.WHITE);
            fetchNews(currentCategory, true);   
            fetchNews(currentCategory, false);  
        } else {
            // MODE OFFLINE
            tvNetworkStatus.setText("Menampilkan berita offline");
            tvNetworkStatus.setBackgroundColor(android.graphics.Color.LTGRAY);
            tvNetworkStatus.setTextColor(android.graphics.Color.DKGRAY);
            loadOfflineData();
            Toast.makeText(getContext(), "Tidak ada internet. Memuat dari cache.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchNews(final String categoryToFetch, boolean isTrending) {
        RetrofitClient.getApiService()
                .getTopHeadlines("us", categoryToFetch, Constants.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (!isAdded()) return;
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles != null) {
                                if (isTrending) {
                                    List<Article> trending = articles.size() > 5 ? articles.subList(0, 5) : articles;
                                    rvTrending.setAdapter(createAdapter(trending, categoryToFetch, NewsAdapter.TYPE_TRENDING));
                                } else {
                                    rvLatestNews.setAdapter(createAdapter(articles, categoryToFetch, NewsAdapter.TYPE_LATEST));
                                    // Update Cache hanya untuk Latest News
                                    updateCache(articles);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        swipeRefresh.setRefreshing(false);
                    }
                });
    }

    private NewsAdapter createAdapter(List<Article> list, String cat, int type) {
        NewsAdapter adapter = new NewsAdapter(list, type, cat);
        adapter.setOnItemClickListener(this::openDetail);
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
                    swipeRefresh.setRefreshing(false);
                    if (!cached.isEmpty()) {
                        rvLatestNews.setAdapter(createAdapter(cached, "Offline", NewsAdapter.TYPE_LATEST));
                        rvTrending.setVisibility(View.GONE); // Sembunyikan trending di mode offline
                    } else {
                        Toast.makeText(getContext(), "Cache kosong", Toast.LENGTH_SHORT).show();
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
