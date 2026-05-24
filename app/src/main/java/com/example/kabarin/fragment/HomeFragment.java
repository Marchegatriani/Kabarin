package com.example.kabarin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kabarin.R;
import com.example.kabarin.model.News;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment untuk menampilkan daftar berita terbaru dan trending.
 */
public class HomeFragment extends Fragment {

    private RecyclerView rvTrending, rvLatestNews;
    private ChipGroup chipGroupCategories;
    private FloatingActionButton fabSearch;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout fragment_home
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inisialisasi View menggunakan findViewById
        rvTrending = view.findViewById(R.id.rvTrending);
        rvLatestNews = view.findViewById(R.id.rvLatestNews);
        chipGroupCategories = view.findViewById(R.id.chipGroupCategories);
        fabSearch = view.findViewById(R.id.fab);

        // 2. Setup RecyclerView dengan data dummy
        setupRecyclerViews();

        // 3. Listener untuk Chip Group (Kategori)
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                // Nantinya bisa digunakan untuk filter berita berdasarkan kategori
                Toast.makeText(getContext(), "Filter kategori aktif", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Listener untuk FAB Search
        fabSearch.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Fitur pencarian segera hadir", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerViews() {
        // Membuat list data dummy
        List<News> dummyNewsList = new ArrayList<>();
        dummyNewsList.add(new News("Terobosan Teknologi AI 2024", "Technology", "AI semakin mendominasi pasar global..."));
        dummyNewsList.add(new News("Timnas Indonesia Menang Telak", "Sports", "Pertandingan semalam berakhir dengan skor 3-0..."));
        dummyNewsList.add(new News("IHSG Menguat Hari Ini", "Business", "Pasar saham menunjukkan tren positif..."));

        // Setup RecyclerView Trending (Horizontal)
        rvTrending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        // Catatan: Pasang adapter di sini nanti, misal: rvTrending.setAdapter(new NewsAdapter(dummyNewsList));

        // Setup RecyclerView Latest News (Vertical)
        rvLatestNews.setLayoutManager(new LinearLayoutManager(getContext()));
        // Catatan: rvLatestNews.setAdapter(new NewsAdapter(dummyNewsList));
        
        // Pesan indikator untuk menunjukkan struktur sudah siap
        Toast.makeText(getContext(), "Daftar berita siap dimuat", Toast.LENGTH_SHORT).show();
    }
}
