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

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    private static final int UNIQUE_ID_FOR_LOADER = 0;

    /**
     * Boolean to listen for whether the user has changed anything
     */
    private boolean mPetHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    /**
     * Populated by the intent from CatalogActivity
     */
    private Uri mCurrentPetUri;

    /**
     * EditText field to enter the pet's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Use getIntent() and getData() to get the associated URI
        mCurrentPetUri = getIntent().getData();
        Log.v(LOG_TAG, "Data captured! Uri: " + mCurrentPetUri);
        // Set title of EditorActivity on which situation we have
        // If the Editor Activity was opened using the ListView item, then we will
        // have uri of pet so change app bar to say "Edit Pet"
        // Otherwise if this is a new pet, uri is null so change app bar to say "Add Pet"
        if (mCurrentPetUri != null) {
            // This is an edited pet
            setTitle("Edit Pet");
            // Prepare the loader
            getSupportLoaderManager().initLoader(UNIQUE_ID_FOR_LOADER, null, this);

            } else {
            // This is a new pet
            setTitle("Add Pet");
            // Invalidate the options menu, so the "Delete" menu option can be hidden
            invalidateOptionsMenu();
            }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);
        // Now set touch listeners on each of these
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hid the "delete" menu item
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the "Keep Editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes
        // should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetContract.PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetContract.PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetContract.PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = PetContract.PetEntry.GENDER_UNKNOWN; // Unknown
            }
        });
    }

    /**
     * Get user input from editor and save new pet into database
     */
    private void savePet() {

        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        // If all the fields are empty (i.e. the user hit save pet on accident,
        // return early without crashing the app
        if (mCurrentPetUri == null &&
                TextUtils.isEmpty(mNameEditText.getText()) &&
                TextUtils.isEmpty(mBreedEditText.getText()) &&
                TextUtils.isEmpty(Integer.toString(mGender))) {
            return;
        }
        // If the user forgets to add a weight, the default value is '0'
        int weightInteger = 0;
        if (!TextUtils.isEmpty(Integer.toString(weightInteger))) {
            weightInteger = Integer.parseInt(mWeightEditText.getText().toString().trim());
        }
        ContentValues value = new ContentValues();
        value.put(PetContract.PetEntry.COLUMN_NAME_NAME, nameString);
        value.put(PetContract.PetEntry.COLUMN_NAME_BREED, breedString);
        value.put(PetContract.PetEntry.COLUMN_NAME_GENDER, mGender);
        value.put(PetContract.PetEntry.COLUMN_NAME_WEIGHT, weightInteger);

        // Used to see whether the pet has been successfully saved
        int rowsAffected = 0;
        // Defines a new Uri Object that receives the result of the insertion
        // and insert new data into the database
        if (mCurrentPetUri == null) {
            // If mCurrentPetUri == null, then we are creating a new pet
            Uri mNewUri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI, value);
            rowsAffected = 1;
        } else {
            // if mCurrentPetUri != null, then we are editing an existing pet
            rowsAffected = getContentResolver().update(mCurrentPetUri, value, null, null);
        }

        String toastMessage;
        if (rowsAffected == 0) {
            toastMessage = getString(R.string.toast_error);
        } else {
            toastMessage = getString(R.string.toast_success);
        }

        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method to check if the user-entered fields are suitable for a database entry.
     *
     * @return
     */
    private Boolean checkFields() {
        if (TextUtils.isEmpty(mNameEditText.getText())
                && TextUtils.isEmpty(mBreedEditText.getText())
                && TextUtils.isEmpty(mWeightEditText.getText())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (checkFields()) {
                    // Save pet to database
                    savePet();
                    // Exit activity
                    finish();
                } else {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mPetHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                mCurrentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Proceed with moving to the first row of the cursor and reading data from it
        // This this be the only row in the cursor
        if (data.moveToFirst()) {
            int nameColumnIndex = data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_NAME_NAME);
            int breedColumnIndex = data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_NAME_BREED);
            int genderColumnIndex = data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_NAME_GENDER);
            int weightColumnIndex = data.getColumnIndexOrThrow(PetContract.PetEntry.COLUMN_NAME_WEIGHT);

            mNameEditText.setText(data.getString(nameColumnIndex));
            mBreedEditText.setText(data.getString(breedColumnIndex));
            mWeightEditText.setText(Integer.toString(data.getInt(weightColumnIndex)));
            // If the gender is know, set the spinner to that value. If not, set it to unknown
            if (data.getInt(genderColumnIndex) == PetContract.PetEntry.GENDER_MALE ||
                    data.getInt(genderColumnIndex) == PetContract.PetEntry.GENDER_FEMALE) {
                mGenderSpinner.setSelection(data.getInt(genderColumnIndex));
            } else {
                mGenderSpinner.setSelection(PetContract.PetEntry.GENDER_UNKNOWN);
            }

            Log.v(LOG_TAG, "********************************Let's see what we got: " + data.getInt(genderColumnIndex));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNameEditText.setText("");
        mBreedEditText.setText("");

    }
}