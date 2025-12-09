import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_1 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String TENANT_ACCESS_TOKEN = "tenant_access_token";
    private static final String OPEN_ID = "ou_1234567890abcdef";
    private static final String CHAT_ID = "";
    
    @Test
    public void testSendTextMessage() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求体
        String requestBody = "{" +
                "\"receive_id\": \"" + OPEN_ID + "\"," +
                "\"msg_type\": \"text\"," +
                "\"content\": \"{\\\"text\\\":\\\"这是一条测试消息\\\"}\"" +
                "}";
        
        // 发送请求并获取响应
        Response response = given()
                .header("Authorization", "Bearer " + TENANT_ACCESS_TOKEN)
                .header("Content-Type", "application/json")
                .queryParam("receive_id_type", "open_id")
                .body(requestBody)
                .when()
                .post("/im/v1/messages")
                .then()
                .extract()
                .response();
        
        // 打印响应内容到控制台
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应内容: " + response.getBody().asString());
        System.out.println("响应头: " + response.getHeaders());
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCode(), "状态码应为200");
        
        // 验证响应体包含message_id字段
        assertNotNull(response.jsonPath().getString("data.message_id"), 
                     "响应体应包含message_id字段");
        
        // 验证响应体包含receive_id字段且值与请求一致
        String receiveId = response.jsonPath().getString("data.receive_id");
        assertNotNull(receiveId, "响应体应包含receive_id字段");
        assertEquals(OPEN_ID, receiveId, "receive_id应与请求值一致");
        
        // 验证响应结构
        assertTrue(response.jsonPath().getInt("code") == 0, 
                  "响应code应为0表示成功");
        assertNotNull(response.jsonPath().getString("msg"), 
                     "响应应包含msg字段");
    }
}