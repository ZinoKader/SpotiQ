package se.zinokader.spotiq.util.comparator;

import org.threeten.bp.LocalDateTime;

import java.util.Comparator;

import se.zinokader.spotiq.model.User;

public class PartyMemberComparator {

    public static ByJoinedTimeComparator getByJoinedTimeComparator() {
        return new ByJoinedTimeComparator();
    }

    static class ByJoinedTimeComparator implements Comparator<User> {

        @Override
        public int compare(User firstUser, User secondUser) {
            LocalDateTime firstUserParsedTimeStamp = LocalDateTime.parse(firstUser.getJoinedTimeStamp());
            LocalDateTime secondUserParsedTimeStamp = LocalDateTime.parse(secondUser.getJoinedTimeStamp());
            if (firstUserParsedTimeStamp.isAfter(secondUserParsedTimeStamp)) {
                return 1;
            }
            else {
                return -1;
            }
        }
    }

}
