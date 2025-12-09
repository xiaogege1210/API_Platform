import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_2 {
    
    // 测试常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/";
    private static final String TENANT_ACCESS_TOKEN = "tenant_access_token";
    private static final String CHAT_ID = "oc_1234567890abcdef";
    
    @Test
    void testGetChatInfo() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + TENANT_ACCESS_TOKEN)
            .header("Content-Type", "application/json");
        
        // 发送GET请求
        Response response = request
            .pathParam("chat_id", CHAT_ID)
            .when()
            .get("/im/v1/chats/{chat_id}");
        
        // 打印响应内容到控制台
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应头: " + response.getHeaders());
        
        // 验证HTTP状态码
        assertEquals(200, response.getStatusCode(), "状态码应为200");
        
        // 验证响应体中的关键字段
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("\"code\":0") || responseBody.contains("\"success\":true"), 
                  "响应应包含成功状态码");
        
        // 验证chat_id字段
        if (response.getStatusCode() == 200) {
            String chatIdInResponse = response.jsonPath().getString("data.chat_id");
            if (chatIdInResponse != null) {
                assertEquals(CHAT_ID, chatIdInResponse, "返回的chat_id应与请求一致");
            }
            
            // 验证群组名称字段
            String name = response.jsonPath().getString("data.name");
            assertNotNull(name, "响应体应包含群组名称（name）字段");
            
            // 验证群组所有者字段
            String ownerId = response.jsonPath().getString("data.owner_id");
            assertNotNull(ownerId, "响应体应包含群组所有者（owner_id）字段");
            
            // 验证群组类型字段
            String type = response.jsonPath().getString("data.type");
            assertNotNull(type, "响应体应包含群组类型（type）字段");
            
            // 打印验证通过的字段值
            System.out.println("验证通过的字段值:");
            System.out.println("群组名称: " + name);
            System.out.println("群组所有者ID: " + ownerId);
            System.out.println("群组类型: " + type);
        }
        
        // 验证响应数据格式
        assertNotNull(response.getContentType(), "响应应包含Content-Type头");
        assertTrue(response.getContentType().contains("application/json"), 
                  "响应Content-Type应为application/json");
    }
}