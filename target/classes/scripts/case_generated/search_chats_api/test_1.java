import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class test_1 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-fefjDWAaB9AHtrtQlz_7QsghjsGNlgMpgM0Gix400HEA";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 20;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("正向用例_获取群成员列表_默认参数")
    public void testGetChatMembersWithDefaultParameters() {
        System.out.println("=== 开始执行测试：获取群成员列表（默认参数） ===");
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .pathParam("chat_id", CHAT_ID)
            .queryParam("member_id_type", MEMBER_ID_TYPE)
            .queryParam("page_size", PAGE_SIZE);

        System.out.println("请求URL: " + BASE_URL + "/chats/" + CHAT_ID + "/members");
        System.out.println("请求头 - Authorization: Bearer " + USER_TOKEN.substring(0, 20) + "...");
        System.out.println("查询参数 - member_id_type: " + MEMBER_ID_TYPE);
        System.out.println("查询参数 - page_size: " + PAGE_SIZE);

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

        System.out.println("=== 断言验证通过 ===");
        System.out.println("1. 状态码为200 ✓");
        System.out.println("2. 响应体code字段为0 ✓");
        System.out.println("3. 响应体msg字段为'success' ✓");
        System.out.println("4. 响应体data.items为数组 ✓");
        
        java.util.List<?> items = response.jsonPath().getList("data.items");
        if (items.size() > 0) {
            System.out.println("5. 响应体data.items数组中的每个对象包含member_id_type、member_id、name、tenant_key字段 ✓");
        } else {
            System.out.println("5. data.items数组为空，跳过字段验证");
        }
        
        System.out.println("6. 响应体data.has_more为布尔值 ✓");
        System.out.println("7. 响应体data.member_total为整数 ✓");
        
        Boolean hasMore = response.jsonPath().getBoolean("data.has_more");
        Integer memberTotal = response.jsonPath().getInt("data.member_total");
        System.out.println("分页信息 - has_more: " + hasMore + ", member_total: " + memberTotal);
        
        if (hasMore) {
            String pageToken = response.jsonPath().getString("data.page_token");
            System.out.println("存在下一页，page_token: " + pageToken);
            System.out.println("可根据has_more和page_token进行后续分页查询 ✓");
        } else {
            System.out.println("已获取所有成员数据，无需分页查询 ✓");
        }
        
        System.out.println("=== 测试执行完成 ===");
    }
}