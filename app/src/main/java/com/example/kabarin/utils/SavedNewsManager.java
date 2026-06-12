package com.example.kabarin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.kabarin.model.Article;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SavedNewsManager {
    private static final String PREF_PREFIX = "saved_news_";
    private static final String KEY_SAVED_ARTICLES = "saved_articles";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public SavedNewsManager(Context context) {
        SharedPreferences mainPrefs = context.getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        String currentUserEmail = mainPrefs.getString("currentUserEmail", "guest");
        
        // Menggunakan file preference unik per user berdasarkan email
        sharedPreferences = context.getSharedPreferences(PREF_PREFIX + currentUserEmail, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveArticle(Article article) {
        List<Article> savedArticles = getSavedArticles();
        for (Article a : savedArticles) {
            if (a.getUrl().equals(article.getUrl())) {
                return;
            }
        }
        savedArticles.add(article);
        String json = gson.toJson(savedArticles);
        sharedPreferences.edit().putString(KEY_SAVED_ARTICLES, json).apply();
    }

    public void removeArticle(Article article) {
        List<Article> savedArticles = getSavedArticles();
        Article toRemove = null;
        for (Article a : savedArticles) {
            if (a.getUrl().equals(article.getUrl())) {
                toRemove = a;
                break;
            }
        }
        if (toRemove != null) {
            savedArticles.remove(toRemove);
            String json = gson.toJson(savedArticles);
            sharedPreferences.edit().putString(KEY_SAVED_ARTICLES, json).apply();
        }
    }

    public List<Article> getSavedArticles() {
        String json = sharedPreferences.getString(KEY_SAVED_ARTICLES, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<Article>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public boolean isSaved(Article article) {
        List<Article> savedArticles = getSavedArticles();
        for (Article a : savedArticles) {
            if (a.getUrl().equals(article.getUrl())) {
                return true;
            }
        }
        return false;
    }
}
