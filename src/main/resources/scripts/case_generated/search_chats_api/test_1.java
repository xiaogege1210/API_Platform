import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_1 {
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String USER_TOKEN = "1";
    private static final String ACCESS_TOKEN = "1"; // 根据实际情况替换为有效的token

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    public void testGetChatMembersWithDefaultParams() {
        // 构建请求
        RequestSpecification request = given()
                .header("Authorization", "Bearer " + ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .queryParam("member_id_type", "open_id")
                .queryParam("page_size", 20);

        // 发送请求
        Response response = request.get("/chats/{chat_id}/members", CHAT_ID);
        
        // 打印响应内容到控制台
        System.out.println("Response Body:");
        System.out.println(response.getBody().asString());
        System.out.println("Status Code: " + response.getStatusCode());
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCode(), "HTTP状态码应为200");
        
        // 验证响应体中的关键字段
        int code = response.path("code");
        String msg = response.path("msg");
        Object data = response.path("data");
        Object items = response.path("data.items");
        
        assertEquals(0, code, "响应体code字段应为0");
        assertEquals("success", msg, "响应体msg字段应为'success'");
        assertNotNull(data, "响应体data字段应存在");
        assertNotNull(items, "响应体data.items数组应存在");
        
        // 验证items数组中的对象结构
        if (items instanceof java.util.List) {
            java.util.List<?> itemsList = (java.util.List<?>) items;
            if (!itemsList.isEmpty()) {
                Object firstItem = itemsList.get(0);
                assertNotNull(response.path("data.items[0].member_id_type"), 
                    "items数组中对象应包含member_id_type字段");
                assertNotNull(response.path("data.items[0].member_id"), 
                    "items数组中对象应包含member_id字段");
                assertNotNull(response.path("data.items[0].name"), 
                    "items数组中对象应包含name字段");
                assertNotNull(response.path("data.items[0].tenant_key"), 
                    "items数组中对象应包含tenant_key字段");
            }
        }
        
        // 验证分页相关字段
        assertNotNull(response.path("data.page_token"), 
            "data字段应包含page_token字段");
        assertNotNull(response.path("data.has_more"), 
            "data字段应包含has_more字段");
        assertNotNull(response.path("data.member_total"), 
            "data字段应包含member_total字段");
        
        System.out.println("测试通过：成功获取群成员列表，返回格式正确，分页参数正常");
    }
}