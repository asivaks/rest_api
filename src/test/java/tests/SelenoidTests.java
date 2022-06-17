package tests;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SelenoidTests {
    // 1. make request to https://selenoid.autotests.cloud/status
    // 2. get response
    // {
    //  "total": 20,
    //  "used": 0,
    //  "queued": 0,
    //  "pending": 0,
    //  "browsers": {
    //    "chrome": {
    //      "100.0": {
    //
    //      },
    //      "99.0": {
    //
    //      }
    //    },
    //    "firefox": {
    //      "97.0": {
    //
    //      },
    //      "98.0": {
    //
    //      }
    //    },
    //    "opera": {
    //      "84.0": {
    //
    //      },
    //      "85.0": {
    //
    //      }
    //    }
    //  }
    //}
    // 3. check total=20

    @Test
    void checkTotal() {
        given()
                .when()
                .get("https://selenoid.autotests.cloud/status")
                .then()
                .body("total", is(20));
    }

    @Test
    void checkWithoutGivenTotal() {
        get("https://selenoid.autotests.cloud/status")
                .then()
                .body("total", is(20));
    }

    @Test
    void checkWithLogsTotal() {
        given()
                .log().all()
                .when()
                .get("https://selenoid.autotests.cloud/status")
                .then()
                .log().all()
                .body("total", is(20));
    }

    @Test
    void checkChrome() {
        get("https://selenoid.autotests.cloud/status")
                .then()
                .body("browsers.chrome", hasKey("100.0"));
    }

    @Test
    void checkTotalBadPractise() {
        Response response = get("https://selenoid.autotests.cloud/status")
                .then()
                .extract().response();
        System.out.println(response.asString());
        String expectedResponse = "{\"total\":20,\"used\":0,\"queued\":0,\"pending\":0,\"browsers\":" +
                "{\"chrome\":{\"100.0\":{},\"99.0\":{}},\"firefox\":{\"97.0\":{},\"98.0\":{}},\"opera\":{\"84.0\":{},\"85.0\":{}}}}\n";
        assertEquals(expectedResponse, response.asString());
    }

    @Test
    void checkTotalGoodPractise() {
        Integer actualTotal = get("https://selenoid.autotests.cloud/status")
                .then()
                .extract().path("total");

        Integer expectedTotal = 20;
        assertEquals(expectedTotal, actualTotal);
    }

    @Test
    void check401Status() {
        get("https://selenoid.autotests.cloud/wd/hub/status")
                .then()
                .statusCode(401);
    }

    @Test
    void check200StatusWithAuthInUrl() {
        //get("https://user1:1234@selenoid.autotests.cloud/wd/hub/status")
        //        .then()
        //        .statusCode(200);
        // =
        given()
                .auth().basic("user1", "1234")
                .get("https://selenoid.autotests.cloud/wd/hub/status")
                .then()
                .statusCode(200);
    }


}
