package com.example.kabarin.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kabarin.R;
import com.example.kabarin.model.Article;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_TRENDING = 0;
    public static final int TYPE_LATEST   = 1;

    private final List<Article> articles;
    private final int viewType;
    private final String category; 
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public NewsAdapter(List<Article> articles, int viewType, String category) {
        this.articles = articles;
        this.viewType = viewType;
        this.category = (category == null) ? "general" : category;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    static class TrendingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvCategory, tvTitle, tvTime, tvAuthor;

        TrendingViewHolder(View v) {
            super(v);
            ivImage    = v.findViewById(R.id.ivTrendingImage);
            tvCategory = v.findViewById(R.id.tvTrendingCategory);
            tvTitle    = v.findViewById(R.id.tvTrendingTitle);
            tvTime     = v.findViewById(R.id.tvTrendingTime);
            tvAuthor   = v.findViewById(R.id.tvTrendingAuthor);
        }
    }

    static class LatestViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvCategory, tvReadTime, tvTitle, tvDescription;

        LatestViewHolder(View v) {
            super(v);
            ivImage       = v.findViewById(R.id.ivNewsImage);
            tvCategory    = v.findViewById(R.id.tvNewsCategory);
            tvReadTime    = v.findViewById(R.id.tvReadTime);
            tvTitle       = v.findViewById(R.id.tvNewsTitle);
            tvDescription = v.findViewById(R.id.tvNewsDescription);
        }
    }

    @Override
    public int getItemViewType(int position) { return viewType; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (type == TYPE_TRENDING) {
            return new TrendingViewHolder(inf.inflate(R.layout.item_trending_news, parent, false));
        } else {
            return new LatestViewHolder(inf.inflate(R.layout.item_news, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Article article = articles.get(position);
        if (holder instanceof TrendingViewHolder) bindTrending((TrendingViewHolder) holder, article);
        else                                       bindLatest  ((LatestViewHolder)   holder, article);

        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClick(article); });
    }

    @Override
    public int getItemCount() { return articles == null ? 0 : articles.size(); }

    private void bindTrending(TrendingViewHolder h, Article article) {
        h.tvTitle.setText(nvl(article.getTitle(), ""));
        h.tvAuthor.setText((article.getAuthor() != null && !article.getAuthor().isEmpty()) ? "By " + article.getAuthor() : "Unknown");
        h.tvTime.setText(formatTimeAgo(article.getPublishedAt()));

        // Menggunakan Smart Labeling
        String displayLabel = inferCategory(article);
        h.tvCategory.setText("#" + displayLabel);

        Glide.with(h.ivImage).load(article.getUrlToImage())
                .placeholder(R.color.selector_chip_bg).error(R.color.selector_chip_bg)
                .centerCrop().into(h.ivImage);
    }

    private void bindLatest(LatestViewHolder h, Article article) {
        h.tvTitle.setText(nvl(article.getTitle(), ""));
        h.tvDescription.setText(nvl(article.getDescription(), ""));

        // Menggunakan Smart Labeling
        String displayLabel = inferCategory(article);
        h.tvCategory.setText(displayLabel);
        
        // Update warna badge agar bervariasi sesuai kategori yang terdeteksi
        if (h.tvCategory.getBackground() != null) {
            h.tvCategory.getBackground().mutate().setTint(badgeColor(displayLabel));
        }

        int words = (article.getDescription() != null) ? article.getDescription().split("\\s+").length : 50;
        h.tvReadTime.setText(Math.max(1, words / 200) + " min read");

        Glide.with(h.ivImage).load(article.getUrlToImage())
                .placeholder(R.color.selector_chip_bg).error(R.color.selector_chip_bg)
                .centerCrop().into(h.ivImage);
    }

    private String nvl(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
    }

    /**
     * Smart Category Inference:
     * Menebak kategori berdasarkan kata kunci jika berada di tab 'All' (general)
     */
    private String inferCategory(Article article) {
        if (!category.equalsIgnoreCase("general")) {
            return category.toUpperCase();
        }

        String text = (nvl(article.getTitle(), "") + " " + 
                      nvl(article.getDescription(), "") + " " + 
                      (article.getSource() != null ? article.getSource().getName() : "")).toLowerCase();

        if (text.contains("tech") || text.contains("google") || text.contains("apple") || text.contains("software") || text.contains(" ai ") || text.contains("android")) return "TECHNOLOGY";
        if (text.contains("sport") || text.contains("football") || text.contains("soccer") || text.contains(" match") || text.contains(" league") || text.contains(" nfl")) return "SPORTS";
        if (text.contains("business") || text.contains("stock") || text.contains("market") || text.contains("economy") || text.contains("finance") || text.contains("price")) return "BUSINESS";
        if (text.contains("health") || text.contains("doctor") || text.contains("medical") || text.contains("virus") || text.contains("vaccine") || text.contains("hospital")) return "HEALTH";
        if (text.contains("science") || text.contains("nasa") || text.contains("space") || text.contains("planet") || text.contains("nature")) return "SCIENCE";
        if (text.contains("politic") || text.contains("government") || text.contains("election") || text.contains("president") || text.contains("minister")) return "POLITICS";
        if (text.contains("movie") || text.contains("music") || text.contains("entertainment") || text.contains("hollywood") || text.contains("celebrity")) return "ENTERTAINMENT";
        
        // Fallback ke nama sumber jika tidak ada kata kunci yang cocok
        return (article.getSource() != null && article.getSource().getName() != null) 
                ? article.getSource().getName().toUpperCase() : "GENERAL";
    }

    private int badgeColor(String cat) {
        String c = cat.toUpperCase();
        if (c.contains("TECH"))     return Color.parseColor("#1565C0"); // Blue
        if (c.contains("SPORT"))    return Color.parseColor("#C62828"); // Red
        if (c.contains("BUSINESS")) return Color.parseColor("#E65100"); // Orange
        if (c.contains("HEALTH"))   return Color.parseColor("#2E7D32"); // Green
        if (c.contains("SCIENCE"))  return Color.parseColor("#6A1B9A"); // Purple
        if (c.contains("POLITIC"))  return Color.parseColor("#37474F"); // Grey Blue
        if (c.contains("ENTERTAIN"))return Color.parseColor("#D81B60"); // Pink
        return Color.parseColor("#1A8A6B"); // Default teal
    }

    private String formatTimeAgo(String publishedAt) {
        if (publishedAt == null || publishedAt.isEmpty()) return "Just now";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(publishedAt);
            if (date == null) return "Just now";
            long mins = (System.currentTimeMillis() - date.getTime()) / 60_000;
            if (mins < 1)    return "Just now";
            if (mins < 60)   return mins + " mins ago";
            if (mins < 1440) return (mins / 60) + " hrs ago";
            return (mins / 1440) + " days ago";
        } catch (Exception e) {
            return "Just now";
        }
    }
}
