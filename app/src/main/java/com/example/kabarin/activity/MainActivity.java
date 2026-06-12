package com.example.kabarin.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.kabarin.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private AppBarLayout appBarLayout;
    private ShapeableImageView ivToolbarProfile;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);
        if (appBarLayout == null && toolbar != null) {
            if (toolbar.getParent() instanceof AppBarLayout) {
                appBarLayout = (AppBarLayout) toolbar.getParent();
            }
        }
        setSupportActionBar(toolbar);
        
        ivToolbarProfile = findViewById(R.id.ivToolbarProfile);
        
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_saved, R.id.nav_profile)
                    .setOpenableLayout(drawer)
                    .build();

            NavigationUI.setupWithNavController(toolbar, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
            NavigationUI.setupWithNavController(bottomNav, navController);

            if (ivToolbarProfile != null) {
                ivToolbarProfile.setOnClickListener(v -> {
                    navController.navigate(R.id.nav_profile);
                });
                updateToolbarProfileImage();
            }

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                
                View container = findViewById(R.id.fragmentContainer);
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) container.getLayoutParams();
                
                int bottomNavHeight = (int) (80 * getResources().getDisplayMetrics().density);

                if (destId == R.id.nav_search) {
                    if (appBarLayout != null) {
                        appBarLayout.setVisibility(View.GONE);
                        appBarLayout.setExpanded(false, false);
                    }
                    params.setBehavior(null);
                    params.bottomMargin = 0; // Search full screen
                } else {
                    if (appBarLayout != null) {
                        appBarLayout.setVisibility(View.VISIBLE);
                        appBarLayout.setExpanded(true, false);
                    }
                    params.setBehavior(new AppBarLayout.ScrollingViewBehavior());
                    params.bottomMargin = bottomNavHeight; // Berikan ruang agar FAB tidak tertutup BottomNav

                    if (ivToolbarProfile != null) {
                        if (destId == R.id.nav_profile || destId == R.id.nav_edit_profile) {
                            ivToolbarProfile.setVisibility(View.GONE);
                        } else {
                            ivToolbarProfile.setVisibility(View.VISIBLE);
                            updateToolbarProfileImage(); 
                        }
                    }
                }
                container.setLayoutParams(params);
                container.requestLayout();
            });
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("profileUri".equals(key)) {
            updateToolbarProfileImage();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateToolbarProfileImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    public void updateToolbarProfileImage() {
        String photoUri = prefs.getString("profileUri", null);
        if (ivToolbarProfile != null) {
            if (photoUri != null && !photoUri.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(photoUri))
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(ivToolbarProfile);
            } else {
                ivToolbarProfile.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences("KabarinPrefs", Context.MODE_PRIVATE);
        if (!prefs.contains("isDarkMode")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
            boolean isDarkMode = prefs.getBoolean("isDarkMode", false);
            AppCompatDelegate.setDefaultNightMode(isDarkMode ? 
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
