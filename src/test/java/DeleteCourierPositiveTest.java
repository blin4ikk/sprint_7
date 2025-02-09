import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.example.Courier;
import org.example.LoginCourier;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class DeleteCourierPositiveTest {
    private String login;
    private String password;
    private String firstName;
    private int userId;
    Gson gson = new Gson();

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
        login = "user_" + RandomStringUtils.randomAlphanumeric(6);
        password = RandomStringUtils.randomNumeric(6);
        firstName = "Test_" + RandomStringUtils.randomAlphabetic(4);
    }

    @Test
    @DisplayName("Успешное удаление курьера")
    public void deleteCourier() {
        Courier courier = new Courier(login, password, firstName);

        // Создаем курьера
        Response createResponse = createCourier(courier);
        createResponse.then().statusCode(201).body("ok", equalTo(true));

        // Логинимся для получения userId
        LoginCourier loginCourier = new LoginCourier(login, password);
        loginCourier(loginCourier);

        // Удаляем курьера
        deleteCourier(userId);

        System.out.println("Курьер успешно удален с Id: " + userId);
    }

    @Step("Создание курьера")
    private Response createCourier(Courier courier) {
        return given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(courier))
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201) // Код ответа 201 (Created)
                .body("ok", equalTo(true)) // Проверяем, что ответ содержит "ok: true"
                .extract().response();
    }

    @Step("Логин курьера с логином: {login}, паролем: {password}")
    private void loginCourier(LoginCourier loginCourier) {
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginCourier))
                .when()
                .post("api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue()) // Проверка, что id возвращается
                .extract().response();

        userId = loginResponse.path("id");
        System.out.println("Успешный логин курьера, ID: " + userId);
    }

    @Step("Удаление курьера с ID: {id}")
    private void deleteCourier(int id) {
        given()
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true)); // Ожидаем успешное удаление

        System.out.println("Курьер с ID " + id + " успешно удален.");
    }
}
