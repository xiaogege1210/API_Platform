import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static io.restassured.RestAssured.given;

public class test_3 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String USER_TOKEN = "1";
    private static final String PAGE_TOKEN = "WWxHTStrOEs5WHZpNktGbU94bUcvMWlxdDUzTWt1OXNrRmlLaGRNVG0yaz0=";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    public void testGetChatMembersWithPagination() {
        RequestSpecification request = given()
                .header("Authorization", "Bearer " + USER_TOKEN)
                .header("Content-Type", "application/json")
                .queryParam("member_id_type", "open_id")
                .queryParam("page_size", 10)
                .queryParam("page_token", PAGE_TOKEN);

        Response response = request
                .pathParam("chat_id", CHAT_ID)
                .when()
                .get("/chats/{chat_id}/members");

        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        System.out.println("Status Code: " + response.getStatusCode());

        Assertions.assertEquals(200, response.getStatusCode(), "HTTP状态码应为200");
        
        int code = response.jsonPath().getInt("code");
        String msg = response.jsonPath().getString("msg");
        Object items = response.jsonPath().get("data.items");
        Object pageToken = response.jsonPath().get("data.page_token");
        Boolean hasMore = response.jsonPath().getBoolean("data.has_more");

        Assertions.assertEquals(0, code, "响应体code字段应为0");
        Assertions.assertEquals("success", msg, "响应体msg字段应为'success'");
        Assertions.assertNotNull(items, "响应体data.items应为数组类型");
        Assertions.assertTrue(items instanceof java.util.List, "响应体data.items应为数组类型");
        Assertions.assertNotNull(pageToken, "data.page_token字段应存在");
        Assertions.assertNotNull(hasMore, "data.has_more应为布尔类型");
        Assertions.assertTrue(hasMore instanceof Boolean, "data.has_more应为布尔类型");
    }
}