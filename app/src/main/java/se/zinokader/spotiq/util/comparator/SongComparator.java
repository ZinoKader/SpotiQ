package se.zinokader.spotiq.util.comparator;

import java.util.List;

import se.zinokader.spotiq.model.Song;

public class SongComparator {

    public static boolean contains(Song s1, List<Song> songList) {
        for (Song listSong : songList) {
            if (isEqual(s1, listSong)) return true;
        }
        return false;
    }

    public static boolean isEqual(Song s1, Song s2) {
        return (s1.getSongSpotifyId().equals(s2.getSongSpotifyId()));
    }

}
