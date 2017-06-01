package se.zinokader.spotiq.util.exception;

public class PartyExistsException extends Exception {

    public PartyExistsException() {
        super("Party already exists");
    }

}
