package com.example.tp_sqlite.app.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;  // Augmente la version de la base de données

    private static final String DATABASE_NAME = "ecole";

    // Mise à jour de la requête CREATE_TABLE_ETUDIANT
    private static final String CREATE_TABLE_ETUDIANT = "create table etudiant(" +
            "id INTEGER primary key autoincrement," +
            "nom TEXT," +
            "prenom TEXT," +
            "date_naissance TEXT," +  // Nouvelle colonne pour la date de naissance
            "image_path TEXT)";       // Nouvelle colonne pour le chemin de l'image

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Création de la table avec les nouvelles colonnes
        db.execSQL(CREATE_TABLE_ETUDIANT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Gérer la mise à jour de la base de données (ici, on supprime la table existante et on la recrée)
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE etudiant ADD COLUMN date_naissance TEXT");  // Ajouter la colonne date_naissance
            db.execSQL("ALTER TABLE etudiant ADD COLUMN image_path TEXT");       // Ajouter la colonne image_path
        }
    }
}

