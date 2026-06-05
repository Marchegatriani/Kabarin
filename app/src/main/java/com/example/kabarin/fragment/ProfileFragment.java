package com.example.kabarin.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.kabarin.R;
import com.example.kabarin.activity.SplashActivity;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Memuat layout fragment_profile
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inisialisasi Burger Menu (ivMenu)
        View ivMenu = view.findViewById(R.id.ivMenu);
        if (ivMenu != null) {
            ivMenu.setOnClickListener(v -> Toast.makeText(getContext(), "Menu clicked", Toast.LENGTH_SHORT).show());
        }

        // Inisialisasi Layout Sign Out
        LinearLayout layoutSignOut = view.findViewById(R.id.layoutSignOut);
        
        layoutSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Memunculkan Window Konfirmasi (AlertDialog)
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 1. Hapus session login di SharedPreferences
                                SharedPreferences prefs = requireActivity().getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
                                prefs.edit().putBoolean("isLogin", false).apply();

                                // 2. Tampilkan pesan Toast
                                Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

                                // 3. Navigasi ke SplashActivity (Tampilan awal)
                                Intent intent = new Intent(getActivity(), SplashActivity.class);
                                
                                // Hapus stack activity agar user tidak bisa kembali
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                                // 4. Tutup activity saat ini
                                if (getActivity() != null) {
                                    getActivity().finish();
                                }
                            }
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
    }
}
