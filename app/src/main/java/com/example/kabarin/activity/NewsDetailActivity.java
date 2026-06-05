package com.example.kabarin.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.kabarin.R;
import com.example.kabarin.model.Article;
import com.example.kabarin.utils.SavedNewsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NewsDetailActivity extends AppCompatActivity {

    private ImageView ivDetailImage;
    private TextView tvDetailTitle, tvDetailAuthorTime, tvDetailContent;
    private Button btnReadMore;
    private FloatingActionButton fabSave;
    private Article article;
    private SavedNewsManager savedNewsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        article = (Article) getIntent().getSerializableExtra("article");
        if (article == null) {
            finish();
            return;
        }

        savedNewsManager = new SavedNewsManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        ivDetailImage = findViewById(R.id.ivDetailImage);
        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailAuthorTime = findViewById(R.id.tvDetailAuthorTime);
        tvDetailContent = findViewById(R.id.tvDetailContent);
        btnReadMore = findViewById(R.id.btnReadMore);
        fabSave = findViewById(R.id.fabSave);

        displayArticle();
        setupSaveButton();

        // MENGGUNAKAN WEBVIEW INTERNAL (NewsWebActivity) agar tidak keluar aplikasi
        btnReadMore.setOnClickListener(v -> {
            Intent intent = new Intent(NewsDetailActivity.this, NewsWebActivity.class);
            intent.putExtra("url", article.getUrl());
            startActivity(intent);
        });
    }

    private void displayArticle() {
        tvDetailTitle.setText(article.getTitle());
        String author = (article.getAuthor() != null) ? article.getAuthor() : "Unknown Source";
        tvDetailAuthorTime.setText("By " + author + " • " + article.getPublishedAt());
        tvDetailContent.setText(article.getContent() != null ? article.getContent() : article.getDescription());

        Glide.with(this)
                .load(article.getUrlToImage())
                .placeholder(R.color.selector_chip_bg)
                .error(R.color.selector_chip_bg)
                .into(ivDetailImage);
    }

    private void setupSaveButton() {
        updateFabIcon();
        fabSave.setOnClickListener(v -> {
            if (savedNewsManager.isSaved(article)) {
                savedNewsManager.removeArticle(article);
                Toast.makeText(this, "Berita dihapus dari simpanan", Toast.LENGTH_SHORT).show();
            } else {
                savedNewsManager.saveArticle(article);
                Toast.makeText(this, "Berita berhasil disimpan!", Toast.LENGTH_SHORT).show();
            }
            updateFabIcon();
        });
    }

    private void updateFabIcon() {
        // Gunakan ic_save sesuai permintaan
        fabSave.setImageResource(R.drawable.ic_save);
        if (savedNewsManager.isSaved(article)) {
            fabSave.setColorFilter(Color.YELLOW); // Kuning jika tersimpan
        } else {
            fabSave.setColorFilter(Color.WHITE);  // Putih jika belum
        }
    }
}
