package roomescape.login.request;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull String name,
        @NotNull String password
) {
}
