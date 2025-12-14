import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_5 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-fefjDWAaB9AHtrtQlz_7QsghjsGNlgMpgM0Gix400HEA";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testMissingAuthorizationToken() {
        String testChatId = "oc_a0553eda9014c201e6969b478895c230";
        
        RequestSpecification request = given()
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id");
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", testChatId);
        
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
        
        response.then()
            .statusCode(401)
            .body("code", notNullValue())
            .body("msg", notNullValue());
    }
}