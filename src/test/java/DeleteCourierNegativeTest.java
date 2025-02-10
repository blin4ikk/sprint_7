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

public class DeleteCourierNegativeTest {
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

        // Создание курьера
        Courier courier = new Courier(login, password, firstName);
        createCourier(courier);

        // Логин для получения ID
        LoginCourier loginCourier = new LoginCourier(login, password);
        loginCourier(loginCourier);
    }

    @Test
    @DisplayName("Неуспешное удаление без courierId")
    public void failedDeleteCourier() {
        // Пытаемся удалить без указания ID
        Response response = deleteCourierWithoutId();
        response.then().statusCode(404)
                .body("message", equalTo("Not Found."));
        System.out.println("Попытка удалить без ID: " + response.asString());
    }

    @Test
    @DisplayName("Неуспешное удаление с несуществующим courierId")
    public void deleteNonExistentCourier() {
        int invalidUserId = 123; // Некорректный ID
        Response response = deleteCourierById(invalidUserId);

        response.then().statusCode(404)
                .body("message", equalTo("Курьера с таким id нет."));
        System.out.println("Курьера нет с ID: " + invalidUserId + " " + response.asString());
    }

    @After
    public void cleanup() {
        deleteCourier(userId); // Удаляем созданного курьера
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
                .statusCode(201)
                .body("ok", equalTo(true))
                .extract().response();
    }

    @Step("Логин курьера с логином: {login}, паролем: {password}")
    private void loginCourier(LoginCourier loginCourier) {
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(loginCourier))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue()) // Проверка на наличие ID
                .extract().response();
        userId = loginResponse.path("id");
        System.out.println("Курьер успешно залогинился, ID: " + userId);
    }

    @Step("Попытка удаления курьера без ID")
    private Response deleteCourierWithoutId() {
        return given()
                .header("Content-Type", "application/json")
                .when()
                .delete("/api/v1/courier/")
                .then()
                .extract().response();
    }

    @Step("Попытка удаления курьера с ID: {id}")
    private Response deleteCourierById(int id) {
        return given()
                .header("Content-Type", "application/json")
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .extract().response();
    }

    @Step("Удаление курьера с ID: {id}")
    private void deleteCourier(int id) {
        given()
                .when()
                .delete("/api/v1/courier/" + id)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
        System.out.println("Курьер с ID " + id + " успешно удален.");
    }

}
