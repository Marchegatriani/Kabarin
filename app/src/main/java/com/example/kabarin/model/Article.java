package com.example.kabarin.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Article implements Serializable {
    @SerializedName("source")
    private Source source;
    @SerializedName("author")
    private String author;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("content")
    private String content;
    @SerializedName("urlToImage")
    private String urlToImage;
    @SerializedName("publishedAt")
    private String publishedAt;
    @SerializedName("url")
    private String url;

    public Article() {}
    public Article(String title, String description, String urlToImage, String url, String sourceName, String publishedAt) {
        this.title = title;
        this.description = description;
        this.urlToImage = urlToImage;
        this.url = url;
        this.source = new Source(sourceName);
        this.publishedAt = publishedAt;
    }

    public Source getSource() { return source; }
    public String getAuthor() { return author; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getContent() { return content; }
    public String getUrlToImage() { return urlToImage; }
    public String getPublishedAt() { return publishedAt; }
    public String getUrl() { return url; }

    public void setSource(Source source) { this.source = source; }
    public void setAuthor(String author) { this.author = author; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setContent(String content) { this.content = content; }
    public void setUrlToImage(String urlToImage) { this.urlToImage = urlToImage; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    public void setUrl(String url) { this.url = url; }

    public static class Source implements Serializable {
        @SerializedName("name")
        private String name;

        public Source() {}
        public Source(String name) { this.name = name; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
