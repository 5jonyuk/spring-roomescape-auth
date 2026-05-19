package roomescape.common.argumentresolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.AuthenticatedMember;
import roomescape.member.LoginMember;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && parameter.getParameterType().equals(AuthenticatedMember.class);
    }

    @Override
    public AuthenticatedMember resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) throws Exception {
        HttpServletRequest servletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (servletRequest == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }

        HttpSession session = servletRequest.getSession(false);
        if (session == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }

        AuthenticatedMember member = (AuthenticatedMember) session.getAttribute("loginMember");
        if (member == null) {
            throw new EscapeRoomException(ErrorCode.UNAUTHORIZED);
        }
        return member;
    }
}
