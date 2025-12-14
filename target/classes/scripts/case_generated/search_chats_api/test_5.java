import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_5 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    @DisplayName("异常用例-超过最大page_size限制")
    public void testGetChatMembersWithExceededPageSize() {
        String validAccessToken = USER_TOKEN;
        String chatId = CHAT_ID;
        int exceededPageSize = 150;
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + validAccessToken)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", exceededPageSize)
            .pathParam("chat_id", chatId);
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members");
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + responseBody);
        
        response.then()
            .statusCode(400)
            .body("code", not(equalTo(0)))
            .body("msg", anyOf(
                containsStringIgnoringCase("page_size"),
                containsStringIgnoringCase("maximum"),
                containsStringIgnoringCase("100"),
                containsStringIgnoringCase("超出限制")
            ));
    }
}