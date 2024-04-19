package com.example.cuoiky;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MusicItemAdapter extends ArrayAdapter<MusicItem> {

    public MusicItemAdapter(Context context, int resource, List<MusicItem> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_song, parent, false);
        }

        MusicItem musicItem = getItem(position);

        if (musicItem != null) {
            TextView titleTextView = convertView.findViewById(R.id.titleTextView);
            ImageView imageView = convertView.findViewById(R.id.imageView);

            titleTextView.setText(musicItem.getTrackName());

            // Load the image using Picasso
            Picasso.get().load(musicItem.getDisplayImageUri()).into(imageView);
        }

        return convertView;
    }
}
