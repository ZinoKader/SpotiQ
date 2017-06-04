package se.zinokader.spotiq.model;

public class UserPartyInformation {

    private User user;
    private boolean userAlreadyExists = false;
    private Party party;

    public UserPartyInformation(User user, Party party) {
        this.user = user;
        this.party = party;
    }

    public UserPartyInformation(User user, boolean userAlreadyExists, Party party) {
        this.user = user;
        this.userAlreadyExists = userAlreadyExists;
        this.party = party;
    }

    public User getUser() {
        return user;
    }

    public Party getParty() {
        return party;
    }

    public boolean userAlreadyExists() {
        return userAlreadyExists;
    }
}
