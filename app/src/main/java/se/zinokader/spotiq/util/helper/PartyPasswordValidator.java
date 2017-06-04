package se.zinokader.spotiq.util.helper;

import android.support.annotation.NonNull;
import com.rengwuxian.materialedittext.validation.METValidator;
import se.zinokader.spotiq.constant.ValidationConstants;

public class PartyPasswordValidator extends METValidator {

    public PartyPasswordValidator() {
        super(ValidationConstants.PARTY_PASSWORD_ERROR_MESSAGE);
    }

    @Override
    public boolean isValid(@NonNull CharSequence charSequence, boolean b) {
        return charSequence.toString().matches(ValidationConstants.PARTY_PASSWORD_REGEX)
                && charSequence.length() >= ValidationConstants.PARTY_PASSWORD_MIN_LENGTH;
    }
}
