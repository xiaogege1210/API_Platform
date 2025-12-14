import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.matchesPattern;

public class test_2 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-fefjDWAaB9AHtrtQlz_7QsghjsGNlgMpgM0Gix400HEA";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    @DisplayName("正向用例_获取群成员列表_指定user_id类型")
    public void testGetChatMembersWithUserIdType() {
        String testChatId = "oc_a0553eda9014c201e6969b478895c230";
        String memberIdType = "user_id";
        int pageSize = 50;
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", memberIdType)
            .queryParam("page_size", pageSize);
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", testChatId);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        System.out.println("Status Code: " + response.getStatusCode());
        System.out.println("Response Headers: " + response.getHeaders());
        
        response.then()
            .statusCode(200)
            .body("code", equalTo(0))
            .body("msg", equalTo("success"))
            .body("data", hasKey("items"))
            .body("data.items", notNullValue())
            .body("data.items.every { it.containsKey('member_id_type') }", equalTo(true))
            .body("data.items.member_id_type", everyItem(equalTo("user_id")))
            .body("data.items.member_id", everyItem(matchesPattern("^[0-9a-zA-Z]+$")));
        
        if (response.path("data.items") != null) {
            int actualItemCount = response.path("data.items.size()");
            System.out.println("Actual returned items count: " + actualItemCount);
            System.out.println("Note: page_size parameter may be affected by filtering rules as documented.");
        }
    }
}