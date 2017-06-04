package se.zinokader.spotiq.util.exception;

public class PartyWrongPasswordException extends Exception {

    public PartyWrongPasswordException() {
        super("Wrong password was entered for party");
    }

}
