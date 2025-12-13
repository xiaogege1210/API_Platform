import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class test_7 {
    
    // 常量定义
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String USER_TOKEN = "u-fGiDd3r651m9k6gnNbOspS0giAsAlggXVOGy7Mk002Yo";
    
    // 测试用例特定常量
    private static final String INVALID_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String MEMBER_ID_TYPE = "open_id";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    public void testGetChatMembers_OperatorNotInChat() {
        // 构建请求
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", MEMBER_ID_TYPE);
        
        // 发送请求
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", INVALID_CHAT_ID);
        
        // 打印响应内容到控制台
        System.out.println("=== 测试用例：获取群成员列表-操作者不在群内 ===");
        System.out.println("请求URL: " + BASE_URL + "/chats/" + INVALID_CHAT_ID + "/members");
        System.out.println("请求头-Authorization: Bearer " + USER_TOKEN);
        System.out.println("查询参数-member_id_type: " + MEMBER_ID_TYPE);
        System.out.println("响应状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody().asString());
        System.out.println("响应时间: " + response.getTime() + "ms");
        System.out.println("=============================================\n");
        
        // 验证HTTP状态码为403或400
        int statusCode = response.getStatusCode();
        assertTrue(statusCode == 403 || statusCode == 400, 
            "期望状态码为403或400，实际为: " + statusCode);
        
        // 验证响应体包含权限或群组关系相关错误信息
        String responseBody = response.getBody().asString();
        assertTrue(responseBody.contains("code") || responseBody.contains("error"), 
            "响应体应包含错误码字段");
        
        // 进一步验证错误类型
        if (responseBody.contains("code")) {
            // 检查是否为权限相关错误码（飞书API常见错误码）
            assertTrue(responseBody.contains("99991663") || // 无权限访问
                      responseBody.contains("99991664") || // 不在群聊中
                      responseBody.contains("99991672") || // 群组不存在
                      responseBody.contains("99991700") || // 权限不足
                      responseBody.contains("permission") ||
                      responseBody.contains("access") ||
                      responseBody.contains("authority") ||
                      responseBody.contains("群组") ||
                      responseBody.contains("不在") ||
                      responseBody.contains("成员"),
                "响应体应包含权限或群组关系相关错误信息");
        }
        
        // 验证响应头
        assertNotNull(response.getHeader("Content-Type"), "响应应包含Content-Type头");
        assertTrue(response.getHeader("Content-Type").contains("application/json"), 
            "Content-Type应为application/json");
    }
}