import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_2 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "invalid_chat_id_123";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersWithInvalidChatId() {
        System.out.println("=== 测试开始：异常用例-无效的群ID ===");
        System.out.println("测试场景：新场景-获取群成员列表");
        System.out.println("前置条件：");
        System.out.println("1. 应用已开启机器人能力");
        System.out.println("2. 操作者（机器人或用户）必须在被查询的群组内");
        System.out.println("3. 已获取有效的访问令牌（tenant_access_token 或 user_access_token）");
        System.out.println();
        
        System.out.println("请求参数：");
        System.out.println("chat_id: " + CHAT_ID);
        System.out.println("member_id_type: open_id");
        System.out.println("Authorization: Bearer " + USER_TOKEN);
        System.out.println();
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .pathParam("chat_id", CHAT_ID)
            .queryParam("member_id_type", "open_id");
        
        System.out.println("发送GET请求到: /chats/" + CHAT_ID + "/members");
        Response response = request.get("/chats/{chat_id}/members");
        
        System.out.println("=== 响应内容 ===");
        String responseBody = response.getBody().asString();
        System.out.println("状态码: " + response.getStatusCode());
        System.out.println("响应体: " + responseBody);
        System.out.println("=== 响应结束 ===");
        System.out.println();
        
        System.out.println("=== 断言验证 ===");
        response.then()
            .statusCode(400)
            .body("code", not(equalTo(0)))
            .body("msg", containsStringIgnoringCase("chat_id"));
        
        System.out.println("断言结果：");
        System.out.println("1. 状态码为400 - 验证通过");
        System.out.println("2. 响应体code字段为非0的错误码 - 验证通过");
        System.out.println("3. 响应体msg字段包含错误提示关键字 - 验证通过");
        System.out.println();
        
        System.out.println("=== 预期结果验证 ===");
        System.out.println("1. 接口返回明确的参数错误 - 验证通过");
        System.out.println("2. 错误信息能指导调用方修正chat_id参数 - 验证通过");
        System.out.println();
        
        System.out.println("=== 测试结束：异常用例-无效的群ID ===");
    }
}