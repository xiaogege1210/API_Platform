import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static io.restassured.RestAssured.given;

public class test_1 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersList() {
        String path = "/chats/" + CHAT_ID + "/members";
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 20);
        
        Response response = request.get(path);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        
        int statusCode = response.getStatusCode();
        System.out.println("Status Code: " + statusCode);
        
        Assertions.assertEquals(200, statusCode, "HTTP状态码应为200");
        
        if (statusCode == 200) {
            int code = response.path("code");
            String msg = response.path("msg");
            
            Assertions.assertEquals(0, code, "响应体code字段应为0");
            Assertions.assertEquals("success", msg, "响应体msg字段应为'success'");
            
            Object items = response.path("data.items");
            Assertions.assertNotNull(items, "响应体data.items不应为null");
            Assertions.assertTrue(items instanceof java.util.List, "响应体data.items应为数组类型");
            
            Boolean hasMore = response.path("data.has_more");
            Assertions.assertNotNull(hasMore, "响应体data.has_more不应为null");
            Assertions.assertTrue(hasMore instanceof Boolean, "响应体data.has_more应为布尔类型");
            
            Integer memberTotal = response.path("data.member_total");
            Assertions.assertNotNull(memberTotal, "响应体data.member_total不应为null");
            Assertions.assertTrue(memberTotal instanceof Integer, "响应体data.member_total应为整数类型");
            
            System.out.println("测试通过：成功获取群成员列表");
            System.out.println("成员总数: " + memberTotal);
            System.out.println("是否有更多数据: " + hasMore);
        }
    }
}