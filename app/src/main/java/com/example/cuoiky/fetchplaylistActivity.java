package com.example.cuoiky;

import static com.example.cuoiky.PlaylistActivity.openDrawer;
import static com.example.cuoiky.PlaylistActivity.rediractActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class fetchplaylistActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    String id;
    // Update the adapter declaration
    private ArrayAdapter<MusicItem> adapter;
    ImageView menu;
    LinearLayout home, library, playlist, setting,statistic, share, about;
    private ListView myListView;
    ArrayList<MusicItem> musicItems;
    MusicItem currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    MusicItem selectedMusicItem;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        id = getIntent().getStringExtra("PLAYLIST_ID");
        // Initialize the DatabaseHelper
        databaseHelper = new DatabaseHelper(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);

        setting = findViewById(R.id.settings);

        playlist = findViewById(R.id.playlist);
        library = findViewById(R.id.library);
        statistic = findViewById(R.id.statistic);
        // Move this line after initializing myListView
        myListView = findViewById(R.id.mylistview);

        // Set up the ListView adapter
        adapter = new MusicItemAdapter(this, R.layout.item_song, new ArrayList<>());
        myListView.setAdapter(adapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected MusicItem
                selectedMusicItem = adapter.getItem(position);

                // Check if the selectedMusicItem is not null
                if (selectedMusicItem != null) {
                    String trackName = selectedMusicItem.getTrackName();
                    updateStatisticalTable(trackName);
                    launchMusicPlayerActivity(selectedMusicItem);
                    //new fetchplaylistActivity.FetchTrackUriTask(selectedMusicItem).execute(trackName);

                }
            }
        });

        // Retrieve the selected song from the intent
        MusicItem selectedMusicItem = (MusicItem) getIntent().getSerializableExtra("SELECTED_SONG");
        // Assuming you have the songsList and mediaPlayer variables defined in MainActivity
        MusicPlayerActivity musicPlayerActivity = new MusicPlayerActivity();
        musicPlayerActivity.setResourcesWithMusic(musicItems, mediaPlayer,selectedMusicItem);
        // Execute the AsyncTask to fetch data from the website
        new fetchplaylistActivity.DownloadMusicTask().execute();

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(fetchplaylistActivity.this, MainActivity.class);
                recreate();
            }
        });



        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(fetchplaylistActivity.this, PlaylistActivity.class);
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(fetchplaylistActivity.this, LibraryActivity.class);
            }
        });
        statistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the StatisticActivity when the "Statistic" TextView is pressed
                Intent intent = new Intent(fetchplaylistActivity.this, statisticActivity.class);
                startActivity(intent);
            }
        });

    }
    private void launchMusicPlayerActivity(MusicItem selectedMusicItem) {
        Intent intent = new Intent(fetchplaylistActivity.this, MusicPlayerActivity.class);
        // Pass the relevant information from selectedMusicItem
        intent.putExtra("LIST", musicItems);
        intent.putExtra("SELECTED_SONG", selectedMusicItem);
        startActivity(intent);
    }
    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Adjust the timeout as needed
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    private void updateStatisticalTable(String songName) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_STATISTICAL_SONG_NAME, songName);

        // Check if the song is already in the statistical table
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_STATISTICAL,
                null,
                DatabaseHelper.COLUMN_STATISTICAL_SONG_NAME + "=?",
                new String[]{songName},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            // Song is already in the table, increment the click_count
            int clickCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATISTICAL_CLICK_COUNT));
            values.put(DatabaseHelper.COLUMN_STATISTICAL_CLICK_COUNT, clickCount + 1);

            // Update the row
            db.update(DatabaseHelper.TABLE_STATISTICAL, values,
                    DatabaseHelper.COLUMN_STATISTICAL_SONG_NAME + "=?", new String[]{songName});
        } else {
            // Song is not in the table, insert a new row with click_count = 1
            values.put(DatabaseHelper.COLUMN_STATISTICAL_CLICK_COUNT, 1);
            db.insert(DatabaseHelper.TABLE_STATISTICAL, null, values);
        }

        cursor.close();
        db.close();
    }
    /*
    private class DownloadMusicTask extends AsyncTask<Void, Void, List<MusicItem>> {
        private final OkHttpClient client = createOkHttpClient();
        @Override
        protected List<MusicItem> doInBackground(Void... voids) {
            List<MusicItem> musicItems = new ArrayList<>();
            try {
                // Update the API endpoint and headers
                Request request = new Request.Builder()
                        .url("https://spotify81.p.rapidapi.com/playlist_tracks?id="+id+"&offset=0&limit=20")
                        .get()
                        .addHeader("X-RapidAPI-Key", "1f30213d71msh94558359c58ef87p15228ajsna837baccf6f7")
                        .addHeader("X-RapidAPI-Host", "spotify81.p.rapidapi.com")
                        .build();

                Response response = client.newCall(request).execute();
                String result = response.body().string();

                // Process the JSON response and extract the "name" value
                JSONObject jsonResponse = new JSONObject(result);
                JSONArray itemsArray = jsonResponse.getJSONArray("items");

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    JSONObject track = item.getJSONObject("track");
                    String trackName = track.getString("name");
                    String id = track.getString("id");

                    JSONObject album = track.getJSONObject("album");

                    // Get the images array from the album object
                    JSONArray imagesArray = album.getJSONArray("images");
                    JSONObject imageObject = imagesArray.getJSONObject(0);
                    String displayImageUri = imageObject.getString("url");
                    // Extract other relevant information as needed

                    String trackUri = "";         // You need to modify this based on your response structure
                    String duration = "";
                    // Create a MusicItem object to store track information
                    MusicItem musicItem = new MusicItem(trackName, displayImageUri, trackUri, duration,id);
                    musicItems.add(musicItem);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return musicItems;
        }

        @Override
        protected void onPostExecute(List<MusicItem> musicItems) {
            // Update the UI with the track names and display image URIs
            adapter.clear();
            adapter.addAll(musicItems);
            adapter.notifyDataSetChanged();
        }

        private String getTrackUriFromSoundCloud(OkHttpClient client, String trackName) throws IOException, JSONException {
            // Remove the local instantiation of OkHttpClient
            Request request = new Request.Builder()
                    .url("https://spotify81.p.rapidapi.com/download_track?q=" + URLEncoder.encode(trackName, "UTF-8") + "&onlyLinks=1")
                    .get()
                    .addHeader("X-RapidAPI-Key", "1f30213d71msh94558359c58ef87p15228ajsna837baccf6f7")
                    .addHeader("X-RapidAPI-Host", "spotify81.p.rapidapi.com")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();

                // Process the JSON response from SoundCloud and extract the trackUri
                JSONArray searchResults = new JSONArray(result);

                if (searchResults.length() > 0) {
                    JSONObject firstResult = searchResults.getJSONObject(0);
                    return firstResult.getString("url");
                } else {
                    return ""; // Handle the case where the trackUri is not found
                }
            } catch (SocketTimeoutException e) {
                // Handle timeout exception, you can retry the request or show an error message
                e.printStackTrace();
                return ""; // or throw an exception or handle as needed
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return ""; // Handle other exceptions
            }
        }
    }
    */
    private class DownloadMusicTask extends AsyncTask<Void, Void, List<MusicItem>> {
        private final OkHttpClient client = createOkHttpClient();

        @Override
        protected List<MusicItem> doInBackground(Void... voids) {
            List<MusicItem> musicItems = new ArrayList<>();
            try {
                // Update the API endpoint and headers
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/v1/playlist/" + id)
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                String result = response.body().string();

                // Process the JSON response and extract the "songs" array
                JSONObject jsonResponse = new JSONObject(result);
                JSONArray songsArray = jsonResponse.getJSONArray("songs");

                // Loop through the songs array and extract relevant information
                for (int i = 0; i < songsArray.length(); i++) {
                    JSONObject songObject = songsArray.getJSONObject(i);
                    String trackName = songObject.getString("trackName");
                    String displayImageUri = songObject.getString("displayImageUri");
                    String trackUri = songObject.getString("trackUri");
                    String duration = songObject.getString("duration");
                    String id = songObject.getString("apiId");

                    // Create a MusicItem object to store track information
                    MusicItem musicItem = new MusicItem(trackName, displayImageUri, trackUri, duration, id);
                    musicItems.add(musicItem);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return musicItems;
        }

        @Override
        protected void onPostExecute(List<MusicItem> musicItems) {
            // Update the UI with the track names and display image URIs
            adapter.clear();
            adapter.addAll(musicItems);
            adapter.notifyDataSetChanged();
        }
    }

    private class FetchTrackUriTask extends AsyncTask<String, Void, Pair<String, String>> {

        private MusicItem musicItemToUpdate;

        public FetchTrackUriTask(MusicItem musicItemToUpdate) {
            this.musicItemToUpdate = musicItemToUpdate;
        }

        @Override
        protected Pair<String, String> doInBackground(String... params) {
            String trackName = params[0];
            return getTrackInfoFromApi(trackName);
        }

        @Override
        protected void onPostExecute(Pair<String, String> trackInfo) {
            String trackUri = trackInfo.first;
            String duration = trackInfo.second;

            musicItemToUpdate.setTrackURI(trackUri);
            musicItemToUpdate.setDuration(duration);

            // Show TrackURI and duration in a toast
            Toast.makeText(fetchplaylistActivity.this, "TrackURI: " + trackUri + "\nDuration: " + duration, Toast.LENGTH_SHORT).show();

            // Play the song with the obtained trackUri
            // playSong(trackUri);
            launchMusicPlayerActivity(selectedMusicItem);
        }
    }

    private Pair<String, String> getTrackInfoFromApi(String trackName) {
        try {
            OkHttpClient client = createOkHttpClient();

            // API request to get the trackUri and duration
            Request request = new Request.Builder()
                    .url("https://spotify81.p.rapidapi.com/download_track?q=" + URLEncoder.encode(trackName, "UTF-8") + "&onlyLinks=1")
                    .get()
                    .addHeader("X-RapidAPI-Key", "1f30213d71msh94558359c58ef87p15228ajsna837baccf6f7")
                    .addHeader("X-RapidAPI-Host", "spotify81.p.rapidapi.com")
                    .build();

            Response response = client.newCall(request).execute();
            String result = response.body().string();

            // Process the JSON response and extract the trackUri and duration
            JSONArray searchResults = new JSONArray(result);

            if (searchResults.length() > 0) {
                JSONObject firstResult = searchResults.getJSONObject(0);
                String trackUri = firstResult.getString("url");
                String duration = firstResult.getString("duration"); // Assuming the API provides duration

                return new Pair<>(trackUri, duration);
            } else {
                return new Pair<>("", ""); // Handle the case where the trackUri is not found
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return new Pair<>("", ""); // Handle timeout exception, you can retry the request or show an error message
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new Pair<>("", ""); // Handle other exceptions
        }
    }
}
