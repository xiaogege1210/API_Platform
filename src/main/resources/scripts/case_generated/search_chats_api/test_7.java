import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class test_7 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-fefjDWAaB9AHtrtQlz_7QsghjsGNlgMpgM0Gix400HEA";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    @DisplayName("异常用例_操作者不在群内")
    public void testNonMemberAccessChatMembers() {
        String invalidChatId = "oc_a0553eda9014c201e6969b478895c230";
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id");
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", invalidChatId);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + responseBody);
        
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 403 || statusCode == 400, 
            "状态码应为403或400，实际为: " + statusCode);
        
        response.then()
            .body("code", not(equalTo(0)))
            .body("msg", 
                anyOf(
                    containsString("权限"),
                    containsString("不在群内"),
                    containsString("无法访问"),
                    containsString("群组"),
                    containsString("access"),
                    containsString("permission")
                ));
    }
}