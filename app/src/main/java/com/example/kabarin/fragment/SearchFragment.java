package com.example.kabarin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kabarin.R;
import com.example.kabarin.activity.NewsDetailActivity;
import com.example.kabarin.adapter.NewsAdapter;
import com.example.kabarin.api.RetrofitClient;
import com.example.kabarin.model.Article;
import com.example.kabarin.model.NewsResponse;
import com.example.kabarin.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private ImageButton btnBack;
    private SearchView searchView;
    private RecyclerView rvSearchResult;
    private ProgressBar progressBar;
    private LinearLayout layoutSearchStatus;
    private ImageView ivSearchStatus;
    private TextView tvSearchStatus;
    private Button btnRetry;
    private String lastQuery = "";

    public SearchFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchView = view.findViewById(R.id.searchView);
        rvSearchResult = view.findViewById(R.id.rvSearchResult);
        progressBar = view.findViewById(R.id.progressBar);
        layoutSearchStatus = view.findViewById(R.id.layoutSearchStatus);
        ivSearchStatus = view.findViewById(R.id.ivSearchStatus);
        tvSearchStatus = view.findViewById(R.id.tvSearchStatus);
        btnRetry = view.findViewById(R.id.btnRetry);

        rvSearchResult.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

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

        // Request focus on search view when fragment starts
        searchView.requestFocus();
    }

    private void performSearch(String query) {
        progressBar.setVisibility(View.VISIBLE);
        rvSearchResult.setVisibility(View.GONE);
        layoutSearchStatus.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        RetrofitClient.getApiService().getEverything(query, Constants.API_KEY)
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (!isAdded()) return;
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
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        showErrorState("Masalah koneksi. Periksa internet Anda.");
                    }
                });
    }

    private void showResults(List<Article> articles) {
        rvSearchResult.setVisibility(View.VISIBLE);
        layoutSearchStatus.setVisibility(View.GONE);

        NewsAdapter adapter = new NewsAdapter(articles, NewsAdapter.TYPE_LATEST, "Search Result");
        adapter.setOnItemClickListener(article -> {
            Intent intent = new Intent(getContext(), NewsDetailActivity.class);
            intent.putExtra("article", article);
            startActivity(intent);
        });
        rvSearchResult.setAdapter(adapter);
    }

    private void showEmptyState(String message) {
        rvSearchResult.setVisibility(View.GONE);
        layoutSearchStatus.setVisibility(View.VISIBLE);
        ivSearchStatus.setImageResource(android.R.drawable.ic_menu_search);
        tvSearchStatus.setText(message);
        btnRetry.setVisibility(View.GONE);
    }

    private void showErrorState(String message) {
        rvSearchResult.setVisibility(View.GONE);
        layoutSearchStatus.setVisibility(View.VISIBLE);
        ivSearchStatus.setImageResource(android.R.drawable.stat_notify_error);
        tvSearchStatus.setText(message);
        btnRetry.setVisibility(View.VISIBLE);
    }
}
