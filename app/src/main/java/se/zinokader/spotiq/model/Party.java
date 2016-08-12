package se.zinokader.spotiq.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Party implements Parcelable {

    private String partyname;
    private String partypassword;
    private String partyhost;

    public Party() {
    }

    public String getPartyName() {
        return partyname;
    }

    public void setPartyName(String partyname) {
        this.partyname = partyname;
    }

    public String getPartyPassword() {
        return partypassword;
    }

    public void setPartyPassword(String partypassword) {
        this.partypassword = partypassword;
    }

    public String getPartyHost() {
        return partyhost;
    }

    public void setPartyHost(String partyhost) {
        this.partyhost = partyhost;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.partyname);
        dest.writeString(this.partypassword);
        dest.writeString(this.partyhost);
    }

    protected Party(Parcel in) {
        this.partyname = in.readString();
        this.partypassword = in.readString();
        this.partyhost = in.readString();
    }

    public static final Parcelable.Creator<Party> CREATOR = new Parcelable.Creator<Party>() {
        @Override
        public Party createFromParcel(Parcel source) {
            return new Party(source);
        }

        @Override
        public Party[] newArray(int size) {
            return new Party[size];
        }
    };

}




