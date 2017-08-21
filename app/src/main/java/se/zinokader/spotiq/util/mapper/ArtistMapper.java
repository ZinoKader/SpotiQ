package se.zinokader.spotiq.util.mapper;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;

public class ArtistMapper {

    private ArtistMapper() {}

    public static String joinArtistNames(List<ArtistSimple> artists) {
        List<String> artistNames = new ArrayList<>();
        for (ArtistSimple artist : artists) {
            artistNames.add(artist.name);
        }
        return TextUtils.join(", ", artistNames);
    }

}
