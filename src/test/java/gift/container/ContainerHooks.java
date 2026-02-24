package gift.container;

import io.cucumber.java.Before;
import io.restassured.RestAssured;

public class ContainerHooks {

    @Before
    public void setUp() {
        RestAssured.port = 28080;
    }
}
