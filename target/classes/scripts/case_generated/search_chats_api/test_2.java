import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class test_2 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_123";
    private static final String VALID_ACCESS_TOKEN = USER_TOKEN;
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersWithInvalidChatId() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + VALID_ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .pathParam("chat_id", INVALID_CHAT_ID)
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 20);
        
        // 发送GET请求
        Response response = request.get("/chats/{chat_id}/members");
        
        // 打印响应内容到控制台
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
        
        // 验证HTTP状态码为400
        assertEquals(400, response.getStatusCode(), 
            "响应状态码应为400");
        
        // 验证响应体包含code字段
        String responseBody = response.getBody().asString();
        assertNotNull(responseBody, "响应体不应为空");
        
        // 验证响应体code字段为非0的错误码
        int code = response.jsonPath().getInt("code");
        assertEquals(232006, code, 
            "响应体code字段应为错误码232006");
        
        // 验证响应体msg字段包含错误描述信息
        String msg = response.jsonPath().getString("msg");
        assertNotNull(msg, "响应体msg字段不应为空");
        
        // 验证错误信息应明确指示chat_id无效
        String errorMessage = response.jsonPath().getString("msg").toLowerCase();
        boolean containsChatIdError = errorMessage.contains("chat") || 
                                     errorMessage.contains("群") || 
                                     errorMessage.contains("invalid");
        assertEquals(true, containsChatIdError, 
            "错误信息应明确指示chat_id无效");
    }
}