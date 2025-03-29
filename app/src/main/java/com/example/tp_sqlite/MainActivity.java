package com.example.tp_sqlite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tp_sqlite.app.classes.Etudiant;
import com.example.tp_sqlite.app.service.EtudiantService;
import com.example.tp_sqlite.app.util.ListActiviy;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText nom, prenom, id;
    private Button add, btnDatePicker, btnChoisirImage, rechercher, delete, liste;
    private TextView dateNaissance, res;
    private ImageView imageEtudiant;
    private String selectedImageUri = "";
    private Date selectedDate = null;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EtudiantService es = new EtudiantService(this);

        initializeViews();
        setupDatePicker();
        setupImagePicker();
        setupButtons(es);
    }

    private void initializeViews() {
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        id = findViewById(R.id.id);
        add = findViewById(R.id.ajouter);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        dateNaissance = findViewById(R.id.dateNaissance);
        imageEtudiant = findViewById(R.id.imageEtudiant);
        btnChoisirImage = findViewById(R.id.btnChoisirImage);
        rechercher = findViewById(R.id.load);
        res = findViewById(R.id.res);
        delete = findViewById(R.id.delete);
        liste = findViewById(R.id.liste);
    }

    private void setupDatePicker() {
        btnDatePicker.setOnClickListener(v -> {
            // Using Material Date Picker for better UI
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Sélectionner la date de naissance")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate = new Date(selection);
                dateNaissance.setText("Date de Naissance: " + dateFormatter.format(selectedDate));
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });
    }

    private void setupImagePicker() {
        btnChoisirImage.setOnClickListener(v -> {
            Log.d("ImagePicker", "Bouton cliqué");
            if (!checkStoragePermission()) {
                Log.e("ImagePicker", "Permission non accordée !");
                return;
            }
            try {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                ((Activity) this).startActivityForResult(intent, ListActiviy.PICK_IMAGE_REQUEST);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "Aucune application trouvée pour sélectionner des images", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupButtons(EtudiantService es) {
        add.setOnClickListener(v -> handleAddStudent(es));
        rechercher.setOnClickListener(v -> handleSearchStudent(es));
        delete.setOnClickListener(v -> handleDeleteStudent(es));
        liste.setOnClickListener(v -> navigateToListActivity());
    }

    private void handleAddStudent(EtudiantService es) {
        String nomText = nom.getText().toString().trim();
        String prenomText = prenom.getText().toString().trim();

        if (nomText.isEmpty() || prenomText.isEmpty()) {
            showToast("Veuillez saisir le nom et le prénom");
            return;
        }

        if (selectedDate == null) {
            showToast("Veuillez sélectionner une date de naissance");
            return;
        }

        if (selectedImageUri.isEmpty()) {
            showToast("Veuillez sélectionner une image");
            return;
        }

        try {
            String imagePath = saveImageToInternalStorage();
            if (imagePath == null) return;

            Etudiant etudiant = new Etudiant(nomText, prenomText, selectedDate, imagePath);
            es.create(etudiant);
            showToast("Étudiant ajouté avec succès");
            clearFields();
        } catch (IOException e) {
            Log.e("AddStudent", "Error saving student", e);
            showToast("Erreur lors de l'ajout de l'étudiant");
        }
    }

    private String saveImageToInternalStorage() throws IOException {
        String fileName = "student_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(getFilesDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(Uri.parse(selectedImageUri));
             FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return imageFile.getAbsolutePath();
    }

    private void handleSearchStudent(EtudiantService es) {
        String idText = id.getText().toString().trim();
        if (idText.isEmpty()) {
            showToast("Veuillez entrer un ID");
            return;
        }

        try {
            Etudiant e = es.findById(Integer.parseInt(idText));
            res.setText(e != null ? e.getNom() + " " + e.getPrenom() : "Étudiant n'existe pas");
        } catch (NumberFormatException e) {
            showToast("ID invalide");
        }
    }

    private void handleDeleteStudent(EtudiantService es) {
        String idText = id.getText().toString().trim();
        if (idText.isEmpty()) {
            showToast("Veuillez entrer un ID");
            return;
        }

        try {
            Etudiant e = es.findById(Integer.parseInt(idText));
            if (e != null) {
                es.delete(e);
                showToast("Étudiant supprimé");
            } else {
                showToast("Étudiant n'existe pas");
            }
        } catch (NumberFormatException e) {
            showToast("ID invalide");
        }
    }

    private void navigateToListActivity() {
        startActivity(new Intent(this, ListActiviy.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ListActiviy.PICK_IMAGE_REQUEST) {
            if (data != null && data.getData() != null) {
                selectedImageUri = data.getData().toString();
                displaySelectedImage(selectedImageUri);
            } else {
                showToast("Aucune image sélectionnée");
            }
        }
    }

    private void displaySelectedImage(String imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(Uri.parse(imageUri))) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            imageEtudiant.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.e("ImageSelection", "Error displaying image", e);
            showToast("Erreur d'affichage de l'image");
        }
    }

    private void clearFields() {
        nom.setText("");
        prenom.setText("");
        id.setText("");
        dateNaissance.setText("Date de Naissance: ");
        imageEtudiant.setImageResource(0);
        selectedDate = null;
        selectedImageUri = "";
    }

    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) et supérieur
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10-12 (API 29-32)
            return true; // Pas besoin de permission pour lire MediaStore
        } else {
            // Android 9 et inférieur
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                return false;
            }
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permission accordée !");
            } else {
                showToast("Permission refusée, impossible de choisir une image");
            }
        }
    }
}