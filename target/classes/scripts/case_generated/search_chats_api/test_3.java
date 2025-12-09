import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_3 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String TENANT_ACCESS_TOKEN = "tenant_access_token";
    private static final String RECEIVE_ID = "ou_1234567890abcdef";
    private static final String INVALID_RECEIVE_ID_TYPE = "invalid_type";
    private static final String MSG_TYPE = "text";
    private static final String CONTENT_TEXT = "测试消息";
    
    @Test
    public void testSendMessageWithInvalidReceiveIdType() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求体JSON
        String requestBody = String.format(
            "{\"receive_id\":\"%s\",\"msg_type\":\"%s\",\"content\":\"{\\\"text\\\":\\\"%s\\\"}\"}",
            RECEIVE_ID, MSG_TYPE, CONTENT_TEXT
        );
        
        // 发送POST请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + TENANT_ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("receive_id_type", INVALID_RECEIVE_ID_TYPE)
            .body(requestBody);
        
        Response response = request.post("/im/v1/messages");
        
        // 打印响应内容到控制台
        System.out.println("=== 接口响应 ===");
        System.out.println("状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应头: " + response.getHeaders());
        System.out.println("=== 响应结束 ===\n");
        
        // 验证HTTP状态码为400
        assertEquals(400, response.getStatusCode(), 
            "HTTP状态码应为400");
        
        // 验证响应体包含错误码字段
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("\"code\""), 
            "响应体应包含错误码字段");
        
        // 验证错误提示包含相关关键词
        assertTrue(responseBody.toLowerCase().contains("receive_id_type") || 
                  responseBody.contains("参数错误") ||
                  responseBody.toLowerCase().contains("invalid parameter"),
            "错误提示应包含'receive_id_type'或'参数错误'相关信息");
        
        // 验证响应体是有效的JSON
        assertNotNull(response.jsonPath().get("code"), 
            "响应体应包含有效的错误码");
    }
}