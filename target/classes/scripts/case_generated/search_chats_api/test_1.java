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
    
    // 用户信息常量
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-fJNSsxTIBaLEdjjELne5sP1g2Tfx5gWVOiyaIQQ02Jni";
    
    // 测试数据常量
    private static final String TEST_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String BASE_URL = "https://open.feishu.cn/open-apis";
    private static final String API_VERSION = "/im/v1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    @DisplayName("获取群成员列表-正向用例-使用默认参数")
    public void testGetChatMembersWithDefaultParams() {
        // 构建请求路径
        String path = API_VERSION + "/chats/{chat_id}/members";
        
        // 发送请求并获取响应
        Response response = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .pathParam("chat_id", TEST_CHAT_ID)
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 20)
            .when()
            .get(path)
            .then()
            .extract()
            .response();
        
        // 打印响应内容到控制台
        System.out.println("=== 接口响应内容 ===");
        System.out.println("HTTP状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应时间: " + response.getTime() + "ms");
        System.out.println("===================\n");
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCode(), "HTTP状态码应为200");
        
        // 验证响应体中的关键字段
        response.then()
            .body("code", equalTo(0))
            .body("msg", equalTo("success"))
            .body("data", notNullValue())
            .body("data.items", notNullValue())
            .body("data.has_more", notNullValue())
            .body("data.member_total", notNullValue());
        
        // 验证data.items是否为数组
        assertTrue(response.path("data.items") instanceof java.util.List, "data.items应为数组类型");
        
        // 验证data.has_more是否为布尔值
        Object hasMore = response.path("data.has_more");
        assertTrue(hasMore instanceof Boolean, "data.has_more应为布尔值");
        
        // 验证data.member_total是否为整数
        Object memberTotal = response.path("data.member_total");
        assertTrue(memberTotal instanceof Integer, "data.member_total应为整数类型");
        
        // 验证接口响应时间（假设可接受范围为5秒内）
        assertTrue(response.getTime() < 5000, "接口响应时间应在5秒内");
        
        // 如果有更多数据，验证page_token存在
        if (Boolean.TRUE.equals(hasMore)) {
            String pageToken = response.path("data.page_token");
            assertNotNull(pageToken, "当has_more为true时，page_token不应为空");
            System.out.println("存在更多数据，page_token: " + pageToken);
        }
        
        // 打印成功信息
        System.out.println("✅ 测试通过：成功获取到指定群组的成员列表");
        System.out.println("✅ 测试通过：返回的数据结构符合接口文档定义");
        System.out.println("✅ 测试通过：接口响应时间在可接受范围内");
    }
}