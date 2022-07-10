package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import helpers.Attach;
import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.remote.DesiredCapabilities;


import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static helpers.CustomApiListener.withCustomTemplates;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;


public class DemowebshopTests {

    static String login = "qaguru@qa.guru",
                  password = "qaguru@qa.guru1",
                  authCookieName = "NOPCOMMERCE.AUTH";

    @BeforeAll
    @Description("ass listener, set base URLs")
    static void beforeAll() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());
        Configuration.baseUrl = "http://demowebshop.tricentis.com";
        RestAssured.baseURI = "http://demowebshop.tricentis.com";

        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", "chrome");
        capabilities.setCapability("browserVersion", "100.0");
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", true);
        Configuration.browserCapabilities = capabilities;
    }

    //it is stated that WebDriver in Selenide is closed after each test but sometimes this doesn't happen
    @AfterEach
    @Description("attachments + it is stated that WebDriver in Selenide is closed after each test but sometimes this doesn't happen")
    void afterEach() {
        //Attach.screenshotAs("Test screenshot");
        Attach.pageSource();
        //Attach.browserConsoleLogs();
        //Attach.addVideo();
        closeWebDriver();
    }

    @Test
    @Tag("demowebshop")
    @Owner("Aleksey Sivaks")
    @Feature("Login")
    @DisplayName("Successful UI authorization to demowebshop ")
    @Severity(SeverityLevel.BLOCKER)
    void loginUiTest() {
        step("Open login page", () ->
            open("/login"));

        step("Fill login form", () ->{
           $("#Email").setValue(login);
           $("#Password").setValue(password).pressEnter();
        });

        step("Verify successful authorization", () ->
                $(".account").shouldHave(text(login)));

    }

    @Test
    @Tag("demowebshop")
    @Owner("Aleksey Sivaks")
    @Feature("Login")
    @DisplayName("Successful API authorization to demowebshop ")
    @Severity(SeverityLevel.BLOCKER)
    void loginApiTest() {

        /*
        //extract all cookies
        Map<String, String> cookies = given()
                //Content-Type: application/x-www-form-urlencoded
                .contentType("application/x-www-form-urlencoded")
                //Email=qaguru%40qa.guru&Password=qaguru%40qa.guru1&RememberMe=false
                //.body("Email=" + login + "&Password=" + password + "&RememberMe=false")  //the same ↓
                //.body(format("Email=%s&Password=%s&RememberMe=false", login, password))  //the same ↓
                .formParam("Email", login)
                .formParam("Password", password)
                .log().all()
                .when()
                .post("/login")
                .then()
                .log().all()
                .statusCode(302)
                .extract().cookies();
         */

        step("Get cookie by api and set it to browser", () -> {
            //extract only auth cookie
            String authCookieValue = given()
                    //.filter(new AllureRestAssured())  //add logs to allure, .log().all() sends logs to console only
                    .filter(withCustomTemplates())
                    //Content-Type: application/x-www-form-urlencoded
                    .contentType("application/x-www-form-urlencoded")
                    //Email=qaguru%40qa.guru&Password=qaguru%40qa.guru1&RememberMe=false
                    //.body("Email=" + login + "&Password=" + password + "&RememberMe=false")  //the same ↓
                    //.body(format("Email=%s&Password=%s&RememberMe=false", login, password))  //the same ↓
                    .formParam("Email", login)
                    .formParam("Password", password)
                    .log().all()
                    .when()
                    .post("/login")
                    .then()
                    .log().all()
                    .statusCode(302)
                    .extract().cookie(authCookieName);

            step("Open minimal content, because cookie can be set when site is opened", () -> {
                //need to open smth from the website to be able to set cookie
                open("/Themes/DefaultClean/Content/images/logo.png");
            });
            step("Set cookie to browser", () -> {
                Cookie authCookie = new Cookie(authCookieName, authCookieValue);
                WebDriverRunner.getWebDriver().manage().addCookie(authCookie);
            });
        });

        step("Open main page", () -> {
            open("/");
        });
        step("Verify successful authorization", () ->
                $(".account").shouldHave(text(login)));

    }
}