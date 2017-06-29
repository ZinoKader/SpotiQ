package se.zinokader.spotiq.util.exception;

public class PartyVersionLowerException extends Exception {

    public PartyVersionLowerException() {
        super("Party version is lower than user version");
    }

}
