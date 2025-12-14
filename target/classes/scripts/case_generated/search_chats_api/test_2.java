import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class test_2 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembersWithUserIdType() {
        System.out.println("=== 开始测试：获取群成员列表（指定user_id类型） ===");
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "user_id")
            .queryParam("page_size", 50)
            .pathParam("chat_id", CHAT_ID);
        
        System.out.println("请求URL: " + BASE_URL + "/chats/" + CHAT_ID + "/members");
        System.out.println("请求参数: member_id_type=user_id, page_size=50");
        System.out.println("请求头: Authorization=Bearer " + USER_TOKEN);
        
        Response response = request.when()
            .get("/chats/{chat_id}/members");
        
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
            .body("data.items.member_id_type", everyItem(equalTo("user_id")))
            .body("data.has_more", instanceOf(Boolean.class))
            .body("data.member_total", instanceOf(Integer.class));
        
        System.out.println("=== 断言验证通过 ===");
        System.out.println("1. 状态码为200 ✓");
        System.out.println("2. 响应体code字段为0 ✓");
        System.out.println("3. 响应体msg字段为'success' ✓");
        System.out.println("4. 响应体data.items为数组类型 ✓");
        System.out.println("5. 每个items元素的member_id_type字段值为'user_id' ✓");
        System.out.println("6. data.has_more为布尔类型 ✓");
        System.out.println("7. data.member_total为整数类型 ✓");
        
        System.out.println("=== 测试完成：成功获取到user_id类型的群成员列表 ===");
    }
}