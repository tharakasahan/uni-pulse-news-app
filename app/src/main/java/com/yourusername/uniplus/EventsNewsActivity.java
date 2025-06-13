package com.yourusername.uniplus;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.yourusername.uniplus.model.News;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.yourusername.uniplus.R;
import com.yourusername.uniplus.model.News;

import java.util.ArrayList;
import java.util.List;

public class EventsNewsActivity extends BaseActivity {
    private static final String TAG = "EventsNewsActivity";
    private BottomNavigationView bottomNavigationView;
    private LinearLayout newsContainer;
    private FirebaseFirestore db;
    private List<News> eventsList;

    private MaterialButton academicBtn;
    private MaterialButton eventsBtn;
    private MaterialButton sportsBtn;


    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_news);

        initializeViews();
        setupBottomNavigation();
        setupFirestore();
        fetchEventsNews();
        setupDrawer();
        setupToolbarWithUsername();

// Ensure the toolbar navigation icon is set and tinted
        toolbar.setNavigationIconTint(Color.BLACK);
        ImageView dropdownIcon = findViewById(R.id.dropdown_icon);

        // Set up the drawer toggle after setting the tint
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Explicitly set the navigation icon to ensure tint applies
        toolbar.setNavigationIcon(R.drawable.ic_menu); // Replace with your menu icon drawable
        toolbar.setNavigationIconTint(Color.BLACK);

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
                    startActivity(new Intent(EventsNewsActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                } else if (itemId == R.id.nav_user_info) {
                    // Already on User Info screen, no action needed
                } else if (itemId == R.id.nav_dev_info) {
                    startActivity(new Intent(EventsNewsActivity.this, DeveloperInfoActivity.class));
                    overridePendingTransition(0, 0);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void initializeViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.getMenu().setGroupCheckable(0, false, true);

        newsContainer = findViewById(R.id.news_container);
        if (newsContainer == null) {
            Log.e(TAG, "news_container not found in layout");
        }

        academicBtn = findViewById(R.id.btn_academic);
        eventsBtn = findViewById(R.id.btn_events);
        sportsBtn = findViewById(R.id.btn_sports);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);

        academicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsNewsActivity.this, AcademicNewsActivity.class);
                startActivity(intent);
            }
        });
        sportsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsNewsActivity.this, SportsNewsActivity.class);
                startActivity(intent);
            }
        });
        eventsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsNewsActivity.this, EventsNewsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(EventsNewsActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }else if (itemId == R.id.nav_user) {
                    startActivity(new Intent(EventsNewsActivity.this, UserInfoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_dev) {
                    startActivity(new Intent(EventsNewsActivity.this, DeveloperInfoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
        eventsList = new ArrayList<>();
    }

    private void fetchEventsNews() {
        db.collection("news")
                .whereEqualTo("newsType", "events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            News news = document.toObject(News.class);
                            eventsList.add(news);
                            Log.d(TAG, "News fetched: " + news.getTitle());
                        }
                        displayNews();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(EventsNewsActivity.this, "Failed to fetch news", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching events news", e);
                    Toast.makeText(EventsNewsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayNews() {
        if (newsContainer == null) {
            Log.e(TAG, "newsContainer is null, cannot display news");
            return;
        }
        newsContainer.removeAllViews();
        for (News news : eventsList) {
            createNewsCard(news);
        }
        if (eventsList.isEmpty()) {
            showNoNewsMessage();
        }
    }

    private void createNewsCard(News news) {
        Typeface playRegular = ResourcesCompat.getFont(this, R.font.play_regular);
        Typeface playBold = ResourcesCompat.getFont(this, R.font.play_bold);

        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(dpToPx(8));
        cardView.setRadius(dpToPx(16));
        cardView.setCardBackgroundColor(Color.WHITE);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // TOP TITLE (above image)
        LinearLayout topTitleContainer = new LinearLayout(this);
        topTitleContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        topTitleContainer.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(8));
        topTitleContainer.setBackgroundColor(Color.WHITE);

        TextView topTitleTextView = new TextView(this);
        topTitleTextView.setText(news.getTitle());
        topTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        topTitleTextView.setTypeface(playBold);
        topTitleTextView.setTextColor(Color.BLACK);
        topTitleTextView.setMaxLines(2);
        topTitleTextView.setEllipsize(TextUtils.TruncateAt.END);

        topTitleContainer.addView(topTitleTextView);

        // Image View
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(180)
        );
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(news.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Content Layout
        LinearLayout contentLayout = new LinearLayout(this);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        contentLayout.setLayoutParams(contentParams);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(20));
        contentLayout.setBackgroundColor(Color.WHITE);

        // SECOND TITLE (below image, underlined)
        TextView titleBelowTextView = new TextView(this);
        titleBelowTextView.setText(news.getTitle());
        titleBelowTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        titleBelowTextView.setTypeface(playRegular);
        titleBelowTextView.setTextColor(Color.BLACK);
        titleBelowTextView.setMaxLines(2);
        titleBelowTextView.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams titleBelowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleBelowParams.setMargins(0, 0, 0, dpToPx(8));
        titleBelowTextView.setLayoutParams(titleBelowParams);

        // Date TextView
        TextView dateTextView = new TextView(this);
        dateTextView.setText(news.getDate());
        dateTextView.setTypeface(playRegular);
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        dateTextView.setTextColor(Color.parseColor("#666666"));
        LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        dateParams.setMargins(0, 0, 0, dpToPx(12));
        dateTextView.setLayoutParams(dateParams);

        // Description TextView
        TextView descriptionTextView = new TextView(this);
        descriptionTextView.setText(news.getDescription());
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        descriptionTextView.setTextColor(Color.parseColor("#333333"));
        descriptionTextView.setMaxLines(3);
        descriptionTextView.setTypeface(playRegular);
        descriptionTextView.setEllipsize(TextUtils.TruncateAt.END);
        descriptionTextView.setLineSpacing(dpToPx(2), 1.0f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.setMargins(0, 0, 0, dpToPx(16));
        descriptionTextView.setLayoutParams(descParams);

        // Button Layout
        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setGravity(Gravity.END);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonLayout.setLayoutParams(buttonLayoutParams);

        // Read News Button
        MaterialButton readButton = new MaterialButton(this);
        LinearLayout.LayoutParams readButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(40)
        );
        readButtonParams.setMargins(0, 0, dpToPx(12), 0);
        readButton.setLayoutParams(readButtonParams);
        readButton.setText("READ NEWS");
        readButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        readButton.setCornerRadius(dpToPx(0));
        readButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6200EE")));
        readButton.setTextColor(Color.WHITE);
        readButton.setPadding(dpToPx(16), 0, dpToPx(16), 0);
        readButton.setAllCaps(false);
        readButton.setTypeface(playRegular);
        readButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventsNewsActivity.this, NewsDetailActivity.class);
            intent.putExtra("news_title", news.getTitle());
            intent.putExtra("news_content", news.getContent());
            intent.putExtra("news_description", news.getDescription());
            intent.putExtra("news_date", news.getDate());
            intent.putExtra("news_image_url", news.getImageUrl());
            intent.putExtra("source_activity", "events");
            startActivity(intent);
        });

        buttonLayout.addView(readButton);
        contentLayout.addView(descriptionTextView);
        contentLayout.addView(buttonLayout);

        // Add to main layout IN CORRECT ORDER
        mainLayout.addView(topTitleContainer); // FIRST: Top title
        mainLayout.addView(imageView);         // SECOND: Image
        mainLayout.addView(contentLayout);     // THIRD: Content with second title, date, description, button

        cardView.addView(mainLayout);
        newsContainer.addView(cardView);
    }

    private TextView createTextView(String text, int textSize, boolean bold, int color) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(4), 0, 0);
        textView.setLayoutParams(params);
        textView.setText(text != null ? text : "");
        textView.setTextSize(textSize);
        textView.setTextColor(color);
        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }

    private void showNoNewsMessage() {
        TextView noNewsText = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(32), 0, 0);
        noNewsText.setLayoutParams(params);
        noNewsText.setText("No Events news available at the moment.");
        noNewsText.setTextSize(16);
        noNewsText.setTextColor(0xFF666666);
        noNewsText.setGravity(Gravity.CENTER);
        newsContainer.addView(noNewsText);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}