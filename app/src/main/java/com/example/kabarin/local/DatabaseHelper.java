package com.example.kabarin.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.kabarin.model.Article;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "kabarin_news.db";
    private static final int DATABASE_VERSION = 2; // Naikkan versi karena ada tabel baru

    // Tabel Berita (Cache)
    private static final String TABLE_NEWS = "news_cache";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_ARTICLE_URL = "article_url";
    private static final String COLUMN_SOURCE = "source";
    private static final String COLUMN_PUBLISHED_AT = "published_at";

    // Tabel User
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_NAME = "name";
    private static final String COL_USER_PASSWORD = "password";
    private static final String COL_USER_NICKNAME = "nickname";
    private static final String COL_USER_LOCATION = "location";
    private static final String COL_USER_IMAGE = "image_uri";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_NEWS_TABLE = "CREATE TABLE " + TABLE_NEWS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " TEXT,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_IMAGE_URL + " TEXT,"
                + COLUMN_ARTICLE_URL + " TEXT,"
                + COLUMN_SOURCE + " TEXT,"
                + COLUMN_PUBLISHED_AT + " TEXT" + ")";
        db.execSQL(CREATE_NEWS_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_EMAIL + " TEXT PRIMARY KEY,"
                + COL_USER_NAME + " TEXT,"
                + COL_USER_PASSWORD + " TEXT,"
                + COL_USER_NICKNAME + " TEXT,"
                + COL_USER_LOCATION + " TEXT,"
                + COL_USER_IMAGE + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                    + COL_USER_EMAIL + " TEXT PRIMARY KEY,"
                    + COL_USER_NAME + " TEXT,"
                    + COL_USER_PASSWORD + " TEXT,"
                    + COL_USER_NICKNAME + " TEXT,"
                    + COL_USER_LOCATION + " TEXT,"
                    + COL_USER_IMAGE + " TEXT" + ")";
            db.execSQL(CREATE_USERS_TABLE);
        }
    }

    // --- User Methods ---

    public boolean addUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_EMAIL, email);
        values.put(COL_USER_PASSWORD, password);
        values.put(COL_USER_NICKNAME, name.toLowerCase().replace(" ", ""));
        values.put(COL_USER_LOCATION, "Indonesia");

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    public Cursor getUserData(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COL_USER_EMAIL + "=?", new String[]{email}, null, null, null);
    }

    public void updateUserData(String email, String name, String nick, String loc, String img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, name);
        values.put(COL_USER_NICKNAME, nick);
        values.put(COL_USER_LOCATION, loc);
        values.put(COL_USER_IMAGE, img);
        db.update(TABLE_USERS, values, COL_USER_EMAIL + "=?", new String[]{email});
    }

    // --- News Cache Methods ---

    public void clearCache() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NEWS, null, null);
    }

    public void saveArticles(List<Article> articles) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Article article : articles) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TITLE, article.getTitle());
                values.put(COLUMN_DESCRIPTION, article.getDescription());
                values.put(COLUMN_IMAGE_URL, article.getUrlToImage());
                values.put(COLUMN_ARTICLE_URL, article.getUrl());
                values.put(COLUMN_SOURCE, article.getSource() != null ? article.getSource().getName() : "Unknown");
                values.put(COLUMN_PUBLISHED_AT, article.getPublishedAt());
                db.insert(TABLE_NEWS, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<Article> getCachedArticles() {
        List<Article> articles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NEWS, null, null, null, null, null, COLUMN_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
                String articleUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTICLE_URL));
                String sourceName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOURCE));
                String publishedAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PUBLISHED_AT));

                articles.add(new Article(title, description, imageUrl, articleUrl, sourceName, publishedAt));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return articles;
    }
}
