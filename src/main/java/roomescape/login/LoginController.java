package roomescape.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.login.request.LoginRequest;
import roomescape.member.AuthenticatedMember;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @RequestBody LoginRequest body,
            HttpServletRequest request
    ) {
        AuthenticatedMember member = loginService.login(body.name(), body.password());

        HttpSession session = request.getSession();
        session.setAttribute("loginMember", member);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
