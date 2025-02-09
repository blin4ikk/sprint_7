import io.qameta.allure.Issue;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import com.google.gson.Gson;
import io.restassured.response.Response;
import org.junit.*;
import java.util.Random;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class GetListOrderPositiveTest {
    @Before
    public void setup() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Запрос списка заказов без параметров")
    public void getOrders() {
        // Выполняем GET-запрос без параметров
        Response response = getOrdersRequest();

        // Проверка, что список заказов не пустой
        response.then().assertThat().body("orders", notNullValue());
        System.out.println("Список заказов: " + response.asString());
    }

    @Test
    @DisplayName("Запрос списка заказов с courierId")
    @Issue("В ответе с заказами отсутствует courierId, поэтому тест падает")
    public void getOrdersWithCourierId() {
        int courierId = 1; // Пример курьера с ID = 1
        // Выполняем GET-запрос с фильтром по courierId
        Response orderResponse = getOrdersRequestWithCourierId(courierId);

        // Проверка, что список заказов не пустой
        orderResponse.then().assertThat().body("orders", notNullValue());
        System.out.println("Заказы курьера с ID = " + courierId + ": " + orderResponse.asString());
    }

    @Test
    @DisplayName("Запрос списка заказов с stationNumber")
    public void getOrdersWithNearestStation() {
        int stationNumber = new Random().nextInt(271) + 1; // Пример номера станции метро
        // Выполняем GET-запрос с фильтром по nearestStation
        Response response = getOrdersRequestWithStationNumber(stationNumber);

        // Проверка, что список заказов не пустой
        response.then().assertThat().body("orders", notNullValue());
        System.out.println("Заказы рядом со станцией " + stationNumber + ": " + response.asString());
    }

    @Test
    @DisplayName("Запрос списка заказов с limit и page")
    public void getOrdersWithLimitAndPage() {
        int limit = new Random().nextInt(30) + 1;
        int page = 0; // Страница = 0 (по умолчанию)
        // Выполняем GET-запрос с фильтрами limit и page
        Response response = getOrdersRequestWithLimitAndPage(limit, page);

        // Проверка, что список заказов не пустой
        response.then().assertThat().body("orders", notNullValue());
        System.out.println("Заказы с фильтрами limit = " + limit + " и page = " + page + ": " + response.asString());
    }

    @Step("Получение всех заказов")
    private Response getOrdersRequest() {
        return given()
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200) // Статус 200 OK
                .extract().response();
    }

    @Step("Получение заказов с courierId = {courierId}")
    private Response getOrdersRequestWithCourierId(int courierId) {
        return given()
                .queryParam("courierId", courierId)
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200) // Статус 200 OK
                .extract().response();
    }

    @Step("Получение заказов с nearestStation = {stationNumber}")
    private Response getOrdersRequestWithStationNumber(int stationNumber) {
        return given()
                .queryParam("nearestStation", stationNumber)
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200) // Статус 200 OK
                .extract().response();
    }

    @Step("Получение заказов с limit = {limit} и page = {page}")
    private Response getOrdersRequestWithLimitAndPage(int limit, int page) {
        return given()
                .queryParam("limit", limit)
                .queryParam("page", page)
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200) // Статус 200 OK
                .extract().response();
    }
}
