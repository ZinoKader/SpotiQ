package se.zinokader.spotiq.util.helper;

import android.support.annotation.NonNull;
import com.rengwuxian.materialedittext.validation.METValidator;
import se.zinokader.spotiq.constant.ValidationConstants;

public class PartyTitleValidator extends METValidator {

    public PartyTitleValidator() {
        super(ValidationConstants.PARTY_TITLE_ERROR_MESSAGE);
    }

    @Override
    public boolean isValid(@NonNull CharSequence charSequence, boolean b) {
        return charSequence.toString().matches(ValidationConstants.PARTY_TITLE_REGEX)
                && charSequence.length() >= ValidationConstants.PARTY_TITLE_MIN_LENGTH;
    }
}
