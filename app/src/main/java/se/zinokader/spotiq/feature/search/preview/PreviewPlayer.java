package se.zinokader.spotiq.feature.search.preview;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

import se.zinokader.spotiq.constant.LogTag;

public class PreviewPlayer extends MediaPlayer implements MediaPlayer.OnPreparedListener {

    private MediaPlayer mediaPlayer = new MediaPlayer();

    public PreviewPlayer() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)

                .build();
        mediaPlayer.setAudioAttributes(audioAttributes);
    }

    public void playPreview(String previewUrl) {

        stopPreview();
        mediaPlayer.reset();

        try {
            mediaPlayer.setDataSource(previewUrl);
        }
        catch (IOException e) {
            Log.d(LogTag.LOG_PREVIEW_PLAYER, "Error when setting preview audio data source " + e.getMessage());
            return;
        }

        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    public void stopPreview() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public void release() {
        mediaPlayer.release();
    }

}
