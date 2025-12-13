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
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String VALID_ACCESS_TOKEN = "1"; // 实际使用时需要替换为有效的token
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 20;
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    @DisplayName("正向用例 - 使用默认参数成功获取群成员列表")
    public void testGetChatMembersSuccessfully() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + VALID_ACCESS_TOKEN)
            .queryParam("member_id_type", MEMBER_ID_TYPE)
            .queryParam("page_size", PAGE_SIZE)
            .pathParam("chat_id", CHAT_ID);
        
        // 发送请求并获取响应
        Response response = request
            .when()
            .get("/chats/{chat_id}/members");
        
        // 打印响应内容到控制台
        System.out.println("=== 响应状态码 ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("\n=== 响应头 ===");
        System.out.println(response.getHeaders());
        System.out.println("\n=== 响应体 ===");
        System.out.println(response.getBody().asString());
        System.out.println("========================================\n");
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCode(), "HTTP状态码应为200");
        
        // 使用RestAssured断言验证响应体
        response.then()
            .statusCode(200)
            .body("code", equalTo(0))
            .body("msg", equalTo("success"))
            .body("data", notNullValue())
            .body("data.items", notNullValue())
            .body("data.items", instanceOf(java.util.List.class))
            .body("data.has_more", instanceOf(Boolean.class))
            .body("data.member_total", instanceOf(Integer.class));
        
        // 验证data字段存在且为对象
        assertNotNull(response.path("data"), "data字段不应为null");
        
        // 验证items数组存在
        Object items = response.path("data.items");
        assertNotNull(items, "data.items字段不应为null");
        assertTrue(items instanceof java.util.List, "data.items应为数组类型");
        
        // 验证has_more字段为布尔类型
        Object hasMore = response.path("data.has_more");
        assertNotNull(hasMore, "data.has_more字段不应为null");
        assertTrue(hasMore instanceof Boolean, "data.has_more应为布尔类型");
        
        // 如果has_more为true，验证page_token字段存在且为字符串
        if (Boolean.TRUE.equals(hasMore)) {
            Object pageToken = response.path("data.page_token");
            assertNotNull(pageToken, "当data.has_more为true时，data.page_token字段不应为null");
            assertTrue(pageToken instanceof String, "data.page_token应为字符串类型");
            assertFalse(((String) pageToken).isEmpty(), "data.page_token不应为空字符串");
        }
        
        // 验证member_total字段为整数类型
        Object memberTotal = response.path("data.member_total");
        assertNotNull(memberTotal, "data.member_total字段不应为null");
        assertTrue(memberTotal instanceof Integer, "data.member_total应为整数类型");
        
        // 验证items数组中的每个对象都包含必要的字段
        java.util.List<java.util.Map<String, Object>> itemsList = response.path("data.items");
        if (itemsList != null && !itemsList.isEmpty()) {
            for (java.util.Map<String, Object> item : itemsList) {
                assertTrue(item.containsKey("member_id_type"), "items中的对象应包含member_id_type字段");
                assertTrue(item.containsKey("member_id"), "items中的对象应包含member_id字段");
                assertTrue(item.containsKey("name"), "items中的对象应包含name字段");
                assertTrue(item.containsKey("tenant_key"), "items中的对象应包含tenant_key字段");
                
                // 验证字段类型
                assertTrue(item.get("member_id_type") instanceof String, "member_id_type应为字符串类型");
                assertTrue(item.get("member_id") instanceof String, "member_id应为字符串类型");
                assertTrue(item.get("tenant_key") instanceof String, "tenant_key应为字符串类型");
            }
        }
        
        // 验证业务预期结果
        assertTrue(itemsList != null, "应成功获取到成员列表");
        assertTrue(itemsList.size() > 0 || (Integer) memberTotal == 0, 
            "如果群组有成员，items应不为空；如果群组无成员，member_total应为0");
        
        System.out.println("测试通过：成功获取到指定群组的成员列表");
        System.out.println("返回的成员信息格式正确，包含预期的字段");
        System.out.println("分页信息（has_more, page_token）正确指示了数据状态");
    }
}