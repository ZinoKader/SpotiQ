package se.zinokader.spotiq.model;

import java.util.List;
import kaaes.spotify.webapi.android.models.Image;
import se.zinokader.spotiq.constant.ApplicationConstants;

public class User {

    private String userId;
    private String userName;
    private String userImageUrl;
    private int songsRequested;

    public User() {}

    public User(String userId, String userName, List<Image> userImages) {
        this.userId = userId;
        this.userName = userName == null
                ? userId
                : userName;
        this.userImageUrl = userImages.isEmpty()
                ? ApplicationConstants.PROFILE_IMAGE_PLACEHOLDER_URL
                : userImages.get(0).url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImageUrl() {
        return userImageUrl;
    }

    public void setUserImageUrl(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public int getSongsRequested() {
        return songsRequested;
    }

    public void setSongsRequested(int songsRequested) {
        this.songsRequested = songsRequested;
    }
}
