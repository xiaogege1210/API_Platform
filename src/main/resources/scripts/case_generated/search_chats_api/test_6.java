import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_6 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis";
    private static final String TENANT_ACCESS_TOKEN = "tenant_access_token";
    private static final String INVALID_CHAT_ID = "oc_invalid_chat_id_123";
    
    @Test
    public void testGetChatInfoWithInvalidChatId() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + TENANT_ACCESS_TOKEN)
            .header("Content-Type", "application/json");
        
        // 发送GET请求
        Response response = request
            .pathParam("chat_id", INVALID_CHAT_ID)
            .get("/im/v1/chats/{chat_id}");
        
        // 打印响应内容到控制台
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
        
        // 验证HTTP状态码为404
        assertEquals(404, response.getStatusCode(), 
            "状态码应为404");
        
        // 验证响应体包含错误码字段
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("\"code\"") || 
                  responseBody.contains("\"error\"") || 
                  responseBody.contains("\"err_code\""), 
            "响应体应包含错误码字段");
        
        // 验证错误提示包含"chat_id"或"未找到"相关信息
        String lowerCaseBody = responseBody.toLowerCase();
        assertTrue(lowerCaseBody.contains("chat_id") || 
                  lowerCaseBody.contains("未找到") || 
                  lowerCaseBody.contains("not found") || 
                  lowerCaseBody.contains("不存在") || 
                  lowerCaseBody.contains("invalid"),
            "错误提示应包含'chat_id'或'未找到'相关信息");
        
        // 验证响应体是有效的JSON
        assertNotNull(response.jsonPath(), "响应体应为有效的JSON格式");
        
        // 验证接口返回资源不存在错误
        assertTrue(response.getStatusCode() == 404, 
            "接口应返回资源不存在错误");
        
        // 验证错误码符合飞书API规范（通常为非0值）
        if (response.jsonPath().get("code") != null) {
            int errorCode = response.jsonPath().getInt("code");
            assertNotEquals(0, errorCode, "错误码应为非0值");
        } else if (response.jsonPath().get("error.code") != null) {
            int errorCode = response.jsonPath().getInt("error.code");
            assertNotEquals(0, errorCode, "错误码应为非0值");
        }
        
        // 验证错误信息明确提示群组不存在
        String errorMsg = "";
        if (response.jsonPath().get("msg") != null) {
            errorMsg = response.jsonPath().getString("msg");
        } else if (response.jsonPath().get("error.msg") != null) {
            errorMsg = response.jsonPath().getString("error.msg");
        } else if (response.jsonPath().get("message") != null) {
            errorMsg = response.jsonPath().getString("message");
        }
        
        assertFalse(errorMsg.isEmpty(), "错误信息不应为空");
        System.out.println("错误信息: " + errorMsg);
    }
}