package se.zinokader.spotiq.util.mapper;

import kaaes.spotify.webapi.android.models.Track;
import se.zinokader.spotiq.model.Song;

public class TrackMapper {

    private TrackMapper() {}

    public static Song trackToSong(Track track) {
        //TODO: Map Track to Song
        return new Song();
    }
}
