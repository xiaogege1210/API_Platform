import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_1 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-ckhDS3pad2nazErBeihDw1glgtOl5gqrpwaaYQs02x1K";
    private static final String ACCESS_TOKEN = USER_TOKEN;
    
    @Test
    public void testGetChatMembersWithDefaultParameters() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .pathParam("chat_id", CHAT_ID)
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 20);
        
        // 发送GET请求
        Response response = request.get("/chats/{chat_id}/members");
        
        // 打印响应内容到控制台
        System.out.println("=== 响应状态码 ===");
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("\n=== 响应头 ===");
        System.out.println(response.getHeaders());
        System.out.println("\n=== 响应体 ===");
        String responseBody = response.getBody().asString();
        System.out.println(responseBody);
        System.out.println("========================================\n");
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCode(), "响应状态码应为200");
        
        // 验证响应体中的关键字段
        assertNotNull(responseBody, "响应体不应为空");
        
        // 验证code字段为0
        int code = response.path("code");
        assertEquals(0, code, "响应体code字段应为0");
        
        // 验证msg字段为'success'
        String msg = response.path("msg");
        assertEquals("success", msg, "响应体msg字段应为'success'");
        
        // 验证data字段存在且为对象
        Object data = response.path("data");
        assertNotNull(data, "data字段不应为空");
        
        // 验证data.items字段存在且为数组
        Object items = response.path("data.items");
        assertNotNull(items, "data.items字段不应为空");
        
        // 验证data.has_more字段为布尔类型
        Object hasMore = response.path("data.has_more");
        assertNotNull(hasMore, "data.has_more字段不应为空");
        assertTrue(hasMore instanceof Boolean, "data.has_more字段应为布尔类型");
        
        // 验证data.member_total字段为整数类型
        Object memberTotal = response.path("data.member_total");
        assertNotNull(memberTotal, "data.member_total字段不应为空");
        assertTrue(memberTotal instanceof Integer, "data.member_total字段应为整数类型");
        
        // 验证items数组中每个对象都包含必需字段
        int itemCount = response.path("data.items.size()");
        if (itemCount > 0) {
            for (int i = 0; i < itemCount; i++) {
                String memberIdType = response.path("data.items[" + i + "].member_id_type");
                String memberId = response.path("data.items[" + i + "].member_id");
                String name = response.path("data.items[" + i + "].name");
                String tenantKey = response.path("data.items[" + i + "].tenant_key");
                
                assertNotNull(memberIdType, "items[" + i + "].member_id_type字段不应为空");
                assertNotNull(memberId, "items[" + i + "].member_id字段不应为空");
                assertNotNull(name, "items[" + i + "].name字段不应为空");
                assertNotNull(tenantKey, "items[" + i + "].tenant_key字段不应为空");
                
                System.out.println("验证通过 - 成员 " + (i + 1) + ":");
                System.out.println("  member_id_type: " + memberIdType);
                System.out.println("  member_id: " + memberId);
                System.out.println("  name: " + name);
                System.out.println("  tenant_key: " + tenantKey);
            }
        }
        
        // 打印测试结果摘要
        System.out.println("\n=== 测试结果摘要 ===");
        System.out.println("测试用例: 获取群成员列表-正向用例-默认参数");
        System.out.println("测试状态: PASS");
        System.out.println("获取成员数量: " + itemCount);
        System.out.println("是否有更多数据: " + hasMore);
        System.out.println("成员总数: " + memberTotal);
        System.out.println("========================================\n");
    }
}