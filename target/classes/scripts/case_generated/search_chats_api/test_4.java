import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class test_4 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String TENANT_ACCESS_TOKEN = "tenant_access_token";
    private static final String RECEIVE_ID = "ou_1234567890abcdef";
    private static final String MSG_TYPE = "text";
    private static final String CONTENT = "{\"text\":\"测试消息\"}";
    
    @Test
    public void testSendMessageMissingRequiredParameter() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求体
        String requestBody = String.format(
            "{\"receive_id\":\"%s\",\"msg_type\":\"%s\",\"content\":\"%s\"}",
            RECEIVE_ID, MSG_TYPE, CONTENT
        );
        
        // 发送POST请求
        Response response = given()
            .header("Authorization", "Bearer " + TENANT_ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .body(requestBody)
            .when()
            .post("/im/v1/messages")
            .then()
            .extract().response();
        
        // 打印响应内容到控制台
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应头: " + response.getHeaders());
        
        // 验证HTTP状态码为400
        assertEquals(400, response.getStatusCode(), 
            "状态码应为400，但实际为: " + response.getStatusCode());
        
        // 验证响应体包含错误码字段
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("\"code\"") || responseBody.contains("code"), 
            "响应体应包含错误码字段");
        
        // 验证错误提示包含相关信息
        String lowerCaseBody = responseBody.toLowerCase();
        boolean containsReceiveIdType = lowerCaseBody.contains("receive_id_type");
        boolean containsMissingParam = lowerCaseBody.contains("缺少必要参数") || 
                                      lowerCaseBody.contains("missing") || 
                                      lowerCaseBody.contains("required");
        
        assertTrue(containsReceiveIdType || containsMissingParam, 
            "错误提示应包含'receive_id_type'或'缺少必要参数'相关信息。实际响应: " + responseBody);
    }
}