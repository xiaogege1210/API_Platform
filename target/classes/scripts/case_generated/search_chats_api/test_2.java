import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class test_2 {
    
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "invalid_chat_id_12345";
    private static final String USER_TOKEN = "1";
    private static final String VALID_ACCESS_TOKEN = "<valid_access_token>";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembersWithInvalidChatId() {
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + VALID_ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id");
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", CHAT_ID);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        
        int statusCode = response.getStatusCode();
        System.out.println("HTTP Status Code: " + statusCode);
        
        assertEquals(400, statusCode, "HTTP状态码应为400");
        
        int code = response.path("code");
        String msg = response.path("msg");
        
        System.out.println("Response code: " + code);
        System.out.println("Response msg: " + msg);
        
        assertNotNull(code, "响应体code字段不应为空");
        assertNotNull(msg, "响应体msg字段不应为空");
        
        assertEquals(232006, code, "响应体code字段应为232006");
        
        assertNotNull(msg, "响应体msg字段不应为空");
    }
}