package se.zinokader.spotiq.model;
import android.util.Base64;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Song implements Serializable {

    private String artist;
    private String songname;
    private String runtime;
    private String uri;
    private String previewurl;
    private String albumarturl;

    private ArrayList<String> votedupby = new ArrayList<>();

    private String addedbyprofilename;
    public String addedbyprofilepicture;

    public Song() {

    }

    public Song(String artist, String songname, long runtime, String uri, String previewurl, String albumarturl) {
        this.artist = artist;
        this.songname = songname;
        this.runtime = (new SimpleDateFormat("mm:ss")).format(new Date(runtime));
        this.uri = uri;
        this.previewurl = previewurl;
        this.albumarturl = albumarturl;
    }

    public Boolean sameSongAs(Song comparetosong) {
        //true om låtnamn och artist är samma (olika addedbyprofilename och addedbyprofilepicture gör att låtar inte kan jämföras som objekt)
        return getSongName().contentEquals(comparetosong.getSongName()) && getArtist().contentEquals(comparetosong.getArtist());
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getSongName() {
        return songname;
    }

    public void setSongName(String songname) {
        this.songname = songname;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getPreviewUrl() {
        return previewurl;
    }

    public void setPreviewUrl(String previewurl) {
        this.previewurl = previewurl;
    }

    public String getAlbumArtUrl() {
        return albumarturl;
    }

    public void setAlbumArtUrl(String albumarturl) {
        this.albumarturl = albumarturl;
    }

    public void setVotedUpBy(ArrayList<String> votedupby) {
        this.votedupby = votedupby;
    }

    public ArrayList<String> getVotedUpBy() {
        return votedupby;
    }

    public String getAddedByProfileName() {
        return addedbyprofilename;
    }

    public void setAddedByProfileName(String profilename) {
        this.addedbyprofilename = profilename;
    }

    @Exclude
    public byte[] getAddedByProfilePicture() {
        return Base64.decode(addedbyprofilepicture, Base64.DEFAULT);
    }

    @Exclude
    public void setAddedByProfilePicture(byte[] profilepicture) {
        this.addedbyprofilepicture = Base64.encodeToString(profilepicture, Base64.DEFAULT);
    }
}

