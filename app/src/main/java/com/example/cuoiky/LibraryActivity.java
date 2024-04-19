package com.example.cuoiky;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import java.io.File;
import java.util.ArrayList;

public class LibraryActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noMusicTextView;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, library, playlist, setting, share, about, logout,statistic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        statistic = findViewById(R.id.statistic);
        recyclerView = findViewById(R.id.recycler_view);
        noMusicTextView = findViewById(R.id.no_song_text);
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);



        playlist = findViewById(R.id.playlist);
        library = findViewById(R.id.library);

        if (checkPermission()== false){
            requestPermission();
            return;
        }

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC +" !=0";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,selection,null,null);

        while(cursor.moveToNext()){
            AudioModel songData = new AudioModel(cursor.getString(1),cursor.getString(0),cursor.getString(2));
            if(new File(songData.getPath()).exists())
                songsList.add(songData);
        }

        if(songsList.size()==0){
            noMusicTextView.setVisibility(View.VISIBLE);
        }else{
            //recyclerview
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new MusicListAdapter(songsList,getApplicationContext()));
        }

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }});

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(LibraryActivity.this, MainActivity.class);
                recreate();
            }
        });
        statistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open the StatisticActivity when the "Statistic" TextView is pressed
                Intent intent = new Intent(LibraryActivity.this, statisticActivity.class);
                startActivity(intent);
            }
        });


        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(LibraryActivity.this, PlaylistActivity.class);
                recreate();
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });


    }

    boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(LibraryActivity.this, Manifest.permission.READ_MEDIA_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else {
            return false;
        }
    }

    void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(LibraryActivity.this, Manifest.permission.READ_MEDIA_AUDIO)){
            Toast.makeText(LibraryActivity.this, "READ PERMISSION TO REQUEST", Toast.LENGTH_SHORT).show();
        }else
            ActivityCompat.requestPermissions(LibraryActivity.this,new String[]{Manifest.permission.READ_MEDIA_AUDIO},123);
    }
    public static void openDrawer(DrawerLayout drawerLayout){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public static void closeDrawer(DrawerLayout drawerLayout){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public static void rediractActivity(Activity activity, Class secondActivity){
        Intent intent = new Intent(activity, secondActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }


    @Override
    protected void onPause(){
        super.onPause();
        closeDrawer(drawerLayout);
    }
}