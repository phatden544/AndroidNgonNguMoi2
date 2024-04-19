package com.example.cuoiky;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class AddPlaylistFragment extends Fragment {

    private EditText editTextPlaylistName;
    private EditText editTextPlaylistDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_playlist, container, false);

        editTextPlaylistName = view.findViewById(R.id.editTextPlaylistName);
        editTextPlaylistDescription = view.findViewById(R.id.editTextPlaylistDescription);

        // Handle the Save Playlist button click event
        Button btnSavePlaylist = view.findViewById(R.id.btnSavePlaylist);
        btnSavePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePlaylist();
            }
        });

        return view;
    }

    private void savePlaylist() {
        // Get the playlist name and description from the EditTexts
        String playlistName = editTextPlaylistName.getText().toString().trim();
        String playlistDescription = editTextPlaylistDescription.getText().toString().trim();

        // Check if the fields are not empty
        if (playlistName.isEmpty() || playlistDescription.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter both name and description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save playlist data to SQLite database
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PLAYLIST_NAME, playlistName);
        values.put(DatabaseHelper.COLUMN_DESCRIPTION, playlistDescription);

        long newRowId = db.insert(DatabaseHelper.TABLE_PLAYLISTS, null, values);

        if (newRowId != -1) {
            Toast.makeText(getActivity(), "Playlist saved successfully", Toast.LENGTH_SHORT).show();
            // Clear the EditTexts after saving
            editTextPlaylistName.getText().clear();
            editTextPlaylistDescription.getText().clear();

            // Navigate back to the previous fragment or activity
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        } else {
            Toast.makeText(getActivity(), "Error saving playlist", Toast.LENGTH_SHORT).show();
        }

        // Close the database
        db.close();
    }
}
