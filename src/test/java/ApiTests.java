import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ApiTests {
    private static final int unexistingPetId = 234453452;
    private static final int existingPetId = 4438409;
    private static final String existingName = "DmKolfild";
    private static final String existingStatus = "available";
    private static final String newName = "NewVersionOfYou";
    private static final String newStatus = "pending";

    @BeforeEach
    public void setUp() {
        // создание baseURI
        RestAssured.baseURI = "https://petstore.swagger.io/v2/";

        // удаление питомца из БД с ID = unexistingPetId
        given().when()
                .delete(baseURI + "pet/{petId}", String.valueOf(unexistingPetId))
                .then()
                .log().all()
                .assertThat();

        // создание питомца в БД с ID = existingPetId
        Map<String, String> request2 = new HashMap<>();
        request2.put("id", String.valueOf(existingPetId));

        given().contentType("application/json")
                .body(request2)
                .when()
                .post(baseURI + "pet/")
                .then()
                .log().all()
                .assertThat();
    }

    @Test
    @DisplayName("Поиск несуществующего питомца")
    public void petNotFoundTestBdd() {
        given().when()
                .get(baseURI + "pet/{petId}", unexistingPetId)
                .then()
                .log().all()
                .statusCode(404)
                .statusLine("HTTP/1.1 404 Not Found")
                .body("type", equalTo("error"),
                        "message", equalTo("Pet not found"));
    }

    @Test
    @DisplayName("Добавление питомца")
    public void addNewPetTestBdd() {
        Map<String, String> request = new HashMap<>();
        request.put("id", String.valueOf(unexistingPetId));
        request.put("name", existingName);
        request.put("status", existingStatus);

        given().contentType("application/json")
                .body(request)
                .when()
                .post(baseURI + "pet/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(unexistingPetId),
                        "name", equalTo(existingName),
                        "status", equalTo(existingStatus));
    }

    @Test
    @DisplayName("Удаление существующего питомца")
    public void deletePetTest() {
        given().when()
                .delete(baseURI + "pet/{petId}", existingPetId)
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .statusLine("HTTP/1.1 200 OK")
                .body("code", equalTo(200),
                        "type", equalTo("unknown"),
                        "message", equalTo(String.valueOf(existingPetId)));
    }

    @Test
    @DisplayName("Обновление статуса существующего питомца")
    public void putPetTest() {
        Map<String, String> request = new HashMap<>();
        request.put("id", String.valueOf(existingPetId));
        request.put("name", newName);
        request.put("status", newStatus);

        given().contentType("application/json")
                .body(request)
                .when()
                .put(baseURI + "pet/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .statusLine("HTTP/1.1 200 OK")
                .body("id", equalTo(existingPetId),
                        "name", equalTo(newName),
                        "status", equalTo(newStatus));
    }
}
