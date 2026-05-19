package roomescape.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.AuthenticatedMember;

@Component
public class SessionLoginMemberProvider {

    private static final String LOGIN_MEMBER_KEY = "loginMember";

    public AuthenticatedMember getAuthenticatedMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }

        AuthenticatedMember member = (AuthenticatedMember) session.getAttribute(LOGIN_MEMBER_KEY);
        if (member == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }
        return member;
    }
}
