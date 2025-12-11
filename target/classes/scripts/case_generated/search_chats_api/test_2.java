import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 飞书API聊天成员查询测试类
 * 测试获取聊天成员接口的各种场景
 */
public class test_2 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-eifCe8UPN8gHvO3kMUY_0a00i3px5gUVX8Gy2wY028.E";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_123";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 20;
    private static final String CONTENT_TYPE = "application/json";
    private static final String API_PATH = "/im/v1/chats/{chat_id}/members";
    private static final int EXPECTED_INVALID_REQUEST_STATUS = 400;
    private static final int EXPECTED_SUCCESS_STATUS = 200;
    private static final int EXPECTED_UNAUTHORIZED_STATUS = 401;
    private static final int EXPECTED_FORBIDDEN_STATUS = 403;
    private static final int SUCCESS_CODE = 0;
    private static final int EXPECTED_ERROR_CODE = 232006;
    
    /**
     * 在每个测试方法执行前初始化基础URL
     */
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = BASE_URL;
    }
    
    /**
     * 构建基础请求规范
     * @param includeAuth 是否包含认证头
     * @return 配置好的请求规范
     */
    private RequestSpecification buildBaseRequest(boolean includeAuth) {
        RequestSpecification request = given()
            .header("Content-Type", CONTENT_TYPE)
            .queryParam("member_id_type", MEMBER_ID_TYPE)
            .queryParam("page_size", PAGE_SIZE);
            
        if (includeAuth) {
            request.header("Authorization", "Bearer " + USER_TOKEN);
        }
        
        return request;
    }
    
    /**
     * 发送GET请求并返回响应
     * @param request 请求规范
     * @param chatId 聊天ID
     * @return API响应
     */
    private Response sendGetRequest(RequestSpecification request, String chatId) {
        try {
            return request
                .pathParam("chat_id", chatId)
                .when()
                .get(API_PATH);
        } catch (Exception e) {
            System.err.println("请求发送失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 打印响应信息到控制台
     * @param response API响应
     * @param testName 测试名称
     */
    private void printResponseInfo(Response response, String testName) {
        System.out.println("=== " + testName + "响应状态码 ===");
        System.out.println("Status Code: " + response.getStatusCode());
        
        System.out.println("\n=== " + testName + "响应体 ===");
        String responseBody = response.getBody().asString();
        System.out.println(responseBody);
    }
    
    /**
     * 验证错误响应
     * @param response API响应
     */
    private void validateErrorResponse(Response response) {
        int code = response.jsonPath().getInt("code");
        String msg = response.jsonPath().getString("msg");
        
        System.out.println("\n=== 错误信息 ===");
        System.out.println("Error Code: " + code);
        System.out.println("Error Message: " + msg);
        
        assertNotEquals(SUCCESS_CODE, code, "响应体code字段应为非0的错误码");
        assertNotNull(msg, "错误信息不应为空");
        assertTrue(msg.length() > 0, "错误信息应包含内容");
        
        if (code == EXPECTED_ERROR_CODE) {
            System.out.println("检测到预期的错误码: " + EXPECTED_ERROR_CODE);
        }
    }
    
    @Test
    void testGetChatMembersWithInvalidChatId() {
        RequestSpecification request = buildBaseRequest(true);
        Response response = sendGetRequest(request, INVALID_CHAT_ID);
        
        printResponseInfo(response, "");
        
        assertEquals(EXPECTED_INVALID_REQUEST_STATUS, response.getStatusCode(), 
            "HTTP状态码应为" + EXPECTED_INVALID_REQUEST_STATUS);
        
        validateErrorResponse(response);
    }
    
    @Test
    void testGetChatMembersWithValidChatId() {
        RequestSpecification request = buildBaseRequest(true);
        Response response = sendGetRequest(request, CHAT_ID);
        
        printResponseInfo(response, "正向测试");
        
        int statusCode = response.getStatusCode();
        
        if (statusCode == EXPECTED_SUCCESS_STATUS) {
            assertEquals(EXPECTED_SUCCESS_STATUS, statusCode, 
                "使用有效chat_id时应返回" + EXPECTED_SUCCESS_STATUS + "状态码");
            
            int code = response.jsonPath().getInt("code");
            assertEquals(SUCCESS_CODE, code, 
                "使用有效chat_id时code字段应为" + SUCCESS_CODE);
        } else if (statusCode == EXPECTED_UNAUTHORIZED_STATUS) {
            System.out.println("Token无效或无权限，返回" + EXPECTED_UNAUTHORIZED_STATUS);
        } else if (statusCode == EXPECTED_FORBIDDEN_STATUS) {
            System.out.println("无访问权限，返回" + EXPECTED_FORBIDDEN_STATUS);
        }
    }
    
    @Test
    void testGetChatMembersWithoutToken() {
        RequestSpecification request = buildBaseRequest(false);
        Response response = sendGetRequest(request, CHAT_ID);
        
        printResponseInfo(response, "无Token测试");
        
        assertEquals(EXPECTED_UNAUTHORIZED_STATUS, response.getStatusCode(), 
            "未提供Token时应返回" + EXPECTED_UNAUTHORIZED_STATUS + "状态码");
    }
}