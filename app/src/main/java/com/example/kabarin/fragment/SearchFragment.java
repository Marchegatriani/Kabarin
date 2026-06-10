package com.example.kabarin.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
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

    private SearchView searchView;
    private RecyclerView rvSearchResult;
    private ProgressBar progressBar;
    private LinearLayout layoutSearchStatus;
    private ImageView ivSearchStatus;
    private TextView tvSearchStatus;
    private Button btnRetry;
    private String lastQuery = "";

    // Handler for debouncing (delaying search while typing)
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

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

        // Configure SearchView to be open and focused immediately
        searchView.setIconified(false);
        searchView.requestFocus();
        
        // Show keyboard automatically
        searchView.postDelayed(() -> {
            if (isAdded() && getContext() != null) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 200);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.trim().isEmpty()) {
                    // Cancel pending search if user presses enter
                    searchHandler.removeCallbacks(searchRunnable);
                    lastQuery = query;
                    performSearch(query);
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Remove old callback whenever user types a new character
                searchHandler.removeCallbacks(searchRunnable);

                if (newText != null && !newText.trim().isEmpty()) {
                    // Set loading status while waiting for debouncing
                    progressBar.setVisibility(View.VISIBLE);
                    layoutSearchStatus.setVisibility(View.GONE);
                    rvSearchResult.setVisibility(View.GONE);

                    // Run search after 800ms delay
                    searchRunnable = () -> {
                        lastQuery = newText;
                        performSearch(newText);
                    };
                    searchHandler.postDelayed(searchRunnable, 800);
                } else {
                    // If text is cleared, hide loading and results
                    progressBar.setVisibility(View.GONE);
                    rvSearchResult.setVisibility(View.GONE);
                    layoutSearchStatus.setVisibility(View.VISIBLE);
                    tvSearchStatus.setText("Start searching for your favorite news");
                }
                return true;
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
                                showEmptyState("No results found for \"" + query + "\"");
                            }
                        } else {
                            showErrorState("Failed to load results. Please try again.");
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        progressBar.setVisibility(View.GONE);
                        showErrorState("Connection problem. Please check your internet.");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear handler when view is destroyed to avoid memory leak
        searchHandler.removeCallbacks(searchRunnable);
    }
}
