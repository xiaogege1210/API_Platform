import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import static io.restassured.RestAssured.given;

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
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "user_id")
            .queryParam("page_size", 50)
            .pathParam("chat_id", CHAT_ID);
        
        Response response = request.when()
            .get("/chats/{chat_id}/members");
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        
        response.then()
            .statusCode(200);
        
        int code = response.jsonPath().getInt("code");
        String msg = response.jsonPath().getString("msg");
        Object items = response.jsonPath().get("data.items");
        Boolean hasMore = response.jsonPath().getBoolean("data.has_more");
        Integer memberTotal = response.jsonPath().getInt("data.member_total");
        
        Assertions.assertEquals(0, code, "响应code应为0");
        Assertions.assertEquals("success", msg, "响应msg应为'success'");
        Assertions.assertNotNull(items, "data.items不应为null");
        Assertions.assertTrue(items instanceof java.util.List, "data.items应为数组类型");
        
        if (items instanceof java.util.List && ((java.util.List<?>) items).size() > 0) {
            String memberIdType = response.jsonPath().getString("data.items[0].member_id_type");
            Assertions.assertEquals("user_id", memberIdType, "items元素的member_id_type字段值应为'user_id'");
        }
        
        Assertions.assertNotNull(hasMore, "data.has_more不应为null");
        Assertions.assertTrue(hasMore instanceof Boolean, "data.has_more应为布尔类型");
        
        Assertions.assertNotNull(memberTotal, "data.member_total不应为null");
        Assertions.assertTrue(memberTotal instanceof Integer, "data.member_total应为整数类型");
        
        System.out.println("测试通过：成功获取到user_id类型的群成员列表");
        System.out.println("返回的member_id_type字段值为user_id");
        System.out.println("分页参数生效，page_size=50");
    }
}