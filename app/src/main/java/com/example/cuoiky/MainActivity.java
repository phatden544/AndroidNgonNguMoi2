package com.example.cuoiky;

import static com.example.cuoiky.PlaylistActivity.openDrawer;
import static com.example.cuoiky.PlaylistActivity.rediractActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    // Update the adapter declaration
    private ArrayAdapter<MusicItem> adapter;
    RelativeLayout thienha;
    ImageView menu;
    LinearLayout home, library, playlist, setting,statistic, share, about, logout ,indie,viral,tramhoa,vpop;
    Button btnAddToPlaylist;
    private ListView myListView;
    private DatabaseHelper databaseHelper;
    ArrayList<MusicItem> musicItems;
    MusicItem currentSong;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    int x = 0;
    MusicItem selectedMusicItem;
    private CountDownTimer countDownTimer;
    LocalDate today = LocalDate.now();
    LocalDate closestPastThursday = today.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY));
    String formattedDate = closestPastThursday.toString();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realmain_activity);

        if (isUserAuthenticated()) {

            Toast.makeText(this, "User is authenticated", Toast.LENGTH_SHORT).show();
        } else {

            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        databaseHelper = new DatabaseHelper(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);
        setting = findViewById(R.id.settings);
        logout = findViewById(R.id.logout);
        playlist = findViewById(R.id.playlist);
        library = findViewById(R.id.library);
        statistic = findViewById(R.id.statistic);
        myListView = findViewById(R.id.mylistview);
        thienha = findViewById(R.id.thienha);
        indie = findViewById(R.id.indie);
        tramhoa = findViewById(R.id.tramhoa);
        viral = findViewById(R.id.viral);
        vpop = findViewById(R.id.vpop);
        adapter = new MusicItemAdapter(this, R.layout.item_song, new ArrayList<>());
        myListView.setAdapter(adapter);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                selectedMusicItem = adapter.getItem(position);

                if (selectedMusicItem != null) {
                    String trackName = selectedMusicItem.getTrackName();
                    updateStatisticalTable(trackName);
                    launchMusicPlayerActivity(selectedMusicItem);
                    //new FetchTrackUriTask(selectedMusicItem).execute(trackName);
                    //new AddMusicItemToAPITask(selectedMusicItem).execute();
                }
            }
        });
        if (checkPermission()== false){
            requestPermission();
            return;
        }

        MusicItem selectedMusicItem = (MusicItem) getIntent().getSerializableExtra("SELECTED_SONG");

        MusicPlayerActivity musicPlayerActivity = new MusicPlayerActivity();
        musicPlayerActivity.setResourcesWithMusic(musicItems, mediaPlayer,selectedMusicItem);

        new DownloadMusicTask().execute();

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(MainActivity.this, MainActivity.class);
                recreate();
            }
        });



        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(MainActivity.this, PlaylistActivity.class);
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(MainActivity.this, LibraryActivity.class);
            }
        });
        statistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, statisticActivity.class);
                startActivity(intent);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Logout", Toast.LENGTH_SHORT).show();
            }
        });
        thienha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFetchPlaylistActivity("66200f06994ce0d261118037");
            }
        });

        indie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFetchPlaylistActivity("66200f37994ce0d261118039");
            }
        });

        tramhoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFetchPlaylistActivity("66200f63994ce0d26111803b");
            }
        });

        viral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFetchPlaylistActivity("66201024994ce0d26111803f");
            }
        });

        vpop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFetchPlaylistActivity("66200fb1994ce0d26111803d");
            }
        });


    }
    private void openFetchPlaylistActivity(String playlistId) {
        Intent intent = new Intent(MainActivity.this, fetchplaylistActivity.class);
        intent.putExtra("PLAYLIST_ID", playlistId);
        startActivity(intent);
    }
    boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            return false;
        }
    }
    void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.RECORD_AUDIO)){
            Toast.makeText(MainActivity.this, "RECORD TO REQUEST", Toast.LENGTH_SHORT).show();
        }else
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},123);
    }

    private void launchMusicPlayerActivity(MusicItem selectedMusicItem) {
        Intent intent = new Intent(MainActivity.this, MusicPlayerActivity.class);
        // Pass the relevant information from selectedMusicItem
        intent.putExtra("LIST", musicItems);
        intent.putExtra("SELECTED_SONG", selectedMusicItem);
        startActivity(intent);
    }
    private void playSong(String trackUri) {

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {

            mediaPlayer.setDataSource(trackUri);


            mediaPlayer.prepare();


            mediaPlayer.start();


            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    mediaPlayer.release();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(MainActivity.this, "Error playing the song", Toast.LENGTH_SHORT).show();
        }
    }
    private void showAddToPlaylistDialog(String selectedSong) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a Playlist")
                .setItems(getPlaylistNames(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedPlaylist = getPlaylistNames()[which];

                        updatePlaylist(selectedPlaylist, selectedSong);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private boolean isUserAuthenticated() {

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        return preferences.getBoolean("isUserAuthenticated", false);
    }
    private String getSelectedSong() {

        int selectedItemPosition = myListView.getCheckedItemPosition();

        if (selectedItemPosition != ListView.INVALID_POSITION) {
            MusicItem selectedMusicItem = adapter.getItem(selectedItemPosition);
            if (selectedMusicItem != null) {
                return selectedMusicItem.getTrackName();
            }
        }


        return null;
    }
    private String[] getPlaylistNames() {

        ArrayList<String> playlists = databaseHelper.getAllPlaylists();
        return playlists.toArray(new String[0]);
    }

    private void updatePlaylist(String playlistName, String songName) {
        // Update the database with the selected song for the playlist
        long playlistId = databaseHelper.getPlaylistId(playlistName);
        if (playlistId != -1) {
            long rowId = databaseHelper.insertSongIntoPlaylist(playlistId, songName);
            if (rowId != -1) {
                Toast.makeText(this, "Song added to playlist: " + playlistName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add song to playlist", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Playlist not found: " + playlistName, Toast.LENGTH_SHORT).show();
        }
    }
    private OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Adjust the timeout as needed
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }
    private OkHttpClient createOkHttpClient2() {
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

            int clickCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATISTICAL_CLICK_COUNT));
            values.put(DatabaseHelper.COLUMN_STATISTICAL_CLICK_COUNT, clickCount + 1);


            db.update(DatabaseHelper.TABLE_STATISTICAL, values,
                    DatabaseHelper.COLUMN_STATISTICAL_SONG_NAME + "=?", new String[]{songName});
        } else {

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

                Request request = new Request.Builder()
                        .url("https://spotify81.p.rapidapi.com/playlist_tracks?id=37i9dQZF1DX34s4fg4Zx3Z&offset=0&limit=70")
                        .get()
                        .addHeader("X-RapidAPI-Key", "1f30213d71msh94558359c58ef87p15228ajsna837baccf6f7")
                        .addHeader("X-RapidAPI-Host", "spotify81.p.rapidapi.com")
                        .build();

                Response response = client.newCall(request).execute();
                String result = response.body().string();


                JSONObject jsonResponse = new JSONObject(result);
                JSONArray itemsArray = jsonResponse.getJSONArray("items");

                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject item = itemsArray.getJSONObject(i);
                    JSONObject track = item.getJSONObject("track");
                    String trackName = track.getString("name");
                    String id = track.getString("id");

                    JSONObject album = track.getJSONObject("album");


                    JSONArray imagesArray = album.getJSONArray("images");
                    JSONObject imageObject = imagesArray.getJSONObject(0);
                    String displayImageUri = imageObject.getString("url");


                    String trackUri = "";
                    String duration = "";

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

            adapter.clear();
            adapter.addAll(musicItems);
            adapter.notifyDataSetChanged();
        }


    }
    */
    private class DownloadMusicTask extends AsyncTask<Void, Void, List<MusicItem>> {
        private final OkHttpClient client = createOkHttpClient();

        @Override
        protected List<MusicItem> doInBackground(Void... voids) {
            List<MusicItem> musicItems = new ArrayList<>();
            try {
                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/v1/song/")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }

                String responseBody = response.body().string();
                JSONArray jsonArray = new JSONArray(responseBody);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String trackName = jsonObject.getString("trackName");
                    String displayImageUri = jsonObject.getString("displayImageUri");
                    String trackUri = jsonObject.getString("trackUri");
                    String duration = jsonObject.getString("duration");
                    String id = jsonObject.getString("apiId");

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



            JSONObject json = new JSONObject();
            try {
                json.put("trackName", selectedMusicItem.getTrackName());
                json.put("displayImageUri", selectedMusicItem.getDisplayImageUri());
                json.put("trackUri", selectedMusicItem.getTrackURI());
                json.put("duration", selectedMusicItem.getDuration());
                json.put("apiId", selectedMusicItem.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("SelectedMusicItemJSON", json.toString());
            Toast.makeText(MainActivity.this, "TrackURI: " + trackUri + "\nDuration: " + duration, Toast.LENGTH_SHORT).show();
            addMusicItemToAPI(selectedMusicItem);
            //launchMusicPlayerActivity(selectedMusicItem);
        }

    }
    private void addMusicItemToAPI(MusicItem musicItem) {
        new AddMusicItemTask().execute(musicItem);
    }
    private class AddMusicItemTask extends AsyncTask<MusicItem, Void, Void> {
        @Override
        protected Void doInBackground(MusicItem... musicItems) {
            if (musicItems.length == 0) {
                return null;
            }

            OkHttpClient client = createOkHttpClient();

            MusicItem musicItem = musicItems[0];
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("trackName", musicItem.getTrackName());
                requestBody.put("displayImageUri", musicItem.getDisplayImageUri());
                requestBody.put("trackUri", musicItem.getTrackURI());
                requestBody.put("duration", musicItem.getDuration());
                requestBody.put("apiId", musicItem.getId());
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestBody.toString());

                Request request = new Request.Builder()
                        .url("http://10.0.2.2:3000/v1/song/")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }






    private Pair<String, String> getTrackInfoFromApi(String trackName) {
        try {
            OkHttpClient client = createOkHttpClient();


            Request request = new Request.Builder()
                    .url("https://spotify81.p.rapidapi.com/download_track?q=" + URLEncoder.encode(trackName, "UTF-8") + "&onlyLinks=1")
                    .get()
                    .addHeader("X-RapidAPI-Key", "1f30213d71msh94558359c58ef87p15228ajsna837baccf6f7")
                    .addHeader("X-RapidAPI-Host", "spotify81.p.rapidapi.com")
                    .build();

            Response response = client.newCall(request).execute();
            String result = response.body().string();


            JSONArray searchResults = new JSONArray(result);

            if (searchResults.length() > 0) {
                JSONObject firstResult = searchResults.getJSONObject(0);
                String trackUri = firstResult.getString("url");
                String duration = firstResult.getString("duration");

                return new Pair<>(trackUri, duration);
            } else {
                return new Pair<>("", "");
            }
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return new Pair<>("", "");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new Pair<>("", "");
        }
    }
}