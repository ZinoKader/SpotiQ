package se.zinokader.spotiq.util.exception;

public class EmptyTracklistException extends Throwable {
    public EmptyTracklistException() {
        super("Tracklist is empty");
    }
}
