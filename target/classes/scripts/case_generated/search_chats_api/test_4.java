import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class test_4 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "1";
    private static final String CHAT_ID = "1";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_123";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    @DisplayName("异常用例-无效的群ID")
    public void testGetChatMembersWithInvalidChatId() {
        System.out.println("=== 开始测试：获取群成员列表（无效群ID） ===");
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id");
        
        System.out.println("请求URL: " + BASE_URL + "/chats/" + INVALID_CHAT_ID + "/members");
        System.out.println("请求Headers: Authorization=Bearer " + USER_TOKEN);
        System.out.println("请求Query参数: member_id_type=open_id");
        
        Response response = request.when()
            .get("/chats/{chat_id}/members", INVALID_CHAT_ID);
        
        String responseBody = response.getBody().asString();
        System.out.println("=== 响应状态码: " + response.getStatusCode() + " ===");
        System.out.println("=== 响应内容: ===");
        System.out.println(responseBody);
        System.out.println("=== 响应结束 ===");
        
        response.then()
            .statusCode(400)
            .body("code", notNullValue())
            .body("code", equalTo(232006))
            .body("msg", notNullValue());
        
        System.out.println("断言验证通过：");
        System.out.println("1. 状态码为400 ✓");
        System.out.println("2. 响应体code字段为非0的错误码(232006) ✓");
        System.out.println("3. 响应体msg字段包含错误提示信息 ✓");
    }
}