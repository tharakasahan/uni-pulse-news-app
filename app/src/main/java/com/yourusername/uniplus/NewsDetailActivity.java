package com.yourusername.uniplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

public class NewsDetailActivity extends BaseActivity {

    private ImageView newsImageView;
    private TextView newsTitleTextView;
    private TextView newsDateTextView;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private TextView newsDescriptionTextView;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private String sourceActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        MaterialButton backBtn = findViewById(R.id.back);

        sourceActivity = getIntent().getStringExtra("source_activity");
        if (sourceActivity == null) {
            sourceActivity = "home";
        }

        initializeViews();
        loadNewsData();
        setupBottomNavigation();

        // Only setup drawer if the layout contains drawer components
        if (drawerLayout != null && navView != null) {
            setupDrawer();
        }

        setupToolbarWithUsername();

        backBtn.setOnClickListener(v -> {
            Intent intent;
            switch (sourceActivity) {
                case "home":
                    intent = new Intent(NewsDetailActivity.this, HomeActivity.class);
                    break;
                case "events":
                    intent = new Intent(NewsDetailActivity.this, EventsNewsActivity.class);
                    break;
                case "sports":
                    intent = new Intent(NewsDetailActivity.this, SportsNewsActivity.class);
                    break;
                case "academic":
                default:
                    intent = new Intent(NewsDetailActivity.this, AcademicNewsActivity.class);
                    break;
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        ImageView dropdownIcon = findViewById(R.id.dropdown_icon);
        toolbar.setNavigationIconTint(Color.BLACK);

        // Only set up drawer toggle if drawer components exist
        if (drawerLayout != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            // Explicitly set the navigation icon to ensure tint applies
            toolbar.setNavigationIcon(R.drawable.ic_menu); // Replace with your menu icon drawable
            toolbar.setNavigationIconTint(Color.BLACK);
        }

        dropdownIcon.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.top_dropdown_menu, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_logout) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear backstack
                    startActivity(intent);
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(NewsDetailActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                } else if (itemId == R.id.nav_user_info) {
                    // Already on User Info screen, no action needed
                } else if (itemId == R.id.nav_dev_info) {
                    startActivity(new Intent(NewsDetailActivity.this, DeveloperInfoActivity.class));
                    overridePendingTransition(0, 0);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void initializeViews() {
        newsImageView = findViewById(R.id.news_detail_image);
        newsTitleTextView = findViewById(R.id.news_detail_title);
        newsDateTextView = findViewById(R.id.news_detail_date);
        newsDescriptionTextView = findViewById(R.id.news_detail_description);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Initialize drawer components - these may be null if not in layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
    }

    private void loadNewsData() {
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("news_title");
            String content = intent.getStringExtra("news_content");
            String description = intent.getStringExtra("news_description");
            String date = intent.getStringExtra("news_date");
            String imageUrl = intent.getStringExtra("news_image_url");

            // Set data to views
            newsTitleTextView.setText(title != null ? title : "No Title");
            newsDescriptionTextView.setText(description != null ? description : "No Description");
//            newsDateTextView.setText(date != null ? date : "No Date");

            // Load image
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(newsImageView);
            } else {
                newsImageView.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    private void setupBottomNavigation() {
        // Set the selected item based on source activity
        switch (sourceActivity) {
            case "home":
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
                break;
            case "events":
                bottomNavigationView.setSelectedItemId(R.id.nav_dev);
                break;
            case "academic":
            default:
                bottomNavigationView.setSelectedItemId(R.id.nav_user);
                break;
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(NewsDetailActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }else if (itemId == R.id.nav_user) {
                    startActivity(new Intent(NewsDetailActivity.this, UserInfoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_dev) {
                    startActivity(new Intent(NewsDetailActivity.this, DeveloperInfoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}