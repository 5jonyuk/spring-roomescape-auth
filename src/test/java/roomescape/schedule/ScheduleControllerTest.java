package roomescape.schedule;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@Import(TestTimeConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ScheduleControllerTest {

    private SessionFilter login() {
        SessionFilter sessionFilter = new SessionFilter();
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", "testAdmin");
        loginRequest.put("password", "test2");

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);

        return sessionFilter;
    }

    @Test
    void 스케줄_생성() {
        SessionFilter sessionFilter = login();
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("date", "2026-05-06");
        schedule.put("timeId", 1);
        schedule.put("themeId", 4);

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(schedule)
                .when().post("/schedules")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(6))
                .body("data.date", is("2026-05-06"))
                .body("data.time_id", is(1))
                .body("data.theme_id", is(4));
    }

    @Test
    void 스케줄_조회() {
        SessionFilter sessionFilter = login();

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/schedules/1")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", is(1))
                .body("data.date", is("2026-05-05"))
                .body("data.time_id", is(1))
                .body("data.theme_id", is(1));
    }

    @Test
    void 스케줄_삭제() {
        SessionFilter sessionFilter = login();
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("date", "2026-05-06");
        schedule.put("timeId", 1);
        schedule.put("themeId", 4);

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(schedule)
                .when().post("/schedules")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(6));

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().delete("/schedules/6")
                .then().log().all()
                .statusCode(204);
    }
}
