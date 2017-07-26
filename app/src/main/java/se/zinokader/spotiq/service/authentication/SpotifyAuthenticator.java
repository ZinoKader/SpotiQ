package se.zinokader.spotiq.service.authentication;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.temporal.ChronoUnit;

public class SpotifyAuthenticator {

    private String accessToken;
    private LocalDateTime expiryTimeStamp;

    /**
     * In case our SpotifyAuthenticator is garbage collected, when a new one is recreated, it is instantly expired
     */
    public SpotifyAuthenticator() {
        this.accessToken = "";
        this.expiryTimeStamp = LocalDateTime.now();
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(expiryTimeStamp);
    }

    public long getExpiresIn() {
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), expiryTimeStamp);
    }

    public LocalDateTime getExpiryTimeStamp() {
        return expiryTimeStamp;
    }

    /**
     * Creates a timestamp of this instant + {@code expiresIn} seconds
     * @param expiresIn The amount of seconds from this instant that the authentication token expires in
     */
    public void setExpiryTimeStamp(int expiresIn) {
        this.expiryTimeStamp = LocalDateTime.now().plusSeconds(expiresIn);
    }
}
