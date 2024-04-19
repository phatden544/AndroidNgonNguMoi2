package com.example.cuoiky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "app_database";
    private static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_USERNAME + " TEXT," +
                    COLUMN_EMAIL + " TEXT," +
                    COLUMN_PASSWORD + " TEXT)";

    // Playlists table
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String COLUMN_PLAYLIST_ID = "_id";
    public static final String COLUMN_PLAYLIST_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";

    private static final String CREATE_TABLE_PLAYLISTS = "CREATE TABLE " +
            TABLE_PLAYLISTS + "(" +
            COLUMN_PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PLAYLIST_NAME + " TEXT, " +
            COLUMN_DESCRIPTION + " TEXT);";

    // Playlist Songs table
    public static final String TABLE_PLAYLIST_SONGS = "playlist_songs";
    public static final String COLUMN_PLAYLIST_ID_FK = "playlist_id";
    public static final String COLUMN_SONG_NAME = "song_name";

    private static final String CREATE_TABLE_PLAYLIST_SONGS = "CREATE TABLE " +
            TABLE_PLAYLIST_SONGS + "(" +
            COLUMN_PLAYLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PLAYLIST_ID_FK + " INTEGER, " +
            COLUMN_SONG_NAME + " TEXT, " +
            "FOREIGN KEY (" + COLUMN_PLAYLIST_ID_FK + ") REFERENCES " +
            TABLE_PLAYLISTS + "(" + COLUMN_PLAYLIST_ID + "));";
    // Favorite Songs table
    // Favorite Songs table
    public static final String TABLE_FAVORITE_SONGS = "favorite_songs";
    public static final String COLUMN_FAVORITE_ID = "_id";
    public static final String COLUMN_FAVORITE_NAME = "song_name";
    public static final String COLUMN_FAVORITE_TRACK_URI = "track_uri";
    public static final String COLUMN_FAVORITE_DURATION = "duration";

    private static final String CREATE_TABLE_FAVORITE_SONGS = "CREATE TABLE " +
            TABLE_FAVORITE_SONGS + "(" +
            COLUMN_FAVORITE_ID + " TEXT, " +
            COLUMN_FAVORITE_NAME + " TEXT, " +
            COLUMN_FAVORITE_TRACK_URI + " TEXT, " +
            COLUMN_FAVORITE_DURATION + " TEXT);";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public static final String COLUMN_STATISTICAL_CLICK_COUNT = "click_count";

    public static final String TABLE_STATISTICAL = "statistic";
    public static final String COLUMN_STATISTICAL_ID = "_id";
    public static final String COLUMN_STATISTICAL_SONG_NAME = "name";
    private static final String CREATE_TABLE_STATISTICAL = "CREATE TABLE " +
            TABLE_STATISTICAL + "(" +
            COLUMN_STATISTICAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_STATISTICAL_SONG_NAME + " TEXT, " +
            COLUMN_STATISTICAL_CLICK_COUNT + " INTEGER DEFAULT 0);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "onCreate: Creating tables...");
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PLAYLISTS);
        db.execSQL(CREATE_TABLE_FAVORITE_SONGS);
        db.execSQL(CREATE_TABLE_PLAYLIST_SONGS);
        db.execSQL(CREATE_TABLE_STATISTICAL);

        Log.d("DatabaseHelper", "onCreate: Tables created successfully!");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle upgrades if needed

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATISTICAL);
        onCreate(db);

    }
    public ArrayList<HashMap<String, String>> getStatisticalData() {
        ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STATISTICAL, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                HashMap<String, String> data = new HashMap<>();

                // Check if the column exists in the cursor
                int songNameIndex = cursor.getColumnIndex(COLUMN_STATISTICAL_SONG_NAME);
                int clickCountIndex = cursor.getColumnIndex(COLUMN_STATISTICAL_CLICK_COUNT);

                if (songNameIndex != -1 && clickCountIndex != -1) {
                    data.put("songName", cursor.getString(songNameIndex));
                    data.put("clickCount", String.valueOf(cursor.getInt(clickCountIndex)));
                    dataList.add(data);
                } else {
                    // Handle the case where the specified column is not found
                    // You can log a message or take appropriate action
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        db.close();
        return dataList;
    }
    public ArrayList<String> getAllPlaylists() {
        ArrayList<String> playlists = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PLAYLIST_NAME + " FROM " + TABLE_PLAYLISTS, null);

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_PLAYLIST_NAME);
            do {
                if (columnIndex != -1) {
                    playlists.add(cursor.getString(columnIndex));
                } else {
                    // Handle the case where the specified column is not found
                    // You can log a message or take appropriate action
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return playlists;
    }
    public long insertFavoriteSong(String songName, String trackURI, String duration, String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE_NAME, songName);
        values.put(COLUMN_FAVORITE_TRACK_URI, trackURI);
        values.put(COLUMN_FAVORITE_DURATION, duration);
        values.put(COLUMN_FAVORITE_ID, duration);

        long rowId = db.insert(TABLE_FAVORITE_SONGS, null, values);

        db.close();

        return rowId;
    }
    public boolean isSongInFavorites(String songName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITE_SONGS +
                " WHERE " + COLUMN_FAVORITE_NAME + "=?", new String[]{songName});

        boolean isSongInFavorites = cursor.moveToFirst();

        cursor.close();
        db.close();

        return isSongInFavorites;
    }
    public ArrayList<MusicItem> getAllFavoriteSongs() {
        ArrayList<MusicItem> favoriteSongs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the columns you want to retrieve
        String[] columns = {COLUMN_FAVORITE_ID, COLUMN_FAVORITE_NAME, COLUMN_FAVORITE_TRACK_URI, COLUMN_FAVORITE_DURATION};

        Cursor cursor = db.query(
                TABLE_FAVORITE_SONGS,     // The table to query
                columns,                  // The columns to return
                null,                     // The columns for the WHERE clause
                null,                     // The values for the WHERE clause
                null,                     // don't group the rows
                null,                     // don't filter by row groups
                null                      // don't sort the order
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(COLUMN_FAVORITE_ID);
                int nameIndex = cursor.getColumnIndex(COLUMN_FAVORITE_NAME);
                int trackUriIndex = cursor.getColumnIndex(COLUMN_FAVORITE_TRACK_URI);
                int durationIndex = cursor.getColumnIndex(COLUMN_FAVORITE_DURATION);

                if (idIndex >= 0 && nameIndex >= 0 && trackUriIndex >= 0 && durationIndex >= 0) {
                    String id = cursor.getString(idIndex);
                    String songName = cursor.getString(nameIndex);
                    String trackUri = cursor.getString(trackUriIndex);
                    String duration = cursor.getString(durationIndex);
                    String displayImageUri = "https://www.pngall.com/wp-content/uploads/13/Apple-Music-Logo-PNG.png";
                    // Create a MusicItem object and add it to the list
                    MusicItem musicItem = new MusicItem(songName,displayImageUri, trackUri, duration, id);
                    favoriteSongs.add(musicItem);
                }
            }
            cursor.close();
        }

        db.close();
        return favoriteSongs;
    }

    public int deleteFavoriteSong(String songName) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_FAVORITE_SONGS, COLUMN_FAVORITE_NAME + "=?", new String[]{songName});
    }
    public long getPlaylistId(String playlistName) {
        SQLiteDatabase db = this.getReadableDatabase();
        long playlistId = -1;

        Cursor cursor = db.rawQuery("SELECT " + COLUMN_PLAYLIST_ID + " FROM " + TABLE_PLAYLISTS +
                " WHERE " + COLUMN_PLAYLIST_NAME + "=?", new String[]{playlistName});

        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(COLUMN_PLAYLIST_ID);
            if (columnIndex != -1) {
                playlistId = cursor.getLong(columnIndex);
            } else {
                // Handle the case where the specified column is not found
                // You can log a message or take appropriate action
            }
        }

        cursor.close();
        db.close();

        return playlistId;
    }

    public long insertSongIntoPlaylist(long playlistId, String songName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAYLIST_ID_FK, playlistId);
        values.put(COLUMN_SONG_NAME, songName);

        long rowId = db.insert(TABLE_PLAYLIST_SONGS, null, values);

        db.close();

        return rowId;
    }

    public boolean isUserAuthenticated(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {COLUMN_USERNAME, COLUMN_PASSWORD};
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                TABLE_USERS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean isAuthenticated = cursor.moveToFirst();

        cursor.close();
        db.close();

        return isAuthenticated;
    }

    public long insertUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long userId = db.insert(TABLE_USERS, null, values);

        db.close();

        return userId;
    }
}
