package se.zinokader.spotiq.util.mapper;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import se.zinokader.spotiq.model.Song;
import se.zinokader.spotiq.model.User;

public class TrackMapper {

    private TrackMapper() {}

    public static List<Song> tracksToSongs(List<Track> tracks, User user) {
        List<Song> songs = new ArrayList<>();
        for (Track track : tracks) {
            songs.add(trackToSong(track, user));
        }
        return songs;
    }

    public static List<Song> playlistTracksToSongs(List<PlaylistTrack> tracksPager, User user) {
        List<Song> songs = new ArrayList<>();
        for (PlaylistTrack playlistTrack : tracksPager) {
            songs.add(trackToSong(playlistTrack.track, user));
        }
        return songs;
    }

    public static Song trackToSong(Track track, User user) {
        return new Song(user.getUserId(), user.getUserName(), track.id, track.artists, track.album, track.duration_ms, track.name, track.preview_url);
    }

}
