/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetCursorAdapter;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = CatalogActivity.class.getSimpleName();
    private static final int UNIQUE_ID_FOR_LOADER = 0;
    /** This is the adapter being used to display the list of pets */
    private PetCursorAdapter mPetCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView listView = (ListView)findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        // Create an empty adapter we will use to display the loaded data
        // i.e. initialize the adapter and set it to the listView
        mPetCursorAdapter = new PetCursorAdapter(this, null);
        listView.setAdapter(mPetCursorAdapter);


        // Prepare the loader. Either re-connect with an existing one,
        // or start a new one.
        // Note: we do not need to capture a reference to this loader because
        // the LoaderManager manages the life of the loader automatically
        getSupportLoaderManager().initLoader(UNIQUE_ID_FOR_LOADER, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long idOfItem) {
                Intent intent = new Intent(getApplication(), EditorActivity.class);

                Uri currentPetUri = ContentUris.withAppendedId(PetContract.PetEntry.CONTENT_URI, idOfItem);
                // Set the URI on the data field of the intent
                intent.setData(currentPetUri);

                startActivity(intent);

                Log.v(LOG_TAG, "URI: " + currentPetUri);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_andy_is_cool:
                updatePet();
                return true;
            case R.id.action_delete_all_entries:
                deletePets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertPet() {
        // Create a new map of values, where column names are the keys
        // This will contain the new values to insert
        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_NAME_NAME, "Andy");
        values.put(PetContract.PetEntry.COLUMN_NAME_BREED, "Terrier");
        values.put(PetContract.PetEntry.COLUMN_NAME_GENDER, PetContract.PetEntry.GENDER_MALE);
        values.put(PetContract.PetEntry.COLUMN_NAME_WEIGHT, 7);

        // Defines a new Uri object that receives the result of the insertion
        Uri mNewUri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, values);

        Log.v("CatalogActivity", "new row created: " + mNewUri);
    }

    private void updatePet() {
        // Defines an object to contain the updated values
        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_NAME_BREED, "Danger");
        // Defines selection criteria for the rows you want to update
        String selection = PetContract.PetEntry.COLUMN_NAME_NAME + "=?";
        String[] selectionArgs = {"Andy"};

        int rowsAffected = getContentResolver().update(PetContract.PetEntry.CONTENT_URI,
                values,
                selection,
                selectionArgs);
    }

    private void deletePets() {
        int rowsAffected = getContentResolver().delete(PetContract.PetEntry.CONTENT_URI,
                null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Projection used to perform query
        String[] projection = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_NAME_BREED,
                PetContract.PetEntry.COLUMN_NAME_NAME,
                PetContract.PetEntry.COLUMN_NAME_GENDER,
                PetContract.PetEntry.COLUMN_NAME_WEIGHT};

        return new CursorLoader(getApplicationContext(),
                PetContract.PetEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Swap the new cursor in. Note: The framework will take care of closing
        // the old cursor once we return.
        mPetCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mPetCursorAdapter.swapCursor(null);

    }
}
