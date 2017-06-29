package se.zinokader.spotiq.model;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

public class Party {

    private String title;
    private String password;
    private String hostSpotifyId;
    private String createdTimeStamp;
    private int partyVersionCode;

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

    public String getCreatedTimeStamp() {
        return createdTimeStamp;
    }

    public void setCreatedNowTimeStamp() {
        this.createdTimeStamp = ZonedDateTime.now(ZoneOffset.UTC).toString();
    }

    public int getPartyVersionCode() {
        return partyVersionCode;
    }

    public void setPartyVersionCode(int partyVersionCode) {
        this.partyVersionCode = partyVersionCode;
    }
}
