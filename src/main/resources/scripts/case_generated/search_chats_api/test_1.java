import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_1 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "Bearer u-dE9V3H07F4lWrOY.pxaxXWg4hcIR5gghjyGajMw00K2w";
    private static final String DEFAULT_MEMBER_ID_TYPE = "open_id";
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    @Test
    void testGetChatMembersWithDefaultParams() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", DEFAULT_MEMBER_ID_TYPE)
            .queryParam("page_size", DEFAULT_PAGE_SIZE)
            .pathParam("chat_id", CHAT_ID);
        
        // 发送GET请求
        Response response = request.when()
            .get("/im/v1/chats/{chat_id}/members");
        
        // 打印响应内容到控制台
        System.out.println("=== 接口响应内容 ===");
        System.out.println("HTTP状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应头: " + response.getHeaders());
        System.out.println("响应时间: " + response.getTime() + "ms");
        System.out.println("===================\n");
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCode(), "HTTP状态码应为200");
        
        // 解析响应体
        int code = response.path("code");
        String msg = response.path("msg");
        Object data = response.path("data");
        
        // 验证响应体中的关键字段
        assertEquals(0, code, "响应体code字段应为0");
        assertEquals("success", msg, "响应体msg字段应为'success'");
        assertNotNull(data, "响应体data字段不应为null");
        
        // 验证data字段的结构
        Object items = response.path("data.items");
        Boolean hasMore = response.path("data.has_more");
        Integer memberTotal = response.path("data.member_total");
        
        assertNotNull(items, "data.items字段不应为null");
        assertTrue(items instanceof java.util.List, "data.items应为数组类型");
        assertNotNull(hasMore, "data.has_more字段不应为null");
        assertTrue(hasMore instanceof Boolean, "data.has_more应为布尔类型");
        assertNotNull(memberTotal, "data.member_total字段不应为null");
        assertTrue(memberTotal instanceof Integer, "data.member_total应为整数类型");
        
        // 如果items数组不为空，验证每个对象的字段
        java.util.List<?> itemsList = response.path("data.items");
        if (itemsList != null && !itemsList.isEmpty()) {
            for (int i = 0; i < itemsList.size(); i++) {
                String memberIdType = response.path("data.items[" + i + "].member_id_type");
                String memberId = response.path("data.items[" + i + "].member_id");
                String name = response.path("data.items[" + i + "].name");
                String tenantKey = response.path("data.items[" + i + "].tenant_key");
                
                assertNotNull(memberIdType, "items[" + i + "].member_id_type不应为null");
                assertNotNull(memberId, "items[" + i + "].member_id不应为null");
                assertNotNull(name, "items[" + i + "].name不应为null");
                assertNotNull(tenantKey, "items[" + i + "].tenant_key不应为null");
                
                // 打印成员信息用于调试
                System.out.println("成员 " + (i + 1) + ":");
                System.out.println("  member_id_type: " + memberIdType);
                System.out.println("  member_id: " + memberId);
                System.out.println("  name: " + name);
                System.out.println("  tenant_key: " + tenantKey);
            }
            
            // 提取成员ID列表，可用于下游接口
            java.util.List<String> memberIdList = new java.util.ArrayList<>();
            for (int i = 0; i < itemsList.size(); i++) {
                String memberId = response.path("data.items[" + i + "].member_id");
                memberIdList.add(memberId);
            }
            System.out.println("\n提取的member_id列表: " + memberIdList);
            
            // 提取分页标记（如果有）
            if (Boolean.TRUE.equals(hasMore)) {
                String pageToken = response.path("data.page_token");
                if (pageToken != null) {
                    System.out.println("下一页token: " + pageToken);
                }
            }
            
            // 记录总成员数
            System.out.println("总成员数: " + memberTotal);
        } else {
            System.out.println("警告: items数组为空，可能群内没有非机器人成员");
        }
        
        // 验证接口响应结构完整性
        assertDoesNotThrow(() -> {
            response.then().body("code", org.hamcrest.Matchers.equalTo(0));
            response.then().body("msg", org.hamcrest.Matchers.equalTo("success"));
            response.then().body("data", org.hamcrest.Matchers.notNullValue());
            response.then().body("data.items", org.hamcrest.Matchers.notNullValue());
            response.then().body("data.has_more", org.hamcrest.Matchers.notNullValue());
            response.then().body("data.member_total", org.hamcrest.Matchers.notNullValue());
        }, "接口响应结构验证失败");
        
        System.out.println("\n✅ 测试通过: 成功获取到指定群聊的成员列表");
        System.out.println("✅ 返回的成员信息格式正确，包含预期的字段");
        System.out.println("✅ 接口响应符合OpenAPI文档定义的成功响应结构");
    }
}