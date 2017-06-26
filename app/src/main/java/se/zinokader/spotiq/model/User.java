package se.zinokader.spotiq.model;

import org.threeten.bp.LocalDateTime;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import se.zinokader.spotiq.constant.ApplicationConstants;

public class User {

    private String userId;
    private String userName;
    private String userImageUrl;
    private String joinedTimeStamp;
    private int songsRequested = 0;
    private boolean hasHostPrivileges = false;

    public User() {}

    public User(String userId, String userName, List<Image> userImages) {
        this.userId = userId;
        this.userName = userName == null
            ? userId
            : userName;
        this.songsRequested = 0;
        this.hasHostPrivileges = false;
        if (userImages != null && !userImages.isEmpty()) {
            this.userImageUrl = userImages.get(0).url;
        }
        else {
            this.userImageUrl = ApplicationConstants.PROFILE_IMAGE_PLACEHOLDER_URL;
        }
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

    public boolean getHasHostPrivileges() {
        return hasHostPrivileges;
    }

    public void setHasHostPrivileges() {
        this.hasHostPrivileges = true;
    }

    public String getJoinedTimeStamp() {
        return joinedTimeStamp;
    }

    public void setJoinedNowTimeStamp() {
        this.joinedTimeStamp = LocalDateTime.now().toString();
    }

}