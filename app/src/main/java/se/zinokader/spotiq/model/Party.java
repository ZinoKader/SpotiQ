package se.zinokader.spotiq.model;

public class Party {

    private String title;
    private String password;
    private String hostSpotifyId;

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

    public String getHostSpotifyId() {
        return hostSpotifyId;
    }

    public void setHostSpotifyId(String hostSpotifyId) {
        this.hostSpotifyId = hostSpotifyId;
    }

}
