package com.example.cuoiky;

import static com.example.cuoiky.PlaylistActivity.openDrawer;
import static com.example.cuoiky.PlaylistActivity.rediractActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class statisticActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    ImageView menu;
    LinearLayout home, library, playlist, setting,statistic, share, about, logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);
        drawerLayout = findViewById(R.id.drawerLayout);
        menu = findViewById(R.id.menu);
        home = findViewById(R.id.home);

        setting = findViewById(R.id.settings);
        logout = findViewById(R.id.logout);
        playlist = findViewById(R.id.playlist);
        library = findViewById(R.id.library);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDrawer(drawerLayout);
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(statisticActivity.this, MainActivity.class);
                recreate();
            }
        });



        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(statisticActivity.this, PlaylistActivity.class);
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rediractActivity(statisticActivity.this, LibraryActivity.class);
            }
        });
        // Call a method to populate the ListView with statistical data
        populateListView();
    }

    private void populateListView() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        ArrayList<HashMap<String, String>> statisticalData = databaseHelper.getStatisticalData();

        // Get the ListView
        ListView listView = findViewById(R.id.listView);

        // Create SimpleAdapter to bind data to the ListView using the custom layout
        SimpleAdapter adapter = new SimpleAdapter(
                this,
                statisticalData,
                R.layout.statistic_item,
                new String[]{"songName", "clickCount"},
                new int[]{R.id.titleTextView, R.id.number}
        );

        // Set the adapter for the ListView
        listView.setAdapter(adapter);
    }
}
