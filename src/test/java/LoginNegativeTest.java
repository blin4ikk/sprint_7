import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.Courier;
import org.example.LoginCourier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginNegativeTest {
    private String login;
    private String password;
    private String firstName;
    private int userId;
    Gson gson = new Gson();

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
        login = generateRandomLogin();
        password = generateRandomPassword();
        firstName = generateRandomFirstName();
        createCourier(login, password, firstName); // Создание курьера
    }

    @Test
    @DisplayName("Неуспешный логин с некорректным паролем")
    public void uncorrectLogin() {
        LoginCourier loginCourier = new LoginCourier(login, login); // Логин с некорректным паролем
        loginCourier(loginCourier, 404, "message", "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Неуспешный логин с несуществующим логином")
    public void nonExistentCourier() {
        LoginCourier loginCourier = new LoginCourier(password, login); // Логин с несуществующим логином
        loginCourier(loginCourier, 404, "message", "Учетная запись не найдена");
    }

    @Test
    @DisplayName("Неуспешный логин без заполнения логина")
    public void missingLoginCourier() {
        LoginCourier loginCourier = new LoginCourier(null, password); // Логин без логина
        loginCourier(loginCourier, 400, "message", "Недостаточно данных для входа");
    }

    @Test
    @DisplayName("Неуспешный логин без заполнения пароля")
    public void missingPasswordCourier() {
        LoginCourier loginCourier = new LoginCourier(login, null); // Логин без пароля
        loginCourier(loginCourier, 400, "message", "Недостаточно данных для входа");
    }

    @After
    public void cleanup() {
        loginCourier(new LoginCourier(login, password)); // Логин для получения ID
        deleteCourier(userId); // Удаление курьера
    }

    @Step("Создание курьера")
    private void createCourier(String login, String password, String firstName) {
        Courier courier = new Courier(login, password, firstName);
        Response response = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(courier))
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true))
                .extract().response();
        System.out.println("Создан курьер с: " + response.asString());
    }

    @Step("Логин курьера с логином: {loginCourier}")
    private void loginCourier(LoginCourier loginCourier, int expectedStatusCode, String expectedMessageKey, String expectedMessageValue) {
        Response response = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginCourier))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(expectedStatusCode)
                .body(expectedMessageKey, equalTo(expectedMessageValue))
                .extract().response();
        System.out.println("Результат логина: " + response.asString());
    }

    @Step("Логин курьера с логином: {loginCourier}")
    private void loginCourier(LoginCourier loginCourier) {
        Response response = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginCourier))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract().response();
        userId = response.body().path("id");
        System.out.println("Успешно залогинился: ID " + userId);
    }

    @Step("Удаление курьера с ID: {id}")
    private void deleteCourier(int id) {
        given()
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
        System.out.println("Курьер удален: ID " + id);
    }

    @Step("Генерация случайного логина")
    private String generateRandomLogin() {
        return "user_" + RandomStringUtils.randomAlphanumeric(6);
    }

    @Step("Генерация случайного пароля")
    private String generateRandomPassword() {
        return RandomStringUtils.randomNumeric(6);
    }

    @Step("Генерация случайного имени")
    private String generateRandomFirstName() {
        return "Test_" + RandomStringUtils.randomAlphabetic(4);
    }
}
