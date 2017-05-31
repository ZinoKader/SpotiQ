package se.zinokader.spotiq.model;

import java.util.List;

public class Party {

    private String title;
    private String password;
    private String hostSpotifyId;
    private List<Song> trackList;

    public Party() {}

    public Party(String title, String password) {
        this.title = title;
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHostSpotifyId() {
        return hostSpotifyId;
    }

    public void setHostSpotifyId(String hostSpotifyId) {
        this.hostSpotifyId = hostSpotifyId;
    }

    public List<Song> getTrackList() {
        return trackList;
    }

    public void setTrackList(List<Song> trackList) {
        this.trackList = trackList;
    }
}
