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

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginPositiveTest {
    private String login;
    private String password;
    private String firstName;
    private int userId; // userId теперь будет получен только после логина

    Gson gson = new Gson();

    @Before
    public void setUp() {
        baseURI = "http://qa-scooter.praktikum-services.ru/";
        login = generateRandomLogin();
        password = generateRandomPassword();
        firstName = generateRandomFirstName();
    }

    @Test
    @DisplayName("Успешный логин курьера")
    public void loginCourierTest() {
        // Создаем курьера
        Courier courier = new Courier(login, password, firstName);
        Response createResponse = createCourier(courier);

        // После успешного создания курьера, логинимся
        LoginCourier loginCourier = new LoginCourier(login, password);
        loginCourier(loginCourier);
    }

    @After
    public void cleanup() {
        if (userId > 0) {
            deleteCourier(userId);
        }
    }

    @Step("Создание курьера")
    private Response createCourier(Courier courier) {
        Response response = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(courier))
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true))
                .extract().response();
        return response;
    }

    @Step("Логин курьера с логином: {loginCourier}")
    private void loginCourier(LoginCourier loginCourier) {
        // Выполняем логин
        Response response = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginCourier))
                .when()
                .post("api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue()) // Проверка, что ID существует
                .extract().response();

        userId = response.body().path("id");
        System.out.println("Успешно залогинился курьер с ID: " + userId);
    }

    @Step("Удаление курьера: ID {id}")
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
