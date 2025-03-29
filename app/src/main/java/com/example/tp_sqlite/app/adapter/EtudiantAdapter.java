package com.example.tp_sqlite.app.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tp_sqlite.R;
import com.example.tp_sqlite.app.classes.Etudiant;
import com.example.tp_sqlite.app.service.EtudiantService;
import com.example.tp_sqlite.app.util.ListActiviy;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {

    private Context context;
    private List<Etudiant> etudiants;
    private EtudiantService etudiantService;
    private String selectedImagePath = "";
    private ImageView currentImagePreview;
    private Date selectedDate;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public EtudiantAdapter(Context context, List<Etudiant> etudiants) {
        this.context = context;
        this.etudiants = etudiants;
        this.etudiantService = new EtudiantService(context);
    }

    public void updateList(List<Etudiant> newList) {
        this.etudiants = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new EtudiantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiants.get(position);
        holder.bind(etudiant);
        holder.itemView.setOnClickListener(v -> showActionDialog(etudiant, position));
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nom, prenom, dateNaissance;
        private final ImageView image;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            nom = itemView.findViewById(R.id.nom);
            prenom = itemView.findViewById(R.id.prenom);
            dateNaissance = itemView.findViewById(R.id.date_naissance);
            image = itemView.findViewById(R.id.imageEtud);
        }

        public void bind(Etudiant etudiant) {
            nom.setText("Nom: " + etudiant.getNom());
            prenom.setText("Prenom: " + etudiant.getPrenom());
            dateNaissance.setText("Date de naissance: " + etudiant.getFormattedDate());

            // Load image
            if (etudiant.getImagePath() != null && !etudiant.getImagePath().isEmpty()) {
                File imgFile = new File(etudiant.getImagePath());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    image.setImageBitmap(bitmap);
                } else {
                    image.setImageResource(R.drawable.ic_launcher_background);
                }
            } else {
                image.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }

    private void showActionDialog(Etudiant etudiant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choisir une action");
        builder.setMessage("Que voulez-vous faire avec cet étudiant ?");

        builder.setPositiveButton("Modifier", (dialog, which) -> {
            showEditDialog(etudiant, position);
        });

        builder.setNegativeButton("Supprimer", (dialog, which) -> {
            showDeleteConfirmationDialog(etudiant, position);
        });

        builder.setNeutralButton("Annuler", null);
        builder.create().show();
    }

    private void showDeleteConfirmationDialog(Etudiant etudiant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation");
        builder.setMessage("Êtes-vous sûr de vouloir supprimer cet étudiant ?");

        builder.setPositiveButton("Oui", (dialog, which) -> {
            etudiantService.delete(etudiant);
            etudiants.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(context, "Étudiant supprimé", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Non", null);
        builder.create().show();
    }

    private void showEditDialog(Etudiant etudiant, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.etudiant_edit_item, null);
        builder.setView(dialogView);

        TextInputEditText etNom = dialogView.findViewById(R.id.nom);
        TextInputEditText etPrenom = dialogView.findViewById(R.id.prenom);
        TextView tvDate = dialogView.findViewById(R.id.dateDisplay);
        ImageView ivPreview = dialogView.findViewById(R.id.image_preview);
        View btnChooseImage = dialogView.findViewById(R.id.choose_image_button);
        View btnDatePicker = dialogView.findViewById(R.id.btnDatePicker);

        // Initialize with current values
        selectedImagePath = etudiant.getImagePath() != null ? etudiant.getImagePath() : "";
        selectedDate =  etudiant.getDateNaissance();
        currentImagePreview = ivPreview;

        etNom.setText(etudiant.getNom());
        etPrenom.setText(etudiant.getPrenom());
        tvDate.setText(selectedDate != null ? dateFormatter.format(selectedDate) : "Date non sélectionnée");

        // Load current image
        if (etudiant.getImagePath() != null && !etudiant.getImagePath().isEmpty()) {
            File imgFile = new File(etudiant.getImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivPreview.setImageBitmap(bitmap);
            }
        }

        // Setup date picker
        btnDatePicker.setOnClickListener(v -> showMaterialDatePicker(tvDate));

        // Setup image picker
        btnChooseImage.setOnClickListener(v -> openImageChooser());

        builder.setPositiveButton("Enregistrer", (dialog, which) -> {
            etudiant.setNom(etNom.getText().toString());
            etudiant.setPrenom(etPrenom.getText().toString());
            etudiant.setDateNaissance(selectedDate);

            if (!selectedImagePath.isEmpty()) {
                etudiant.setImagePath(selectedImagePath);
            }

            etudiantService.update(etudiant);
            notifyItemChanged(position);
            Toast.makeText(context, "Étudiant mis à jour", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Annuler", null);
        builder.create().show();
    }

    private void showMaterialDatePicker(TextView dateView) {
        long selection = selectedDate != null ? selectedDate.getTime() : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Sélectionner la date de naissance")
                .setSelection(selection)
                .build();

        datePicker.addOnPositiveButtonClickListener(selectedTimestamp -> {
            selectedDate = new Date(selectedTimestamp);
            dateView.setText(dateFormatter.format(selectedDate));
        });

        datePicker.show(((AppCompatActivity) context).getSupportFragmentManager(), "DATE_PICKER");
    }

    private void openImageChooser() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            ((Activity) context).startActivityForResult(intent, ListActiviy.PICK_IMAGE_REQUEST);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "Aucune application trouvée pour sélectionner des images", Toast.LENGTH_SHORT).show();
        }
    }

    public void handleImageSelectionResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ListActiviy.PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                processSelectedImage(selectedImageUri);
            } else {
                Toast.makeText(context, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processSelectedImage(Uri imageUri) {
        try {
            // Create temp file for the image
            File tempFile = File.createTempFile("student_img_", ".jpg", context.getCacheDir());

            // Copy the image to temp file
            try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            selectedImagePath = tempFile.getAbsolutePath();
            displaySelectedImage(selectedImagePath);
        } catch (IOException e) {
            Log.e("EtudiantAdapter", "Error processing image", e);
            Toast.makeText(context, "Erreur de traitement de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySelectedImage(String imagePath) {
        if (currentImagePreview != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                currentImagePreview.setImageBitmap(bitmap);
            } else {
                Toast.makeText(context, "Impossible de charger l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}