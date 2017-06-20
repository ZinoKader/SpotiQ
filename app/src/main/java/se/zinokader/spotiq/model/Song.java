package se.zinokader.spotiq.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import se.zinokader.spotiq.constant.ApplicationConstants;


public class Song implements Parcelable {

    private String addedBySpotifyId;
    private String addedByUserName;
    private String songSpotifyId;
    private AlbumSimple album;
    private List<ArtistSimple> artists;
    private long durationMs;
    private String name;
    private String previewUrl;

    public Song() {
    }

    public Song(String addedBySpotifyId, String addedByUserName, String songSpotifyId,
                List<ArtistSimple> artists, AlbumSimple album, long durationMs,
                String name, String previewUrl) {
        this.addedBySpotifyId = addedBySpotifyId;
        this.songSpotifyId = songSpotifyId;
        this.addedByUserName = addedByUserName;
        this.artists = artists;
        this.album = album;
        this.durationMs = durationMs;
        this.name = name;
        this.previewUrl = previewUrl;
    }

    public String getAddedBySpotifyId() {
        return addedBySpotifyId;
    }

    public String getAddedByUserName() {
        return addedByUserName != null
            ? addedByUserName
            : addedBySpotifyId;
    }

    public String getSongSpotifyId() {
        return songSpotifyId;
    }

    public List<ArtistSimple> getArtists() {
        return artists;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getName() {
        return name;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public AlbumSimple getAlbum() {
        return album;
    }

    @Exclude
    public String getSongUri() {
        return "spotify:track:" + songSpotifyId;
    }

    @Exclude
    public String getAlbumArtUrl() {
        return album.images.isEmpty()
            ? ApplicationConstants.ALBUM_ART_PLACEHOLDER_URL
            : album.images.get(0).url;
    }

    protected Song(Parcel in) {
        addedBySpotifyId = in.readString();
        addedByUserName = in.readString();
        songSpotifyId = in.readString();
        album = (AlbumSimple) in.readValue(AlbumSimple.class.getClassLoader());
        if (in.readByte() == 0x01) {
            artists = new ArrayList<ArtistSimple>();
            in.readList(artists, ArtistSimple.class.getClassLoader());
        } else {
            artists = null;
        }
        durationMs = in.readLong();
        name = in.readString();
        previewUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(addedBySpotifyId);
        dest.writeString(addedByUserName);
        dest.writeString(songSpotifyId);
        dest.writeValue(album);
        if (artists == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(artists);
        }
        dest.writeLong(durationMs);
        dest.writeString(name);
        dest.writeString(previewUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
