import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ApiTests {
    private static final int UNEXISTING_PET_ID = 234453452;
    private static final int EXISTING_PET_ID = 4438409;
    private static final String EXISTING_NAME = "DmKolfild";
    private static final String EXISTING_STATUS = "available";
    private static final String NEW_NAME = "NewVersionOfYou";
    private static final String NEW_STATUS = "pending";

    private static void sendRequestCreatePet(int petId) {
        Map<String, String> bodyForCreatePet = new HashMap<>();
        bodyForCreatePet.put("id", String.valueOf(petId));
        given().contentType(ContentType.JSON)
                .body(bodyForCreatePet)
                .when()
                .post(baseURI + "pet/")
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK);
    }

    private static void sendRequestDeletePet(int petId) {
        given().when()
                .delete(baseURI + "pet/{petId}", String.valueOf(petId))
                .then()
                .log().all();
    }

    @BeforeEach
    public void setUp() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2/";
    }

    @Test
    @DisplayName("Поиск несуществующего питомца")
    public void petNotFoundTestBdd() {
        sendRequestDeletePet(UNEXISTING_PET_ID);

        given().when()
                .get(baseURI + "pet/{petId}", UNEXISTING_PET_ID)
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
        sendRequestDeletePet(UNEXISTING_PET_ID);

        Map<String, String> bodyForAddNewPet = new HashMap<>();
        bodyForAddNewPet.put("id", String.valueOf(UNEXISTING_PET_ID));
        bodyForAddNewPet.put("name", EXISTING_NAME);
        bodyForAddNewPet.put("status", EXISTING_STATUS);

        given().contentType("application/json")
                .body(bodyForAddNewPet)
                .when()
                .post(baseURI + "pet/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .body("id", equalTo(UNEXISTING_PET_ID),
                        "name", equalTo(EXISTING_NAME),
                        "status", equalTo(EXISTING_STATUS));
    }

    @Test
    @DisplayName("Удаление существующего питомца")
    public void deletePetTest() {
        sendRequestCreatePet(EXISTING_PET_ID);

        given().when()
                .delete(baseURI + "pet/{petId}", EXISTING_PET_ID)
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .statusLine("HTTP/1.1 200 OK")
                .body("code", equalTo(200),
                        "type", equalTo("unknown"),
                        "message", equalTo(String.valueOf(EXISTING_PET_ID)));
    }

    @Test
    @DisplayName("Обновление статуса существующего питомца")
    public void putPetTest() {
        sendRequestCreatePet(EXISTING_PET_ID);

        Map<String, String> bodyForPutPet = new HashMap<>();
        bodyForPutPet.put("id", String.valueOf(EXISTING_PET_ID));
        bodyForPutPet.put("name", NEW_NAME);
        bodyForPutPet.put("status", NEW_STATUS);

        given().contentType("application/json")
                .body(bodyForPutPet)
                .when()
                .put(baseURI + "pet/")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .statusLine("HTTP/1.1 200 OK")
                .body("id", equalTo(EXISTING_PET_ID),
                        "name", equalTo(NEW_NAME),
                        "status", equalTo(NEW_STATUS));
    }
}
