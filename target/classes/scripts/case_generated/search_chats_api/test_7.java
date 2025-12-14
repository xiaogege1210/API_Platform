import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_7 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";
    private static final String USER_TOKEN = "1";
    
    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }
    
    @Test
    public void testGetChatMembers_OperatorNotInGroup() {
        RequestSpecification request = given()
            .header("Authorization", "Bearer " + USER_TOKEN)
            .header("Content-Type", "application/json")
            .queryParam("member_id_type", "open_id");
        
        Response response = request
            .when()
            .get("/chats/{chat_id}/members", CHAT_ID);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + responseBody);
        
        response.then()
            .statusCode(403)
            .body("code", not(equalTo(0)))
            .body("msg", anyOf(
                containsStringIgnoringCase("Forbidden"),
                containsStringIgnoringCase("not in chat"),
                containsStringIgnoringCase("无权限"),
                containsStringIgnoringCase("不在群内")
            ));
    }
}