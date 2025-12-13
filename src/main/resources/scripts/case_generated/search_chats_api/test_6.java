import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class test_6 {
    
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-fGiDd3r651m9k6gnNbOspS0giAsAlggXVOGy7Mk002Yo";
    
    // 测试用例特定常量
    private static final String TEST_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int INVALID_PAGE_SIZE = 200;
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembersWithExceededPageSize() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", MEMBER_ID_TYPE)
            .queryParam("page_size", INVALID_PAGE_SIZE);
        
        // 发送请求
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", TEST_CHAT_ID);
        
        // 打印响应内容到控制台
        System.out.println("=== 响应状态码 ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("\n=== 响应头 ===");
        System.out.println(response.getHeaders());
        System.out.println("\n=== 响应体 ===");
        System.out.println(response.getBody().asString());
        System.out.println("========================================\n");
        
        // 验证HTTP状态码
        assertEquals(400, response.getStatusCode(), "HTTP状态码应为400");
        
        // 验证响应体包含错误信息
        response.then()
            .body("code", notNullValue())
            .body("msg", notNullValue())
            .body(containsString("page_size"))
            .body(anyOf(
                containsString("参数"),
                containsString("验证"),
                containsString("错误"),
                containsString("invalid"),
                containsString("parameter")
            ));
        
        // 验证响应体结构
        if (response.getStatusCode() == 400) {
            String responseBody = response.getBody().asString();
            assertTrue(responseBody.contains("page_size") || 
                      responseBody.contains("参数") || 
                      responseBody.contains("验证") ||
                      responseBody.contains("限制"),
                      "响应体应包含参数验证相关错误信息");
        }
    }
}