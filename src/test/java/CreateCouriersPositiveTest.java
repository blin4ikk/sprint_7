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
import static org.hamcrest.Matchers.*;

public class CreateCouriersPositiveTest {
    private String login;
    private String password;
    private String firstName;
    private int userId;
    private final Gson gson = new Gson();

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
        login = generateRandomLogin();
        password = generateRandomPassword();
        firstName = generateRandomFirstName();
    }

    @Test
    @DisplayName("Успешное создание курьера")
    public void createNewCourier() {
        Courier courier = new Courier(login, password, firstName);

        // Создаем курьера
        Response response = createCourier(courier);
        response.then().statusCode(201).body("ok", equalTo(true));

        System.out.println("Курьер успешно создан: " + response.asString());
    }

    @After
    public void cleanup() {
        // Выполняем логин для получения ID
        userId = loginCourierAndGetId(login, password);

        // Удаляем курьера
        deleteCourier(userId);
        System.out.println("Курьер удален: ID " + userId);
    }

    @Step("Создание курьера")
    private Response createCourier(Courier courier) {
        return given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(courier))
                .when()
                .post("/api/v1/courier")
                .then()
                .extract().response();
    }

    @Step("Логин курьера с логином: {login}, паролем: {password}")
    private int loginCourierAndGetId(String login, String password) {
        LoginCourier loginCourier = new LoginCourier(login, password);
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginCourier))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .extract().response();
        return loginResponse.body().path("id");
    }

    @Step("Удаление курьера с ID: {id}")
    private void deleteCourier(int id) {
        given()
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
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
