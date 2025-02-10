import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class GetListOrderNegativeTest {
    @Before
    public void setup() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
    }

    @Test
    @DisplayName("Запрос списка заказов с несуществующим courierId")
    public void getOrdersWithNonExistentCourierId() {
        int invalidCourierId = 9999; // Не существующий идентификатор курьера

        // Выполнение запроса с несуществующим courierId
        Response response = getOrdersWithInvalidCourierId(invalidCourierId);

        // Проверка статуса и тела ответа
        response.then().statusCode(404) // Ожидаем 404 Not Found
                .body("message", equalTo("Курьер с идентификатором " + invalidCourierId + " не найден"));

        System.out.println("Курьер с идентификатором " + invalidCourierId + " не найден" + " " + response.asString());
    }

    @Step("Выполнение запроса для получения списка заказов с несуществующим courierId = {invalidCourierId}")
    private Response getOrdersWithInvalidCourierId(int invalidCourierId) {
        return given()
                .queryParam("courierId", invalidCourierId)
                .when()
                .get("/api/v1/orders")
                .then()
                .extract().response();
    }
}
