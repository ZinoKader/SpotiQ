package se.zinokader.spotiq.model;

import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;

public class ChildEvent {

    private DataSnapshot dataSnapshot;
    private String previousChildName;
    private Type changeType;

    public enum Type {
        ADDED, CHANGED, REMOVED, MOVED
    }

    public ChildEvent(DataSnapshot dataSnapshot, Type changeType) {
        this.dataSnapshot = dataSnapshot;
        this.changeType = changeType;
    }

    public ChildEvent(DataSnapshot dataSnapshot, Type changeType, String previousChildName) {
        this.dataSnapshot = dataSnapshot;
        this.changeType = changeType;
        this.previousChildName = previousChildName;
    }

    public DataSnapshot getDataSnapshot() {
        return this.dataSnapshot;
    }

    @Nullable
    public String getPreviousChildName() {
        return this.previousChildName;
    }

    public Type getChangeType() {
        return this.changeType;
    }


}
