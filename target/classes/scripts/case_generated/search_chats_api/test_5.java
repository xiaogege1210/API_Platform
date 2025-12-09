import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_5 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String TENANT_ACCESS_TOKEN = "tenant_access_token";
    private static final String OPEN_ID = "ou_1234567890abcdef";
    
    @Test
    public void testSendMessageWithInvalidContentFormat() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求体（非JSON格式的content字段）
        String invalidRequestBody = "{" +
            "\"receive_id\": \"" + OPEN_ID + "\"," +
            "\"msg_type\": \"text\"," +
            "\"content\": \"这不是一个JSON字符串\"" +
            "}";
        
        // 发送POST请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + TENANT_ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("receive_id_type", "open_id")
            .body(invalidRequestBody);
        
        Response response = request.post("/im/v1/messages");
        
        // 打印响应内容到控制台
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应内容: " + response.getBody().asString());
        System.out.println("响应头: " + response.getHeaders());
        
        // 验证HTTP状态码为400
        assertEquals(400, response.getStatusCode(), "状态码应为400");
        
        // 验证响应体包含错误码字段
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("\"code\"") || 
                  responseBody.contains("code") || 
                  responseBody.contains("error"), "响应体应包含错误码字段");
        
        // 验证错误提示包含"content"或"JSON格式"相关信息
        String lowerCaseBody = responseBody.toLowerCase();
        assertTrue(lowerCaseBody.contains("content") || 
                  lowerCaseBody.contains("json") || 
                  lowerCaseBody.contains("格式") || 
                  lowerCaseBody.contains("parse") ||
                  lowerCaseBody.contains("invalid"),
                  "错误提示应包含content或JSON格式相关信息");
    }
}