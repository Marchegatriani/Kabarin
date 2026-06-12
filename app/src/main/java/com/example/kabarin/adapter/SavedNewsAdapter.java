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
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class SavedNewsAdapter extends RecyclerView.Adapter<SavedNewsAdapter.ViewHolder> {

    private final List<Article> articles;
    private OnItemClickListener listener;
    private OnRemoveClickListener removeListener;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(Article article);
    }

    public SavedNewsAdapter(List<Article> articles) {
        this.articles = articles;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_articles, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.tvTitle.setText(article.getTitle());
        
        String displayLabel = inferCategory(article);
        holder.chipCategory.setText(displayLabel);
        holder.chipCategory.setTextColor(badgeColor(displayLabel));

        holder.tvTime.setText(formatTimeAgo(article.getPublishedAt()));

        Glide.with(holder.ivImage.getContext())
                .load(article.getUrlToImage())
                .placeholder(R.color.selector_chip_bg)
                .error(R.color.selector_chip_bg)
                .centerCrop()
                .into(holder.ivImage);

        holder.ivBookmark.setImageResource(R.drawable.ic_save);
        holder.ivBookmark.setColorFilter(Color.parseColor("#BA1A1A"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(article);
        });

        holder.ivBookmark.setOnClickListener(v -> {
            if (removeListener != null) removeListener.onRemoveClick(article);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    private String inferCategory(Article article) {
        String text = (nvl(article.getTitle(), "") + " " + 
                      nvl(article.getDescription(), "") + " " + 
                      (article.getSource() != null ? article.getSource().getName() : "")).toLowerCase();

        if (text.contains("tech") || text.contains("google") || text.contains("apple") || text.contains("software") || text.contains(" ai ")) return "TECHNOLOGY";
        if (text.contains("sport") || text.contains("football") || text.contains("soccer") || text.contains(" match")) return "SPORTS";
        if (text.contains("business") || text.contains("stock") || text.contains("market") || text.contains("economy")) return "BUSINESS";
        if (text.contains("health") || text.contains("doctor") || text.contains("medical") || text.contains("virus")) return "HEALTH";
        if (text.contains("science") || text.contains("nasa") || text.contains("space") || text.contains("research")) return "SCIENCE";
        if (text.contains("politic") || text.contains("government") || text.contains("election")) return "POLITICS";
        if (text.contains("movie") || text.contains("music") || text.contains("entertainment") || text.contains("hollywood")) return "ENTERTAINMENT";
        
        return (article.getSource() != null && article.getSource().getName() != null) 
                ? article.getSource().getName().toUpperCase() : "GENERAL";
    }

    private int badgeColor(String cat) {
        String c = cat.toUpperCase();
        if (c.contains("TECH"))     return Color.parseColor("#1565C0");
        if (c.contains("SPORT"))    return Color.parseColor("#C62828");
        if (c.contains("BUSINESS")) return Color.parseColor("#E65100");
        if (c.contains("HEALTH"))   return Color.parseColor("#2E7D32");
        if (c.contains("SCIENCE"))  return Color.parseColor("#6A1B9A");
        if (c.contains("POLITIC"))  return Color.parseColor("#37474F");
        if (c.contains("ENTERTAIN"))return Color.parseColor("#D81B60");
        return Color.parseColor("#1A8A6B");
    }

    private String nvl(String s, String fallback) {
        return (s != null && !s.isEmpty()) ? s : fallback;
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage, ivBookmark;
        TextView tvTitle, tvTime;
        Chip chipCategory;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivArticleImage);
            ivBookmark = itemView.findViewById(R.id.ivBookmark);
            tvTitle = itemView.findViewById(R.id.tvArticleTitle);
            tvTime = itemView.findViewById(R.id.tvSavedTime);
            chipCategory = itemView.findViewById(R.id.chipCategory);
        }
    }
}
