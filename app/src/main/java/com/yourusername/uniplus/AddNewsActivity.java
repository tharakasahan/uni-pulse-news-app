package com.yourusername.uniplus;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddNewsActivity extends BaseActivity {
    private static final String TAG = "AddNewsActivity";

    private TextInputEditText titleInput, subtitleInput, contentInput, dateInput;
    private Spinner spinnerNewsType;
    private ImageView imageView;
    private Button btnSelectImage, btnSubmit;
    private BottomNavigationView bottomNavigationView;

    private Uri selectedImageUri;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imageView.setImageURI(uri);
                    Log.d(TAG, "Image selected: " + uri.toString());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);

        initializeViews();
        setupBottomNavigation();
        setupFirebase();
        setupSpinner();
        setupClickListeners();

        setupToolbarWithUsername();

        ImageView dropdownIcon = findViewById(R.id.dropdown_icon);

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

    private void initializeViews() {
        titleInput = findViewById(R.id.input_news_title);
        subtitleInput = findViewById(R.id.input_news_subtitle);
        contentInput = findViewById(R.id.input_news_content);
        dateInput = findViewById(R.id.input_news_date);
        spinnerNewsType = findViewById(R.id.spinner_news_type);
        imageView = findViewById(R.id.image_news);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnSubmit = findViewById(R.id.btn_submit_news);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupBottomNavigation() {
        // Don't set any item as selected for AddNews activity
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(AddNewsActivity.this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }else if (itemId == R.id.nav_user) {
                    startActivity(new Intent(AddNewsActivity.this, UserInfoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.nav_dev) {
                    startActivity(new Intent(AddNewsActivity.this, DeveloperInfoActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupFirebase() {
        try {
            firestore = FirebaseFirestore.getInstance();
            storageReference = FirebaseStorage.getInstance().getReference();
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase", e);
            Toast.makeText(this, "Error initializing Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupSpinner() {
        // Create news type options
        String[] newsTypes = {"academic", "events", "sports"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, newsTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNewsType.setAdapter(adapter);
    }

    private void setupClickListeners() {
        dateInput.setOnClickListener(v -> showDatePicker());
        btnSelectImage.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnSubmit.setOnClickListener(v -> submitNews());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    dateInput.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void submitNews() {
        String title = titleInput.getText() != null ? titleInput.getText().toString().trim() : "";
        String subtitle = subtitleInput.getText() != null ? subtitleInput.getText().toString().trim() : "";
        String content = contentInput.getText() != null ? contentInput.getText().toString().trim() : "";
        String date = dateInput.getText() != null ? dateInput.getText().toString().trim() : "";
        String newsType = spinnerNewsType.getSelectedItem() != null ? spinnerNewsType.getSelectedItem().toString() : "";

        // Validation
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter news title", Toast.LENGTH_SHORT).show();
            titleInput.requestFocus();
            return;
        }

        if (subtitle.isEmpty()) {
            Toast.makeText(this, "Please enter news subtitle", Toast.LENGTH_SHORT).show();
            subtitleInput.requestFocus();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter news content", Toast.LENGTH_SHORT).show();
            contentInput.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newsType.isEmpty()) {
            Toast.makeText(this, "Please select news type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable submit button to prevent double submission
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        // Upload image first
        uploadImageAndSubmitNews(title, subtitle, content, date, newsType);
    }

    private void uploadImageAndSubmitNews(String title, String subtitle, String content, String date, String newsType) {
        String fileName = "news_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageReference.child(fileName);

        Log.d(TAG, "Starting image upload: " + fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Image uploaded successfully");
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                Log.d(TAG, "Image URL obtained: " + imageUrl);
                                saveNewsToFirestore(title, subtitle, content, date, newsType, imageUrl);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error getting download URL", e);
                                Toast.makeText(this, "Error getting image URL: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                resetSubmitButton();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Image upload failed", e);
                    Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetSubmitButton();
                });
    }

    private void saveNewsToFirestore(String title, String subtitle, String content, String date, String newsType, String imageUrl) {
        // Prepare news data map
        Map<String, Object> newsData = new HashMap<>();
        newsData.put("title", title);
        newsData.put("subtitle", subtitle);
        newsData.put("description", content); // Using 'description' to match your News model
        newsData.put("date", date);
        newsData.put("imageUrl", imageUrl);
        newsData.put("newsType", newsType);
        newsData.put("timestamp", System.currentTimeMillis()); // Add timestamp for ordering

        Log.d(TAG, "Saving news to Firestore: " + newsData.toString());

        // Save news document to Firestore
        firestore.collection("news")
                .add(newsData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "News document written with ID: " + documentReference.getId());
                    Toast.makeText(this, "News submitted successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();
                    finish(); // Go back to previous activity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding news document", e);
                    Toast.makeText(this, "Failed to submit news: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    resetSubmitButton();
                });
    }

    private void clearForm() {
        titleInput.setText("");
        subtitleInput.setText("");
        contentInput.setText("");
        dateInput.setText("");
        spinnerNewsType.setSelection(0);
        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        selectedImageUri = null;
    }

    private void resetSubmitButton() {
        btnSubmit.setEnabled(true);
        btnSubmit.setText("Submit News");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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