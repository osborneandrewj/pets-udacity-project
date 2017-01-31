package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by osbor on 1/13/2017.
 */

public final class PetContract {

    /** String constant whose value is the same as that from the AndroidManifest.xml file */
    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";

    /** This URI will be shared by every URI associated with PetContract */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** This constant stores the path for each of the tables which will be appended to the base
     * content URI */
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PETS = "pets";

    // This class should never be instantiated
    private PetContract(){}

    public static class PetEntry implements BaseColumns {

        /** The content URI to access the pet data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        /**
         * The MIME type of the CONTENT_URI for a list of pets
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY
                        + "/" + PATH_PETS;
        /**
         * The MIME type of the CONTENT_URI for a single pet
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY
                        + "/" + PATH_PETS;

        /** Name of database table for pets */
        public static final String TABLE_NAME = "pets";

        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_BREED = "breed";
        public static final String COLUMN_NAME_GENDER = "gender";
        public static final String COLUMN_NAME_WEIGHT = "weight";

        /**
         * Possible values for gender of animal
         */
        public static final int GENDER_FEMALE = 2;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_UNKNOWN = 0;

        /**
         * Default value for weight which is 0
         */
        public static final int WEIGHT_DEFAULT = 0;

        public static final String SQL_CREATE_PETS_TABLE = "CREATE TABLE " +
                PetEntry.TABLE_NAME +
                " (" +
                PetEntry._ID + " INTEGER PRIMARY KEY," +
                PetEntry.COLUMN_NAME_NAME + " TEXT NOT NULL," +
                PetEntry.COLUMN_NAME_BREED + " TEXT," +
                PetEntry.COLUMN_NAME_GENDER + " INTEGER NOT NULL," +
                PetEntry.COLUMN_NAME_WEIGHT + " INTEGER NOT NULL DEFAULT 0)";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + PetEntry.TABLE_NAME;
    }

}
