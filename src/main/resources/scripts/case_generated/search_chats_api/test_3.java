import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_3 {

    // 用户信息常量
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-eifCe8UPN8gHvO3kMUY_0a00i3px5gUVX8Gy2wY028.E";
    
    // 测试用例数据常量
    private static final String INVALID_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String INVALID_TOKEN = "Bearer invalid_or_expired_token";
    private static final String BASE_URL = "https://open.feishu.cn/open-apis";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 20;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testGetChatMembersWithInvalidToken() {
        // 构建请求
        RequestSpecification request = given()
                .header("Authorization", INVALID_TOKEN)
                .header("Content-Type", "application/json")
                .queryParam("member_id_type", MEMBER_ID_TYPE)
                .queryParam("page_size", PAGE_SIZE);

        // 发送GET请求
        Response response = request
                .when()
                .get("/im/v1/chats/{chat_id}/members", INVALID_CHAT_ID);

        // 打印响应内容到控制台
        System.out.println("=== 响应状态码 ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("=== 响应头 ===");
        System.out.println(response.getHeaders());
        System.out.println("=== 响应体 ===");
        String responseBody = response.getBody().asString();
        System.out.println(responseBody);
        System.out.println("=== 响应结束 ===\n");

        // 验证HTTP状态码
        assertEquals(401, response.getStatusCode(), 
                "HTTP状态码应为401");

        // 验证响应体中的关键字段
        if (responseBody != null && !responseBody.isEmpty()) {
            // 验证code字段为非0的错误码
            int code = response.path("code");
            assertNotEquals(0, code, 
                    "响应体code字段应为非0的错误码");
            
            // 验证msg字段包含身份验证相关的错误描述
            String msg = response.path("msg");
            assertNotNull(msg, 
                    "响应体msg字段不应为空");
            
            // 检查msg是否包含身份验证相关的关键词
            String lowerCaseMsg = msg.toLowerCase();
            boolean hasAuthError = lowerCaseMsg.contains("auth") || 
                                  lowerCaseMsg.contains("token") || 
                                  lowerCaseMsg.contains("invalid") || 
                                  lowerCaseMsg.contains("expired") ||
                                  lowerCaseMsg.contains("unauthorized");
            assertTrue(hasAuthError, 
                    "响应体msg字段应包含身份验证相关的错误描述信息，实际msg: " + msg);
        }
    }
}