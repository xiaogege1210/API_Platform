import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_5 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "1";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembersWithExceededPageSize() {
        String endpoint = "/chats/{chat_id}/members";
        
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .pathParam("chat_id", CHAT_ID)
            .queryParam("member_id_type", "open_id")
            .queryParam("page_size", 150);
        
        Response response = request.get(endpoint);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);
        System.out.println("Status Code: " + response.getStatusCode());
        
        response.then()
            .statusCode(400)
            .body("code", not(equalTo(0)))
            .body("msg", anyOf(
                containsString("page_size"),
                containsString("maximum"),
                containsString("100"),
                containsString("超出限制")
            ));
    }
}