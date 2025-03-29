package com.example.tp_sqlite.app.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.tp_sqlite.app.classes.Etudiant;
import com.example.tp_sqlite.app.util.MySQLiteHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EtudiantService {
    private static final String TABLE_NAME = "etudiant";

    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISSANCE = "date_naissance";
    private static final String KEY_IMAGE_PATH = "image_path";

    private static String[] COLUMNS = {KEY_ID, KEY_NOM, KEY_PRENOM};

    private MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    public void create(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        if (e.getDateNaissance() != null) {
            values.put(KEY_DATE_NAISSANCE, e.getDateNaissance().getTime());
        } // Ajouter la date de naissance
        values.put(KEY_IMAGE_PATH, e.getImagePath()); // Ajouter l'image
        db.insert(TABLE_NAME, null, values);
//        Log.d("datenaiss", e.getDateNaissance());
        Log.d("imgpath", e.getImagePath());
        db.close();
    }

    public void update(Etudiant e) {
        SQLiteDatabase db = null;
        try {
            db = this.helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_ID, e.getId());
            values.put(KEY_NOM, e.getNom());
            values.put(KEY_PRENOM, e.getPrenom());

            // Handle date properly - store as milliseconds since epoch
            if (e.getDateNaissance() != null) {
                values.put(KEY_DATE_NAISSANCE, e.getDateNaissance().getTime());
            } else {
                values.putNull(KEY_DATE_NAISSANCE);
            }

            // Handle image path
            if (e.getImagePath() != null && !e.getImagePath().isEmpty()) {
                values.put(KEY_IMAGE_PATH, e.getImagePath());
            } else {
                values.putNull(KEY_IMAGE_PATH);
            }

            int rowsAffected = db.update(
                    TABLE_NAME,
                    values,
                    "id = ?",
                    new String[]{String.valueOf(e.getId())}
            );

            if (rowsAffected == 0) {
                Log.w("EtudiantService", "No rows affected when updating student with ID: " + e.getId());
            } else {
                Log.d("EtudiantService", "Successfully updated student with ID: " + e.getId());
            }
        } catch (Exception ex) {
            Log.e("EtudiantService", "Error updating student", ex);
            throw new RuntimeException("Failed to update student", ex);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception ex) {
                    Log.e("EtudiantService", "Error closing database", ex);
                }
            }
        }
    }

    public Etudiant findById(int id) {
        Etudiant e = null;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = null;

        try {
            c = db.query(TABLE_NAME,
                    COLUMNS,
                    "id = ?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null);

            if (c != null && c.moveToFirst()) {
                e = new Etudiant();
                e.setId(c.getInt(0));  // Use column name instead of index for safety
                e.setNom(c.getString(1));
                e.setPrenom(c.getString(2));

                // Handle date field, check for null values properly
                long dateMillis = c.getLong(3);
                if (!c.isNull(4)) {
                    e.setDateNaissance(new Date(dateMillis));
                }

                e.setImagePath(c.getString(5));
            }
        } catch (Exception ex) {
            Log.e("EtudiantService", "Error retrieving student by ID", ex);
        } finally {
            if (c != null) {
                c.close();
            }
            db.close();
        }

        return e;
    }


    public void delete(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        db.delete(TABLE_NAME,
                "id = ?",
                new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public List<Etudiant> findAll() {
        List<Etudiant> eds = new ArrayList<>();
        String req = "select * from " + TABLE_NAME;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = db.rawQuery(req, null);
        Etudiant e = null;
        if (c.moveToFirst()) {
            do {
                e = new Etudiant();
                e.setId(c.getInt(0));
                e.setNom(c.getString(1));
                e.setPrenom(c.getString(2));
                long dateMillis = c.getLong(3);
                if (!c.isNull(3)) {
                    e.setDateNaissance(new Date(dateMillis));
                }

                e.setImagePath(c.getString(4));
                eds.add(e);
                Log.d("id = ", e.getId() + "");
            } while (c.moveToNext());
        }
        return eds;
    }
}


