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
    
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String VALID_ACCESS_TOKEN = "1";
    private static final String MEMBER_ID_TYPE = "open_id";
    private static final int PAGE_SIZE = 20;
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    @DisplayName("正向用例-获取群成员列表-基础查询")
    public void testGetChatMembers() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + VALID_ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .pathParam("chat_id", CHAT_ID)
            .queryParam("member_id_type", MEMBER_ID_TYPE)
            .queryParam("page_size", PAGE_SIZE);
        
        // 发送请求并获取响应
        Response response = request.when()
            .get("/chats/{chat_id}/members");
        
        // 打印响应内容到控制台
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应内容: ");
        response.prettyPrint();
        
        // 验证HTTP状态码
        response.then().statusCode(200);
        
        // 验证响应体中的关键字段
        response.then()
            .body("code", equalTo(0))
            .body("msg", equalTo("success"))
            .body("data", notNullValue())
            .body("data.items", notNullValue())
            .body("data.has_more", instanceOf(Boolean.class))
            .body("data.member_total", instanceOf(Integer.class));
        
        // 验证data.items数组中的元素结构
        if (response.path("data.items") != null) {
            response.then()
                .body("data.items[0].member_id_type", notNullValue())
                .body("data.items[0].member_id", notNullValue())
                .body("data.items[0].name", notNullValue())
                .body("data.items[0].tenant_key", notNullValue());
        }
        
        // 使用JUnit断言进行额外验证
        assertEquals(200, response.getStatusCode(), "HTTP状态码应为200");
        assertEquals(0, response.path("code"), "code字段应为0");
        assertEquals("success", response.path("msg"), "msg字段应为'success'");
        
        // 验证响应时间（可选，这里设置5秒超时）
        assertTrue(response.getTime() < 5000, "接口响应时间应小于5秒");
        
        // 打印数据映射信息
        System.out.println("\n数据映射信息:");
        System.out.println("此接口返回的data.items数组中的member_id字段（用户ID）和member_id_type字段（ID类型），");
        System.out.println("可以被业务链路中的其他接口（如发送消息接口、@用户等）作为输入参数使用。");
        System.out.println("例如，发送消息时可以使用这些member_id来指定消息接收者。");
    }
}