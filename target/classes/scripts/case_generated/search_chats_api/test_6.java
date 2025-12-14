import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class test_6 {
    private static final String BASE_URL = "https://open.feishu.cn/open-apis/im/v1";
    private static final String USER_TOKEN = "u-fefjDWAaB9AHtrtQlz_7QsghjsGNlgMpgM0Gix400HEA";
    private static final String CHAT_ID = "oc_b254fcb0d0bd5cd29d27f104bad6d3a5";
    private static final String TEST_CHAT_ID = "oc_a0553eda9014c201e6969b478895c230";

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    @DisplayName("异常用例_超出最大page_size限制")
    public void testExceedMaxPageSize() {
        RequestSpecification request = given()
                .header("Authorization", "Bearer " + USER_TOKEN)
                .header("Content-Type", "application/json")
                .queryParam("member_id_type", "open_id")
                .queryParam("page_size", 150);

        Response response = request.get("/chats/{chat_id}/members", TEST_CHAT_ID);
        
        String responseBody = response.getBody().asString();
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + responseBody);
        
        response.then()
                .statusCode(400)
                .body("code", not(0))
                .body("msg", containsString("page_size"));
    }
}