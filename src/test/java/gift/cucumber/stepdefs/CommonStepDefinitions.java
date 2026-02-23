package gift.cucumber.stepdefs;

import gift.cucumber.ScenarioContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

public class CommonStepDefinitions {

    @Autowired
    private ScenarioContext context;

    @Autowired
    private DataSource dataSource;

    @Given("공통 데이터가 초기화되어 있다")
    public void 공통_데이터가_초기화되어_있다() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("sql/common-init.sql"));
        }
    }

    @Then("응답 상태 코드가 {int}이다")
    public void 응답_상태_코드가_N이다(int statusCode) {
        context.getResponse()
            .then()
            .statusCode(statusCode);
    }
}
