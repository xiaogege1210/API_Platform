import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class test_3 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-fefjDWAaB9AHtrtQlz_7QsghjsGNlgMpgM0Gix400HEA";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String PAGE_TOKEN = "WWxHTStrOEs5WHZpNktGbU94bUcvMWlxdDUzTWt1OXNrRmlLaGRNVG0yaz0=";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 10;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @DisplayName("正向用例_获取群成员列表_使用分页令牌")
    public void testGetChatMembersWithPageToken() {
        RequestSpecification request = given()
                .header("Authorization", "Bearer " + USER_TOKEN)
                .header("Content-Type", "application/json")
                .pathParam("chat_id", CHAT_ID)
                .queryParam("member_id_type", MEMBER_ID_TYPE)
                .queryParam("page_size", PAGE_SIZE)
                .queryParam("page_token", PAGE_TOKEN);

        Response response = request.when().get("/chats/{chat_id}/members");
        
        String responseBody = response.getBody().asString();
        System.out.println("响应内容: " + responseBody);
        
        response.then()
                .statusCode(200)
                .body("code", equalTo(0))
                .body("data", hasKey("items"))
                .body("data.items", instanceOf(java.util.List.class));
        
        boolean hasMore = response.path("data.has_more");
        if (hasMore) {
            response.then().body("data.page_token", notNullValue());
            System.out.println("还有更多数据，下一页令牌: " + response.path("data.page_token"));
        } else {
            response.then().body("data.page_token", nullValue());
            System.out.println("已到达最后一页");
        }
        
        java.util.List<?> items = response.path("data.items");
        assertNotNull(items, "items列表不应为null");
        System.out.println("获取到 " + items.size() + " 个成员");
        
        assertTrue(items.size() <= PAGE_SIZE, "返回数量不应超过page_size");
    }
}