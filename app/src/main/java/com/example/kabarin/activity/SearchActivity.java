package com.example.kabarin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kabarin.R;
import com.example.kabarin.adapter.NewsAdapter;
import com.example.kabarin.api.RetrofitClient;
import com.example.kabarin.model.Article;
import com.example.kabarin.model.NewsResponse;
import com.example.kabarin.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private SearchView searchView;
    private RecyclerView rvSearchResult;
    private ProgressBar progressBar;
    private LinearLayout layoutNoResult;
    private ImageView ivStatus;
    private TextView tvStatus;
    private Button btnRetry;
    private String lastQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialize Views
        btnBack = findViewById(R.id.btnBack);
        searchView = findViewById(R.id.searchView);
        rvSearchResult = findViewById(R.id.rvSearchResult);
        progressBar = findViewById(R.id.progressBar);
        
        // PERBAIKAN: ID di XML adalah 'layoutStatus', bukan 'layoutNoResult'
        layoutNoResult = findViewById(R.id.layoutSearchStatus);
        ivStatus = findViewById(R.id.ivSearchStatus);
        tvStatus = findViewById(R.id.tvSearchStatus);
        
        btnRetry = findViewById(R.id.btnRetry);

        rvSearchResult.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    lastQuery = query;
                    performSearch(query);
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        btnRetry.setOnClickListener(v -> {
            if (!lastQuery.isEmpty()) {
                performSearch(lastQuery);
            }
        });
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        rvSearchResult.setVisibility(View.GONE);
        layoutNoResult.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        RetrofitClient.getApiService().getEverything(query, Constants.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Article> articles = response.body().getArticles();
                            if (articles != null && !articles.isEmpty()) {
                                showResults(articles);
                            } else {
                                showEmptyState("Tidak ada hasil untuk \"" + query + "\"");
                            }
                        } else {
                            showErrorState("Gagal memuat hasil. Silakan coba lagi.");
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showErrorState("Masalah koneksi: " + t.getMessage());
                        Toast.makeText(SearchActivity.this, "Gagal terhubung ke internet", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showResults(List<Article> articles) {
        rvSearchResult.setVisibility(View.VISIBLE);
        layoutNoResult.setVisibility(View.GONE);

        NewsAdapter adapter = new NewsAdapter(articles, NewsAdapter.TYPE_LATEST, "Search Result");
        adapter.setOnItemClickListener(article -> {
            Intent intent = new Intent(SearchActivity.this, NewsDetailActivity.class);
            intent.putExtra("article", article);
            startActivity(intent);
        });
        rvSearchResult.setAdapter(adapter);
    }

    private void showEmptyState(String message) {
        rvSearchResult.setVisibility(View.GONE);
        layoutNoResult.setVisibility(View.VISIBLE);
        ivStatus.setImageResource(android.R.drawable.ic_menu_search);
        tvStatus.setText(message);
        btnRetry.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        rvSearchResult.setVisibility(View.GONE);
        layoutNoResult.setVisibility(View.VISIBLE);
        ivStatus.setImageResource(android.R.drawable.stat_notify_error);
        tvStatus.setText(message);
        btnRetry.setVisibility(View.VISIBLE);
    }
}
