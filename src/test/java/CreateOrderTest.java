import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.Order;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class CreateOrderTest {
    private final Gson gson = new Gson();
    private int trackNumber; // Track номер заказа
    private final String firstName;
    private final String lastName;
    private final String address;
    private final String metroStation;
    private final String phone;
    private final int rentTime;
    private final String deliveryDate;
    private final String comment;
    private final String[] color;

    // Конструктор для параметризации тестов
    public CreateOrderTest(String firstName, String lastName, String address, String metroStation,
                           String phone, int rentTime, String deliveryDateType, String comment, String colorStr) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.metroStation = metroStation;
        this.phone = phone;
        this.rentTime = rentTime;
        this.deliveryDate = generateDeliveryDate(deliveryDateType);
        this.comment = comment;
        this.color = colorStr.isEmpty() ? new String[]{} : colorStr.split(",");
    }

    @Step("Генерация даты доставки для типа: {dateType}")
    public static String generateDeliveryDate(String dateType) {
        LocalDate date;
        switch (dateType) {
            case "tomorrow":
                date = LocalDate.now().plusDays(1);
                break;
            case "next_week":
                date = LocalDate.now().plusWeeks(1);
                break;
            default: //"today"
                date = LocalDate.now();
                break;
        }
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Before
    public void setup() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru/";
    }

    @Parameterized.Parameters()
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Василий", "Иванов", "Адрес", "5", "+78003553535", 5, "today", "  ", "BLACK"},
                {"Иван", "Васильев", "Адрес", "4", "+71231231212", 3, "tomorrow", "Комментарий", "GRAY"},
                {"Пятнадцатьбуков", "ВполеФамилияНетОграниченияНаКоличествоСимволовНоЕстьОграничениеНаСимволы",
                        "Адрес пробелы символы -, цифры 10 ёЁ привет", "3", "+7 777 000 00 00", 7, "next_week",
                        "Тестовый комментарий", "BLACK,GRAY"},
                {"Бу", "Бу", "Адрес", "2", "+70000000000", 2, "today", "", ""}
        });
    }

    @Test
    @DisplayName("Успешное создание заказа")
    public void testCreateOrderWithDifferentData() {
        Order order = new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, color);

        Response response = createOrder(order);

        trackNumber = response.path("track");
        System.out.println("Заказ создан: " + trackNumber + ", дата доставки: " + deliveryDate);
    }

    @Step("Отправка запроса на создание заказа")
    private Response createOrder(Order order) {
        return given()
                .header("Content-Type", "application/json")
                .body(gson.toJson(order))
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201) // Ожидаем статус 201 (Created)
                .body("track", notNullValue()) // Проверяем, что поле track не пустое
                .extract().response();
    }
}
