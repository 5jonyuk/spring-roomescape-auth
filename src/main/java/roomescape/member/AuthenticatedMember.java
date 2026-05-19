package roomescape.member;

public record AuthenticatedMember(
        Long id,
        Role role,
        String name
) {
    public static AuthenticatedMember of(long id, Role role, String name) {
        return new AuthenticatedMember(id, role, name);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
