package roomescape.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.AuthenticatedMember;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }

        AuthenticatedMember member = (AuthenticatedMember) session.getAttribute("loginMember");
        if (member == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }

        if (!member.isAdmin()) {
            throw new EscapeRoomException(ErrorCode.FORBIDDEN);
        }
        return true;
    }
}
