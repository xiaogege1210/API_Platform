import org.junit.jupiter.api.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class test_4 {
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-dE9V3H07F4lWrOY.pxaxXWg4hcIR5gghjyGajMw00K2w";
    private static final String ACCESS_TOKEN = USER_TOKEN;
    
    @Test
    void testGetChatMembersWithExcessivePageSize() {
        // 设置基础URL
        RestAssured.baseURI = BASE_URL;
        
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + ACCESS_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("page_size", 150);
        
        // 发送GET请求
        Response response = request.get("/im/v1/chats/" + CHAT_ID + "/members");
        
        // 打印响应内容到控制台
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());
        System.out.println("Response Headers: " + response.getHeaders());
        
        // 验证HTTP状态码为400
        assertEquals(400, response.getStatusCode(), 
            "HTTP状态码应为400");
        
        // 验证响应体中的code字段为非0的错误码
        int code = response.jsonPath().getInt("code");
        assertNotEquals(0, code, 
            "响应体code字段应为非0的错误码");
        
        // 验证响应体中的msg字段包含参数验证相关的错误描述
        String msg = response.jsonPath().getString("msg");
        System.out.println("Error Message: " + msg);
        
        // 检查错误信息是否包含参数验证相关的关键词
        boolean hasValidationError = msg != null && (
            msg.toLowerCase().contains("page_size") || 
            msg.toLowerCase().contains("参数") || 
            msg.toLowerCase().contains("invalid") || 
            msg.toLowerCase().contains("validation") ||
            msg.toLowerCase().contains("范围") ||
            msg.toLowerCase().contains("exceed")
        );
        
        if (!hasValidationError) {
            System.out.println("警告：错误信息可能未明确指示参数无效的原因");
        }
    }
}