import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class test_4 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembersWithExcessivePageSize() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("page_size", 150);
        
        // 发送请求
        Response response = request.get("/chats/{chat_id}/members", CHAT_ID);
        
        // 打印响应内容到控制台
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
        
        // 验证HTTP状态码为400
        assertEquals(400, response.getStatusCode(), 
            "HTTP状态码应为400，但实际为: " + response.getStatusCode());
        
        // 验证响应体中的code字段为非0的错误码
        int code = response.jsonPath().getInt("code");
        assertNotEquals(0, code, 
            "响应体code字段应为非0错误码，但实际为: " + code);
        
        // 验证响应体中的msg字段不为空（表明参数验证失败）
        String msg = response.jsonPath().getString("msg");
        assertNotEquals(null, msg, "响应体msg字段不应为空");
        assertNotEquals("", msg.trim(), "响应体msg字段不应为空字符串");
        
        // 打印验证结果
        System.out.println("测试通过：");
        System.out.println("1. HTTP状态码验证成功：400");
        System.out.println("2. 响应体code字段验证成功：非0错误码 (" + code + ")");
        System.out.println("3. 响应体msg字段验证成功：" + msg);
    }
}