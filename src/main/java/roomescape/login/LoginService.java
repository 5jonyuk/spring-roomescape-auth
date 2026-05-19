package roomescape.login;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.AuthenticatedMember;
import roomescape.member.Member;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    public AuthenticatedMember login(String name, String password) {
        Member member = memberRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(""));

        if (!member.isSamePassword(password)) {
            throw new IllegalStateException("Wrong password");
        }

        return AuthenticatedMember.of(member.getId(), member.getRole(), member.getName());
    }
}
