package se.zinokader.spotiq.model;

public class User {

    private String userName;
    private int songsRequested;

    public User() {}

    public User(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getSongsRequested() {
        return songsRequested;
    }

    public void setSongsRequested(int songsRequested) {
        this.songsRequested = songsRequested;
    }
}
