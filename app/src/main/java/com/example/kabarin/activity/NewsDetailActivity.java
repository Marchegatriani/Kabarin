package com.example.kabarin.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
            Toast.makeText(this, "Data berita tidak ditemukan", Toast.LENGTH_SHORT).show();
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

        btnReadMore.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
                startActivity(browserIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Tidak dapat membuka browser", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayArticle() {
        tvDetailTitle.setText(article.getTitle());
        
        String author = (article.getAuthor() != null && !article.getAuthor().isEmpty()) 
                ? article.getAuthor() : "Unknown Source";
        String date = (article.getPublishedAt() != null) ? article.getPublishedAt() : "";
        
        tvDetailAuthorTime.setText("By " + author + (date.isEmpty() ? "" : " • " + date));
        
        // Pengecekan isi konten
        String content = article.getContent();
        if (content == null || content.isEmpty()) {
            content = article.getDescription();
        }
        tvDetailContent.setText(content != null ? content : "No content available.");

        if (!isFinishing()) {
            Glide.with(this)
                    .load(article.getUrlToImage())
                    .placeholder(R.drawable.ic_kabarin_logo)
                    .error(R.drawable.ic_kabarin_logo)
                    .into(ivDetailImage);
        }
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
        fabSave.setImageResource(R.drawable.ic_save);
        if (savedNewsManager.isSaved(article)) {
            fabSave.setColorFilter(Color.YELLOW);
        } else {
            fabSave.setColorFilter(Color.WHITE);
        }
    }
}
