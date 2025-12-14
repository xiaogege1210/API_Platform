import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_7 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembers_OperatorNotInChat() {
        System.out.println("=== 测试开始：异常用例-操作者不在群组内 ===");
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id");
        
        System.out.println("请求URL: " + BASE_URL + "/chats/" + CHAT_ID + "/members");
        System.out.println("请求Headers: Authorization=Bearer " + USER_TOKEN);
        System.out.println("请求Query参数: member_id_type=open_id");
        System.out.println("请求Path参数: chat_id=" + CHAT_ID);
        
        Response response = request.get("/chats/{chat_id}/members", CHAT_ID);
        
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();
        
        System.out.println("=== 响应信息 ===");
        System.out.println("状态码: " + statusCode);
        System.out.println("响应体: " + responseBody);
        System.out.println("=== 响应结束 ===");
        
        response.then()
            .assertThat()
            .statusCode(403)
            .body("code", not(equalTo(0)))
            .body("msg", anyOf(
                containsStringIgnoringCase("Forbidden"),
                containsStringIgnoringCase("not in chat"),
                containsStringIgnoringCase("无权限"),
                containsStringIgnoringCase("不在群内")
            ));
        
        System.out.println("=== 断言验证通过 ===");
        System.out.println("1. 状态码为403: 验证通过");
        System.out.println("2. 响应体code字段为非0的错误码: 验证通过");
        System.out.println("3. 响应体msg字段包含错误提示关键字: 验证通过");
        System.out.println("=== 测试结束 ===");
    }
}