import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class test_2 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_123";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 20;
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersWithInvalidChatId() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", MEMBER_ID_TYPE)
            .queryParam("page_size", PAGE_SIZE);
        
        // 发送请求
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", INVALID_CHAT_ID);
        
        // 打印响应内容到控制台
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
        
        // 验证HTTP状态码
        assertEquals(400, response.getStatusCode(), 
            "响应状态码应为400");
        
        // 验证响应体中的code字段为非0的错误码
        int responseCode = response.jsonPath().getInt("code");
        assertNotEquals(0, responseCode, 
            "响应体code字段应为非0的错误码");
        
        // 验证响应体中的msg字段包含错误描述
        String responseMsg = response.jsonPath().getString("msg");
        System.out.println("Error Message: " + responseMsg);
        
        // 检查错误消息是否包含相关提示（由于具体错误消息可能变化，这里只检查非空）
        assert responseMsg != null && !responseMsg.isEmpty() : 
            "响应体msg字段应包含错误描述";
    }
}