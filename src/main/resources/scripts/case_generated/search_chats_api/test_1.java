import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_1 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersDefaultParams() {
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .pathParam("chat_id", CHAT_ID)
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 20);
        
        Response response = request.when()
            .get("/chats/{chat_id}/members");
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        
        response.then()
            .statusCode(200)
            .body("code", equalTo(0))
            .body("msg", equalTo("success"))
            .body("data.items", instanceOf(java.util.List.class))
            .body("data.has_more", instanceOf(Boolean.class))
            .body("data.member_total", instanceOf(Integer.class));
        
        if (response.jsonPath().getList("data.items").size() > 0) {
            response.then()
                .body("data.items[0]", hasKey("member_id_type"))
                .body("data.items[0]", hasKey("member_id"))
                .body("data.items[0]", hasKey("name"))
                .body("data.items[0]", hasKey("tenant_key"));
        }
    }
}