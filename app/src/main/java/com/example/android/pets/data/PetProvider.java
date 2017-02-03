package com.example.android.pets.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static com.example.android.pets.data.PetContract.CONTENT_AUTHORITY;
import static com.example.android.pets.data.PetContract.PATH_PETS;

/**
 * Created by Zark on 1/23/2017.
 */

public class PetProvider extends ContentProvider {

    /** Tag for any log messages */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    /** Create and initialize a PetDbHelper object to gain access to the pets database. */
    private PetDbHelper mPetDbHelper;

    /** URI matcher code for the content URI for the pets table */
    private static final int PETS = 100;
    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PETS_ID = 101;

    /** Creates a UriMatcher object - "s" means static variable */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS);
        sUriMatcher.addURI(CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PETS_ID);
    }

    /**
     * Initialize the provider and the database helper object
     *
     * Note: A provider is not created until a ContentResolver object tries to access it.
     */
    @Override
    public boolean onCreate() {
        // TODO: Create and initialize a PetDbHelper object to gain access to the pets database.
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mPetDbHelper = new PetDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments,
     * and sort order.
     */
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {

        // Get the readable database
        SQLiteDatabase database = mPetDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                // The idea here is to perform a query on the pets table and enable the user to
                // capture the entire database.
                // Here the selection and selection arguments are null
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case PETS_ID:
                // For the PET_ID code, extract out the ID from the URI
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = PetContract.PetEntry._ID + "=?";
                // See this: https://developer.android.com/reference/android/content/ContentUris.html#parseId(android.net.Uri)
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table
                cursor = database.query(PetContract.PetEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * Insert new data inot the provider with the given ContentValues
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                // We do not want a case with PETS_ID
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     *
     * This returns the number of rows affected
     */
    @Override
    public int update(Uri uri,
                      ContentValues contentValues,
                      String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Delete the data at the given selection and selection arguments
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return deletePet(uri, selection, selectionArgs);
            case PETS_ID:
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return deletePet(uri, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Delection is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }


    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertPet(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(PetContract.PetEntry.COLUMN_NAME_NAME);
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        String breed = values.getAsString(PetContract.PetEntry.COLUMN_NAME_BREED);
        //if (breed == null || breed.equals("")) {
        //    throw new IllegalArgumentException("Pet requires a breed");
        //}
        int gender = values.getAsInteger(PetContract.PetEntry.COLUMN_NAME_GENDER);
        if (gender > 2) {
            throw new IllegalArgumentException("Pet requires a valid gender or 'unknown' if not known");
        }

        // Insert a new pet into the database table with the given ContentValues
        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();

        // Insert the pet into the database with the given ContentValues
        // Return the ID of the new row
        long id_value = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        // If the id_value = -1, then the insertion failed. Log an error and return null.
        if (id_value == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id_value);
    }

    /**
     * Update a database entry
     */
    private int updatePet(Uri uri,
                          ContentValues values,
                          String selection,
                          String[] selectionArgs) {
        // First, sanity check the data:
        if (values.containsKey(PetContract.PetEntry.COLUMN_NAME_NAME)) {
            String name = values.getAsString(PetContract.PetEntry.COLUMN_NAME_NAME);
            if (name == null || name.equals("")) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_NAME_BREED)) {
            String breed = values.getAsString(PetContract.PetEntry.COLUMN_NAME_BREED);
            if (breed == null || breed.equals("")) {
                throw new IllegalArgumentException("Pet requires a breed");
            }
        }
        if (values.containsKey(PetContract.PetEntry.COLUMN_NAME_GENDER)) {
            int gender = values.getAsInteger(PetContract.PetEntry.COLUMN_NAME_GENDER);
            if (gender > 2) {
                throw new IllegalArgumentException("Pet requires a valid gender or 'unknown' if not known");
            }
        }

        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();

        int rowsAffected = database.update(PetContract.PetEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsAffected;
    }

    /**
     * Delete entries from the database
     */
    private int deletePet(Uri uri,
                          String selection,
                          String[] selectionArgs) {

        SQLiteDatabase database = mPetDbHelper.getWritableDatabase();

        int numberOfRowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME,
                selection,
                selectionArgs);

        // Notify all listeners that the data has changed for the pet content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return numberOfRowsDeleted;
    }
}
