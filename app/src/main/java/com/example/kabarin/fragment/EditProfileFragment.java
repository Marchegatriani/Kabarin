package com.example.kabarin.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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

public class EditProfileFragment extends Fragment {

    private TextInputEditText etFullName, etUsername, etLocation;
    private TextInputLayout tlLocation;
    private ShapeableImageView ivProfilePhoto;
    private FrameLayout frameProfilePicture;
    private Button btnSave;
    private Toolbar toolbar;
    private SharedPreferences prefs;
    private FusedLocationProviderClient fusedLocationClient;
    private Uri selectedImageUri;

    // Launcher untuk Galeri
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    // Penting: Ambil hak akses baca persisten untuk URI ini agar bisa dibaca di halaman lain/setelah restart
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                    Glide.with(this).load(uri).into(ivProfilePhoto);
                }
            });

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

    public EditProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        toolbar = view.findViewById(R.id.toolbar);
        ivProfilePhoto = view.findViewById(R.id.ivProfilePhoto);
        frameProfilePicture = view.findViewById(R.id.frameProfilePicture);
        etFullName = view.findViewById(R.id.etFullName);
        etUsername = view.findViewById(R.id.etUsername);
        etLocation = view.findViewById(R.id.etLocation);
        tlLocation = view.findViewById(R.id.tlLocation);
        btnSave = view.findViewById(R.id.btnSave);

        // Load current data
        String currentName = prefs.getString("userName", "User");
        String currentUsername = prefs.getString("userNickname", "username");
        String currentLocation = prefs.getString("userLocation", "Location");
        String currentPhotoUri = prefs.getString("profileUri", null);

        etFullName.setText(currentName);
        etUsername.setText(currentUsername);
        etLocation.setText(currentLocation);
        
        if (currentPhotoUri != null) {
            Glide.with(this).load(Uri.parse(currentPhotoUri)).placeholder(R.drawable.ic_profile).into(ivProfilePhoto);
        }

        frameProfilePicture.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        tlLocation.setEndIconOnClickListener(v -> checkLocationPermission());

        btnSave.setOnClickListener(v -> {
            String newName = etFullName.getText().toString();
            String newUsername = etUsername.getText().toString();
            String newLocation = etLocation.getText().toString();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("userName", newName);
            editor.putString("userNickname", newUsername);
            editor.putString("userLocation", newLocation);
            
            if (selectedImageUri != null) {
                editor.putString("profileUri", selectedImageUri.toString());
            }
            
            // Gunakan commit() alih-alih apply() agar perubahan langsung tersimpan secara sinkron
            // sebelum kita kembali (navigateUp)
            editor.commit();

            Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
        });
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
