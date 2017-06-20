package se.zinokader.spotiq.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.threeten.bp.LocalDateTime;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import se.zinokader.spotiq.constant.ApplicationConstants;

public class User implements Parcelable {

    private String userId;
    private String userName;
    private String userImageUrl;
    private String joinedTimeStamp;
    private int songsRequested = 0;
    private boolean hasHostPriviliges = false;

    public User() {}

    public User(String userId, String userName, List<Image> userImages) {
        this.userId = userId;
        this.userName = userName == null
            ? userId
            : userName;
        this.userImageUrl = userImages.isEmpty()
            ? ApplicationConstants.PROFILE_IMAGE_PLACEHOLDER_URL
            : userImages.get(0).url;
        this.songsRequested = 0;
        this.hasHostPriviliges = false;
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

    public boolean getHasHostPriviliges() {
        return hasHostPriviliges;
    }

    public void setHasHostPriviliges() {
        this.hasHostPriviliges = true;
    }

    public String getJoinedTimeStamp() {
        return joinedTimeStamp;
    }

    public void setJoinedNowTimeStamp() {
        this.joinedTimeStamp = LocalDateTime.now().toString();
    }

    protected User(Parcel in) {
        userId = in.readString();
        userName = in.readString();
        userImageUrl = in.readString();
        joinedTimeStamp = in.readString();
        songsRequested = in.readInt();
        hasHostPriviliges = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(userImageUrl);
        dest.writeString(joinedTimeStamp);
        dest.writeInt(songsRequested);
        dest.writeByte((byte) (hasHostPriviliges ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}