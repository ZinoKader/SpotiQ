package se.zinokader.spotiq.util.exception;

public class EmptyTracklistException extends Exception {
    public EmptyTracklistException() {
        super("Tracklist is empty");
    }
}
