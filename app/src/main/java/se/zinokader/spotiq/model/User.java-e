package se.zinokader.spotiq.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.bumptech.glide.Glide;

import java.util.concurrent.ExecutionException;

public class User implements Parcelable {

    private String profilename;
    private Bitmap profilepicture;
    private String userid;

    public User() {
    }

    public User(Context context, String profilename, String profilepictureurl) throws ExecutionException, InterruptedException {
        this.profilename = profilename;
        this.profilepicture = Glide.with(context).load(profilepictureurl).asBitmap().into(-1, -1).get();
    }

    public User(Context context, String profilename, Bitmap profilepicturebitmap) {
        this.profilename = profilename;
        this.profilepicture = profilepicturebitmap;
    }

    public String getProfileName() {
        return profilename;
    }

    public Bitmap getProfilePicture() {
        return profilepicture;
    }

    public String getUserId() {
        return userid;
    }

    public void setUserId(String userid) {
        this.userid = userid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.profilename);
        dest.writeParcelable(this.profilepicture, flags);
        dest.writeString(this.userid);
    }

    protected User(Parcel in) {
        this.profilename = in.readString();
        this.profilepicture = in.readParcelable(Bitmap.class.getClassLoader());
        this.userid = in.readString();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
