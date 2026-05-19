package roomescape;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import jakarta.websocket.Session;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.Map;

import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(TestTimeConfig.class)
public class MissionStepTest {

    SessionFilter sessionFilter = new SessionFilter();

    @Test
    void 예약_조회() {
        RestAssured.given()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(Map.of("name", "a", "password", "test1"))
                .when().post("/login")
                .then().statusCode(200);

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("data.size()", is(4));
    }
}
