import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_3 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-dE9V3H07F4lWrOY.pxaxXWg4hcIR5gghjyGajMw00K2w";
    private static final String CONTENT_TYPE = "application/json";
    private static final String INVALID_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testGetChatMembersWithoutAuthorization() {
        String endpoint = "/im/v1/chats/{chat_id}/members";
        
        RequestSpecification request = given()
                .pathParam("chat_id", INVALID_CHAT_ID)
                .header("Content-Type", CONTENT_TYPE);
        
        Response response = request.get(endpoint);
        
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
        
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 401 || statusCode == 403, 
                "Expected HTTP status 401 or 403 but got: " + statusCode);
        
        if (statusCode == 200) {
            int code = response.path("code");
            assertNotEquals(0, code, "Expected non-zero error code but got: " + code);
            
            String msg = response.path("msg");
            assertNotNull(msg, "Error message should not be null");
            assertFalse(msg.isEmpty(), "Error message should not be empty");
        }
    }
}