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

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private RecyclerView rvSavedArticles;
    // Jika di XML ada view empty state (misal: layout atau TextView), inisialisasi di sini
    // private View layoutEmptyState; 

    public SavedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inisialisasi RecyclerView menggunakan findViewById
        rvSavedArticles = view.findViewById(R.id.rvSavedArticles);
        // layoutEmptyState = view.findViewById(R.id.layoutEmpty); // Sesuaikan ID jika ada

        // 2. Setup RecyclerView dengan data dummy
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        // Simulasi data tersimpan (dummy)
        List<News> savedList = new ArrayList<>();
        savedList.add(new News("Judul Berita Tersimpan 1", "Politics", "Isi deskripsi berita..."));
        savedList.add(new News("Judul Berita Tersimpan 2", "Business", "Isi deskripsi berita..."));

        // 3. Logika Empty State
        if (savedList.isEmpty()) {
            rvSavedArticles.setVisibility(View.GONE);
            // if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvSavedArticles.setVisibility(View.VISIBLE);
            // if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);

            // Setup LayoutManager
            rvSavedArticles.setLayoutManager(new LinearLayoutManager(getContext()));
            
            // Placeholder: Pasang adapter di sini nanti
            // NewsAdapter adapter = new NewsAdapter(savedList);
            // rvSavedArticles.setAdapter(adapter);
            
            Toast.makeText(getContext(), "Berita tersimpan berhasil dimuat", Toast.LENGTH_SHORT).show();
        }
    }
}
