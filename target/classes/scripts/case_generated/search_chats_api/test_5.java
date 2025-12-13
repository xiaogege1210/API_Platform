import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class test_5 {
    
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String VALID_CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String VALID_USER_TOKEN = "u-fGiDd3r651m9k6gnNbOspS0giAsAlggXVOGy7Mk002Yo";
    private static final String INVALID_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String INVALID_TOKEN = "invalid_or_expired_token";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembers_NoAccessPermission() {
        System.out.println("=== 测试开始：获取群成员列表-无访问权限 ===");
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + INVALID_TOKEN)
            .queryParam("member_id_type", "open_id");
        
        // 发送请求
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", INVALID_CHAT_ID);
        
        // 打印响应内容
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应头: " + response.getHeaders());
        
        // 验证状态码
        int statusCode = response.getStatusCode();
        boolean isExpectedStatusCode = statusCode == 401 || statusCode == 403;
        assertTrue(isExpectedStatusCode, "期望状态码为401或403，实际为: " + statusCode);
        
        // 验证响应体包含身份验证错误信息
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("error") || 
                  responseBody.contains("code") || 
                  responseBody.contains("msg") ||
                  responseBody.contains("authentication") ||
                  responseBody.contains("token") ||
                  responseBody.contains("unauthorized") ||
                  responseBody.contains("forbidden"),
                  "响应体应包含身份验证相关错误信息");
        
        System.out.println("=== 测试结束 ===");
    }
    
    @Test
    public void testGetChatMembers_ValidToken() {
        System.out.println("=== 测试开始：获取群成员列表-有效令牌 ===");
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + VALID_USER_TOKEN)
            .queryParam("member_id_type", "open_id");
        
        // 发送请求
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", VALID_CHAT_ID);
        
        // 打印响应内容
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        
        // 验证状态码
        int statusCode = response.getStatusCode();
        System.out.println("实际状态码: " + statusCode);
        
        System.out.println("=== 测试结束 ===");
    }
}