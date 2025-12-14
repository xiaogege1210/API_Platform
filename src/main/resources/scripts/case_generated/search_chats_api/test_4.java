import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_4 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-fefjDWAaB9AHtrtQlz_7QsghjsGNlgMpgM0Gix400HEA";
    private static final String VALID_CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String INVALID_CHAT_ID = "invalid_chat_id_123";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    @DisplayName("异常用例_无效的群ID")
    public void testGetChatMembersWithInvalidChatId() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id");
        
        // 发送请求
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", INVALID_CHAT_ID);
        
        // 打印响应内容到控制台
        System.out.println("=== 请求信息 ===");
        System.out.println("URL: " + BASE_URL + "/chats/" + INVALID_CHAT_ID + "/members");
        System.out.println("Method: GET");
        System.out.println("Headers: Authorization=Bearer " + USER_TOKEN.substring(0, 20) + "...");
        System.out.println("Query Params: member_id_type=open_id");
        System.out.println("\n=== 响应信息 ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Time: " + response.getTime() + "ms");
        System.out.println("========================\n");
        
        // 断言验证
        response.then()
            .statusCode(400) // 验证状态码为400
            .body("code", not(equalTo(0))) // 验证code字段为非0错误码
            .body("msg", containsString("chat_id")); // 验证msg字段包含chat_id相关错误描述
    }
}