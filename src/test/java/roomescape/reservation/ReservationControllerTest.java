package roomescape.reservation;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.config.TestTimeConfig;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;

@Import(TestTimeConfig.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = {"/truncate.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationControllerTest {

    private SessionFilter login() {
        SessionFilter sessionFilter = new SessionFilter();
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("name", "a");
        loginRequest.put("password", "test1");

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200);

        return sessionFilter;
    }

    private Map<String, Object> reservationRequest() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("memberId", 1);
        reservation.put("date", "2026-05-05");
        reservation.put("timeId", 4);
        reservation.put("themeId", 4);
        return reservation;
    }

    @Test
    void 예약_생성() {
        SessionFilter sessionFilter = login();

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(reservationRequest())
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(5))
                .body("data.memberId", is(1))
                .body("data.scheduleId", is(4));
    }

    @Test
    void 예약_조회() {
        SessionFilter sessionFilter = login();

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4));
    }

    @Test
    void 예약_추가_및_삭제() {
        SessionFilter sessionFilter = login();

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .contentType(ContentType.JSON)
                .body(reservationRequest())
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("success", is(true))
                .body("data.id", is(5));

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(5));

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4));
    }

    @Test
    void 나의_특정_예약_삭제_및_나의_예약_목록_조회() {
        SessionFilter sessionFilter = login();

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(4));

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .pathParam("id", 1)
                .when().delete("/reservations/{id}")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .filter(sessionFilter)
                .when().get("/reservations/me")
                .then().log().all()
                .statusCode(200)
                .body("success", is(true))
                .body("data.size()", is(3));
    }
}
