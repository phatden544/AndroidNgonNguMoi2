package com.example.cuoiky;

import static com.example.cuoiky.PlaylistActivity.rediractActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.chibde.visualizer.CircleBarVisualizer;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MusicPlayerActivity extends AppCompatActivity {
    TextView titleTv, currentTimeTv, totalTimeTv, lyricsTextView;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon, clockImageView, back, download, like_button, lyricbtn;
    ArrayList<MusicItem> songsList;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    int x = 0;
    MusicItem currentSong;
    private CountDownTimer countDownTimer;
    private DatabaseHelper dbHelper;
    ScrollView lyricpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        lyricsTextView = findViewById(R.id.lyricsTextView);
        titleTv = findViewById(R.id.song_title);
        currentTimeTv = findViewById(R.id.current_time);
        totalTimeTv = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pause_play);
        musicIcon = findViewById(R.id.music_icon_big);
        clockImageView = findViewById(R.id.clock_image);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        back = findViewById(R.id.back);
        download = findViewById(R.id.download);
        like_button = findViewById(R.id.like_button);
        lyricbtn = findViewById(R.id.lyricbtn);
        lyricpanel = findViewById(R.id.lyricpanel);

        titleTv.setSelected(true);
        songsList = (ArrayList<MusicItem>) getIntent().getSerializableExtra("LIST");
        currentSong = (MusicItem) getIntent().getSerializableExtra("SELECTED_SONG");
        Toast.makeText(MusicPlayerActivity.this, "Playing: " + currentSong.getTrackName(), Toast.LENGTH_SHORT).show();
        playMusic(currentSong);

        titleTv.setText(currentSong.getTrackName());
        String trackId = currentSong.getId();
        new FetchLyricsTask().execute(trackId);

        String durationString = getIntent().getStringExtra("duration");
        double durationDouble = TextUtils.isEmpty(durationString) ? 0.0 : Double.parseDouble(durationString);
        int totalSeconds = (int) durationDouble;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String formattedDuration = String.format("%02d:%02d", minutes, seconds);
        totalTimeTv.setText(formattedDuration);

        if (currentSong != null) {
            setResourcesWithMusic(songsList, mediaPlayer, currentSong);
        } else {
            Log.e("MusicPlayerActivity", "selectedMusicItem is null");
        }

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadCurrentSong();
            }
        });
        pausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pausePlay();
            }
        });
        clockImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimerDialog();
            }
        });
        lyricbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLyricPanelVisibility();
            }
        });
        dbHelper = new DatabaseHelper(this);

        like_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorite();

            }
        });
        if (currentSong != null) {
            updateLikeButton(dbHelper.isSongInFavorites(currentSong.getTrackName()));
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(MusicPlayerActivity.this, MainActivity.class);
            }
        });

        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTv.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));

                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.baseline_pause_circle_24);
                        musicIcon.setRotation(x++);
                    } else {
                        pausePlay.setImageResource(R.drawable.baseline_play_circle_24);
                        musicIcon.setRotation(0);
                    }
                }
                new Handler().postDelayed(this, 100);
            }
        }, 100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    private void updateLikeButton(boolean isFavorite) {
        if (isFavorite) {
            like_button.setImageResource(R.drawable.baseline_favorite_24); // Change to your favorite icon resource
        } else {
            like_button.setImageResource(R.drawable.baseline_favorite_border_24); // Change to your default icon resource
        }
    }
    private void toggleLyricPanelVisibility() {
        if (lyricpanel.getVisibility() == View.VISIBLE) {
            lyricpanel.setVisibility(View.GONE);
        } else {
            lyricpanel.setVisibility(View.VISIBLE);
        }
    }
    private void downloadCurrentSong() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(MusicPlayerActivity.this, "External storage is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(currentSong.getTrackURI());

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setTitle("Downloading " + currentSong.getTrackName())
                .setDescription("Downloading music file")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, currentSong.getTrackName() + ".mp3");

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(MusicPlayerActivity.this, "Downloading...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MusicPlayerActivity.this, "DownloadManager not available", Toast.LENGTH_SHORT).show();
        }
    }

    void setResourcesWithMusic(ArrayList<MusicItem> songsList, MediaPlayer mediaPlayer, MusicItem selectedMusicItem) {
        if (songsList == null || songsList.isEmpty()) {
            Log.e("MusicPlayerActivity", "songsList is null or empty");
            return;
        }

        Log.d("MusicPlayerActivity", "songsList size: " + songsList.size());

        if (MyMediaPlayer.currentIndex >= 0 && MyMediaPlayer.currentIndex < songsList.size()) {
            currentSong = (selectedMusicItem != null) ? selectedMusicItem : songsList.get(MyMediaPlayer.currentIndex);

            titleTv.setText(currentSong.getTrackName());
        } else {
            Log.e("MusicPlayerActivity", "Invalid currentIndex: " + MyMediaPlayer.currentIndex);
        }
    }

    private void playMusic(MusicItem song) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(song.getTrackURI());
            mediaPlayer.prepare();
            mediaPlayer.start();

            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());

            // Load and display the image using Picasso
            Picasso.get()
                    .load(song.getDisplayImageUri())
                    .placeholder(getResources().getDrawable(R.drawable.circular_background)) // You can set a placeholder image if needed
                    .error(R.drawable.circular_background) // You can set an error image if needed
                    .transform(new CircleTransform())
                    .fit() // Ensures image fit into ImageView
                    .centerCrop() // Centers and crops the image
                    .into(musicIcon);
            int audioSessionId = mediaPlayer.getAudioSessionId();
            if (audioSessionId != -1){
                CircleBarVisualizer circleBarVisualizer = findViewById(R.id.visualizer);
                circleBarVisualizer.setColor(ContextCompat.getColor(this, R.color.semiTransparentGray));
                circleBarVisualizer.setPlayer(audioSessionId);
            }
            else{

            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(this, 100);
                }
            }, 100);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pausePlay() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();
    }

    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    private void addToFavorite() {
        if (currentSong != null) {
            String id = currentSong.getId();
            String songName = currentSong.getTrackName();
            String trackURI = currentSong.getTrackURI();
            String duration = currentSong.getDuration();

            boolean isFavorite = dbHelper.isSongInFavorites(songName);

            if (isFavorite) {
                int rowsDeleted = dbHelper.deleteFavoriteSong(songName);

                if (rowsDeleted > 0) {
                    Toast.makeText(MusicPlayerActivity.this, "Song removed from favorites", Toast.LENGTH_SHORT).show();
                    updateLikeButton(false);
                } else {
                    Toast.makeText(MusicPlayerActivity.this, "Failed to remove song from favorites", Toast.LENGTH_SHORT).show();
                }
            } else {
                long rowId = dbHelper.insertFavoriteSong(songName, trackURI, duration, id);

                if (rowId != -1) {
                    Toast.makeText(MusicPlayerActivity.this, "Song added to favorites", Toast.LENGTH_SHORT).show();
                    updateLikeButton(true);
                } else {
                    Toast.makeText(MusicPlayerActivity.this, "Failed to add song to favorites", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(MusicPlayerActivity.this, "No song selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void showTimerDialog() {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                stopMusicAtSelectedTime(hourOfDay, minute);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                timeSetListener,
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                Calendar.getInstance().get(Calendar.MINUTE),
                false
        );

        timePickerDialog.show();
    }

    private void stopMusicAtSelectedTime(int selectedHour, int selectedMinute) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
        calendar.set(Calendar.MINUTE, selectedMinute);
        calendar.set(Calendar.SECOND, 0);

        long selectedTimeMillis = calendar.getTimeInMillis();
        long currentTimeMillis = System.currentTimeMillis();
        long remainingTimeMillis = selectedTimeMillis - currentTimeMillis;

        if (remainingTimeMillis <= 0) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        } else {
            countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    }
                }
            };

            countDownTimer.start();
        }
    }

    private class FetchLyricsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String trackId = params[0];
            String lyrics = null;

            try {
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://spotify-scraper.p.rapidapi.com/v1/track/lyrics?trackId="+trackId)
                        .get()
                        .addHeader("X-RapidAPI-Key", "d5a456d89fmsh394173a51630e7dp101704jsn8c658269bbdf")
                        .addHeader("X-RapidAPI-Host", "spotify-scraper.p.rapidapi.com")
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    lyrics = response.body().string();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return lyrics;
        }

        @Override
        protected void onPostExecute(String lyrics) {
            if (lyrics != null) {
                // Remove everything inside "[" and "]"
                lyrics = lyrics.replaceAll("\\[.*?\\]", "");

                lyricsTextView.setText(lyrics);
                // Highlight the lyrics based on timing

            } else {
                lyricsTextView.setText("Lyrics not available");
            }

        }

    }
}
