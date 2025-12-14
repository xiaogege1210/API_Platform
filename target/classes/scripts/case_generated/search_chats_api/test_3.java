import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static io.restassured.RestAssured.given;

public class test_3 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "1";
    private static final String CHAT_ID = "1";
    
    private static final String VALID_ACCESS_TOKEN = "Bearer " + USER_TOKEN;
    private static final String TEST_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String PAGE_TOKEN = "WWxHTStrOEs5WHZpNktGbU94bUcvMWlxdDUzTWt1OXNrRmlLaGRNVG0yaz0=";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 10;
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersWithPagination() {
        RequestSpecification request = given()
            .header("Authorization", VALID_ACCESS_TOKEN)
            .queryParam("member_id_type", MEMBER_ID_TYPE)
            .queryParam("page_size", PAGE_SIZE)
            .queryParam("page_token", PAGE_TOKEN);
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", TEST_CHAT_ID);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        
        int statusCode = response.getStatusCode();
        System.out.println("Status Code: " + statusCode);
        
        Assertions.assertEquals(200, statusCode, "状态码应为200");
        
        int code = response.path("code");
        String msg = response.path("msg");
        Object items = response.path("data.items");
        Object pageToken = response.path("data.page_token");
        Boolean hasMore = response.path("data.has_more");
        
        System.out.println("code字段值: " + code);
        System.out.println("msg字段值: " + msg);
        System.out.println("data.items类型: " + (items != null ? items.getClass().getSimpleName() : "null"));
        System.out.println("data.page_token值: " + pageToken);
        System.out.println("data.has_more值: " + hasMore);
        
        Assertions.assertEquals(0, code, "响应体code字段应为0");
        Assertions.assertEquals("success", msg, "响应体msg字段应为'success'");
        Assertions.assertNotNull(items, "响应体data.items不应为null");
        Assertions.assertTrue(items instanceof java.util.List || items.getClass().isArray(), 
            "响应体data.items应为数组类型");
        Assertions.assertNotNull(hasMore, "data.has_more字段不应为null");
        Assertions.assertTrue(hasMore instanceof Boolean, "data.has_more应为布尔类型");
        
        System.out.println("测试通过：成功使用page_token进行分页查询");
        System.out.println("测试通过：返回正确的分页数据");
        System.out.println("测试通过：分页机制正常工作");
    }
}