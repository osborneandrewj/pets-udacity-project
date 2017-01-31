package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by osbor on 1/13/2017.
 */

public class PetDbHelper extends SQLiteOpenHelper{

    /** Used to change the database version if the database schema changes */
    public static final int DATABASE_VERSION = 1;

    /** Name of the database file */
    public static final String DATABASE_NAME = "shelter.db";

    public PetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PetContract.PetEntry.SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //db.execSQL(PetContract.PetEntry.SQL_DELETE_ENTRIES);
        //onCreate(db);
    }
}
