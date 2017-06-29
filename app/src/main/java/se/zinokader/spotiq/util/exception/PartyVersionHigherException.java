package se.zinokader.spotiq.util.exception;

public class PartyVersionHigherException extends Exception {

    public PartyVersionHigherException() {
        super("Party version is higher than user version");
    }

}
