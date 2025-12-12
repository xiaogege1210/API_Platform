import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class test_2 {
    
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-ckhDS3pad2nazErBeihDw1glgtOl5gqrpwaaYQs02x1K";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_123";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembersWithInvalidChatId() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .pathParam("chat_id", INVALID_CHAT_ID)
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 20);
        
        // 发送请求并获取响应
        Response response = request
            .when()
            .get("/chats/{chat_id}/members");
        
        // 打印响应内容到控制台
        System.out.println("=== 测试用例：获取群成员列表-反向用例-无效群ID ===");
        System.out.println("请求URL: " + BASE_URL + "chats/" + INVALID_CHAT_ID + "/members");
        System.out.println("请求头: Authorization=Bearer " + USER_TOKEN.substring(0, 20) + "...");
        System.out.println("查询参数: member_id_type=open_id, page_size=20");
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应时间: " + response.getTime() + "ms");
        System.out.println("=============================================\n");
        
        // 验证HTTP状态码
        assertEquals(400, response.getStatusCode(), "HTTP状态码应为400");
        
        // 验证响应体结构
        response.then()
            .body("code", not(equalTo(0))) // 验证code字段为非0错误码
            .body("msg", notNullValue()) // 验证msg字段不为空
            .body("msg", containsStringIgnoringCase("chat_id") // 验证错误信息包含chat_id相关提示
                .or(containsStringIgnoringCase("无效"))
                .or(containsStringIgnoringCase("不存在"))
                .or(containsStringIgnoringCase("错误")));
        
        // 验证错误码是否为预期的232006（如果API文档明确指定）
        // 注意：由于实际错误码可能不同，这里只验证为非0
        int errorCode = response.jsonPath().getInt("code");
        assertNotEquals(0, errorCode, "错误码应为非0值");
        
        // 验证响应体包含必要的字段
        assertTrue(response.getBody().asString().contains("code"), "响应体应包含code字段");
        assertTrue(response.getBody().asString().contains("msg"), "响应体应包含msg字段");
        
        // 验证错误信息的具体内容（根据实际响应调整）
        String errorMsg = response.jsonPath().getString("msg");
        assertNotNull(errorMsg, "错误信息不应为空");
        assertFalse(errorMsg.trim().isEmpty(), "错误信息不应为空字符串");
    }
}

// ===== 自动化优化建议 =====
// 由于LLM处理失败，以下是通用优化建议：
// 1. 确保使用显式等待而非Thread.sleep
// 2. 添加适当的异常处理
// 3. 提取重复代码为工具方法
// 4. 添加有意义的断言消息