package com.example.cuoiky;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;


import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PlaylistActivity extends AppCompatActivity {
    private ArrayAdapter<MusicItem> adapter;
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, library, playlist, setting, share, about, logout,statistic;
    ListView mylistview2;
    String[] items;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    MusicItem selectedMusicItem;
    ArrayList<MusicItem> musicItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playlist);
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Fetch favorite songs from the database
        ArrayList<MusicItem> favoriteSongs = dbHelper.getAllFavoriteSongs();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        statistic = findViewById(R.id.statistic);
        setting = findViewById(R.id.settings);
        logout = findViewById(R.id.logout);
        playlist = findViewById(R.id.playlist);
        library = findViewById(R.id.library);
        mylistview2 = findViewById(R.id.mylistview2);
        // Create an ArrayAdapter to bind data to the ListView
        adapter = new MusicItemAdapter(this, R.layout.item_song, favoriteSongs);
        // Set the adapter to the ListView
        mylistview2.setAdapter(adapter);
        mylistview2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected MusicItem
                selectedMusicItem = adapter.getItem(position);

                // Check if the selectedMusicItem is not null
                if (selectedMusicItem != null) {
                    String trackName = selectedMusicItem.getTrackName();
                    new PlaylistActivity.FetchTrackUriTask(selectedMusicItem).execute(trackName);

                }
            }
        });
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(PlaylistActivity.this, MainActivity.class);
                recreate();
            }
        });


        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(PlaylistActivity.this, LibraryActivity.class);
                recreate();
            }
        });
        statistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the StatisticActivity when the "Statistic" TextView is pressed
                Intent intent = new Intent(PlaylistActivity.this, statisticActivity.class);
                startActivity(intent);
            }
        });
        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PlaylistActivity.this, "Logout", Toast.LENGTH_SHORT).show();
            }
        });



    }
    private void launchMusicPlayerActivity(MusicItem selectedMusicItem) {
        Intent intent = new Intent(PlaylistActivity.this, MusicPlayerActivity.class);
        // Pass the relevant information from selectedMusicItem
        intent.putExtra("LIST", musicItems);
        intent.putExtra("SELECTED_SONG", selectedMusicItem);
        startActivity(intent);
    }

    public static void openDrawer(DrawerLayout drawerLayout) {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void rediractActivity(Activity activity, Class secondActivity) {
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
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
            Toast.makeText(PlaylistActivity.this, "TrackURI: " + trackUri + "\nDuration: " + duration, Toast.LENGTH_SHORT).show();

            // Play the song with the obtained trackUri
            // playSong(trackUri);
            launchMusicPlayerActivity(selectedMusicItem);
        }
    }
    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Adjust the timeout as needed
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
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
