package com.example.kabarin.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.kabarin.R;
import com.example.kabarin.activity.WelcomeActivity;
import com.example.kabarin.local.DatabaseHelper;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ProfileFragment extends Fragment {

    private ShapeableImageView ivProfilePhoto;
    private TextView tvUserName, tvMemberBadge, tvUserBio;
    private SwitchMaterial switchDarkMode;
    private SharedPreferences prefs;
    private DatabaseHelper dbHelper;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        dbHelper = new DatabaseHelper(requireContext());

        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvMemberBadge = view.findViewById(R.id.tvMemberBadge);
        tvUserBio = view.findViewById(R.id.tvUserBio);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        updateUI();

        boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
        switchDarkMode.setChecked(isDarkMode);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("isDarkMode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        view.findViewById(R.id.frameProfilePicture).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_nav_profile_to_nav_edit_profile);
        });

        setupSignOut(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (!isAdded()) return;

        String currentEmail = prefs.getString("currentUserEmail", "");
        Cursor cursor = dbHelper.getUserData(currentEmail);

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
            String location = cursor.getString(cursor.getColumnIndexOrThrow("location"));
            String photoUri = cursor.getString(cursor.getColumnIndexOrThrow("image_uri"));

            tvUserName.setText(name);
            tvMemberBadge.setText("@" + nickname);
            tvUserBio.setText(location);

            if (photoUri != null && !photoUri.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(photoUri))
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(ivProfilePhoto);
            } else {
                ivProfilePhoto.setImageResource(R.drawable.ic_profile);
            }
            cursor.close();
        }
    }

    private void setupSignOut(View view) {
        view.findViewById(R.id.layoutSignOut).setOnClickListener(v -> 
            new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya", (d, w) -> {
                    prefs.edit().putBoolean("isLogin", false).apply();
                    prefs.edit().remove("currentUserEmail").apply(); // Hapus email saat logout
                    
                    Intent intent = new Intent(requireContext(), WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Tidak", null)
                .show());
    }
}
