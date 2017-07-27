package se.zinokader.spotiq.service.player;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import se.zinokader.spotiq.constant.ApplicationConstants;

class MediaSessionHandler {

    private MediaSession mediaSession;
    private MediaSessionCompat mediaSessionCompat;
    private PlaybackState playbackStatePlaying;
    private PlaybackState playbackStatePaused;
    private PlaybackStateCompat playbackStateCompatPlaying;
    private PlaybackStateCompat playbackStateCompatPaused;
    
    MediaSessionHandler(Context context) {
        mediaSession = new MediaSession(context, ApplicationConstants.MEDIA_SESSSION_TAG);
        mediaSessionCompat = new MediaSessionCompat(context, ApplicationConstants.MEDIA_SESSSION_TAG);
        playbackStatePlaying = new PlaybackState.Builder()
            .setState(PlaybackState.STATE_PLAYING, 0, 1).build();
        playbackStatePaused = new PlaybackState.Builder()
            .setState(PlaybackState.STATE_STOPPED, 0, 1).build();
        playbackStateCompatPlaying = new PlaybackStateCompat.Builder()
            .setState(PlaybackState.STATE_PLAYING, 0, 1).build();
        playbackStateCompatPaused = new PlaybackStateCompat.Builder()
            .setState(PlaybackState.STATE_STOPPED, 0, 1).build();
    }

    void setPlaybackStatePlaying() {
        mediaSession.setPlaybackState(playbackStatePlaying);
        mediaSessionCompat.setPlaybackState(playbackStateCompatPlaying);
    }

    void setPlaybackStatePaused() {
        mediaSession.setPlaybackState(playbackStatePaused);
        mediaSessionCompat.setPlaybackState(playbackStateCompatPaused);
    }

    void setSessionActive() {
        mediaSession.setActive(true);
        mediaSessionCompat.setActive(true);
    }

    void setMetaData(MediaMetadata mediaMetadata) {
        mediaSession.setMetadata(mediaMetadata);
    }

    void setMetaData(MediaMetadataCompat mediaMetadataCompat) {
        mediaSessionCompat.setMetadata(mediaMetadataCompat);
    }

    MediaSession getMediaSession() {
        return mediaSession;
    }

    MediaSessionCompat getMediaSessionCompat() {
        return mediaSessionCompat;
    }

    void releaseSessions() {
        mediaSession.release();
        mediaSessionCompat.release();
    }

}
