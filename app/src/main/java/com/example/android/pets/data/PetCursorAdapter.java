package com.example.android.pets.data;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.R;

/**
 * {@link PetCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */

public class PetCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link PetCursorAdapter}.
     *
     * @param context the context
     * @param c is the cursor from which to get the data
     */
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0/* flags */);
    }

    /**
     * Makes a new blank list item view. Note: No data is set (or bound) to the views yet.
     *
     * @param context app context (Interface to application's global information)
     * @param cursor The cursor form which to get the data. The cursor is already
     *               moved to the correct position.
     * @param parent The parent to which the new view is attached
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // TODO: Fill out this method and return the list item view (instead of null)
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor The cursor from which to get the data. The cursor is already moved to the
     *               correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // TODO: Fill out this method
        // Find fields to populate in the inflated template
        TextView name = (TextView)view.findViewById(R.id.name);
        TextView summary = (TextView)view.findViewById(R.id.summary);
        // Extract the properties from the cursor
        String nameString = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        String summaryString = cursor.getString(cursor.getColumnIndexOrThrow("breed"));
        // Populate the fields using the extracted data
        name.setText(nameString);
        summary.setText(summaryString);

    }
}
