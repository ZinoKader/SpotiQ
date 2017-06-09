package se.zinokader.spotiq.util.mapper;

import kaaes.spotify.webapi.android.models.Track;
import se.zinokader.spotiq.model.Song;

public class TrackMapper {

    private TrackMapper() {}

    public static Song trackToSong(Track track, String addedBySpotifyId) {
        return new Song(addedBySpotifyId, track.id, track.artists, track.album, track.duration_ms, track.name, track.preview_url);
    }
}
