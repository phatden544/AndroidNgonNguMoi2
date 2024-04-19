package com.example.cuoiky;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {

    private Visualizer visualizer;
    private MediaPlayer mediaPlayer;
    private Paint paint;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.colorAccent));
        paint.setStrokeWidth(5);
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        setupVisualizer();
    }

    public void setupVisualizer() {
        if (mediaPlayer != null) {
            // Attach the Visualizer to the MediaPlayer
            visualizer = new Visualizer(mediaPlayer.getAudioSessionId());
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            // Set up the listener for the visualizer data
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    // Process and visualize audio data here
                    postInvalidate();
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    // Optional: Process and visualize FFT data here
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);

            visualizer.setEnabled(true);
        }
    }

    @Override

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(getResources().getColor(android.R.color.black));

        int width = getWidth();
        int height = getHeight();

        int centerY = height / 2;

        if (mediaPlayer != null && visualizer != null) {
            // Visualize audio data here using waveform or FFT data
            // For example, you can use waveform data:
            byte[] waveform = new byte[visualizer.getCaptureSize()];
            visualizer.getWaveForm(waveform);

            for (int i = 0; i < waveform.length - 1; i++) {
                float x = (float) i / waveform.length * width;
                float y = centerY + waveform[i] / 128.0f * centerY;

                canvas.drawLine(x, centerY, x, y, paint);
            }
        }

        postInvalidateDelayed(50); // Redraw every 50 milliseconds
    }

    public void releaseVisualizer() {
        if (visualizer != null) {
            visualizer.setEnabled(false);
            visualizer.release();
        }
    }
}

