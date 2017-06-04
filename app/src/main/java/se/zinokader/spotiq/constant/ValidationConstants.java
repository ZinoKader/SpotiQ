package se.zinokader.spotiq.constant;

public class ValidationConstants {

    private ValidationConstants() {}

    public static final int PARTY_TITLE_MIN_LENGTH = 3;

    public static final int PARTY_PASSWORD_MIN_LENGTH = 4;

    public static final String PARTY_TITLE_ERROR_MESSAGE =
            "Title must be at least " + PARTY_TITLE_MIN_LENGTH + " characters long and only consist of A-Z, 0-9, '-' and '_'";

    public static final String PARTY_PASSWORD_ERROR_MESSAGE =
            "Password must be at least " + PARTY_PASSWORD_MIN_LENGTH + " characters long and only consist of A-Z and 0-9";

    //matches alphanumerics, hyphens and underscores
    public static final String PARTY_TITLE_REGEX = "^[a-zA-Z0-9_-]*$";

    //matches alphanumerics
    public static final String PARTY_PASSWORD_REGEX = "^[a-zA-Z0-9]*$";


}
