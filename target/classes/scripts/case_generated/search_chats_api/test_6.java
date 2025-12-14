import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_6 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    private static final String INVALID_TOKEN = "invalid_or_expired_token";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testGetChatMembersWithInvalidToken() {
        RequestSpecification request = given()
                .header("Authorization", "Bearer " + INVALID_TOKEN)
                .header("Content-Type", "application/json")
                .pathParam("chat_id", CHAT_ID)
                .queryParam("member_id_type", "open_id");

        Response response = request.when()
                .get("/chats/{chat_id}/members");

        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        System.out.println("Status Code: " + response.getStatusCode());

        response.then()
                .statusCode(401)
                .body("code", not(equalTo(0)))
                .body("msg", anyOf(
                        containsStringIgnoringCase("Unauthorized"),
                        containsStringIgnoringCase("token"),
                        containsStringIgnoringCase("invalid"),
                        containsStringIgnoringCase("认证失败")
                ));
    }
}