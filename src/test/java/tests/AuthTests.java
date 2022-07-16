package tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import config.CredentialsConfig;
import config.DemowebshopConfig;
import config.DodoConfig;
import helpers.Attach;
import io.qameta.allure.*;
import io.qameta.allure.selenide.AllureSelenide;
import io.restassured.RestAssured;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.*;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.Map;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$;
import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthTests {

    static DodoConfig config = ConfigFactory.create(DodoConfig.class, System.getProperties());

    static String login = config.userLogin();
    static String password = config.userPassword();
    static String authCookieName = config.authCookieName();

    @BeforeAll
    @Description("ass listener, set base URLs")
    static void beforeAll() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide());

        //Configuration.baseUrl = uiBaseUrl;
        //RestAssured.baseURI = apiBaseUri;

        Configuration.baseUrl = config.webUrl();
        //Configuration.browser = browserName;
        //Configuration.browserVersion = browserVersion;
        RestAssured.baseURI = config.apiUrl();

        DesiredCapabilities capabilities = new DesiredCapabilities();
        //capabilities.setCapability("browser", browserName);
        //capabilities.setCapability("version", browserVersion);
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("enableVideo", true);
        Configuration.browserCapabilities = capabilities;
        System.out.println("capabilities= " + capabilities.asMap());
        System.out.println("Will login to " + Configuration.baseUrl + " with user " + login + " & password " + password);

        //CredentialsConfig credentialsConfig = ConfigFactory.create(CredentialsConfig.class);
        //String remoteString = "https://" + credentialsConfig.remoteUser() + ":" + credentialsConfig.remotePassword() + "@" + credentialsConfig.remoteUrl();
        //System.out.println("Connecting to " + remoteString);
        //Configuration.remote = remoteString;

    }

    //it is stated that WebDriver in Selenide is closed after each test but sometimes this doesn't happen
    @AfterEach
    @Description("attachments + it is stated that WebDriver in Selenide is closed after each test but sometimes this doesn't happen")
    void afterEach() {
        //Attach.screenshotAs("Test screenshot");
        //Attach.pageSource();
        //Attach.browserConsoleLogs();
        //Attach.addVideo();
        //closeWebDriver();
    }

    @Test
    @Tag("dodo")
    @Owner("Aleksey Sivaks")
    @Feature("Login")
    @DisplayName("Step 1 Dodo auth")
    @Severity(SeverityLevel.BLOCKER)
    void loginApiTest() {
        Map<String, String> cookies1 = given()
                .contentType("multipart/form-data")
                .multiPart("CountryCode", "Ru")
                .multiPart("login", login)
                .multiPart("password", password)
                .log().all()
                .when()
                .post("/Authenticate/LogOn")
                .then()
                .log().all()
                .statusCode(302)
                .extract().cookies()
                ;

        System.out.println("cookies=" + cookies1);

        // TODO: 14.07.2022 send  GET ru-auth.dev-drive.dodois.dev/Authenticate/Roles and get ID for RootAdmin
        //"name": "RootAdmin",
        //        "title": "Центральный администратор",
        //        "units": [
        //       {
        //    "title": "Центральный офис",
        //        "id": "000D3A240C719A8711E68ABA13F7F862"
        //       }
        //],


        // Send POST https://ru-auth.dev-drive.dodois.dev/Authenticate/AuthorizeUnitRoute
        // with role=RootAdmin, unitId from previous request
        // get in response body
        //{
        //    "url": "https://ru-admin.dev-drive.dodois.dev/Infrastructure/Authenticate/Auth?Id=e9d8347 ...
        //}


        String url = given()
                .contentType("multipart/form-data")
                .cookies(cookies1)
                .multiPart("role", "RootAdmin")
                .multiPart("unitId", "000D3A240C719A8711E68ABA13F7F862")
                .log().all()
                .when()
                .post("/Authenticate/AuthorizeUnitRoute")
                .then()
                .log().all()
                .statusCode(200)
                .body("url", is(not(emptyOrNullString())))
                .extract().path("url")
                ;

        System.out.println("url= " + url);

        // GET with url from the previous request, get cookie2

        Map<String, String> cookies2 = given()
                .cookies(cookies1)
                .log().all()
                .when()
                .redirects().follow(false)
                .get(url)
                .then()
                .log().all()
                .statusCode(302)
                .extract().cookies()
                ;

        System.out.println("cookies2=" + cookies2);

        //add Organization with cookies2
        //unique nameShort, nameFull, id

        String addUnitJson = "{\n" +
                "    \"organizationTypeId\": \"0242AC110029A74D11EA09DBB39D6134\",\n" +
                "    \"shareCapital\": \"\",\n" +
                "    \"checkingAccount\": \"\",\n" +
                "    \"bankName\": \"\",\n" +
                "    \"headManagerName\": \"\",\n" +
                "    \"positionOfHead\": \"\",\n" +
                "    \"address\": \"\",\n" +   //check if necessary
                "    \"nameShort\": \"test_unit_short_39\",\n" +
                "    \"nameFull\": \"test_unit_39\",\n" +
                "    \"countryId\": 643,\n" +
                "    \"version\": 1,\n" +
                "    \"id\": \"11ed010ab63b9f398c851649fc7a2f39\",\n" +
                "    \"selectedOrganizationType\": \"Общество с ограниченной ответственностью\",\n" +
                "    \"requisites\": {\n" +
                "        \"INN\": \"0300473093\",\n" +
                "        \"OGRN\": \"\",\n" +
                "        \"OKPO\": \"\"\n" +
                "    }\n" +
                "}";
        given()
                .contentType("application/json")
                //header from original request, maybe could be removed
                //.header("authority", "ru-admin.dev-drive.dodois.dev")
                //.header("accept", "application/json")
                //.header("accept-language", "en-GB,en;q=0.9,ru-RU;q=0.8,ru;q=0.7,pl-PL;q=0.6,pl;q=0.5,de-DE;q=0.4,de;q=0.3,en-US;q=0.2")
                //.header("content-type", "application/json")
                //.header("origin", "https://ru-admin.dev-drive.dodois.dev")
                //.header("referer", "https://ru-admin.dev-drive.dodois.dev/Managment/Organizations")
                //.header("sec-ch-ua", "\".Not/A)Brand\";v=\"99\", \"Google Chrome\";v=\"103\", \"Chromium\";v=\"103\"")
                //.header("sec-ch-ua-mobile", "?0")
                //.header("sec-ch-ua-platform", "\"macOS\"")
                //.header("sec-fetch-dest", "empty")
                //.header("sec-fetch-mode", "cors")
                //.header("sec-fetch-site", "same-origin")
                //.header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36")
                .body(addUnitJson)
                .cookies(cookies2)
                .log().all()
                .when()
                .post("https://ru-admin.dev-drive.dodois.dev/DataCatalog/api/v1/Organizations")
                .then()
                .log().all()
                .statusCode(200)
                .body("success", is(true)) //Matchers.is (org.hamcrest)
                .body("errorCode", is(emptyOrNullString() ) )
                .body("errorDetails", is(emptyOrNullString() ) )
                .body("errors", empty() )
                ;

    }



}
