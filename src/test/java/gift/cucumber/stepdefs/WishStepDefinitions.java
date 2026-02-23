package gift.cucumber.stepdefs;

import gift.cucumber.ScenarioContext;
import gift.model.Wish;
import gift.model.WishRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WishStepDefinitions {

    @Autowired
    private ScenarioContext context;

    @Autowired
    private WishRepository wishRepository;

    @Autowired
    private DataSource dataSource;

    @Given("상품 {long}이 존재한다")
    public void 상품이_존재한다(long productId) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/common-init.sql"));
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/wish/success.sql"));
        }
    }

    @When("회원 {long}이 상품 {long}을 위시리스트에 추가한다")
    public void 회원이_상품을_위시리스트에_추가한다(long memberId, long productId) {
        context.setResponse(
            RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", memberId)
                .body("""
                    {"productId": %d}
                    """.formatted(productId))
            .when()
                .post("/api/wishes")
        );
    }

    @When("상품 ID에 {string}을 담아 위시리스트 요청을 보낸다")
    public void 상품_ID에_잘못된값을_담아_위시리스트_요청을_보낸다(String invalidValue) {
        context.setResponse(
            RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Member-Id", 1L)
                .body("""
                    {"productId": "%s"}
                    """.formatted(invalidValue))
            .when()
                .post("/api/wishes")
        );
    }

    @And("위시리스트에 {int}개 항목이 저장되어 있다")
    public void 위시리스트에_N개_항목이_저장되어_있다(int expectedCount) {
        List<Wish> wishes = wishRepository.findAll();
        assertThat(wishes).hasSize(expectedCount);
    }

    @And("위시리스트에 회원 {long}과 상품 {long}의 연결이 존재한다")
    public void 위시리스트에_회원과_상품의_연결이_존재한다(long memberId, long productId) {
        List<Wish> wishes = wishRepository.findAll();
        assertThat(wishes)
            .anyMatch(w -> w.getMember().getId().equals(memberId) && w.getProduct().getId().equals(productId));
    }
}
