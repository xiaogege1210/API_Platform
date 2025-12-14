import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class test_1 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembersWithDefaultParameters() {
        System.out.println("=== 开始测试：获取群成员列表（默认参数） ===");
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .pathParam("chat_id", CHAT_ID)
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 20);
        
        System.out.println("请求URL: " + BASE_URL + "/chats/" + CHAT_ID + "/members");
        System.out.println("请求头: Authorization=Bearer " + USER_TOKEN);
        System.out.println("查询参数: member_id_type=open_id, page_size=20");
        
        Response response = request.when().get("/chats/{chat_id}/members");
        
        String responseBody = response.getBody().asString();
        System.out.println("=== 响应内容 ===");
        System.out.println("状态码: " + response.getStatusCode());
        System.out.println("响应体: " + responseBody);
        System.out.println("=== 响应结束 ===");
        
        response.then()
            .statusCode(200)
            .body("code", equalTo(0))
            .body("msg", equalTo("success"))
            .body("data.items", instanceOf(java.util.List.class))
            .body("data.has_more", instanceOf(Boolean.class))
            .body("data.member_total", instanceOf(Integer.class));
        
        if (response.jsonPath().getList("data.items").size() > 0) {
            response.then()
                .body("data.items[0].member_id_type", notNullValue())
                .body("data.items[0].member_id", notNullValue())
                .body("data.items[0].name", notNullValue())
                .body("data.items[0].tenant_key", notNullValue());
        }
        
        System.out.println("=== 测试通过：成功获取群成员列表 ===");
    }
}