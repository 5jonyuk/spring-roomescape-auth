package roomescape.theme.presentation.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.theme.application.ThemeService;
import roomescape.theme.dto.response.ThemeFindResponse;

import java.util.List;

@RestController
@RequestMapping("/api/themes")
@RequiredArgsConstructor
public class UserThemeController {
    private final ThemeService themeService;

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<ThemeFindResponse>>> findByDayAndLimit() {
        List<ThemeFindResponse> responses = themeService.findPopularTheme();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }
}
