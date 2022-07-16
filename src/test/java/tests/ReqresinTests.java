package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import models.LombokUser;
import models.LombokUserData;
import org.apache.commons.validator.GenericValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReqresinTests {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "https://reqres.in/";
    }

    @Test
    void loginTest() {
        String loginReqBody = "{ \"email\": \"eve.holt@reqres.in\", \"password\": \"cityslicka\" }";
        given()
                .log().all()
                .body(loginReqBody)
                .contentType(JSON)
                .when()
                .post("/api/login")
                .then()
                .log().all()
                .statusCode(200)
                .body("token", notNullValue());
    }

    @Test
    void missingPasswordLoginTest() {
        String loginReqBody = "{ \"email\": \"eve.holt@reqres.in\"}";
        given()
                .log().uri()
                .log().body()
                .body(loginReqBody)
                .contentType(JSON)
                .when()
                .post("/api/login")
                .then()
                .log().status()
                .log().body()
                .statusCode(400)
                .body("error", is("Missing password"))
        ;
    }

    @Test
    void createUserTest() {
        String createUserReqBody = "{ \"name\": \"morpheus\", \"job\": \"leader\" }";
        /* SHOULD RETURN
        {
            "name": "morpheus",
                "job": "leader",
                "id": "313",
                "createdAt": "2022-06-08T21:11:31.710Z"
        }
        */

        //SCHEMA GENERATOR https://www.liquid-technologies.com/online-json-to-schema-converter

        given()
                .log().uri()
                .log().body()
                .body(createUserReqBody)
                .contentType(JSON)
                .when()
                .post("/api/users")
                .then()
                .log().status()
                .log().body()
                .statusCode(201)
                .body("name", is("morpheus"))
                .body("job", is("leader"))
                .body(matchesJsonSchemaInClasspath("createUserSchema.json")); //dependency JSON Schema Validator
    }

    @Test
    void createUserAssertIdAndDateTest() {
        final String userName = "morpheus";
        final String userJob = "leader";

        String createUserReqBody = "{ \"name\": \"" + userName + "\", \"job\": \"" + userJob + "\" }";
        /* SHOULD RETURN
        {
            "name": "morpheus",
            "job": "leader",
            "id": "313",
            "createdAt": "2022-06-08T21:11:31.710Z"
        }
        */

        //SCHEMA GENERATOR https://www.liquid-technologies.com/online-json-to-schema-converter

        JsonPath responseJson = given()
                .log().uri()
                .log().body()
                .body(createUserReqBody)
                .contentType(JSON)
                .when()
                .post("/api/users")
                .then()
                .log().status()
                .log().body()
                .statusCode(201)
                .body("name", is(userName))
                .body("job", is(userJob))
                .body(matchesJsonSchemaInClasspath("createUserSchema.json")) //dependency JSON Schema Validator
                .extract().body().jsonPath();
        String createdAt = responseJson.get("createdAt");
        String id = responseJson.get("id");
        assertTrue(GenericValidator.isInt(id));
        assertTrue(GenericValidator.isDate(createdAt, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", false));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        LocalDateTime createdAtDateTime = LocalDateTime.parse(createdAt, formatter);
        System.out.println("createdAtDateTime= " + createdAtDateTime);

        //get local dateTimeAtUtc
        Clock cl = Clock.systemUTC();
        LocalDateTime currentDateTimeAtUtc = LocalDateTime.now(cl);
        System.out.println("currentDateTimeAtUtc= " + currentDateTimeAtUtc);

        long diffInSeconds = ChronoUnit.SECONDS.between(createdAtDateTime, currentDateTimeAtUtc);
        System.out.println("diffInSeconds= " + diffInSeconds);
        final long maxTimeDiffInSec = 5;
        assertThat(diffInSeconds, is(lessThan(maxTimeDiffInSec)));

        //date format https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
    }

    @Test
    void listUsersTest() {
        given()
                .log().uri()
                .log().body()
                .when()
                .get("/api/users?page=1")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body(notNullValue())
        ;
    }

    @Test
    void singleUserTest() {
        given()
                .log().uri()
                .log().body()
                .when()
                .get("/api/users/1")
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body(notNullValue())
        ;
    }

    @Test
    void singleUserNotFoundTest() {
        given()
                .log().uri()
                .log().body()
                .when()
                .get("/api/users/100500")
                .then()
                .log().status()
                .log().body()
                .statusCode(404)
                .body(notNullValue())
        ;
    }

    @Test
    void updateUserTest() {
        final String userName = "morpheus";
        final String userJob = "some_new_job";
        final Integer userNumber = 2;

        String createUserReqBody = "{ \"name\": \"" + userName + "\", \"job\": \"" + userJob + "\" }";

        given()
                .log().uri()
                .log().body()
                .body(createUserReqBody)
                .contentType(JSON)
                .when()
                .patch("/api/users/" + userNumber)
                .then()
                .log().status()
                .log().body()
                .statusCode(200)
                .body("name", is(userName))
                .body("job", is(userJob))
        ;

    }
}
