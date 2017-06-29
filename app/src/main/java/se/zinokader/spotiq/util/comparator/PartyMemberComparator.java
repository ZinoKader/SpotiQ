package se.zinokader.spotiq.util.comparator;

import org.threeten.bp.ZonedDateTime;

import java.util.Comparator;

import se.zinokader.spotiq.model.User;

public class PartyMemberComparator {

    public static ByJoinedTimeComparator getByJoinedTimeComparator() {
        return new ByJoinedTimeComparator();
    }

    static class ByJoinedTimeComparator implements Comparator<User> {

        @Override
        public int compare(User firstUser, User secondUser) {
            ZonedDateTime firstUserParsedTimeStamp = ZonedDateTime.parse(firstUser.getJoinedTimeStamp());
            ZonedDateTime secondUserParsedTimeStamp = ZonedDateTime.parse(secondUser.getJoinedTimeStamp());
            if (firstUserParsedTimeStamp.isAfter(secondUserParsedTimeStamp)) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }

}
