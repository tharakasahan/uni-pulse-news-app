package com.yourusername.uniplus;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserInfoActivity extends BaseActivity {
    private static final String TAG = "UserInfoActivity";
    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";

    private TextView tvUsername, tvEmail, tvPhone;
    private Button btnUpdateInfo;
    private MaterialButton btnDeleteAccount;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;
    private MaterialButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initViews();
        setupBottomNavigation();
        setupDrawer();
        setupToolbarWithUsername();

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserDataFromFirebase(userId);
        }

        logoutButton.setOnClickListener(v -> showSignOutDialog());
        setupClickListeners();

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

    private void initViews() {
        tvUsername = findViewById(R.id.tv_username);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        btnUpdateInfo = findViewById(R.id.btn_update_info);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        logoutButton = findViewById(R.id.logoutButton);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
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
                    startActivity(new Intent(UserInfoActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                } else if (itemId == R.id.nav_user_info) {
                    // Already on User Info screen, no action needed
                } else if (itemId == R.id.nav_dev_info) {
                    startActivity(new Intent(UserInfoActivity.this, DeveloperInfoActivity.class));
                    overridePendingTransition(0, 0);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void setupBottomNavigation() {
        // Set the User Info item as selected when this activity loads
        bottomNavigation.setSelectedItemId(R.id.nav_user);
        bottomNavigation.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(UserInfoActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_user) {
                    // Already on User Info screen, no action needed
                    return true;
                } else if (itemId == R.id.nav_dev) {
                    startActivity(new Intent(UserInfoActivity.this, DeveloperInfoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void showSignOutDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.sign_out_dialog);
        dialog.setCancelable(true);

        Button btnOk = dialog.findViewById(R.id.btn_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        btnOk.setOnClickListener(v -> {
            performSignOut();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.y = 200;
            window.setAttributes(layoutParams);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }

    private void performSignOut() {
        Intent intent = new Intent(UserInfoActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserDataFromFirebase(String uid) {
        firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String phone = documentSnapshot.getString("phone");

                        String displayUsername = username != null ? username : "Guest User";
                        String displayEmail = email != null ? email : "guest@example.com";
                        String displayPhone = phone != null ? phone : "Not provided";

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_USERNAME, displayUsername);
                        editor.putString(KEY_EMAIL, displayEmail);
                        editor.apply();

                        tvUsername.setText(displayUsername);
                        tvEmail.setText(displayEmail);
                        tvPhone.setText(displayPhone);

                        Log.d(TAG, "User data fetched from Firebase: " + username + ", " + email);
                    } else {
                        Log.d(TAG, "No user document found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user data", e);
                    Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupClickListeners() {
        btnUpdateInfo.setOnClickListener(v -> showUpdateDialog());
    }

    private void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "User data cleared from SharedPreferences");
    }

    private void showUpdateDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_user);
        dialog.setCancelable(true);

        TextInputLayout tilUsername = dialog.findViewById(R.id.til_username);
        TextInputEditText etUsername = dialog.findViewById(R.id.et_username);

        TextInputLayout tilEmail = dialog.findViewById(R.id.til_email);
        TextInputEditText etEmail = dialog.findViewById(R.id.et_email);

        TextInputLayout tilPassword = dialog.findViewById(R.id.til_password);
        TextInputEditText etPassword = dialog.findViewById(R.id.et_password);

        Button btnSave = dialog.findViewById(R.id.btn_save);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        etUsername.setText(sharedPreferences.getString(KEY_USERNAME, ""));
        etEmail.setText(sharedPreferences.getString(KEY_EMAIL, ""));

        btnSave.setOnClickListener(v -> {
            String newUsername = etUsername.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newPassword = etPassword.getText().toString().trim();

            boolean valid = true;

            if (newUsername.isEmpty()) {
                tilUsername.setError("Username cannot be empty");
                valid = false;
            } else if (newUsername.length() < 3) {
                tilUsername.setError("Username must be at least 3 characters");
                valid = false;
            } else {
                tilUsername.setError(null);
            }

            if (newEmail.isEmpty()) {
                tilEmail.setError("Email cannot be empty");
                valid = false;
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                tilEmail.setError("Invalid email format");
                valid = false;
            } else {
                tilEmail.setError(null);
            }

            if (!newPassword.isEmpty() && newPassword.length() < 6) {
                tilPassword.setError("Password must be at least 6 characters");
                valid = false;
            } else {
                tilPassword.setError(null);
            }

            if (!valid) return;

            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                user.updateEmail(newEmail)
                        .addOnSuccessListener(unused -> {
                            firestore.collection("users").document(userId)
                                    .update("username", newUsername, "email", newEmail)
                                    .addOnSuccessListener(aVoid -> {
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(KEY_USERNAME, newUsername);
                                        editor.putString(KEY_EMAIL, newEmail);
                                        editor.apply();

                                        tvUsername.setText(newUsername);
                                        tvEmail.setText(newEmail);

                                        Toast.makeText(this, "User info updated", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update Firestore", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Firestore update failed", e);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Email update failed", e);
                        });

                if (!newPassword.isEmpty()) {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> Log.d(TAG, "Password updated"))
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Password update failed", e);
                            });
                }
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(R.drawable.dialog_background);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public static void saveUserData(android.content.Context context, String username, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }
}