import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class test_3 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String INVALID_TOKEN = "Bearer invalid_or_missing_token";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembersWithoutValidToken() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", INVALID_TOKEN)
            .pathParam("chat_id", CHAT_ID);
        
        // 发送请求
        Response response = request.when()
            .get("/chats/{chat_id}/members");
        
        // 打印响应内容到控制台
        System.out.println("=== 接口响应内容 ===");
        System.out.println("HTTP状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("=== 响应结束 ===\n");
        
        // 验证HTTP状态码为401或403
        int statusCode = response.getStatusCode();
        boolean isExpectedStatusCode = statusCode == 401 || statusCode == 403;
        assertEquals(true, isExpectedStatusCode, 
            String.format("HTTP状态码应为401或403，实际为: %d", statusCode));
        
        // 验证响应体中的code字段为非0的错误码
        int code = response.jsonPath().getInt("code");
        assertNotEquals(0, code, "响应体code字段应为非0错误码");
        
        // 验证响应体中的msg字段表明身份验证或授权失败
        String msg = response.jsonPath().getString("msg");
        boolean isAuthError = msg != null && 
            (msg.toLowerCase().contains("auth") || 
             msg.toLowerCase().contains("token") || 
             msg.toLowerCase().contains("unauthorized") || 
             msg.toLowerCase().contains("forbidden") ||
             msg.toLowerCase().contains("权限") ||
             msg.toLowerCase().contains("认证"));
        
        assertEquals(true, isAuthError, 
            String.format("响应体msg字段应表明身份验证失败，实际为: %s", msg));
    }
}