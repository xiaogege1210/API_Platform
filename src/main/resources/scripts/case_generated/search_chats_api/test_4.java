import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class test_4 {
    
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "Bearer u-eifCe8UPN8gHvO3kMUY_0a00i3px5gUVX8Gy2wY028.E";
    private static final String INVALID_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    
    // 请求头常量
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    
    // 查询参数常量
    private static final String MEMBER_ID_TYPE_PARAM = "member_id_type";
    private static final String OPEN_ID_VALUE = "open_id";
    private static final String PAGE_SIZE_PARAM = "page_size";
    
    // 路径参数常量
    private static final String CHAT_ID_PATH_PARAM = "chat_id";
    private static final String API_PATH = "/im/v1/chats/{chat_id}/members";
    
    // 响应字段常量
    private static final String CODE_FIELD = "code";
    private static final String MSG_FIELD = "msg";
    
    @Test
    public void testGetChatMembersWithExcessivePageSize() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求
        RequestSpecification request = buildRequest();
        
        // 发送GET请求
        Response response = sendGetRequest(request);
        
        // 打印响应内容到控制台
        printResponseDetails(response);
        
        // 验证HTTP状态码
        validateHttpStatusCode(response);
        
        // 验证响应体中的code字段为非0的错误码
        int code = extractCodeFromResponse(response);
        validateErrorCode(code);
        
        // 验证响应体中的msg字段存在（不为空）
        String msg = extractMessageFromResponse(response);
        validateErrorMessage(msg);
        
        // 打印验证结果
        printValidationResults(response, code, msg);
    }
    
    private RequestSpecification buildRequest() {
        return given()
            .header(AUTHORIZATION_HEADER, USER_TOKEN)
            .header(CONTENT_TYPE_HEADER, APPLICATION_JSON)
            .queryParam(MEMBER_ID_TYPE_PARAM, OPEN_ID_VALUE)
            .queryParam(PAGE_SIZE_PARAM, 150);
    }
    
    private Response sendGetRequest(RequestSpecification request) {
        return request
            .pathParam(CHAT_ID_PATH_PARAM, INVALID_CHAT_ID)
            .when()
            .get(API_PATH);
    }
    
    private void printResponseDetails(Response response) {
        System.out.println("=== 响应状态码 ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("\n=== 响应头 ===");
        System.out.println(response.getHeaders());
        System.out.println("\n=== 响应体 ===");
        String responseBody = response.getBody().asString();
        System.out.println(responseBody);
    }
    
    private void validateHttpStatusCode(Response response) {
        int statusCode = response.getStatusCode();
        assertEquals(400, statusCode, 
            "HTTP状态码应为400，但实际为: " + statusCode);
    }
    
    private int extractCodeFromResponse(Response response) {
        return response.jsonPath().getInt(CODE_FIELD);
    }
    
    private void validateErrorCode(int code) {
        assertNotEquals(0, code, 
            "响应体code字段应为非0错误码，但实际为: " + code);
    }
    
    private String extractMessageFromResponse(Response response) {
        return response.jsonPath().getString(MSG_FIELD);
    }
    
    private void validateErrorMessage(String msg) {
        assertNotEquals(null, msg, "响应体msg字段不应为空");
        assertNotEquals("", msg.trim(), "响应体msg字段不应为空字符串");
    }
    
    private void printValidationResults(Response response, int code, String msg) {
        System.out.println("\n=== 测试验证结果 ===");
        System.out.println("HTTP状态码验证通过: " + response.getStatusCode());
        System.out.println("错误码验证通过: " + code);
        System.out.println("错误信息: " + msg);
    }
}