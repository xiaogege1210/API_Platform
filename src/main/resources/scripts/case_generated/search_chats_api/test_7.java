import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_7 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String INVALID_TOKEN = "Bearer invalid_or_expired_token";
    private static final String CHAT_ID = "oc_1234567890abcdef";
    
    @Test
    public void testGetChatInfoWithInvalidToken() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", INVALID_TOKEN)
            .pathParam("chat_id", CHAT_ID);
        
        // 发送GET请求
        Response response = request.get("/open-apis/im/v1/chats/{chat_id}");
        
        // 打印响应内容到控制台
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应内容: ");
        response.prettyPrint();
        
        // 获取响应状态码
        int statusCode = response.getStatusCode();
        
        // 验证状态码为401或403
        assertTrue(statusCode == 401 || statusCode == 403, 
            "期望状态码为401或403，实际为: " + statusCode);
        
        // 验证响应体包含错误码字段
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("\"code\"") || 
                  responseBody.contains("\"error\"") ||
                  responseBody.contains("\"err_code\""),
            "响应体应包含错误码字段");
        
        // 验证错误提示包含相关关键词
        String lowerCaseBody = responseBody.toLowerCase();
        boolean containsTokenKeyword = lowerCaseBody.contains("token") || 
                                      lowerCaseBody.contains("认证") || 
                                      lowerCaseBody.contains("权限");
        assertTrue(containsTokenKeyword, 
            "错误提示应包含'token'、'认证'或'权限'相关信息");
        
        // 验证错误码符合飞书API规范
        if (responseBody.contains("\"code\"")) {
            int codeValue = response.path("code");
            assertNotEquals(0, codeValue, "错误码不应为0");
        }
        
        System.out.println("测试通过：接口返回了预期的认证或权限错误");
    }
}