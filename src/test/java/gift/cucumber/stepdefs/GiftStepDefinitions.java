package gift.cucumber.stepdefs;

import gift.cucumber.ScenarioContext;
import gift.model.Option;
import gift.model.OptionRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

public class GiftStepDefinitions {

    @Autowired
    private ScenarioContext context;

    @Autowired
    private OptionRepository optionRepository;

    @Autowired
    private DataSource dataSource;

    @Value("${test.sql.base-path:sql}")
    private String sqlBasePath;

    @Given("재고가 {int}인 옵션이 존재한다")
    public void 재고가_N인_옵션이_존재한다(int quantity) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource(sqlBasePath + "/common-init.sql"));
            String sqlFile = switch (quantity) {
                case 10 -> sqlBasePath + "/gift/success.sql";
                case 5 -> sqlBasePath + "/gift/exact-quantity.sql";
                case 2 -> sqlBasePath + "/gift/insufficient-stock.sql";
                case 0 -> sqlBasePath + "/gift/zero-stock.sql";
                default -> throw new IllegalArgumentException("미지원 재고 수량: " + quantity);
            };
            ScriptUtils.executeSqlScript(conn, new ClassPathResource(sqlFile));
        }
    }

    @When("회원 {long}이 옵션 {long}을 {int}개 선물한다")
    public void 회원이_옵션을_N개_선물한다(long memberId, long optionId, int quantity) {
        context.setResponse(
            RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", memberId)
                .body("""
                    {"optionId": %d, "quantity": %d, "receiverId": 2, "message": "선물"}
                    """.formatted(optionId, quantity))
            .when()
                .post("/api/gifts")
        );
    }

    @When("옵션 ID에 {string}을 담아 선물 요청을 보낸다")
    public void 옵션_ID에_잘못된값을_담아_선물_요청을_보낸다(String invalidValue) {
        context.setResponse(
            RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1L)
                .body("""
                    {"optionId": "%s", "quantity": 1, "receiverId": 2, "message": "선물"}
                    """.formatted(invalidValue))
            .when()
                .post("/api/gifts")
        );
    }

    @And("옵션 {long}의 재고가 {int}이다")
    public void 옵션의_재고가_N이다(long optionId, int expectedQuantity) {
        Option option = optionRepository.findById(optionId).orElseThrow();
        assertThat(option.getQuantity()).isEqualTo(expectedQuantity);
    }
}
