import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_4 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "1";
    private static final String CHAT_ID = "1";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_123";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersWithInvalidChatId() {
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id")
            .pathParam("chat_id", INVALID_CHAT_ID);
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members");
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + responseBody);
        
        response.then()
            .statusCode(400)
            .body("code", not(equalTo(0)))
            .body("msg", containsStringIgnoringCase("chat_id"));
    }
}