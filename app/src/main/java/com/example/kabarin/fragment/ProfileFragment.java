package com.example.kabarin.fragment;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.kabarin.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private ShapeableImageView ivProfilePhoto;
    private TextInputEditText etFullName, etBio, etLocation;
    private TextInputLayout tlLocation;
    private TextView tvSave;
    private SharedPreferences prefs;
    private FusedLocationProviderClient fusedLocationClient;

    // Launcher untuk Galeri
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // Berikan izin permanen untuk URI jika perlu, atau cukup tampilkan
                    saveImageUri(uri);
                    loadProfileImage(uri.toString());
                }
            });

    // Launcher untuk Permission Lokasi
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    getCurrentLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Izin lokasi ditolak", Toast.LENGTH_SHORT).show();
                }
            });

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        etFullName = view.findViewById(R.id.etFullName);
        etBio = view.findViewById(R.id.etBio);
        etLocation = view.findViewById(R.id.etLocation);
        tlLocation = view.findViewById(R.id.tlLocation);
        tvSave = view.findViewById(R.id.tvSave);

        // Load data tersimpan
        etFullName.setText(prefs.getString("userName", ""));
        etBio.setText(prefs.getString("userBio", ""));
        etLocation.setText(prefs.getString("userLocation", ""));
        loadProfileImage(prefs.getString("profileUri", null));

        // Event Klik Foto Profil
        ivProfilePhoto.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        // Event Klik Ambil Lokasi (Icon di ujung field)
        tlLocation.setEndIconOnClickListener(v -> checkLocationPermission());

        // Tombol Simpan
        tvSave.setOnClickListener(v -> {
            prefs.edit()
                    .putString("userName", etFullName.getText().toString())
                    .putString("userBio", etBio.getText().toString())
                    .putString("userLocation", etLocation.getText().toString())
                    .apply();
            Toast.makeText(getContext(), "Profil diperbarui", Toast.LENGTH_SHORT).show();
        });

        setupSignOut(view);
    }

    private void loadProfileImage(String uriString) {
        if (uriString != null) {
            Glide.with(this).load(Uri.parse(uriString)).into(ivProfilePhoto);
        }
    }

    private void saveImageUri(Uri uri) {
        prefs.edit().putString("profileUri", uri.toString()).apply();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                updateLocationField(location);
            } else {
                Toast.makeText(getContext(), "Gagal mendapatkan lokasi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocationField(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                String city = addresses.get(0).getLocality();
                if (city == null) city = addresses.get(0).getSubAdminArea();
                etLocation.setText(city);
                prefs.edit().putString("userLocation", city).apply();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupSignOut(View view) {
        view.findViewById(R.id.layoutSignOut).setOnClickListener(v -> 
            new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Yakin ingin keluar?")
                .setPositiveButton("Ya", (d, w) -> {
                    prefs.edit().putBoolean("isLogin", false).apply();
                    requireActivity().finish();
                })
                .setNegativeButton("Tidak", null)
                .show());
    }
}
