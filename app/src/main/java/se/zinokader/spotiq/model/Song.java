package se.zinokader.spotiq.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

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

    @Exclude
    @Override
    public int describeContents() {
        return 0;
    }

    @Exclude
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.addedBySpotifyId);
        dest.writeString(this.addedByUserName);
        dest.writeString(this.songSpotifyId);
        dest.writeParcelable(this.album, flags);
        dest.writeTypedList(this.artists);
        dest.writeLong(this.durationMs);
        dest.writeString(this.name);
        dest.writeString(this.previewUrl);
    }

    protected Song(Parcel in) {
        this.addedBySpotifyId = in.readString();
        this.addedByUserName = in.readString();
        this.songSpotifyId = in.readString();
        this.album = in.readParcelable(AlbumSimple.class.getClassLoader());
        this.artists = in.createTypedArrayList(ArtistSimple.CREATOR);
        this.durationMs = in.readLong();
        this.name = in.readString();
        this.previewUrl = in.readString();
    }

    @Exclude
    public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {
        @Exclude
        @Override
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        @Exclude
        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
