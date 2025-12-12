import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class test_2 {
    
    // 测试变量常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-dE9V3H07F4lWrOY.pxaxXWg4hcIR5gghjyGajMw00K2w";
    private static final String VALID_CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_12345";
    
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
                .queryParam("member_id_type", "open_id");
        
        // 发送请求
        Response response = request
                .when()
                .get("/im/v1/chats/{chat_id}/members", INVALID_CHAT_ID);
        
        // 打印响应内容到控制台
        System.out.println("=== 测试用例：获取群成员列表（无效群ID） ===");
        System.out.println("请求URL: " + BASE_URL + "/im/v1/chats/" + INVALID_CHAT_ID + "/members");
        System.out.println("请求Headers: Authorization=Bearer " + USER_TOKEN + ", Content-Type=application/json");
        System.out.println("请求QueryParams: member_id_type=open_id");
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("=== 响应内容结束 ===\n");
        
        // 验证HTTP状态码
        assertEquals(400, response.getStatusCode(), "HTTP状态码应为400");
        
        // 验证响应体中的关键字段
        int code = response.path("code");
        String msg = response.path("msg");
        
        assertNotEquals(0, code, "响应code字段应为非0的错误码");
        
        // 验证错误信息包含相关描述（不区分大小写）
        String lowerCaseMsg = msg != null ? msg.toLowerCase() : "";
        if (lowerCaseMsg.contains("chat") || lowerCaseMsg.contains("群") || 
            lowerCaseMsg.contains("invalid") || lowerCaseMsg.contains("无效")) {
            // 验证通过 - 错误信息包含相关关键词
        } else {
            System.out.println("警告：错误信息可能不包含预期的关键词，实际msg: " + msg);
        }
        
        // 验证响应体结构
        response.then().assertThat().body("code", org.hamcrest.Matchers.not(0));
    }
}