import com.google.gson.Gson;
import io.qameta.allure.Issue;
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

public class CreateCouriersNegativeTest {
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
    @DisplayName("Создание дублирующего курьера")
    public void createDuplicateCourier() {
        Courier courier = new Courier(login, password, firstName);

        // Создаем курьера
        createCourier(courier);

        // Логинимся, чтобы получить userId
        userId = loginCourierAndGetId(login, password);

        // Пытаемся создать дубликат
        Response duplicateResponse = createCourier(courier);
        duplicateResponse.then().statusCode(409)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));

        System.out.println("Попытка создать дубликат: " + duplicateResponse.asString());
    }

    @Test
    @DisplayName("Создание курьера без логина")
    public void missingLoginCreateCourier() {
        Courier courier = new Courier(null, password, firstName);
        Response response = createCourier(courier);

        response.then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));

        System.out.println("Попытка создать курьера без логина: " + response.asString());
    }

    @Test
    @DisplayName("Создание курьера без пароля")
    public void missingPasswordCreateCourier() {
        Courier courier = new Courier(login, null, firstName);
        Response response = createCourier(courier);

        response.then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));

        System.out.println("Попытка создать курьера без пароля: " + response.asString());
    }

    @Test
    @DisplayName("Создание курьера без firstName")
    @Issue("создается курьер без firstName, поэтому добавлен логин для последующего удаления")
    public void missingFirstNameCreateCourier() {
        Courier courier = new Courier(login, password, null);
        Response response = createCourier(courier);

        response.then().statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));

        System.out.println("Попытка создать курьера без firstName: " + response.asString());

        // Логинимся для удаления, так как здесь баг - создается курьер без firstName
        userId = loginCourierAndGetId(login, password);
    }

    @After
    public void cleanup() {
        if (userId > 0) {
            deleteCourier(userId);
            System.out.println("Курьер удален: ID " + userId);
        }
    }

    @Step("Создание курьера с логином: {login}, паролем: {password}, именем: {firstName}")
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
        return loginResponse.path("id");
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