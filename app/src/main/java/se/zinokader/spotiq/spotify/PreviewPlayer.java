package se.zinokader.spotiq.spotify;

import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class PreviewPlayer {

    private MediaPlayer player = new MediaPlayer();

    public void playPreview(final String previewurl) {
        //kör i bg thread då player.prepare() är ganska tungt
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resetPlayer();
                    play(previewurl);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        })
        .start();
    }

    public void resetPlayer() {
        player.reset();
    }

    public void play(String previewurl) throws IOException {
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDataSource(previewurl);
        player.prepare();
        player.start();
    }

    public void killPlayer() {
        player.release();
    }

}
